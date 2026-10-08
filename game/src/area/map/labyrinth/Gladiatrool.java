package area.map.labyrinth;

import entity.monster.Monster;
import game.world.World;
import kernel.Constant;
import util.TimerWaiter;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Gladiatrool {

    public static void initialize() {
        initializeGladiatrool();
    }

    public static void respawn(short mapid) {
        TimerWaiter.addNext(() -> spawnGroupGladiatrool(mapid), 5, TimeUnit.SECONDS);
    }

    private static void initializeGladiatrool() {
        spawnGroups();
    }

    private static void spawnGroups() {
        for(short i=15000; i < 15080; i= (short) (i+8)){
            if(Constant.isInGladiatorDonjon(i)){
                    spawnGroupGladiatrool(i);
            }
        }
    }

    public static void spawnGroupGladiatrool(short Mapid) {
        do {
            int min = 1, max = 1, minArchi = 1, maxArchi = 1, minBoss = 1, maxBoss = 1, nbMob = 0;
            boolean hasBoss = false;
            boolean hasArchi = false;
            String groupData = "";
            switch (Mapid) {
                case 15000: // 10 jeton
                    min = 40;
                    max = 51;
                    break;
                case 15008: // 30 jeton
                    min = 50;
                    max = 70;
                    break;
                case 15016: // 70 jeton
                    hasArchi = true;
                    minArchi = 40;
                    maxArchi = 50;
                    min = 60;
                    max = 80;
                    break;
                case 15024: // 130 jeton
                    hasArchi = true;
                    minArchi = 50;
                    maxArchi = 60;
                    min = 80;
                    max = 100;
                    break;
                case 15032: // 220 jeton
                    hasBoss = true;
                    minBoss = 140;
                    maxBoss = 190;
                    min = 90;
                    max = 120;
                    break;
                case 15040: // 340 jeton
                    hasBoss = true;
                    min = 115;
                    max = 140;
                    minBoss = 140;
                    maxBoss = 190;
                    break;
                case 15048: // 500 jeton
                    hasBoss = true;
                    hasArchi = true;
                    minArchi = 90;
                    maxArchi = 110;
                    minBoss = 140;
                    maxBoss = 200;
                    min = 120;
                    max = 170;
                    break;
                case 15056: // 700 jeton
                    hasArchi = true;
                    hasBoss = true;
                    minArchi = 100;
                    maxArchi = 120;
                    minBoss = 140;
                    maxBoss = 440;
                    min = 125;
                    max = 200;
                    break;
                case 15064: // 950 jeton
                    hasBoss = true;
                    hasArchi = true;
                    minArchi = 120;
                    maxArchi = 140;
                    minBoss = 180;
                    maxBoss = 480;
                    min = 130;
                    max = 210;
                    break;
                case 15072: // 1250 jeton
                    hasArchi = true;
                    hasBoss = true;
                    minArchi = 140;
                    maxArchi = 200;
                    minBoss = 440;
                    maxBoss = 1000;
                    min = 170;
                    max = 250;
                    break;
            }

            ArrayList<Monster.MobGrade> arraypossiblemob = World.world.getMobgradeBetweenLvl(min, max);
            ArrayList<Monster.MobGrade> arraypossibleBoss = World.world.getBossMobgradeBetweenLvlGladia(minBoss, maxBoss);
            ArrayList<Monster.MobGrade> arraypossibleArchi = World.world.getArchiMobgradeBetweenLvlGladia(minArchi, maxArchi);

            if (hasBoss) {
                Random random = new Random();
                int randomIndex = random.nextInt(arraypossibleBoss.size());
                Monster.MobGrade randomInt = arraypossibleBoss.get(randomIndex);
                groupData += "" + randomInt.getTemplate().getId() + "," + randomInt.getLevel() + "," + randomInt.getLevel() + ";";
                nbMob++;
            }
            if (hasArchi) {
                Random random = new Random();
                int randomIndex = random.nextInt(arraypossibleArchi.size());
                Monster.MobGrade randomInt = arraypossibleArchi.get(randomIndex);
                groupData += "" + randomInt.getTemplate().getId() + "," + randomInt.getLevel() + "," + randomInt.getLevel() + ";";
                nbMob++;
            }
            while (nbMob < 4) {
                Random random = new Random();
                int randomIndex = random.nextInt(arraypossiblemob.size());
                Monster.MobGrade randomInt = arraypossiblemob.get(randomIndex);
                groupData += "" + randomInt.getTemplate().getId() + "," + randomInt.getLevel() + "," + randomInt.getLevel() + ";";
                arraypossiblemob.remove(randomIndex);
                nbMob++;
            }


            try {
                World.world.getMap(Mapid).spawnGroupGladiatrool(groupData);
            } catch (Exception e) {
                e.printStackTrace();
                spawnGroupGladiatrool(Mapid);
            }
                //World.world.logger.trace("   >> new gladiatrool groupe in " + Mapid + ".");

        }
        while (World.world.getMap(Mapid).getMobGroups().size() > 1);

    }


}
