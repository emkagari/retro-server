package database.statics.data;

import client.Player;
import com.zaxxer.hikari.HikariDataSource;
import database.Database;
import database.statics.AbstractDAO;
import entity.mount.Mount;
import game.world.World;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MountData extends AbstractDAO<Mount> {

    public MountData(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void load(Object obj) {
        try (Result result = getData("SELECT * from `world.entity.mounts` WHERE `id` = " + String.valueOf((int) obj) + ";")) {
            ResultSet RS = result.getResultSet();
            while (RS.next()) {
                World.world.addMount(new Mount(RS.getInt("id"), RS.getInt("color"), RS.getInt("sex"), RS.getInt("amour"), RS.getInt("endurance"), RS.getInt("level"), RS.getLong("xp"),
                        RS.getString("name"), RS.getInt("fatigue"), RS.getInt("energy"), RS.getInt("reproductions"), RS.getInt("maturity"), RS.getInt("serenity"), RS.getString("objects"),
                        RS.getString("ancestors"), RS.getString("capacitys"), RS.getInt("size"), RS.getInt("cell"), RS.getShort("map"), RS.getInt("owner"), RS.getInt("orientation"),
                        RS.getLong("fecundatedDate"), RS.getInt("couple"), RS.getInt("savage")));
            }
        } catch (SQLException e) {
            super.sendError("MountData load", e);
        }
    }

    @Override
    public boolean update(Mount mount) {
        String query = "UPDATE `world.entity.mounts` SET `name` = ?, `xp` = ?, `level` = ?, `endurance` = ?, `amour` = ?, `maturity` = ?, `serenity` = ?, `reproductions` = ?, " +
                "`fatigue` = ?, `energy` = ?, `ancestors` = ?, `objects` = ?, `owner` = ?, `capacitys` = ?, `size` = ?, `cell` = ?, `map` = ?, " +
                "`orientation` = ?, `fecundatedDate` = ?, `couple` = ? WHERE `id` = ?;";
        try (Connection conn = dataSource.getConnection(); PreparedStatement p = conn.prepareStatement(query)) {
            p.setString(1, mount.getName());
            p.setLong(2, mount.getExp());
            p.setInt(3, mount.getLevel());
            p.setInt(4, mount.getEndurance());
            p.setInt(5, mount.getAmour());
            p.setInt(6, mount.getMaturity());
            p.setInt(7, mount.getState());
            p.setInt(8, mount.getReproduction());
            p.setInt(9, mount.getFatigue());
            p.setInt(10, mount.getEnergy());
            p.setString(11, mount.getAncestors());
            p.setString(12, mount.parseObjectsToString());
            p.setInt(13, mount.getOwner());
            p.setString(14, mount.parseCapacitysToString());
            p.setInt(15, mount.getSize());
            p.setInt(16, mount.getCellId());
            p.setInt(17, mount.getMapId());
            p.setInt(18, mount.getOrientation());
            p.setLong(19, mount.getFecundatedDate());
            p.setInt(20, mount.getCouple());
            p.setInt(21, mount.getId());
            executeUpdate(p);
            return true;
        } catch (SQLException e) {
            super.sendError("MountData update", e);
        }
        return false;
    }

    public void delete(int id) {
        String query = "DELETE FROM `world.entity.mounts` WHERE `id` = ?;";
        try (Connection conn = dataSource.getConnection(); PreparedStatement p = conn.prepareStatement(query)) {
            p.setInt(1, id);
            executeUpdate(p);
        } catch (SQLException e) {
            super.sendError("MountData delete", e);
        }
    }

    public void delete(Player player) {
        this.delete(player.getMount().getId());
        World.world.delDragoByID(player.getMount().getId());
        player.setMountGiveXp(0);
        player.setMount(null);
        Database.getStatics().getPlayerData().update(player);
    }

    public void add(Mount mount) {
        String query = "INSERT INTO `world.entity.mounts`(`id`, `color`, `sex`, `name`, `xp`, `level`, `endurance`, `amour`, `maturity`, `serenity`, `reproductions`, `fatigue`, `energy`, " +
                "`objects`, `ancestors`, `capacitys`, `size`, `map`, `cell`, `owner`, `orientation`, `fecundatedDate`, `couple`, `savage`) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement p = conn.prepareStatement(query)) {
            p.setInt(1, mount.getId());
            p.setInt(2, mount.getColor());
            p.setInt(3, mount.getSex());
            p.setString(4, mount.getName());
            p.setLong(5, mount.getExp());
            p.setInt(6, mount.getLevel());
            p.setInt(7, mount.getEndurance());
            p.setInt(8, mount.getAmour());
            p.setInt(9, mount.getMaturity());
            p.setInt(10, mount.getState());
            p.setInt(11, mount.getReproduction());
            p.setInt(12, mount.getFatigue());
            p.setInt(13, mount.getEnergy());
            p.setString(14, mount.parseObjectsToString());
            p.setString(15, mount.getAncestors());
            p.setString(16, mount.parseCapacitysToString());
            p.setInt(17, mount.getSize());
            p.setInt(18, mount.getMapId());
            p.setInt(19, mount.getCellId());
            p.setInt(20, mount.getOwner());
            p.setInt(21, mount.getOrientation());
            p.setLong(22, mount.getFecundatedDate());
            p.setInt(23, mount.getCouple());
            p.setInt(24, mount.getSavage());
            executeUpdate(p);
        } catch (SQLException e) {
            super.sendError("MountData add", e);
        }
    }

    public int getNextId() {
        return Database.getStatics().getWorldEntityData().getNextMountId();
    }
}
