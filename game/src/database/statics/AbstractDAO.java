package database.statics;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.zaxxer.hikari.HikariDataSource;
import database.DAO;
import org.slf4j.LoggerFactory;

import java.sql.*;

public abstract class AbstractDAO<T> implements DAO<T> {

    protected final Object locker = new Object();
    protected HikariDataSource dataSource;
    protected Logger logger = (Logger) LoggerFactory.getLogger(AbstractDAO.class + " - [staticBDD]");

    public AbstractDAO(HikariDataSource dataSource) {
        this.dataSource = dataSource;
        logger.setLevel(Level.ERROR);
    }

    protected Result getData(String query) {
        synchronized (locker) {
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {

                if (!query.endsWith(";")) {
                    query = query + ";";
                }

                ResultSet resultSet = statement.executeQuery(query);
                logger.debug("SQL request executed successfully {}", query);

                return new Result(connection, statement, resultSet);
            } catch (SQLException e) {
                logger.error("Can't execute SQL Request :" + query, e);
                return null;
            }
        }
    }

    public void execute(PreparedStatement statement) {
        synchronized (locker) {
            try (PreparedStatement stmt = statement) {
                stmt.execute();
                logger.debug("SQL request executed successfully {}", stmt);
            } catch (SQLException e) {
                logger.error("Can't execute SQL Request :" + statement, e);
            }
        }
    }

    public void executeUpdate(PreparedStatement statement) {
        synchronized (locker) {
            try (PreparedStatement stmt = statement) {
                int affectedRows = stmt.executeUpdate();
                logger.debug("SQL request executed successfully, affected rows: {}", affectedRows);
            } catch (SQLException e) {
                logger.error("Can't execute SQL Request :" + statement, e);
            }
        }
    }

    protected void sendError(String msg, Exception e) {
        e.printStackTrace();
        logger.error("Error statics database " + msg + " : " + e.getMessage());
    }

    public class Result implements AutoCloseable {
        private final Connection connection;
        private final Statement statement;
        private final ResultSet resultSet;

        public Result(Connection connection, Statement statement, ResultSet resultSet) {
            this.connection = connection;
            this.statement = statement;
            this.resultSet = resultSet;
        }

        public ResultSet getResultSet() {
            return resultSet;
        }

        @Override
        public void close() {
            try {
                if (resultSet != null && !resultSet.isClosed()) {
                    resultSet.close();
                }
                if (statement != null && !statement.isClosed()) {
                    statement.close();
                }
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                logger.error("Error closing ResultSet, Statement, or Connection", e);
            }
        }
    }
}