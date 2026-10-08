package area.map.labyrinth;

import entity.monster.Monster;
import game.world.World;
import kernel.Constant;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Random;

public class Hotomani {

    private static short mapid = -1;

    public static void initialize() {
        initializeBoss();
    }

    private static void initializeBoss() {
        spawnGroupe1();
        spawnGroupe2();
        spawnGroupe3();
        spawnGroupe4();
        spawnGroupe5();
        spawnBoss1();
        spawnBoss2();
        spawnBossBoth();
        spawnGroups();
    }

    public static void spawnGroupe1() {
        mapid = 12010;
        String groupData = "181,53,53;182,80,80;180,200,200;99,43,43";
        // Boss Possible :
        World.world.getMap(mapid).spawnNewGroupWithDifficulty(false,238,groupData,200);
    }

    public static void spawnGroupe2() {
        mapid = 12017;
        String groupData = "251,200,200;263,77,77;261,40,40;112,19,19";
        World.world.getMap(mapid).spawnNewGroupWithDifficulty(false,238,groupData,200);

    }
    public static void spawnGroupe3() {
        mapid = 12000;
        String groupData = "612,240,240;295,140,140;929,198,198;233,125,125";
        World.world.getMap(mapid).spawnNewGroupWithDifficulty(false,238,groupData,200);
    }

    public static void spawnGroupe4() {
        mapid = 12015;
        String groupData = "230,94,94;226,200,200;216,88,88;450,165,165";
        World.world.getMap(mapid).spawnNewGroupWithDifficulty(false,238,groupData,200);

    }
    public static void spawnGroupe5() {
        mapid = 12014;
        String groupData = "854,480,480;2685,220,220;2680,220,220;404,145,145";
        World.world.getMap(mapid).spawnNewGroupWithDifficulty(false,100,groupData,200);

    }

    public static void spawnBoss2() {
        mapid = 12028;
        String groupData = "866,890,890;2786,150,150;373,160,160";
        World.world.getMap(mapid).spawnNewGroupWithDifficulty(false,122,groupData,200);
    }

    public static void spawnBoss1() {
        mapid = 12028;
        String groupData = "865,890,890;2786,150,150;373,160,160";
        World.world.getMap(mapid).spawnNewGroupWithDifficulty(false,121,groupData,200);
    }

    public static void spawnBossBoth() {
        mapid = 12028;
        String groupData = "866,890,890;865,890,890;2786,150,150;373,160,160";
        World.world.getMap(mapid).spawnNewGroupWithDifficulty(false,123,groupData,200);
    }

    private static void spawnGroups() {
        for(int mapid : Constant.HOTOMANI_MAPID){
            do {spawnGroupHotomani((short) mapid);}
            while(World.world.getMap((short)mapid).getMaxGroupNumb()>World.world.getMap((short)mapid).getMobGroups().size());
        }
    }

    public static void spawnGroupHotomani(short Mapid) {
        int min = 1, max = 1 , minArchi = 1 , maxArchi = 1, minBoss = 1 , maxBoss = 1, nbMob = 0;
        boolean hasBoss = false;
        boolean hasArchi = false;
        String groupData = "";

        if(!ArrayUtils.contains(Constant.HOTOMANI_MAPID,Mapid))
            return;

        Random random2 = new Random();
        int randomIndex2 = random2.nextInt(100);
        if(randomIndex2 <= 5){
            hasBoss = true;
        }

        if(randomIndex2 <= 25){
            hasArchi = true;
        }

        ArrayList<Monster.MobGrade> arraypossiblemob =  World.world.getMobgradeBetweenLvl(1,400);

        if(hasBoss){
            ArrayList<Monster.MobGrade> arraypossibleBoss =  World.world.getBossMobgradeBetweenLvl(50,800);
            Random random = new Random();
            int randomIndex = random.nextInt(arraypossibleBoss.size());
            Monster.MobGrade randomInt = arraypossibleBoss.get(randomIndex);
            groupData += ""+randomInt.getTemplate().getId()+","+randomInt.getLevel()+","+randomInt.getLevel()+";";
            nbMob++;

            World.world.sendMessageToAll("(<b>Infos</b>) : Le WorldBoss '<b>" + randomInt.getTemplate().getName() + "</b>' vient d'apparaitre à Hotomani ! Serez-vous à la hauteur pour le vaincre ?");
        }

        if(hasArchi) {
            ArrayList<Monster.MobGrade> arraypossibleArchi =  World.world.getArchiMobgradeBetweenLvl(1,400);
            Random random = new Random();
            int randomIndex = random.nextInt(arraypossibleArchi.size());
            Monster.MobGrade randomInt = arraypossibleArchi.get(randomIndex);
            groupData += ""+randomInt.getTemplate().getId()+","+randomInt.getLevel()+","+randomInt.getLevel()+";";
            nbMob++;
        }

        while(nbMob<4){
            Random random = new Random();
            int randomIndex = random.nextInt(arraypossiblemob.size());
            Monster.MobGrade randomInt = arraypossiblemob.get(randomIndex);
            groupData += ""+randomInt.getTemplate().getId()+","+randomInt.getLevel()+","+randomInt.getLevel()+";";
            arraypossiblemob.remove(randomIndex);
            nbMob++;
        }

        if( World.world.getMap(Mapid).getMobGroups().size() < 3) {
            World.world.getMap(Mapid).spawnNewGroupWithDifficulty(groupData,200);
        }
    }

    public static void spawnOldGroupHotomani(short Mapid, Monster.MobGroup MobList) {
        if(!ArrayUtils.contains(Constant.HOTOMANI_MAPID,Mapid))
            return;

        String groupData = "";
        if(MobList.getMobs().size() > 3) {
            for (Monster.MobGrade mob : MobList.getMobs().values() ) {
                 int grade =  mob.getGrade();
                 int level = mob.getTemplate().getGrade(grade).getLevel();
                 groupData += "" + mob.getTemplate().getId() + "," + level + "," + level + ";";
            }
            if (World.world.getMap(Mapid).getMobGroups().size() < 3) {
                World.world.getMap(Mapid).spawnNewGroupWithDifficulty(groupData, 200);
            }
        }
        else{
            spawnGroupHotomani(Mapid);
        }
    }
}
