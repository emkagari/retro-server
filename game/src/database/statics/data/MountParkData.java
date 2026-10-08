package database.statics.data;

import com.zaxxer.hikari.HikariDataSource;
import area.map.GameMap;
import area.map.entity.MountPark;
import database.Database;
import database.statics.AbstractDAO;
import game.world.World;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MountParkData extends AbstractDAO<MountPark>
{
	public MountParkData(HikariDataSource dataSource)
	{
		super(dataSource);
	}

	@Override
	public void load(Object obj)
	{
    }

	@Override
	public boolean update(MountPark MP) {
		String query = "UPDATE `mountpark_data` SET `cellMount` =?, `cellPorte`=?, `cellEnclos`=? WHERE `mapid`=?";
		try (Connection conn = dataSource.getConnection(); PreparedStatement p = conn.prepareStatement(query)) {
			p.setInt(1, MP.getMountcell());
			p.setInt(2, MP.getDoor());
			p.setString(3, MP.parseStringCellObject());
			p.setInt(4, MP.getMap().getId());
			executeUpdate(p);
			return true;
		} catch (SQLException e) {
			sendError("Mountpark_dataData update", e);
		}
		return false;
	}

	public int load() {
		int nbr = 0;
		String query = "SELECT * from mountpark_data";
		try (Result result = getData(query)) {
			if (result != null) {
				ResultSet RS = result.getResultSet();
				while (RS.next()) {
					GameMap map = World.world.getMap(RS.getShort("mapid"));
					if (map == null)
						continue;
					MountPark MP = new MountPark(map, RS.getInt("cellid"), RS.getInt("size"), RS.getInt("priceBase"), RS.getInt("cellMount"), RS.getInt("cellporte"), RS.getString("cellEnclos"), RS.getInt("sizeObj"));
					World.world.addMountPark(MP);
					Database.getDynamics().getMountParkData().exist(MP);
					nbr++;
				}
			}
		} catch (SQLException e) {
			sendError("Mountpark_dataData load", e);
		}
		return nbr;
	}

	public void reload(int i) {
		String query = "SELECT * from mountpark_data";
		try (Result result = getData(query)) {
			if (result != null) {
				ResultSet RS = result.getResultSet();
				while (RS.next()) {
					GameMap map = World.world.getMap(RS.getShort("mapid"));
					if (map == null || RS.getShort("mapid") != i)
						continue;
					if (!World.world.getMountPark().containsKey(RS.getShort("mapid"))) {
						MountPark MP = new MountPark(map, RS.getInt("cellid"), RS.getInt("size"), RS.getInt("priceBase"), RS.getInt("cellMount"), RS.getInt("cellporte"), RS.getString("cellEnclos"), RS.getInt("sizeObj"));
						World.world.addMountPark(MP);
					} else {
						World.world.getMountPark().get(RS.getShort("mapid")).setInfos(map, RS.getInt("cellid"), RS.getInt("size"), RS.getInt("cellMount"), RS.getInt("cellporte"), RS.getString("cellEnclos"), RS.getInt("sizeObj"));
					}
				}
			}
		} catch (SQLException e) {
			sendError("Mountpark_dataData reload", e);
		}
	}
}
