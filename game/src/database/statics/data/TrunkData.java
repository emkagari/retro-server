package database.statics.data;

import com.zaxxer.hikari.HikariDataSource;
import area.map.entity.Trunk;
import database.Database;
import database.statics.AbstractDAO;
import game.world.World;
import kernel.Main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TrunkData extends AbstractDAO<Trunk> {

    public TrunkData(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void load(Object obj) {
    }

    @Override
    public boolean update(Trunk t) {
        return false;
    }

    public int load() {
        String query = "SELECT * from coffres";
        int nbr = 0;
        try (Result result = getData(query)) {
            if (result != null && result.getResultSet() != null) {
                ResultSet RS = result.getResultSet();
                while (RS.next()) {
                    Trunk trunk = new Trunk(RS.getInt("id"), RS.getInt("id_house"), RS.getShort("mapid"), RS.getInt("cellid"));
                    World.world.addTrunk(trunk);
                    Database.getDynamics().getTrunkData().exist(trunk);
                    nbr++;
                }
            }
        } catch (SQLException e) {
            sendError("CoffreData load", e);
        }
        return nbr;
    }

    public void insert(Trunk trunk) {
        String query = "INSERT INTO `coffres` (`id`, `id_house`, `mapid`, `cellid`) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement statement = conn.prepareStatement(query) ) {
            statement.setInt(1, trunk.getId());
            statement.setInt(2, trunk.getHouseId());
            statement.setInt(3, trunk.getMapId());
            statement.setInt(4, trunk.getCellId());
            executeUpdate(statement);

            Database.getDynamics().getTrunkData().insert(trunk);
        } catch (SQLException e) {
            sendError("Coffre insert", e);
        }
    }

    public int getNextId() {
        String query = "SELECT MAX(id) AS max FROM `coffres`";
        int guid = -1;
        try (Result result = getData(query)) {
            if (result != null && result.getResultSet() != null) {
                ResultSet RS = result.getResultSet();
                if (RS.first()) {
                    guid = RS.getInt("max") + 1;
                }
            }
        } catch (SQLException e) {
            sendError("CoffreData getNextId", e);
            Main.INSTANCE.stop(e.getMessage());
        }
        return guid;
    }
}
