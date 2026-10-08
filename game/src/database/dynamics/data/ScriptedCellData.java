package database.dynamics.data;

import com.zaxxer.hikari.HikariDataSource;
import area.map.GameCase;
import area.map.GameMap;
import database.dynamics.AbstractDAO;
import game.GameServer;
import game.world.World;
import kernel.Main;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ScriptedCellData extends AbstractDAO<Object> {
    public ScriptedCellData(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void load(Object obj) {
    }

    @Override
    public boolean update(Object obj) {
        return false;
    }

    public int load() {
        return load("");
    }

    /**
     * Ré-applique les cellules scriptees d'UNE map.
     *
     * Les actions sont portees par les `GameCase`. Des qu'on reconstruit les
     * cellules d'une map (rechargement a chaud apres edition), elles partent
     * avec les anciennes cellules : il faut les reposer.
     */
    public int loadForMap(short mapId) {
        return load(" WHERE `MapID` = " + mapId);
    }

    private int load(String filtre) {
        Result result = null;
        int nbr = 0;
        try {
            result = getData("SELECT * FROM `scripted_cells`" + filtre);
            ResultSet RS = result.resultSet;
            while (RS.next()) {
                short mapId = RS.getShort("MapID");
                int cellId = RS.getInt("CellID");
                GameMap map = World.world.getMap(mapId);
                if (map == null)
                    continue;

                GameCase cell = map.getCase(cellId);
                if (cell == null)
                    continue;

                switch (RS.getInt("EventID")) {
                    case 1: // Stop sur la case (triggers)
                        cell.addOnCellStopAction(RS.getInt("ActionID"), RS.getString("ActionsArgs"), RS.getString("Conditions"), null);
                        break;

                    default:
                        GameServer.a("Pas d'action connue" + RS.getInt("EventID"));
                        break;
                }
                nbr++;
            }
        } catch (SQLException e) {
            super.sendError("Scripted_cellData load", e);
            // Au demarrage, un echec est fatal. Sur un rechargement a chaud
            // d'une seule map, non : on ne tue pas un serveur en production
            // parce qu'une commande d'admin a bute sur la base.
            if (filtre.isEmpty()) Main.INSTANCE.stop("unknown");
        } finally {
            close(result);
        }
        return nbr;
    }

    public boolean update(int mapID1, int cellID1, int action, int event,
                          String args, String cond) {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("REPLACE INTO `scripted_cells` VALUES (?,?,?,?,?,?)");
            p.setInt(1, mapID1);
            p.setInt(2, cellID1);
            p.setInt(3, action);
            p.setInt(4, event);
            p.setString(5, args);
            p.setString(6, cond);
            execute(p);
            return true;
        } catch (SQLException e) {
            super.sendError("Scripted_cellData update", e);
        } finally {
            close(p);
        }
        return false;
    }


    public boolean delete(int mapID, int cellID) {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("DELETE FROM `scripted_cells` WHERE `MapID` = ? AND `CellID` = ?");
            p.setInt(1, mapID);
            p.setInt(2, cellID);
            execute(p);
            return true;
        } catch (SQLException e) {
            super.sendError("Scripted_cellData delete", e);
        } finally {
            close(p);
        }
        return false;
    }
}
