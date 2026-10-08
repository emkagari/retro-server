package database.statics.data;

import com.zaxxer.hikari.HikariDataSource;
import database.statics.AbstractDAO;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Locos on 15/09/2015.
 */
public class WorldEntityData extends AbstractDAO<Object> {

    private long nextObjectId, nextPetId;
    private int nextMountId, nextQuestId, nextGuildId;

    public WorldEntityData(HikariDataSource dataSource) {
        super(dataSource);
        this.load(null);
    }

    @Override
    public void load(Object obj) {
        try (Result result = getData("SELECT MIN(id) AS min FROM `world.entity.mounts`;")) {
            ResultSet RS = result.getResultSet();
            if (RS.next()) {
                this.nextMountId = RS.getInt("min");
            } else {
                this.nextMountId = -1;
            }
        } catch (SQLException e) {
            logger.error("WorldEntityData load", e);
        }

        try (Result result = getData("SELECT MAX(id) AS max FROM `world.entity.objects`;")) {
            ResultSet RS = result.getResultSet();
            if (RS.next()) {
                this.nextObjectId = RS.getLong("max");
            } else {
                this.nextObjectId = 1;
            }
        } catch (SQLException e) {
            logger.error("WorldEntityData load", e);
        }

        try (Result result = getData("SELECT MAX(id) AS max FROM `world.entity.players.quests`;")) {
            ResultSet RS = result.getResultSet();
            if (RS.next()) {
                this.nextQuestId = RS.getInt("max");
            } else {
                this.nextQuestId = 1;
            }
        } catch (SQLException e) {
            logger.error("WorldEntityData load", e);
        }

        try (Result result = getData("SELECT MAX(id) AS max FROM `world.entity.guilds`;")) {
            ResultSet RS = result.getResultSet();
            if (RS.next()) {
                this.nextGuildId = RS.getInt("max");
            } else {
                this.nextGuildId = 1;
            }
        } catch (SQLException e) {
            logger.error("WorldEntityData load", e);
        }

        try (Result result = getData("SELECT MAX(id) AS max FROM `world.entity.pets`;")) {
            ResultSet RS = result.getResultSet();
            if (RS.next()) {
                this.nextPetId = RS.getLong("max");
            } else {
                this.nextPetId = 1;
            }
        } catch (SQLException e) {
            logger.error("WorldEntityData load", e);
        }
    }

    @Override
    public boolean update(Object obj) {
        return false;
    }

    public synchronized int getNextMountId() {
        return --nextMountId;
    }

    // TODO : A changer on peut essayer de demander a la base de générer le nouvel ID pour éviter les ID non unique
    public long getNextObjectId() {
        /*int nextObjectId = -1;
        try {
            Result result = getData("SELECT nextval('object_id_seq') FROM `sequence_table`;");
            ResultSet RS = result.resultSet;
            if (RS.next()) {
                nextObjectId = RS.getInt(1);
            }
            this.nextObjectId = nextObjectId;
        } catch (SQLException e) {
            e.printStackTrace();
            return ++this.nextObjectId;
            // Gérer l'erreur
        }
        return nextObjectId;*/

        return ++nextObjectId;
    }

    public synchronized int getNextQuestPlayerId() {
        return ++nextQuestId;
    }

    public synchronized int getNextGuildId() {
        return ++nextGuildId;
    }

    public synchronized long getNextPetId() {
        return ++nextPetId;
    }
}
