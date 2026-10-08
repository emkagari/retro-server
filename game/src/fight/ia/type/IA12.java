package fight.ia.type;

import fight.Fight;
import fight.Fighter;
import fight.ia.AbstractNeedSpell;
import fight.ia.util.Function;
import fight.spells.SpellGrade;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA12 extends AbstractNeedSpell  {

    private int attack = 0;
    private boolean boost = false;

    public IA12(Fight fight, Fighter fighter, byte count) {
        super(fight, fighter, count,"IA12");
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            Fighter ennemy = Function.getInstance().getNearestEnnemy(this.fight, this.fighter);
            int PA = this.fighter.getCurPa(this.fight), PM = this.fighter.getCurPm(this.fight), time = 100, maxPo = 1;
            boolean action = false;

            if (this.fighter.getMob().getPa() < PA) this.boost = true;

            maxPo = Function.getInstance().getMaxPoUsableSpell(this.fighter, this.highests);

            Fighter target = Function.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 3);

            if (target != null)
                if (target.isHide())
                    target = null;


            PA = this.fighter.getCurPa(this.fight);
            PM = this.fighter.getCurPm(this.fight);

            if (PA > 0 && ennemy != null) {
                int num = 0;
                while (PA > 3) {
                    num = Function.getInstance().attackIfPossible(this.fight, this.fighter, this.highests);
                    if (num != 0) {
                        PM = this.fighter.getCurPm(this.fight);
                        if(PM > 0)
                        {
                            num = Function.getInstance().movediagIfPossible(this.fight, this.fighter, ennemy);
                            if(num == 0)
                            {
                                boolean numable = Function.getInstance().moveNearIfPossible(this.fight, this.fighter, ennemy);
                                if(!numable)
                                {
                                    time = num;
                                    action = true;
                                    break;
                                }
                            }
                        }
                        else {
                            time = num;
                            action = true;
                            this.attack++;
                            break;
                        }
                    } else {
                        PA = this.fighter.getCurPa(this.fight);
                    }
                }
                if (num == 0 && PM > 0) {
                    num = Function.getInstance().moveFarIfPossible(this.fight, this.fighter);
                    if (num != 0) {
                        time = num;
                        action = true;
                    }
                }
            }

            if ((this.fighter.getCurPa(this.fight) == 0 || this.fighter.getCurPa(this.fight) < 4) && this.fighter.getCurPm(this.fight) == 0)
                this.stop = true;

            addNext(this::decrementCount, time);
        } else {
            this.stop = true;
        }
    }
}