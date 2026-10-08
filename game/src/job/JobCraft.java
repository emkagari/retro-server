package job;

import client.Player;
import common.SocketManager;
import util.TimerWaiter;

import java.util.concurrent.TimeUnit;

public class JobCraft {

    public Player player;
    public JobAction jobAction;
    private int time = 0;
    private boolean itsOk = true;
    private final static short CRAFT_TIME = 200;

    public JobCraft(JobAction jobAction, Player player) {
        this.jobAction = jobAction;
        this.player = player;

        TimerWaiter.addNext(() -> {
            if (itsOk) jobAction.craft(false, -1);
        }, CRAFT_TIME, TimeUnit.MILLISECONDS);

        TimerWaiter.addNext(() -> {
            if (!itsOk) repeat(time+1, time, player);
        }, CRAFT_TIME, TimeUnit.MILLISECONDS);
    }

    public void setAction(int time) {
        this.time = time;
        this.jobAction.broken = false;
        this.itsOk = false;
    }



    public void repeat(final int time1, final int time2, final Player player) {
        this.player.sendMessage("La création est instantannée, cela peut créer un bug de l'interface");
        this.jobAction.player = player;
        this.jobAction.isRepeat = true;
        boolean isOneShotCraft = false;
        if (this.jobAction.broke || this.jobAction.broken || player.getExchangeAction() == null || !player.isOnline()) {
            if (player.getExchangeAction() == null)
                this.jobAction.broken = true;
            if (player.isOnline())
                SocketManager.GAME_SEND_Ea_PACKET(this.jobAction.player, this.jobAction.broken ? "2" : "4");
            this.end();
            return;
        } else {
                SocketManager.GAME_SEND_EA_PACKET(this.jobAction.player, 0 + "");
                this.jobAction.craft(this.jobAction.isRepeat, time1-1);
                isOneShotCraft = true;
        }

        if (time2 <= 0 || isOneShotCraft && !this.jobAction.isMagging() ) this.end();
        else {
            TimerWaiter.addNext(() -> this.repeat(time1, (time2 - 1), player), CRAFT_TIME, TimeUnit.MILLISECONDS);
        }
    }

    public void end() {
        SocketManager.GAME_SEND_Ea_PACKET(this.jobAction.player, "1");
        if (!this.jobAction.data.isEmpty())
            SocketManager.GAME_SEND_EXCHANGE_MOVE_OK_FM(this.jobAction.player, 'O', "+", this.jobAction.data);

        if(this.jobAction.isMagging()) {
            this.jobAction.ingredients.clear();
        }
        this.jobAction.isRepeat = false;
        //this.jobAction.setJobCraft(null);

    }
}