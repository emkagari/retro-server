package fight.traps;

import area.map.GameCase;
import common.PathFinding;
import common.SocketManager;
import fight.Fight;
import fight.Fighter;
import fight.spells.Spell;
import fight.spells.SpellGrade;
import game.world.World;

import java.util.List;

public class Glyph {

    private Fighter caster;
    private GameCase cell;
    private List<GameCase> zonecells;
    private String area;
    private byte size;
    private int spell;
    private SpellGrade trapSpell;
    private byte duration;
    private Fight fight;
    private int color;

    public Glyph(Fight fight, Fighter caster, GameCase cell, String area,
                 SpellGrade trapSpell, byte duration, int spell, int color) {
        this.fight = fight;
        this.caster = caster;
        this.cell = cell;
        this.spell = spell;
        this.area = area;
        this.zonecells = PathFinding.getCellListFromAreaString(fight.getMap(), cell.getId(), caster.getCell().getId(), this.area);;
        this.size = (byte) World.world.getCryptManager().getIntByHashedValue(this.area.charAt(1));
        this.trapSpell = trapSpell;
        this.duration = duration;
        this.color = color;
    }

    public Fighter getCaster() {
        return this.caster;
    }

    public GameCase getCell() {
        return this.cell;
    }

    public byte getSize() {
        return this.size;
    }

    public int getSpell() {
        return this.spell;
    }

    public List<GameCase> getCellsZone() {
        return this.zonecells;
    }

    public int decrementDuration() {
        //if(this.duration == -1) return -1;
        this.duration--;
        return this.duration;
    }

    public int getColor() {
        return this.color;
    }

    public void onTrapped(Fighter target) {
            Spell spell = World.world.getSort(this.spell);
            String str = this.spell + "," + this.cell.getId() + ", 0, 1, 1," + this.caster.getId();
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this.fight, 7, 307, target.getId() + "", str);
            this.trapSpell.applySpellEffectToFight(this.fight, this.caster, target.getCell(), false, true);
            this.fight.verifIfTeamAllDead();
    }

    private void desappear(final int team)
    {
        final StringBuilder str = new StringBuilder();
        final StringBuilder str2 = new StringBuilder();
        str.append("GDZ-").append(this.cell.getId()).append(";").append(this.size).append(";").append(this.color);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this.fight, team, 999, "" + this.caster.getId() + "", str.toString());
        str2.append("GDC").append(this.cell.getId());
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this.fight, team, 999, "" + this.caster.getId() + "", str2.toString());
    }

    public void appear() {
        StringBuilder str = new StringBuilder();
        StringBuilder str2 = new StringBuilder();
        str.append("GDZ");
        str2.append("GDC");
        if(!(this.area.substring(0,1).equals("C"))) {
            for (GameCase zonecell : zonecells) {
                if(zonecell.isWalkable(false)) {
                    str.append("+" + zonecell.getId()).append(";0;").append(this.color).append("|");
                    str2.append(zonecell.getId()).append(";Haaaaaaaaa3005;").append("|");
                }
            }
            str.deleteCharAt(str.length() - 1);
            str2.deleteCharAt(str2.length() - 1);
        }
        else{
            str.append("+" + this.cell.getId() + ";" + this.size + ";" + this.color);
            str2.append(this.cell.getId()).append(";Haaaaaaaaa3005;");
        }
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, caster.getId() + "", str.toString(), 999);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, caster.getId() + "", str2.toString(), 999);
    }

    public void disappear() {
        StringBuilder str = new StringBuilder();
        StringBuilder str2 = new StringBuilder();
        str.append("GDZ");
        str2.append("GDC");

        if(!(this.area.substring(0,1).equals("C"))) {
            for (GameCase zonecell : zonecells) {
                str.append("-"+zonecell.getId()).append(";0;").append(this.color).append("|");
                str2.append(zonecell.getId()).append("|");
            }
            str.deleteCharAt(str.length() - 1);
            str2.deleteCharAt(str2.length() - 1);
        }
        else {
            str.append("-" + this.cell.getId() + ";" + this.size + ";" + this.color);
            str2.append(this.cell.getId());
        }
        SocketManager.GAME_SEND_GDZ_PACKET_TO_FIGHT_DONE(this.fight, 7, str.toString());
        SocketManager.GAME_SEND_GDC_PACKET_TO_FIGHT_DONE(this.fight, 7, str2.toString());

    }
}