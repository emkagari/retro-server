package database.dynamics.data;

import com.zaxxer.hikari.HikariDataSource;
import database.dynamics.AbstractDAO;
import fight.spells.Effect;
import fight.spells.SpellGrade;
import game.world.World;
import kernel.Main;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SpellEffectData extends AbstractDAO<Effect> {
    public SpellEffectData(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void load(Object obj) {
    }

    @Override
    public boolean update(Effect obj) {
        return false;
    }

    public void load() {
        Result result = null;
        try {

            result = getData("SELECT  * from spells_effect");
            ResultSet RS = result.resultSet;

            while (RS.next()) {
                int spellid = RS.getInt("spellID");
                int gradeid = RS.getInt("gradeID");

                Effect se2 = new Effect(RS.getInt("effectID"), spellid, gradeid, RS.getInt("min"), RS.getInt("max"), RS.getInt("args"), RS.getString("area"), RS.getInt("chance"), RS.getInt("turn"), RS.getBoolean("isCCeffect"), RS.getString("jet"), RS.getInt("effectTarget"), RS.getInt("trigger"), RS.getInt("onHitTrigger"));
                SpellGrade spellG = World.world.getSort(spellid).getSpellGrade(gradeid);
                if (spellG != null) {
                    spellG.addEffectSpell(se2);
                } else {
                    System.out.println("Spellgrade " + gradeid + " non trouvé pour le sort " + spellid + " afin d'ajouté un effet");
                }
            }
        } catch (SQLException e) {
            super.sendError("SpellEffect load", e);
            Main.INSTANCE.stop("SpellEffect load");
        } finally {
            close(result);
        }
    }


}
