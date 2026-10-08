package database.dynamics.data;

import com.zaxxer.hikari.HikariDataSource;
import area.map.GameMap;
import database.dynamics.AbstractDAO;
import game.world.World;
import kernel.Constant;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MapData extends AbstractDAO<GameMap> {
    public MapData(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void load(Object obj) {
    }

    @Override
    public boolean update(GameMap obj) {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("UPDATE `maps` SET `places` = ?, `numgroup` = ? WHERE id = ?");
            p.setString(1, obj.getPlaces());
            p.setInt(2, obj.getMaxGroupNumb());
            p.setInt(3, obj.getId());
            execute(p);
            return true;
        } catch (SQLException e) {
            super.sendError("MapData update", e);
        } finally {
            close(p);
        }
        return false;
    }

    public boolean updateGs(GameMap map) {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("UPDATE `maps` SET `numgroup` = ?, `minSize` = ?, `fixSize` = ?, `maxSize` = ? WHERE id = ?");
            p.setInt(1, map.getMaxGroupNumb());
            p.setInt(2, map.getMinSize());
            p.setInt(3, map.getFixSize());
            p.setInt(4, map.getMaxSize());
            p.setInt(5, map.getId());
            execute(p);
            return true;
        } catch (SQLException e) {
            super.sendError("MapData updateGs", e);
        } finally {
            close(p);
        }
        return false;
    }
    public boolean updateCapabilities(GameMap map) {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("UPDATE `maps` SET `capabilities` = ? WHERE id = ?");
            p.setInt(1, map.getCapabilities());
            p.setInt(2, map.getId());
            execute(p);
            return true;
        } catch (SQLException e) {
            super.sendError("MapData updateForbidden", e);
        } finally {
            close(p);
        }
        return false;
    }
    /*public boolean updateForbidden(GameMap map) {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("UPDATE `maps` SET `forbidden` = ? WHERE id = ?");
            p.setString(1, map.getForbidden());
            p.setInt(2, map.getId());
            execute(p);
            return true;
        } catch (SQLException e) {
            super.sendError("MapData updateForbidden", e);
        } finally {
            close(p);
        }
        return false;
    }*/

    public boolean updateFightCells(GameMap map)
    {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("UPDATE `maps` SET `places` = ? WHERE id = ?");
            p.setString(1, map.getPlaces());
            p.setInt(2, map.getId());
            execute(p);
            return true;
        } catch (SQLException e) {
            super.sendError("MapData updateFightCells", e);
        } finally {
            close(p);
        }
        return false;
    }
    public boolean updateMobsNormal(GameMap map, String monsters)
    {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("UPDATE `maps` SET `monsters` = ? WHERE id = ?");
            p.setString(1, monsters);
            p.setInt(2, map.getId());
            execute(p);
            return true;
        } catch (SQLException e) {
            super.sendError("MapData MobsNormal", e);
        } finally {
            close(p);
        }
        return false;
    }

    public boolean updateMonster(GameMap map, String monsters) {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("UPDATE `maps` SET `monsters` = ? WHERE id = ?");
            p.setString(1, monsters);
            p.setInt(2, map.getId());
            execute(p);
            return true;
        } catch (SQLException e) {
            super.sendError("MapData updateMonster", e);
        } finally {
            close(p);
        }
        return false;
    }
    public boolean updateMaxNumGroup(GameMap map, byte limite)
    {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("UPDATE `maps` SET `numgroup` = ? WHERE id = ?");
            p.setByte(1, limite);
            p.setInt(2, map.getId());
            execute(p);
            return true;
        } catch (SQLException e) {
            super.sendError("MapData updateMaxNumGroup", e);
        } finally {
            close(p);
        }
        return false;
    }
    public boolean updateMaxSizeGroup(GameMap map, byte limite)
    {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("UPDATE `maps` SET `maxSize` = ? WHERE id = ?");
            p.setByte(1, limite);
            p.setInt(2, map.getId());
            execute(p);
            return true;
        } catch (SQLException e) {
            super.sendError("MapData updateMaxSizeGroup", e);
        } finally {
            close(p);
        }
        return false;
    }
    public boolean replaceMobFix(int mapid, int cellid, String groupdata, int timer)
    {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("REPLACE INTO `mobgroups_fix` (mapid,cellid,groupData,Donjon,Salle,Timer) VALUES(?,?,?,'','0',?)");
            p.setInt(1, mapid);
            p.setInt(2, cellid);
            p.setString(3, groupdata);
            p.setInt(4, timer);
            execute(p);
            return true;
        } catch (SQLException e) {
            super.sendError("MapData replaceMobFix", e);
        } finally {
            close(p);
        }
        return false;
    }
    public boolean deleteMobGroupFix(GameMap map)
    {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("DELETE FROM `mobgroups_fix` WHERE `mapid` = ?");
            p.setInt(1, map.getId());
            execute(p);
            return true;
        } catch (SQLException e) {
            super.sendError("MapData deleteMobGroupFix", e);
        } finally {
            close(p);
        }
        return false;
    }

    public void load() {
        Result result = null;
        try {
            result = getData("SELECT  * from maps LIMIT " + Constant.DEBUG_MAP_LIMIT);
            ResultSet RS = result.resultSet;
            while (RS.next()) {
                World.world.addMap(new GameMap(RS.getShort("id"), RS.getString("date"), RS.getByte("width"), RS.getByte("heigth"), RS.getString("key"), RS.getString("places"), RS.getString("mapData"), RS.getString("monsters"), RS.getString("mappos"), RS.getByte("numgroup"), RS.getByte("fixSize"), RS.getByte("minSize"), RS.getByte("maxSize"), RS.getString("forbidden"), RS.getByte("sniffed"), RS.getShort("capabilities"), RS.getInt("musicID"), RS.getInt("ambianceID"), RS.getInt("bgID"), RS.getInt("outDoor"), RS.getInt("maxMerchant")));
            }
            close(result);

            result = getData("SELECT  * from mobgroups_fix");
            RS = result.resultSet;
            while (RS.next()) {
                GameMap c = World.world.getMap(RS.getShort("mapid"));
                if (c == null)
                    continue;
                if (c.getCase(RS.getInt("cellid")) == null)
                    continue;
                c.addStaticGroup(RS.getInt("cellid"), RS.getString("groupData"), false);
                World.world.addGroupFix(RS.getInt("mapid") + ";" + RS.getInt("cellid"), RS.getString("groupData"), RS.getInt("Timer"));
            }
        } catch (SQLException e) {
            super.sendError("MapData load", e);
        } finally {
            close(result);
        }
    }

    public void reload() {
        Result result = null;
        try {
            result = getData("SELECT  * from maps LIMIT "
                    + Constant.DEBUG_MAP_LIMIT);
            ResultSet RS = result.resultSet;
            while (RS.next()) {
                GameMap map = World.world.getMap(RS.getShort("id"));
                if (map == null) {
                    World.world.addMap(new GameMap(RS.getShort("id"), RS.getString("date"), RS.getByte("width"), RS.getByte("heigth"), RS.getString("key"), RS.getString("places"), RS.getString("mapData"), RS.getString("monsters"), RS.getString("mappos"), RS.getByte("numgroup"), RS.getByte("fixSize"), RS.getByte("minSize"), RS.getByte("maxSize"), RS.getString("forbidden"), RS.getByte("sniffed"), RS.getShort("capabilities"), RS.getInt("musicID"), RS.getInt("ambianceID"), RS.getInt("bgID"), RS.getInt("outDoor"), RS.getInt("maxMerchant")));
                    continue;
                }
                map.setInfos(RS.getString("date"), RS.getString("monsters"), RS.getString("mappos"), RS.getByte("numgroup"), RS.getByte("fixSize"), RS.getByte("minSize"), RS.getByte("maxSize"), RS.getString("forbidden"));
            }
        } catch (SQLException e) {
            super.sendError("MapData reload", e);
        } finally {
            close(result);
        }
    }
}
