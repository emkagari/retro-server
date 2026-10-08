package database.statics.data;

import com.zaxxer.hikari.HikariDataSource;
import database.Database;
import database.statics.AbstractDAO;
import game.world.World;
import kernel.Main;
import object.GameObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ObjectData extends AbstractDAO<GameObject> {

    public ObjectData(HikariDataSource dataSource) {
        super(dataSource);
    }

    public void load() {
        String query = "SELECT * FROM `world.entity.objects`;";
        try (Result result = getData(query)) {
            if (result != null) {
                ResultSet RS = result.getResultSet();
                while (RS.next()) {
                    long id = RS.getLong("id");
                    int template = RS.getInt("template");
                    int quantity = RS.getInt("quantity");
                    int position = RS.getInt("position");
                    String stats = RS.getString("stats");
                    int puit = RS.getInt("puit");
                    int rarity = RS.getInt("rarity");
                    int mimibiote = RS.getInt("mimibiote");
                    if (quantity == 0) continue;
                    World.world.addGameObjectInWorld(World.world.newObjet(id, template, quantity, position, stats, puit, rarity, mimibiote));
                }
            }
        } catch (SQLException e) {
            sendError("ObjectData load", e);
            Main.INSTANCE.stop("unknown");
        }
    }

    @Override
    public void load(Object obj) {
        String query = "SELECT * FROM `world.entity.objects` WHERE `id` IN (" + obj + ");";
        try (Result result = getData(query)) {
            if (result != null) {
                ResultSet RS = result.getResultSet();
                while (RS.next()) {
                    long id = RS.getLong("id");
                    int template = RS.getInt("template");
                    int quantity = RS.getInt("quantity");
                    int position = RS.getInt("position");
                    String stats = RS.getString("stats");
                    int puit = RS.getInt("puit");
                    int rarity = RS.getInt("rarity");
                    int mimibiote = RS.getInt("mimibiote");
                    if (quantity == 0) continue;
                    World.world.addGameObjectInWorld(World.world.newObjet(id, template, quantity, position, stats, puit, rarity, mimibiote));
                }
            }
        } catch (SQLException e) {
            sendError("ObjectData load", e);
            Main.INSTANCE.stop("unknown");
        }
    }

    @Override
    public boolean update(GameObject object) {
        if (object == null || object.getTemplate() == null)
            return false;

        String query = "UPDATE `world.entity.objects` SET `template` = ?, `quantity` = ?, `position` = ?, `puit` = ?, `rarity` = ?, `mimibiote` = ?, `stats` = ? WHERE `id` = ?;";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement p = conn.prepareStatement(query)) {
            p.setInt(1, object.getTemplate().getId());
            p.setInt(2, object.getQuantity());
            p.setInt(3, object.getPosition());
            p.setInt(4, object.getPuit());
            p.setInt(5, object.getRarity());
            p.setInt(6, object.getMimibiote());
            p.setString(7, object.parseToSave());
            p.setLong(8, object.getGuid());
            executeUpdate(p);
            return true;
        } catch (SQLException e) {
            sendError("ObjectData update", e);
        }
        return false;
    }

    public boolean updateID(GameObject object,long oldID) {
        if (object == null || object.getTemplate() == null)
            return false;

        String query = "UPDATE `world.entity.objects` SET `id` = ? WHERE `id` = ?;";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement p = conn.prepareStatement(query)) {
            p.setLong(1, object.getGuid());
            p.setLong(2, oldID);
            executeUpdate(p);
            Database.getDynamics().getHdvObjectData().updateHDVID(object,oldID);
            return true;
        } catch (SQLException e) {
            sendError("ObjectData update", e);
        }
        return false;
    }

    public void insert(GameObject object) {
        if (object == null || object.getTemplate() == null) {
            sendError("ObjectData insert", new Exception("Object or template is null"));
            return;
        }

        long id = object.getGuid();
        int checkResult;

         checkResult = existsAndSameTemplate(id, object.getTemplate().getId());
        if (checkResult == 1) { // ID exists and template matches, so update
            update(object);
        }
        else if (checkResult == 0) { // Insert the object with the unique ID
            String query = "INSERT INTO `world.entity.objects`(`id`, `template`, `quantity`, `position`, `stats`, `puit`, `rarity`, `mimibiote`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement p = conn.prepareStatement(query)) {
                p.setLong(1, object.getGuid());
                p.setInt(2, object.getTemplate().getId());
                p.setInt(3, object.getQuantity());
                p.setInt(4, object.getPosition());
                p.setString(5, object.parseToSave());
                p.setInt(6, object.getPuit());
                p.setInt(7, object.getRarity());
                p.setInt(8, object.getMimibiote());
                executeUpdate(p);
            } catch (SQLException e) {
                sendError("ObjectData insert", e);
            }
        }
        else if (checkResult == 2){
            System.out.println("Etrangement dans un cas d'object écrasé par un nouveau" + object.getGuid());
            do{
                object.setId();
            }
            while (existsAndSameTemplate(object.getGuid(), object.getTemplate().getId()) ==0 );

            String query = "INSERT INTO `world.entity.objects`(`id`, `template`, `quantity`, `position`, `stats`, `puit`, `rarity`, `mimibiote`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement p = conn.prepareStatement(query)) {
                p.setLong(1, object.getGuid());
                p.setInt(2, object.getTemplate().getId());
                p.setInt(3, object.getQuantity());
                p.setInt(4, object.getPosition());
                p.setString(5, object.parseToSave());
                p.setInt(6, object.getPuit());
                p.setInt(7, object.getRarity());
                p.setInt(8, object.getMimibiote());
                executeUpdate(p);
            } catch (SQLException e) {
                sendError("ObjectData insert", e);
            }
        }
    }

    public int existsAndSameTemplate(long id, int templateId) {
        String query = "SELECT template FROM `world.entity.objects` WHERE `id` = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int existingTemplateId = rs.getInt("template");
                    if (existingTemplateId == templateId) {
                        return 1; // ID exists and template matches
                    } else {
                        return 2; // ID exists but template does not match
                    }
                }
            }
        } catch (SQLException e) {
            sendError("ObjectData existsAndSameTemplate", e);
        }
        return 0; // ID does not exist
    }

    public void delete(Long id) {
        String query = "DELETE FROM `world.entity.objects` WHERE id = ?;";
        try (Connection conn = dataSource.getConnection() ; PreparedStatement p = conn.prepareStatement(query) ) {
            p.setLong(1, id);
            executeUpdate(p);
        } catch (SQLException e) {
            sendError("ObjectData delete", e);
        }
    }

    // TODO : changer cette gestion de merde des IDs qui écrase certains items
    public long getNextId() {
        return Database.getStatics().getWorldEntityData().getNextObjectId();
    }
}
