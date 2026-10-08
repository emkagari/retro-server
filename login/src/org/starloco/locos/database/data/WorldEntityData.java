package org.starloco.locos.database.data;

import com.zaxxer.hikari.HikariDataSource;
import org.starloco.locos.database.AbstractDAO;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Locos on 15/09/2015.
 */
public class WorldEntityData extends AbstractDAO<Object> {
    private long nextObjectId;
    private int nextMountId, nextQuestId, nextGuildId, nextPetId;

    public WorldEntityData(HikariDataSource dataSource) {
        super(dataSource);
        this.load(null);
    }

    @Override
    public Object load(Object obj) {
        Result result = null;
        try {
            result = getData("SELECT MIN(id) AS min FROM `world.entity.mounts`;");
            ResultSet RS = result.resultSet;
            if (RS.next()) this.nextMountId = RS.getInt("min");
            else this.nextMountId = -1;
        } catch (SQLException e) {
            logger.error("WorldEntityData load", e);
        } finally {
            close(result);
        }
        try {
            result = getData("SELECT MAX(id) AS max FROM `world.entity.objects`;");
            ResultSet RS = result.resultSet;
            if (RS.next()) this.nextObjectId = RS.getLong("max");
            else this.nextObjectId = 1;
        } catch (SQLException e) {
            logger.error("WorldEntityData load", e);
        } finally {
            close(result);
        }
        try {
            result = getData("SELECT MAX(id) AS max FROM `world.entity.players.quests`;");
            ResultSet RS = result.resultSet;
            if (RS.next()) this.nextQuestId = RS.getInt("max");
            else this.nextQuestId = 1;
        } catch (SQLException e) {
            logger.error("WorldEntityData load", e);
        } finally {
            close(result);
        }
        try {
            result = getData("SELECT MAX(id) AS max FROM `world.entity.guilds`;");
            ResultSet RS = result.resultSet;
            if (RS.next()) this.nextGuildId = RS.getInt("max");
            else this.nextGuildId = 1;
        } catch (SQLException e) {
            logger.error("WorldEntityData load", e);
        } finally {
            close(result);
        }
        try {
            result = getData("SELECT MAX(id) AS max FROM `world.entity.pets`;");
            ResultSet RS = result.resultSet;
            if (RS.next()) this.nextPetId = RS.getInt("max");
            else this.nextPetId = 1;
        } catch (SQLException e) {
            logger.error("WorldEntityData load", e);
        } finally {
            close(result);
        }

        return null;
    }

    @Override
    public boolean update(Object obj) {
        return false;
    }

    public synchronized int getNextMountId() {
        return --nextMountId;
    }

    public synchronized long getNextObjectId() {
        return ++nextObjectId;
    }

    public synchronized int getNextQuestPlayerId() {
        return ++nextQuestId;
    }

    public synchronized int getNextGuildId() {
        return ++nextGuildId;
    }

    public synchronized int getNextPetId() {
        return ++nextPetId;
    }
}
