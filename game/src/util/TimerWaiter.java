package util;

import game.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class TimerWaiter {


    private static final int numberOfThread = 6;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(numberOfThread, r -> new Thread(r, "TimerWaiter"));

    public static ScheduledFuture<?> addNext(Runnable run, long time, TimeUnit unit) {
        return TimerWaiter.scheduler.schedule(catchRunnable(run), time, unit);
    }

    /*public static void purge() {
        System.out.println("Purge of schedulers...");
        try {
            mapScheduler.purge();
            clientScheduler.purge();
            fightScheduler.purge();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("Purge of schedulers OK !");
        }
    }*/

    public static Runnable catchRunnable(Runnable run) {
        return () -> {
            try {
                run.run();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println(e.getCause().getMessage());


            }
        };
    }

}