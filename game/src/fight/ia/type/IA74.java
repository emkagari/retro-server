package fight.ia.type;

import fight.Fight;
import fight.Fighter;
import fight.ia.AbstractNeedSpell;
import fight.ia.util.Function;
import fight.spells.SpellGrade;

import java.util.ArrayList;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA74 extends AbstractNeedSpell  {

    public IA74(Fight fight, Fighter fighter, byte count) {
        super(fight, fighter, count,"IA74");
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            int time = 100;
            boolean action = false;
            Fighter E = Function.getInstance().getNearestEnnemy(this.fight, this.fighter);

            ArrayList<SpellGrade> spells = new ArrayList<>();
            spells.addAll(this.fighter.getMob().getSpells().values());

            Fighter ennemy = Function.getInstance().getNearestEnnemy(this.fight, this.fighter);

            if(ennemy != null) if(ennemy.isHide()) ennemy = null;
            int PA = this.fighter.getCurPa(this.fight), PM = this.fighter.getCurPm(this.fight);
            if(PM > 0)
            {
                int value = Function.getInstance().movecacIfPossible(this.fight, this.fighter, ennemy);
                if(value == 0)
                {
                    boolean boolvalue = Function.getInstance().moveNearIfPossible(this.fight, this.fighter, ennemy);
                    if(!boolvalue)
                    {
                        while(PA > 3) {
                            value = Function.getInstance().attackIfPossible(this.fight, this.fighter, spells);
                            if (value != 0) {
                                if(value == 800)
                                {
                                    PA = this.fighter.getCurPa(this.fight);
                                }
                                else {
                                    time = 200;
                                    break;
                                }
                            } else{
                                PA = this.fighter.getCurPa(this.fight);
                            }
                        }
                    } else {
                        while(PA > 3) {
                            value = Function.getInstance().attackIfPossible(this.fight, this.fighter, spells);
                            if (value != 0) {
                                if(value == 800)
                                {
                                    PA = this.fighter.getCurPa(this.fight);
                                }
                                else {
                                    time = 200;
                                    break;
                                }
                            } else{
                                PA = this.fighter.getCurPa(this.fight);
                            }
                        }
                    }
                } else {
                    while(PA > 3) {
                        value = Function.getInstance().attackIfPossible(this.fight, this.fighter, spells);
                        if (value != 0) {
                            if(value == 800)
                            {
                                PA = this.fighter.getCurPa(this.fight);
                            }
                            else {
                                time = 200;
                                break;
                            }
                        } else{
                            PA = this.fighter.getCurPa(this.fight);
                        }
                    }
                }
            }

            if((this.fighter.getCurPa(this.fight) == 0 || PA < 4) && this.fighter.getCurPm(this.fight) == 0) this.stop = true;
            addNext(this::decrementCount, time);
        } else {
            this.stop = true;
        }
    }
}