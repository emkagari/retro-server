package org.starloco.locos.login.packet;

import org.starloco.locos.kernel.Config;
import org.starloco.locos.kernel.Main;
import org.starloco.locos.login.LoginClient;
import org.starloco.locos.login.LoginServer;

class AccountName {

    public static void verify(LoginClient client, String name) {
        try {
            client.setAccount(Main.database.getAccountData().load(name.toLowerCase()));
            client.getAccount().setClient(client);
        } catch (Exception e) {
            client.send("AlEf");
            client.kick();
            return;
        }

        if (client.getAccount() == null) {
            client.send("AlEf");
            client.kick();
            return;
        }


        if (Config.loginServer.clients.containsKey(name)) // S'il est d�j� en connexion, on le kick : pas bon, il faut v�rifier le mdp avant
            Config.loginServer.clients.get(name).kick();
        Config.loginServer.clients.put(name, client);
        client.setStatus(LoginClient.Status.WAIT_PASSWORD);
    }
}
