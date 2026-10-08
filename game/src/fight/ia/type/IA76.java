package fight.ia.type;

import fight.Fight;
import fight.Fighter;
import fight.ia.AbstractNeedSpell;
import fight.ia.util.Function;
import fight.spells.SpellGrade;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA76 extends AbstractNeedSpell  {

    public IA76(Fight fight, Fighter fighter, byte count) {
        super(fight, fighter, count,"IA76");
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            int time = 100, maxPo = 1;
            boolean action = false;
            Fighter ennemy = Function.getInstance().getNearestEnnemy(this.fight, this.fighter);
            Fighter ennemyBuffed = Function.getInstance().getEnnemyWithBuff(this.fight, this.fighter);

            for(SpellGrade S : this.highests) {
                if (S != null && S.getMaxPO() > maxPo) {
                    maxPo = S.getMaxPO();
                }
            }

            if(this.fighter.getCurPm(this.fight) > 0)
            {
                int value = Function.getInstance().moveToAttackIfPossibleAll(this.fight, this.fighter);
                if ( value != -1) {
                    time = value;
                    action = true;
                }
            }

            if(this.fighter.getCurPa(this.fight) > 0 && !action)
            {
                if(ennemyBuffed != null) {
                    if (Function.getInstance().debuffIfPossiblePerco(this.fight, this.fighter, ennemyBuffed)) {
                        time = 800;
                        action=true;
                    }
                }
                else {
                    if (Function.getInstance().debuffIfPossiblePerco(this.fight, this.fighter, ennemy)) {
                        time = 800;
                        action=true;
                    }
                }

            }

            if(this.fighter.getCurPa(this.fight) > 0 && !action)
            {
                if(Function.getInstance().attackIfPossiblePerco(this.fight, this.fighter, ennemy, this.highests) != 0)
                {
                    time = 500;
                    action = true;
                }
            }
            if(this.fighter.getCurPa(this.fight) > 0 && !action)
            {
                if(Function.getInstance().HealIfPossible(this.fight, this.fighter, false))
                {
                    time = 500;
                    action = true;
                }
            }

            if(this.fighter.getCurPm(this.fight) > 0 && !action)
            {
                if(Function.getInstance().moveFarIfPossible(this.fight, this.fighter) == 0)
                {
                    time = 1000;
                }
            }



            if(this.fighter.getCurPa(this.fight) == 0 && this.fighter.getCurPm(this.fight) == 0) this.stop = true;
            addNext(this::decrementCount, time);
        } else {
            this.stop = true;
        }
    }
}