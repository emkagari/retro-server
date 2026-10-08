package kernel;

import area.map.GameMap;
import area.map.entity.InteractiveObject;
import ch.qos.logback.classic.Logger;
import database.Database;
import entity.mount.Mount;
import event.EventManager;
import exchange.ExchangeClient;
import game.GameServer;
import game.scheduler.entity.WorldPub;
import game.scheduler.entity.WorldSave;
import game.world.World;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class Main {

    public static final Main INSTANCE = new Main();

    private final List<Runnable> runnables = new LinkedList<>();

    private boolean mapAsBlocked = false;
    private boolean fightAsBlocked = false;
    private boolean tradeAsBlocked = false;

    private static final Logger logger = (Logger) LoggerFactory.getLogger(Main.class);
    private final Thread shutdownThread = new Thread(this::closeServer);

    public static ExchangeClient exchangeClient = null;

    public static void main(String[] args) throws Exception {
        Runtime.getRuntime().addShutdownHook(INSTANCE.shutdownThread);
        INSTANCE.start();
    }

    private void start() throws Exception {
        logger.info("You use " + System.getProperty("java.vendor") + " with the version " + System.getProperty("java.version"));
        logger.debug("Starting of the server : " + new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss", Locale.FRANCE).format(new Date()));
        logger.debug("Current timestamp ms : " + System.currentTimeMillis());
        logger.debug("Current timestamp ns : " + System.nanoTime());

        if (!Database.launchDatabase()) {
            logger.error("An error occurred when the server have try a connection on the Mysql server. Please verify your identification.");
            return;
        }

        Config.INSTANCE.setRunning(true);
        if (!ExchangeClient.INSTANCE.start()) {
            stop("Can't init discussion with login", 3);
            return;
        }

        World.world.createWorld();
        if (!GameServer.INSTANCE.start()) {
            stop("Can't init game server", 2);
            return;
        }

        GameServer.INSTANCE.setState(1);
        logger.info("Server is ready ! Waiting for connection..\n");

        while (Config.INSTANCE.isRunning()) {
            try {
                WorldSave.updatable.update();
                GameMap.updatable.update();
                InteractiveObject.updatable.update();
                Mount.updatable.update();
                //WorldPlayerOption.updatable.update();
                WorldPub.updatable.update();
                EventManager.getInstance().update();

                if (!runnables.isEmpty()) {
                    for (Runnable runnable : new LinkedList<>(runnables)) {
                        try {
                            if (runnable != null) {
                                runnable.run();
                                runnables.remove(runnable);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void closeServer() {
        if (Config.INSTANCE.isRunning()) {
            GameServer.INSTANCE.setState(0);
            GameServer.INSTANCE.kickAll(true);
            WorldSave.cast(0);
            if (!Config.INSTANCE.getHEROIC()) {
                Database.getDynamics().getHeroicMobsGroups().deleteAll();
                /*for (map in World.world.maps) {
                    map.mobGroups.values.filterNot { it.isFix }.forEach { Database.getDynamics().heroicMobsGroups.insert(map.id, it, null) }
                }*/
            }
            Database.getStatics().getServerData().loggedZero();
            //Config.isRunning = false
        }
        GameServer.INSTANCE.stop();
        logger.info("The server is now closed.");
    }

    private void closeServerForPlayers() {
        if (Config.INSTANCE.isRunning()) {
            GameServer.INSTANCE.setState(0);
            Config.INSTANCE.setRunning(true);
            GameServer.INSTANCE.kickAll(true);
            WorldSave.cast(0);
            Database.getStatics().getServerData().loggedZero();
        }

        logger.info("The server is now closed for players.");
    }

    private void openServerForPlayers() {
        if (Config.INSTANCE.isRunning()) {
            GameServer.INSTANCE.setState(1);
            Config.INSTANCE.setRunning(true);
            GameServer.INSTANCE.kickAll(true);
            WorldSave.cast(0);
        }

        logger.info("The server is now open for players.");
    }

    public void stop(String reason) {
        stop(reason, 0);
    }

    public void stop(String reason, int exitCode) {
        logger.error("Start closing server : {}", reason);
        Runtime.getRuntime().removeShutdownHook(shutdownThread);
        closeServer();
        System.exit(exitCode);
    }

    public static void refreshTitle() {
        //if (Main.isRunning)
        //    Main.setTitle(Config.getInstance().NAME + " - Port : " + Main.gamePort + " | " + Main.key + " | " + Main.gameServer.getClients().size() + " Joueur(s)");
    }

    public List<Runnable> getRunnables() {
        return runnables;
    }

    public boolean getMapAsBlocked() {
        return mapAsBlocked;
    }

    public void setMapAsBlocked(boolean mapAsBlocked) {
        this.mapAsBlocked = mapAsBlocked;
    }

    public boolean getFightAsBlocked() {
        return fightAsBlocked;
    }

    public void setFightAsBlocked(boolean fightAsBlocked) {
        this.fightAsBlocked = fightAsBlocked;
    }

    public boolean getTradeAsBlocked() {
        return tradeAsBlocked;
    }

    public void setTradeAsBlocked(boolean tradeAsBlocked) {
        this.tradeAsBlocked = tradeAsBlocked;
    }
}
