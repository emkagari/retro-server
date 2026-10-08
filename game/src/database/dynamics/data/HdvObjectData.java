package database.dynamics.data;

import com.zaxxer.hikari.HikariDataSource;
import database.Database;
import database.dynamics.AbstractDAO;
import game.world.World;
import hdv.Hdv;
import hdv.HdvEntry;
import object.GameObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HdvObjectData extends AbstractDAO<Object> {
    public HdvObjectData(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void load(Object obj) {
    }

    @Override
    public boolean update(Object obj) {
        return false;
    }

    public void load() {
        Result result = null;
        try {
            result = getData("SELECT * FROM `hdvs_items`");
            ResultSet RS = result.resultSet;
            while (RS.next()) {
                Hdv tempHdv = World.world.getHdv(RS.getInt("map"));
                if (tempHdv == null)
                    continue;
                if (World.world.getGameObject(RS.getLong("itemID")) == null) {
                    Database.getDynamics().getHdvObjectData().delete(RS.getInt("id"));
                    continue;
                }
                tempHdv.addEntry(new HdvEntry(RS.getInt("id"), RS.getInt("price"), RS.getByte("count"), RS.getInt("ownerGuid"), World.world.getGameObject(RS.getLong("itemID"))), true);
                World.world.setNextObjectHdvId(RS.getInt("id"));
            }
        } catch (SQLException e) {
            super.sendError("Hdvs_itemsData load", e);
        } finally {
            close(result);
        }
    }

    public boolean add(HdvEntry toAdd) {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("INSERT INTO `hdvs_items` (`map`,`ownerGuid`,`price`,`count`,`itemID`) VALUES(?,?,?,?,?)");
            p.setInt(1, -1);
            p.setInt(2, toAdd.getOwner());
            p.setInt(3, toAdd.getPrice());
            p.setInt(4, toAdd.getAmount(false));
            p.setLong(5, toAdd.getGameObject().getGuid());
            execute(p);
            Database.getDynamics().getObjectTemplateData().saveAvgprice(toAdd.getGameObject().getTemplate());
            return true;
        } catch (SQLException e) {
            super.sendError("Hdvs_itemsData add", e);
        } finally {
            close(p);
        }
        return false;
    }

    public boolean updateHDVID(GameObject object, long oldID) {
        if (object == null || object.getTemplate() == null)
            return false;

        String query = "UPDATE `hdvs_items` SET `itemID` = ? WHERE `itemID` = ?;";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement p = conn.prepareStatement(query)) {
            p.setLong(1, object.getGuid());
            p.setLong(2, oldID);
            execute(p);
            return true;
        } catch (SQLException e) {
            sendError("ObjectData update", e);
        }
        return false;
    }

    public void delete(long id) {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("DELETE FROM hdvs_items WHERE itemID = ?");
            p.setLong(1, id);
            execute(p);
            if (World.world.getGameObject(id) != null)
                Database.getDynamics().getObjectTemplateData().saveAvgprice(World.world.getGameObject(id).getTemplate());
        } catch (SQLException e) {
            super.sendError("Hdvs_itemsData delete", e);
        } finally {
            close(p);
        }
    }
}
