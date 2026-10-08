package fight.ia.type;

import fight.Fight;
import fight.Fighter;
import fight.ia.AbstractNeedSpell;
import fight.ia.util.Function;
import fight.spells.Spell;
import fight.spells.SpellGrade;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA39 extends AbstractNeedSpell  {

    private byte attack = 0;

    public IA39(Fight fight, Fighter fighter, byte count) {
        super(fight, fighter, count,"IA39");
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            int time = 100, maxPo = 1, minPo =0;
            Fighter ennemy = Function.getInstance().getNearestEnnemy(this.fight, this.fighter);
            maxPo = Function.getInstance().getMaxPoUsableSpell(this.fighter, this.highests);

            if (this.fighter.getCurPa(this.fight) > 0) {
                Function.getInstance().attackIfPossibleCroca(this.fight, this.fighter, ennemy);
            }
            Function.getInstance().invocIfPossible(this.fight, this.fighter);
            Function.getInstance().buffIfPossible(this.fight, this.fighter, this.fighter);
            if(this.fighter.getCurPm(this.fight) > 0)
            {
                Function.getInstance().moveenfaceIfPossibleWithLOS(this.fight, this.fighter, ennemy, maxPo + 1 + this.fighter.getBuffValue(117));
                    if(this.fighter.getCurPa(this.fight) > 0) {
                        Function.getInstance().attackIfPossibleCroca(this.fight, this.fighter, ennemy);
                    }
            }
            if (this.fighter.getCurPa(this.fight) > 0) {
                Function.getInstance().attackIfPossibleCroca(this.fight, this.fighter, ennemy);
            }
            if (this.fighter.getCurPm(this.fight) > 0) {
                Function.getInstance().moveNearIfPossible(this.fight, this.fighter, ennemy);
                if(this.fighter.getCurPa(this.fight) > 0) {
                    Function.getInstance().attackIfPossibleCroca(this.fight, this.fighter, ennemy);
                }
            }
            if (this.fighter.getCurPa(this.fight) > 0) {
                Function.getInstance().attackIfPossibleCroca(this.fight, this.fighter, ennemy);
            }
            addNext(this::decrementCount, time);
            if (this.fighter.getCurPa(this.fight) == 0 && this.fighter.getCurPm(this.fight) == 0) this.stop = true;
            addNext(this::decrementCount, time);
        } else {
            this.stop = true;
        }
    }
}