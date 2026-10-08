package database.dynamics.data;

import com.zaxxer.hikari.HikariDataSource;
import database.dynamics.AbstractDAO;
import fight.spells.EffectTrigger;
import fight.spells.Spell;
import game.world.World;
import kernel.Main;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TriggerSpellEffectData extends AbstractDAO<EffectTrigger> {
    public TriggerSpellEffectData(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void load(Object obj) {
    }

    @Override
    public boolean update(EffectTrigger obj) {
        return false;
    }


    public void load() {
        Result result = null;
        try {
            result = getData("SELECT  * from spells_effect_triggers");
            ResultSet RS4 = result.resultSet;
            while (RS4.next()) {
                int id = RS4.getInt("trigger_id");
                EffectTrigger trigger = null;
                if (World.world.getEffectTrigger(id) != null) {
                    trigger = World.world.getEffectTrigger(id);
                    trigger.setInfos(RS4.getString("effects_triggering"), RS4.getInt("target"), RS4.getInt("isunbuffable"), RS4.getString("description"));
                }
                else {
                    trigger = new EffectTrigger(id, RS4.getString("effects_triggering"), RS4.getInt("target"), RS4.getInt("isunbuffable"), RS4.getString("description"));
                    World.world.addEffectTrigger(trigger);
                }
            }
            close(result);

        } catch (SQLException e) {
            super.sendError("TriggerSpellEffectData load", e);
            Main.INSTANCE.stop("loading TriggerSpellEffectData");
        } finally {
            close(result);
        }
    }

    public boolean updateOnHitEffect(Spell spell,int trigger,int onHitTrigger,int effectID) {
        if (spell == null) {
            super.sendError("SpellData update", new Exception("spell is null"));
            return false;
        }

        PreparedStatement p = null;
        try {
            p = getPreparedStatement("UPDATE `spells_effect` SET `trigger`= ?, `onHitTrigger`= ? WHERE `spellID` = ? && `effectID` = ?");
            p.setInt(1, trigger);
            p.setInt(2, onHitTrigger);
            p.setInt(3, spell.getSpellID());
            p.setInt(4, effectID);
            execute(p);
        }
        catch (Exception e) {
            super.sendError("PlayerData update", e);
        }
        finally {
            close(p);
        }
        return true;
    }

    public boolean updateTargetEffect(Spell spell,int target,int effectID) {
        if (spell == null) {
            super.sendError("SpellData update", new Exception("spell is null"));
            return false;
        }

        PreparedStatement p = null;
        try {
            p = getPreparedStatement("UPDATE `spells_effect` SET `effectTarget`= ? WHERE `spellID` = ? && `effectID` = ?");
            p.setInt(1, target);
            p.setInt(2, spell.getSpellID());
            p.setInt(3, effectID);
            execute(p);
        }
        catch (Exception e) {
            super.sendError("PlayerData update", e);
        }
        finally {
            close(p);
        }
        return true;
    }
}
