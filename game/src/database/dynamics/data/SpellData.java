package database.dynamics.data;

import com.zaxxer.hikari.HikariDataSource;
import database.dynamics.AbstractDAO;
import fight.spells.Spell;
import game.world.World;
import kernel.Main;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SpellData extends AbstractDAO<Spell> {
    public SpellData(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void load(Object obj) {
    }

    @Override
    public boolean update(Spell obj) {
        return false;
    }

    public boolean updateType(Spell spell) {
        if (spell == null) {
            super.sendError("SpellData update", new Exception("spell is null"));
            return false;
        }

        PreparedStatement p = null;
        try {
            p = getPreparedStatement("UPDATE `spell` SET `type`= ? WHERE `spell`.`id` = ? LIMIT 1");
            p.setInt(1, spell.getType());
            p.setInt(2, spell.getSpellID());
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

    public void load() {
        Result result = null,result2 ,result3,result4;
        try {
            result = getData("SELECT  * from spells");
            ResultSet RS = result.resultSet;
            boolean modif = false;
            while (RS.next()) {
                int id = RS.getInt("id");
                Spell spell = null;
                if (World.world.getSort(id) != null) {
                    spell = World.world.getSort(id);
                    spell.setInfos(RS.getInt("sprite"), RS.getString("spriteinfo"), RS.getInt("type"), RS.getInt("duration"));
                    modif = true;
                } else {
                    spell = new Spell(id, RS.getString("name"), RS.getInt("sprite"), RS.getString("spriteinfo"), RS.getInt("type"), RS.getInt("duration"));
                    World.world.addSort(spell);
                }
            }
            /*if (modif)
                for (Monster monster : World.world.getMonstres())
                    monster.getGrades().values().forEach(Monster.MobGrade::refresh);*/
        } catch (SQLException e) {
            super.sendError("SortData load", e);
            Main.INSTANCE.stop("SortData load");
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
