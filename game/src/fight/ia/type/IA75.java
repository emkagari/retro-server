package fight.ia.type;

import fight.Fight;
import fight.Fighter;
import fight.ia.AbstractNeedSpell;
import fight.ia.util.Function;

public class IA75 extends AbstractNeedSpell {
    public IA75(Fight fight, Fighter fighter, byte count) {
        super(fight, fighter, count,"IA75");
    }

    public void apply() {

        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            int time = 1000;
            Fighter target = Function.getInstance().getNearestEnnemy(this.fight, this.fighter);
            if(this.fighter.getCurPa(this.fight) > 0 & target != null) {
                if (Function.getInstance().buffIfPossible(this.fight, this.fighter, this.fighter, this.buffs)) {
                    time = 1500;
                }
            }
            if(this.fighter.getCurPa(this.fight) > 0)
            {
                if(Function.getInstance().HealIfPossible(this.fight, this.fighter, false))
                {
                    time = 1500;
                }
            }
            if(this.fighter.getCurPa(this.fight) > 0) {
                if (Function.getInstance().invocIfPossibleloin(this.fight, this.fighter, this.invocations,target)) {
                    time = 1500;
                }
            }
            if(this.fighter.getCurPm(this.fight) > 0)
            {
                if(Function.getInstance().moveNearIfPossible(this.fight, this.fighter, target))
                {
                    time = 1500;
                }
            }
            if(this.fighter.getCurPa(this.fight) > 0)
            {
                if(Function.getInstance().attackIfPossibleTot(this.fight, this.fighter, target))
                {
                    time = 1500;
                }
            }
            if ((this.fighter.getCurPa(this.fight) == 0 || this.fighter.getCurPa(this.fight) < 4) && this.fighter.getCurPm(this.fight) == 0)
                this.stop = true;
            addNext(this::decrementCount, time);
        }else{
            this.stop = true;
        }
    }
}
