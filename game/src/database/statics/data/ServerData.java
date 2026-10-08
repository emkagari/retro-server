package database.statics.data;

import com.zaxxer.hikari.HikariDataSource;
import database.statics.AbstractDAO;
import kernel.Config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ServerData extends AbstractDAO<Object> {
    public ServerData(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void load(Object obj) {
    }

    @Override
    public boolean update(Object obj) {
        return false;
    }

    public void updateTime(long time) {
        String query = "UPDATE servers SET `uptime` = ? WHERE `id` = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement p = conn.prepareStatement(query)) {
            p.setLong(1, time);
            p.setInt(2, Config.INSTANCE.getSERVER_ID());
            executeUpdate(p);
        } catch (SQLException e) {
            sendError("ServerData updateTime", e);
        }
    }

    public void loggedZero() {
        String query = "UPDATE players SET `logged` = 0 WHERE `server` = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement p = conn.prepareStatement(query)) {
            p.setInt(1, Config.INSTANCE.getSERVER_ID());
            executeUpdate(p);
        } catch (SQLException e) {
            sendError("ServerData loggedZero", e);
        }
    }
}
