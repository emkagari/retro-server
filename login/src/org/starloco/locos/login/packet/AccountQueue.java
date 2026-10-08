package org.starloco.locos.login.packet;

import org.starloco.locos.kernel.Config;
import org.starloco.locos.kernel.Console;
import org.starloco.locos.kernel.Main;
import org.starloco.locos.login.LoginClient;
import org.starloco.locos.login.LoginClient.Status;
import org.starloco.locos.object.Account;
import org.starloco.locos.object.Server;

class AccountQueue {

    /**
     * AlEa - None : D�j� en connexion. Veuillez r�essayer AlEb : Connexion
     * refus�e. Ton compte a �t� banni. AlEc - None : Ce compte est d�j�
     * connect� � un serveur de jeu. Veuillez r�essayer. AlEd : Tu viens de
     * d�connecter un personnage utilisant d�j� ce compte. AlEe - None :
     * ATTENTION : Vous devez utilisez vos identifiants ANkama Games ! ... AlEf
     * : Connexion refus�e. Nom de compte ou mot de passe incorrect.
     */

    public static void verify(final LoginClient client) {
        final Account account = client.getAccount(), recentLoad = Main.database.getAccountData().load(account.getName());
        byte state = recentLoad.getState();

        if (Main.database.getAccountData().isBanned(client.getIoSession().getRemoteAddress().toString().replace("/", "").split(":")[0]) || account.isBanned())
            state = 3;

        switch (state) {
            case 0: //disconnected
                Console.instance.write("[" + client.getIoSession().getId() + "] The account " + account.getName() + " is ready to be connected.");
                account.setState(1);
                sendInformation(client);
                break;

            case 1: //in login
                account.setState(0);
                try {
                    // Pour chaque personnage du compte
                    // On v�rifie s'ils sont logged = 1
                    // Si c'est le cas, on retourne le serveur.
                    int logged = Main.database.getPlayerData().isLogged(account);
                    Server server = Server.get(logged);
                    if (logged != 0 && server != null) {
                        Console.instance.write("[" + client.getIoSession().getId() + "] The account " + account.getName() + " has a player connected, he going to be kicked.");
                        client.send("AlEd");
                        Main.database.getAccountData().resetLogged(account.getUUID(), logged);
                        server.send("WK" + account.getUUID() + "#");
                        client.kick();
                        return;
                    }

                    if(server != null) server.send("WK" + account.getUUID() + "#");
                    Console.instance.write("[" + client.getIoSession().getId() + "] Send to the server id " + logged + " to ask the expulsion of the player of account id " + account.getUUID() + ".");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                account.setState(1);
                sendInformation(client);
                return;

            case 2: //in game
                Console.instance.write("[" + client.getIoSession().getId() + "] State to 2. Error to report ! Account id " + account.getUUID());
                account.setState(0);

                int logged = Main.database.getPlayerData().isLogged(account);
                Server server = Server.get(logged);

                if (server != null && logged != 0)
                    server.send("WK" + account.getUUID() + "#");

                client.send("AlEd");
                client.kick();
                return;

            case 3: //banned
                if(recentLoad.getBannedTime() > 0) {
                    long time = recentLoad.getBannedTime() - System.currentTimeMillis();
                    System.out.println(time);
                    if(time < 0) {
                        account.setBanned(false);
                        account.setBannedTime(0);
                        Main.database.getAccountData().update(account);
                        AccountQueue.verify(client);
                        Console.instance.write("[" + client.getIoSession().getId() + "] The account has been unbanned.");
                        return;
                    } else {
                        long minutes = (time / (60 * 1000) % 60) + 1;
                        long hours = time / (60 * 60 * 1000) % 60;
                        long days = time / (24 * 60 * 60 * 1000) % 24;

                        Console.instance.write("[" + client.getIoSession().getId() + "] The account is banned.");
                        client.send("AlEk" + days + "|" + hours + "|" + minutes);
                    }
                } else {
                    Console.instance.write("[" + client.getIoSession().getId() + "] The account is banned.");
                    client.send("AlEb");
                }
                client.kick();
                return;

            default: //unknown
                Console.instance.write("[" + client.getIoSession().getId() + "] Unknown state " + state);
                client.send("AlEf");
                client.kick();
                break;
        }
    }

    private static void sendInformation(LoginClient client) {
        Account account = client.getAccount();

        if (account == null) {
            Console.instance.write("[" + client.getIoSession().getId() + "] The account doesn't exist. The client going to be kicked.");
            client.send("AlEf");
            client.kick();
            return;
        }

        if (account.getPseudo().isEmpty()) {
            Console.instance.write("[" + client.getIoSession().getId() + "] The account " + account.getName() + " have not a pseudo. Send information to set the pseudo.");
            client.send("AlEr");
            client.setStatus(Status.WAIT_NICKNAME);
            return;
        }

        Console.instance.write("[" + client.getIoSession().getId() + "] Sending information to the game server for the account name " + account.getName() + ".");
        Main.database.getPlayerData().load(account);

        client.send("Af0|0|0|1|-1");
        client.send("Ad" + account.getPseudo());
        client.send("Ac0");
        client.send(Server.getHostList());
        client.send("AlK" + (account.isMj() ? 1 : 0)); // packet mj ou non ! :)
        if (account.isMj())
            Console.instance.write("[" + client.getIoSession().getId() + "] The account have a GM Player.");
        client.send("AQ" + account.getQuestion());
    }
}
