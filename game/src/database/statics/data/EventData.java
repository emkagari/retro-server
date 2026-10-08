package database.statics.data;

import com.zaxxer.hikari.HikariDataSource;
import client.Account;
import database.statics.AbstractDAO;
import event.EventReward;
import event.type.Event;
import event.type.EventFindMe;
import event.type.EventSmiley;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Locos on 02/10/2016.
 */
public class EventData extends AbstractDAO<Account> {

    public EventData(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void load(Object obj) {
    }

    @Override
    public boolean update(Account obj) {
        return false;
    }

    public Event[] load() {
        Event[] events = new Event[this.getNumberOfEvent()];
        String query = "SELECT * FROM `world.event.type`;";

        try (Result result = getData(query)) {
            if (result != null) {
                ResultSet RS = result.getResultSet();
                byte i = 0;

                while (RS.next()) {
                    Event event = this.getEventById(RS.getByte("id"), RS);

                    if (event != null) {
                        events[i] = event;
                        i++;
                    }
                }
            }
        } catch (SQLException e) {
            super.sendError("EventData load", e);
        }
        return events;
    }

    private byte getNumberOfEvent() {
        byte numbers = 0;
        String query = "SELECT COUNT(id) AS numbers FROM `world.event.type`;";

        try (Result result = getData(query)) {
            if (result != null) {
                ResultSet RS = result.getResultSet();
                if (RS.next()) {
                    numbers = RS.getByte("numbers");
                }
            }
        } catch (SQLException e) {
            super.sendError("EventData getNumberOfEvent", e);
        }
        return numbers;
    }

    private byte loadFindMeRow() {
        byte numbers = 0;
        String query = "SELECT COUNT(id) AS numbers FROM `world.event.findme`;";

        try (Result result = getData(query)) {
            if (result != null) {
                ResultSet RS = result.getResultSet();
                while (RS.next()) {
                    EventFindMe.FindMeRow row = new EventFindMe.FindMeRow(RS.getShort("map"), RS.getShort("cell"), RS.getString("indices").split("\\|"));
                }
                numbers = RS.getByte("numbers");
            }
        } catch (SQLException e) {
            super.sendError("EventData loadFindMeRow", e);
        }
        return numbers;
    }

    private Event getEventById(byte id, ResultSet result) throws SQLException {
        switch (id) {
            case 1:
                return new EventSmiley(id, result.getByte("maxPlayers"), result.getString("name"), result.getString("description"), EventReward.parse(result.getString("firstWinner")));
            default:
                return null;
        }
    }
}
