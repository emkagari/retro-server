package org.starloco.locos.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.starloco.locos.database.data.AccountData;
import org.starloco.locos.database.data.PlayerData;
import org.starloco.locos.database.data.ServerData;
import org.starloco.locos.database.data.WorldEntityData;
import org.starloco.locos.kernel.Config;
import org.starloco.locos.kernel.Main;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

public class Database {
    private static final Logger logger = (Logger) LoggerFactory.getLogger(Database.class);
    //connection
    private HikariDataSource dataSource;
    //data
    private AccountData accountData;
    private PlayerData playerData;
    private ServerData serverData;
    private WorldEntityData worldEntityData;

    void initializeData() {
        this.accountData = new AccountData(dataSource);
        this.playerData = new PlayerData(dataSource);
        this.serverData = new ServerData(dataSource);
        this.worldEntityData = new WorldEntityData(dataSource);
    }

    public void initializeConnection() {
        logger.trace("Reading database config");
        HikariConfig config = new HikariConfig();

        config.setDataSourceClassName("com.mysql.cj.jdbc.MysqlDataSource");
        config.addDataSourceProperty("characterEncoding", "utf8");
        config.addDataSourceProperty("serverName", Config.host);
        config.addDataSourceProperty("port", Config.port);
        config.addDataSourceProperty("databaseName", Config.databaseName);
        config.addDataSourceProperty("user", Config.user);
        config.addDataSourceProperty("password", Config.pass);

        dataSource = new HikariDataSource(config);
        if (!testConnection(dataSource)) {
            logger.error("Please check your username and password and database connection");
            Main.exit();
            return;
        }
        logger.info("Database connection established");
        this.initializeData();
    }

    public AccountData getAccountData() {
        return accountData;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

    public ServerData getServerData() {
        return serverData;
    }

    public WorldEntityData getWorldEntityData() {
        return worldEntityData;
    }


    private boolean testConnection(HikariDataSource dataSource) {
        try {
            Connection connection = dataSource.getConnection();
            connection.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
