package object.entity;

import common.Formulas;
import game.world.World;
import game.world.World.Couple;
import kernel.Constant;
import object.GameObject;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;

public class SoulStone extends GameObject {

    private ArrayList<Couple<Integer, Integer>> monsters;
    private String mobString;

    public SoulStone(long id, int quantity, int template, int pos, String strStats) {
        super(id, template, quantity, pos, strStats, 0,0,-1);
        this.monsters = new ArrayList<>();
        this.parseStringToStats(strStats);
        if(strStats.contains("|") ) {
            // TODO : a supprimer a terme, permet juste de géré les transitions des pierre d'ame actuel vers le nouveau systeme
            String[] split = strStats.split("\\|");
            String stats = "";
            for (String s : split) {
                try {
                    int monstre = Integer.parseInt(s.split(",")[0]);
                    int grade =  5;
                    stats+= "274#"+grade+"#0#"+ Integer.toHexString(monstre) +",";

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            stats = Formulas.removeLastChar(stats);
            this.setModification();
            this.mobString = stats;
        }
        else if(!strStats.contains("#")){
            String stats = "";

            try {
                int monstre = Integer.parseInt(strStats.split(",")[0]);
                int grade =  5;
                stats+= "274#"+grade+"#0#"+ Integer.toHexString(monstre);

            } catch (Exception e) {
                e.printStackTrace();
            }

            this.setModification();
            this.mobString = stats;

        }
        else{
            this.mobString = strStats;
        }
    }

    public void parseStringToStats(String m) {
        if (!m.equalsIgnoreCase("")) {
            if (this.monsters == null)
                this.monsters = new ArrayList<>();

            // TODO : a supprimer a terme, permet juste de géré les transitions des pierre d'ame actuel vers le nouveau systeme
            if(m.contains("|")){
                String[] split = m.split("\\|");
                for (String s : split) {
                    try {
                        int monstre = Integer.parseInt(s.split(",")[0]);
                        int level =  Integer.parseInt(s.split(",")[1]);
                        Couple<Integer, Integer> couple = new Couple<Integer, Integer>(monstre, level);
                        this.monsters.add(couple);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            else if(!m.contains("#")){
                try {
                    int monstre = Integer.parseInt(m.split(",")[0]);
                    int level =  Integer.parseInt(m.split(",")[1]);
                    Couple<Integer, Integer> couple = new Couple<Integer, Integer>(monstre, level);
                    this.monsters.add(couple);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // Nouveau systeme plus proche du fonctionnement offi  - Integer.toHexString(coupl.first) passe decimal en Hexa / Integer.parseInt(stats[0], 16); passe hexa en decimal
            else{
                String[] split = m.split(",");
                for (String s : split) {
                    int StatsInvok = Integer.parseInt(s.split("#")[0], 16);
                    try {
                        if( StatsInvok == 621 || StatsInvok == 623 || StatsInvok == 628) {
                            int monstre = Integer.parseInt(s.split("#")[3], 16);
                            int level = World.world.getMonstre(monstre).getGrade(Integer.parseInt(s.split("#")[1])).getLevel();
                            Couple<Integer, Integer> couple = new Couple<Integer, Integer>(monstre, level);
                            this.monsters.add(couple);
                        }
                        else{
                            //System.out.println("Etrange pas une stat de pierre d'ame habituel, on ignore");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }

        }
    }

    public String parseStatsString() {
        /*StringBuilder stats = new StringBuilder();
        boolean isFirst = true;
        for (Couple<Integer, Integer> coupl : this.monsters) {
            if (!isFirst)
                stats.append(",");
            try {
                stats.append("26f#0#0#").append(Integer.toHexString(coupl.first));
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            isFirst = false;
        }*/
        return mobString;
    }

    public String parseGroupData() {
        StringBuilder toReturn = new StringBuilder();
        boolean isFirst = true;
        for (Couple<Integer, Integer> curMob : this.monsters) {
            if (!isFirst)
                toReturn.append(";");
            toReturn.append(curMob.first).append(",").append(curMob.second).append(",").append(curMob.second);
            isFirst = false;
        }
        return toReturn.toString();
    }

    public String parseToSave() {
        /*StringBuilder toReturn = new StringBuilder();
        boolean isFirst = true;
        for (Couple<Integer, Integer> curMob : this.monsters) {
            if (!isFirst)
                toReturn.append("|");
            toReturn.append(curMob.first).append(",").append(curMob.second);
            isFirst = false;
        }*/
        return mobString;
    }

    public static boolean isInArenaMap(int id) {
        return ArrayUtils.contains(Constant.ARENA_MAPID,id);
    }
}
