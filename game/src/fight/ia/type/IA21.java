package fight.ia.type;

import fight.Fight;
import fight.Fighter;
import fight.ia.AbstractNeedSpell;
import fight.ia.util.Function;
import fight.spells.SpellGrade;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA21 extends AbstractNeedSpell  {

    public IA21(Fight fight, Fighter fighter, byte count) {
        super(fight, fighter, count,"IA21");
    }

    @Override
    public void apply() {
        @SuppressWarnings("unused")
        int time = 100, maxPo = 1;
        boolean action = false;
        @SuppressWarnings("unused")
        Fighter E = Function.getNearestEnnemy2(this.fight, this.fighter);

        maxPo = Function.getInstance().getMaxPoUsableSpell(this.fighter, this.highests);

        Fighter firstEnnemy = Function.getNearestEnnemynbrcasemaxNoHide(this.fight, this.fighter, 1, maxPo + 1);
        Fighter secondEnnemy = Function.getNearestEnnemynbrcasemaxNoHide(this.fight, this.fighter, 0, 2);

        if(this.fighter.getCurPa(this.fight) > 0 && !action) {
            if (Function.getInstance().buffIfPossible(this.fight, this.fighter, this.fighter, this.buffs)) {
                time = 400;
                action = true;
            }
        }

        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            Function.getInstance().buffIfPossibleKrala(this.fight, this.fighter, this.fighter);
            Function.getInstance().invoctantaIfPossible(this.fight, this.fighter);
            //System.out.println("La elle a invoqué si elle peut " );
            if(this.fighter.getCurPa(this.fight) > 0 && firstEnnemy != null && secondEnnemy == null && !action) {
                int num = Function.getInstance().attackIfPossible(this.fight, this.fighter, this.highests);
                if(num != 0) {
                    time = num;
                    action = true;
                }
            } else if(this.fighter.getCurPa(this.fight) > 0 && secondEnnemy != null && !action) {
                int num = Function.getInstance().attackIfPossible(this.fight, this.fighter, this.cacs);
                if(num != 0) {
                    time = num;
                    action = true;
                }
            }
            addNext(this::decrementCount, 1000);
        } else {
            this.stop = true;
        }
    }
}