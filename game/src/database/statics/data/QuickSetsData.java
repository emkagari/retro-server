package database.statics.data;

import com.zaxxer.hikari.HikariDataSource;
import database.statics.AbstractDAO;
import game.world.World;
import other.QuickSets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class QuickSetsData extends AbstractDAO<QuickSets> {

    public QuickSetsData(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void load(Object obj) {}

    @Override
    public boolean update(QuickSets set) {
        String query = "UPDATE `sets` SET `name` = ?, `objects`=?, `icon`= ?, `playerId`= ?, `nb`= ? WHERE `id`= ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement p = conn.prepareStatement(query)) {
            p.setString(1, set.getName());
            p.setString(2, set.getObjects());
            p.setInt(3, set.getIcon());
            p.setInt(4, set.getPlayerId());
            p.setInt(5, set.getNb());
            p.setInt(6, set.getId());
            executeUpdate(p);
            return true;
        } catch (SQLException e) {
            super.sendError("SetsData update", e);
            return false;
        }
    }

    public void load() {
        try (Result result = getData("SELECT * FROM `sets`;")) {
            ResultSet RS = result.getResultSet();
            while (RS.next()) {
                QuickSets set = new QuickSets(RS.getInt("id"), RS.getInt("playerId"), RS.getInt("nb"), RS.getString("name"), RS.getString("objects"), RS.getInt("icon"));
                World.world.addSets(set);
            }
        } catch (SQLException e) {
            super.sendError("SetsData load", e);
        }
    }

    public int getNextId() {
        int guid = 0;
        try (Result result = getData("SELECT id FROM sets ORDER BY id DESC LIMIT 1")) {
            ResultSet RS = result.getResultSet();
            if (!RS.first())
                guid = 1;
            else
                guid = RS.getInt("id") + 1;
        } catch (SQLException e) {
            super.sendError("SetsData getNextId", e);
        }
        return guid;
    }

    public boolean add(QuickSets set) {
        String query = "INSERT INTO sets( `id` ,`playerId`, `nb`, `name`, `objects`, `icon`) VALUES (?,?,?,?,?,?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement p = conn.prepareStatement(query)) {
            p.setInt(1, set.getId());
            p.setInt(2, set.getPlayerId());
            p.setInt(3, set.getNb());
            p.setString(4, set.getName());
            p.setString(5, set.getObjects());
            p.setInt(6, set.getIcon());
            executeUpdate(p);
            return true;
        } catch (SQLException e) {
            super.sendError("SetsData add", e);
        }
        return false;
    }

    public boolean delete(QuickSets set) {
        String query = "DELETE FROM sets WHERE id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement p = conn.prepareStatement(query)) {
            p.setInt(1, set.getId());
            executeUpdate(p);
            return true;
        } catch (SQLException e) {
            super.sendError("SetsData delete", e);
        }
        return false;
    }

    public boolean deletebyPerso(QuickSets set) {
        String query = "DELETE FROM sets WHERE playerid = ? and nb = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement p = conn.prepareStatement(query)) {
            p.setInt(1, set.getPlayerId());
            p.setInt(2, set.getNb());
            executeUpdate(p);
            return true;
        } catch (SQLException e) {
            super.sendError("SetsData deletebyPerso", e);
        }
        return false;
    }

    public boolean updateInfos(QuickSets set) {
        String query = "UPDATE `sets` SET `name` = ?, `objects`=?, `icon`= ? WHERE `id`= ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement p = conn.prepareStatement(query)) {
            p.setString(1, set.getName());
            p.setString(2, set.getObjects());
            p.setInt(3, set.getIcon());
            p.setInt(4, set.getId());
            executeUpdate(p);
            return true;
        } catch (SQLException e) {
            super.sendError("SetsData updateInfos", e);
        }
        return false;
    }


}
