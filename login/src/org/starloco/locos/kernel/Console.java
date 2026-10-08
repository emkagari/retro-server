package org.starloco.locos.kernel;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.starloco.locos.login.LoginClient;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class Console extends Thread {

    public static Console instance;
    private static final Logger logger = (Logger) LoggerFactory.getLogger(Console.class);
    private final Scanner scanner = new Scanner(System.in);

    public void initialize() {
        super.setDaemon(true);
        super.start();
        logger.setLevel(Level.ALL);
        this.write(" > Console >");
    }

    @Override
    public void run() {
        while (Config.isRunning) {
            try {
                this.parse(scanner.nextLine());
            } catch (NoSuchElementException ignored) {}
        }
    }

    void parse(String line) {
        if(!line.isEmpty()) {
            char space = ' ';
            String[] infos = line.split(String.valueOf(space));

            if(infos.length > 1) {
                switch (infos[0].toUpperCase()) {
                    case "UPTIME":
                        this.write(EmulatorInfo.uptime());
                        break;
                    case "SEND":
                        String infos1 = infos[1];
                        LoginClient client = Config.loginServer.getClient(Long.parseLong(infos1));
                        client.send(line.substring(5).replace(infos1 + space, ""));
                        this.write("Send : " + line.substring(5).replace(infos1 + space, ""));
                        break;
                    default:
                        this.write(" > Unknown command '" + infos[0].toUpperCase() + "' !");
                        break;
                }
            }

            this.write(" > Console >");
        }
    }

    public void write(String msg) {
        if (!msg.isEmpty())
            logger.info(msg);
    }
}