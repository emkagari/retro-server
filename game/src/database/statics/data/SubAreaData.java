package database.statics.data;

import com.zaxxer.hikari.HikariDataSource;
import area.SubArea;
import database.statics.AbstractDAO;
import game.world.World;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SubAreaData extends AbstractDAO<SubArea> {

	public SubAreaData(HikariDataSource dataSource)
	{
		super(dataSource);
	}

	@Override
	public void load(Object obj) {}

	@Override
	public boolean update(SubArea subarea)
	{
		return false;
	}

	public void load() {
		String query = "SELECT * FROM `subarea_data`;";
		try (Result result = getData(query)) {
			if (result != null && result.getResultSet() != null) {
				ResultSet RS = result.getResultSet();
				while (RS.next()) {
					SubArea SA = new SubArea(RS.getInt("id"), RS.getInt("area"));
					World.world.addSubArea(SA);

					if (SA.getArea() != null) {
						SA.getArea().addSubArea(SA); // On ajoute la sous zone à la zone
					}
				}
			}
		} catch (SQLException e) {
			sendError("Subarea_dataData load", e);
		}
	}
}
