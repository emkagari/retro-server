package database.dynamics.data;

import com.zaxxer.hikari.HikariDataSource;
import database.dynamics.AbstractDAO;
import entity.monster.Monster;
import game.world.World;
import kernel.Main;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MonsterData extends AbstractDAO<Monster> {
    public MonsterData(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void load(Object obj) {}

    @Override
    public boolean update(Monster obj) {
        return false;
    }

    public void load() {
        Result result = null;
        try {
            result = getData("SELECT * FROM monsters");
            ResultSet RS = result.resultSet;
            while (RS.next()) {

                int id = RS.getInt("id");
                //if(id == 1044) continue;
                String name = RS.getString("name");
                int gfxID = RS.getInt("gfxID");
                int align = RS.getInt("align");
                String colors = RS.getString("colors");
                String grades = RS.getString("grades");
                String spells = RS.getString("spells");
                String stats = RS.getString("stats");
                String statsInfos = RS.getString("statsInfos");
                String pdvs = RS.getString("pdvs");
                String pts = RS.getString("points");
                String inits = RS.getString("inits");
                int mK = RS.getInt("minKamas");
                int MK = RS.getInt("maxKamas");
                int IAType = RS.getInt("AI_Type");
                String xp = RS.getString("exps");
                int aggroDistance = RS.getInt("aggroDistance");
                boolean capturable = RS.getInt("capturable") == 1;
                int type = RS.getInt("type");
                Monster monster = new Monster(id, name, gfxID, align, colors, grades, spells, stats, statsInfos, pdvs, pts, inits, mK, MK, xp, IAType, capturable, aggroDistance,type);
                World.world.addMobTemplate(id, monster);
            }

        } catch (SQLException e) {
            super.sendError("MonsterData load", e);
            Main.INSTANCE.stop("unknown");
        } finally {
            close(result);
        }
    }

    public void reload() {
        Result result = null;
        try {
            result = getData("SELECT * FROM monsters");
            ResultSet RS = result.resultSet;
            while (RS.next()) {
                int id = RS.getInt("id");
                //if(id == 1044) continue;
                String name = RS.getString("name");
                int gfxID = RS.getInt("gfxID");
                int align = RS.getInt("align");
                String colors = RS.getString("colors");
                String grades = RS.getString("grades");
                String spells = RS.getString("spells");
                String stats = RS.getString("stats");
                String statsInfos = RS.getString("statsInfos");
                String pdvs = RS.getString("pdvs");
                String pts = RS.getString("points");
                String inits = RS.getString("inits");
                int mK = RS.getInt("minKamas");
                int MK = RS.getInt("maxKamas");
                int IAType = RS.getInt("AI_Type");
                String xp = RS.getString("exps");
                int aggroDistance = RS.getInt("aggroDistance");
                boolean capturable = RS.getInt("capturable") == 1;
                int type = RS.getInt("type");

                if (World.world.getMonstre(id) == null) {
                    Monster monster = new Monster(id, name, gfxID, align, colors, grades, spells, stats, statsInfos, pdvs, pts, inits, mK, MK, xp, IAType, capturable, aggroDistance,type);
                    World.world.addMobTemplate(id, monster);
                } else {
                    World.world.getMonstre(id).setInfos(id, name, gfxID, align, colors, grades, spells, stats, statsInfos, pdvs, pts, inits, mK, MK, xp, IAType, capturable, aggroDistance,type);
                }

            }
        } catch (SQLException e) {
            super.sendError("MonsterData reload", e);
        } finally {
            close(result);
        }
    }

    public boolean updateMonsterStats(int id, String stats, String pdv, String exp, String minKamas, String maxKamas, String statsInfoBase, String statsAction, String statsinit) {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("UPDATE monsters SET stats = ?, pdvs = ?,exps = ? ,minKamas = ?,maxKamas = ?,statsInfos = ?,points = ?,inits = ? WHERE id = ?;");
            p.setString(1, stats);
            p.setString(2, pdv);
            p.setString(3, exp);
            p.setString(4, minKamas);
            p.setString(5, maxKamas);
            p.setString(6, statsInfoBase);
            p.setString(7, statsAction);
            p.setString(8, statsinit);
            p.setInt(9, id);
            execute(p);
            return true;
        } catch (SQLException e) {
            super.sendError("MonsterData updateMonsterStats", e);
        } finally {
            close(p);
        }
        return false;
    }

    public boolean updateMonsterIA(int id, int IA) {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("UPDATE monsters SET AI_Type = ? WHERE id = ?;");
            p.setInt(1, IA);
            p.setInt(2, id);
            execute(p);
            return true;
        } catch (SQLException e) {
            super.sendError("MonsterData updateMonsterStats", e);
        } finally {
            close(p);
        }
        return false;
    }

}
