package fight.ia.type;

import fight.Fight;
import fight.Fighter;
import fight.ia.AbstractNeedSpell;
import fight.ia.util.Function;
import fight.spells.Spell;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA79 extends AbstractNeedSpell  {

    private boolean boost = false, heal = false;

    public IA79(Fight fight, Fighter fighter, byte count) {
        super(fight, fighter, count,"IA79");
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            int time = 100, maxPo = 1;
            boolean action = false;

            Fighter A = Function.getInstance().getNearestFriendNoInvok(this.fight, this.fighter);

            maxPo = Function.getInstance().getMaxPoUsableSpell(this.fighter, this.highests);

            Fighter L = Function.getInstance().getNearestinvocateurnbrcasemax(this.fight, this.fighter, 1, maxPo + 1);// pomax +1;

            if(L != null) if(L.isHide()) L = null;

            if(this.fighter.getCurPm(this.fight) > 0 && !action && this.boost) {
                int value = Function.getInstance().moveFarIfPossible(this.fight, this.fighter);
                if(value != 0) {
                    time = value;
                    action = true;
                }
            }
            if(this.fighter.getCurPa(this.fight) > 0 && !action && L != null && !this.boost) {
                if (Function.getInstance().buffIfPossible(this.fight, this.fighter, (L == null ? A : L), this.buffs)) {
                    time = 1000;
                    action = true;
                    this.boost = true;
                }
            }

            if(this.fighter.getCurPm(this.fight) > 0 && !action) {
                int value = Function.getInstance().moveFarIfPossible(this.fight, this.fighter);
                if(value != 0) time = value;
            }

            if(this.fighter.getCurPa(this.fight) == 0 && this.fighter.getCurPm(this.fight) == 0 || heal && boost && this.fighter.getCurPm(this.fight) == 0) this.stop = true;
            addNext(this::decrementCount, time);
        } else {
            this.stop = true;
        }
    }
}