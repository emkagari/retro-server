package kernel;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Configuration du serveur de jeu.
 *
 * Remplace l'ancien couple Kotlin Config.kt / ConfigReader.kt (lib konfig).
 * Les valeurs sont lues depuis config.properties, une variable d'environnement
 * pouvant surcharger une clé (comportement konfig "EnvironmentVariables overriding").
 *
 * Rappel konfig : le nom de propriété Kotlin avec des underscores devient une clé
 * avec des tirets (percent_exo -> rate.percent-exo). Le camelCase est conservé.
 * Les clés ci-dessous sont donc celles du fichier config.properties.
 */
public class Config {

    public static final Config INSTANCE = new Config();

    private final Properties props = new Properties();

    private final long startTime = System.currentTimeMillis();

    private boolean LOG;
    private boolean AUTO_CLEAN;
    private boolean AZURIOM;
    private boolean LINUX;
    private boolean HALLOWEEN;
    private boolean NOEL;
    private boolean HEROIC;
    private boolean DISCORD_BOT;
    private boolean DISCORD_WH;
    private boolean HDV_GLOBAL;

    private boolean TEAM_MATCH;
    private boolean DEATH_MATCH;
    private boolean AUTO_EVENT;
    private boolean AUTO_REBOOT;
    private boolean ALL_ZAAP;
    private boolean ALL_EMOTE;

    private boolean isSaving = false;
    private boolean isRunning = false;

    private boolean ENCRYPT_PACKET;
    private short TIME_PER_EVENT;

    private int START_MAP;
    private int START_CELL;
    private int RATE_KAMAS;
    private int RATE_DROP;
    private int RATE_HONOR;
    private int RATE_JOB;
    private double RATE_XP;
    private int RATE_FM;
    private int PERCENT_EXO;

    private String DISCORD_KEY;
    private String DISCORD_CHANNEL_COMMAND;
    private String DISCORD_CHANNEL_EVENT;
    private String DISCORD_CHANNEL_LOG;
    private String DISCORD_CHANNEL_INFO;
    private String DISCORD_CHANNEL_FAILLE;

    private int PRIX_CHANGEMENT_CLASSE;
    private int PRIX_CHANGEMENT_COULEUR;
    private int PRIX_CHANGEMENT_PSEUDO;

    private int exchangePort;
    private int gamePort;
    private String exchangeIp;
    private String loginHostDB;
    private int loginPortDB;
    private String loginNameDB;
    private String loginUserDB;
    private String loginPassDB;

    private String siteHostDB;
    private int sitePortDB;
    private String siteNameDB;
    private String siteUserDB;
    private String sitePassDB;

    private String hostDB;
    private int portDB;
    private String nameDB;
    private String userDB;
    private String passDB;
    private String ip;

    private int SERVER_ID;
    private String SERVER_KEY;
    private String SERVER_NAME;
    private String SERVER_VER;

    private String url = "http://127.0.0.1";
    private String startMessage;
    private String colorMessage = "B9121B";

    private boolean subscription;

    private long startKamas;
    private int startLevel;

