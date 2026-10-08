package database.statics.data;

import client.Classe;
import com.zaxxer.hikari.HikariDataSource;
import database.statics.AbstractDAO;
import game.world.World;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ClasseData extends AbstractDAO<Classe> {
    public ClasseData(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void load(Object obj) {
    }

    @Override
    public boolean update(Classe classe) {
        return false;
    }

    public int load() {
        int numero = 0;
        String query = "SELECT * from classes";

        try (Result result = getData(query)) {
            if (result != null) {
                ResultSet RS = result.getResultSet();
                while (RS.next()) {
                    World.world.addClasse(new Classe(RS.getInt("id"), RS.getString("name"),
                            RS.getString("gfxs"), RS.getString("size"), RS.getInt("mapInit")
                            , RS.getInt("cellInit"), RS.getInt("pdv")
                            , RS.getString("boostVita"), RS.getString("boostSage")
                            , RS.getString("boostForce"), RS.getString("boostIntel"), RS.getString("boostChance")
                            , RS.getString("boostAgi"), RS.getString("statsInit")
                            , RS.getString("sorts")));
                    numero++;
                }
            }
        } catch (SQLException e) {
            super.sendError("ClasseData load", e);
        }
        return numero;
    }
}
