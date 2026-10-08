package database.dynamics.data;

import com.zaxxer.hikari.HikariDataSource;
import database.dynamics.AbstractDAO;
import fight.spells.Spell;
import fight.spells.SpellGrade;
import game.world.World;
import kernel.Main;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class SpellGradeData extends AbstractDAO<SpellGrade> {
    public SpellGradeData(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void load(Object obj) {
    }

    @Override
    public boolean update(SpellGrade obj) {
        return false;
    }

    public void load() {
        Result result = null;
        try {

            result = getData("SELECT * from spells_grade");
            ResultSet RS = result.resultSet;
            while (RS.next()) {
                int spellid = RS.getInt("spellID");
                int gradeid = RS.getInt("gradeID");
                String[] sForbiddenStates = RS.getString("statesForbidden").split(";");
                ArrayList<Integer> ForbiddenStates= new ArrayList<>();
                for (String forbiddenState : sForbiddenStates) {
                    ForbiddenStates.add(Integer.parseInt(forbiddenState));
                }

                SpellGrade spellgrade = new SpellGrade(spellid,gradeid,RS.getInt("paCost"),RS.getInt("poMin"),RS.getInt("poMax"),RS.getInt("ratioCC"),RS.getInt("ratioEC"),RS.getBoolean("isLine"),RS.getBoolean("needLOS"),RS.getBoolean("needEmptyC"),RS.getBoolean("isPoModif"),RS.getInt("maxByTurn"),RS.getInt("maxByTarget"),RS.getInt("CD"),RS.getInt("lvlLearn"),RS.getBoolean("endTurn"),ForbiddenStates,RS.getInt("stateNeed"));
                World.world.addSortGrade(spellgrade);

                Spell spell = World.world.getSort(spellid);
                if (spell != null) {
                    spell.addSortStats(gradeid,spellgrade);
                }
                else{
                    System.out.println("Sort " + spellid + " non trouvé pour ajouté son SpellGrade");
                }
            }
        } catch (SQLException e) {
            super.sendError("SpellGrades load", e);
            Main.INSTANCE.stop("SpellGrades load");
        } finally {
            close(result);
        }
    }



}
