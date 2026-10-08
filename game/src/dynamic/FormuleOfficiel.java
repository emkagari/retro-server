package dynamic;

import client.Player;
import entity.Collector;
import fight.Fighter;
import fight.spells.EffectConstant;
import game.world.World;
import kernel.Config;

import java.util.ArrayList;

public class FormuleOfficiel {

    public static long getXp(Object object, ArrayList<Fighter> winners,
                             long groupXp, byte nbonus, int star, int challenge, int lvlMax,
                             int lvlMin, int lvlLoosers, int lvlWinners) {
        if (lvlMin <= 0 || object == null)
            return 0;
        if (object instanceof Fighter) {
            Fighter fighter = (Fighter) object;
            Player player = fighter.getPlayer();

            if (winners.contains(fighter)) {
                if (lvlWinners <= 0)
                    return 0;

                double sagesse = fighter.getLvl() * 0.5 + fighter.getPlayer().getTotalStats().getEffect(EffectConstant.STATS_ADD_SAGE),
                        nvGrpMonster = ((double) lvlMax / (double) lvlMin),
                        bonus = 1.0,
                        rapport = ((double) lvlLoosers / (double) lvlWinners);

                if (winners.size() == 1)
                    rapport = 0.6;
                else if (rapport == 0)
                    return 0;
                else if (rapport <= 1.1 && rapport >= 0.9)
                    rapport = 1;
                else {
                    if (rapport > 1)
                        rapport = 1 / rapport;
                    if (rapport < 0.01)
                        rapport = 0.01;
                }

                int sizeGroupe = 0;
                for (Fighter f : winners) {
                    if (f.getPlayer() != null && !f.isInvocation()
                            && !f.isMob() && !f.isCollector() && !f.isDouble())
                        sizeGroupe++;
                }
                if (sizeGroupe < 1)
                    return 0;
                if (sizeGroupe > 8)
                    sizeGroupe = 8;

                if (nbonus > 8)
                    nbonus = 8;
                switch (nbonus) {
                    case 0:
                        bonus = 1;
                        break;
                    case 1:
                        bonus = 1;
                        break;
                    case 2:
                        bonus = 2.1;
                        break;
                    case 3:
                        bonus = 3.2;
                        break;
                    case 4:
                        bonus = 4.3;
                        break;
                    case 5:
                        bonus = 5.4;
                        break;
                    case 6:
                        bonus = 6.5;
                        break;
                    case 7:
                        bonus = 7.8;
                        break;
                    case 8:
                        bonus = 9;
                        break;
                }
                if (nvGrpMonster == 0)
                    return 0;
                else if (nvGrpMonster < 3.0)
                    nvGrpMonster = 1;
                else
                    nvGrpMonster = 1 / nvGrpMonster;

                if (nvGrpMonster < 0)
                    nvGrpMonster = 0;
                else if (nvGrpMonster > 1)
                    nvGrpMonster = 1;

                long total = (long) (((1 + (sagesse / 100)) * (1 + (challenge / 100)) * (1 + (star / 100))
                        * (bonus + rapport) * (nvGrpMonster) * (groupXp / sizeGroupe))
                        * Config.INSTANCE.getRATE_XP() * World.world.getConquestBonusNew(fighter.getPlayer()));

                //System.out.println("Bonus xp = " + total);
                //System.out.println((1 + (sagesse / 100)) + " * " + (1 + (challenge / 100)) + " * " + (1 + (star / 100)) + " * " + (bonus + rapport) + " * " + (nvGrpMonster) + " * " + (groupXp / sizeGroupe) + " * " + Config.INSTANCE.getRATE_XP() + " * " + World.world.getConquestBonusNew(fighter.getPlayer()) );
                return total;
            }
        } else if (object instanceof Collector) {
            Collector collector = (Collector) object;

            if (World.world.getGuild(collector.getGuildId()) == null)
                return 0;

            if (lvlWinners <= 0)
                return 0;

            double sagesse = World.world.getGuild(collector.getGuildId()).getLvl()
                    * 0.5
                    + World.world.getGuild(collector.getGuildId()).getStats(EffectConstant.STATS_ADD_SAGE), nvGrpMonster = ((double) lvlMax / (double) lvlMin), bonus = 1.0, rapport = ((double) lvlLoosers / (double) lvlWinners);

            if (winners.size() == 1)
                rapport = 0.6;
            else if (rapport == 0)
                return 0;
            else if (rapport <= 1.1 && rapport >= 0.9)
                rapport = 1;
            else {
                if (rapport > 1)
                    rapport = 1 / rapport;
                if (rapport < 0.01)
                    rapport = 0.01;
            }

            int sizeGroupe = 0;
            for (Fighter f : winners) {
                if (f.getPlayer() != null && !f.isInvocation()
                        && !f.isMob() && !f.isCollector() && !f.isDouble())
                    sizeGroupe++;
            }
            if (sizeGroupe < 1)
                return 0;
            if (sizeGroupe > 8)
                sizeGroupe = 8;

            if (nbonus > 8)
                nbonus = 8;
            switch (nbonus) {
                case 0:
                    bonus = 0.5;
                    break;
                case 1:
                    bonus = 0.5;
                    break;
                case 2:
                    bonus = 2.1;
                    break;
                case 3:
                    bonus = 3.2;
                    break;
                case 4:
                    bonus = 4.3;
                    break;
                case 5:
                    bonus = 5.4;
                    break;
                case 6:
                    bonus = 6.5;
                    break;
                case 7:
                    bonus = 7.8;
                    break;
                case 8:
                    bonus = 9;
                    break;
            }
            if (nvGrpMonster == 0)
                return 0;
            else if (nvGrpMonster < 3.0)
                nvGrpMonster = 1;
            else
                nvGrpMonster = 1 / nvGrpMonster;

            if (nvGrpMonster < 0)
                nvGrpMonster = 0;
            else if (nvGrpMonster > 1)
                nvGrpMonster = 1;

            return (long) (((1 + ((sagesse + star + challenge) / 100))
                    * (bonus + rapport) * (nvGrpMonster) * (groupXp / sizeGroupe)) * Config.INSTANCE.getRATE_XP());
        }
        return 0;
    }

