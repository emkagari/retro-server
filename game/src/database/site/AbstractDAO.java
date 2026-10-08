package database.site;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.zaxxer.hikari.HikariDataSource;
import database.DAO;
import database.Database;
import kernel.Main;
import org.slf4j.LoggerFactory;

import java.sql.*;

public abstract class AbstractDAO<T> implements DAO<T> {

    protected final Object locker = new Object();
    protected HikariDataSource dataSource;
    protected Logger logger = (Logger) LoggerFactory.getLogger(AbstractDAO.class + " - [siteBDD]");

    public AbstractDAO(HikariDataSource dataSource) {
        this.dataSource = dataSource;
        logger.setLevel(Level.ERROR);
    }

    protected Result getData(String query) {
        synchronized (locker) {
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {

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

    protected PreparedStatementWrapper getPreparedStatement(String query) throws SQLException {
        try {
            Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            return new PreparedStatementWrapper(connection, preparedStatement);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("Can't get datasource connection", e);
            dataSource.close();
            if (!Database.getSites().initializeConnection())
                Main.INSTANCE.stop("statics prepared statement failed");
            throw e;
        }
    }

    public void execute(PreparedStatementWrapper statementWrapper) {
        synchronized (locker) {
            try (PreparedStatementWrapper wrapper = statementWrapper) {
                PreparedStatement statement = wrapper.getPreparedStatement();
                statement.execute();
                logger.debug("SQL request executed successfully {}", statement.toString());
            } catch (SQLException e) {
                e.printStackTrace();
                logger.error("Can't execute SQL Request :" + statementWrapper.getPreparedStatement().toString(), e);
            }
        }
    }

    public void executeUpdate(PreparedStatementWrapper statementWrapper) {
        synchronized (locker) {
            try (PreparedStatementWrapper wrapper = statementWrapper) {
                PreparedStatement statement = wrapper.getPreparedStatement();
                int affectedRows = statement.executeUpdate();
                logger.debug("SQL request executed successfully, affected rows: {}", affectedRows);
            } catch (SQLException e) {
                e.printStackTrace();
                logger.error("Can't execute SQL Request :" + statementWrapper.getPreparedStatement().toString(), e);
            }
        }
    }

    protected void sendError(String msg, Exception e) {
        e.printStackTrace();
        logger.error("Error sites database " + msg + " : " + e.getMessage());
    }

    public class PreparedStatementWrapper implements AutoCloseable {
        private final Connection connection;
        private final PreparedStatement preparedStatement;

        public PreparedStatementWrapper(Connection connection, PreparedStatement preparedStatement) {
            this.connection = connection;
            this.preparedStatement = preparedStatement;
        }

        public PreparedStatement getPreparedStatement() {
            return preparedStatement;
        }

        @Override
        public void close() {
            try {
                if (preparedStatement != null && !preparedStatement.isClosed()) {
                    preparedStatement.close();
                }
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
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
                e.printStackTrace();
            }
        }
    }
}