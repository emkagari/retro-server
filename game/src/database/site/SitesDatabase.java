package database.site;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import database.Database;
import database.site.data.AccountWebData;
import kernel.Config;
import kernel.Main;
import org.slf4j.LoggerFactory;

public class SitesDatabase {
    //connection
    private HikariDataSource dataSource;
    private static Logger logger = (Logger) LoggerFactory.getLogger(SitesDatabase.class);

    //data
    private AccountWebData accountWebData;

    private void initializeData() {
        this.accountWebData = new AccountWebData(this.dataSource);

    }

    public boolean initializeConnection() {
        try {
            logger.setLevel(Level.ERROR);
            logger.trace("Reading database config");

            HikariConfig config = new HikariConfig();
            config.setDataSourceClassName("org.mariadb.jdbc.MySQLDataSource");
            config.addDataSourceProperty("serverName", Config.INSTANCE.getSiteHostDB());
            config.addDataSourceProperty("port", Config.INSTANCE.getSitePortDB());
            config.addDataSourceProperty("databaseName", Config.INSTANCE.getSiteNameDB());
            config.addDataSourceProperty("user", Config.INSTANCE.getSiteUserDB());
            config.addDataSourceProperty("password", Config.INSTANCE.getSitePassDB());
            config.setMaximumPoolSize(10); // Ajustez en fonction de vos besoins
            config.setMinimumIdle(2);
            config.setAutoCommit(true); // AutoCommit, c'est cool

            this.dataSource = new HikariDataSource(config);

            if (!Database.tryConnection(this.dataSource)) {
                logger.error("Please verify your username and password and database connection");
                Main.INSTANCE.stop("site try connection failed");
                return false;
            }
            logger.info("Database connection established");
            initializeData();
            logger.info("Database data loaded");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }

    public AccountWebData getAccountWebData() {
        return accountWebData;
    }

}