    private Config() {
        load();

        this.LOG = bool("mode.log");
        this.AUTO_CLEAN = bool("mode.autoClean");
        this.AZURIOM = bool("mode.azuriom");
        this.LINUX = bool("mode.linux");
        this.HALLOWEEN = bool("mode.halloween");
        this.NOEL = bool("mode.christmas");
        this.HEROIC = bool("mode.heroic");
        this.DISCORD_BOT = bool("mode.discordBot");
        this.DISCORD_WH = bool("mode.discordWebhooks");
        this.HDV_GLOBAL = bool("mode.hdvGlobal");

        this.TEAM_MATCH = bool("options.teamMatch");
        this.DEATH_MATCH = bool("options.deathMatch");
        this.AUTO_EVENT = bool("options.event.active");
        this.AUTO_REBOOT = bool("options.autoReboot");
        this.ALL_ZAAP = bool("options.allZaap");
        this.ALL_EMOTE = bool("options.allEmote");

        this.ENCRYPT_PACKET = bool("options.encryptPacket");
        this.TIME_PER_EVENT = (short) intVal("options.event.timePerEvent");

        this.START_MAP = intVal("options.start.map");
        this.START_CELL = intVal("options.start.cell");
        this.RATE_KAMAS = intVal("rate.kamas");
        this.RATE_DROP = intVal("rate.farm");
        this.RATE_HONOR = intVal("rate.honor");
        this.RATE_JOB = intVal("rate.job");
        this.RATE_XP = dbl("rate.xp");
        this.RATE_FM = intVal("rate.fm");
        this.PERCENT_EXO = intVal("rate.percent-exo");

        this.DISCORD_KEY = str("discord.key");
        this.DISCORD_CHANNEL_COMMAND = str("discord.channelId.command");
        this.DISCORD_CHANNEL_EVENT = str("discord.channelId.event");
        this.DISCORD_CHANNEL_LOG = str("discord.channelId.log");
        this.DISCORD_CHANNEL_INFO = str("discord.channelId.info");
        this.DISCORD_CHANNEL_FAILLE = str("discord.channelId.faille");

        this.PRIX_CHANGEMENT_CLASSE = intVal("prix.prix-changement-classe");
        this.PRIX_CHANGEMENT_COULEUR = intVal("prix.prix-changement-couleur");
        this.PRIX_CHANGEMENT_PSEUDO = intVal("prix.prix-changement-pseudo");

        this.exchangePort = intVal("exchange.port");
        this.gamePort = intVal("server.port");
        this.exchangeIp = str("exchange.host");
        this.loginHostDB = str("database.login.host");
        this.loginPortDB = intVal("database.login.port");
        this.loginNameDB = str("database.login.name");
        this.loginUserDB = str("database.login.user");
        this.loginPassDB = str("database.login.pass");

        this.siteHostDB = str("database.site.host");
        this.sitePortDB = intVal("database.site.port");
        this.siteNameDB = str("database.site.name");
        this.siteUserDB = str("database.site.user");
        this.sitePassDB = str("database.site.pass");

        this.hostDB = str("database.game.host");
        this.portDB = intVal("database.game.port");
        this.nameDB = str("database.game.name");
        this.userDB = str("database.game.user");
        this.passDB = str("database.game.pass");
        this.ip = str("server.host");

        this.SERVER_ID = intVal("server.id");
        this.SERVER_KEY = str("server.key");
        this.SERVER_NAME = str("server.name");
        this.SERVER_VER = str("server.version");

        this.subscription = bool("options.subscription");

        this.startKamas = lng("options.start.kamas");
        this.startLevel = intVal("options.start.level");

        this.startMessage = "Bienvenue sur le serveur " + SERVER_NAME + " - (" + SERVER_VER + ") !";
    }

    private void load() {
        try (FileInputStream in = new FileInputStream(new File("config.properties"))) {
            props.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Impossible de charger config.properties", e);
        }
    }

    /** Surcharge possible par variable d'environnement (clé en MAJUSCULES, '.' et '-' -> '_'). */
    private String raw(String key) {
        String env = System.getenv(key.toUpperCase().replace('.', '_').replace('-', '_'));
        if (env != null) return env;
        String v = props.getProperty(key);
        if (v == null) {
            throw new RuntimeException("config.properties : clé manquante '" + key + "'");
        }
        return v.trim();
    }

    private String str(String key) {
        return raw(key);
    }

    private int intVal(String key) {
        return Integer.parseInt(raw(key));
    }

    private long lng(String key) {
        return Long.parseLong(raw(key));
    }

    private double dbl(String key) {
        return Double.parseDouble(raw(key));
    }

    private boolean bool(String key) {
        return Boolean.parseBoolean(raw(key));
    }

    public long getStartTime() { return startTime; }

    public boolean getLOG() { return LOG; }
    public boolean getAUTO_CLEAN() { return AUTO_CLEAN; }
    public boolean getAZURIOM() { return AZURIOM; }
    public boolean getLINUX() { return LINUX; }
    public boolean getHALLOWEEN() { return HALLOWEEN; }
    public boolean getNOEL() { return NOEL; }
    public boolean getHEROIC() { return HEROIC; }
    public boolean getDISCORD_BOT() { return DISCORD_BOT; }
    public boolean getDISCORD_WH() { return DISCORD_WH; }
    public boolean getHDV_GLOBAL() { return HDV_GLOBAL; }

