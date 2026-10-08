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
public class IA32 extends AbstractNeedSpell  {

    private int attack = 0;

    public IA32(Fight fight, Fighter fighter, byte count) {
        super(fight, fighter, count,"IA32");
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            int time = 100, maxPo = 1;
            boolean action = false;
            Fighter nearestEnnemy = Function.getInstance().getNearestEnnemy(this.fight, this.fighter);
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

            Fighter longestEnnemy = Function.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, maxPo);//po max+ 1;
            if(longestEnnemy != null) if(longestEnnemy.isHide()) longestEnnemy = null;

            if(this.fighter.getCurPa(this.fight) > 0 && !action) {
                if (Function.getInstance().invocIfPossible(this.fight, this.fighter, this.invocations)) {
                    time = 1000;
                    action = true;
                }
            }

            if(this.fighter.getCurPa(this.fight) > 0 && !action) {
                if (Function.getInstance().invocIfPossibleloin(this.fight, this.fighter, this.invocations,nearestEnnemy)) {
                    time = 1000;
                    action = true;
                }
            }
            if(this.fighter.getCurPa(this.fight) > 0 && !action) {
                if (Function.getInstance().invocIfPossibleloin(this.fight, this.fighter, this.invocations,longestEnnemy)) {
                    time = 1000;
                    action = true;
                }
            }

            if(this.fighter.getCurPa(this.fight) > 0 && !action) {
                if (Function.getInstance().HealIfPossible(this.fight, this.fighter, true, 70) != 0) {
                    time = 1000;
                    action = true;
                }
            }
            if(this.fighter.getCurPa(this.fight) > 0 && !action) {
                if (Function.getInstance().buffIfPossible(this.fight, this.fighter, this.fighter, this.buffs)) {
                    time = 400;
                    action = true;
                }
            }

            if(this.fighter.getCurPa(this.fight) > 0 && longestEnnemy != null && !action) {
                if (Function.getInstance().debuffIfPossible(this.fight, this.fighter, longestEnnemy)) {
                    time = 800;
                    action = true;
                }
            } else if(this.fighter.getCurPa(this.fight) > 0 && nearestEnnemy != null && !action) {
                boolean value = Function.getInstance().debuffIfPossible(this.fight, this.fighter, nearestEnnemy);
                if (value) {
                    time = 800;
                    action = true;
                }
            }

            if(this.fighter.getCurPm(this.fight) > 0 && this.fighter.getCurPa(this.fight) > 0 && !action) {
                int value = Function.getInstance().movetoAttackwithLOS(this.fight, this.fighter,nearestEnnemy,maxPo);
                if(value != 0) {
                    time = value;
                    action = true;
                }
            }

            int MaxAttackPossible = Function.getInstance().getMaxLaunchableAttaque(this.fight, this.fighter);
            for(int i = 0; i < MaxAttackPossible; i++)
            {
                if(this.fighter.getCurPa(this.fight) > 0 && nearestEnnemy != null && !action) {
                    int value = Function.getInstance().attackIfPossible(this.fight, this.fighter, this.highests);
                    if(value != -1) {
                        time = value;
                        action = true;
                        this.attack++;
                    }
                    int value2 = Function.getInstance().attackIfPossible(this.fight, this.fighter, this.cacs);
                    if(value2 != -1) {
                        time = value2;
                        action = true;
                        this.attack++;
                    }
                    if(i == MaxAttackPossible - 1)
                    {
                        if(this.fighter.getCurPm(this.fight) > 0)
                        {
                            value = Function.getInstance().moveFarIfPossible(this.fight, this.fighter);
                            if(value != 0) time = value;
                        }
                    }
                }

            }

            if(this.fighter.getCurPm(this.fight) > 0 && !action) {
                int value = Function.getInstance().moveFarIfPossible(this.fight, this.fighter);
                if(value != 0) {
                    time = value;
                    this.stop = true;
                }
            }

            if(this.fighter.getCurPa(this.fight) == 0 && this.fighter.getCurPm(this.fight) == 0)
                this.stop = true;

            addNext(this::decrementCount, time);
        } else {
            this.stop = true;
        }
    }
}