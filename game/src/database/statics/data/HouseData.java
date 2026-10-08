package database.statics.data;

import com.zaxxer.hikari.HikariDataSource;
import area.map.GameMap;
import area.map.entity.House;
import database.statics.AbstractDAO;
import game.world.World;

import java.sql.ResultSet;
import java.sql.SQLException;

public class HouseData extends AbstractDAO<House>
{
	public HouseData(HikariDataSource dataSource) {
		super(dataSource);
	}

	@Override
	public void load(Object obj) {
	}

	@Override
	public boolean update(House h) {
		return false;
	}

	public int load() {
		int nbr = 0;
		String query = "SELECT * from houses";
		try (Result result = getData(query)) {
			if (result != null) {
				ResultSet RS = result.getResultSet();
				while (RS.next()) {
					GameMap map = World.world.getMap(RS.getShort("map_id"));
					if (map == null)
						continue;
					World.world.addHouse(new House(RS.getInt("id"), RS.getShort("map_id"), RS.getInt("cell_id"), RS.getInt("mapid"), RS.getInt("caseid"), RS.getString("houseMaps"), RS.getLong("saleBase")));
					nbr++;
				}
			}
		} catch (SQLException e) {
			super.sendError("HouseData load", e);
			nbr = 0;
		}
		return nbr;
	}
}
