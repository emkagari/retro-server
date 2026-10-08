package game.scheduler.entity;

import game.scheduler.Updatable;

public class WorldPlayerOption extends Updatable {

    public final static Updatable updatable = new WorldPub(300000);

    public WorldPlayerOption(int wait) {
        super(wait);
    }

    @Override
    public void update() {
        if(this.verify()) {
            //Database.getStatics().getAccountData().updateVoteAll();
            // World.world.getOnlinePlayers().stream().filter(player -> player != null && player.isOnline()).forEach(client.Player::checkVote);
        }
    }

    @Override
    public Object get() {
        return null;
    }
}