package fight.ia.type;

import fight.Fight;
import fight.Fighter;
import fight.ia.AbstractIA;
import fight.ia.util.Function;
import fight.spells.SpellGrade;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA73 extends AbstractIA  {

    public IA73(Fight fight, Fighter fighter, byte count) {
        super(fight, fighter, count,"IA73");
    }

    @Override
    public void apply() {
        if (this.count > 0 && this.fighter.canPlay() && !this.stop) {
            Fighter target = Function.getInstance().getNearestEnnemy(this.fight, this.fighter);
            Fighter invocator = this.fighter.getInvocator();
            if (target == null) return;

            if(!Function.getInstance().buffIfPossible(this.fight, this.fighter, invocator))
            {
                Function.getInstance().buffIfPossible(this.fight, this.fighter, this.fighter);
            }

            int value = Function.getInstance().moveToAttackIfPossible(this.fight, this.fighter), cellId = value - (value / 1000) * 1000;
            SpellGrade spellStats = this.fighter.getMob().getSpells().get(value / 1000);

            if (cellId != -1) {
                if (this.fight.canCastSpell1(this.fighter, spellStats, this.fighter.getCell(), cellId))
                    this.fight.tryCastSpell(this.fighter, spellStats, cellId);
            } else if (Function.getInstance().moveFarIfPossible(this.fight, this.fighter) != 0) {
                this.stop = true;
            }

            addNext(this::decrementCount, 800);
        } else {
            this.stop = true;
        }
    }
}