package command.administration;

import client.Account;
import client.Player;
import common.SocketManager;
import game.GameClient;
import game.world.World;
import kernel.Config;
import kernel.Main;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class AdminUser {

    private Account account;
    private Player player;
    private GameClient client;

    private boolean timerStart = false;
    private Timer timer;

    public AdminUser(Player player) {
        this.account = player.getAccount();
        this.player = player;
        this.client = player.getAccount().getGameClient();
    }

    public Account getAccount() {
        return account;
    }

    public Player getPlayer() {
        return player;
    }

    public GameClient getClient() {
        return client;
    }

    public boolean isTimerStart() {
        return timerStart;
    }

    public void setTimerStart(boolean timerStart) {
        this.timerStart = timerStart;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public Timer createTimer(final int timer) {
        World.sendWebhookInformations(Config.INSTANCE.getDISCORD_CHANNEL_INFO()," Un reboot a été programmé dans "+timer+ " minutes.",this.getPlayer() );
        ActionListener action = new ActionListener() {
            int time = timer;

            public void actionPerformed(ActionEvent event) {
                time = time - 1;
                if (time == 1)
                    SocketManager.GAME_SEND_Im_PACKET_TO_ALL("115;" + time + " minute");
                else
                    SocketManager.GAME_SEND_Im_PACKET_TO_ALL("115;" + time + " minutes");
                if (time <= 0) Main.INSTANCE.stop("Shutdown by an administrator");
            }
        };
        return new Timer(60000, action);
    }

    // Format 1.43.7 console admin (cf. dofus.aks.Basics.onAuthorizedCommand) :
    //   BAT{type}|{reportFlag}|{commandName}|{message}
    //   - type        : 0 = log, 1 = erreur, 2 = info
    //   - reportFlag  : 0 = pas de lien report
    //   - commandName : nom de la commande (vide accepté)
    //   - message     : contenu, peut contenir des '|' (joinés client-side)
    public void sendMessage(String message) {
        this.player.send(buildConsolePacket(0, message));
    }

    public void sendErrorMessage(String message) {
        this.player.send(buildConsolePacket(1, message));
    }

    public void sendSuccessMessage(String message) {
        this.player.send(buildConsolePacket(2, message));
    }

    private static String buildConsolePacket(int type, String message) {
        if (message == null) message = "";
        return "BAT" + type + "|0||" + message.replace("\r", "").replace("\n", "");
    }

    public abstract void apply(String packet);

}