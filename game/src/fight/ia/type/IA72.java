package fight.ia.type;

import fight.Fight;
import fight.Fighter;
import fight.ia.AbstractNeedSpell;
import fight.ia.util.Function;
import fight.spells.Spell;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA72 extends AbstractNeedSpell  {
    private byte attack = 0;

    public IA72(Fight fight, Fighter fighter, byte count) {
        super(fight, fighter, count,"IA72");
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            int time = 10, maxPo = 1;
            boolean action = false;
            Fighter ennemy = Function.getInstance().getNearestEnnemy(this.fight, this.fighter);

            maxPo = Function.getInstance().getMaxPoUsableSpell(this.fighter, this.highests);

            Fighter C = Function.getInstance().getNearestEnnemynbrcasemaxNoHide(this.fight, this.fighter, 0, 2);//2 = po min 1 + 1;
            Fighter L = Function.getInstance().getNearestEnnemynbrcasemaxNoHide(this.fight, this.fighter, 1, maxPo + 1);// pomax +1;

            if(this.fighter.getCurPa(this.fight) > 0) {
                if (Function.getInstance().invocIfPossible(this.fight, this.fighter, this.invocations)) {
                    time = 2000;
                    action = true;
                }
            }

            if(this.fighter.getCurPa(this.fight) > 0 && !action )
            {
                int value = Function.getInstance().attackIfPossible(this.fight, this.fighter, this.highests);
                if(value != -1) {
                    time = 800;
                    action = true;
                }
            }

            if(this.fighter.getCurPm(this.fight) > 0 && !action ) {
                int value = Function.getInstance().moveenfaceIfPossibleWithLOS(this.fight, this.fighter, ennemy, maxPo + 1 + this.fighter.getBuffValue(117) - this.fighter.getBuffValue(116));
                if(value != 0) {
                    time = value;
                    action = true;
                }
            }

            if(this.fighter.getCurPa(this.fight) > 0 && C != null && !action) {
                if (Function.getInstance().debuffIfPossible(this.fight, this.fighter, C)) {
                    time = 800;
                    action = true;
                }
            } else if(this.fighter.getCurPa(this.fight) > 0 && L != null && !action) {
                boolean value = Function.getInstance().debuffIfPossible(this.fight, this.fighter, L);
                if (value) {
                    time = 800;
                    action = true;
                }
            }

            if(this.fighter.getCurPa(this.fight) > 0 && !action) {
                if (Function.getInstance().buffIfPossible(this.fight, this.fighter, this.fighter, this.buffs)) {
                    time = 800;
                    action = true;
                }
            }


            if(this.fighter.getCurPm(this.fight) > 0 && !action) {
                int value = Function.getInstance().moveFarIfPossible(this.fight, this.fighter);
                if(value != 0){
                    time = value;
                    this.stop = true;
                }
            }

            if(this.fighter.getCurPa(this.fight) == 0 && this.fighter.getCurPm(this.fight) == 0) this.stop = true;
            addNext(this::decrementCount, time);
        } else {
            this.stop = true;
        }
    }
}