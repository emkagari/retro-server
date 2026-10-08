package fight.ia.type;

import common.PathFinding;
import fight.Fight;
import fight.Fighter;
import fight.ia.AbstractNeedSpell;
import fight.ia.util.Function;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA82 extends AbstractNeedSpell  {

    public IA82(Fight fight, Fighter fighter, byte count) {
        super(fight, fighter, count,"IA82");
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            Fighter ennemy = Function.getInstance().getNearestEnnemy(this.fight, this.fighter);
            Fighter invocationAllie = Function.getInstance().getNearestFriendInvoc(this.fight, this.fighter);
            int time = 100, maxPo = 1;
            boolean action = false;


            maxPo = Function.getInstance().getMaxPoUsableSpell(this.fighter, this.highests);

            Fighter target = Function.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 2);
            if(target != null)
                if(target.isHide())
                    target = null;

            /*if(this.fighter.getCurPm(this.fight) > 0 && firstEnnemy == null && secondEnnemy == null && !action ) {
                if(this.fighter.getCurPm(this.fight) > 0 && this.fighter.getCurPa(this.fight) > 0 && !action) {
                    int value = Function.getInstance().movetoAttackwithLOS(this.fight, this.fighter,nearestEnnemy,maxPo);
                    if(value != 0) {
                        time = value;
                        action = true;
                    }
                }
            }
            else if (this.fighter.getCurPm(this.fight) > 0 & nearestEnnemy != null && !action)
            {
                int num = Function.getInstance().moveautourIfPossible(this.fight, this.fighter, nearestEnnemy);
                if(num != 0) {
                    time = num;
                    action = true;
                }
            }*/

            if(this.fighter.getCurPa(this.fight) > 0) {
                if (Function.getInstance().invocIfPossibleloin(this.fight, this.fighter, this.invocations,ennemy)) {
                    time = 2000;
                    action = true;
                }
            }

            if(this.fighter.getCurPa(this.fight) > 0 && !action && invocationAllie != null) {
                if (Function.getInstance().buffIfPossible(this.fight, this.fighter, invocationAllie,this.buffs)) {
                    time = 800;
                    action = true;
                }
            }


            if(this.fighter.getCurPm(this.fight) < PathFinding.getDistanceBetween(fight.getMap(),this.fighter.getCell().getId(),ennemy.getCell().getId()) && target == null && !action) {
                if(Function.getInstance().mobilityIfPossible(this.fight, this.fighter, ennemy)) {
                    time = 800;
                    action = true;
                    target = Function.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 2);
                }
            }


            if(this.fighter.getCurPm(this.fight) > 0 && target == null && !action) {
                int num = Function.getInstance().moveautourIfPossible(this.fight, this.fighter, ennemy);
                if(num != 0) {
                    time = num;
                    action = true;
                    target = Function.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 2);
                }
            }

            if(this.fighter.getCurPa(this.fight) > 0 && target == null && !action) {
                int num = Function.getInstance().attackBondIfPossible(this.fight, this.fighter, ennemy);
                if(num != 0) {
                    time = num;
                    action = true;
                    target = Function.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 2);//2 = po min 1 + 1;
                }
            }

            if(this.fighter.getCurPa(this.fight) > 0 && target == null && !action) {
                int num = Function.getInstance().attackIfPossible(this.fight, this.fighter, this.highests);
                if(num != 0) {
                    time = num;
                    action = true;
                }
            } else if(this.fighter.getCurPa(this.fight) > 0 && target != null && !action) {
                int num = Function.getInstance().attackIfPossible(this.fight, this.fighter, this.cacs);
                if(num != 0) {
                    time = num;
                    action = true;
                }
            }

            if(this.fighter.getCurPm(this.fight) > 0 && !action) {
                int num = Function.getInstance().moveautourIfPossible(this.fight, this.fighter, ennemy);
                if(num != 0) time = num;
            }

            if(this.fighter.getCurPa(this.fight) == 0 && this.fighter.getCurPm(this.fight) == 0) this.stop = true;
            addNext(this::decrementCount, time);
        } else {
            this.stop = true;
        }
    }
}