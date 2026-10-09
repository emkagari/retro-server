package org.starloco.locos.login.packet;

import org.starloco.locos.kernel.Config;
import org.starloco.locos.login.LoginClient;
import org.starloco.locos.login.LoginClient.Status;

class Version {

    public static void verify(LoginClient client, String version) {

        // `system.server.login.version`, as is or with the Electron client's "e".
        String accepted = Config.version.trim();
        if (!version.equalsIgnoreCase(accepted) && !version.equalsIgnoreCase(accepted + "e")) {
            System.out.println("[" + client.getIoSession().getId() + "] The version of the client '" + version + "' is not like the server '" + accepted + "'. The client going to be kicked.");
            client.send("AlEv" + accepted);
            client.kick();
            return;
        }

        client.setStatus(Status.WAIT_ACCOUNT);
    }
}
