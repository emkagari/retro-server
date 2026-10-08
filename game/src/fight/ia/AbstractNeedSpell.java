package fight.ia;

import fight.Fight;
import fight.Fighter;
import fight.spells.SpellGrade;
import game.world.World;
import guild.Guild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Locos on 04/10/2015.
 */
public abstract class AbstractNeedSpell extends AbstractIA {

    protected List<SpellGrade> buffs, glyphs, invocations, cacs, highests, moveable, heals;

    public AbstractNeedSpell(Fight fight, Fighter fighter, byte count,String IA) {
        super(fight, fighter, count,IA+" - NeedSpell");

        if(fighter.isCollector())
        {
            Map<String, List<SpellGrade>> spellPerco = getPercoSpells(fighter);
            if(spellPerco != null) {
                this.buffs = spellPerco.get("BUFF");
                this.heals = spellPerco.get("BUFF");
                this.glyphs = spellPerco.get("GLYPH");
                this.invocations = spellPerco.get("INVOCATION");
                this.cacs = spellPerco.get("CAC");
                this.highests = spellPerco.get("HIGHEST");
                this.moveable = spellPerco.get("DEPLACEMENT");
            }
        }
        else {
            this.buffs = AbstractNeedSpell.getListSpellOf(fighter, "BUFF");
            this.heals = AbstractNeedSpell.getListSpellOf(fighter, "HEAL");
            this.glyphs = AbstractNeedSpell.getListSpellOf(fighter, "GLYPH");
            this.invocations = AbstractNeedSpell.getListSpellOf(fighter, "INVOCATION");
            this.cacs = AbstractNeedSpell.getListSpellOf(fighter, "CAC");
            this.highests = AbstractNeedSpell.getListSpellOf(fighter, "HIGHEST");
            this.moveable = AbstractNeedSpell.getListSpellOf(fighter, "DEPLACEMENT");
        }
    }

    private static List<SpellGrade> getListSpellOf(Fighter fighter, String type) {
        final List<SpellGrade> spells = new ArrayList<>();
            for (SpellGrade spell : fighter.getMob().getSpells().values()) {
                if (spells.contains(spell)) continue;
                switch (type) {
                    case "BUFF":
                        if (spell.getTypeSwitchSpellEffects() == 1) spells.add(spell);
                        break;
                    case "DEPLACEMENT":
                        if (spell.getTypeSwitchSpellEffects() == 5) spells.add(spell);
                        break;
                    case "HEAL":
                        if (spell.getTypeSwitchSpellEffects() == 3) spells.add(spell);
                        break;
                    case "GLYPH":
                        if (spell.getTypeSwitchSpellEffects() == 4) spells.add(spell);
                        break;
                    case "INVOCATION":
                        if (spell.getTypeSwitchSpellEffects() == 2) spells.add(spell);
                        break;
                    case "CAC":
                        if (spell.getTypeSwitchSpellEffects() == 0) {
                            boolean effect = false;
                           /* for (Effect spellEffect : spell.getEffects())
                                if (spellEffect.getEffectID() == 4 || spellEffect.getEffectID() == 6)
                                    effect = true;*/
                            if (!effect && spell.getMaxPO() < 3) spells.add(spell);
                        }
                        break;
                    case "HIGHEST":
                        if (spell.getTypeSwitchSpellEffects() == 0) {
                            boolean effect = false;
                            /*for (Effect spellEffect : spell.getEffects())
                                if (spellEffect.getEffectID() == 4 || spellEffect.getEffectID() == 6)
                                    effect = true;*/
                            if (effect && spell.getSpellID() != 805) continue;
                            if (spell.getMaxPO() > 1) spells.add(spell);
                        }
                        break;
                }
            }
        return spells;
    }

    private Map<String, List<SpellGrade>> getPercoSpells(Fighter fighter) {
        Guild guild = World.world.getGuild(fighter.getCollector().getGuildId());
        if(guild != null)
        {
            Map<Integer, SpellGrade> spellsPerco = guild.getSpells();
            Map<String, List<SpellGrade>> spellsExploit = new HashMap<>();
            List<SpellGrade> spellBuff = new ArrayList<>();
            List<SpellGrade> spellHighest = new ArrayList<>();
            List<SpellGrade> spellGlyph = new ArrayList<>();
            List<SpellGrade> spellInvocation = new ArrayList<>();
            List<SpellGrade> spellMove = new ArrayList<>();
            List<SpellGrade> spellCac = new ArrayList<>();
            for(SpellGrade spell :  spellsPerco.values())
            {
                if(spell != null) {
                    if (spell.getLevel() > 0) {
                        if (spell.getSpellID() == 451 | spell.getSpellID() == 452 | spell.getSpellID() == 453 | spell.getSpellID() == 454 | spell.getSpellID() == 459 | spell.getSpellID() == 461) {
                            spellBuff.add(spell);
                        } else if (spell.getSpellID() == 455 | spell.getSpellID() == 456 | spell.getSpellID() == 457 | spell.getSpellID() == 458 | spell.getSpellID() == 460 | spell.getSpellID() == 462) {
                            spellHighest.add(spell);
                        }
                    }
                }
            }
            spellsExploit.put("BUFF", spellBuff);
            spellsExploit.put("HIGHEST", spellHighest);
            spellsExploit.put("GLYPH", spellGlyph);
            spellsExploit.put("INVOCATION", spellInvocation);
            spellsExploit.put("CAC", spellCac);
            spellsExploit.put("DEPLACEMENT", spellMove);
            return  spellsExploit;
        }
        return null;
    }
}
