package game;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import client.Account;
import client.Player;
import com.sun.istack.NotNull;
import common.SocketManager;
import database.Database;
import exchange.ExchangeClient;
import game.scheduler.entity.WorldSave;
import game.world.World;
import kernel.Config;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static game.world.World.sendWebhookInformationsServeur;

public class GameServer {

    public static short MAX_PLAYERS = 700;
    public static GameServer INSTANCE = new GameServer();

    private final static @NotNull
    ArrayList<Account> waitingClients = new ArrayList<>();
    private final static @NotNull Logger log = (Logger) LoggerFactory.getLogger(GameServer.class);

    private final @NotNull IoAcceptor acceptor;
    private int status = 0;

   // static {

    //}

    private GameServer(){
        NioSocketAcceptor a = new NioSocketAcceptor();
        // SO_REUSEADDR : permet de re-bind immédiatement après stop sans attendre que le port
        // sorte du TIME_WAIT (sinon "Address already in use" sur restart rapide).
        a.setReuseAddress(true);
        acceptor = a;
        TextLineCodecFactory line = new TextLineCodecFactory(StandardCharsets.UTF_8, LineDelimiter.NUL, new LineDelimiter("\n\0"));
        line.setDecoderMaxLineLength(16384);
        acceptor.getFilterChain().addLast("codec",  new ProtocolCodecFilter(line));
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 60 * 10 /*10 Minutes*/);
        acceptor.setHandler(new GameHandler());
    }



    public static String getServerTime() {
        return "BT" + (new Date().getTime() + 3600000 * 2);
    }

    public boolean start() {
        log.setLevel(Level.ALL);
        if (acceptor.isActive()) {
            log.warn("Error already start but try to launch again");
            return false;
        }

        try {
            acceptor.bind(new InetSocketAddress(Config.INSTANCE.getGamePort()));
            log.info("Game server started on address : {}:{}", Config.INSTANCE.getIp(), Config.INSTANCE.getGamePort());
            return true;
        } catch (IOException e) {
            log.error("Error while starting game server", e);
            return false;
        }
    }

    public void stop() {
        // Bug historique : la condition était inversée (`!acceptor.isActive()`) → le
        // cleanup ne se faisait que quand l'acceptor n'était PAS actif. Résultat : le port
        // 5555 restait bound, IOException au prochain start "Address already in use".
        // Ordre correct : ferme les sessions, unbind du port, puis dispose des threads.
        if (acceptor.isActive()) {
            acceptor.getManagedSessions().values().stream()
                    .filter(session -> session.isConnected() || !session.isClosing())
                    .forEach(session -> session.closeNow());
            acceptor.unbind();
            acceptor.dispose();
        }
        sendWebhookInformationsServeur("Le serveur est éteint pour maintenance");
        log.error("The game server was stopped.");
    }

    public static List<GameClient> getClients() {
        return INSTANCE.acceptor.getManagedSessions().values().stream()
                .filter(session -> session.getAttribute("client") != null)
                .map(session -> (GameClient) session.getAttribute("client"))
                .collect(Collectors.toList());
    }

    public static int getPlayersNumberByIp() {
        return (int) getClients().stream().filter(client -> client != null && client.getAccount() != null)
                .map(client -> client.getAccount().getCurrentIp())
                .distinct().count();
    }

    public void setState(int state) {
        this.status = state;
        ExchangeClient.INSTANCE.send("SS" + state);
    }

    public int getState() {
        return this.status;
    }

    public void closeServerForPlayers() {
       this.kickAll(false);
       WorldSave.cast(0);
       this.setState(0);
       Database.getStatics().getServerData().loggedZero();

    }

    public void openServerForPlayers() {
        this.kickAll(false);
        WorldSave.cast(0);
        this.setState(1);
        Database.getStatics().getServerData().loggedZero();
    }

    public static Account getAndDeleteWaitingAccount(int id){
        Iterator<Account> it = waitingClients.listIterator();
        while(it.hasNext()){
            Account account = it.next();
            if(account.getId() == id){
                it.remove();
                return account;
            }
        }
        return null;
    }

    public static void addWaitingAccount(Account account) {
        if(!waitingClients.contains(account)) waitingClients.add(account);
    }

    public static void a(String err) {
        log.warn("Unexpected behaviour detected : "+ err);
    }

    /**
     * Tells every connected client the server is going away ("k"): when the
     * connection then closes, the client goes back to its login screen quietly
     * instead of reporting a lost connection or trying to reconnect.
     */
    public void notifyClosing() {
        for (GameClient client : getClients()) {
            if (client != null)
                SocketManager.send(client, "k");
        }
        try {
            Thread.sleep(500); // for the packets to leave before the sockets close
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    public void kickAll(boolean kickGm) {
        for (Player player : World.world.getOnlinePlayers()) {
            if (player != null && player.getGameClient() != null) {
                if (player.getGroupe() != null && !player.getGroupe().isPlayer() && kickGm)
                    continue;
                Database.getStatics().getPlayerData().update(player);
                player.send("M04");
                player.getGameClient().kick();
            }
        }

        for (Account client : waitingClients){
            if(client.getGameClient() != null) {
                client.getGameClient().kick();
            }
        }
    }
}
