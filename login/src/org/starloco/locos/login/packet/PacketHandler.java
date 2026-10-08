package org.starloco.locos.login.packet;

import org.starloco.locos.kernel.Console;
import org.starloco.locos.kernel.Main;
import org.starloco.locos.login.LoginClient;
import org.starloco.locos.object.Account;

public class PacketHandler {

    private static final String POLICY = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<cross-domain-policy>" +
                "<site-control permitted-cross-domain-policies=\"all\"/>" +
                "<allow-access-from domain=\"*\" to-ports=\"*\" secure=\"false\"/>" +
                "<allow-http-request-headers-from domain=\"*\" headers=\"*\" secure=\"false\"/>" +
            "</cross-domain-policy>";

    public static void parser(LoginClient client, String packet) {
        if (packet.equalsIgnoreCase("<policy-file-request/>")) {
            client.send(POLICY);
            client.getIoSession().close(true);
            return;
        }

        // Character switch ticket flow (1.43.7) : le client envoie "#S\n<ticket>"
        // après la version pour revenir directement en sélection de personnage.
        // On le détecte AVANT le state-machine standard.
        if ("#S".equals(packet)) {
            client.setStatus(LoginClient.Status.WAIT_SWITCH_TICKET);
            return;
        }
        if (client.getStatus() == LoginClient.Status.WAIT_SWITCH_TICKET) {
            handleSwitchTicket(client, packet);
            return;
        }

        switch (client.getStatus()) {
            case WAIT_VERSION: // ok
                Console.instance.write("[" + client.getIoSession().getId() + "] Checking for version '" + packet + "'.");
                String[] test;

                if(packet.contains("|")) {
                    test = packet.split("\\|");
                    packet = test[0];
                }

                Version.verify(client, packet);
                client.setClientVersion(packet);
                break;

            case WAIT_ACCOUNT: // a modifier
                if (packet.length() < 3) {
                    Console.instance.write("[" + client.getIoSession().getId() + "] Sending of packet '" + packet + "' to verify the account. The client going to be kicked.");
                    client.send("AlEf");
                    client.kick();
                    return;
                }

                if (!isValidInput(packet)) {
                    Console.instance.write("[" + client.getIoSession().getId() + "] Sending of packet '" + packet + "' to verify the account. The client going to be kicked.");
                    client.send("AlEf");
                    client.kick();
                    return; // Rejette l'entrée si elle est invalide
                }

                Console.instance.write("[" + client.getIoSession().getId() + "] Verification of account '" + packet + "'.");
                AccountName.verify(client, packet);
                break;

            case WAIT_PASSWORD: // ok
                if (packet.length() < 3) {
                    Console.instance.write("[" + client.getIoSession().getId() + "] Sending of packet '" + packet + "' to verify the password. The client going to be kicked.");
                    client.send("AlEf");
                    client.kick();
                    return;
                }

                Console.instance.write("[" + client.getIoSession().getId() + "] Verification of password '" + packet + "'.");
                Password.verify(client, packet);
                break;

            case WAIT_NICKNAME: // ok
                Console.instance.write("[" + client.getIoSession().getId() + "] Verification of nickname '" + packet + "'.");
                ChooseNickName.verify(client, packet);
                break;

            case SERVER:
                switch (packet.substring(0, 2)) {
                    case "AF":
                        FriendServerList.get(client, packet.substring(2));
                        break;

                    case "Af": // ok
                        AccountQueue.verify(client);
                        break;

                    case "AX":
                        ServerSelected.get(client, packet.substring(2));
                        break;

                    case "Ax":
                        ServerList.get(client);
                        break;

                    case "BA":
                        client.send(packet.substring(2));
                        break;
                    // Modif
                    case "Ap":
                        break;

                    case"Ai":
                        break;

                    default:
                        client.kick();
                        break;
                }
                break;

        }
    }

    public static boolean isValidInput(String input) {
        // Par exemple, on n'autorise que les lettres, chiffres et quelques caractères spéciaux
        return input != null && input.matches("[a-zA-Z0-9_@.-]+");
    }

    /**
     * Valide un ticket de switch (déposé par le game via Exchange "WS")
     * et envoie le client directement en SERVER state pour qu'il accède à la sélection
     * de personnage sans repasser par login/password.
     */
    private static void handleSwitchTicket(LoginClient client, String ticket) {
        Integer accountId = LoginClient.SWITCH_TICKETS.remove(ticket);
        if (accountId == null) {
            Console.instance.write("[" + client.getIoSession().getId() + "] Invalid switch ticket. Falling back to normal auth.");
            client.send("AlEf");
            client.kick();
            return;
        }
        Account account = Main.database.getAccountData().load(accountId);
        if (account == null) {
            Console.instance.write("[" + client.getIoSession().getId() + "] Switch ticket valid but account " + accountId + " missing.");
            client.send("AlEf");
            client.kick();
            return;
        }
        client.setAccount(account);
        account.setClient(client);
        client.setStatus(LoginClient.Status.SERVER);
        Console.instance.write("[" + client.getIoSession().getId() + "] Switch ticket accepted for " + account.getName() + ".");
        // Le client envoie "Af" juste après (getQueuePosition), AccountQueue.verify enverra
        // Ad/Ac/AH/AlK/AQ et la sélection de personnage s'ouvrira.
    }



}
