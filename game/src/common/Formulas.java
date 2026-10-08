package common;

import area.map.GameMap;
import client.Player;
import fight.Fight;
import fight.Fighter;
import fight.spells.Effect;
import fight.spells.EffectConstant;
import game.world.World.Couple;
import guild.GuildMember;
import kernel.Config;
import kernel.Constant;
import object.GameObject;
import org.apache.commons.lang3.ArrayUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

public class Formulas {

    public final static Random random = new Random();

    public static int countCell(int i) {
        if (i > 64)
            i = 64;
        return 2 * (i) * (i + 1);
    }

    public static int getRandomValue(int i1, int i2) {
        if (i2 < i1)
            return 0;
        return (random.nextInt((i2 - i1) + 1)) + i1;
    }

    public static int getMinJet(String jet) {
        int num = 0;
        try {
            int des = Integer.parseInt(jet.split("d")[0]);
            int add = Integer.parseInt(jet.split("d")[1].split("\\+")[1]);
            num = des + add;
            return num;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private final static short[] order = new short[] {

            EffectConstant.STATS_ADD_PA,
            EffectConstant.STATS_ADD_PM,
            EffectConstant.STATS_ADD_PO,
            EffectConstant.STATS_ADD_VITA,
            EffectConstant.STATS_ADD_AGIL,
            EffectConstant.STATS_ADD_CHAN,
            EffectConstant.STATS_ADD_FORC,
            EffectConstant.STATS_ADD_INTE,
            EffectConstant.STATS_ADD_SAGE,
            EffectConstant.STATS_ADD_CC,
            EffectConstant.STATS_ADD_DOMA,
            EffectConstant.STATS_ADD_PERDOM,
            EffectConstant.STATS_MULTIPLY_DOMMAGE,
            EffectConstant.STATS_ADD_INIT,


    };

    /**
     * Permet de trier les stats d'un item template et d'un item cree. Il faut lui donner uniquement les stats normaux et non les autres.
     * Les stats qui ne sont pas specifie dans le tableaux order seront mis a la fin par ordre croissant selon leur stat ID
     *
     * @author Sarazar928Ghost Kevin#6537
     * Stats de l'objet
     * @return Stats sort by order
     */
    public static String sortStatsByOrder(final String strStats) {

        if(strStats.isEmpty()) return strStats;

        final Map<Byte, String> orderStats = new TreeMap<>();
        final Map<Integer, String> unknowStats = new TreeMap<>();

        for(final String stat : strStats.split(",")) {
            final int id = Integer.parseInt(stat.split("#")[0], 16);

            boolean ok = false;

            for(byte i = 0; i < order.length; ++i) {
                if(order[i] != id) continue;
                orderStats.put(i, stat);
                ok = true;
                break;
            }

            if(ok) continue;

            unknowStats.put(id, stat);

        }


        final StringBuilder theReturn = new StringBuilder();
        boolean isFirst = true;

        for(final byte number : orderStats.keySet())
        {

            if(!isFirst)
                theReturn.append(",");
            isFirst = false;

            theReturn.append(orderStats.get(number));

        }

        for(final String stat : unknowStats.values()) {

            if(!isFirst)
                theReturn.append(",");
            isFirst = false;

            theReturn.append(stat);
        }

        return theReturn.toString();

    }


    public static int getRandomJetWithRarity(int min ,int max, int rarity,boolean positifStats)//1d5+6
    {
        try {
            int num = 0;
            switch (rarity) {
                // c'est un jet rare
                case 2 : {
                    // Stat positive a boost
                    if(positifStats) {
                        if (min < max) {
                            min = (int) Math.floor(min + ((max - min) * 0.75));
                            num = getRandomValue(min, max);
                        } else {
                            num = min;
                        }
                    }// Stat negative a limité
                    else{
                        if (min < max) {
                            max = (int) Math.round(min + ((max - min) * 0.25));
                            num = getRandomValue(min, max);
                        } else {
                            num = min;
                        }
                    }
                    break;
                }
                case 3 : {
                    // pas géré ici car simple jet parfait
                    break;
                }
                // C'est un jet legendaire
                case 4 : {
                    // Stat positive a boost
                    if(positifStats) {
                        if (min < max) {
                            min = (int) Math.floor(min + ((max - min)*0.75));
                            max = (int) Math.floor(max *1.5);
                            num = getRandomValue(min, max);
                        }
                        else if (min == max) {
                            min = max;
                            max = (int) Math.floor(max *1.5);
                            num = getRandomValue(min, max);
                        }
                        else {
                            num = min;
                        }
                    }
                    // Stat negative a limité
                    else{
                        if (min < max) {
                            max = (int) Math.floor(min + ((max - min)*0.25));
                            min = (int) Math.round(min - ((min) *0.50));
                            num = getRandomValue(min, max);
                        }
                        else if (min == max) {
                            min = max;
                            min = (int) Math.round(min - ((min) *0.50));
                            num = getRandomValue(min, max);
                        }
                        else {
                            num = (int) Math.round(min - ((min) *0.50));
                        }
                    }
                    break;
                }
                // C'est un jet primal
                case 5 : {
                    if(positifStats) {
                        if (min < max) {
                            max = (int) Math.floor(max + ((max) * 0.5));
                            num = max;
                        }
                        else {
                            num = (int) Math.floor(min + ((min) * 0.5));
                        }
                    }
                    else{
                        num = (int) Math.round(min - ((min) * 0.50));
                    }
                    break;
                }
            }

            return num;
        } catch (NumberFormatException e) {
            //World.world.logger.trace("New item 2: "+e);
            e.printStackTrace();
            return 0;
        }
    }

    public static int getMaxJet(String jet) {
        int num = 0;
        try {
            int des = Integer.parseInt(jet.split("d")[0]);
            int faces = Integer.parseInt(jet.split("d")[1].split("\\+")[0]);
            int add = Integer.parseInt(jet.split("d")[1].split("\\+")[1]);
            for (int a = 0; a < des; a++) {
                num += faces;
            }
            num += add;
            return num;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int getAlteredJet(int jet,int max, Fighter target, Fighter caster)//1d5+6
    {
        // Si le lanceur a POISSE ON PRIO
        if(target != null && caster.hasBuff(EffectConstant.EFFECTID_MINIMUMDAMAGE))
            return jet;

        // Pour les valeurs fixes
        if(max == -1){
            return jet;
        }
        if(max <= jet)
            return jet;

        // Si la cible a BROKLE on maximise le jet
        if(target != null && target.hasBuff(EffectConstant.EFFECTID_MAXIMUMDAMAGE)){
            return max;
        }

        return jet;
    }

    public static int getRandomJet(int min,int max, Fighter caster)//1d5+6
    {
        // Pour les valeurs fixes
        if(max == -1){
            return min;
        }
        if(max <= min)
            return min;

        // Gestion
        if(caster != null && caster.hasBuff(EffectConstant.EFFECTID_MINIMUMDAMAGE)){
            return min;
        }
        else {
            try {
                // je trouve ca plus simple
                Random random = new Random();
                return random.nextInt(max - min + 1) + min;

            } catch (NumberFormatException e) {
                e.printStackTrace();
                return min;
            }
        }
    }

    public static double getWeightByStat(int effectID){
        double weight = 0;
        //Familier
        switch (effectID){
            case EffectConstant.STATS_ADD_VITA:
                weight= 0.2; // + 600 PP Max
                break;
            case EffectConstant.STATS_ADD_PROS:
                weight= 1.2; // + 100 PP Max
                break;
            case EffectConstant.STATS_ADD_PODS:
                weight= 0.02; // + 6000 pods Max
                break;
            case EffectConstant.STATS_ADD_INIT:
                weight= 0.05; // + 2400 Ini Max
                break;
            case EffectConstant.STATS_ADD_INTE:
            case EffectConstant.STATS_ADD_FORC:
            case EffectConstant.STATS_ADD_CHAN:
            case EffectConstant.STATS_ADD_AGIL:
                weight= 1; // + 120 stats Max
                break;
            case EffectConstant.STATS_ADD_PERDOM:
                weight= 1.2; // + 100 stats Max
                break;
            case EffectConstant.STATS_ADD_SOIN:
            case EffectConstant.STATS_ADD_DOMA:
            case EffectConstant.STATS_ADD_DOMA2:
            case EffectConstant.STATS_RETDOM:
                weight= 8; // + 15 soin/do Max/renvoieDO
                break;
            case EffectConstant.STATS_ADD_RP_AIR:
            case EffectConstant.STATS_ADD_RP_EAU:
            case EffectConstant.STATS_ADD_RP_FEU:
            case EffectConstant.STATS_ADD_RP_NEU:
            case EffectConstant.STATS_ADD_RP_TER:
                weight= 4; // + 30 % res Max
                break;
            case EffectConstant.STATS_ADD_SAGE:
                weight= 3; // + 80 sag Max
                break;
            case EffectConstant.STATS_ADD_FINALDMG:
                weight= 10; // + 12% dommage finaux Max
                break;
            case EffectConstant.STATS_ADD_CC:
                weight= 12; // + 10 CC
                break;
            case EffectConstant.STATS_ADD_PA:
                weight= 90; // + 1 PA Max
                break;
            case EffectConstant.STATS_ADD_PO:
                weight= 40; // + 1 PM Max
                break;
            case EffectConstant.STATS_ADD_PM:
                weight= 70; // + 1 PM Max
                break;
            case Constant.STATS_PETS_PDV:
            case Constant.STATS_PETS_POIDS:
            case Constant.STATS_PETS_REPAS:
            case Constant.STATS_PETS_DATE:
            case Constant.STATS_PETS_EPO:
            case Constant.STATS_PETS_SOUL: // Pas de poid
            default:
                break;
        }

        return weight;
    }

    public static int getRandomJet(String jet)//1d5+6
    {
        try {
            int num = 0;
            int splited = jet.split("d").length;
            if (jet.split("d").length > 1) {
                int des = Integer.parseInt(jet.split("d")[0]);
                int faces = Integer.parseInt(jet.split("d")[1].split("\\+")[0]);
                int add = Integer.parseInt(jet.split("d")[1].split("\\+")[1]);
                if (faces == 0 && add == 0) {
                    num = getRandomValue(0, des);
                } else {
                    for (int a = 0; a < des; a++) {
                        num += getRandomValue(1, faces);
                    }
                }
                num += add;
                return num;
            }
            return Integer.parseInt(jet);
        } catch (NumberFormatException e) {
            System.out.println("Pas de valeur approprié pour le jet + "+jet + " :"+e.getMessage());
            return -1;
        }
    }

    // Method to check if adding two integers would overflow
    public static boolean willIntAdditionOverflow(int a, int b) {
        // Check if both are positive and result is negative
        if (a > 0 && b > 0 && (a > Integer.MAX_VALUE - b)) {
            return true;
        }
        // Check if both are negative and result is positive
        if (a < 0 && b < 0 && (a < Integer.MIN_VALUE - b)) {
            return true;
        }
        return false;
    }

    // Method to check if adding two integers would overflow
    public static boolean willLongAdditionOverflow(long a, long b) {
        // Check if both are positive and result is negative
        if (a > 0 && b > 0 && (a > Long.MAX_VALUE - b)) {
            return true;
        }
        // Check if both are negative and result is positive
        if (a < 0 && b < 0 && (a < Long.MIN_VALUE - b)) {
            return true;
        }
        return false;
    }



    public static int getMiddleJet(Effect ss)//Corrigé pour 0d0+XXXX
    {
        String jet = ss.getJet();
        int num = 0;

        if(jet.equals("")){
            if(ss.getArgs1() == ss.getArgs2())
                num = ss.getArgs1();
            else if(ss.getArgs1() > ss.getArgs2() )
                num = ss.getArgs1() ;
            else if(ss.getArgs1() < ss.getArgs2()){
                num = Math.round(ss.getArgs1()+ss.getArgs2()/2);
            }
            else{
                num = ss.getArgs1();
            }
            return num;
        }
        else if (jet.equals("0d0*")) {
            String[] test = jet.split("\\+");
            num = Integer.parseInt(test[1]);
            return num ;
        }
        else {
            try {
                int des = Integer.parseInt(jet.split("d")[0]);
                int faces = Integer.parseInt(jet.split("d")[1].split("\\+")[0]);
                int add = Integer.parseInt(jet.split("d")[1].split("\\+")[1]);

                if (des > 0) {
                    num += ((1 + faces) / 2) * des;//on calcule moyenne
                    num += add;
                } else { // AJOUT : Si valeur fixe
                    num = add;
                }
                return num;
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return num;
            }
        }
    }

    public static int getTacleChance(Fighter fight, Fighter fighter) {
        int agiTacleur = fight.getTotalStats().getEffect(EffectConstant.STATS_ADD_AGIL);
        int agiEnemi = fighter.getTotalStats().getEffect(EffectConstant.STATS_ADD_AGIL);
        if(fighter.isInvocation()){
            if(fighter.getMob() != null && fighter.getMob().getTemplate() != null) {
                if(ArrayUtils.contains(Constant.STATIC_INVOCATIONS,fighter.getMob().getTemplate().getId())){
                    return 100;
                }
            }
        }

        int div = agiTacleur + agiEnemi + 50;
        if (div == 0)
            div = 1;
        int esquive = 300 * (agiTacleur + 25) / div - 100;
        return esquive;
    }


    public static int calculFinalHeal(Fighter healer, int rank, boolean isCac, int spellId) {
        int intel = healer.getTotalStats().getEffect(EffectConstant.STATS_ADD_INTE);
        int heals = healer.getTotalStats().getEffect(EffectConstant.STATS_ADD_SOIN) - healer.getTotalStats().getEffect(EffectConstant.STATS_REM_SOIN);
        if (intel < 0)
            intel = 0;
        float adic = 100; // TODO : petit bonus 100 normalement (ca boost un peu les heal)
        if (isCac)
            adic = 100;

        // On le met ici !
        if(!isCac) {
            if (healer.hasBuff(EffectConstant.STATS_SPELL_ADD_HEAL)) {
                if (spellId == healer.getBuff(EffectConstant.STATS_SPELL_ADD_HEAL).getFixvalue()) {
                    int value = healer.getBuff(EffectConstant.STATS_SPELL_ADD_HEAL).getArgs2();
                    heals += value;
                }
            }
        }

        return (int) (rank * ((100.00 + intel) / adic) + heals);
    }

    public static int calculXpWinCraft(int lvl, int numCase) {
        if (lvl == 100)
            return 0;
        switch (numCase) {
            case 1:
                if (lvl < 40)
                    return 1;
                return 0;
            case 2:
                if (lvl < 60)
                    return 10;
                return 0;
            case 3:
                if (lvl > 9 && lvl < 80)
                    return 25;
                return 0;
            case 4:
                if (lvl > 19)
                    return 50;
                return 0;
            case 5:
                if (lvl > 39)
                    return 100;
                return 0;
            case 6:
                if (lvl > 59)
                    return 250;
                return 0;
            case 7:
                if (lvl > 79)
                    return 500;
                return 0;
            case 8:
                if (lvl > 99)
                    return 1000;
                return 0;
        }
        return 0;
    }

    public static int calculXpWinFm(int lvl, float poid) {
        if (lvl <= 1) {
            if (poid <= 10)
                return 10;
            else if (poid <= 50)
                return 25;
            else
                return 50;
        }
        if (lvl <= 25) {
            if (poid <= 10)
                return 10;
            else
                return 50;
        } else if (lvl <= 50) {
            if (poid <= 1)
                return 10;
            if (poid <= 10)
                return 25;
            if (poid <= 50)
                return 50;
            else
                return 100;
        } else if (lvl <= 75) {
            if (poid <= 3)
                return 25;
            if (poid <= 10)
                return 50;
            if (poid <= 50)
                return 100;
            else
                return 250;
        } else if (lvl <= 100) {
            if (poid <= 3)
                return 50;
            if (poid <= 10)
                return 100;
            if (poid <= 50)
                return 250;
            else
                return 500;
        } else if (lvl <= 125) {
            if (poid <= 3)
                return 100;
            if (poid <= 10)
                return 250;
            if (poid <= 50)
                return 500;
            else
                return 1000;
        } else if (lvl <= 150) {
            if (poid <= 10)
                return 250;
            else
                return 1000;
        } else if (lvl <= 175) {
            if (poid <= 1)
                return 250;
            if (poid <= 10)
                return 500;
            else
                return 1000;
        } else {
            if (poid <= 1)
                return 500;
            else
                return 1000;
        }
    }

    public static int calculXpLooseCraft(int lvl, int numCase) {
        if (lvl == 100)
            return 0;
        switch (numCase) {
            case 1:
                if (lvl < 40)
                    return 1;
                return 0;
            case 2:
                if (lvl < 60)
                    return 5;
                return 0;
            case 3:
                if (lvl > 9 && lvl < 80)
                    return 12;
                return 0;
            case 4:
                if (lvl > 19)
                    return 25;
                return 0;
            case 5:
                if (lvl > 39)
                    return 50;
                return 0;
            case 6:
                if (lvl > 59)
                    return 125;
                return 0;
            case 7:
                if (lvl > 79)
                    return 250;
                return 0;
            case 8:
                if (lvl > 99)
                    return 500;
                return 0;
        }
        return 0;
    }

    public static int calculHonorWin(ArrayList<Fighter> winner,
                                     ArrayList<Fighter> looser, Fighter F) {
        float totalGradeWin = 0;
        float totalLevelWin = 0;
        float totalGradeLoose = 0;
        float totalLevelLoose = 0;
        boolean Prisme = false;
        int fighters = 0;
        for (Fighter f : winner) {
            if (f.getPlayer() == null && f.getPrism() == null)
                continue;
            if (f.getPlayer() != null) {
                totalLevelWin += f.getLvl();
                totalGradeWin += f.getPlayer().getGrade();
            } else {
                Prisme = true;
                totalLevelWin += (f.getPrism().getLevel() * 15 + 80);
                totalGradeWin += f.getPrism().getLevel();
            }
        }
        for (Fighter f : looser) {
            if (f.getPlayer() == null && f.getPrism() == null)
                continue;
            if (f.getPlayer() != null) {
                totalLevelLoose += f.getLvl();
                totalGradeLoose += f.getPlayer().getGrade();
                fighters++;
            } else {
                Prisme = true;
                totalLevelLoose += (f.getPrism().getLevel() * 15 + 80);
                totalGradeLoose += f.getPrism().getLevel();
            }
        }
        if (!Prisme)
            if (totalLevelWin - totalLevelLoose > 15 * fighters)
                return 0;
        int base = (int) (100 * ((totalGradeLoose * totalLevelLoose) / (totalGradeWin * totalLevelWin)))
                / winner.size();
        if (Prisme && base <= 0)
            return 100;
        if (looser.contains(F))
            base = -base;
        return base * Config.INSTANCE.getRATE_HONOR();
    }

    public static String removeLastChar(String s) {
        return (s == null || s.length() == 0)
                ? null
                : (s.substring(0, s.length() - 1));
    }

    public static int stringToInt(String str,boolean hexa) {
        // Check if the string is empty
        if (str.isEmpty()) {
            return 0;
        }

        // Convert the string to int
        try {
            if(hexa) {
                return Integer.parseInt(str,16);
            }
            else{
                return Integer.parseInt(str);
            }
        } catch (NumberFormatException e) {
            // Handle the case where the string is not a valid integer
            e.printStackTrace();
            //System.err.println("Invalid input: " + str);
            return 0; // or throw an exception if appropriate
        }
    }

    public static int calculFinalDommagee(Fight fight, Fighter caster,
                                         Fighter target, int statID, int jet, boolean isHeal, boolean isCaC,
                                         int spellid) {
        // if (target.hasBuff(788) && target.getBuff(788).getValue() == 101)

        if(!isCaC){
            //Si le sort est boost� par un buff sp�cifique
            for (Effect SE : caster.getBuffsByEffectID(EffectConstant.STATS_SPELL_ADD_BASE_DAMAGE)) {
                if (SE.getArgs1() == spellid) {
                    int add = -1;
                    try {
                        add = SE.getArgs3();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (add <= 0)
                        continue;
                    jet += add;
                }
            }
        }

        float i = 0;//Bonus maitrise
        float j = 100; //Bonus de Classe
        float a = 1;//Calcul
        float num = 0;
        float statC = 0, domC = 0, perdomC = 0, resfT = 0, respT = 0, mulT = 1;
        int multiplier = 0;
        if (!isHeal) {
            domC = caster.getTotalStats().getEffect(EffectConstant.STATS_ADD_DOMA);
            domC +=	caster.getTotalStats().getEffect(EffectConstant.STATS_ADD_DOMA2);

            perdomC = caster.getTotalStats().getEffect(EffectConstant.STATS_ADD_PERDOM);
            multiplier = caster.getTotalStats().getEffect(EffectConstant.STATS_MULTIPLY_DOMMAGE);
            if (caster.hasBuff(EffectConstant.STATS_MULTIPLY_DOMMAGE))
                mulT = caster.getBuffValue(EffectConstant.STATS_MULTIPLY_DOMMAGE);
        } else {
            domC = caster.getTotalStats().getEffect(EffectConstant.STATS_ADD_SOIN);
        }

        switch (statID) {
            case Constant.ELEMENT_NULL://Fixe
                statC = 0;
                resfT = 0;
                respT = 0;
                respT = 0;
                mulT = 1;
                break;
            case Constant.ELEMENT_NEUTRE://neutre
                statC = caster.getTotalStats().getEffect(EffectConstant.STATS_ADD_FORC);
                resfT = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_R_NEU);
                respT = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_NEU);
                if (caster.getPlayer() != null)//Si c'est un joueur
                {
                    respT += target.getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_PVP_NEU);
                    resfT += target.getTotalStats().getEffect(EffectConstant.STATS_ADD_R_PVP_NEU);
                }
                //on ajoute les dom Physique
                domC += caster.getTotalStats().getEffect(EffectConstant.STATS_ADD_PDOM);
                //Ajout de la resist Physique
                resfT = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_PRED);
                break;
            case Constant.ELEMENT_TERRE://force
                statC = caster.getTotalStats().getEffect(EffectConstant.STATS_ADD_FORC);
                resfT = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_R_TER);
                respT = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_TER);
                if (caster.getPlayer() != null)//Si c'est un joueur
                {
                    respT += target.getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_PVP_TER);
                    resfT += target.getTotalStats().getEffect(EffectConstant.STATS_ADD_R_PVP_TER);
                }
                //on ajout les dom Physique
                domC += caster.getTotalStats().getEffect(EffectConstant.STATS_ADD_PDOM);
                //Ajout de la resist Physique
                resfT += target.getTotalStats().getEffect(EffectConstant.STATS_ADD_PRED);
                break;
            case Constant.ELEMENT_EAU://chance
                statC = caster.getTotalStats().getEffect(EffectConstant.STATS_ADD_CHAN);
                resfT = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_R_EAU);
                respT = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_EAU);
                if (caster.getPlayer() != null)//Si c'est un joueur
                {
                    respT += target.getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_PVP_EAU);
                    resfT += target.getTotalStats().getEffect(EffectConstant.STATS_ADD_R_PVP_EAU);
                }
                //Ajout de la resist Magique
                resfT += target.getTotalStats().getEffect(EffectConstant.STATS_ADD_MRED);
                break;
            case Constant.ELEMENT_FEU://intell
                statC = caster.getTotalStats().getEffect(EffectConstant.STATS_ADD_INTE);
                resfT = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_R_FEU);
                respT = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_FEU);
                if (caster.getPlayer() != null)//Si c'est un joueur
                {
                    respT += target.getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_PVP_FEU);
                    resfT += target.getTotalStats().getEffect(EffectConstant.STATS_ADD_R_PVP_FEU);
                }
                //Ajout de la resist Magique
                resfT += target.getTotalStats().getEffect(EffectConstant.STATS_ADD_MRED);
                break;
            case Constant.ELEMENT_AIR://agilit�
                statC = caster.getTotalStats().getEffect(EffectConstant.STATS_ADD_AGIL);
                resfT = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_R_AIR);
                respT = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_AIR);
                if (caster.getPlayer() != null)//Si c'est un joueur
                {
                    respT += target.getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_PVP_AIR);
                    resfT += target.getTotalStats().getEffect(EffectConstant.STATS_ADD_R_PVP_AIR);
                }
                //Ajout de la resist Magique
                resfT += target.getTotalStats().getEffect(EffectConstant.STATS_ADD_MRED);

                break;
        }


        //On bride la resistance a 50% si c'est un joueur
        if (target.getMob() == null && respT > 50)
            respT = 50;

        if (statC < 0)
            statC = 0;
        if (caster.getPlayer() != null && isCaC) {
            int ArmeType = caster.getPlayer().getObjetByPos(1).getTemplate().getType();

            if ((caster.getSpellValueBool(392)) && ArmeType == 2)//ARC
            {
                i = caster.getMaitriseDmg(392);
            }
            if ((caster.getSpellValueBool(390)) && ArmeType == 4)//BATON
            {
                i = caster.getMaitriseDmg(390);
            }
            if ((caster.getSpellValueBool(391)) && ArmeType == 6)//EPEE
            {
                i = caster.getMaitriseDmg(391);
            }
            if ((caster.getSpellValueBool(393)) && ArmeType == 7)//MARTEAUX
            {
                i = caster.getMaitriseDmg(393);
            }
            if ((caster.getSpellValueBool(394)) && ArmeType == 3)//BAGUETTE
            {
                i = caster.getMaitriseDmg(394);
            }
            if ((caster.getSpellValueBool(395)) && ArmeType == 5)//DAGUES
            {
                i = caster.getMaitriseDmg(395);
            }
            if ((caster.getSpellValueBool(396)) && ArmeType == 8)//PELLE
            {
                i = caster.getMaitriseDmg(396);
            }
            if ((caster.getSpellValueBool(397)) && ArmeType == 19)//HACHE
            {
                i = caster.getMaitriseDmg(397);
            }
            a = (((100 + i) / 100) * (j / 100));
        }


        num = a * mulT * (jet * ((100 + statC + perdomC + (multiplier * 100)) / 100))
                + domC;//d�gats bruts

        if (num < 0){
            num = 0;
        }

        //Poisons
        if (spellid != -1) {
            switch (spellid) {

                case 66:
                    statC = caster.getTotalStats().getEffect(EffectConstant.STATS_ADD_AGIL);
                    num = (jet * ((100 + statC + perdomC + (multiplier * 100)) / 100)) + domC;
                    if (target.hasBuff(105) && spellid != 71) {
                        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 105, caster.getId() + "", target.getId() + "," + target.getBuff(105).getFixvalue(), 105);
                        int value = (int) num - target.getBuff(105).getFixvalue();
                        return value > 0 ? value : 0 ;
                    }
                    if (target.hasBuff(184) && spellid != 71) {
                        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 105, caster.getId() + "", target.getId() + "," + target.getBuff(184).getFixvalue(), 105);
                        int value = (int) num - target.getBuff(184).getFixvalue();
                        return value > 0 ? value : 0 ;
                    }
                    //return (int) num;
                    break;
                case 71:
                case 196:
                case 219:
                    statC = caster.getTotalStats().getEffect(EffectConstant.STATS_ADD_FORC);
                    num = (jet * ((100 + statC + perdomC + (multiplier * 100)) / 100)) + domC;
                    if (target.hasBuff(105) && spellid != 71) {
                        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 105, caster.getId() + "", target.getId() + "," + target.getBuff(105).getFixvalue(), 105);
                        int value = (int) num - target.getBuff(105).getFixvalue();
                        return value > 0 ? value : 0 ;
                    }
                    if (target.hasBuff(184) && spellid != 71) {
                        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 105, caster.getId() + "", target.getId() + "," + target.getBuff(184).getFixvalue(), 105);
                        int value = (int) num - target.getBuff(184).getFixvalue();
                        return value > 0 ? value : 0 ;
                    }
                    //return (int) num;
                    break;
                case 181:
                case 200:
                    statC = caster.getTotalStats().getEffect(EffectConstant.STATS_ADD_INTE);
                    num = (jet * ((100 + statC + perdomC + (multiplier * 100)) / 100)) + domC;
                    if (target.hasBuff(105) && spellid != 71) {
                        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 105, caster.getId() + "", target.getId() + "," + target.getBuff(105).getFixvalue(), 105);
                        int value = (int) num - target.getBuff(105).getFixvalue();
                        return value > 0 ? value : 0 ;
                    }
                    if (target.hasBuff(184) && spellid != 71) {
                        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 105, caster.getId() + "", target.getId() + "," + target.getBuff(184).getFixvalue(), 105);
                        int value = (int) num - target.getBuff(184).getFixvalue();
                        return value > 0 ? value : 0 ;
                    }
                    //return (int) num;
                    break;
            }
        }

        //Renvoie
        if (caster.getId() != target.getId()) {
            int renvoie = target.getTotalStatsLessBuff().getEffect(EffectConstant.STATS_RETDOM);
            if (renvoie > 0 && !isHeal) {
                if (renvoie > num)
                    renvoie = (int) num;
                num -= renvoie;
                SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 107, "-1", target.getId()
                        + "," + renvoie, 107);
                if (renvoie > caster.getPdv())
                    renvoie = caster.getPdv();
                if (num < 1)
                    num = 0;

                if (caster.getPdv() <= renvoie) {
                    caster.removePdv(caster, renvoie);
                    //fight.onFighterDie(caster, caster);
                } else
                    caster.removePdv(caster, renvoie);

                SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
                        + "", caster.getId() + ",-" + renvoie, 107);
            }
        }


        //Armures feca
        int reduc = (int) ((num / (float) 100) * respT);//Reduc %resis
        if (!isHeal)
            num -= reduc;
        int armor = getArmorResist(target, statID);
        if (!isHeal)
            num -= armor;
        if (!isHeal)
            if (armor > 0)
                SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 105, caster.getId()
                        + "", target.getId() + "," + armor);

        if (!isHeal)
            num -= resfT;//resis fixe


        //d�gats finaux
        if (num < 1)
            num = 0;

        // Stat dégats finaux
        if (!isHeal && num > 0 ) {
            int finaldmg = caster.getTotalStats().getEffect(EffectConstant.STATS_ADD_FINALDMG);
            if (finaldmg < 0) {
                finaldmg = Math.abs(finaldmg);
                num = Math.round(num - (num * ((double)finaldmg / 100.0 )));
            } else {
                num = Math.round(num + (num * ((double)finaldmg / 100.0 )));
            }
        }

        //Perte de 10% des PDV MAX par points de degat 10 PDV = 1PDV max en moins
        if (target.getPlayer() != null) {
            int addFixEro = 0;
            if(target.hasBuff(EffectConstant.EFFECTID_EROSION)){
                for(Effect ero : target.getBuffsByEffectID(EffectConstant.EFFECTID_EROSION)){
                    addFixEro+=ero.getArgs1();
                }
            }
            target.removePdvMax((int) Math.floor(num / (10 + addFixEro)));
        }

        // D�but Formule pour les MOBs -- OSEF CA
        if (caster.getPlayer() == null && !caster.isCollector()) {
            if (caster.getMob().getTemplate().getId() == 116)//Sacrifi� Dommage = PDV*2
            {
                //return (int) ((num / 25) * caster.getPdvMax());
            } else {
                int niveauMob = caster.getLvl();
                double CalculCoef = ((niveauMob * 0.5) / 100);
                int Multiplicateur = (int) Math.ceil(CalculCoef);
                return (int) num ;
            }
        }

        return (int) num;
    }

    public static int calculZaapCost(GameMap map1, GameMap map2) {
        int cost =  Math.abs(10 * (Math.abs(map2.getX() - map1.getX())
                + Math.abs(map2.getY() - map1.getY()) - 1));
        return cost;
    }

    private static int getArmorResist(Fighter target, int statID) {
        int armor = 0;

        for (Effect SE : target.getBuffsByEffectID(EffectConstant.EFFECTID_REDUCEDAMAGE_ELEM)) {
            Fighter fighter;

            switch (SE.getSpell()) {
                case 1://Armure incandescente
                    //Si pas element feu, on ignore l'armure
                    if (statID != Constant.ELEMENT_FEU)
                        continue;
                    //Les stats du f�ca sont prises en compte
                    fighter = SE.getCaster();
                    break;
                case 6://Armure Terrestre
                    //Si pas element terre/neutre, on ignore l'armure
                    if (statID != Constant.ELEMENT_TERRE
                            && statID != Constant.ELEMENT_NEUTRE)
                        continue;
                    //Les stats du f�ca sont prises en compte
                    fighter = SE.getCaster();
                    break;
                case 14://Armure Venteuse
                    //Si pas element air, on ignore l'armure
                    if (statID != Constant.ELEMENT_AIR)
                        continue;
                    //Les stats du f�ca sont prises en compte
                    fighter = SE.getCaster();
                    break;
                case 18://Armure aqueuse
                    //Si pas element eau, on ignore l'armure
                    if (statID != Constant.ELEMENT_EAU)
                        continue;
                    //Les stats du f�ca sont prises en compte
                    fighter = SE.getCaster();
                    break;

                default://Dans les autres cas on prend les stats de la cible et on ignore l'element de l'attaque
                    fighter = target;
                    break;
            }
            int intell = fighter.getTotalStats().getEffect(EffectConstant.STATS_ADD_INTE);
            int carac = 0;
            switch (statID) {
                case Constant.ELEMENT_AIR:
                    carac = fighter.getTotalStats().getEffect(EffectConstant.STATS_ADD_AGIL);
                    break;
                case Constant.ELEMENT_FEU:
                    carac = fighter.getTotalStats().getEffect(EffectConstant.STATS_ADD_INTE);
                    break;
                case Constant.ELEMENT_EAU:
                    carac = fighter.getTotalStats().getEffect(EffectConstant.STATS_ADD_CHAN);
                    break;
                case Constant.ELEMENT_NEUTRE:
                case Constant.ELEMENT_TERRE:
                    carac = fighter.getTotalStats().getEffect(EffectConstant.STATS_ADD_FORC);
                    break;
            }
            int value = SE.getFixvalue();
            int a = value * (100 + intell / 2 + carac / 2)
                    / 100;
            armor += a;
        }

        for (Effect SE : target.getBuffsByEffectID(EffectConstant.EFFECTID_REDUCEDAMAGE)) {
            int intell = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_INTE);
            int carac = 0;
            switch (statID) {
                case Constant.ELEMENT_AIR:
                    carac = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_AGIL);
                    break;
                case Constant.ELEMENT_FEU:
                    carac = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_INTE);
                    break;
                case Constant.ELEMENT_EAU:
                    carac = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_CHAN);
                    break;
                case Constant.ELEMENT_NEUTRE:
                case Constant.ELEMENT_TERRE:
                    carac = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_FORC);
                    break;
            }
            int value = SE.getFixvalue();
            int a = value * (100 + intell / 2 + carac / 2)
                    / 100;
            armor += a;
        }

        return armor;
    }

    public static int getPointsLost(char z, int value, Fighter caster, Fighter target) {
        float esquiveC = z == 'a' ? caster.getTotalStats().getEffect(EffectConstant.STATS_ADD_AFLEE) : caster.getTotalStats().getEffect(EffectConstant.STATS_ADD_MFLEE);
        float esquiveT = z == 'a' ? target.getTotalStats().getEffect(EffectConstant.STATS_ADD_AFLEE) : target.getTotalStats().getEffect(EffectConstant.STATS_ADD_MFLEE);
        float ptsMax = z == 'a' ? target.getTotalStatsLessBuff().getEffect(EffectConstant.STATS_ADD_PA) : target.getTotalStatsLessBuff().getEffect(EffectConstant.STATS_ADD_PM);

        int retrait = 0;

        for (int i = 0; i < value; i++) {
            if (ptsMax == 0 && target.getMob() != null) {
                ptsMax = z == 'a' ? target.getPa() : target.getPm();
            }

            float pts = z == 'a' ? target.getPa() : target.getPm();
            float ptsAct = pts - retrait;

            if (esquiveT <= 0)
                esquiveT = 1;
            if (esquiveC <= 0)
                esquiveC = 1;

            float a = esquiveC / esquiveT;
            float b = (ptsAct / ptsMax);

            float pourcentage = a * b * 50;
            int chance = (int) Math.ceil(pourcentage);

            if (chance < 0)
                chance = 0;
            if (chance > 100)
                chance = 100;

            int jet = getRandomValue(0, 99);
            if (jet < chance) {
                retrait++;
            }
        }
        return retrait;
    }

    public static long getGuildXpWin(Fighter perso, AtomicReference<Long> xpWin) {
        if (perso.getPlayer() == null)
            return 0;
        if (perso.getPlayer().getGuildMember() == null)
            return 0;

        GuildMember gm = perso.getPlayer().getGuildMember();

        double xp = (double) xpWin.get(),
                Lvl = perso.getLvl(),
                LvlGuild = perso.getPlayer().getGuild().getLvl(),
                pXpGive = (double) gm.getXpGive() / 100;

        double maxP = xp * pXpGive * 0.10; //Le maximum donn� � la guilde est 10% du montant pr�lev� sur l'xp du combat
        double diff = Math.abs(Lvl - LvlGuild); //Calcul l'�cart entre le niveau du personnage et le niveau de la guilde
        double toGuild;
        if (diff >= 70) {
            toGuild = maxP * 0.10; //Si l'�cart entre les deux level est de 70 ou plus, l'experience donn�e a la guilde est de 10% la valeur maximum de don
        } else if (diff >= 31 && diff <= 69) {
            toGuild = maxP - ((maxP * 0.10) * (Math.floor((diff + 30) / 10)));
        } else if (diff >= 10 && diff <= 30) {
            toGuild = maxP - ((maxP * 0.20) * (Math.floor(diff / 10)));
        } else
        //Si la diff�rence est [0,9]
        {
            toGuild = maxP;
        }
        xpWin.set((long) (xp - xp * pXpGive));
        return Math.round(toGuild);
    }

    public static long getMountXpWin(Fighter perso, AtomicReference<Long> xpWin) {
        if (perso.getPlayer() == null)
            return 0;
        if (perso.getPlayer().getMount() == null)
            return 0;

        int diff = Math.abs(perso.getLvl()
                - perso.getPlayer().getMount().getLevel());

        double coeff = 0;
        double xp = (double) xpWin.get();
        double pToMount = (double) perso.getPlayer().getMountXpGive() / 100 + 0.2;

        if (diff >= 0 && diff <= 9)
            coeff = 0.1;
        else if (diff >= 10 && diff <= 19)
            coeff = 0.08;
        else if (diff >= 20 && diff <= 29)
            coeff = 0.06;
        else if (diff >= 30 && diff <= 39)
            coeff = 0.04;
        else if (diff >= 40 && diff <= 49)
            coeff = 0.03;
        else if (diff >= 50 && diff <= 59)
            coeff = 0.02;
        else if (diff >= 60 && diff <= 69)
            coeff = 0.015;
        else
            coeff = 0.01;

        if (pToMount > 0.2)
            xpWin.set((long) (xp - (xp * (pToMount - 0.2))));

        return Math.round(xp * pToMount * coeff);
    }

    public static int getKamasWin(Fighter i, ArrayList<Fighter> winners,
                                  int maxk, int mink) {
        maxk++;
        int rkamas = (int) (Math.random() * (maxk - mink)) + mink;
        return rkamas * Config.INSTANCE.getRATE_KAMAS();
    }

    public static int getKamasWinPerco(int maxk, int mink) {
        maxk++;
        int rkamas = (int) (Math.random() * (maxk - mink)) + mink;
        return rkamas * Config.INSTANCE.getRATE_KAMAS();
    }

    public static Couple<Integer, Integer> decompPierreAme(GameObject toDecomp) {
        Couple<Integer, Integer> toReturn;
        String[] stats = toDecomp.parseStatsString().split("#");
        int lvlMax = Integer.parseInt(stats[3], 16);
        int chance = Integer.parseInt(stats[1], 16);
        toReturn = new Couple<Integer, Integer>(chance, lvlMax);

        return toReturn;
    }

    // TODO : gestion nulle il faudrait utilisé le boost 750 dans le sort plutot que le niveau du sort.
    public static int totalCaptChance(int pierreChance, Player p) {
        int sortChance = 0;

        switch (p.getSortStatBySortIfHas(413).getLevel()) {
            case 1:
                sortChance = 1;
                break;
            case 2:
                sortChance = 3;
                break;
            case 3:
                sortChance = 6;
                break;
            case 4:
                sortChance = 10;
                break;
            case 5:
                sortChance = 15;
                break;
            case 6:
                sortChance = 25;
                break;
        }

        return sortChance + pierreChance;
    }

    public static int spellCost(int nb) {
        int total = 0;
        for (int i = 1; i < nb; i++) {
            total += i;
        }

        return total;
    }

    public static int getLoosEnergy(int lvl, boolean isAgression,
                                    boolean isPerco) {

        int returned = 5 * lvl;
        if (isAgression)
            returned *= (7 / 4);
        if (isPerco)
            returned *= (3 / 2);
        return returned;
    }

    public static int totalAppriChance(boolean Amande, boolean Rousse,
                                       boolean Doree, Player p) {
        int sortChance = 0;
        int ddChance = 0;
        switch (p.getSortStatBySortIfHas(414).getLevel()) {
            case 1:
                sortChance = 15;
                break;
            case 2:
                sortChance = 20;
                break;
            case 3:
                sortChance = 25;
                break;
            case 4:
                sortChance = 30;
                break;
            case 5:
                sortChance = 35;
                break;
            case 6:
                sortChance = 45;
                break;
        }
        if (Amande || Rousse)
            ddChance = 15;
        if (Doree)
            ddChance = 5;
        return sortChance + ddChance;
    }

    public static int getCouleur(boolean Amande, boolean Rousse, boolean Doree) {
        int Couleur = 0;
        if (Amande && !Rousse && !Doree)
            return 20;
        if (Rousse && !Amande && !Doree)
            return 10;
        if (Doree && !Amande && !Rousse)
            return 18;

        if (Amande && Rousse && !Doree) {
            int Chance = Formulas.getRandomValue(1, 2);
            if (Chance == 1)
                return 20;
            if (Chance == 2)
                return 10;
        }
        if (Amande && !Rousse && Doree) {
            int Chance = Formulas.getRandomValue(1, 2);
            if (Chance == 1)
                return 20;
            if (Chance == 2)
                return 18;
        }
        if (!Amande && Rousse && Doree) {
            int Chance = Formulas.getRandomValue(1, 2);
            if (Chance == 1)
                return 18;
            if (Chance == 2)
                return 10;
        }
        if (Amande && Rousse && Doree) {
            int Chance = Formulas.getRandomValue(1, 3);
            if (Chance == 1)
                return 20;
            if (Chance == 2)
                return 10;
            if (Chance == 3)
                return 18;
        }
        return Couleur;
    }

    public static int calculEnergieLooseForToogleMount(int pts) {
        if (pts <= 170)
            return 4;
        if (pts >= 171 && pts < 180)
            return 5;
        if (pts >= 180 && pts < 200)
            return 6;
        if (pts >= 200 && pts < 210)
            return 7;
        if (pts >= 210 && pts < 220)
            return 8;
        if (pts >= 220 && pts < 230)
            return 10;
        if (pts >= 230 && pts <= 240)
            return 12;
        return 10;
    }

    public static int getLvlDopeuls(int lvl)//Niveau du dopeul � combattre
    {
        if (lvl < 20)
            return 20;
        if (lvl > 19 && lvl < 40)
            return 40;
        if (lvl > 39 && lvl < 60)
            return 60;
        if (lvl > 59 && lvl < 80)
            return 80;
        if (lvl > 79 && lvl < 100)
            return 100;
        if (lvl > 99 && lvl < 120)
            return 120;
        if (lvl > 119 && lvl < 140)
            return 140;
        if (lvl > 139 && lvl < 160)
            return 160;
        if (lvl > 159 && lvl < 180)
            return 180;
        if (lvl > 180)
            return 200;
        return 200;
    }

    public static int calculChanceByElement(int lvlJob, int lvlObject,
                                            int lvlRune) {
        int K = 1;
        if (lvlRune == 1)
            K = 100;
        else if (lvlRune == 25)
            K = 175;
        else if (lvlRune == 50)
            K = 350;
        return lvlJob * 100 / (K + lvlObject);
    }

    public static ArrayList<Integer> chanceFM(int WeightTotalBase,
                                                        int WeightTotalBaseMin, int currentWeithTotal,
                                                        int currentWeightStats, int weight, int diff, float coef,
                                                        int maxStat, int minStat, int actualStat, float x,
                                                        boolean bonusRune, int statsAdd) {
        ArrayList<Integer> chances = new ArrayList<Integer>();
        float c = 1, m1 = (maxStat - (actualStat + statsAdd)), m2 = (maxStat - minStat);
        if ((1 - (m1 / m2)) > 1.0)
            c = (1 - ((1 - (m1 / m2)) / 2)) / 2;
        else if ((1 - (m1 / m2)) > 0.8)
            c = 1 - ((1 - (m1 / m2)) / 2);
        if (c < 0)
            c = 0;
        // la variable c reste � 1 si le jet ne depasse pas 80% sinon il diminue tr�s fortement. Si le jet d�passe 100% alors il diminue encore plus.

        int moyenne = (int) Math.floor(WeightTotalBase
                - ((WeightTotalBase - WeightTotalBaseMin) / 2));

        float mStat = ((float) moyenne / (float) currentWeithTotal); // Si l'item est un bon jet dans l'ensemble, diminue les chances sinon l'inverse.

        if (mStat > 1.2)
            mStat = 1.2F;

        float a = ((((((WeightTotalBase + diff) * coef) * mStat) * c) * x));
        float b = (float) (Math.sqrt(currentWeithTotal + currentWeightStats) + weight);
        if (b < 1.0)
            b = 1.0F;

        int p1 = (int) Math.floor(a / b); // Succes critique
        int p2 = 0; // Succes neutre
        int p3 = 0; // Echec critique
        if (bonusRune)
            p1 += 20;
        if (p1 < 1) {
            p1 = 1;
            p2 = 0;
            p3 = 99;
        } else if (p1 > 100) {
            p1 = 66;
            p2 = 34;
        } else if (p1 > 66)
            p1 = 66;

        if (p2 == 0 && p3 == 0) {
            p2 = (int) Math.floor(a
                    / (Math.sqrt(currentWeithTotal + currentWeightStats)));
            if (p2 > (100 - p1))
                p2 = (100 - p1);
            if (p2 > 50)
                p2 = 50;
        }
        chances.add(0, p1);
        chances.add(1, p2);
        chances.add(2, p3);
        return chances;
    }

    public static String convertToDate(long time) {
        String hexDate = "#";
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = formatter.format(time);

        String[] split = date.split("\\s");

        String[] split0 = split[0].split("-");
        hexDate += Integer.toHexString(Integer.parseInt(split0[0])) + "#";
        int mois = Integer.parseInt(split0[1]) - 1;
        int jour = Integer.parseInt(split0[2]);
        hexDate += Integer.toHexString(Integer.parseInt((mois < 10 ? "0" + mois : mois)
                + "" + (jour < 10 ? "0" + jour : jour)))
                + "#";

        String[] split1 = split[1].split(":");
        String heure = split1[0] + split1[1];
        hexDate += Integer.toHexString(Integer.parseInt(heure));
        return hexDate;
    }

    public static int getXpStalk(int lvl) {
        switch (lvl) {
            case 50:
            case 51:
            case 52:
            case 53:
            case 54:
            case 55:
            case 56:
            case 57:
            case 58:
            case 59:
                return 65000;
            case 60:
            case 61:
            case 62:
            case 63:
            case 64:
            case 65:
            case 66:
            case 67:
            case 68:
            case 69:
                return 90000;
            case 70:
            case 71:
            case 72:
            case 73:
            case 74:
            case 75:
            case 76:
            case 77:
            case 78:
            case 79:
                return 120000;
            case 80:
            case 81:
            case 82:
            case 83:
            case 84:
            case 85:
            case 86:
            case 87:
            case 88:
            case 89:
                return 160000;
            case 90:
            case 91:
            case 92:
            case 93:
            case 94:
            case 95:
            case 96:
            case 97:
            case 98:
            case 99:
                return 210000;
            case 100:
            case 101:
            case 102:
            case 103:
            case 104:
            case 105:
            case 106:
            case 107:
            case 108:
            case 109:
                return 270000;
            case 110:
            case 111:
            case 112:
            case 113:
            case 114:
            case 115:
            case 116:
            case 117:
            case 118:
            case 119:
                return 350000;
            case 120:
            case 121:
            case 122:
            case 123:
            case 124:
            case 125:
            case 126:
            case 127:
            case 128:
            case 129:
                return 440000;
            case 130:
            case 131:
            case 132:
            case 133:
            case 134:
            case 135:
            case 136:
            case 137:
            case 138:
            case 139:
                return 540000;
            case 140:
            case 141:
            case 142:
            case 143:
            case 144:
            case 145:
            case 146:
            case 147:
            case 148:
            case 149:
                return 650000;
            case 150:
            case 151:
            case 152:
            case 153:
            case 154:
                return 760000;
            case 155:
            case 156:
            case 157:
            case 158:
            case 159:
                return 880000;
            case 160:
            case 161:
            case 162:
            case 163:
            case 164:
                return 1000000;
            case 165:
            case 166:
            case 167:
            case 168:
            case 169:
                return 1130000;
            case 170:
            case 171:
            case 172:
            case 173:
            case 174:
                return 1300000;
            case 175:
            case 176:
            case 177:
            case 178:
            case 179:
                return 1500000;
            case 180:
            case 181:
            case 182:
            case 183:
            case 184:
                return 1700000;
            case 185:
            case 186:
            case 187:
            case 188:
            case 189:
                return 2000000;
            case 190:
            case 191:
            case 192:
            case 193:
            case 194:
                return 2500000;
            case 195:
            case 196:
            case 197:
            case 198:
            case 199:
            case 200:
                return 3000000;

        }
        return 65000;
    }

    public static String translateMsg(String msg) {
        String alpha = "a b c d e f g h i j k l n o p q r s t u v w x y z é è à ç & û â ê ô î ä ë ü ï ö";
        for (String i : alpha.split(" "))
            msg = msg.replace(i, "m");
        alpha = "A B C D E F G H I J K L M N O P Q R S T U V W X Y Z Ë Ü Ä Ï Ö Â Ê Û Î Ô";
        for (String i : alpha.split(" "))
            msg = msg.replace(i, "H");
        return msg;
    }

    // NOUVELLE FONCTION PAS TROP MAL
    public static ArrayList<Integer> chanceFM2(final int PoidMaxItem, final int PoidMiniItem,final int PoidTotItemActuel, final int PoidActuelStatAFm,
                                               final int PoidTotStatsExoItemActuel,final float poidRune,  final int maxStat, final int minStat,final int actualStat, final int statsAdd,
                                               double poidUnitaire, final int statsJetfutur, final float x, final float coef, Player player, int puit,String loi) {


        final ArrayList<Integer> chances = new ArrayList<Integer>();

        int p1 = 0;
        int p2 = 0;
        int p3 = 0;

        if(loi == "exo") {
            p1 = (Config.INSTANCE.getPERCENT_EXO() * Config.INSTANCE.getRATE_FM());
            p2 = 0;
            p3 = (100-p1);

            chances.add(0, p1);
            chances.add(1, p2);
            chances.add(2, p3);

            return chances;
        }

        final int diff = (int) Math.abs( (PoidMaxItem +100) - PoidTotItemActuel);

        //player.sendMessage("Poid en JP : " + WeightTotalBase );
        //player.sendMessage("Poid en JM : " + WeightTotalBaseMin );
        //player.sendMessage("Poid de l'obj actuel : " + PoidTotItemActuel );
        //player.sendMessage("Stats Max ITEM : " + maxStat );
        //player.sendMessage("Stats Min ITEM : " + minStat );
        //player.sendMessage("Stats Actuel : " + actualStat );
        //player.sendMessage("Je confond sans doute : " + PoidTotItemActuel + " != " +PoidActuelStatAFm +" + "+ PoidTotStatsExoItemActuel  );
        //player.sendMessage("Poid de la stats a montÃ© : " + (PoidActuelStatAFm + PoidTotStatsExoItemActuel)  );
        //player.sendMessage("Poid de la rune : " + poidRune );
        //player.sendMessage("DiffÃ©rentiel  : " + diff );
        //player.sendMessage("Coefficient : " + coef );
        //player.sendMessage("Je sais pas trop mais dans la multiplication : " + x );
        //player.sendMessage("Stats ajoutÃ© : " + statsAdd );
        //player.sendMessage("Puits de l'objet : " + puit );
        //player.sendMessage("Rate FM : " + Config.getInstance().rateFm );



        float c = 1.0f;
        final float m1 = maxStat - (actualStat + statsAdd);
        final float m2 = maxStat - minStat;
        if (1.0f - m1 / m2 > 1.0) {
            c = (1.0f - (1.0f - m1 / m2) / 2.0f) / 2.0f;
        } else if (1.0f - m1 / m2 > 0.8) {
            c = 1.0f - (1.0f - m1 / m2) / 2.0f;
        }
        if (c < 0.0f) {
            c = 0.0f;
        }


        try {

            final int moyenne = (int) Math.floor(maxStat - (((maxStat - minStat)) / 2) );
            //player.sendMessage("moyenne : " + moyenne );

            float mStat = 1.0f;
            // Si la stats n'est pas prÃ©sent sur l'obj (Exclu EXO)
            if(maxStat == 0) {
                mStat = 1f;
            }
            else {
                //player.sendMessage("moyenne : " + moyenne );
                //player.sendMessage("actualStat : " + actualStat );
                //player.sendMessage("actualStat : " + actualStat );
                // Si la diffÃ©rence entre la stat sencÃ© etre prÃ©sente et la sats est null on limite la hausse
                if( actualStat > 0) {
                    mStat = (float)((float)moyenne / (float)actualStat);
                    //player.sendMessage("mStat : " + mStat );
                    if (mStat > 5f) {
                        mStat = 5f;
                    }
                }
                else {
                    mStat = 5f;
                }
                //player.sendMessage("mStat : " + mStat );
            }

            if(mStat==0f) {
                mStat = 1;
            }
            //player.sendMessage("a = "+ (PoidMaxItem + diff)+ "*" + coef + "*" + mStat + "*" + x + "*" +  Config.getInstance().rateFm  );
            final float a = (PoidMaxItem + diff) * coef * mStat * x * Config.INSTANCE.getRATE_FM();
            float b = (float) (Math.sqrt(PoidTotItemActuel + PoidActuelStatAFm)  + poidRune);

            //player.sendMessage("Le total c'est  " + a );
            //player.sendMessage("Le diviseur c'est  " + b );

            if (b < 1.0) {
                b = 1.0f;
            }

            p1 = (int) Math.floor(a / b);
            p2 = 0;
            p3 = 0;

            if (p1 < 5) {
                p1 = 5;
                p2 = 0;
                p3 = 95;
            }
            else if (p1 > 80) {
                p1 = 80;
                p2 = 19;
            }
            if (p2 == 0 && p3 == 0) {
                p2 = (int) Math.floor(a / Math.sqrt(PoidTotItemActuel + PoidActuelStatAFm ));
                if (p2 > 100 - p1) {
                    p2 = 100 - p1;
                }
                if (p2 > 50) {
                    p2 = 50;
                }
            }

            //player.sendMessage("La chance SN c'est  " + p2 );
            chances.add(0, p1);
            chances.add(1, p2);
            chances.add(2, p3);
        }
        catch(Exception e) {
            player.sendMessage("ERREUR:" + e );
            p1 = 5;
            p2 = 0;
            p3 = 95;
            chances.add(0, p1);
            chances.add(1, p2);
            chances.add(2, p3);
        }
        return chances;
    }

    public static int[] getRandomsInt(int[] table,int nb)
    {
        int[] randomInts = new int[nb];
        Random random = new Random();

        // Shuffle the array using Fisher-Yates algorithm
        for (int i = table.length - 1; i >= 1; i--) {
            int j = random.nextInt(i + 1);
            int temp = table[j];
            table[j] = table[i];
            table[i] = temp;
        }

        // Copy the first 7 elements of the shuffled array to the randomInts array
        System.arraycopy(table, 0, randomInts, 0, nb);
        return randomInts;
    }
}
