package fight.ia.type;

import fight.Fight;
import fight.Fighter;
import fight.ia.AbstractNeedSpell;
import fight.ia.util.Function;
import game.world.World;

import java.util.ArrayList;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA81 extends AbstractNeedSpell  {

    public IA81(Fight fight, Fighter fighter, byte count) {
        super(fight, fighter, count,"IA81");
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            int time = 100;
            int maxPoDammage = 1, maxPoHeal = 1, MaxPoBuff = 1;
            boolean action = false;

            // MAX PO DAMMAGE
            maxPoDammage = Function.getInstance().getMaxPoUsableSpell(this.fighter, this.highests);

            // MAX PO SOUTIEN
            MaxPoBuff = Function.getInstance().getMaxPoUsableSpell(this.fighter, this.buffs);

            // MAX PO HEAL
            maxPoHeal =  Function.getInstance().getMaxPoUsableSpell(this.fighter, this.heals);

            ArrayList<Fighter> AllyToHeal = Function.getInstance().getAlliesToHeal(this.fight, this.fighter,80);
            Fighter AllyToBuff = Function.getInstance().getAllyToBuff(this.fight, this.fighter);


            // On essaie le heal de zone
            if(AllyToHeal.size() >= 2){
                if (Function.getInstance().HealIfPossible(this.fight, this.fighter,this.fighter, World.world.getSort(1274).getStatsByLevel(1) ) != 0 ) {
                    time = 1000;
                    action = true;
                }
            }

            // On essaie le heal d'allié
            if(this.fighter.getCurPa(this.fight) > 0 && !action) {
                if (Function.getInstance().HealIfPossible(this.fight, this.fighter, false, 50) != 0) {
                    time = 1000;
                    action = true;
                }
            }

            // On essaie le autoheal
            if(this.fighter.getCurPa(this.fight) > 0 && !action) {
                if (Function.getInstance().HealIfPossible(this.fight, this.fighter, true, 50) != 0) {
                    time = 1000;
                    action = true;
                }
            }

            Fighter ennemy = Function.getInstance().getNearestEnnemy(this.fight, this.fighter);
            Fighter C = Function.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, maxPoDammage +1 );//2 = po min 1 + 1;
            Fighter A = Function.getInstance().getNearestAminoinvocnbrcasemax(this.fight, this.fighter, 1, maxPoHeal + 1);// pomax +1;

            if(C != null && C.isHide()) C = null;

            if(this.fighter.getCurPa(this.fight) > 0) {
                if (Function.getInstance().HealIfPossible(this.fight, this.fighter, false, 98) != 0) {
                    time = 1000;
                    action = true;
                }
            }

            if(this.fighter.getCurPm(this.fight) > 0 && AllyToBuff != null && !action) {
                int value = Function.getInstance().moveautourIfPossible(this.fight, this.fighter, AllyToBuff);
                if(value != 0) {
                    time = value;
                    action = true;
                }
            }
            else{
                int value = Function.getInstance().movetoAttackwithLOS(this.fight, this.fighter,ennemy,maxPoDammage + this.fighter.getBuffValue(117) - this.fighter.getBuffValue(116));
                if(value != 0) {
                    time = value;
                    action = true;
                }
            }

            if(this.fighter.getCurPa(this.fight) > 0 && !action && AllyToBuff != null) {
                if (Function.getInstance().buffIfPossible(this.fight, this.fighter, AllyToBuff, this.buffs)) {
                    time = 1000;
                    action = true;
                }
            }
            if(this.fighter.getCurPa(this.fight) > 0 && !action && A != null) {
                if (Function.getInstance().buffIfPossible(this.fight, this.fighter, A, this.buffs)) {
                    time = 1000;
                    action = true;
                }
            }
            if(this.fighter.getCurPa(this.fight) > 0 && !action ) {
                if (Function.getInstance().buffIfPossible(this.fight, this.fighter, this.fighter, this.buffs)) {
                    time = 1000;
                    action = true;
                }
            }

            if(this.fighter.getCurPm(this.fight) > 0 && A == null && AllyToHeal.size() > 0 && !action) {
                int value = Function.getInstance().moveautourIfPossible(this.fight, this.fighter, AllyToHeal.get(0) );
                if(value != 0) {
                    time = value;
                    action = true;
                }
            }

            if(this.fighter.getCurPa(this.fight) > 0 && C != null && !action) {
                int num = Function.getInstance().attackIfPossible(this.fight, this.fighter, this.highests);
                if(num != 0) {
                    time = num;
                    action = true;
                }
            }

            if(this.fighter.getCurPm(this.fight) > 0 && !action) {
                int value = Function.getInstance().moveFarIfPossible(this.fight, this.fighter);
                if(value != 0) time = value;
            }

            if((this.fighter.getCurPa(this.fight) == 0 && this.fighter.getCurPm(this.fight) == 0) || !action) this.stop = true;
            addNext(this::decrementCount, time);
        } else {
            this.stop = true;
        }
    }
}