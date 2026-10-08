package org.starloco.locos.database;

import ch.qos.logback.classic.Logger;
import com.zaxxer.hikari.HikariDataSource;
import org.starloco.locos.kernel.Main;
import org.slf4j.LoggerFactory;

import java.sql.*;

public abstract class AbstractDAO<T> implements DAO<T> {

    private final Object locker = new Object();
    private final HikariDataSource dataSource;
    protected Logger logger = (Logger) LoggerFactory.getLogger("test");

    protected AbstractDAO(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    protected void execute(String query) {
        synchronized (locker) {
            Connection connection = null;
            Statement statement = null;
            try {
                connection = dataSource.getConnection();
                statement = connection.createStatement();
                statement.execute(query);
                logger.debug("SQL request executed successfully {}", query);
            } catch (SQLException e) {
                logger.error("Can't execute SQL Request :" + query, e);
            } finally {
                close(statement);
                close(connection);
            }
        }
    }

    protected void execute(PreparedStatement statement) {
        synchronized (locker) {
            Connection connection = null;
            try {
                connection = statement.getConnection();
                statement.execute();
                logger.debug("SQL request executed successfully {}", statement.toString());
            } catch (SQLException e) {
                logger.error("Can't execute SQL Request :" + statement.toString(), e);
            } finally {
                close(statement);
                close(connection);
            }
        }
    }

    protected Result getData(String query) {
        synchronized (locker) {
            Connection connection = null;
            try {
                if (!query.endsWith(";"))
                    query = query + ";";
                connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                Result result = new Result(connection, statement.executeQuery(query));
                logger.debug("SQL request executed successfully {}", query);
                return result;
            } catch (SQLException e) {
                logger.error("Can't execute SQL Request :" + query, e);
            }
            return null;
        }
    }

    protected PreparedStatement getPreparedStatement(String query) {
        try {
            Connection connection = dataSource.getConnection();
            return connection.prepareStatement(query);
        } catch (SQLException e) {
            logger.error("Can't get data source connection", e);
            dataSource.close();
            Main.database.initializeConnection();
            return null;
        }
    }

    void close(PreparedStatement statement) {
        if (statement == null)
            return;
        try {
            statement.clearParameters();
            statement.close();
        } catch (Exception e) {
            logger.error("Can't close statement", e);
        }
    }

    void close(Connection connection) {
        if (connection == null)
            return;
        try {
            connection.close();
            logger.trace("{} released", connection);
        } catch (Exception e) {
            logger.error("Can't close connection", e);
        }
    }

    void close(Statement statement) {
        if (statement == null)
            return;
        try {
            statement.close();
        } catch (Exception e) {
            logger.error("Can't close statement", e);
        }
    }

    protected void close(Result result) {
        if (result != null) {
            try {
                if (result.resultSet != null)
                    result.resultSet.close();
                if (result.connection != null)
                    result.connection.close();
                logger.trace("Connection {} has been released", result.connection);
            } catch (SQLException e) {
                logger.error("Can't close result");
            }
        }
    }

    protected class Result {
        public final Connection connection;
        public final ResultSet resultSet;

        Result(Connection connection, ResultSet resultSet) {
            this.connection = connection;
            this.resultSet = resultSet;
        }
    }
}
