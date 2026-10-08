package entity.exchange;

import client.Player;
import game.world.World;

import java.util.ArrayList;

public abstract class Exchange {

    protected final Player player1, player2;
    protected long kamas1 = 0, kamas2 = 0;
    protected ArrayList<World.Couple<Long, Integer>> items1 = new ArrayList<>(),
            items2 = new ArrayList<>();
    protected boolean ok1, ok2;

    public Exchange(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    public static World.Couple<Long, Integer> getCoupleInList(
            ArrayList<World.Couple<Long, Integer>> items, long id) {
        for (World.Couple<Long, Integer> couple : items)
            if (couple.first == id)
                return couple;
        return null;
    }

    public abstract boolean toogleOk(int id);

    public abstract void apply();

    public abstract void cancel();
}