package org.starloco.locos.login.packet;

import org.starloco.locos.kernel.Main;
import org.starloco.locos.login.LoginClient;
import org.starloco.locos.object.Account;
import org.starloco.locos.object.Player;
import org.starloco.locos.object.Server;

class FriendServerList {

    public static void get(LoginClient client, String packet) {
        try {
            String name = Main.database.getAccountData().exist(packet);

            if (name == null) {
                client.send("AF");
                return;
            }

            Account account = Main.database.getAccountData().load(name);

            if (account == null) {
                client.send("AF");
                return;
            }

            Main.database.getPlayerData().load(account);
            client.send("AF" + getList(account));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getList(Account account) {
        StringBuilder sb = new StringBuilder();

        for (Server server : Server.servers.values()) {
            int i = getNumber(account, server.getId());
            if (i != 0)
                sb.append(server.getId()).append(",").append(i).append(";");
        }
        return sb.toString();
    }

    private static int getNumber(Account account, int id) {
        int i = 0;
        for (Player character : account.getPlayers().values())
            if (character.getServer() == id)
                i++;
        return i;
    }
}
