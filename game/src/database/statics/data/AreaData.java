package database.statics.data;

import com.zaxxer.hikari.HikariDataSource;
import area.Area;
import database.statics.AbstractDAO;
import game.world.World;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AreaData extends AbstractDAO<Area>
{
	public AreaData(HikariDataSource dataSource) {
		super(dataSource);
	}

	@Override
	public void load(Object obj) {}

	@Override
	public boolean update(Area area) {
		return false;
	}

	public void load() {
		String query = "SELECT * FROM area_data";
		try (Result result = getData(query); ResultSet RS = result.getResultSet()) {
			while (RS.next()) {
				Area area = new Area(RS.getInt("id"), RS.getInt("superarea"));
				World.world.addArea(area);
			}
		} catch (SQLException e) {
			super.sendError("AreaData load", e);
		}
	}
}
