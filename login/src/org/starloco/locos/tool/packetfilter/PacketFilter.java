package org.starloco.locos.tool.packetfilter;

import org.starloco.locos.kernel.Console;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PacketFilter {


    private final int maxConnections;
    private final int restrictedTime;
    private final Map<String, IpInstance> ipInstances = new HashMap<>();
    private boolean safe;

    public PacketFilter() {
        this.maxConnections = 16;
        this.restrictedTime = 8000;
    }

    synchronized boolean safeCheck(String ip) {
        return unSafeCheck(ip);
    }

    boolean unSafeCheck(String ip) {
        IpInstance ipInstance = find(ip);

        if (ipInstance.isBanned()) {
            Console.instance.write("l'ip " + ip + " a tenté de se connecté alors que bannie");
            return false;
        } else {
            ipInstance.addConnection();

            if (ipInstance.getLastConnection() + this.restrictedTime >= System.currentTimeMillis()) {
                if (ipInstance.getConnections() < this.maxConnections)
                    return true;
                else {
                    Console.instance.write("l'ip " + ip + " a été banni pour tentative frauduleuses");
                    ipInstance.ban();
                    return false;
                }
            } else {
                ipInstance.updateLastConnection();
                ipInstance.resetConnections();
            }
            return true;
        }
    }

    public boolean authorizes(String ip) {
        return safe ? safeCheck(ip) : unSafeCheck(ip);
    }

    public PacketFilter activeSafeMode() {
        this.safe = true;
        return this;
    }

    private IpInstance find(String ip) {
        ip = clearIp(ip);

        IpInstance result = ipInstances.get(ip);
        if (result != null)
            return result;

        result = new IpInstance();
        ipInstances.put(ip, result);
        return result;
    }

    private String clearIp(String ip) {
        return ip.contains(":") ? ip.split(":")[0] : ip;
    }
}
