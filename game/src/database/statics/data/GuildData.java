package database.statics.data;

import com.zaxxer.hikari.HikariDataSource;
import database.statics.AbstractDAO;
import exchange.transfer.DataQueue;
import game.world.World;
import guild.Guild;
import kernel.Main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GuildData extends AbstractDAO<Guild> {

    public GuildData(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void load(Object obj) {
        String query = "SELECT * FROM `world.entity.guilds` WHERE `id` = " + obj + ";";
        try (Result result = getData(query)) {
            ResultSet RS = result.getResultSet();

            while (RS.next())
                World.world.addGuild(new Guild(RS.getInt("id"), RS.getString("name"), RS.getString("emblem"), RS.getInt("lvl"), RS.getLong("xp"), RS.getInt("capital"), RS.getInt("maxCollectors"), RS.getString("spells"), RS.getString("stats"), RS.getLong("date"), RS.getString("announce")), false);
        } catch (SQLException e) {
            super.sendError("GuildData load", e);
        }
    }

    @Override
    public boolean update(Guild guild) {
        String query = "UPDATE `world.entity.guilds` SET `lvl` = ?, `xp` = ?, `capital` = ?, `maxCollectors` = ?, `spells` = ?, `stats` = ?, `announce` = ? WHERE id = ?;";
        try (Connection conn = dataSource.getConnection(); PreparedStatement p = conn.prepareStatement(query)) {
            p.setInt(1, guild.getLvl());
            p.setLong(2, guild.getXp());
            p.setInt(3, guild.getCapital());
            p.setInt(4, guild.getNbCollectors());
            p.setString(5, guild.compileSpell());
            p.setString(6, guild.compileStats());
            p.setString(7, guild.getAnnounce());
            p.setInt(8, guild.getId());
            execute(p);
            return true;
        } catch (SQLException e) {
            super.sendError("GuildData update", e);
        }
        return false;
    }

    public void add(Guild guild) {
        String query = "INSERT INTO `world.entity.guilds` VALUES (?,?,?,1,0,0,0,?,?,?,'');";
        try (Connection conn = dataSource.getConnection(); PreparedStatement p = conn.prepareStatement(query)) {
            p.setInt(1, guild.getId());
            p.setString(2, guild.getName());
            p.setString(3, guild.getEmblem());
            p.setString(4, "462;0|461;0|460;0|459;0|458;0|457;0|456;0|455;0|454;0|453;0|452;0|451;0|");
            p.setString(5, "176;100|158;1000|124;100|");
            p.setLong(6, guild.getDate());
            execute(p);
        } catch (SQLException e) {
            super.sendError("GuildData add", e);
        }
    }

    public void delete(int id) {
        String query = "DELETE FROM `world.entity.guilds` WHERE `id` = ?;";
        try (Connection conn = dataSource.getConnection(); PreparedStatement p = conn.prepareStatement(query)) {
            p.setInt(1, id);
            execute(p);
        } catch (SQLException e) {
            super.sendError("GuildData delete", e);
        }
    }

    public int getNextId() {
        final DataQueue.Queue<Integer> queue = new DataQueue.Queue<>((byte) 4);
        try {
            synchronized(queue) {
                long count = DataQueue.count();
                DataQueue.queues.put(count, queue);
                Main.exchangeClient.send("DI" + queue.getType() + count);
                queue.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return queue.getValue();
    }
}
