package fight.traps;

import area.map.GameCase;
import common.PathFinding;
import common.SocketManager;
import fight.Fight;
import fight.Fighter;
import fight.spells.Effect;
import fight.spells.SpellGrade;
import kernel.Constant;

import java.util.ArrayList;
import java.util.List;

public class Trap {
    private final Fighter caster;
    private final GameCase cell;
    private final byte size;
    private final int spell;
    private final SpellGrade trapSpell;
    private final Fight fight;
    private final int color;
    private boolean isUnHide = false;
    private byte teamUnHide = -1;
    private boolean isPushing = false;
    private final  byte level;
    private final short animationSpell;

    public Trap(Fight fight, Fighter caster, GameCase cell, byte size, SpellGrade trapSpell, int spell, byte level, int color) {
        this.fight = fight;
        this.caster = caster;
        this.cell = cell;
        this.spell = spell;
        this.size = size;
        this.trapSpell = trapSpell;
        this.color = color;
        this.level = level;
        this.animationSpell = Constant.getTrapsAnimation(spell);
        for(final Effect se : trapSpell.getEffects())
            if(se.getEffectID() == 5)
            {
                this.isPushing = true;
                break;
            }
    }

    public int getSpell() {
        return this.spell;
    }

    public GameCase getCell() {
        return this.cell;
    }

    public byte getSize() {
        return this.size;
    }

    public Fighter getCaster() {
        return this.caster;
    }

    public boolean isUnHide() {
        return this.isUnHide;
    }

    public void setIsUnHide(Fighter f) {
        this.isUnHide = true;
        this.teamUnHide = (byte) f.getTeam();
    }

    public int getColor() {
        return this.color;
    }

    public void desappear() {
        this.desappear(this.caster.getTeam() + 1);
        if (!this.isUnHide) return;
        this.desappear(this.teamUnHide + 1);
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

    public void appear(Fighter f) {
        StringBuilder str = new StringBuilder();
        StringBuilder str2 = new StringBuilder();

        int team = f.getTeam() + 1;
        str.append("GDZ+").append(this.cell.getId()).append(";").append(this.size).append(";").append(this.color);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this.fight, team, 999, this.caster.getId()
                + "", str.toString(), 999);
        str2.append("GDC").append(this.cell.getId()).append(";Haaaaaaaaz3005;");
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this.fight, team, 999, this.caster.getId()
                + "", str2.toString(), 999);
    }

    public void onTraped(final Fighter target) {
        this.fight.getAllTraps().remove(this);
        this.desappear();
        final String str = "" + this.spell + "," + this.cell.getId() + "," + this.animationSpell + "," + this.level + ",1," + this.caster.getId();
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this.fight, 7, 306, "" + target.getId() + "", str);
        final Fighter fakeCaster = this.caster.getPlayer() == null ? new Fighter(this.fight, this.caster.getMob()) : new Fighter(this.fight, this.caster.getPlayer());
        fakeCaster.setCell(this.cell);
        final GameCase gc = this.getSize() > 0 ? this.cell : target.getCell();
        this.trapSpell.applySpellEffectToFight(this.fight, fakeCaster, gc, false, true);

        this.fight.verifIfTeamAllDead();
    }

    public boolean isPushing() {
        return isPushing;
    }

    public static void doTraps(final Fight fight, final Fighter fighter) {
        if(fighter.isDead()) return;
        final List<Trap> traps = new ArrayList<Trap>(fight.getAllTraps());
        final short currentCell = (short) fighter.getCell().getId();
        short idTrapPushing = -1;
        for (short i = 0; i < traps.size(); ++i) {
            final Trap trap = traps.get(i);
            if(trap.isPushing())
            {
                // On prend le premier piège qui pousse. Cela permet de faire de gros reseau
                if(idTrapPushing == -1 && PathFinding.getDistanceBetween(fight.getMap(), trap.getCell().getId(), currentCell) <= trap.getSize())
                    idTrapPushing = i;
                continue;
            }
            if (PathFinding.getDistanceBetween(fight.getMap(), trap.getCell().getId(), currentCell) <= trap.getSize())
                trap.onTraped(fighter);
            if (fighter.isDead() || fight.getState() == Constant.FIGHT_STATE_FINISHED)
                return;
        }
        if(idTrapPushing != -1)
            traps.get(idTrapPushing).onTraped(fighter);

    }

    public static boolean checkPushingTraps(final Fight fight, final Fighter fighter) {
        if(fighter.isDead()) return false;
        final List<Trap> traps = new ArrayList<>(fight.getAllTraps());
        final short currentCell = (short) fighter.getCell().getId();
        for (short i = 0; i < traps.size(); ++i) {
            final Trap trap = traps.get(i);
            if (trap.isPushing() && PathFinding.getDistanceBetween(fight.getMap(), trap.getCell().getId(), currentCell) <= trap.getSize())
                return true;
        }
        return false;
    }

    public int getSpellID() { return spell;}

    public SpellGrade getTrapSpell() { return trapSpell;
    }
}

