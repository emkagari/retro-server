package org.starloco.locos.login.packet;

import org.starloco.locos.kernel.Main;
import org.starloco.locos.login.LoginClient;
import org.starloco.locos.login.LoginClient.Status;
import org.starloco.locos.object.Account;

class ChooseNickName {

    public static void verify(LoginClient client, String nickname) {
        Account account = client.getAccount();

        if (!account.getPseudo().isEmpty()) {
            client.kick();
            return;
        }

        if (nickname.toLowerCase().equals(account.getName().toLowerCase())) {
            client.send("AlEr");
            return;
        }

        String s[] = {"admin", "modo", " ", "&", "é", "\"", "'",
                "(", "-", "è", "_", "ç", "à", ")", "=", "~", "#",
                "{", "[", "|", "`", "^", "@", "]", "}", "°", "+",
                "^", "$", "ù", "*", ",", ";", ":", "!", "<", ">",
                "¨", "£", "%", "µ", "?", ".", "/", "§", "\n", "`"};

        for (String value : s) {
            if (nickname.contains(value)) {
                client.send("AlEs");
                break;
            }
        }

        if (Main.database.getAccountData().exist(nickname) != null) {
            client.send("AlEs");
            return;
        }

        client.getAccount().setPseudo(nickname);
        client.setStatus(Status.SERVER);
        client.getAccount().setState(0);
        AccountQueue.verify(client);
    }
}