    public static long getXp2(Object object, ArrayList<Fighter> winners,
                             long groupXp, double nbonus, int star, int challenge, int lvlMax,
                             int lvlMin, int lvlLoosers, int lvlWinners,double bonusip, double bonusclasse,boolean offiLike) {

        if (lvlMin <= 0 || object == null)
            return 0;
        if (object instanceof Fighter) {
            Fighter fighter = (Fighter) object;
            Player player = fighter.getPlayer();

            double bonusVip = 1;
            if(player.getAccount().getVip() == 1 ){
                bonusVip = 1.15;
            }


            if (winners.contains(fighter)) {
                if (lvlWinners <= 0)
                    return 0;


                double sagesse = fighter.getLvl() * 0.5 + (fighter.getPlayer().getTotalStats()
                        .getEffect(EffectConstant.STATS_ADD_SAGE) / 2) , nvGrpMonster = ((double) lvlMax / (double) lvlMin),rapport = ((double) lvlLoosers / (double) lvlWinners),
                        bonus = 1.0;

                if (winners.size() == 1)
                    rapport = 0.6;
                else if (rapport == 0)
                    return 0;
                else if (rapport <= 1.1 && rapport >= 0.9)
                    rapport = 1;
                else {
                    if (rapport > 1)
                        rapport = 1 / rapport;
                    if (rapport < 0.01)
                        rapport = 0.01;
                }

                nbonus = nbonus + rapport;

                if(sagesse < 100){
                    sagesse = 100;
                }

                int sizeGroupe = 0;
                for (Fighter f : winners) {
                    if (f.getPlayer() != null && !f.isInvocation()
                            && !f.isMob() && !f.isCollector() && !f.isDouble())
                        sizeGroupe++;
                }
                if (sizeGroupe < 1)
                    return 0;
                if (sizeGroupe > 8)
                    sizeGroupe = 8;

                if (nvGrpMonster == 0)
                    return 0;
                else if (nvGrpMonster < 3.0)
                    nvGrpMonster = 1;
                else
                    nvGrpMonster = 1 / nvGrpMonster;

                if (nvGrpMonster < 0)
                    nvGrpMonster = 0;
                else if (nvGrpMonster > 1)
                    nvGrpMonster = 1;


                double bonusChallenge = 1.0  + ((double)challenge / 100);
                double bonusStar = 1.0 + ((double)star / 100);
                long total=0L;
                if(offiLike){
                    sagesse = fighter.getLvl() * 0.5 + (fighter.getPlayer().getTotalStats().getEffect(EffectConstant.STATS_ADD_SAGE));
                    total = (long) (((1 + (sagesse / 100)) * bonusChallenge * 1
                            * (1) * (nvGrpMonster) * (groupXp) * 1)
                            * 1 * 1 * 1 * 1 );
                }
                else{
                    total = (long) (((1 + (sagesse / 100)) * bonusChallenge * bonusStar
                            * (nbonus) * (nvGrpMonster) * (groupXp) * bonusVip)
                            * Config.INSTANCE.getRATE_XP() * World.world.getConquestBonusNew(fighter.getPlayer()) * bonusip * bonusclasse );
                }

                //System.out.println("Bonus xp2 = " + total);
                //System.out.println((1 + (sagesse / 100)) + " * " + ( bonusChallenge ) + " * " + ( bonusStar) + " * " + (nbonus) + " * " + (nvGrpMonster) + " * " + (groupXp) + " * " + Config.INSTANCE.getRATE_XP() + " * " + World.world.getConquestBonusNew(fighter.getPlayer())  + " * " + bonusip  + " * " + bonusclasse + " * " + bonusVip);
                return total;
            }
        }
        return 0;
    }
}