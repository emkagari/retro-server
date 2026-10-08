package game.world;

import area.Area;
import area.SubArea;
import area.map.GameMap;
import area.map.entity.*;
import area.map.entity.InteractiveObject.InteractiveObjectTemplate;
import area.map.labyrinth.Gladiatrool;
import area.map.labyrinth.Hotomani;
import area.map.labyrinth.Minotoror;
import area.map.labyrinth.PigDragon;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import client.Account;
import client.AccountWeb;
import client.Classe;
import client.Player;
import client.other.Stats;
import command.AzuriomCommands;
import command.administration.Command;
import command.administration.Group;
import common.*;
import database.Database;
import entity.Collector;
import entity.Prism;
import entity.monster.Monster;
import entity.mount.Mount;
import entity.npc.NpcAnswer;
import entity.npc.NpcQuestion;
import entity.npc.NpcTemplate;
import entity.pet.Pet;
import entity.pet.PetEntry;
import exchange.transfer.DataQueue;
import fight.spells.*;
import game.GameClient;
import game.GameServer;
import game.scheduler.entity.WorldPub;
import guild.Guild;
import guild.GuildMember;
import hdv.Hdv;
import hdv.HdvEntry;
import job.Job;
import kernel.*;
import object.GameObject;
import object.ObjectSet;
import object.ObjectTemplate;
import object.entity.Fragment;
import object.entity.SoulStone;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.LoggerFactory;
import other.DiscordBot;
import other.QuickSets;
import other.Titre;
import quest.Quest;
import quest.QuestObjectif;
import quest.QuestPlayer;
import quest.QuestStep;
import util.TimerWaiter;
import util.lang.Lang;