    public boolean getTEAM_MATCH() { return TEAM_MATCH; }
    public boolean getDEATH_MATCH() { return DEATH_MATCH; }
    public boolean getAUTO_EVENT() { return AUTO_EVENT; }
    public boolean getAUTO_REBOOT() { return AUTO_REBOOT; }
    public boolean getALL_ZAAP() { return ALL_ZAAP; }
    public boolean getALL_EMOTE() { return ALL_EMOTE; }

    public boolean isSaving() { return isSaving; }
    public void setSaving(boolean saving) { isSaving = saving; }
    public boolean isRunning() { return isRunning; }
    public void setRunning(boolean running) { isRunning = running; }

    public boolean getENCRYPT_PACKET() { return ENCRYPT_PACKET; }
    public short getTIME_PER_EVENT() { return TIME_PER_EVENT; }

    public int getSTART_MAP() { return START_MAP; }
    public int getSTART_CELL() { return START_CELL; }
    public int getRATE_KAMAS() { return RATE_KAMAS; }
    public int getRATE_DROP() { return RATE_DROP; }
    public int getRATE_HONOR() { return RATE_HONOR; }
    public int getRATE_JOB() { return RATE_JOB; }
    public double getRATE_XP() { return RATE_XP; }
    public int getRATE_FM() { return RATE_FM; }
    public int getPERCENT_EXO() { return PERCENT_EXO; }

    public String getDISCORD_KEY() { return DISCORD_KEY; }
    public String getDISCORD_CHANNEL_COMMAND() { return DISCORD_CHANNEL_COMMAND; }
    public String getDISCORD_CHANNEL_EVENT() { return DISCORD_CHANNEL_EVENT; }
    public String getDISCORD_CHANNEL_LOG() { return DISCORD_CHANNEL_LOG; }
    public String getDISCORD_CHANNEL_INFO() { return DISCORD_CHANNEL_INFO; }
    public String getDISCORD_CHANNEL_FAILLE() { return DISCORD_CHANNEL_FAILLE; }

    public int getPRIX_CHANGEMENT_CLASSE() { return PRIX_CHANGEMENT_CLASSE; }
    public int getPRIX_CHANGEMENT_COULEUR() { return PRIX_CHANGEMENT_COULEUR; }
    public int getPRIX_CHANGEMENT_PSEUDO() { return PRIX_CHANGEMENT_PSEUDO; }

    public int getExchangePort() { return exchangePort; }
    public int getGamePort() { return gamePort; }
    public String getExchangeIp() { return exchangeIp; }
    public String getLoginHostDB() { return loginHostDB; }
    public int getLoginPortDB() { return loginPortDB; }
    public String getLoginNameDB() { return loginNameDB; }
    public String getLoginUserDB() { return loginUserDB; }
    public String getLoginPassDB() { return loginPassDB; }

    public String getSiteHostDB() { return siteHostDB; }
    public int getSitePortDB() { return sitePortDB; }
    public String getSiteNameDB() { return siteNameDB; }
    public String getSiteUserDB() { return siteUserDB; }
    public String getSitePassDB() { return sitePassDB; }

    public String getHostDB() { return hostDB; }
    public int getPortDB() { return portDB; }
    public String getNameDB() { return nameDB; }
    public String getUserDB() { return userDB; }
    public String getPassDB() { return passDB; }
    public String getIp() { return ip; }

    public int getSERVER_ID() { return SERVER_ID; }
    public String getSERVER_KEY() { return SERVER_KEY; }
    public String getSERVER_NAME() { return SERVER_NAME; }
    public String getSERVER_VER() { return SERVER_VER; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getStartMessage() { return startMessage; }
    public void setStartMessage(String startMessage) { this.startMessage = startMessage; }
    public String getColorMessage() { return colorMessage; }
    public void setColorMessage(String colorMessage) { this.colorMessage = colorMessage; }

    public boolean getSubscription() { return subscription; }

    public long getStartKamas() { return startKamas; }
    public int getStartLevel() { return startLevel; }
}
