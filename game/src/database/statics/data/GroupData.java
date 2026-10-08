package database.statics.data;

import com.zaxxer.hikari.HikariDataSource;
import command.administration.Group;
import database.statics.AbstractDAO;
import kernel.Main;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GroupData extends AbstractDAO<Group> {

    public GroupData(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void load(Object obj) {
        String query = "SELECT * FROM `administration.groups`;";
        try (Result result = getData(query)) {
            ResultSet RS = result.getResultSet();

            while (RS.next()) {
                new Group(RS.getInt("id"), RS.getString("name"), RS.getBoolean("isPlayer"), RS.getString("commands"));
            }
        } catch (SQLException e) {
            super.sendError("GroupData load", e);
            Main.INSTANCE.stop("unknown");
        }
    }

    @Override
    public boolean update(Group obj) {
        return false;
    }
}
