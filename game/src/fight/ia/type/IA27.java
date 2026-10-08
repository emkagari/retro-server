package fight.ia.type;

import fight.Fight;
import fight.Fighter;
import fight.ia.AbstractNeedSpell;
import fight.ia.util.Function;
import fight.spells.LaunchedSpell;
import fight.spells.SpellGrade;

import java.util.ArrayList;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA27 extends AbstractNeedSpell  {

    public IA27(Fight fight, Fighter fighter, byte count) {
        super(fight, fighter, count,"IA27");
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            int time = 100, maxPo = 1;
            boolean action = false;
            Fighter nearestEnnemy = Function.getInstance().getNearestEnnemy(this.fight, this.fighter);
            //System.out.println(this.fighter.getCurPa(this.fight));

            ArrayList<Fighter> Exclusions = new ArrayList<>();
            for(SpellGrade S : this.highests) {
                if (S != null && S.getMaxPO() > maxPo) {
                    if (S.getTypeSwitchSpellEffects() == 0 && LaunchedSpell.cooldownGood(this.fighter, S.getSpellID())) {
                        maxPo = S.getMaxPO();

                        if(S.isModifPO())
                            maxPo = maxPo + this.fighter.getBuffValue(117) - this.fighter.getBuffValue(116);

                        int numLunchT = S.getMaxLaunchByTarget();
                        if(numLunchT - LaunchedSpell.getNbLaunchTarget(this.fighter, nearestEnnemy, S.getSpellID()) <= 0 && numLunchT > 0) {
                            if(!Exclusions.contains(nearestEnnemy))
                                Exclusions.add(nearestEnnemy);

                            nearestEnnemy = Function.getInstance().getNearestEnnemyWithExclusion(this.fight, this.fighter,Exclusions);
                            if (nearestEnnemy == null){
                                nearestEnnemy = Function.getInstance().getNearestEnnemy(this.fight, this.fighter);
                            }
                        }
                    }
                }
            }

            Fighter firstEnnemy = Function.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 1, maxPo + 1);
            Fighter secondEnnemy = Function.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 2);

            //if(maxPo == 1) firstEnnemy = null;
            if(secondEnnemy != null) if(secondEnnemy.isHide()) secondEnnemy = null;
            if(firstEnnemy != null) if(firstEnnemy.isHide()) firstEnnemy = null;

            if ( !this.buffs.isEmpty() && this.fighter.getCurPa(this.fight) > 0 ) {
                if (Function.getInstance().buffIfPossible(this.fight, this.fighter, this.fighter, this.buffs)) {
                    time = 400;
                    action = true;
                }
            }

            if( !this.invocations.isEmpty() && this.fighter.getCurPa(this.fight) > 0 && !action) {
                if (Function.getInstance().invocIfPossibleloin(this.fight, this.fighter, this.invocations,nearestEnnemy)) {
                    time = 600;
                    action = true;
                }
            }

            if( !this.invocations.isEmpty() && this.fighter.getCurPa(this.fight) > 0 && !action) {
                if (Function.getInstance().invocIfPossible(this.fight, this.fighter, this.invocations)) {
                    time = 600;
                    action = true;
                }
            }

            if(!this.heals.isEmpty() && this.fighter.getCurPa(this.fight) > 0 && !action) {
                if (Function.getInstance().HealIfPossible(this.fight, this.fighter, true, 70) != 0) {
                    time = 1000;
                    action = true;
                }
            }

            if(this.fighter.getCurPm(this.fight) > 0 && firstEnnemy == null && secondEnnemy == null && !action ) {
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
            }


            if(this.fighter.getCurPa(this.fight) > 0 && !action && secondEnnemy ==null && nearestEnnemy !=null) {
                if (Function.getInstance().mobilityIfPossible(this.fight, this.fighter, nearestEnnemy)) {
                    time = 600;
                    action = true;
                }
            }



            if ( !this.buffs.isEmpty() && this.fighter.getCurPa(this.fight) > 0 && !action) {
                if (Function.getInstance().buffIfPossible(this.fight, this.fighter, this.fighter, this.buffs)) {
                    time = 400;
                    action = true;
                }
            }


            if( !this.highests.isEmpty() && this.fighter.getCurPa(this.fight) > 0 && firstEnnemy != null && secondEnnemy == null && !action) {
                int num = Function.getInstance().attackIfPossible(this.fight, this.fighter, this.highests);
                if(num != -1 && num != 0) {
                    time = num;
                    action = true;
                }
            } else if(!this.cacs.isEmpty() && this.fighter.getCurPa(this.fight) > 0 && secondEnnemy != null && !action) {
                int num = Function.getInstance().attackIfPossible(this.fight, this.fighter, this.cacs);
                if(num != -1 && num != 0) {
                    time = num;
                    action = true;
                }
            }

                if(!this.highests.isEmpty() && this.fighter.getCurPa(this.fight) > 0 && nearestEnnemy != null && !action) {
                    int num = Function.getInstance().attackIfPossible(this.fight, this.fighter, this.highests);
                    if(num != -1 && num != 0) {
                        time = num;
                        action = true;
                    }
                }


            if(this.fighter.getCurPm(this.fight) > 0 && !action) {
                int num = Function.getInstance().moveautourIfPossible(this.fight, this.fighter, nearestEnnemy);
                if(num != 0){
                    time = num;
                }
            }

            if(this.fighter.getCurPa(this.fight) == 0 && this.fighter.getCurPm(this.fight) == 0) this.stop = true;
            addNext(this::decrementCount, time);
        } else {
            this.stop = true;
        }
    }
}