package org.starloco.locos.object;

import org.starloco.locos.exchange.ExchangeClient;
import org.apache.mina.core.session.IoSession;
import org.starloco.locos.kernel.Config;
import org.starloco.locos.login.LoginClient;
import org.starloco.locos.login.LoginHandler;
import org.starloco.locos.login.LoginServer;

import java.util.HashMap;
import java.util.Map;

public class Server {

    public static final Map<Integer, Server> servers = new HashMap<>();
    private final int id;
    private int port;
    private int state;
    private final int sub;
    private int freePlaces;
    private String ip;
    private final String key;
    private ExchangeClient client;

    public Server(int id, String key, int sub) {
        this.id = id;
        this.key = key;
        this.state = 0;
        this.sub = sub;
        servers.put(this.id, this);
    }

    public static Server get(int id) {
        if (!servers.containsKey(id))
            return null;
        else
            return servers.get(id);
    }

    private static void sendHostListToAll() {
        LoginServer login = Config.loginServer;
        if (login == null) return;
        for (LoginClient client : login.getClients().values()) {
            if (client == null) continue;
            IoSession session = client.getIoSession();
            if (session != null && (session.isConnected() || !session.isClosing()))
                client.send(getHostList(client.getClientVersion()));
        }
    }

    /** 1.43 format. */
    public static String getHostList() {
        return getHostList(null);
    }

    /**
     * "AH" + servers ("id;state;completion;canLog"), the 1.43 format. Newer
     * clients (1.49.5 at least) read a flag first — "isCoLimited" — then the
     * servers with a 5th field, "isLimited": "AH0|id;state;110;1;0". Given the
     * 1.43 format, they take the first server for that flag and list none.
     */
    public static String getHostList(String clientVersion) {
        boolean newer = isNewerThan143(clientVersion);
        StringBuilder sb = new StringBuilder(newer ? "AH0" : "AH");
        Server.servers.values().stream().filter(server -> server != null).forEach(server ->
                sb.append(sb.length() > 2 ? "|" : "").append(server.getId()).append(";").append(server.getState())
                  .append(";110;1").append(newer ? ";0" : ""));
        return sb.toString();
    }

    /** "1.49.5e" → true, "1.43.7" → false; unknown → false (the server's own format). */
    private static boolean isNewerThan143(String version) {
        if (version == null) return false;
        String[] p = version.replaceAll("[^0-9.]", "").split("\\.");
        try {
            int major = Integer.parseInt(p[0]), minor = p.length > 1 ? Integer.parseInt(p[1]) : 0;
            return major > 1 || (major == 1 && minor > 43);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    public int getId() {
        return id;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
        sendHostListToAll();
    }

    public String getKey() {
        return key;
    }

    public int getSub() {
        return sub;
    }

    public ExchangeClient getClient() {
        return client;
    }

    public void setClient(ExchangeClient client) {
        this.client = client;
    }

    public int getFreePlaces() {
        return freePlaces;
    }

    public void setFreePlaces() {
        this.client.send("F?#");
    }

    public void setFreePlaces(int freePlaces) {
        this.freePlaces = freePlaces;
    }

    public void send(Object arg0) {
        if(this.getClient() == null) return;
        if (arg0 instanceof String) {
            this.getClient().send((String) arg0);
        } else {
            this.getClient().getIoSession().write(arg0);
        }
    }
}
