package database.statics.data;

import com.zaxxer.hikari.HikariDataSource;
import database.statics.AbstractDAO;
import object.GameObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ObvejivanData extends AbstractDAO<GameObject> {

    public ObvejivanData(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void load(Object obj) {}

    @Override
    public boolean update(GameObject obj) {
        return false;
    }

    public void add(GameObject obvijevan, GameObject object) {
        String query = "INSERT INTO `world.entity.obvijevans`(`id`, `template`) VALUES(?, ?);";
        try (Connection conn = dataSource.getConnection(); PreparedStatement p = conn.prepareStatement(query)) {
            p.setLong(1, object.getGuid());
            p.setInt(2, obvijevan.getTemplate().getId());
            executeUpdate(p);
        } catch (Exception e) {
            sendError("ObvejivanData add", e);
        }
    }

    public int getAndDelete(GameObject object, boolean delete) {
        String selectQuery = "SELECT * FROM `world.entity.obvijevans` WHERE `id` = ?;";
        String deleteQuery = "DELETE FROM `world.entity.obvijevans` WHERE id = ?;";
        int template = -1;

        try (Connection conn = dataSource.getConnection(); PreparedStatement p = conn.prepareStatement(selectQuery)) {
            p.setLong(1, object.getGuid());
            try (ResultSet resultSet = p.executeQuery()) {
                if (resultSet.next()) {
                    template = resultSet.getInt("template");
                    if (delete) {
                        try (Connection conn2 = dataSource.getConnection(); PreparedStatement deletePs = conn2.prepareStatement(deleteQuery)) {
                            deletePs.setLong(1, object.getGuid());
                            executeUpdate(deletePs);
                        }
                        catch (SQLException e) {
                            sendError("ObvejivanData Delete", e);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            sendError("ObvejivanData get", e);
        }
        return template;
    }
}
