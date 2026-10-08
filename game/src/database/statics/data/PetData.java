package database.statics.data;

import com.zaxxer.hikari.HikariDataSource;
import database.Database;
import database.statics.AbstractDAO;
import entity.pet.PetEntry;
import game.world.World;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PetData extends AbstractDAO<PetEntry> {

    public PetData(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void load(Object obj) {}

    @Override
    public boolean update(PetEntry pets) {
        String query = "UPDATE `world.entity.pets` SET `lastEatDate` = ?, `quantityEat` = ?, `pdv` = ?, `corpulence` = ?, `isEPO` = ? WHERE `id` = ?;";
        try (Connection conn = dataSource.getConnection(); PreparedStatement p = conn.prepareStatement(query)) {
            p.setLong(1, pets.getLastEatDate());
            p.setInt(2, pets.getQuaEat());
            p.setInt(3, pets.getPdv());
            p.setInt(4, pets.getCorpulence());
            p.setInt(5, (pets.getIsEupeoh() ? 1 : 0));
            p.setLong(6, pets.getObjectId());
            executeUpdate(p);
            return true;
        } catch (SQLException e) {
            super.sendError("PetData update", e);
        }
        return false;
    }

    public boolean updateID(PetEntry pets,long oldID) {
        String query = "UPDATE `world.entity.pets` SET `id` = ? WHERE `id` = ?;";
        try (Connection conn = dataSource.getConnection(); PreparedStatement p = conn.prepareStatement(query)) {
            p.setLong(1, pets.getObjectId());
            p.setLong(2, oldID);
            executeUpdate(p);
            return true;
        } catch (SQLException e) {
            super.sendError("PetData update", e);
        }
        return false;
    }


    public int load() {
        String query = "SELECT * FROM `world.entity.pets`;";
        int count = 0;
        try (Result result = getData(query)) {
            ResultSet RS = result.getResultSet();
            while (RS.next()) {
                count++;
                World.world.addPetsEntry(new PetEntry(RS.getLong("id"), RS.getInt("template"), RS.getLong("lastEatDate"), RS.getInt("quantityEat"), RS.getInt("pdv"), RS.getInt("corpulence"), (RS.getInt("isEPO") == 1)));
            }
        } catch (SQLException e) {
            super.sendError("PetData load", e);
        }
        return count;
    }

    public void add(long id, long lastEatDate, int template) {
        String query = "INSERT INTO `world.entity.pets`(`id`, `template`, `lastEatDate`, `quantityEat`, `pdv`, `corpulence`, `isEPO`) VALUES (?, ?, ?, ?, ?, ?, ?);";
        try (Connection conn = dataSource.getConnection(); PreparedStatement p = conn.prepareStatement(query)) {
            p.setLong(1, id);
            p.setInt(2, template);
            p.setLong(3, lastEatDate);
            p.setInt(4, 0);
            p.setInt(5, 10);
            p.setInt(6, 0);
            p.setInt(7, 0);
            executeUpdate(p);
        } catch (SQLException e) {
            super.sendError("PetData add", e);
        }
    }

    public void delete(Long id) {
        String query = "DELETE FROM `world.entity.pets` WHERE `id` = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement p = conn.prepareStatement(query)) {
            p.setLong(1, id);
            executeUpdate(p);
        } catch (SQLException e) {
            super.sendError("PetData delete", e);
        }
    }

    public long getNextId() {
        return Database.getStatics().getWorldEntityData().getNextPetId();
    }
}