import javax.security.auth.login.LoginException;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class World {

    public final static World world = new World();

    public Logger logger = (Logger) LoggerFactory.getLogger(World.class);


    private Map<Integer, Account>    accounts    = new HashMap<>();
    private Map<Integer, AccountWeb>    accountsWeb    = new HashMap<>();
    private Map<Integer, Player>     players     = new HashMap<>();
    private Map<Short, GameMap>    maps        = new HashMap<>();
    private Map<Long, GameObject> objects     = new ConcurrentHashMap<>();

    private Map<Integer, ExpLevel> experiences = new HashMap<>();
    private Map<Integer, Spell> spells = new HashMap<>();
    private Map<Integer, EffectTrigger> spellstriggers = new HashMap<>();
    private Map<String, SpellGrade> spellsgrades = new HashMap<>();
    private Map<Integer, Effect> spellseffects = new HashMap<>();
    private Map<Integer, ObjectTemplate> ObjTemplates = new HashMap<>();
    private Map<Integer, Monster> MobTemplates = new HashMap<>();
    private Map<Integer, NpcTemplate> npcsTemplate = new HashMap<>();
    private Map<Integer, NpcQuestion> questions = new HashMap<>();
    private Map<Integer, NpcAnswer> answers = new HashMap<>();
    private Map<Integer, InteractiveObjectTemplate> IOTemplate = new HashMap<>();
    private Map<Integer, Mount> Dragodindes = new HashMap<>();
    private Map<Integer, Area> areas = new HashMap<>();
    private Map<Integer, SubArea> subAreas = new HashMap<>();
    private Map<Integer, Job> Jobs = new HashMap<>();
    private Map<Integer, ArrayList<Couple<Integer, Integer>>> Crafts = new HashMap<>();
    private Map<Integer, ObjectSet> ItemSets = new HashMap<>();
    private Map<Integer, Titre> titres = new HashMap<>();
    private Map<Integer, QuickSets> QuickSets = new HashMap<>();
    private Map<Integer, Guild> Guildes = new HashMap<>();
    private Map<Integer, Hdv> Hdvs = new HashMap<>();
    private Map<Integer, Map<Integer, ArrayList<HdvEntry>>> hdvsItems = new HashMap<>();
    private Map<Integer, Animation> Animations = new HashMap<>();
    private Map<Short, area.map.entity.MountPark> MountPark = new HashMap<>();
    private Map<Integer, Trunk> Trunks = new HashMap<>();
    private Map<Integer, Collector> collectors = new HashMap<>();
    private Map<Integer, House> Houses = new HashMap<>();
    private Map<Short, Collection<Integer>> Seller = new HashMap<>();
    private StringBuilder Challenges = new StringBuilder();
    private Map<Integer, Prism> Prismes = new HashMap<>();
    private Map<Integer, Map<String, String>> fullmorphs = new HashMap<>();
    private Map<Integer, Pet> Pets = new HashMap<>();
    private Map<Long, PetEntry> PetsEntry = new HashMap<>();
    private Map<String, Map<String, String>> mobsGroupsFix = new HashMap<>();
    private Map<Integer, Map<String, Map<String, Integer>>> extraMonstre = new HashMap<>();
    private Map<Integer, GameMap> extraMonstreOnMap = new HashMap<>();
    private Map<Integer, area.map.entity.Tutorial> Tutorial = new HashMap<>();

    private Map<Integer, Classe> Classes = new HashMap<>();
    public ArrayList<House> houseToClear = new ArrayList<>();
    public DiscordBot bot = new DiscordBot();
    private boolean isOnline = false;
    private final Set<GameClient> clients = Collections.synchronizedSet(new HashSet<>());

    public void addClient(GameClient client) {
        clients.add(client);
    }

    public void removeClient(GameClient client) {
        clients.remove(client);
    }

    public Set<GameClient> getClients() {
        return clients;
    }

    private boolean timerStart = false;
    private Timer timer;


    private HouseManager houseManager = new HouseManager();

    public HouseManager getHouseManager() {
        return houseManager;
    }

    private Encryptador encryptador = new Encryptador();

    public Encryptador getEncryptador() {
        return encryptador;
    }

    private CryptManager cryptManager = new CryptManager();

    public CryptManager getCryptManager() {
        return cryptManager;
    }

    private ConditionParser conditionManager = new ConditionParser();

    public ConditionParser getConditionManager() {
        return conditionManager;
    }

    private DataQueue dataQueue = new DataQueue();

    public DataQueue getDataQueue() {
        return dataQueue;
    }

    public int getNumberOfThread() {
        int fight = getNumberOfFight();
        int player = getOnlinePlayers().size();
        return (fight + player) / 30;
    }

    public int getNumberOfFight() {
        final int[] fights = {0};
        this.maps.values().forEach(map -> fights[0] += map.getFights().size());
        return fights[0];
    }

    private int nextObjectHdvId, nextLineHdvId;

    //region Accounts data
    public void addAccount(Account account) {
        accounts.put(account.getId(), account);
    }

    public Account getAccount(int id) {
        return accounts.get(id);
    }

    public Collection<Account> getAccounts() {
        return accounts.values();
    }

    public Map<Integer, Account> getAccountsByIp(String ip) {
        Map<Integer, Account> newAccounts = new HashMap<>();
        accounts.values().stream().filter(account -> account.getLastIP().equalsIgnoreCase(ip)).forEach(account -> newAccounts.put(newAccounts.size(), account));
        return newAccounts;
    }

    public Account getAccountByPseudo(String pseudo) {
        for (Account account : accounts.values())
            if (account.getPseudo().equals(pseudo))
                return account;
        return null;
    }
    //endregion

    //region Accounts data
    public void addWebAccount(AccountWeb account) {
        accountsWeb.put(account.getId(), account);
    }

    public Map<Integer, AccountWeb> getWebAccountsByIp(String ip) {
        Map<Integer, AccountWeb> newAccounts = new HashMap<>();
        accountsWeb.values().stream().filter(accountweb -> accountweb.getLastIP().equalsIgnoreCase(ip)).forEach(accountweb -> newAccounts.put(newAccounts.size(), accountweb));
        return newAccounts;
    }

    public AccountWeb getWebAccount(int id) {
        return accountsWeb.get(id);
    }

    /*public AccountWeb getWebAccountBygameAccountid(int id) {
        for (AccountWeb account : accountsWeb.values()){
            for (int accountID : account.getAccounts().values()){
                if (id == accountID)
                    return account;
            }
        }
        return null;
    }*/

    public Collection<AccountWeb> getWebAccounts() {
        return accountsWeb.values();
    }

    public AccountWeb getAccountByName(String pseudo) {
        for (AccountWeb account : accountsWeb.values())
            if (account.getName().equals(pseudo))
                return account;
        return null;
    }

    //endregion

    //region Players data
    public Collection<Player> getPlayers() {
        return players.values();
    }

    public void addPlayer(Player player) {
        players.put(player.getId(), player);
    }

    public Player getPlayerByName(String name) {
        for (Player player : players.values())
            if (player.getName().equalsIgnoreCase(name))
                return player;
        return null;
    }

    public Player getPlayer(int id) {
        return players.get(id);
    }

    public Map<Integer, Titre> getTitres() {
        return titres;
    }

    public Map<Integer, QuickSets> getQuickSets() {
        return QuickSets;
    }

    public QuickSets getSetsById(int id) {
        return QuickSets.get(id);
    }

    public QuickSets getSetByPersoIDandNB(int persoid, int nb) {
        for (QuickSets set : QuickSets.values()) {
            if(set.getNb() == nb && set.getPlayerId() == persoid){
                return set;
            }
        }
        return null;
    }

    public List<QuickSets> getSetsByPlayer(int persoid) {
        return QuickSets.values().stream().filter(set -> set.getPlayerId() == persoid).collect(Collectors.toList());
    }

    public Titre getTitreById(int id) {
        return titres.get(id);
    }

    public ObjectTemplate getTemplateById(int id) {
        return ObjTemplates.get(id);
    }

    public List<Player> getOnlinePlayers() {
        List<Player> playerlist = new ArrayList<>();
        //if(isOnline) {
            playerlist =  players.values().stream().filter(player -> player.isOnline() && player.getGameClient() != null).collect(Collectors.toList());
        //}
        return playerlist;
    }
    //endregion

    //region Maps data
    public Collection<GameMap> getMaps() {
        return maps.values();
    }

    public GameMap getMap(short id) {
        return maps.get(id);
    }

    public void addMap(GameMap map) {
        if(map.getSubArea() != null && map.getSubArea().getArea().getId() == 42 && !Config.INSTANCE.getNOEL())
            return;
        maps.put(map.getId(), map);
    }
    //endregion

    //region Objects data
    public CopyOnWriteArrayList<GameObject> getGameObjects() {
        return new CopyOnWriteArrayList<>(objects.values());
    }

    public void addGameObjectInWorld(GameObject gameObject) {
        if (gameObject != null) {
            // Si on créé l'item car il existait pas on le rajoute aux items du monde
            objects.put(gameObject.getGuid(), gameObject);
        }
    }

    public void addGameObject(GameObject gameObject, boolean toCreate) {
        if (gameObject != null) {
            // Si on créé l'item car il existait pas on le rajoute aux items du monde
            if (toCreate) {
                addGameObjectInWorld(gameObject);
                Database.getStatics().getObjectData().insert(gameObject);
            }
        }
    }


    public GameObject getGameObject(Long guid) {
        GameObject Obj = objects.get(guid);
        // Réparation des objet négatif
        if(Obj != null) {
            if (guid < 0 && Obj.getTemplate().getType() != Constant.ITEM_TYPE_DRAGODINDE) {
                long newID = Database.getStatics().getObjectData().getNextId();
                Obj.setGuid(newID);
                Database.getStatics().getObjectData().updateID(Obj,guid);
                objects.remove(guid);
                objects.put(newID,Obj);
                if(Obj.getTemplate().getType() == Constant.ITEM_TYPE_FAMILIER){

                    PetEntry petTochange = PetsEntry.get(guid);
                    if(petTochange != null){
                        petTochange.setObjectId(newID);
                        System.out.println(guid + " : correction d'ID : " + newID);
                        Database.getStatics().getPetData().updateID(petTochange,guid);
                        PetsEntry.remove(guid);
                        PetsEntry.put(newID,petTochange);
                    }
                }
            }
        }
        return Obj;
    }

    public void removeGameObject(Long id) {
        if(objects.containsKey(id))
            objects.remove(id);
        Database.getStatics().getObjectData().delete(id);
    }
    //endregion

    public Map<Integer, Spell> getSpells() {
        return spells;
    }

    public Map<Integer, ObjectTemplate> getObjectsTemplates() {
        return ObjTemplates;
    }

    public Map<Integer, NpcAnswer> getAnswers() {
        return answers;
    }

    public Map<Integer, Mount> getMounts() {
        return Dragodindes;
    }

    public Map<Integer, Area> getAreas() {
        return areas;
    }

    public Map<Integer, SubArea> getSubAreas() {
        return subAreas;
    }

    public Map<Integer, Guild> getGuilds() {
        return Guildes;
    }

    public Map<Short, MountPark> getMountparks() {
        return MountPark;
    }

    public Map<Integer, Trunk> getTrunks() {
        return Trunks;
    }

    public Map<Integer, Collector> getCollectors() {
        return collectors;
    }

    public Map<Integer, House> getHouses() {
        return Houses;
    }

    public Map<Integer, Prism> getPrisms() {
        return Prismes;
    }

    public Map<Integer, Map<String, Map<String, Integer>>> getExtraMonsters() {
        return extraMonstre;
    }
    /**
     * end region *
     */

    public void createWorld() throws LoginException {
        logger.info("Loading of data..");
        long time = System.currentTimeMillis();

        Logger minaLogger = (Logger) LoggerFactory.getLogger("org.apache.mina");
        if(minaLogger!=null)
        {
            minaLogger.setLevel(Level.ERROR);
        }

        Database.getStatics().getServerData().loggedZero();
        logger.debug("The reset of the logged players were done successfully.");

        Logger hikariLogger = (Logger) LoggerFactory.getLogger("com.zaxxer.hikari");
        if(hikariLogger!=null)
        {
            hikariLogger.setLevel(Level.ERROR);
        }

        Database.getStatics().getWorldEntityData().load(null);
        logger.debug("The max id of all entities were done successfully.");

        logger.debug("Loading Pet Template...");
        Database.getDynamics().getPetTemplateData().load();
        logger.debug("... The " + Pets.size() +" templates of pets were loaded successfully.");

        logger.debug("Loading administration commands...");
        Database.getStatics().getCommandData().load(null);
        logger.debug("... The " + Command.commands.size() +" administration commands were loaded successfully.");

        logger.debug("Loading administration groups...");
        Database.getStatics().getGroupData().load(null);
        logger.debug("... The " + Group.getGroups().size() +" administration groups were loaded successfully.");

        logger.debug("Loading informations messages ...");
        Database.getStatics().getPubData().load(null);
        logger.debug("... The " + WorldPub.pubs.size() +" informations messages were loaded successfully.");

        logger.debug("Loading incarnations ...");
        Database.getDynamics().getFullMorphData().load();
        logger.debug("... The " + fullmorphs.size() +" incarnations were loaded successfully.");

        logger.debug("Loading extra-monsters ...");
        Database.getDynamics().getExtraMonsterData().load();
        logger.debug("... The " + extraMonstre.size() +" extra-monsters were loaded successfully.");

        logger.debug("Loading Level step experiences ...");
        Database.getDynamics().getExperienceData().load();
        logger.debug("... The " + experiences.size() +" level steps experiences were loaded successfully.");

        logger.debug("Loading Spells templates (Retro value) ...");
        Database.getDynamics().getSpellData().load();
        logger.debug("... The "+ spells.size() + " spells templates were loaded successfully.");

        logger.debug("Loading Spells grade (Retro value) ...");
        Database.getDynamics().getSpellGradeData().load();
        logger.debug("... The "+ spellsgrades.size() + " spells grade were loaded successfully.");

        logger.debug("Loading Trigger Spells effects templates (Retro value) ...");
        Database.getDynamics().getTriggerSpellEffectData().load();
        logger.debug("... The "+ spellstriggers.size() + " Trigger SpellsEffects templates were loaded successfully.");

        logger.debug("Loading Spells effects (Retro value) ...");
        Database.getDynamics().getSpellEffectData().load();
        logger.debug("... All spells effects were loaded successfully.");

        logger.debug("Affecting Type to SpellGrade ...");
        for (SpellGrade Sg : spellsgrades.values()) {
            Sg.setTypeSwitchSpellEffects();
        }

        logger.debug("... All SpellGrade have been Typed successfully.");

        logger.debug("Loading Monster's templates ...");
        Database.getDynamics().getMonsterData().load();
        logger.debug("... The "+ MobTemplates.size() +" monster's templates were loaded successfully.");

        logger.debug("Loading object's templates ...");
        Database.getDynamics().getObjectTemplateData().load();
        logger.debug("... The "+ ObjTemplates.size() +" object's templates were loaded successfully.");
        logger.debug("... "+Boutique.items.size()+" Boutique object's were loaded successfully.");

        logger.debug("Loading already generated objects...");
        Database.getStatics().getObjectData().load();
        logger.debug("... The "+ objects.size() +" generated objects were loaded successfully.");

        logger.debug("Loading NPC template ...");
        Database.getDynamics().getNpcTemplateData().load();
        logger.debug("... The "+ npcsTemplate.size()+ " NPC template were loaded successfully.");

        logger.debug("Loading NPC's questions ...");
        Database.getDynamics().getNpcQuestionData().load();
        logger.debug("... The "+ questions.size() +" NPC's questions were loaded successfully.");

        logger.debug("Loading NPC's answers ...");
        Database.getDynamics().getNpcAnswerData().load();
        logger.debug("... The "+ answers.size() +" NPC's answers were loaded successfully.");

        logger.debug("Loading Quest Goals ...");
        Database.getDynamics().getQuestObjectiveData().load();
        logger.debug("... The "+ QuestObjectif.getQuestObjectifList().size() +" quest goals were loaded successfully.");

        logger.debug("Loading Quest steps ...");
        Database.getDynamics().getQuestStepData().load();
        logger.debug("... The "+ QuestStep.getQuestStepList().size() +" quest steps were loaded successfully.");

        logger.debug("Loading Quest data ...");
        Database.getDynamics().getQuestData().load();
        logger.debug("... The "+ Quest.getQuestList().size() +" quests data were loaded successfully.");

        logger.debug("Adding Quest data to NPC ...");
        Database.getDynamics().getNpcTemplateData().loadQuest();
        logger.debug("... All the quests have been added on non-player characters.");

        Database.getDynamics().getPrismData().load();
        logger.debug("The prisms were loaded successfully.");
        Database.getStatics().getClasseData().load();
        logger.debug("The statics classes were loaded successfully.");
        Database.getStatics().getAreaData().load();
        logger.debug("The statics areas data were loaded successfully.");
        Database.getDynamics().getAreaData().load();
        logger.debug("The dynamics areas data were loaded successfully.");

        Database.getStatics().getSubAreaData().load();
        logger.debug("The statics sub-areas data were loaded successfully.");
        Database.getDynamics().getSubAreaData().load();
        logger.debug("The dynamics sub-areas data were loaded successfully.");

        Database.getDynamics().getInteractiveDoorData().load();
        logger.debug("The templates of interactive doors were loaded successfully.");

        Database.getDynamics().getInteractiveObjectData().load();
        logger.debug("The templates of interactive objects were loaded successfully.");

        Database.getDynamics().getCraftData().load();
        logger.debug("The crafts were loaded successfully.");

        Database.getDynamics().getJobData().load();
        logger.debug("The jobs were loaded successfully.");

        Database.getDynamics().getObjectSetData().load();
        logger.debug("The panoplies were loaded successfully.");

        Database.getDynamics().getMapData().load();
        logger.debug("The maps were loaded successfully.");

        Database.getDynamics().getScriptedCellData().load();
        logger.debug("The scripted cells were loaded successfully.");

        Database.getDynamics().getEndFightActionData().load();
        logger.debug("The end fight actions were loaded successfully.");

        Database.getDynamics().getNpcData().load();
        logger.debug("The placement of non-player character were done successfully.");

        Database.getStatics().getPetData().load();
        logger.debug("The pets were loaded successfully.");

        Database.getDynamics().getObjectActionData().load();
        logger.debug("The action of objects were loaded successfully.");

        Database.getDynamics().getDropData().load();
        logger.debug("The drops were loaded successfully.");

        logger.debug("The mounts were loaded successfully.");

        Database.getDynamics().getAnimationData().load();
        logger.debug("The animations were loaded successfully.");

        if(Config.INSTANCE.getAZURIOM()) {
            logger.debug("Loading Web Accounts ...");
            Database.getSites().getAccountWebData().load();
            logger.debug("... "+ accountsWeb.size()+ " Web Accounts were loaded successfully.");
        }

        logger.debug("Loading Game Accounts ...");
        Database.getStatics().getAccountData().load();
        logger.debug("... "+accounts.size()+" Game Accounts were loaded successfully.");

        if(Config.INSTANCE.getAZURIOM()) {
            Database.getSites().getAccountWebData().syncGameAccountWithWebAccount();
            logger.debug("Linkage between GameAccount and WebAccount done successfully.");
        }

        logger.debug("Loading Players ...");
        Database.getStatics().getPlayerData().load();
        logger.debug("The "+ players.size()+" players were loaded successfully.");

        logger.debug("Loading Players Quests ...");
        Database.getStatics().getQuestPlayerData().loadAll();
        logger.debug("The Players Quests were loaded successfully.");

        Database.getDynamics().getGuildMemberData().load();
        logger.debug("The guilds and guild members were loaded successfully.");

        Database.getStatics().getTitleData().load();
        logger.debug("The titles were loaded successfully.");

        Database.getStatics().getSetsData().load();
        logger.debug("The sets were loaded successfully.");

        Database.getDynamics().getTutorialData().load();
        logger.debug("The tutorials were loaded successfully.");

        Database.getStatics().getMountParkData().load();
        logger.debug("The statics parks of the mounts were loaded successfully.");
        Database.getDynamics().getMountParkData().load();
        logger.debug("The dynamics parks of the mounts were loaded successfully.");

        Database.getDynamics().getCollectorData().load();
        logger.debug("The collectors were loaded successfully.");

        Database.getStatics().getHouseData().load();
        logger.debug("The statics houses were loaded successfully.");
        Database.getDynamics().getHouseData().load();
        logger.debug("The dynamics houses were loaded successfully.");

        Database.getStatics().getTrunkData().load();
        logger.debug("The statics trunks were loaded successfully.");
        Database.getDynamics().getTrunkData().load();
        logger.debug("The dynamics trunks were loaded successfully.");

        if(Config.INSTANCE.getAUTO_CLEAN()){
            clearInactiveAccounts();
            logger.debug("Nettoyage des comptes vides ou inactifs terminé.");
            //TODO Supprimer les comptes inactifs
            //TODO Supprimer les liens web des compte supprimés

            clearInactiveGuilds();
            logger.debug("Nettoyage des guildes vides ou inactives terminé.");

        }
        clearInactiveHousesTrunk();

        Database.getDynamics().getZaapData().load();
        logger.debug("The zaaps were loaded successfully.");

        Database.getDynamics().getZaapiData().load();
        logger.debug("The zappys were loaded successfully.");

        Database.getDynamics().getChallengeData().load();
        logger.debug("The challenges were loaded successfully.");

        Database.getDynamics().getHdvData().load();
        logger.debug("The hotels of sales were loaded successfully.");

        Database.getDynamics().getHdvObjectData().load();
        logger.debug("The objects of hotels were loaded successfully.");

        Database.getDynamics().getDungeonData().load();
        logger.debug("The dungeons were loaded successfully.");

        Database.getDynamics().getRuneData().load(null);
        logger.debug("The runes were loaded successfully.");

        loadExtraMonster();
        logger.debug("The adding of extra-monsters on the maps were done successfully.");

        loadMonsterOnMap();
        logger.debug("The adding of mobs groups on the maps were done successfully.");

        Database.getDynamics().getGangsterData().load();
        logger.debug("The adding of gangsters on the maps were done successfully.");

        logger.debug("Initialization of the dungeon : Dragon Pig.");
        PigDragon.initialize();
        logger.debug("Initialization of the dungeon : Labyrinth of the Minotoror.");
        Minotoror.initialize();
        logger.debug("Initialization of the dungeon : Hotomani.");
        Hotomani.initialize();
        logger.debug("Initialization of the dungeons : Gladiatrool.");
        Gladiatrool.initialize();
        logger.debug("Initialisation de la boutique IG.");
        Boutique.initPacket();

        Database.getStatics().getServerData().updateTime(time);
        logger.info("All data was loaded successfully at "
        + new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss", Locale.FRANCE).format(new Date()) + " in "
                + new SimpleDateFormat("mm", Locale.FRANCE).format((System.currentTimeMillis() - time)) + " min "
                + new SimpleDateFormat("ss", Locale.FRANCE).format((System.currentTimeMillis() - time)) + " s.");


        if(Config.INSTANCE.getAZURIOM()) {
            logger.debug("Initialisation Connection with Azuriom.");
            new AzuriomCommands(2333).start();
        }

        if(Config.INSTANCE.getDISCORD_BOT()) {
            bot.start();
            logger.debug("Initialisation Connection with Discord.");
        }

        sendWebhookInformationsServeur("Le serveur de jeu est désormais accessible !");
        clearDoubleMountDupplication();


        if(Config.INSTANCE.getLOG()){
            logger.setLevel(Level.ALL);
        }
        else{
            logger.setLevel(Level.DEBUG);
        }
    }


    // L'ensemble des fonction ci dessous concerne le nettoyage automatique
    private void clearDoubleMountDupplication() {
        for(GameObject DD : objects.values()){
            if(DD.getTemplate().getType() == Constant.ITEM_TYPE_CERTIF_MONTURE){
                boolean dupplication = false;
                int idDD = -1*DD.getStats().getEffect(Constant.STATS_DD_ID);
                if(idDD == 0){
                    dupplication = true;
                    System.out.println("DD " + idDD + " - ID non valide" );
                }

                if(!dupplication) {
                    Mount RealMount = World.world.getMountById(idDD);
                    if (RealMount == null) {
                        dupplication = true;
                        System.out.println("DD " + idDD + " - non trouvée" );
                    }

                    if(!dupplication) {
                        for (MountPark test : World.world.getMountparks().values()) {
                            if (test.getListOfRaising().contains(idDD)) {
                                dupplication = true;
                                System.out.println("DD " + idDD + " - déjà en enclos" );
                                break;
                            }
                            if (test.getEtable().contains(RealMount)) {
                                System.out.println("DD " + idDD + " - déjà en étable" );
                                dupplication = true;
                                break;
                            }
                        }
                    }
                    if(!dupplication) {
                        for (Player player : World.world.getPlayers()) {
                            if (player.getMount() == RealMount){
                                System.out.println("DD " + idDD + " - déjà équipée" );
                                dupplication = true;
                                break;
                            }
                        }
                    }
                }
                if(dupplication){
                    DD.getTxtStat().put(EffectConstant.EFFECTID_INVALID_MOUNT,"1");
                    DD.getTxtStat().remove(Constant.STATS_DD_OWNER);
                    DD.getTxtStat().remove(Constant.STATS_DD_NAME);
                    DD.getStats().getEffects().remove(Constant.STATS_DD_ID);
                    DD.setModification();
                }
            }
        }

    }

    public boolean checkIfDDAlreadySomeWhereElse(int idDD) {
        Mount RealMount = World.world.getMountById(idDD);
        if (RealMount == null) {
            return false;
        }

        for (MountPark test : World.world.getMountparks().values()) {
            if (test.getListOfRaising().contains(idDD)) {
                System.out.println("DD " + idDD + " - déjà en enclos" );
                return true;
            }
            if (test.getEtable().contains(RealMount)) {
                System.out.println("DD " + idDD + " - déjà en étable" );
                return true;
            }
        }

        for (Player player : World.world.getPlayers()) {
            if (player.getMount() == RealMount){
                System.out.println("DD " + idDD + " - déjà équipée" );
                return true;
            }
        }
        return false;
    }

    // L'ensemble des fonction ci dessous concerne le nettoyage automatique
    private void clearInactiveHousesTrunk() {
        if(!houseToClear.isEmpty()) {
            for (House house : houseToClear) {
                for (Trunk trunk : Trunk.getTrunksByHouse(house)) {
                    for(Long itemID : trunk.getObject().keySet()){
                        World.world.removeGameObject(itemID);
                    }
                    trunk.getObject().clear();
                    trunk.setKamas(0);//Retrait kamas
                    trunk.setKey("-");//ResetPass
                    trunk.setOwnerId(0);//ResetOwner
                    Database.getDynamics().getTrunkData().update(trunk);
                }
            }
        }
    }

    private void clearInactiveAccounts() {
        int accountCleared = 0;
        int playerCleared = 0;
        LocalDate currentDate = LocalDate.now();
        LocalDate UnUsedDate = currentDate.minus(Constant.AUTO_CLEAN_MONTH, ChronoUnit.MONTHS);
        ZonedDateTime MaximumDate = UnUsedDate.atStartOfDay(ZoneId.systemDefault());

        ArrayList<Integer> ids = new ArrayList<>();
        ArrayList<Account> allAccounts = new ArrayList<>();
        allAccounts.addAll(accounts.values());

        for (Account account : allAccounts) {
            if (Config.INSTANCE.getAUTO_CLEAN()) {
                if (Config.INSTANCE.getAZURIOM()) {
                    AccountWeb wA = account.getWebAccount();
                    if (wA != null) {
                        Timestamp timeStamp = wA.getLastConnectionDate();
                        if (timeStamp != null) {
                            Instant insant = timeStamp.toInstant();
                            // Convertir l'Instant en ZonedDateTime
                            ZonedDateTime zonedDateTimeFromMysql = insant.atZone(ZoneId.systemDefault());
                            if (zonedDateTimeFromMysql.isBefore(MaximumDate)) {
                                if (wA.getRole() != 1) {
                                    System.out.println("Le compte " + account.getName() + " doit être supprimé mais ne le sera pas car Donateur ou au dessus");
                                } else {
                                    //if (perso.getGroupe() == null) {
                                    LocalDateTime now = LocalDateTime.now();
                                    LocalDate firstDate = now.toLocalDate();
                                    LocalDate UnUsedDate3 = firstDate.plusMonths(-(Constant.AUTO_CLEAN_MONTH));
                                    String dateactuelle = account.getLastConnectionDate();
                                    LocalDate datecreation = LocalDate.parse(account.creationDate);
                                    if(!dateactuelle.isEmpty()) {
                                        String[] table = dateactuelle.split("~");
                                        LocalDate lastConnection = LocalDate.parse(table[0] + "-" + table[1] + "-" + table[2]);
                                        if (lastConnection.isBefore(UnUsedDate3)) {
                                            System.out.println("Le compte " + account.getName() + " va etre supprimé car ne s'est plus connecté depuis plus de " + Constant.AUTO_CLEAN_MONTH + " mois :" + zonedDateTimeFromMysql + " / Derniere connexion au jeu " + lastConnection);
                                            fullClearAccount(account);
                                            accountCleared++;
                                        }
                                    }
                                    else {
                                        //World.world.sendWebhookInformationsServeur("Le compte **" + account.getPseudo() + "** est supprimé car ne s'est jamais connecté et créé depuis plus de " + Constant.AUTO_CLEAN_MONTH + " mois :" + datecreation);
                                        System.out.println("Le compte " + account.getPseudo() + " doit être supprimé car le compte ne s'est jamais connecté et créé depuis plus de " + Constant.AUTO_CLEAN_MONTH + " mois : " + datecreation);
                                        fullClearAccount(account);
                                        accountCleared++;
                                    }
                                    //}
                                }
                            }
                        } else {
                            System.out.println("Etrangement pas de dernière connexion au compte Web " + wA.getName());
                        }
                    } else {

                        //if (perso.getGroupe() == null) {
                            LocalDateTime now = LocalDateTime.now();
                            LocalDate firstDate = now.toLocalDate();
                            LocalDate UnUsedDate3 = firstDate.plusMonths(-(Constant.AUTO_CLEAN_MONTH));
                            String dateactuelle = account.getLastConnectionDate();
                            LocalDate datecreation = LocalDate.parse(account.creationDate);
                            if(!dateactuelle.isEmpty()) {
                                String[] table = dateactuelle.split("~");
                                LocalDate lastConnection = LocalDate.parse(table[0] + "-" + table[1] + "-" + table[2]);
                                //System.out.println("Pas de compte Web associé au compte " + account.getName() + " dernière connexion en jeu " + lastConnection);
                                if (lastConnection.isBefore(UnUsedDate3)) {
                                    //World.world.sendWebhookInformationsServeur("Le compte **" + account.getPseudo() + "** est supprimé car ne s'est plus connecté depuis plus de " + Constant.AUTO_CLEAN_MONTH + " mois :" + lastConnection);
                                    System.out.println("Le compte " + account.getPseudo() + " doit être supprimé car le compte n'est plus utilisé depuis plus de " + Constant.AUTO_CLEAN_MONTH + " mois : Derniere connexion au jeu " + lastConnection);
                                    fullClearAccount(account);
                                    accountCleared++;
                                }
                            }
                            else{
                                if(datecreation.isBefore(UnUsedDate3)){
                                    //World.world.sendWebhookInformationsServeur("Le compte **" + account.getPseudo() + "** est supprimé car ne s'est jamais connecté et créé depuis plus de " + Constant.AUTO_CLEAN_MONTH + " mois :" + datecreation);
                                    System.out.println("Le compte " + account.getPseudo() + " doit être supprimé car le compte ne s'est jamais connecté et créé depuis plus de " + Constant.AUTO_CLEAN_MONTH + " mois : " + datecreation);
                                    fullClearAccount(account);
                                    accountCleared++;
                                }
                            }


                        //}
                    }
                }

            }

        }

        System.out.println(accountCleared + " comptes purgés");
        System.out.println(playerCleared + " personnages purgés");
    }

    private Map<String,Integer> fullClearAccount(Account account){
        Map<String,Integer> clearance = new HashMap<>();

        // On supprime tous les joueurs associés au compte et ses dépences
        int nb = clearAllPlayerFromAccount(account);
        clearance.put("Players",nb);

        // On supprime toutes les dépences lié au compte (Banque, Maison ??, Coffre ??, )
        clearAccountBank(account);
        clearance.put("BankObj",nb);

        // On supprime le lien avec le site Web
        if(account.getWebAccount() != null) {
            Database.getSites().getAccountWebData().delete(account.getId());
        }

        // En very last on supprime en bdd
        Database.getStatics().getAccountData().delete(account);

        // On supprime aussi de la liste world
        accounts.remove(account.getId());

        return clearance;
    }

    private int clearAllPlayerFromAccount(Account account){
        int i = 0;
        // Créer une copie de la map originale
        Map<Integer,Player> copiedMap = new HashMap<>(account.getPlayers());

        // Supprimer les entrées une par une de la copie
        Iterator<Map.Entry<Integer,Player>> iterator = copiedMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer,Player> entry = iterator.next();
            System.out.println("Suppression du personnage " + entry.getValue().getName() + " car associé au compte");
            World.sendWebhookInformations(Config.INSTANCE.getDISCORD_CHANNEL_INFO(),"Le personnage "+entry.getValue().getName()+" est supprimé car ne s'est plus connecté depuis plus de 9 mois.",entry.getValue() );
            account.deletePlayer(entry.getValue().getId());
            account.getPlayers().remove(entry.getValue().getId());
            i++;
        }
        return i;
    }

    private int clearAccountBank(Account account){
        int nbObj = 0;
        // On supprime les objets dans la banque
        for (GameObject obj : account.getBank() ) {
            if(obj.getTemplate().getType() == Constant.ITEM_TYPE_FAMILIER){
                World.world.removePets(obj.getGuid());
            }
            World.world.removeGameObject(obj.getGuid());
            nbObj++;
        }

        // On supprime la banque
        Database.getDynamics().getBankData().remove(account.getId());

        return nbObj;
    }

    public void addExtraMonster(int idMob, String superArea,
                                       String subArea, int chances) {
        Map<String, Map<String, Integer>> map = new HashMap<>();
        Map<String, Integer> _map = new HashMap<>();
        _map.put(subArea, chances);
        map.put(superArea, _map);
        extraMonstre.put(idMob, map);
    }

    public Map<Integer, GameMap> getExtraMonsterOnMap() {
        return extraMonstreOnMap;
    }

    public void loadExtraMonster() {
        ArrayList<GameMap> mapPossible = new ArrayList<>();
        for (Entry<Integer, Map<String, Map<String, Integer>>> i : extraMonstre.entrySet()) {
            try {
                Map<String, Map<String, Integer>> map = i.getValue();

                for (Entry<String, Map<String, Integer>> areaChances : map.entrySet()) {
                    Integer chances = null;
                    for (Entry<String, Integer> _e : areaChances.getValue().entrySet()) {
                        Integer _c = _e.getValue();
                        if (_c != null && _c != -1)
                            chances = _c;
                    }
                    if (!areaChances.getKey().equals("")) {// Si la superArea n'est pas null
                        for (String ar : areaChances.getKey().split(",")) {
                            Area Area = areas.get(Integer.parseInt(ar));
                            for (GameMap Map : Area.getMaps()) {
                                if (Map == null)
                                    continue;
                                if (Map.haveMobFix())
                                    continue;
                                if (!Map.isPossibleToPutMonster())
                                    continue;

                                if (chances != null)
                                    Map.addMobExtra(i.getKey(), chances);
                                else if (!mapPossible.contains(Map))
                                    mapPossible.add(Map);
                            }
                        }
                    }
                    if (areaChances.getValue() != null) // Si l'area n'est pas null
                    {
                        for (Entry<String, Integer> area : areaChances.getValue().entrySet()) {
                            String areas = area.getKey();
                            for (String sub : areas.split(",")) {
                                SubArea subArea = null;
                                try {
                                    subArea = subAreas.get(Integer.parseInt(sub));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (subArea == null)
                                    continue;
                                for (GameMap Map : subArea.getMaps()) {
                                    if (Map == null)
                                        continue;
                                    if (Map.haveMobFix())
                                        continue;
                                    if (!Map.isPossibleToPutMonster())
                                        continue;

                                    if (chances != null)
                                        Map.addMobExtra(i.getKey(), chances);
                                    if (!mapPossible.contains(Map))
                                        mapPossible.add(Map);
                                }
                            }
                        }
                    }
                }
                if (mapPossible.size() <= 0) {
                    throw new Exception(" no maps was found for the extra monster " + i.getKey() +".");
                } else {
                    GameMap randomMap;
                    if (mapPossible.size() == 1)
                        randomMap = mapPossible.get(0);
                    else
                        randomMap = mapPossible.get(Formulas.getRandomValue(0, mapPossible.size() - 1));
                    if (randomMap == null)
                        throw new Exception("the random map is null.");
                    if (getMonstre(i.getKey()) == null)
                        throw new Exception("the monster template of the extra monster is invalid (id : " + i.getKey() + ").");
                    if (randomMap.loadExtraMonsterOnMap(i.getKey()))
                        extraMonstreOnMap.put(i.getKey(), randomMap);
                    else
                        throw new Exception("a empty mobs group or invalid monster.");
                }
                mapPossible.clear();
            } catch(Exception e) {
                e.printStackTrace();
                mapPossible.clear();
                logger.error("An error occurred when the server try to put extra-monster caused by : " + e.getMessage());
            }
        }
    }

    public Map<String, String> getGroupFix(int map, int cell) {
        return mobsGroupsFix.get(map + ";" + cell);
    }

    public void addGroupFix(String str, String mob, int Time) {
        mobsGroupsFix.put(str, new HashMap<>());
        mobsGroupsFix.get(str).put("groupData", mob);
        mobsGroupsFix.get(str).put("timer", Time + "");
    }

    public void loadMonsterOnMap() {
        if(Config.INSTANCE.getHEROIC()) {
            Database.getDynamics().getHeroicMobsGroups().load();
        }
        maps.values().stream().filter(map -> map != null).forEach(map -> {
            try {
                map.loadMonsterOnMap();
            } catch (Exception e) {
                logger.error("An error occurred when the server try to put monster on the map id " + map.getId() + ".");
            }
        });
    }

    public Area getArea(int areaID) {
        return areas.get(areaID);
    }


    public SubArea getSubArea(int areaID) {
        return subAreas.get(areaID);
    }

    public void addArea(Area area) {
        areas.put(area.getId(), area);
    }

    public void addClasse(Classe classe) {
        Classes.put(classe.getId(), classe);
    }


    public void addSubArea(SubArea SA) {
        subAreas.put(SA.getId(), SA);
    }

    public String getSousZoneStateString() {
        String str = "";
        boolean first = false;
        for (SubArea subarea : subAreas.values()) {
            if (!subarea.getConquistable())
                continue;
            if (first)
                str += "|";
            str += subarea.getId() + ";" + subarea.getAlignement();
            first = true;
        }
        return str;
    }

    public void addNpcAnswer(NpcAnswer rep) {
        answers.put(rep.getId(), rep);
    }

    public void delNpcAnswer(NpcAnswer rep) {
        answers.remove(rep.getId(), rep);
    }

    public NpcAnswer getNpcAnswer(int guid) {
        return answers.get(guid);
    }

    public double getBalanceArea(Area area, int alignement) {
        int cant = 0;
        for (SubArea subarea : subAreas.values()) {
            if (subarea.getArea() == area
                    && subarea.getAlignement() == alignement)
                cant++;
        }
        if (cant == 0)
            return 0;
        return Math.rint((1000 * cant / (area.getSubAreas().size())) / 10);
    }

    public double getBalanceWorld(int alignement) {
        int cant = 0;
        for (SubArea subarea : subAreas.values()) {
            if (subarea.getAlignement() == alignement)
                cant++;
        }

        if (cant == 0)
            return 0;
        return Math.rint((10 * cant / 4) / 10);
    }

    public double getBalanceWorldPercent(int alignement) {
        double i = 0.0D;
        double tot = 0.0D;

        for (SubArea subarea : subAreas.values()) {
            if (subarea.getAlignement() != 0){
                tot++;
                if (subarea.getAlignement() == alignement) {
                    i++;
                }
            }
        }

        double ratio = (double) i / tot * 100;
        return Math.floor(ratio);
    }

    public double getBalanceWorldNew(int alignement) {
        double i = 0.0D;
        double tot = 0.0D;
        double totfull = subAreas.size();
        int nonValue = 0;
        for (SubArea subarea : subAreas.values()) {
            if (subarea.getAlignement() != 0){
                tot++;
                if (subarea.getAlignement() == alignement) {
                    i++;
                }
            }
            else{
                nonValue++;
            }
        }

        double ratio = (double) i / tot;
        double distance = ratio - 0.5;
        double factor = 100 * (1 - 4 * Math.pow(distance, 2));

        if (tot == 0 || factor<0 || i==0)
            return 0;

        return Math.floor(factor);
    }

    public double getConquestBonusNew(Player player) {
        if(player == null) return 1;
        if(player.get_align() == 0) return 1;
        if(player.getCurMap().getSubArea().getAlignement() == player.get_align()) {
            if(player.is_showWings()) {
                final double factor = (100 + (getBalanceWorldNew(player.get_align()) * (1.0D + ((Math.rint((player.getGrade() / 2.5) + 1)) * 4)/100)) ) / 100;
                if (factor < 1) return 1;
                return factor;
            }
            return 1;
        }
        else {
            return 1;
        }
    }

    public double getConquestBonus(Player player) {
        if(player == null) return 1;
        if(player.get_align() == 0) return 1;
        final double factor = 1 + (getBalanceWorld(player.get_align()) * Math.rint((player.getGrade() / 2.5) + 1)) / 100;
        if(factor < 1) return 1;
        return factor;
    }

    public int getExpLevelSize() {
        return experiences.size();
    }

    public void addExpLevel(int lvl, ExpLevel exp) {
        experiences.put(lvl, exp);
    }

    public void addNPCQuestion(NpcQuestion quest) {
        questions.put(quest.getId(), quest);
    }

    public NpcQuestion getNPCQuestion(int guid) {
        return questions.get(guid);
    }

    public NpcTemplate getNPCTemplate(int guid) {
        return npcsTemplate.get(guid);
    }

    public void addNpcTemplate(NpcTemplate temp) {
        npcsTemplate.put(temp.getId(), temp);
    }

    public void removePlayer(Player player) {
        if (player.getGuild() != null) {
            if (player.getGuild().getPlayers().size() <= 1) {
                removeGuild(player.getGuild().getId());
            } else if (player.getGuildMember().getRank() == 1) {
                int curMaxRight = 0;
                Player leader = null;

                for (Player newLeader : player.getGuild().getPlayers()) {
                    if(leader == null){
                        leader = newLeader;
                    }
                    if (newLeader != player && newLeader.getGuildMember().getRights() < curMaxRight) {
                        leader = newLeader;
                    }
                }

                player.getGuild().removeMember(player);
                if(leader != null) {
                    GuildMember toChange = leader.getGuildMember();
                    toChange.setAllRights(1,(byte)-1,1,leader);
                }
            } else {
                player.getGuild().removeMember(player);
            }
        }
        if(player.getWife() != 0) {
            Player wife = getPlayer(player.getWife());

            if(wife != null) {
                wife.setWife(0);
            }
        }
        Map<Integer,QuestPlayer> qps = player.getQuestPerso();
        for(QuestPlayer qp : qps.values()){
            qp.removeQuestPlayer();
        }

        deletePlayerMountAndObj(player);

        SuppressPerso(player.getId());
        player.removeFromDDB();
        players.remove(player.getId());
    }

    public void deletePlayerMountAndObj(Player player){
        Iterator<Mount> mountIterator = World.world.getMounts().values().iterator();
        while (mountIterator.hasNext()) {
            Mount mount = mountIterator.next();
            if (mount.getOwner() == player.getId()) {
                for (GameObject obj : mount.getObjects().values()) {
                    World.world.removeGameObject(obj.getGuid());
                }

                // Supprime la monture dans la base de données
                Database.getStatics().getMountData().delete(mount.getId());

                // Supprime la monture de la collection en utilisant l'iterator
                mountIterator.remove();

                // Déconnecte la monture du joueur
                player.setMount(null);
            }
        }
    }

    public void unloadPerso(Player perso) {
        unloadPerso(perso.getId());//UnLoad du perso+item
        players.remove(perso.getId());
    }

    public long getPersoXpMin(int _lvl) {
        if (_lvl > getExpLevelSize())
            _lvl = getExpLevelSize();
        if (_lvl < 1)
            _lvl = 1;
        return experiences.get(_lvl).perso;
    }

    public long getPersoXpMax(int _lvl) {
        if (_lvl >= getExpLevelSize())
            _lvl = (getExpLevelSize() - 1);
        if (_lvl <= 1)
            _lvl = 1;
        return experiences.get(_lvl + 1).perso;
    }

    public long getTourmenteursXpMax(int _lvl) {
        if (_lvl >= getExpLevelSize())
            _lvl = (getExpLevelSize() - 1);
        if (_lvl <= 1)
            _lvl = 1;
        return experiences.get(_lvl + 1).tourmenteurs;
    }

    public long getBanditsXpMin(int _lvl) {
        if (_lvl > getExpLevelSize())
            _lvl = getExpLevelSize();
        if (_lvl < 1)
            _lvl = 1;
        return experiences.get(_lvl).bandits;
    }

    public long getBanditsXpMax(int _lvl) {
        if (_lvl >= getExpLevelSize())
            _lvl = (getExpLevelSize() - 1);
        if (_lvl <= 1)
            _lvl = 1;
        return experiences.get(_lvl + 1).bandits;
    }


    public void addSort(Spell sort) {
        spells.put(sort.getSpellID(), sort);
    }

    public void addSortGrade(SpellGrade sort) {
        spellsgrades.put(sort.getSpellID()+"_"+sort.getLevel(), sort);
    }

    public Spell getSort(int id) {
        return spells.get(id);
    }



    public Spell getSpellbyName(String name) {
        for(Spell s: this.spells.values()){
            if(s.getName().equals(name))
                return s;
        }
        return null;
    }

    public void addEffectTrigger(EffectTrigger trigger) {
        spellstriggers.put(trigger.getTriggerID(), trigger);
    }

    public EffectTrigger getEffectTrigger(int id) {
        return spellstriggers.get(id);
    }

    public void addObjTemplate(ObjectTemplate obj) {
        ObjTemplates.put(obj.getId(), obj);
    }

    public ObjectTemplate getObjTemplate(int id) {
        return ObjTemplates.get(id);
    }

    public ArrayList<ObjectTemplate> getEtherealWeapons(int level) {
        ArrayList<ObjectTemplate> array = new ArrayList<>();
        final int levelMin = (level - 5 < 0 ? 0 : level - 5), levelMax = level + 5;
        getObjectsTemplates().values().stream().filter(objectTemplate -> objectTemplate != null && objectTemplate.getStrTemplate().contains("32c#")
                && (levelMin < objectTemplate.getLevel() && objectTemplate.getLevel() < levelMax) && objectTemplate.getType() != 93).forEach(array::add);
        return array;
    }

    public void addMobTemplate(int id, Monster mob) {
        MobTemplates.put(id, mob);
    }

    public Monster getMonstre(int id) {
        return MobTemplates.get(id);
    }

    public Collection<Monster> getMonstres() {
        return MobTemplates.values();
    }


    public String getStatOfAlign() {
        int ange = 0;
        int demon = 0;
        int total = 0;
        for (Player i : getPlayers()) {
            if (i == null)
                continue;
            if (i.get_align() == 1)
                ange++;
            if (i.get_align() == 2)
                demon++;
            total++;
        }
        ange = ange / total;
        demon = demon / total;
        if (ange > demon)
            return "Les Brâkmarien sont actuellement en minorité, je peux donc te proposer de rejoindre les rangs Brâkmarien ?";
        else if (demon > ange)
            return "Les Bontarien sont actuellement en minorité, je peux donc te proposer de rejoindre les rangs Bontarien ?";
        else if (demon == ange)
            return " Aucune milice est actuellement en minorité, je peux donc te proposer de rejoindre aléatoirement une milice ?";
        return "Undefined";
    }

    public void addIOTemplate(InteractiveObjectTemplate IOT) {
        IOTemplate.put(IOT.getId(), IOT);
    }

    public Mount getMountById(int id) {
        Mount mount = Dragodindes.get(id);
        if(mount == null) {
            Database.getStatics().getMountData().load(id);
            mount = Dragodindes.get(id);
        }
        return mount;
    }

    public void addMount(Mount mount) {
        Dragodindes.put(mount.getId(), mount);
    }

    public void removeMount(long id) {
        Dragodindes.remove(id);
    }

    public void addTutorial(Tutorial tutorial) {
        Tutorial.put(tutorial.getId(), tutorial);
    }

    public Tutorial getTutorial(int id) {
        return Tutorial.get(id);
    }

    public ExpLevel getExpLevel(int lvl) {
        return experiences.get(lvl);
    }

    public InteractiveObjectTemplate getIOTemplate(int id) {
        return IOTemplate.get(id);
    }

    public Job getMetier(int id) {
        return Jobs.get(id);
    }

    public void addJob(Job metier) {
        Jobs.put(metier.getId(), metier);
    }

    public void addCraft(int id, ArrayList<Couple<Integer, Integer>> m) {
        Crafts.put(id, m);
    }

    public ArrayList<Couple<Integer, Integer>> getCraft(int i) {
        return Crafts.get(i);
    }

    public void addFullMorph(int morphID, String name, int gfxID,
                                    String spells, String[] args) {
        if (fullmorphs.get(morphID) != null)
            return;

        fullmorphs.put(morphID, new HashMap<>());

        fullmorphs.get(morphID).put("name", name);
        fullmorphs.get(morphID).put("gfxid", gfxID + "");
        fullmorphs.get(morphID).put("spells", spells);
        if (args != null) {
            fullmorphs.get(morphID).put("vie", args[0]);
            fullmorphs.get(morphID).put("pa", args[1]);
            fullmorphs.get(morphID).put("pm", args[2]);
            fullmorphs.get(morphID).put("vitalite", args[3]);
            fullmorphs.get(morphID).put("sagesse", args[4]);
            fullmorphs.get(morphID).put("terre", args[5]);
            fullmorphs.get(morphID).put("feu", args[6]);
            fullmorphs.get(morphID).put("eau", args[7]);
            fullmorphs.get(morphID).put("air", args[8]);
            fullmorphs.get(morphID).put("initiative", args[9]);
            fullmorphs.get(morphID).put("stats", args[10]);
            fullmorphs.get(morphID).put("donjon", args[11]);
            if(Constant.isGladiatroolMorph(morphID)){
                fullmorphs.get(morphID).put("do", args[12]);
                fullmorphs.get(morphID).put("doper", args[13]);
                fullmorphs.get(morphID).put("invo", args[14]);
                fullmorphs.get(morphID).put("esPA", args[15]);
                fullmorphs.get(morphID).put("esPM", args[16]);
                fullmorphs.get(morphID).put("resiNeu", args[17]);
                fullmorphs.get(morphID).put("resiTer", args[18]);
                fullmorphs.get(morphID).put("resiFeu", args[19]);
                fullmorphs.get(morphID).put("resiEau", args[20]);
                fullmorphs.get(morphID).put("resiAir", args[21]);
                fullmorphs.get(morphID).put("PO", args[22]);
                fullmorphs.get(morphID).put("soin", args[23]);
                fullmorphs.get(morphID).put("crit", args[24]);
                fullmorphs.get(morphID).put("rfixNeu", "0");
                fullmorphs.get(morphID).put("rfixTer", "0");
                fullmorphs.get(morphID).put("rfixFeu", "0");
                fullmorphs.get(morphID).put("rfixEau", "0");
                fullmorphs.get(morphID).put("rfixAir", "0");
                fullmorphs.get(morphID).put("renvoie", "0");
                fullmorphs.get(morphID).put("dotrap", "0");
                fullmorphs.get(morphID).put("perdotrap", "0");
                fullmorphs.get(morphID).put("dophysique", "0");
            }
        }
    }

    public Map<String, String> getFullMorph(int morphID) {
        return fullmorphs.get(morphID);
    }

    public int getObjectByIngredientForJob(ArrayList<Integer> list,
                                                  Map<Integer, Integer> ingredients) {
        if (list == null)
            return -1;
        for (int tID : list) {
            ArrayList<Couple<Integer, Integer>> craft = getCraft(tID);
            if (craft == null)
                continue;
            if (craft.size() != ingredients.size())
                continue;
            boolean ok = true;
            for (Couple<Integer, Integer> c : craft) {
                if (!((ingredients.get(c.first) + " ").equals(c.second + " "))) //si ingredient non pr�sent ou mauvaise quantit�
                    ok = false;
            }
            if (ok)
                return tID;
        }
        return -1;
    }



    public void addItemSet(ObjectSet itemSet) {
        ItemSets.put(itemSet.getId(), itemSet);
    }

    public ObjectSet getItemSet(int tID) {
        return ItemSets.get(tID);
    }

    public int getItemSetNumber() {
        return 250;
    }

    public ArrayList<GameMap> getMapByPosInArray(int mapX, int mapY) {
        ArrayList<GameMap> i = new ArrayList<>();
        for (GameMap map : maps.values())
            if (map.getX() == mapX && map.getY() == mapY)
                i.add(map);
        return i;
    }

    public ArrayList<GameMap> getMapByPosInArrayPlayer(int mapX, int mapY, Player player) {
        return maps.values().stream().filter(map -> map != null && map.getSubArea() != null && player.getCurMap().getSubArea() != null).filter(map -> map.getX() == mapX && map.getY() == mapY && map.getSubArea().getArea().getSuperArea() == player.getCurMap().getSubArea().getArea().getSuperArea()).collect(Collectors.toCollection(ArrayList::new));
    }

    public void addGuild(Guild g, boolean save) {
        Guildes.put(g.getId(), g);
        if (save)
            Database.getStatics().getGuildData().add(g);
    }

    public boolean guildNameIsUsed(String name) {
        for (Guild g : Guildes.values())
            if (g.getName().equalsIgnoreCase(name))
                return true;
        return false;
    }

    public boolean guildEmblemIsUsed(String emb) {
        for (Guild g : Guildes.values()) {
            if (g.getEmblem().equals(emb))
                return true;
        }
        return false;
    }

    public Guild getGuild(int i) {
        Guild guild = Guildes.get(i);
        if(guild == null) {
            Database.getStatics().getGuildData().load(i);
            guild = Guildes.get(i);
        }
        return guild;
    }

    public int getGuildByName(String name) {
        for (Guild g : Guildes.values()) {
            if (g.getName().equalsIgnoreCase(name))
                return g.getId();
        }
        return -1;
    }

    public long getGuildXpMax(int _lvl) {
        if (_lvl >= 200)
            _lvl = 199;
        if (_lvl <= 1)
            _lvl = 1;
        return experiences.get(_lvl + 1).guilde;
    }

    public void ReassignAccountToChar(Account account) {
        Database.getStatics().getPlayerData().loadByAccountId(account.getId());
        players.values().stream().filter(player -> player.getAccID() == account.getId()).forEach(player -> player.setAccount(account));
    }

    /*public void ReassignAccountWebToAccount(AccountWeb accountweb) {
        Database.getStatics().getAccountData().loadByAccountWebId(accountweb.getId());
        accounts.values().stream().filter(account -> account.getAccWebID() == accountweb.getId()).forEach(account -> account.setWebaccount(accountweb));
    }*/

    public int getZaapCellIdByMapId(short i) {
        for (Entry<Integer, Integer> zaap : Constant.ZAAPS.entrySet()) {
            if (zaap.getKey() == i)
                return zaap.getValue();
        }
        return -1;
    }

    public int getEncloCellIdByMapId(short i) {
        GameMap map = getMap(i);
        if(map != null && map.getMountPark() != null && map.getMountPark().getCell() > 0)
            return map.getMountPark().getCell();
        return -1;
    }

    public void delDragoByID(long getId) {
        Dragodindes.remove(getId);
    }

    public void removeGuild(int id) {
        this.getHouseManager().removeHouseGuild(id);
        GameMap.removeMountPark(id);
        Collector.removeCollector(id);
        Guildes.remove(id);
        Database.getDynamics().getGuildMemberData().deleteAll(id);
        Database.getStatics().getGuildData().delete(id);
    }

    public void unloadPerso(int g) {
        Player toRem = players.get(g);
        if (!toRem.getItems().isEmpty())
            for (Entry<Long, GameObject> curObj : toRem.getItems().entrySet())
                objects.remove(curObj.getKey());
    }

    public void SuppressPerso(int g) {
        Player toRem = players.get(g);
        if (!toRem.getItems().isEmpty()) {
            for (Entry<Long, GameObject> curObj : toRem.getItems().entrySet()) {
                if(curObj.getValue().getTemplate().getType() == Constant.ITEM_TYPE_FAMILIER){
                    World.world.removePets(curObj.getKey());
                }
                World.world.removeGameObject(curObj.getKey());
            }
        }

        if(!toRem.getStoreItems().isEmpty()){
            for (Entry<Long, Integer> curObj : toRem.getStoreItems().entrySet()) {
                World.world.removeGameObject(curObj.getKey());
            }
        }
    }

    public GameObject newObjet(long id, int template, int qua, int pos, String stats, int puit, int rarity, int mimibiote) {
        if (getObjTemplate(template) == null) {
            return null;
        }

        if (template == 8378) {
            return new Fragment(id, stats);
        } else if (getObjTemplate(template).getType() == Constant.ITEM_TYPE_PIERRE_AME_PLEINE_ARCHI || getObjTemplate(template).getType() == Constant.ITEM_TYPE_PIERRE_AME_PLEINE || getObjTemplate(template).getType() == Constant.ITEM_TYPE_PIERRE_AME_PLEINE_BOSS) {
            return new SoulStone(id, qua, template, pos, stats);
        } else if (getObjTemplate(template).getType() == 24 && (Constant.isCertificatDopeuls(getObjTemplate(template).getId()) || getObjTemplate(template).getId() == 6653)) {
            try {
                Map<Integer, String> txtStat = new HashMap<>();
                txtStat.put(Constant.STATS_DATE, stats.substring(3) + "");
                return new GameObject(id, template, qua, Constant.ITEM_POS_NO_EQUIPED, new Stats(false, null), new ArrayList<>(), new HashMap<>(), txtStat, puit, rarity, mimibiote);
            } catch (Exception e) {
                e.printStackTrace();
                return new GameObject(id, template, qua, pos, stats, 0,rarity, -1);
            }
        }
        else {
            return new GameObject(id, template, qua, pos, stats, 0,rarity, mimibiote);
        }
    }

    public Map<Integer, Integer> getChangeHdv() {
        Map<Integer, Integer> changeHdv = new HashMap<>();
        changeHdv.put(8753, 8759); // HDV Annimaux
        changeHdv.put(4607, 4271); // HDV Alchimistes
        changeHdv.put(4622, 4216); // HDV Bijoutiers
        changeHdv.put(4627, 4232); // HDV Bricoleurs
        changeHdv.put(5112, 4178); // HDV Bûcherons
        changeHdv.put(4562, 4183); // HDV Cordonniers
        changeHdv.put(8754, 8760); // HDV Bibliothèque
        changeHdv.put(5317, 4098); // HDV Forgerons
        changeHdv.put(4615, 4247); // HDV Pêcheurs
        changeHdv.put(4646, 4262); // HDV Ressources
        changeHdv.put(8756, 8757); // HDV Forgemagie
        changeHdv.put(4618, 4174); // HDV Sculpteurs
        changeHdv.put(4588, 4172); // HDV Tailleurs
        changeHdv.put(8482, 10129); // HDV Âmes
        changeHdv.put(4595, 4287); // HDV Bouchers
        changeHdv.put(4630, 2221); // HDV Boulangers
        changeHdv.put(5311, 4179); // HDV Mineurs
        changeHdv.put(4629, 4299); // HDV Paysans
        changeHdv.put(-1, 1); // HDV Global
        return changeHdv;
    }

    // Utilis� deux fois. Pour tous les modes HDV dans la fonction getHdv ci-dessous et dans le mode Vente de GameClient.java
    public int changeHdv(int map) {
        Map<Integer, Integer> changeHdv = getChangeHdv();
        if (changeHdv.containsKey(map)) {
            map = changeHdv.get(map);
        }
        return map;
    }

    public Hdv getHdv(int map) {
        if(Hdvs.get(changeHdv(map)) != null){
            return Hdvs.get(changeHdv(map));
        }
        else{
            return Hdvs.get(-1);
        }
    }

    public synchronized int getNextObjectHdvId() {
        nextObjectHdvId++;
        return nextObjectHdvId;
    }

    public synchronized void setNextObjectHdvId(int id) {
        nextObjectHdvId = id;
    }

    public synchronized int getNextLineHdvId() {
        nextLineHdvId++;
        return nextLineHdvId;
    }

    public void addHdvItem(int compteID, int hdvID, HdvEntry toAdd) {
        if (hdvsItems.get(compteID) == null) //Si le compte n'est pas dans la memoire
            hdvsItems.put(compteID, new HashMap<>()); //Ajout du compte cl�:compteID et un nouveau Map<hdvID,items<>>
        if (hdvsItems.get(compteID).get(hdvID) == null)
            hdvsItems.get(compteID).put(hdvID, new ArrayList<>());
        hdvsItems.get(compteID).get(hdvID).add(toAdd);
    }



    public void removeHdvItem(int compteID, int hdvID, HdvEntry toDel) {
        hdvsItems.get(compteID).get(hdvID).remove(toDel);
    }

    public void addHdv(Hdv toAdd) {
        Hdvs.put(toAdd.getHdvId(), toAdd);
    }

    public Map<Integer, ArrayList<HdvEntry>> getMyItems(
            int compteID) {
        if (hdvsItems.get(compteID) == null)//Si le compte n'est pas dans la memoire
            hdvsItems.put(compteID, new HashMap<>());//Ajout du compte cl�:compteID et un nouveau Map<hdvID,items
        return hdvsItems.get(compteID);
    }

    public Collection<ObjectTemplate> getObjTemplates() {
        return ObjTemplates.values();
    }

    public void priestRequest(Player boy, Player girl, Player asked) {
        if(boy.getSexe() == 0 && girl.getSexe() == 1) {
            final GameMap map = boy.getCurMap();
            if (boy.getWife() != 0) {// 0 : femme | 1 = homme
                boy.setBlockMovement(false);
                SocketManager.GAME_SEND_MESSAGE_TO_MAP(map, boy.getName() + " est déjà marier !", Config.INSTANCE.getColorMessage());
                return;
            }
            if (girl.getWife() != 0) {
                boy.setBlockMovement(false);
                SocketManager.GAME_SEND_MESSAGE_TO_MAP(map, girl.getName() + " est déjà marier !", Config.INSTANCE.getColorMessage());
                return;
            }
            SocketManager.GAME_SEND_cMK_PACKET_TO_MAP(map, "", -1, "Prêtre", asked.getName() + " acceptez-vous d'épouser " + (asked.getSexe() == 1 ? girl : boy).getName() + " ?");
            SocketManager.GAME_SEND_WEDDING(map, 617, (boy == asked ? boy.getId() : girl.getId()), (boy == asked ? girl.getId() : boy.getId()), -1);
        }
    }


    public void wedding(Player boy, Player girl, int isOK) {
        if (isOK > 0) {
            SocketManager.GAME_SEND_cMK_PACKET_TO_MAP(boy.getCurMap(), "", -1, "Prêtre", "Je déclare "
                    + boy.getName() + " et " + girl.getName() + " unis par les liens sacrés du mariage.");
            boy.setWife(girl.getId());
            girl.setWife(boy.getId());
        } else {
            SocketManager.GAME_SEND_Im_PACKET_TO_MAP(boy.getCurMap(), "048;" + boy.getName() + "~" + girl.getName());
        }
        boy.setisOK(0);
        boy.setBlockMovement(false);
        girl.setisOK(0);
        girl.setBlockMovement(false);
    }

    public Animation getAnimation(int AnimationId) {
        return Animations.get(AnimationId);
    }

    public void addAnimation(Animation animation) {
        Animations.put(animation.getId(), animation);
    }

    public void addHouse(House house) {
        Houses.put(house.getId(), house);
    }

    public House getHouse(int id) {
        return Houses.get(id);
    }

    public void addCollector(Collector Collector) {
        collectors.put(Collector.getId(), Collector);
    }

    public Collector getCollector(int CollectorID) {
        return collectors.get(CollectorID);
    }

    public void addTrunk(Trunk trunk) {
        Trunks.put(trunk.getId(), trunk);
    }

    public Trunk getTrunk(int id) {
        return Trunks.get(id);
    }

    public void addMountPark(MountPark mp) {
        MountPark.put(mp.getMap().getId(), mp);
    }

    public Map<Short, MountPark> getMountPark() {
        return MountPark;
    }

    public String parseMPtoGuild(int GuildID) {
        Guild G = getGuild(GuildID);
        byte enclosMax = (byte) Math.floor(G.getLvl() / 10);
        StringBuilder packet = new StringBuilder();
        packet.append(enclosMax);

        for (Entry<Short, MountPark> mp : MountPark.entrySet()) {
            if (mp.getValue().getGuild() != null
                    && mp.getValue().getGuild().getId() == GuildID) {
                packet.append("|").append(mp.getValue().getMap().getId()).append(";").append(mp.getValue().getSize()).append(";").append(mp.getValue().getMaxObject());// Nombre d'objets pour le dernier
                if (mp.getValue().getListOfRaising().size() > 0) {
                    packet.append(";");
                    boolean primero = false;
                    for (Integer id : mp.getValue().getListOfRaising()) {
                        Mount dd = getMountById(id);
                        if (dd != null) {
                            if (primero)
                                packet.append(",");
                            packet.append(dd.getColor()).append(",").append(dd.getName()).append(",");
                            if (getPlayer(dd.getOwner()) == null)
                                packet.append("Sans maitre");
                            else
                                packet.append(getPlayer(dd.getOwner()).getName());
                            primero = true;
                        }
                    }
                }
            }
        }
        return packet.toString();
    }

    public int totalMPGuild(int GuildID) {
        int i = 0;
        for (Entry<Short, MountPark> mp : MountPark.entrySet())
            if (mp.getValue().getGuild() != null && mp.getValue().getGuild().getId() == GuildID)
                i++;
        return i;
    }

    public void clearInactiveGuilds() {
        int i = 0;
        LocalDateTime now = LocalDateTime.now();
        LocalDate firstDate = now.toLocalDate();
        LocalDate UnUsedDate = firstDate.plusMonths(-Constant.AUTO_CLEAN_MONTH);
        Map<Integer, Guild> Test = Guildes;
        ArrayList<Integer> ids = new ArrayList<>();

        for (Entry<Integer, Guild> guild : Test.entrySet()){
            Boolean isInactifGuild = true;
            Boolean hasMeneur = false;
            List<Player> Members = guild.getValue().getPlayers();
            if(Members.size() != 0){
                for (Player player : Members) {
                    String dateactuelle = guild.getValue().getMember(player.getId()).getLastCo();
                    String[] table = dateactuelle.split("~");
                    LocalDate lastConnection =LocalDate.parse(table[0]+"-"+table[1]+"-"+table[2]);
                    GuildMember test = player.getGuildMember();
                    if(test != null && test.getRank() == 1){
                        if(!hasMeneur)
                            hasMeneur = true;
                    }

                    if(test.getRank() != 1 && test.getRights() == 1){
                        test.setAllRights(test.getRank(), (byte) test.getXpGive(), 29694, test.getPlayer());//1 => Meneur (Tous droits);
                        System.out.println("On reset les droits de "+test.getPlayer().getName()+" car droit 1 alors que pas meneur");
                    }


                    if(lastConnection.isAfter(UnUsedDate) ){
                        isInactifGuild = false;
                        //break ;
                    }

                    if(!isInactifGuild && hasMeneur)
                        break;
                }
            }

            if(isInactifGuild){
                System.out.println("La guilde " + guild.getValue().getName() +" est inactive, on supprime");
                sendWebhookInformationsServeur("Suppression de la guilde **" + guild.getValue().getName() +"** car vide");
                ids.add(guild.getValue().getId());
            }

            if(!hasMeneur && !isInactifGuild){

                GuildMember higherRank = null;
                for (GuildMember gmember : guild.getValue().getMembers()) {
                    if(higherRank == null)
                        higherRank = gmember;

                    if((gmember.getRank() < higherRank.getRank()) && gmember.getRank() != 0 ){
                        higherRank=gmember;
                    }
                }

                if(higherRank != null) {
                    sendWebhookInformationsServeur("Nouveau meneur '**" + higherRank.getPlayer().getName() + "**' pour la guilde **"+  higherRank.getGuild().getName()+ "**");
                    higherRank.setAllRights(1, (byte) 0, 1, higherRank.getPlayer());//1 => Meneur (Tous droits);
                }

            }
        }

        for (Integer id : ids) {
            World.world.removeGuild(id);
        }


    }

    public void addChallenge(String chal) {
        if (!Challenges.toString().isEmpty())
            Challenges.append(";");
        Challenges.append(chal);
    }

    public synchronized void addPrisme(Prism Prisme) {
        Prismes.put(Prisme.getId(), Prisme);
    }

    public Classe getClasse(int id)
    {
        return Classes.get(id);
    }
    public Prism getPrisme(int id) {
        return Prismes.get(id);
    }

    public void removePrisme(int id) {
        Prismes.remove(id);
    }

    public Collection<Prism> AllPrisme() {
        if (Prismes.size() > 0)
            return Prismes.values();
        return null;
    }

    public String PrismesGeoposition(int alignement) {
        String str = "";
        boolean isfirst = true;
        int subareas = 0;

        // Count
        int demonsubArea = 0;
        int angesubArea = 0;

        int demonArea = 0;
        int angeArea = 0;
        int totareasConquest = 0;

        int neutralsubArea = 0;

        for (SubArea subarea : subAreas.values()) {
            if (!subarea.getConquistable())
                continue;
            if(subarea.getAlignement() == 1) {
                angesubArea++;
            }
            else if(subarea.getAlignement() == 2){
                demonsubArea++;
            }
            else {
                neutralsubArea++;
            }
        }

        for (SubArea subarea : subAreas.values()) {
            if (!subarea.getConquistable())
                continue;

            if (!isfirst)
                str += ";";

            str += subarea.getId()
                    + ","
                    + (subarea.getAlignement() == 0 ? -1 : subarea.getAlignement())
                    + ",0,";
            if (getPrisme(subarea.getPrismId()) == null)
                str += 0 + ",1";
            else
                str += (subarea.getPrismId() == 0 ? 0 : getPrisme(subarea.getPrismId()).getMap())
                        + ",1";
            isfirst = false;
            subareas++;
        }


        for (Area area : areas.values()) {
            if(!area.canbeCapturable)
                continue;
            totareasConquest++;
            if (area.getAlignement() == 1)
                angeArea++;
            else if (area.getAlignement() == 2)
                demonArea++;

        }

        if (alignement == 1)
            str += "|" + angeArea;
        else if (alignement == 2)
            str += "|" + demonArea;


        str += "|" + totareasConquest + "|";
        isfirst = true;
        for (Area area : areas.values()) {
            if(!area.canbeCapturable)
                continue;

            if (area.getAlignement() == 0)
                continue;
            if (!isfirst)
                str += ";";
            str += area.getId() + "," + area.getAlignement() + ",1,"
                    + (area.getPrismId() == 0 ? 0 : 1);
            isfirst = false;
        }

        if (alignement == 1) {
            str = angesubArea + "|" + subareas + "|"
                    + (subareas - (angesubArea + demonsubArea)) + "|"
                    + str;
        }
        else if (alignement == 2) {
            str = demonsubArea + "|" + subareas + "|"
                    + (subareas - (angesubArea + demonsubArea)) + "|"
                    + str;
        }
        return str;
    }

    public void showPrismes(Player perso) {
        for (SubArea subarea : subAreas.values()) {
            if (subarea.getAlignement() == 0)
                continue;
            SocketManager.GAME_SEND_am_ALIGN_PACKET_TO_SUBAREA(perso, subarea.getId()
                    + "|" + subarea.getAlignement() + "|1");
        }
    }

    public synchronized int getNextIDPrisme() {
        int max = -102;
        for (int a : Prismes.keySet())
            if (a < max)
                max = a;
        return max - 3;
    }

    public House getHouseWithInMap(Short mapID) {
        for (House casa : Houses.values()) {
            if (casa.getHouseMaps().contains(mapID)) {
                return casa;
            }
        }
        return null;
    }

    public void addPets(Pet pets) {
        Pets.put(pets.getTemplateId(), pets);
    }

    public Pet getPets(int Tid) {
        return Pets.get(Tid);
    }

    public Collection<Pet> getPets() {
        return Pets.values();
    }

    public void addPetsEntry(PetEntry pets) {
        PetsEntry.put(pets.getObjectId(), pets);
    }

    public PetEntry getPetsEntry(Long guid) {
        return PetsEntry.get(guid);
    }

    public PetEntry removePetsEntry(Long guid) {
        return PetsEntry.remove(guid);
    }

    public void removePets(Long guid) {
        PetEntry pet = World.world.getPetsEntry(guid);
        if(pet != null) {
            World.world.removePetsEntry(guid);
            Database.getStatics().getPetData().delete(guid);
        }
    }


    public String getChallengeFromConditions(boolean sevEnn,
                                                    boolean sevAll, boolean bothSex, boolean EvenEnn, boolean MoreEnn,
                                                    boolean hasCaw, boolean hasChaf, boolean hasRoul, boolean hasArak,
                                                    int isBoss, boolean ecartLvlPlayer, boolean hasArround,
                                                    boolean hasDisciple, boolean isSolo) {
        StringBuilder toReturn = new StringBuilder();
        boolean isFirst = true, isGood = false;
        int cond;

        for (String chal : Challenges.toString().split(";")) {
            if (!isFirst && isGood)
                toReturn.append(";");
            isGood = true;
            int id = Integer.parseInt(chal.split(",")[0]);
            cond = Integer.parseInt(chal.split(",")[4]);
            //Necessite plusieurs ennemis
            if (((cond & 1) == 1) && !sevEnn)
                isGood = false;
            //Necessite plusieurs allies
            if ((((cond >> 1) & 1) == 1) && !sevAll)
                isGood = false;
            //Necessite les deux sexes
            if ((((cond >> 2) & 1) == 1) && !bothSex)
                isGood = false;
            //Necessite un nombre pair d'ennemis
            if ((((cond >> 3) & 1) == 1) && !EvenEnn)
                isGood = false;
            //Necessite plus d'ennemis que d'allies
            if ((((cond >> 4) & 1) == 1) && !MoreEnn)
                isGood = false;
            //Jardinier
            if (!hasCaw && (id == 7))
                isGood = false;
            //Fossoyeur
            if (!hasChaf && (id == 12))
                isGood = false;
            //Casino Royal
            if (!hasRoul && (id == 14))
                isGood = false;
            //Araknophile
            if (!hasArak && (id == 15))
                isGood = false;
            //Les mules d'abord
            if (!ecartLvlPlayer && (id == 48))
                isGood = false;
            //Contre un boss de donjon
            if (isBoss != -1 && id == 5)
                isGood = false;
            //Hardi
            if (!hasArround && id == 36)
                isGood = false;
            //Mains propre
            //TODO Debug ce chall
            if (/*!hasDisciple && */id == 19)
                isGood = false;

            switch (id) {
                case 47:
                case 46:
                case 45:
                case 44:
                    if (isSolo)
                        isGood = false;
                    break;
            }

            switch (isBoss) {
                case 1045://Kimbo
                    switch (id) {
                        case 37:
                        case 8:
                        case 1:
                        case 2:
                            isGood = false;
                            break;
                    }
                    break;
                case 1072://Tynril
                case 1085://Tynril
                case 1086://Tynril
                case 1087://Tynril
                    switch (id) {
                        case 36:
                        case 20:
                            isGood = false;
                            break;
                    }
                    break;
                case 1071://Rasboul Majeur
                    switch (id) {
                        case 9:
                        case 22:
                        case 17:
                        case 47:
                            isGood = false;
                            break;
                    }
                    break;
                case 780://Skeunk
                    switch (id) {
                        case 35:
                        case 25:
                        case 4:
                        case 32:
                        case 3:
                        case 31:
                        case 34:
                            isGood = false;
                            break;
                    }
                    break;
                case 113://DC
                    switch (id) {
                        case 12:
                        case 15:
                        case 7:
                        case 41:
                            isGood = false;
                            break;
                    }
                    break;
                case 612://Maitre pandore
                    switch (id) {
                        case 20:
                        case 37:
                            isGood = false;
                            break;
                    }
                    break;
                case 478://Bworker
                case 568://Tanukoui san
                case 940://Rat blanc
                    switch (id) {
                        case 20:
                            isGood = false;
                            break;
                    }
                    break;
                case 1188://Blop multi
                    switch (id) {
                        case 20:
                        case 46:
                        case 44:
                            isGood = false;
                            break;
                    }
                    break;

                case 865://Grozila
                case 866://Grasmera
                    switch (id) {
                        case 31:
                        case 32:
                            isGood = false;
                            break;
                    }
                    break;

            }
            if (isGood)
                toReturn.append(chal);
            isFirst = false;
        }
        return toReturn.toString();
    }

    public void verifyClone(Player p) {
        if (p.getCurCell() != null && p.getFight() == null) {
            if (p.getCurCell().getPlayers().contains(p)) {
                p.getCurCell().removePlayer(p);
                Database.getStatics().getPlayerData().update(p);
            }
        }
        if (p.isOnline())
            Database.getStatics().getPlayerData().update(p);
    }

    public ArrayList<String> getRandomChallenge(int nombreChal,
                                                       String challenges) {
        String MovingChals = ";1;2;8;36;37;39;40;";// Challenges de d�placements incompatibles
        boolean hasMovingChal = false;
        String TargetChals = ";3;4;10;25;31;32;34;35;38;42;";// ceux qui ciblent
        boolean hasTargetChal = false;
        String SpellChals = ";5;6;9;11;19;20;24;41;";// ceux qui obligent � caster sp�cialement
        boolean hasSpellChal = false;
        String KillerChals = ";28;29;30;44;45;46;48;";// ceux qui disent qui doit tuer
        boolean hasKillerChal = false;
        String HealChals = ";18;43;";// ceux qui emp�chent de soigner
        boolean hasHealChal = false;

        int compteur = 0, i;
        ArrayList<String> toReturn = new ArrayList<>();
        String chal;
        //toReturn.add("31,20,20,5");
        while (compteur < 100 && toReturn.size() < nombreChal) {
            compteur++;
            i = Formulas.getRandomValue(1, challenges.split(";").length);
            chal = challenges.split(";")[i - 1];// challenge au hasard dans la liste

            if (!toReturn.contains(chal))// si le challenge n'y etait pas encore
            {
                if (MovingChals.contains(";" + chal.split(",")[0] + ";"))// s'il appartient a une liste
                    if (!hasMovingChal)// et qu'aucun de la liste n'a ete choisi deja
                    {
                        hasMovingChal = true;
                        toReturn.add(chal);
                        continue;
                    } else
                        continue;
                if (TargetChals.contains(";" + chal.split(",")[0] + ";"))
                    if (!hasTargetChal) {
                        hasTargetChal = true;
                        toReturn.add(chal);
                        continue;
                    } else
                        continue;
                if (SpellChals.contains(";" + chal.split(",")[0] + ";"))
                    if (!hasSpellChal) {
                        hasSpellChal = true;
                        toReturn.add(chal);
                        continue;
                    } else
                        continue;
                if (KillerChals.contains(";" + chal.split(",")[0] + ";"))
                    if (!hasKillerChal) {
                        hasKillerChal = true;
                        toReturn.add(chal);
                        continue;
                    } else
                        continue;
                if (HealChals.contains(";" + chal.split(",")[0] + ";"))
                    if (!hasHealChal) {
                        hasHealChal = true;
                        toReturn.add(chal);
                        continue;
                    } else
                        continue;
                toReturn.add(chal);
            }
            compteur++;
        }
        return toReturn;
    }

    public Collector getCollectorByMap(int id) {

        for (Entry<Integer, Collector> Collector : getCollectors().entrySet()) {
            GameMap map = getMap(Collector.getValue().getMap());
            if (map.getId() == id) {
                return Collector.getValue();
            }
        }
        return null;
    }

    public void reloadPlayerGroup() {
        GameServer.getClients().stream()
                .filter(client -> client != null && client.getPlayer() != null)
                .forEach(client -> Database.getStatics().getPlayerData().reloadGroup(client.getPlayer()));
    }

    public void reloadDrops() {
        try{
            Database.getDynamics().getDropData().reload();
        }
       catch (Exception e){
            //System.out.println("IciBase" + e);
       }
    }

    public void loadDropsBlackItem() {
        //Database.getDynamics().getDropData().reload();
        ArrayList<Double> percents = new ArrayList<>();
        percents.add(0.1);
        percents.add(0.12);
        percents.add(0.14);
        percents.add(0.16);
        percents.add(0.18);

        for(int i =10 ; i<=180 ; i+=10){
            int finalI = i;
            ArrayList<ObjectTemplate> arrayBlackItem = new ArrayList<>();
            ArrayList<Monster> arrayMonstre = new ArrayList<>();
            try {
            getObjectsTemplates().values().stream().filter(objectTemplate -> objectTemplate != null && objectTemplate.getPanoId() == -1 && !objectTemplate.getStrTemplate().contains("32c#") && (!objectTemplate.getStrTemplate().isEmpty()) && !objectTemplate.getName().contains("Polyk")
                    && (objectTemplate.getLevel() >= finalI && objectTemplate.getLevel()<= finalI+9) && ArrayUtils.contains( Constant.ITEM_TYPE_OBJ_BLACK, objectTemplate.getType() )  && !isDropable(objectTemplate.getId()) && !ArrayUtils.contains(Constant.ITEMS_EXCLUDE_DROP,objectTemplate.getId()) ).forEach(arrayBlackItem::add);
            }
            catch (Exception e){
                //System.out.println("ici item" + e);
            }

            try {
               getMonstres().stream().filter(monster -> monster != null && !(ArrayUtils.contains(Constant.FILTER_MONSTRE_SPE, monster.getType())) && !(ArrayUtils.contains(Constant.EXCLUDE_MOBID_TODROP, monster.getId())) && !(monster.getGrade(1).getSpells().keySet().isEmpty())
                       && (getLvlMoyenMonstre(monster) >= finalI && getLvlMoyenMonstre(monster) <= finalI + 9)).forEach(arrayMonstre::add);
           }
           catch (Exception e){
                //System.out.println("ici monstre" + e);
           }

            if(arrayMonstre.size() == 0){
                int k=1;
                do {
                    int finalK = k;
                    getMonstres().stream().filter(monster -> monster != null && !(ArrayUtils.contains(Constant.FILTER_MONSTRE_SPE, monster.getType()))  && !(ArrayUtils.contains(Constant.EXCLUDE_MOBID_TODROP, monster.getId())) && !(monster.getGrade(1).getSpells().keySet().isEmpty())
                            && (getLvlMoyenMonstre(monster) >= finalI && getLvlMoyenMonstre(monster) <= finalI + 9+ finalK)).forEach(arrayMonstre::add);
                    k++;
                }
                while(arrayMonstre.size()<1);
            }
            //int rapport =  (int)Math.ceil((float)arrayBlackItem.size()/(float) arrayMonstre.size());
            int l=0;
            boolean hasreset=false;
            for(ObjectTemplate item : arrayBlackItem){
                Monster Mob = arrayMonstre.get(l);
                int action = -1;
                String condition = "";
                if(item.getLevel() > 175 && item.getLevel() <=180){
                     action = 10;
                     condition = "1";
                }
                else if(item.getLevel() > 180){
                    action = 10;
                    condition = "2";
                }
                World.Drop drop = new World.Drop(item.getId(), percents, 0, action, -1, condition,false,true,false);
                Mob.addDrop(drop);
                l++;
                if(l>=arrayMonstre.size()){
                    l=0;
                    hasreset =true;
                }
                //System.out.println("");
            }

            if(l<arrayMonstre.size() && !hasreset){
                int p=0;
                for(int x=l ;x < arrayMonstre.size();x++ ){
                    Monster Mob = arrayMonstre.get(x);
                    ObjectTemplate item = arrayBlackItem.get(p);

                    int action = -1;
                    String condition = "";
                    if(item.getLevel() > 175 && item.getLevel() <=180){
                        action = 10;
                        condition = "1";
                    }
                    else if(item.getLevel() > 180){
                        action = 10;
                        condition = "2";
                    }
                    World.Drop drop = new World.Drop(item.getId(), percents, 0, action, -1, condition,false,true,false);
                    Mob.addDrop(drop);

                    p++;
                    if(p>=arrayBlackItem.size()){
                        p=0;
                    }
                }
            }



        }
    }

    public boolean isDropable(int id) {
        boolean isdropable=false;
        getMonstres();
        for(Monster monstre : getMonstres()){
            ArrayList<Drop> drops = monstre.getDrops();
            for(Drop drop : drops){
                if(id == drop.getObjectId())
                    return true;
            }
        }

        return isdropable;
    }

    public int getLvlMoyenMonstre(Monster monstre){
        int levelmoyen = monstre.getGrade(3).getLevel();
        if(levelmoyen<5){
            levelmoyen=5;
        }
        if(levelmoyen>180){
            levelmoyen=180;
        }
        return levelmoyen;
    }

    public void reloadEndFightActions() {
        Database.getDynamics().getEndFightActionData().reload();
    }

    public void reloadNpcs() {
        Database.getDynamics().getNpcTemplateData().reload();
        questions.clear();
        Database.getDynamics().getNpcQuestionData().load();
        answers.clear();
        Database.getDynamics().getNpcAnswerData().load();
    }

    public void reloadHouses() {
        Houses.clear();
        Database.getStatics().getHouseData().load();
        Database.getDynamics().getHouseData().load();
    }

    public void reloadTrunks() {
        Trunks.clear();
        Database.getStatics().getTrunkData().load();
        Database.getDynamics().getTrunkData().load();
    }

    public void reloadTitres() {
        titres.clear();
        Database.getStatics().getTitleData().load();
    }

    public void reloadMaps() {
        Database.getDynamics().getMapData().reload();
    }

    public void reloadMountParks(int i) {
        Database.getStatics().getMountParkData().reload(i);
        Database.getDynamics().getMountParkData().reload(i);
    }

    public void reloadMonsters() {
        Database.getDynamics().getMonsterData().reload();
    }

    public void reloadQuests() {
        Database.getDynamics().getQuestData().load();
    }

    public void reloadObjectsActions() {
        Database.getDynamics().getObjectActionData().reload();
    }

    public void reloadSpells() {
        Database.getDynamics().getSpellData().load();
    }

    public void reloadItems() {
        Database.getDynamics().getObjectTemplateData().load();
    }

    public void addSeller(Player player) {
        if (player.getStoreItems().isEmpty())
            return;

        short map = player.getCurMap().getId();

        if (Seller.get(map) == null) {
            ArrayList<Integer> players = new ArrayList<>();
            players.add(player.getId());
            Seller.put(map, players);
        } else {
            ArrayList<Integer> players = new ArrayList<>();
            players.add(player.getId());
            players.addAll(Seller.get(map));
            Seller.remove(map);
            Seller.put(map, players);
        }
    }

    public Collection<Integer> getSeller(short map) {
        return Seller.get(map);
    }

    public void removeSeller(int player, short map) {
        if(getSeller(map) != null)
            Seller.get(map).remove(player);
    }

    /*public double getPwrPerEffet(int effect) {
        double r = 0.0;
        switch (effect) {
            case EffectConstant.STATS_ADD_PA:
                r = 100.0;
                break;
            case EffectConstant.STATS_ADD_PM2:
                r = 90.0;
                break;
            case EffectConstant.STATS_ADD_VIE:
                r = 0.25;
                break;
            case EffectConstant.STATS_MULTIPLY_DOMMAGE:
                r = 100.0;
                break;
            case EffectConstant.STATS_ADD_CC:
                r = 30.0;
                break;
            case Constant.STATS_ADD_PO:
                r = 51.0;
                break;
            case Constant.STATS_ADD_FORC:
                r = 1.0;
                break;
            case Constant.STATS_ADD_AGIL:
                r = 1.0;
                break;
            case Constant.STATS_ADD_PA2:
                r = 100.0;
                break;
            case Constant.STATS_ADD_DOMA2:
            case Constant.STATS_ADD_DOMA:
                r = 20.0;
                break;
            case Constant.STATS_ADD_EC:
                r = 1.0;
                break;
            case Constant.STATS_ADD_CHAN:
                r = 1.0;
                break;
            case Constant.STATS_ADD_SAGE:
                r = 3.0;
                break;
            case Constant.STATS_ADD_VITA:
                r = 0.25;
                break;
            case Constant.STATS_ADD_INTE:
                r = 1.0;
                break;
            case Constant.STATS_ADD_PM:
                r = 90.0;
                break;
            case Constant.STATS_ADD_PERDOM:
                r = 2.0;
                break;
            case Constant.STATS_ADD_PDOM:
                r = 2.0;
                break;
            case Constant.STATS_ADD_PODS:
                r = 0.25;
                break;
            case Constant.STATS_ADD_AFLEE:
                r = 1.0;
                break;
            case Constant.STATS_ADD_MFLEE:
                r = 1.0;
                break;
            case Constant.STATS_ADD_INIT:
                r = 0.1;
                break;
            case Constant.STATS_ADD_PROS:
                r = 3.0;
                break;
            case Constant.STATS_ADD_SOIN:
                r = 20.0;
                break;
            case Constant.STATS_CREATURE:
                r = 30.0;
                break;
            case Constant.STATS_ADD_RP_TER:
                r = 6.0;
                break;
            case Constant.STATS_ADD_RP_EAU:
                r = 6.0;
                break;
            case Constant.STATS_ADD_RP_AIR:
                r = 6.0;
                break;
            case Constant.STATS_ADD_RP_FEU:
                r = 6.0;
                break;
            case Constant.STATS_ADD_RP_NEU:
                r = 6.0;
                break;
            case Constant.STATS_TRAPDOM:
                r = 15.0;
                break;
            case Constant.STATS_TRAPPER:
                r = 2.0;
                break;
            case Constant.STATS_ADD_R_FEU:
                r = 2.0;
                break;
            case Constant.STATS_ADD_R_NEU:
                r = 2.0;
                break;
            case Constant.STATS_ADD_R_TER:
                r = 2.0;
                break;
            case Constant.STATS_ADD_R_EAU:
                r = 2.0;
                break;
            case Constant.STATS_ADD_R_AIR:
                r = 2.0;
                break;
            case Constant.STATS_ADD_RP_PVP_TER:
                r = 6.0;
                break;
            case Constant.STATS_ADD_RP_PVP_EAU:
                r = 6.0;
                break;
            case Constant.STATS_ADD_RP_PVP_AIR:
                r = 6.0;
                break;
            case Constant.STATS_ADD_RP_PVP_FEU:
                r = 6.0;
                break;
            case Constant.STATS_ADD_RP_PVP_NEU:
                r = 6.0;
                break;
            case Constant.STATS_ADD_R_PVP_TER:
                r = 2.0;
                break;
            case Constant.STATS_ADD_R_PVP_EAU:
                r = 2.0;
                break;
            case Constant.STATS_ADD_R_PVP_AIR:
                r = 2.0;
                break;
            case Constant.STATS_ADD_R_PVP_FEU:
                r = 2.0;
                break;
            case Constant.STATS_ADD_R_PVP_NEU:
                r = 2.0;
                break;
        }
        return r;
    }

    public double getOverPerEffet(int effect) {
        double r = 0.0;
        switch (effect) {
            case Constant.STATS_ADD_PA:
                r = 0.0;
                break;
            case Constant.STATS_ADD_PM2:
                r = 404.0;
                break;
            case Constant.STATS_ADD_VIE:
                r = 404.0;
                break;
            case Constant.STATS_MULTIPLY_DOMMAGE:
                r = 0.0;
                break;
            case Constant.STATS_ADD_CC:
                r = 3.0;
                break;
            case Constant.STATS_ADD_PO:
                r = 0.0;
                break;
            case Constant.STATS_ADD_FORC:
                r = 101.0;
                break;
            case Constant.STATS_ADD_AGIL:
                r = 101.0;
                break;
            case Constant.STATS_ADD_PA2:
                r = 0.0;
                break;
            case Constant.STATS_ADD_DOMA2:
            case Constant.STATS_ADD_DOMA:
                r = 5.0;
                break;
            case Constant.STATS_ADD_EC:
                r = 0.0;
                break;
            case Constant.STATS_ADD_CHAN:
                r = 101.0;
                break;
            case Constant.STATS_ADD_SAGE:
                r = 33.0;
                break;
            case Constant.STATS_ADD_VITA:
                r = 404.0;
                break;
            case Constant.STATS_ADD_INTE:
                r = 101.0;
                break;
            case Constant.STATS_ADD_PM:
                r = 0.0;
                break;
            case Constant.STATS_ADD_PERDOM:
                r = 50.0;
                break;
            case Constant.STATS_ADD_PDOM:
                r = 50.0;
                break;
            case Constant.STATS_ADD_PODS:
                r = 404.0;
                break;
            case Constant.STATS_ADD_AFLEE:
                r = 0.0;
                break;
            case Constant.STATS_ADD_MFLEE:
                r = 0.0;
                break;
            case Constant.STATS_ADD_INIT:
                r = 1010.0;
                break;
            case Constant.STATS_ADD_PROS:
                r = 33.0;
                break;
            case Constant.STATS_ADD_SOIN:
                r = 5.0;
                break;
            case Constant.STATS_CREATURE:
                r = 3.0;
                break;
            case Constant.STATS_ADD_RP_TER:
                r = 16.0;
                break;
            case Constant.STATS_ADD_RP_EAU:
                r = 16.0;
                break;
            case Constant.STATS_ADD_RP_AIR:
                r = 16.0;
                break;
            case Constant.STATS_ADD_RP_FEU:
                r = 16.0;
                break;
            case Constant.STATS_ADD_RP_NEU:
                r = 16.0;
                break;
            case Constant.STATS_TRAPDOM:
                r = 6.0;
                break;
            case Constant.STATS_TRAPPER:
                r = 50.0;
                break;
            case Constant.STATS_ADD_R_FEU:
                r = 50.0;
                break;
            case Constant.STATS_ADD_R_NEU:
                r = 50.0;
                break;
            case Constant.STATS_ADD_R_TER:
                r = 50.0;
                break;
            case Constant.STATS_ADD_R_EAU:
                r = 50.0;
                break;
            case Constant.STATS_ADD_R_AIR:
                r = 50.0;
                break;
            case Constant.STATS_ADD_RP_PVP_TER:
                r = 16.0;
                break;
            case Constant.STATS_ADD_RP_PVP_EAU:
                r = 16.0;
                break;
            case Constant.STATS_ADD_RP_PVP_AIR:
                r = 16.0;
                break;
            case Constant.STATS_ADD_RP_PVP_FEU:
                r = 16.0;
                break;
            case Constant.STATS_ADD_RP_PVP_NEU:
                r = 16.0;
                break;
            case Constant.STATS_ADD_R_PVP_TER:
                r = 50.0;
                break;
            case Constant.STATS_ADD_R_PVP_EAU:
                r = 50.0;
                break;
            case Constant.STATS_ADD_R_PVP_AIR:
                r = 50.0;
                break;
            case Constant.STATS_ADD_R_PVP_FEU:
                r = 50.0;
                break;
            case Constant.STATS_ADD_R_PVP_NEU:
                r = 50.0;
                break;
        }
        return r;
    }*/

    public double getTauxObtentionIntermediaire(double bonus, boolean b1, boolean b2) {
        double taux = bonus;
        // 100.0 + 2*(30.0 + 2*10.0) => true true
        // 30.0 + 2*(10.0 + 2*3.0) => true false
        // 10.0 + 2*(3.0 + 2*1.0) => true true
        if (b1) {
            if (bonus == 100.0)
                taux += 2.0 * getTauxObtentionIntermediaire(30.0, true, b2);
            if (bonus == 30.0)
                taux += 2.0 * getTauxObtentionIntermediaire(10.0, (!b2), b2); // Si b2 est false alors on calculera 2*3.0 dans 10.0
            if (bonus == 10.0)
                taux += 2.0 * getTauxObtentionIntermediaire(3.0, (b2), b2); // Si b2 est true alors on calculera apr�s
            else if (bonus == 3.0)
                taux += 2.0 * getTauxObtentionIntermediaire(1.0, false, b2);
        }

        return taux;
    }

    public int getMetierByMaging(int idMaging) {
        int mId = -1;
        switch (idMaging) {
            case 43: // FM Dagues
                mId = 17;
                break;
            case 44: // FM Ep�es
                mId = 11;
                break;
            case 45: // FM Marteaux
                mId = 14;
                break;
            case 46: // FM Pelles
                mId = 20;
                break;
            case 47: // FM Haches
                mId = 31;
                break;
            case 48: // FM Arcs
                mId = 13;
                break;
            case 49: // FM Baguettes
                mId = 19;
                break;
            case 50: // FM B�tons
                mId = 18;
                break;
            case 62: // Cordo
                mId = 15;
                break;
            case 63: // Jaillo
                mId = 16;
                break;
            case 64: // Costu
                mId = 27;
                break;
        }
        return mId;
    }

    public int getTempleByClasse(int classe) {
        int temple = -1;
        switch (classe) {
            case Constant.CLASS_FECA: // f�ca
                temple = 1554;
                break;
            case Constant.CLASS_OSAMODAS: // osa
                temple = 1546;
                break;
            case Constant.CLASS_ENUTROF: // �nu
                temple = 1470;
                break;
            case Constant.CLASS_SRAM: // sram
                temple = 6926;
                break;
            case Constant.CLASS_XELOR: // xelor
                temple = 1469;
                break;
            case Constant.CLASS_ECAFLIP: // �ca
                temple = 1544;
                break;
            case Constant.CLASS_ENIRIPSA: // �ni
                temple = 6928;
                break;
            case Constant.CLASS_IOP: // iop
                temple = 1549;
                break;
            case Constant.CLASS_CRA: // cra
                temple = 1558;
                break;
            case Constant.CLASS_SADIDA: // sadi
                temple = 1466;
                break;
            case Constant.CLASS_SACRIEUR: // sacri
                temple = 6949;
                break;
            case Constant.CLASS_PANDAWA: // panda
                temple = 8490;
                break;
        }
        return temple;
    }

    public static void sendWebhookMessage(String webhookUrl, String message, Player player) {
        try {
            if(Config.INSTANCE.getDISCORD_WH()) {
                if(!webhookUrl.isEmpty()) {
                    URL url = new URL(webhookUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);
                    connection.addRequestProperty("User-Agent", "Aegnor Webhook 1.0");

                    String jsonMessage = "{\"content\":\"" + "Compte '" + player.getAccount().getName() + "' **("+player.getAccount().getId()+")** - Joueur '" + player.getName() + "' **(" +player.getId()+ ")** : " +  message + "\"}";
                    try (OutputStream os = connection.getOutputStream()) {
                        byte[] input = jsonMessage.getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }

                    int responseCode = connection.getResponseCode();
                    if (responseCode != 204) {
                        if (Logging.USE_LOG) {
                            Logging.getInstance().write("WebHookFail", message + " | Webhook Response Code:" + responseCode);
                        }
                    }
                    connection.disconnect();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendWebhookInformations(String webhookUrl, String message, Player player) {
        try {
            if(Config.INSTANCE.getDISCORD_WH()) {
                if(!webhookUrl.isEmpty()) {
                    URL url = new URL(webhookUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);
                    connection.addRequestProperty("User-Agent", "Aegnor Webhook 1.0");

                    String jsonMessage = "{\"content\":\"" + "**" + player.getName() + "** : " +  message + "\"}";
                    try (OutputStream os = connection.getOutputStream()) {
                        byte[] input = jsonMessage.getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }

                    int responseCode = connection.getResponseCode();
                    if (responseCode != 204) {
                        if (Logging.USE_LOG) {
                            Logging.getInstance().write("WebHookFail", message + " | Webhook Response Code:" + responseCode);
                        }
                    }
                    connection.disconnect();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendWebhookInformationsServeur(String message) {
        try {
            if(Config.INSTANCE.getDISCORD_WH()) {
                if(!Config.INSTANCE.getDISCORD_CHANNEL_INFO().isEmpty()) {
                    URL url = new URL(Config.INSTANCE.getDISCORD_CHANNEL_INFO());
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);
                    connection.addRequestProperty("User-Agent", "Aegnor Webhook 1.0");

                    String jsonMessage = "{\"content\":\"" + "**" + Config.INSTANCE.getSERVER_NAME() + "** : " +  message + "\"}";
                    try (OutputStream os = connection.getOutputStream()) {
                        byte[] input = jsonMessage.getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }

                    int responseCode = connection.getResponseCode();
                    if (responseCode != 204) {
                        if (Logging.USE_LOG) {
                            Logging.getInstance().write("WebHookFail", message + " | Webhook Response Code:" + responseCode);
                        }
                    }
                    connection.disconnect();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessageToAll(String message) {
        TimerWaiter.addNext(() -> World.world.getOnlinePlayers().stream()
                        .filter(player -> player != null && player.getGameClient() != null && player.isOnline())
                        .forEach(player -> player.sendMessage(message)),
                10, TimeUnit.SECONDS);
    }

    public ArrayList<ObjectTemplate> getPotentialBlackItem(int level,int fightdifficulty) {
        ArrayList<ObjectTemplate> array = new ArrayList<>();
        int boostdiff = 0;
        switch (fightdifficulty){
            case 0 :
                break;
            case 1:
                boostdiff = 5;
                break;
            case 2 :
                boostdiff = 10;
                break;
            default:
                boostdiff = 0;
                break;
        }
        if(level>=175){
            level=175;
        }
        final int levelMin = (level - 5 <= 1 ? 2 : level - 5), levelMax = level + 5+boostdiff;
        getObjectsTemplates().values().stream().filter(objectTemplate -> objectTemplate != null && objectTemplate.getPanoId() == -1 && !objectTemplate.getStrTemplate().contains("32c#") && !objectTemplate.getName().contains("Polyk")
                && (levelMin <= objectTemplate.getLevel() && objectTemplate.getLevel() <= levelMax) && ArrayUtils.contains( Constant.ITEM_TYPE_OBJ_BLACK, objectTemplate.getType() )  ).forEach(array::add);
        return array;
    }

    public Drop getPotentialMount(int level,int fightdifficulty){
        int boostdiff = 1;
        switch (fightdifficulty){
            case 0 :
                break;
            case 1:
                boostdiff = 5;
                break;
            case 2 :
                boostdiff = 10;
                break;
            case 3 :
                boostdiff = 15;
                break;
            case 4 :
                boostdiff = 20;
                break;
            default:
                boostdiff = 1;
                break;
        }
        Drop runesfinal;
        if(1 <= level && level <= 100 ){
            runesfinal = new Drop(17197, 0.1*boostdiff, 0,false);
        }
        else if(100 < level && level <= 150 ){

            runesfinal = new Drop(17198, 0.075*boostdiff, 0,false);
        }
        else if(150 < level && level <= 180 ){

            runesfinal = new Drop(17199, 0.05*boostdiff, 0,false);
        }
        else if(180 < level){
            runesfinal = new Drop(17200, 0.025*boostdiff, 0,false);
        }
        else{
            runesfinal = new Drop(17197, 0.1*boostdiff, 0,false);
        }

        return runesfinal;
    }

    public Drop getPotentialRuneReini(int level, int fightdifficulty) {
        int boostdiff = 1;
        switch (fightdifficulty){
            case 0 :
                break;
            case 1:
                boostdiff = 5;
                break;
            case 2 :
                boostdiff = 10;
                break;
            case 3 :
                boostdiff = 15;
                break;
            case 4 :
                boostdiff = 20;
                break;
            default:
                boostdiff = 1;
                break;
        }
        Drop runesfinal;
        if(1 <= level && level <= 100 ){
            runesfinal = new Drop(17197, 0.1*boostdiff, 0,false);
        }
        else if(100 < level && level <= 150 ){

            runesfinal = new Drop(17198, 0.075*boostdiff, 0,false);
        }
        else if(150 < level && level <= 180 ){

            runesfinal = new Drop(17199, 0.05*boostdiff, 0,false);
        }
        else if(180 < level){
            runesfinal = new Drop(17200, 0.025*boostdiff, 0,false);
        }
        else{
            runesfinal = new Drop(17197, 0.1*boostdiff, 0,false);
        }

        return runesfinal;
    }

    public Player getPlayerPerName(String nombre) {
        Player p = null;
        for(Player player : players.values())
        {
            if(player.getName().equals(nombre))
            {
                p = player;
            }
        }
        return p;
    }

    public void addTitre(Titre titre) {
        titres.put(titre.getId(), titre);
    }

    public void addSets(QuickSets set) {
        QuickSets.put(set.getId(), set);
    }

    // POUR LA GESTION DES REBOOT
    public void reboot(int launch,int min){
        int time = 30, OffOn = 0;
        try {
            OffOn = launch;
            time = min;
        } catch (Exception e) {
            // ok
        }


        if (OffOn == 1 && isTimerStart() )// demande de demarer le reboot
        {
            //System.out.println("Reboot deja en cours");
        } else if (OffOn == 1 && !isTimerStart()) {
            if (time <= 5) {
                for(Player player : World.world.getOnlinePlayers()) {
                    player.sendServerMessage(Lang.get(player, 14));
                    player.send("M13");
                }
                Main.INSTANCE.setFightAsBlocked(true);
            }
            setTimer(createTimer(time));
            getTimer().start();
            setTimerStart(true);

            String timeMSG = "minutes";
            if (time <= 1)
                timeMSG = "minute";
            SocketManager.GAME_SEND_Im_PACKET_TO_ALL("115;" + time + " " + timeMSG);
        } else if (OffOn == 0 && isTimerStart() ) {
            getTimer().stop();
            setTimerStart(false);
            for(Player player : World.world.getOnlinePlayers()){
                player.sendServerMessage("La maintenance a été annulée, le serveur ne va plus redémarrer.");
                player.sendServerMessage(Lang.get(player, 15));
            }
            Main.INSTANCE.setFightAsBlocked(false);
        } else if (OffOn == 0 && !isTimerStart() ) {
            //System.out.println("Aucun reboot n'est lancé.");
        }
    }

    public boolean isTimerStart() {
        return timerStart;
    }

    public void setTimerStart(boolean timerStart) {
        this.timerStart = timerStart;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public Timer createTimer(final int timer) {
        sendWebhookInformationsServeur("Un reboot a été programmé dans "+timer+ " minutes.");
        ActionListener action = new ActionListener() {
            int time = timer;

            public void actionPerformed(ActionEvent event) {
                time = time - 1;
                if (time == 1)
                    SocketManager.GAME_SEND_Im_PACKET_TO_ALL("115;" + time + " minute");
                else
                    SocketManager.GAME_SEND_Im_PACKET_TO_ALL("115;" + time + " minutes");
                if (time <= 0) Main.INSTANCE.stop("Shutdown by WebSite or DiscordBot");
            }
        };
        return new Timer(60000, action);
    }

    public ArrayList<Monster.MobGrade> getMobgradeBetweenLvl(int min, int max){
        ArrayList<Monster> arrayMonstre = new ArrayList<>();
        ArrayList<Monster.MobGrade> arrayMobgrade = new ArrayList<>();
        getMonstres().stream().filter(monster -> monster != null && !(ArrayUtils.contains(Constant.FILTER_MONSTRE_SPE, monster.getType())) && !(monster.getGrade(1).getSpells().keySet().isEmpty()) && (monster.getAlign() == -1)
                && !(ArrayUtils.contains(Constant.BOSS_ID, monster.getId())) && !(ArrayUtils.contains(Constant.EXCEPTION_HOTOMANI_MONSTRES, monster.getId())) && (getLvlMax(monster) >= min && getLvlMax(monster) < max)).forEach(arrayMonstre::add);

        for(Monster mob : arrayMonstre){
            arrayMobgrade.add(mob.getGrade(5));
        }
        return arrayMobgrade;
    }

    public ArrayList<Monster.MobGrade> getMobgradeBetweenLvlGladia(int min, int max){
        ArrayList<Monster> arrayMonstre = new ArrayList<>();
        ArrayList<Monster.MobGrade> arrayMobgrade = new ArrayList<>();
        getMonstres().stream().filter(monster -> monster != null && !(org.apache.commons.lang.ArrayUtils.contains(Constant.FILTER_MONSTRE_SPE, monster.getType())) && !(monster.getGrade(1).getSpells().keySet().isEmpty()) && (monster.getAlign() == -1)
                && !(org.apache.commons.lang.ArrayUtils.contains(Constant.BOSS_ID, monster.getId())) && !(org.apache.commons.lang.ArrayUtils.contains(Constant.EXCEPTION_GLADIATROOL_MONSTRES, monster.getId())) && (getLvlMax(monster) >= min && getLvlMax(monster) < max)).forEach(arrayMonstre::add);

        for(Monster mob : arrayMonstre){
            arrayMobgrade.add(mob.getGrade(5));
        }
        return arrayMobgrade;
    }

    public ArrayList<Monster.MobGrade> getArchiMobgradeBetweenLvl(int min, int max){
        ArrayList<Monster> arrayMonstre = new ArrayList<>();
        ArrayList<Monster.MobGrade> arrayMobgrade = new ArrayList<>();
        getMonstres().stream().filter(monster -> monster != null && (ArrayUtils.contains(Constant.MONSTRE_TYPE_ARCHI, monster.getType())) && !(ArrayUtils.contains(Constant.EXCEPTION_HOTOMANI_ARCHI, monster.getId())) && !(monster.getGrade(1).getSpells().keySet().isEmpty())
                && (getLvlMax(monster) >= min && getLvlMax(monster) < max)).forEach(arrayMonstre::add);

        for(Monster mob : arrayMonstre){
            arrayMobgrade.add(mob.getGrade(5));
        }
        return arrayMobgrade;
    }

    public ArrayList<Monster.MobGrade> getArchiMobgradeBetweenLvlGladia(int min, int max){
        ArrayList<Monster> arrayMonstre = new ArrayList<>();
        ArrayList<Monster.MobGrade> arrayMobgrade = new ArrayList<>();
        getMonstres().stream().filter(monster -> monster != null && (org.apache.commons.lang.ArrayUtils.contains(Constant.MONSTRE_TYPE_ARCHI, monster.getType())) && !(org.apache.commons.lang.ArrayUtils.contains(Constant.EXCEPTION_GLADIATROOL_ARCHI, monster.getId())) && !(monster.getGrade(1).getSpells().keySet().isEmpty())
                && (getLvlMax(monster) >= min && getLvlMax(monster) < max)).forEach(arrayMonstre::add);

        for(Monster mob : arrayMonstre){
            arrayMobgrade.add(mob.getGrade(5));
        }
        return arrayMobgrade;
    }


    public ArrayList<Monster.MobGrade> getBossMobgradeBetweenLvl(int min, int max){
        ArrayList<Monster> arrayMonstre = new ArrayList<>();
        ArrayList<Monster.MobGrade> arrayMobgrade = new ArrayList<>();
        getMonstres().stream().filter(monster -> monster != null && (ArrayUtils.contains(Constant.BOSS_ID, monster.getId())) && !(ArrayUtils.contains(Constant.EXCEPTION_HOTOMANI_BOSS, monster.getId())) && !(monster.getGrade(1).getSpells().keySet().isEmpty()) && (monster.getAlign() == -1)
                && (getLvlMax(monster) >= min && getLvlMax(monster) < max)).forEach(arrayMonstre::add);

        for(Monster mob : arrayMonstre){
            arrayMobgrade.add(mob.getGrade(5));
        }
        return arrayMobgrade;
    }

    public ArrayList<Monster.MobGrade> getBossMobgradeBetweenLvlGladia(int min, int max){
        ArrayList<Monster> arrayMonstre = new ArrayList<>();
        ArrayList<Monster.MobGrade> arrayMobgrade = new ArrayList<>();
        getMonstres().stream().filter(monster -> monster != null && (ArrayUtils.contains(Constant.BOSS_ID, monster.getId())) && !(ArrayUtils.contains(Constant.EXCEPTION_GLADIATROOL_BOSS, monster.getId())) && !(monster.getGrade(1).getSpells().keySet().isEmpty()) && (monster.getAlign() == -1)
                && (getLvlMax(monster) >= min && getLvlMax(monster) < max)).forEach(arrayMonstre::add);

        for(Monster mob : arrayMonstre){
            arrayMobgrade.add(mob.getGrade(5));
        }
        return arrayMobgrade;
    }


    public int getLvlMax(Monster monstre){
        int levelmoyen = monstre.getGrade(5).getLevel();
        return levelmoyen;
    }

    public void removeAccount(int guid) {
            accounts.remove(guid);
    }

    public void updateGameObject(GameObject obj) {

    }

    public static class Drop {
        private int objectId, ceil, action, level;
        private String condition;
        private ArrayList<Double> percents;
        private double localPercent;
        private boolean isOnAllMob=false;
        private boolean isBlackitem=false;
        private boolean isNoPPInfluence=false;

        public Drop(int objectId, ArrayList<Double> percents, int ceil, int action, int level, String condition,boolean isOnAllMob,boolean isBlackitem,boolean isNoPPInfluence) {
            this.objectId = objectId;
            this.percents = percents;
            this.ceil = ceil;
            this.action = action;
            this.level = level;
            this.condition = condition;
            this.isOnAllMob = isOnAllMob;
            this.isBlackitem = isBlackitem;
            this.isNoPPInfluence= isNoPPInfluence;
        }

        public Drop(int objectId, double percent, int ceil, boolean isNoPPInfluence) {
            this.objectId = objectId;
            this.localPercent = percent;
            this.ceil = ceil;
            this.action = -1;
            this.level = -1;
            this.condition = "";
            this.isOnAllMob=false;
            this.isBlackitem=false;
            this.isNoPPInfluence=isNoPPInfluence;
        }

        public int getObjectId() {
            return objectId;
        }

        public boolean getisOnAllMob() {
            return isOnAllMob;
        }

        public int getCeil() {
            return ceil;
        }

        public int getAction() {
            return action;
        }

        public boolean isBlackitem() {
            return isBlackitem;
        }

        public boolean isNoPPInfluence() {
            return isNoPPInfluence;
        }

        public void setNoPPInfluence(boolean value) {
             this.isNoPPInfluence = value;
        }

        public int getLevel() {
            return level;
        }

        public String getCondition() {
            return condition;
        }

        public double getLocalPercent() {
            return localPercent;
        }

        public void setLocalPercent(double localPercent) {
            this.localPercent = localPercent;
        }

        public Drop copy(int grade) {
            Drop drop = new Drop(this.objectId, null, this.ceil, this.action, this.level, this.condition,this.isOnAllMob,this.isBlackitem,this.isNoPPInfluence);
            if(this.percents == null) return null;
            if(this.percents.isEmpty()) return null;
            try {
                if (this.percents.get(grade - 1) == null) return null;
                drop.localPercent = this.percents.get(grade - 1);
            } catch(IndexOutOfBoundsException ignored) { return null; }
            return drop;
        }

        public double getPercentbyGrade(int Grade) {
            double percent = percents.get(Grade -1);
            return percent;
        }
    }

    public House getCasaDentroPorMapa(Short mapaID) {
        for (House casa : Houses.values()) {
            if (casa.getHouseMaps().contains(mapaID)) {
                return casa;
            }
        }
        return null;
    }

    public static class Couple<L, R> {
        public L first;
        public R second;

        public Couple(L s, R i) {
            this.first = s;
            this.second = i;
        }
    }


    public static class ExpLevel {
        public long perso;
        public int metier;
        public int mount;
        public int pvp;
        public long guilde;
        public long tourmenteurs;
        public long bandits;

        public ExpLevel(long c, int m, int d, int p, long t, long b) {
            perso = c;
            metier = m;
            this.mount = d;
            pvp = p;
            guilde = perso * 10;
            tourmenteurs = t;
            bandits = b;
        }
    }
}
