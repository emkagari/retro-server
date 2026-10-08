package job.maging;

import game.world.World.Couple;

import java.util.ArrayList;

public class BreakingObject {

    private ArrayList<Couple<Long, Integer>> objects = new ArrayList<>();
    private int count = 0;
    private boolean stop = false;

    public ArrayList<Couple<Long, Integer>> getObjects() {
        return objects;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public boolean isStop() {
        return stop;
    }

    public synchronized int addObject(long id, int quantity) {
        Couple<Long, Integer> couple = this.search(id);

        if (couple == null) {
            this.objects.add(new Couple<>(id, quantity));
            return quantity;
        } else {
            couple.second += quantity;
            return couple.second;
        }
    }

    public synchronized int removeObject(long id, int quantity) {
        Couple<Long, Integer> couple = this.search(id);

        if (couple != null) {
            if (quantity > couple.second) {
                this.objects.remove(couple);
                return quantity;
            } else {
                couple.second -= quantity;
                if (couple.second <= 0) {
                    this.objects.remove(couple);
                    return 0;
                }
                return couple.second;
            }
        }
        return 0;
    }

    public Couple<Long, Integer> search(long id) {
        for (Couple<Long, Integer> couple : this.objects)
            if (couple.first == id)
                return couple;
        return null;
    }

}