package org.starloco.locos.login.packet;

import org.starloco.locos.kernel.Config;
import org.starloco.locos.login.LoginClient;
import org.starloco.locos.login.LoginClient.Status;

class Version {

    public static void verify(LoginClient client, String version) {

        // `system.server.login.version` may list several versions: "1.43.7,1.49.5".
        // Each is accepted as is or with the Electron client's "e".
        String[] accepted = Config.version.split(",");
        boolean ok = false;
        for (String v : accepted) {
            v = v.trim();
            if (version.equalsIgnoreCase(v) || version.equalsIgnoreCase(v + "e")) ok = true;
        }
        if (!ok) {
            System.out.println("[" + client.getIoSession().getId() + "] The version of the client '" + version + "' is not like the server '" + Config.version + "'. The client going to be kicked.");
            client.send("AlEv" + accepted[0].trim());
            client.kick();
            return;
        }

        client.setStatus(Status.WAIT_ACCOUNT);
    }
}
