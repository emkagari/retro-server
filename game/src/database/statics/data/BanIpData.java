package database.statics.data;

import com.zaxxer.hikari.HikariDataSource;
import database.statics.AbstractDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BanIpData extends AbstractDAO<Object> {
    public BanIpData(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void load(Object obj) {
    }

    @Override
    public boolean update(Object obj) {
        return false;
    }

    public boolean add(String ip) {
        String query = "INSERT INTO `banip` VALUES (?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, ip);
            executeUpdate(statement);
            return true;
        } catch (SQLException e) {
            super.sendError("BanipData add", e);
        }
        return false;
    }

    public boolean delete(String ip) {
        String query = "DELETE FROM `banip` WHERE `ip` = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, ip);
            executeUpdate(ps);
            return true;
        } catch (SQLException e) {
            super.sendError("BanipData delete", e);
        }
        return false;
    }
}
