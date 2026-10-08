package client;

import area.map.GameCase;
import area.map.GameMap;
import area.map.entity.House;
import area.map.entity.InteractiveObject;
import area.map.entity.MountPark;
import area.map.labyrinth.Minotoror;
import area.map.labyrinth.PigDragon;
import client.other.Party;
import client.other.Restriction;
import client.other.Stalk;
import client.other.Stats;
import command.administration.Group;
import common.Formulas;
import common.SocketManager;
import database.Database;
import dynamic.Start;
import entity.Collector;
import entity.Prism;
import entity.monster.Monster;
import entity.mount.Mount;
import entity.pet.Pet;
import entity.pet.PetEntry;
import event.EventManager;
import fight.Fight;
import fight.Fighter;
import fight.arena.DeathMatch;
import fight.arena.TeamMatch;
import fight.spells.Effect;
import fight.spells.EffectConstant;
import fight.spells.SpellGrade;
import game.GameClient;
import game.GameServer;
import game.action.ExchangeAction;
import game.action.GameAction;
import game.world.World;
import guild.Guild;
import guild.GuildMember;
import job.Job;
import job.JobAction;
import job.JobStat;
import kernel.*;
import object.GameObject;
import object.ObjectSet;
import object.ObjectTemplate;
import org.apache.commons.lang3.ArrayUtils;
import other.Action;
import other.Dopeul;
import other.QuickSets;
import other.Titre;
import quest.Quest;
import quest.QuestPlayer;
import util.TimerWaiter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class Player {
    /** special fight **/
    public TeamMatch kolizeum;
    public DeathMatch deathMatch;

    public final Restriction restriction;
    public Stats stats;

    /** var for commands **/
    public boolean isNew = false;
    public boolean controleinvo = false;
    public boolean noxp=false;
    public byte needRestat = 0;
    //Suiveur - Suivi
    public Map<Integer, Player> follower = new HashMap<>();
    public Player follow = null;
    //Commande h�h�
    public int thatMap = -1;
    public int thatCell = -1;
    public boolean walkFast = false;
    public boolean getCases = false;
    public boolean mpToTp = false;
    public boolean noall = false;
    public boolean isXpOffilike = false;


    //Job
    private JobAction _curJobAction;
    //Disponibilit�
    public boolean _isAbsent = false;
    public boolean _isInvisible = false;
    //Double
    public boolean _isClone = false;

    //Prison Alignement :
    public boolean isInEnnemyFaction;
    public long enteredOnEnnemyFaction;
    public boolean donjon;


    public ArrayList<Integer> thisCases = new ArrayList<>();
    private int id;
    private String name;
    private int sexe;
    private int classe;
    private Classe classeinit;
    private int color1;
    private int color2;
    private int color3;
    private int level;
    private int energy;
    private long exp;
    private int curPdv;
    private int maxPdv;
    private Stats statsParcho = new Stats(true);
    private long kamas;
    private int _spellPts;
    private int _capital;
    private int _size;
    private int gfxId;
    private int _orientation = 1;
    private Account account;
    //PDV
    private int _accID;
    private boolean canAggro = true;
    //Emote
    private List<Integer> emotes = new ArrayList<>();
    //Variables d'ali
    private byte _align = 0;
    private int _deshonor = 0;
    private int _honor = 0;
    private boolean _showWings = false;
    private int _aLvl = 0;
    private GuildMember _guildMember;
    private boolean _showFriendConnection;
    private String _canaux;
    private Fight fight;
    private boolean away;
    private GameMap curMap;
    private GameCase curCell;
    private boolean ready = false;
    private boolean isOnline = false;
    private Party party;
    private int duelId = -1;
    private Map<Integer, Effect> buffs = new HashMap<Integer, Effect>();
    private Map<Long, GameObject> objects = new HashMap<>();
    private String _savePos;
    private int _emoteActive = 0;
    private int savestat;
    private House _curHouse;
    //Invitation
    private int _inviting = 0;
    private ArrayList<Integer> craftingType = new ArrayList<>();
    private Map<Integer, JobStat> _metiers = new HashMap<Integer, JobStat>();
    //Enclos

    //Monture
    private Mount _mount;
    private int _mountXpGive = 0;
    private boolean _onMount = false;
    //Zaap
    private ArrayList<Short> _zaaps = new ArrayList<Short>();
    //Sort
    private Map<Integer, SpellGrade> _sorts = new HashMap<Integer, SpellGrade>();
    private Map<Integer, Character> _sortsPlaces = new HashMap<Integer, Character>();
    //Inventory shortcut bar (1.43.7) : position → object GUID
    private Map<Integer, Long> _inventoryShortcuts = new HashMap<Integer, Long>();
    public Map<Integer, Long> getInventoryShortcuts() { return _inventoryShortcuts; }
    public String parseInventoryShortcutsToDB() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Long> e : _inventoryShortcuts.entrySet()) {
            if (sb.length() > 0) sb.append(",");
            sb.append(e.getKey()).append(":").append(e.getValue());
        }
        return sb.toString();
    }
    public void parseInventoryShortcuts(String s) {
        _inventoryShortcuts.clear();
        if (s == null || s.isEmpty()) return;
        for (String pair : s.split(",")) {
            String[] kv = pair.split(":");
            if (kv.length == 2) {
                try {
                    _inventoryShortcuts.put(Integer.parseInt(kv[0]), Long.parseLong(kv[1]));
                } catch (NumberFormatException ignored) {}
            }
        }
    }
    /**
     * Construit les packets OrA à envoyer au client au login pour restaurer la
     * shortcut bar d'items. Format : `OrA<pos>;<templateID>;<compressedEffects>`
     * (cf. InventoryShortcutItem côté client).
     * Les entrées dont l'objet n'existe plus dans l'inventaire sont silencieusement
     * filtrées (l'item a peut-être été détruit/vendu depuis).
     */
    public String parseInventoryShortcutsListPacket() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<Integer, Long> e : _inventoryShortcuts.entrySet()) {
            object.GameObject obj = this.getItems().get(e.getValue());
            if (obj == null) continue;
            if (!first) sb.append("|");
            sb.append("OrA").append(e.getKey())
              .append(";").append(obj.getTemplate().getId())
              .append(";").append(obj.parseStatsString());
            first = false;
        }
        return sb.toString();
    }
    //Titre
    private byte _title = 0;
    //Mariage
    private int wife = 0;
    private int _isOK = 0;
    //Fantome
    private boolean isGhost = false;
    private int _Speed = 0;
    //Marchand
    private boolean _seeSeller = false;
    private Map<Long, Integer> _storeItems = new HashMap<>();                    //<ObjID, Prix>
    //Metier
    private boolean _metierPublic = false;
    private boolean _livreArti = false;

    //Fight end
    private int hasEndFight = -1;
    private Action endFightAction;
    private Monster.MobGroup hasMobGroup = null;
    //Item classe

    private Map<Integer, HashMap<Integer, Integer>> objectsClassSpell = new HashMap<>();
    private int _bendHechizo = 0;
    private int _bendEfecto = 0;
    private int _bendModif = 0;

    // Taverne
    private long timeTaverne = 0;
    //GA
    private GameAction _gameAction = null;
    //Name
    //Fight :
    private boolean _spec;
    //Traque
    private Stalk _traqued;
    private boolean doAction;
    //FullMorph Stats
    private boolean _morphMode = false;
    private int _morphId;
    private Map<Integer, SpellGrade> _saveSorts = new HashMap<Integer, SpellGrade>();
    private Map<Integer, Character> _saveSortsPlaces = new HashMap<Integer, Character>();
    private int _saveSpellPts;
    private int pa = 0,
            pm = 0,
            vitalite = 0,
            sagesse = 0,
            terre = 0,
            feu = 0,
            eau = 0,
            air = 0, initiative = 0;
    private boolean useStats = false;
    private boolean useCac = true;
    private boolean useInventory = true;
    // Other ?
    private short oldMap = 0;
    private int oldCell = 0;
    private String _allTitle = null;
    private boolean isBlocked = false;
    private int action = -1;
    //Regen hp
    private boolean sitted;
    private int regenRate = 500;
    private long regenTime = -1;                                                //-1 veut dire que la personne ne c'est jamais connecte
    private boolean isInPrivateArea = false;
    public Start start;
    private Group groupe;
    private boolean isInvisible = false;

    private Map<Integer, QuestPlayer> questList = new HashMap<>();
    private boolean changeName;
    public boolean afterFight = false;

    public int lastfightmap = 0;
    public GameCase lastfightcell = null;

    public int isParcho = 0;
    private boolean maitre;
    public boolean ipdrop = false;
    public boolean oneWindows = false;
    public int difficulty = 0;
    public boolean noitems = false;
    public boolean noblackitems = false;
    public boolean passturn = false;
    //public boolean boutique =  false;
    public boolean isInvocControlable = false;
    private ArrayList<Player> compagnon = new ArrayList<>();
    private Fighter currentCompagnon = null;
    public int[] LastTonicProposed = new int[3];
    //Systeme de Maitre
    //Commande .maitre
    public List<Player> PlayerList1 = new ArrayList<Player>();
    public Player SlaveLeader = null;
    public String lastTonicPacket="";

    //Retourne la liste des esclaves
    @SuppressWarnings("rawtypes")
    public List<Player> getSlaves(){
        return PlayerList1;
    }

    public void addSlave(Player givenSlave){
        PlayerList1.add(givenSlave);
    }
    //Retourne le chef des esclaves
    public Player getSlaveLeader(){
        return SlaveLeader;
    }
    //Defini un chef pour ce Player
    public void setSlaveLeader(Player givenPlayer){
        SlaveLeader = givenPlayer;
    }
    //Dispose
    public void disposeSlavery(){
        for(Player slave: PlayerList1){
            if(slave == null)continue;
            if(slave.getFight() != null)continue;
            if(!slave.isOnline())continue;
            if(slave.getSlaveLeader() != this)continue;
            slave.setSlaveLeader(null);
        }
        this.PlayerList1.clear(); // on vide la liste
    }

    public ArrayList<Integer> getIsCraftingType() {
        return craftingType;
    }

    public Player(int id, String name, int groupe, int sexe, int classe,
                  int color1, int color2, int color3, long kamas, int pts,
                  int _capital, int energy, int level, long exp, int _size,
                  int _gfxid, byte alignement, int account,
                  Map<Integer, Integer> stats, byte seeFriend,
                  byte seeAlign, byte seeSeller, String canaux, short map, int cell,
                  String stuff, String storeObjets, int pdvPer, String spells,
                  String savePos, String jobs, int mountXp, int mount, int honor,
                  int deshonor, int alvl, String z, byte title, int wifeGuid,
                  String morphMode, String allTitle, String emotes, long prison,
                  boolean isNew, String parcho, long timeDeblo, boolean noall, String deadInformation, byte needRestat, long totalKills, int isParcho) {
        this.id = id;
        this.noall = noall;
        this.name = name;
        this.groupe = Group.getGroupeById(groupe);
        this.sexe = sexe;
        this.classe = classe;
        this.classeinit = World.world.getClasse(classe);
        this.color1 = color1;
        this.color2 = color2;
        this.color3 = color3;
        this.kamas = kamas;
        this._capital = _capital;
        this._align = alignement;
        this._honor = honor;
        this._deshonor = deshonor;
        this._aLvl = alvl;
        this.energy = energy;
        this.level = level;
        this.exp = exp;
        if (mount != -1) {
            this._mount = World.world.getMountById(mount);
        }
        this._size = _size;
        this.gfxId = _gfxid;
        this._mountXpGive = mountXp;
        this.stats = new Stats(stats, true, this);
        this._accID = account;
        this.account = World.world.getAccount(account);
        this._showFriendConnection = seeFriend == 1;
        this.wife = wifeGuid;
        this._metierPublic = false;
        this._title = title;
        this.changeName = false;
        this._allTitle = allTitle;
        this._seeSeller = seeSeller == 1;
        savestat = 0;
        this._canaux = canaux;
        this.curMap = World.world.getMap(map);
        this._savePos = savePos;
        this.isNew = isNew;
        this.regenTime = System.currentTimeMillis();
        //Database.getStatics().getQuestPlayerData().loadPerso(this);
        this.restriction = Restriction.get(this.id);
        this.timeTaverne = timeDeblo;
        try {
            String[] split = deadInformation.split(",");
            this.dead = Byte.parseByte(split[0]);
            this.deadTime = Long.parseLong(split[1]);
            this.deadType = Byte.parseByte(split[2]);
            this.killByTypeId = Long.parseLong(split[3]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.totalKills = totalKills;
        this.needRestat = needRestat;
        try {
            if (!emotes.isEmpty())
                for (String i : emotes.split(";"))
                    this.addStaticEmote(Integer.parseInt(i));
            if (!morphMode.equals("")) {
                if (morphMode.equals("0"))
                    morphMode = "0;0";
                String[] i = morphMode.split(";");
                _morphMode = i[0].equals("1");
                if (!i[1].equals(""))
                    _morphId = Integer.parseInt(i[1]);
            }
            if (_morphMode)
                this._saveSpellPts = pts;
            else
                this._spellPts = pts;

            if (prison != 0) {
                this.isInEnnemyFaction = true;
                this.enteredOnEnnemyFaction = prison;
            }
            this._showWings = this.get_align() != 0 && seeAlign == 1;
            if (curMap == null && World.world.getMap((short) 7411) != null) {
                this.curMap = World.world.getMap((short) 7411);
                this.curCell = curMap.getCase(311);
            } else if (curMap == null && World.world.getMap((short) 7411) == null) {
                GameServer.a("Null MAP");
                Main.INSTANCE.stop("Player1");
                return;
            } else if (curMap != null) {
                this.curCell = curMap.getCase(cell);
                if (curCell == null) {
                    this.curMap = World.world.getMap((short) 7411);
                    this.curCell = curMap.getCase(311);
                }
            }
            if (!z.equalsIgnoreCase("")) {
                for (String str : z.split(",")) {
                    try {
                        _zaaps.add(Short.parseShort(str));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (!isNew && (curMap == null || curCell == null)) {
                Main.INSTANCE.stop("Player2");
                return;
            }
            if (!stuff.equals("")) {
                if (stuff.charAt(stuff.length() - 1) == '|')
                    stuff = stuff.substring(0, stuff.length() - 1);
                Database.getStatics().getObjectData().load(stuff.replace("|", ","));
            }
            for (String item : stuff.split("\\|")) {
                if (item.equals(""))
                    continue;
                String[] infos = item.split(":");

                long guid = 0;
                try {
                    guid = Long.parseLong(infos[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }

                GameObject obj = World.world.getGameObject(guid);

                if (obj == null)
                    continue;

                try {
                    if (obj.getTemplate().getType() == Constant.ITEM_TYPE_FAMILIER) {
                        PetEntry MyPets = World.world.getPetsEntry(guid);
                        if (MyPets == null) {
                            obj.getTemplate().createOldFamilier(obj);
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    //System.out.println(this.getName() + " - Erreur avec la création de fami "+ obj.getGuid() + " - " + e );
                    continue;
                }

                objects.put(obj.getGuid(), obj);
            }
            try {
                if (parcho != null && !parcho.equalsIgnoreCase(""))
                    for (String stat : parcho.split(";"))
                        if (!stat.equalsIgnoreCase("")){
                            int value = Integer.parseInt(stat.split(",")[1]);
                            if( value > 101 ){
                                value = 101;
                            }
                            this.statsParcho.addOneStat(Integer.parseInt(stat.split(",")[0]), value );
                        }

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!storeObjets.equals("")) {
                for (String _storeObjets : storeObjets.split("\\|")) {
                    String[] infos = _storeObjets.split(",");
                    long guid = 0;
                    int price = 0;
                    try {
                        guid = Long.parseLong(infos[0]);
                        price = Integer.parseInt(infos[1]);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }

                    GameObject obj = World.world.getGameObject(guid);
                    if (obj == null)
                        continue;

                    _storeItems.put(obj.getGuid(), price);
                }
            }
            this.maxPdv = (this.level - 1) * 5 + 55
                    + getTotalStats().getEffect(EffectConstant.STATS_ADD_VITA)
                    + getTotalStats().getEffect(EffectConstant.STATS_ADD_VIE);
            if (this.curPdv <= 0)
                this.curPdv = 1;
            if (pdvPer > 100)
                this.curPdv = (this.maxPdv * 100 / 100);
            else
                this.curPdv = (this.maxPdv * pdvPer / 100);
            if (this.curPdv <= 0)
                this.curPdv = 1;
            parseSpells(spells);

            String jobs2 = "2,0;24,0;28,0;25,0;36,0;58,0;41,0;56,0;26,0;15,0;16,0;27,0;11,0;14,0;17,0;20,0;31,0;13,0;18,0;19,0;60,0;65,0;62,0;63,0;64,0;43,0;44,0;45,0;46,0;47,0;48,0;49,0;50,0";
            //Chargement de tous les métier
            for (String aJobData : jobs2.split(";")) {
                String[] infos = aJobData.split(",");
                try {
                    int jobID = Integer.parseInt(infos[0]);
                    long xp = Long.parseLong(infos[1]);
                    Job m = World.world.getMetier(jobID);
                    JobStat SM = _metiers.get(learnJob(m));
                    SM.addXp(this, xp);
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }

            //Chargement des métiers du joueurs
            if (!jobs.equals("")) {
                for (String aJobData : jobs.split(";")) {
                    String[] infos = aJobData.split(",");
                    try {
                        int jobID = Integer.parseInt(infos[0]);
                        long xp = Long.parseLong(infos[1]);
                        JobStat SM = _metiers.get(getPosByJob(jobID));
                        SM.addXp(this, xp);
                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                }
            }

            if (this.energy == 0)
                setGhost();
            else if (this.energy == -1)
                setFuneral();
            this.isParcho = isParcho;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Clone double
    public Player(int id, String name, int groupe, int sexe, int classe,
                  int color1, int color2, int color3, int level, int _size,
                  int _gfxid, Map<Integer, Integer> stats, String stuff,
                  int pdvPer, byte seeAlign, int mount, int alvl, byte alignement) {
        this.id = id;
        this.name = name;
        this.groupe = Group.getGroupeById(groupe);
        this.sexe = sexe;
        this.classe = classe;
        this.color1 = color1;
        this.color2 = color2;
        this.color3 = color3;
        this.level = level;
        this._aLvl = alvl;
        this._size = _size;
        this.gfxId = _gfxid;
        this.stats = new Stats(stats, true, this);
        this.changeName = false;
        this.restriction = null;
        this.set_isClone(true);

        for (String item : stuff.split("\\|")) {
            if (item.equals(""))
                continue;
            String[] infos = item.split(":");
            long guid = Long.parseLong(infos[0]);
            GameObject obj = World.world.getGameObject(guid);
            if (obj == null)
                continue;
            objects.put(obj.getGuid(), obj);
        }
        this.maxPdv = (this.level - 1) * 5 + 50 + getStats().getEffect(EffectConstant.STATS_ADD_VITA);
        this.curPdv = (this.maxPdv * pdvPer) / 100;
        this._align = alignement;
        this._showWings = this.get_align() != 0 && seeAlign == 1;
        if (mount != -1)
            this._mount = World.world.getMountById(mount);
    }

    public static Player CREATE_PERSONNAGE(String name, int sexe, int classe,
                                           int color1, int color2, int color3, Account compte) {
        String z = "";
        if (Config.INSTANCE.getALL_ZAAP()) {
            for (Entry<Integer, Integer> i : Constant.ZAAPS.entrySet()) {
                if (z.length() != 0)
                    z += ",";
                z += i.getKey();
            }
        }
        if (classe > 12 || classe < 1)
            return null;
        if (sexe < 0 || sexe > 1)
            return null;
        int startMap = Config.INSTANCE.getSTART_MAP();
        int startCell = Config.INSTANCE.getSTART_CELL();
        
        Player perso = new Player(Database.getStatics().getPlayerData().getNextId(), name, -1, sexe, classe, color1, color2, color3, Config.INSTANCE.getStartKamas(), ((Config.INSTANCE.getStartLevel() - 1)), ((Config.INSTANCE.getStartLevel() - 1) * 5), 10000, Config.INSTANCE.getStartLevel(), World.world.getPersoXpMin(Config.INSTANCE.getStartLevel()), 100, Integer.parseInt(classe
                + "" + sexe), (byte) 0, compte.getId(), new HashMap<Integer, Integer>(), (byte) 1, (byte) 0, (byte) 0, "*#%!pi$:?", (startMap != 0 ? (short) startMap : Constant.getStartMap(classe)), (startCell != 0 ? (short) startCell : Constant.getStartCell(classe)),
                //(short)6824,
                //224,
                "", "", 100, "", (startMap != 0 ? (short) startMap : Constant.getStartMap(classe))
                + ","
                + (startCell != 0 ? (short) startCell : Constant.getStartCell(classe)), "2,0;24,0;28,0;25,0;36,0;58,0;41,0;56,0;26,0;15,0;16,0;27,0;11,0;14,0;17,0;20,0;31,0;13,0;18,0;19,0;60,0;65,0;62,0;63,0;64,0;43,0;44,0;45,0;46,0;47,0;48,0;49,0;50,0", 0, -1, 0, 0, 0, z, (byte) 0, 0, "0;0", "", Config.INSTANCE.getALL_EMOTE() ? "0;1;2;3;4;5;6;7;8;9;10;11;12;13;14;15;16;17;18;19;20;21" : "0", 0, true, "118,0;119,0;123,0;124,0;125,0;126,0", 0, false, "0,0,0,0", (byte) 0, 0, 0);
        perso.emotes.add(1);

        perso._sorts = Constant.getStartSorts(classe);
        for (int a = 1; a <= perso.getLevel(); a++)
            Constant.onLevelUpSpells(perso, a);
        perso._sortsPlaces = Constant.getStartSortsPlaces(classe);

        SocketManager.GAME_SEND_WELCOME(perso);


        if (!Database.getStatics().getPlayerData().add(perso))
            return null;
        World.world.addPlayer(perso);
        if (Config.INSTANCE.getSERVER_KEY().equals("aegnor")) {
            for (ObjectTemplate t : World.world.getItemSet(5).getItemTemplates()) {
                GameObject obj = t.createNewItem(1, true,5);
                if(perso.addObjet(obj, true))
                    World.world.addGameObject(obj, true);
            }
            ObjectTemplate template = World.world.getObjTemplate(10207);
            if(template != null) {
                GameObject object = template.createNewItem(1, true,5);
                if(object != null) {
                    object.getTxtStat().clear();
                    object.getTxtStat().putAll(Dopeul.generateStatsTrousseau());
                    if(perso.addObjet(object, true))
                        World.world.addGameObject(object, true);
                }
            }
        }
        return perso;
    }

    //controlable
    public Player(final int id, final Monster.MobGrade mobModelo, Fighter caster) {
        Stats stats = new Stats(mobModelo);
        int  PDV = mobModelo.getPdv();
        this.id = id;
        name = mobModelo.getTemplate().getName();
        classe = Constant.CLASS_MULTIMAN;
        level = mobModelo.getLevel();
        gfxId = mobModelo.getTemplate().getGfxId();
        _size = 100;
        this.stats = stats;
        maxPdv = curPdv = PDV;
        modifStatByInvocator(caster, mobModelo);
        account = null;
        restriction = null;
        int i = 0;
        for (Entry<Integer, SpellGrade> entry : mobModelo.getSpells().entrySet()) {
            SpellGrade st = entry.getValue();
            if (st == null) {
                continue;
            }
            learnSpell(st.getSpellID(), st.getLevel(), Constant.SPELL_PLACES.get(i+1));
            i++;
        }
        _saveSorts.putAll(mobModelo.getSpells());
    }

    public void modifStatByInvocator(Fighter caster, Monster.MobGrade MobModelo) {
        float tauxlvl = (1.0F + (caster.getLvl()) / 100.0F);
        float tauxstat = 0.25F;
        float tauxhp = 0.1F;

        float boostlife = caster.getPdvMax() * tauxhp;
        Stats casterboost = caster.getTotalStats();
        float force2 = 0,intel2 = 0,agili2=0,sages2=0,chanc2=0;
        if(!ArrayUtils.contains(Constant.STATIC_INVO,MobModelo.getTemplate().getId())) {
            force2 = casterboost.get(EffectConstant.STATS_ADD_FORC) * tauxstat;
            intel2 = casterboost.get(EffectConstant.STATS_ADD_INTE) * tauxstat;
            agili2 = casterboost.get(EffectConstant.STATS_ADD_AGIL) * tauxstat;
            sages2 = casterboost.get(EffectConstant.STATS_ADD_SAGE) * tauxstat;
            chanc2 = casterboost.get(EffectConstant.STATS_ADD_CHAN) * tauxstat;
        }

        float force3 = (this.stats.get(EffectConstant.STATS_ADD_FORC) * tauxlvl) ;
        float intel3 =  (this.stats.get(EffectConstant.STATS_ADD_INTE) * tauxlvl) ;
        float agili3 = (this.stats.get(EffectConstant.STATS_ADD_AGIL) * tauxlvl) ;
        float sages3 =  (this.stats.get(EffectConstant.STATS_ADD_SAGE) * tauxlvl) ;
        float chanc3 =  (this.stats.get(EffectConstant.STATS_ADD_CHAN) * tauxlvl) ;

        int life = Math.round(this.maxPdv * tauxlvl) + Math.round(boostlife);
        this.curPdv = life;
        this.maxPdv = life;

        int force = Math.round( force3 + force2 );
        int intel = Math.round( intel3 + intel2 );
        int agili = Math.round( agili3 + agili2 );
        int sages = Math.round( sages3 + sages2 );
        int chanc = Math.round( chanc3 + chanc2 );
        this.stats.addOneStat(EffectConstant.STATS_ADD_FORC, -(this.stats.get(EffectConstant.STATS_ADD_FORC)));
        this.stats.addOneStat(EffectConstant.STATS_ADD_INTE, -(this.stats.get(EffectConstant.STATS_ADD_INTE)));
        this.stats.addOneStat(EffectConstant.STATS_ADD_AGIL, -(this.stats.get(EffectConstant.STATS_ADD_AGIL)));
        this.stats.addOneStat(EffectConstant.STATS_ADD_SAGE, -(this.stats.get(EffectConstant.STATS_ADD_SAGE)));
        this.stats.addOneStat(EffectConstant.STATS_ADD_CHAN, -(this.stats.get(EffectConstant.STATS_ADD_CHAN)));
        this.stats.addOneStat(EffectConstant.STATS_ADD_FORC, force);
        this.stats.addOneStat(EffectConstant.STATS_ADD_INTE, intel);
        this.stats.addOneStat(EffectConstant.STATS_ADD_AGIL, agili);
        this.stats.addOneStat(EffectConstant.STATS_ADD_SAGE, sages);
        this.stats.addOneStat(EffectConstant.STATS_ADD_CHAN, chanc);
    }

    public static Player createInvoControlable(final int id, final Monster.MobGrade grade, Fighter caster) {
        Player multiman = new Player(id, grade, caster);
        multiman.isInvocControlable  = true;
        return multiman;
    }

    public static String getCompiledEmote(List<Integer> i) {
        int i2 = 0;
        for (Integer b : i) i2 += (2 << (b - 2));
        return i2 + "|0";
    }

    public void setCurrentCompagnon(Fighter fighter)
    {
        currentCompagnon = fighter;
    }

    public Fighter getCurrentCompagnon() {return currentCompagnon; }

    public void deleteCurrentCompagnon()
    {
        currentCompagnon = null;
    }

    public int getisParcho() { return isParcho;}
    public void setisParcho(int activate) { this.isParcho = activate;}

    public ArrayList<Player> getAllCompagnons()
    {
        return compagnon;
    }

    public Player getCompagnon(Player player)
    {
        for(Player p : compagnon)
        {
            if(p == player)
            {
                return p;
            }
        }
        return null;
    }

    public void addCompagnon(Player player)
    {
        compagnon.add(player);
    }

    public boolean isMultiman()
    {
        if(classe == Constant.CLASS_MULTIMAN)
        {
            return true;
        }
        return false;
    }

    //CLONAGE
    public static Player ClonePerso(Player P, int id, int pdv) {
        HashMap<Integer, Integer> stats = new HashMap<Integer, Integer>();
        stats.put(EffectConstant.STATS_ADD_VITA, pdv);
        stats.put(EffectConstant.STATS_ADD_FORC, P.getStats().getEffect(EffectConstant.STATS_ADD_FORC));
        stats.put(EffectConstant.STATS_ADD_SAGE, P.getStats().getEffect(EffectConstant.STATS_ADD_SAGE));
        stats.put(EffectConstant.STATS_ADD_INTE, P.getStats().getEffect(EffectConstant.STATS_ADD_INTE));
        stats.put(EffectConstant.STATS_ADD_CHAN, P.getStats().getEffect(EffectConstant.STATS_ADD_CHAN));
        stats.put(EffectConstant.STATS_ADD_AGIL, P.getStats().getEffect(EffectConstant.STATS_ADD_AGIL));
        stats.put(EffectConstant.STATS_ADD_PA, P.getStats().getEffect(EffectConstant.STATS_ADD_PA));
        stats.put(EffectConstant.STATS_ADD_PM, P.getStats().getEffect(EffectConstant.STATS_ADD_PM));
        stats.put(EffectConstant.STATS_ADD_RP_NEU, P.getStats().getEffect(EffectConstant.STATS_ADD_RP_NEU));
        stats.put(EffectConstant.STATS_ADD_RP_TER, P.getStats().getEffect(EffectConstant.STATS_ADD_RP_TER));
        stats.put(EffectConstant.STATS_ADD_RP_FEU, P.getStats().getEffect(EffectConstant.STATS_ADD_RP_FEU));
        stats.put(EffectConstant.STATS_ADD_RP_EAU, P.getStats().getEffect(EffectConstant.STATS_ADD_RP_EAU));
        stats.put(EffectConstant.STATS_ADD_RP_AIR, P.getStats().getEffect(EffectConstant.STATS_ADD_RP_AIR));
        stats.put(EffectConstant.STATS_ADD_AFLEE, P.getStats().getEffect(EffectConstant.STATS_ADD_AFLEE));
        stats.put(EffectConstant.STATS_ADD_MFLEE, P.getStats().getEffect(EffectConstant.STATS_ADD_MFLEE));

        byte showWings = 0;
        int alvl = 0;
        if (P.get_align() != 0 && P._showWings) {
            showWings = 1;
            alvl = P.getGrade();
        }
        int mountID = -1;
        if (P.getMount() != null) {
            mountID = P.getMount().getId();
        }

        Player Clone = new Player(id, P.getName(), (P.getGroupe() != null) ? P.getGroupe().getId() : -1, P.getSexe(), P.getClasse(), P.getColor1(), P.getColor2(), P.getColor3(), P.getLevel(), 100, P.getGfxId(), stats, "", 100, showWings, mountID, alvl, P.get_align());
        Clone.objects = new HashMap<>();
        Clone.objects.putAll(P.objects);
        Clone.set_isClone(true);
        if (P._onMount) {
            Clone._onMount = true;
        }
        return Clone;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
        this.changeName = false;

        Database.getStatics().getPlayerData().updateInfos(this);
        if (this.getGuildMember() != null)
            Database.getDynamics().getGuildMemberData().update(this);
    }

    public Group getGroupe() {
        return this.groupe;
    }

    public void setGroupe(Group groupe, boolean reload) {
        this.groupe = groupe;
        if (reload)
            Database.getStatics().getPlayerData().updateGroupe(this);
    }

    public boolean isInvisible() {
        return this.isInvisible;
    }

    public void setInvisible(boolean b) {
        this.isInvisible = b;
    }

    public int getSexe() {
        return this.sexe;
    }

    public void setSexe(int sexe) {
        this.sexe = sexe;
        this.setGfxId(10 * this.getClasse() + this.sexe);
    }

    public int getClasse() {
        return this.classe;
    }

    public void setClasse(int classe) {
        this.classe = classe;
        this.classeinit = World.world.getClasse(classe);

    }

    public int getColor1() {
        return this.color1;
    }

    public int getColor2() {
        return this.color2;
    }

    public int getColor3() {
        return this.color3;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getEnergy() {
        return this.energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public long getExp() {
        return this.exp;
    }

    public void setExp(long exp) {
        this.exp = exp;
    }

    public int getCurPdv() {
        if(isInvocControlable)
        {
            return this.curPdv;
        }
        else {
            refreshLife(false);
            return this.curPdv;
        }
    }

    public void setPdv(int pdv) {
        this.curPdv = pdv;
        if (this.curPdv >= this.maxPdv)
            this.curPdv = this.maxPdv;
        if (party != null)
            SocketManager.GAME_SEND_PM_MOD_PACKET_TO_GROUP(party, this);
    }

    public int getMaxPdv() {
        return this.maxPdv;
    }

    public void setMaxPdv(int maxPdv) {
        this.maxPdv = maxPdv;
        SocketManager.GAME_SEND_STATS_PACKET(this);
        if (party != null)
            SocketManager.GAME_SEND_PM_MOD_PACKET_TO_GROUP(party, this);
    }

    public Stats getStats() {
        if (useStats)
            return newStatsMorph();
        else
            return this.stats;
    }

    public Stats getStatsParcho() {
        return statsParcho;
    }

    public String parseStatsParcho() {
        String parcho = "";
        for (Entry<Integer, Integer> i : statsParcho.getEffects().entrySet()){
            int value = i.getValue();
            if(value > 101){
                value = 101;
            }

            parcho += (parcho.isEmpty() ? i.getKey() + "," + value : ";" + i.getKey() + "," + value);
        }

        return parcho;
    }

    public boolean getDoAction() {
        return doAction;
    }

    public void setDoAction(boolean b) {
        doAction = b;
    }

    public void setRoleplayBuff(int id) {
        int objTemplate = 0;
        switch (id) {
            case 10673:
                objTemplate = 10844;
                break;
            case 10669:
                objTemplate = 10681;
                break;
        }
        if (objTemplate == 0)
            return;
        if (getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF) != null) {
            long guid = getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF).getGuid();
            SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, guid);
            this.deleteItem(guid);
        }

        GameObject obj = World.world.getObjTemplate(objTemplate).createNewRoleplayBuff();
        this.addObjet(obj, false);
        World.world.addGameObject(obj, true);
        SocketManager.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(), this);
        SocketManager.GAME_SEND_Ow_PACKET(this);
        SocketManager.GAME_SEND_STATS_PACKET(this);
        Database.getStatics().getPlayerData().update(this);
    }

    public void setBenediction(int id) {
        if (getObjetByPos(Constant.ITEM_POS_BENEDICTION) != null) {
            long guid = getObjetByPos(Constant.ITEM_POS_BENEDICTION).getGuid();
            SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, guid);
            this.deleteItem(guid);
        }
        if (id == 0) {
            SocketManager.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(), this);
            return;
        }
        int turn = 0;
        switch (id) {
            case 10682:
                turn = 20;
                break;
            default:
                turn = 1;
                break;
        }

        GameObject obj = World.world.getObjTemplate(id).createNewBenediction(turn);
        this.addObjet(obj, false);
        World.world.addGameObject(obj, true);
        SocketManager.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(), this);
        SocketManager.GAME_SEND_Ow_PACKET(this);
        SocketManager.GAME_SEND_STATS_PACKET(this);
        Database.getStatics().getPlayerData().update(this);
    }

    public void setMalediction(int id) {
        int objTemplate = 0;
        switch (id) {
            case 10827:
                objTemplate = 10838;
                break;
            default:
                objTemplate = id;
        }
        if (objTemplate == 0) {
            SocketManager.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(), this);
            return;
        }
        if (getObjetByPos(Constant.ITEM_POS_MALEDICTION) != null) {
            long guid = getObjetByPos(Constant.ITEM_POS_MALEDICTION).getGuid();
            SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, guid);
            this.deleteItem(guid);
        }

        GameObject obj = World.world.getObjTemplate(objTemplate).createNewMalediction();
        this.addObjet(obj, false);
        World.world.addGameObject(obj, true);
        if (this.getFight() != null) {
            SocketManager.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(), this);
            SocketManager.GAME_SEND_Ow_PACKET(this);
            SocketManager.GAME_SEND_STATS_PACKET(this);
            Database.getStatics().getPlayerData().update(this);
        }
    }

    public void setMascotte(int id) {
        if (getObjetByPos(Constant.ITEM_POS_PNJ_SUIVEUR) != null) {
            long guid = getObjetByPos(Constant.ITEM_POS_PNJ_SUIVEUR).getGuid();
            SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, guid);
            this.deleteItem(guid);
        }
        if (id == 0) {
            SocketManager.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(), this);
            return;
        }

        GameObject obj = World.world.getObjTemplate(id).createNewFollowPnj(1);
        if (obj != null)
            if (this.addObjet(obj, false))
                World.world.addGameObject(obj, true);

        SocketManager.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(), this);
        SocketManager.GAME_SEND_Ow_PACKET(this);
        SocketManager.GAME_SEND_STATS_PACKET(this);
        Database.getStatics().getPlayerData().update(this);
    }

    public void setCandy(int id) {
        if (getObjetByPos(Constant.ITEM_POS_BONBON) != null) {
            long guid = getObjetByPos(Constant.ITEM_POS_BONBON).getGuid();
            SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, guid);
            this.deleteItem(guid);
        }
        int turn = 30;
        switch (id) {
            case 8948:
            case 8949:
            case 8950:
            case 8951:
            case 8952:
            case 8953:
            case 8954:
            case 8955:
                turn = 5;
                break;
            case 10665:
                turn = 20;
                break;
            default:
                turn = 30;
                break;
        }

        GameObject obj = World.world.getObjTemplate(id).createNewCandy(turn);
        this.addObjet(obj, false);
        World.world.addGameObject(obj, true);
        SocketManager.GAME_SEND_Ow_PACKET(this);
        SocketManager.GAME_SEND_STATS_PACKET(this);
        Database.getStatics().getPlayerData().update(this);
    }

    public void calculTurnCandy() {
        GameObject obj = getObjetByPos(Constant.ITEM_POS_BONBON);
        if (obj != null) {
            obj.getStats().addOneStat(Constant.STATS_TURN, -1);
            if (obj.getStats().getEffect(Constant.STATS_TURN) <= 0) {
                SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, obj.getGuid());
                this.deleteItem(obj.getGuid());
            } else {
                SocketManager.GAME_SEND_UPDATE_ITEM(this, obj);
            }
            Database.getStatics().getObjectData().update(obj);
        }
        obj = getObjetByPos(Constant.ITEM_POS_PNJ_SUIVEUR);
        if (obj != null) {
            obj.getStats().addOneStat(Constant.STATS_TURN, -1);
            if (obj.getStats().getEffect(Constant.STATS_TURN) <= 0) {
                SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, obj.getGuid());
                this.deleteItem(obj.getGuid());
            } else {
                SocketManager.GAME_SEND_UPDATE_ITEM(this, obj);
            }
            Database.getStatics().getObjectData().update(obj);
        }
        obj = getObjetByPos(Constant.ITEM_POS_BENEDICTION);
        if (obj != null) {
            obj.getStats().addOneStat(Constant.STATS_TURN, -1);
            if (obj.getStats().getEffect(Constant.STATS_TURN) <= 0) {
                SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, obj.getGuid());
                this.deleteItem(obj.getGuid());
            } else {
                SocketManager.GAME_SEND_UPDATE_ITEM(this, obj);
            }
            Database.getStatics().getObjectData().update(obj);
        }
        obj = getObjetByPos(Constant.ITEM_POS_MALEDICTION);
        if (obj != null) {
            obj.getStats().addOneStat(Constant.STATS_TURN, -1);
            if (obj.getStats().getEffect(Constant.STATS_TURN) <= 0) {
                gfxId = getClasse() * 10 + getSexe();
                if (this.getFight() == null)
                    SocketManager.GAME_SEND_ALTER_GM_PACKET(getCurMap(), this);
                SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, obj.getGuid());
                switch (obj.getTemplate().getId()) {
                    case 8169:
                    case 8170:
                        unsetFullMorph();
                        break;
                }

                this.deleteItem(obj.getGuid());
            } else {
                SocketManager.GAME_SEND_UPDATE_ITEM(this, obj);
            }
            Database.getStatics().getObjectData().update(obj);
        }
        obj = getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF);
        if (obj != null) {
            obj.getStats().addOneStat(Constant.STATS_TURN, -1);
            if (obj.getStats().getEffect(Constant.STATS_TURN) <= 0) {
                gfxId = getClasse() * 10 + getSexe();
                SocketManager.GAME_SEND_ALTER_GM_PACKET(getCurMap(), this);
                SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, obj.getGuid());
                this.deleteItem(obj.getGuid());
            } else {
                SocketManager.GAME_SEND_UPDATE_ITEM(this, obj);
            }
            Database.getStatics().getObjectData().update(obj);
        }
    }

    public boolean isSpec() {
        return _spec;
    }

    public void setSpec(boolean s) {
        this._spec = s;
    }

    public String getAllTitle() {
        _allTitle = Database.getStatics().getPlayerData().loadTitles(this.getId());
        return _allTitle;
    }

    public List<QuickSets> getAllSets(Player player) {
        World.world.getSetsByPlayer(player.getId());
        return World.world.getSetsByPlayer(player.getId());
    }

    public boolean haveTitrebyID(int Id){
        Map<Integer, Titre> titres = World.world.getTitres();
        String titlepossess = this.getAllTitle();

        if(titlepossess == null || titlepossess =="" ||titlepossess.isEmpty() ||titlepossess.isBlank())
            return false;

        if(titlepossess.contains(",")) {
            String[] words = titlepossess.split(",");
            int[] arr = new int[words.length];

            for (int i = 0; i < words.length; i++) {
                arr[i] = Integer.valueOf(words[i]);
            }

            if( ArrayUtils.contains( arr, Id ) ){
                return true;
            }
        }
        else{
            int titreid = Integer.valueOf(titlepossess);
            if(titreid == Id){
                return true;
            }
        }

        return false;
    }

    public void setAllTitle(String title) {
        boolean erreur = false;
        if (title.equals("") ||title.isEmpty() || title == null)
            title = "0";

        if (_allTitle != null)
            for (String i : _allTitle.split(","))
                if (i.equals(title))
                    erreur = true;
        if (_allTitle == null && !erreur)
            _allTitle = title;
        else if (!erreur)
            _allTitle += "," + title;

        Database.getStatics().getPlayerData().updateTitles(this.getId(), _allTitle);
    }

    public void setSpells(Map<Integer, SpellGrade> spells) {
        _sorts.clear();
        _sortsPlaces.clear();
        _sorts = spells;
        _sortsPlaces = Constant.getStartSortsPlaces(this.getClasse());
    }

    public void teleportOldMap() {
        this.teleport(oldMap, oldCell);
    }

    public void setCurrentPositionToOldPosition() {
        this.curMap = World.world.getMap(this.oldMap);
        this.curCell = this.curMap.getCase(this.oldCell);
    }

    public void setOldPosition() {
        this.oldMap = this.getCurMap().getId();
        this.oldCell = this.getCurCell().getId();
    }

    public void setOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public Party getParty() {
        return party;
    }

    public void setParty(Party party) {
        this.party = party;
    }

    public String parseSpellToDB() {
        StringBuilder sorts = new StringBuilder();

        if (_morphMode) {
            if (_saveSorts.isEmpty())
                return "";
            for (int key : _saveSorts.keySet()) {
                //3;1;a,4;3;b
                SpellGrade SS = _saveSorts.get(key);
                if (SS == null)
                    continue;
                sorts.append(SS.getSpellID()).append(";").append(SS.getLevel()).append(";");
                if (_saveSortsPlaces.get(key) != null)
                    sorts.append(_saveSortsPlaces.get(key));
                else
                    sorts.append("_");
                sorts.append(",");
            }
        } else {
            if (_sorts.isEmpty())
                return "";
            for (int key : _sorts.keySet()) {
                //3;1;a,4;3;b
                SpellGrade SS = _sorts.get(key);
                if (SS == null)
                    continue;
                sorts.append(SS.getSpellID()).append(";").append(SS.getLevel()).append(";");
                if (_sortsPlaces.get(key) != null)
                    sorts.append(_sortsPlaces.get(key));
                else
                    sorts.append("_");
                sorts.append(",");
            }
        }
        return sorts.substring(0, sorts.length() - 1);
    }

    private void parseSpells(String str) {
        if (!str.equalsIgnoreCase("")) {
            if (_morphMode) {
                String[] spells = str.split(",");
                _saveSorts.clear();
                _saveSortsPlaces.clear();
                for (String e : spells) {
                    try {
                        int id = Integer.parseInt(e.split(";")[0]);
                        int lvl = Integer.parseInt(e.split(";")[1]);
                        char place = e.split(";")[2].charAt(0);
                        learnSpell(id, lvl);
                        this._saveSortsPlaces.put(id, place);
                    } catch (NumberFormatException e1) {
                        e1.printStackTrace();
                    }
                }
            } else {
                String[] spells = str.split(",");
                _sorts.clear();
                _sortsPlaces.clear();
                for (String e : spells) {
                    try {
                        int id = Integer.parseInt(e.split(";")[0]);
                        int lvl = Integer.parseInt(e.split(";")[1]);
                        char place = e.split(";")[2].charAt(0);
                        if (!_morphMode)
                            learnSpell(id, lvl, false, false, false);
                        else
                            learnSpell(id, lvl, false, true, false);
                        _sortsPlaces.put(id, place);
                    } catch (NumberFormatException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

    private void parseSpellsFullMorph(String str) {
        String[] spells = str.split(",");
        _sorts.clear();
        _sortsPlaces.clear();
        for (String e : spells) {
            try {
                int id = Integer.parseInt(e.split(";")[0]);
                int lvl = Integer.parseInt(e.split(";")[1]);
                char place = e.split(";")[2].charAt(0);

                if (!_morphMode)
                    learnSpell(id, lvl, false, false, false);
                else
                    learnSpell(id, lvl, false, true, false);
                _sortsPlaces.put(id, place);
            } catch (NumberFormatException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void parseSpellsFullMorphGladia(String str) {
        String[] spells = str.split(",");
        _sorts.clear();
        _sortsPlaces.clear();
        for (String e : spells) {
            try {
                int id = Integer.parseInt(e.split(";")[0]);
                int lvl = Integer.parseInt(e.split(";")[1]);
                int placeid = Integer.parseInt(e.split(";")[2],16);

                char place = Constant.SPELL_PLACES.get(placeid);

                learnSpell(id, lvl, false, false, false);

                _sortsPlaces.put(id, place);
            } catch (NumberFormatException e1) {
                e1.printStackTrace();
            }
        }
        SocketManager.GAME_SEND_SPELL_LIST(this);
    }


    public String getSavePosition() {
        return _savePos;
    }

    public void set_savePos(String savePos) {
        _savePos = savePos;
    }

    public long getKamas() {
        return kamas;
    }

    public void setKamas(long l) {
        if(l < 0) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String str = "";
            int i = 0;
            for (StackTraceElement caller : stackTrace ) {
                i++;
                str += "["+ i +"] :" + "De " + caller.getMethodName() + "/" + caller.getClassName() + " && ";
                if(i > 4)
                    break;
            }
            World.sendWebhookMessage(Config.INSTANCE.getDISCORD_CHANNEL_FAILLE(),"BAN : Tentative de retrait de "+l+" kamas alors qu'il n'en n'avait que "+this.getKamas() +" : Trace" + str, this);
            this.banAccount();
        }
        else{
            this.kamas = l;
        }
    }

    public Map<Integer, Effect> get_buff() {
        return buffs;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account c) {
        account = c;
    }

    public int get_spellPts() {
        if (_morphMode)
            return _saveSpellPts;
        else
            return _spellPts;
    }

    public void set_spellPts(int pts) {
        if (_morphMode)
            _saveSpellPts = pts;
        else
            _spellPts = pts;
    }

    public Guild getGuild() {
        if (_guildMember == null)
            return null;
        return _guildMember.getGuild();
    }

    public void setChangeName(boolean changeName) {
        this.changeName = changeName;
        if (changeName) this.send("AlEr");
    }

    public boolean isChangeName() {
        return changeName;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public int getDuelId() {
        return duelId;
    }

    public void setDuelId(int _duelid) {
        duelId = _duelid;
    }

    public Fight getFight() {
        return fight;
    }

    public void setFight(Fight fight) {
        refreshLife(false);
        if (fight == null)
            SocketManager.send(this, "ILS2000");
        else
            SocketManager.send(this, "ILF0");
        this.sitted = false;
        this.fight = fight;
    }

    public boolean is_showFriendConnection() {
        return _showFriendConnection;
    }

    public boolean is_showWings() {
        return _showWings;
    }

    public boolean isShowSeller() {
        return _seeSeller;
    }

    public void setShowSeller(boolean is) {
        _seeSeller = is;
    }

    public String get_canaux() {
        return _canaux;
    }

    public GameCase getCurCell() {
        return curCell;
    }

    public void setCurCell(GameCase cell) {
        curCell = cell;
    }

    public int get_size() {
        return _size;
    }

    public void set_size(int _size) {
        this._size = _size;
    }

    public int getGfxId() {
        return gfxId;
    }

    public void setGfxId(int _gfxid) {
        if (this.getClasse() * 10 + this.getSexe() != _gfxid) {
            if (this.isOnMount())
                this.toogleOnMount();
            this.send("AR3K");
        } else {
            this.send("AR6bK");
        }
        gfxId = _gfxid;
    }

    public boolean isMorphMercenaire() {
        return (this.gfxId == 8009 || this.gfxId == 8006);
    }

    public GameMap getCurMap() {
        return curMap;
    }

    public void setCurMap(GameMap curMap) {
        this.curMap = curMap;
    }

    public boolean isAway() {
        return away;
    }

    public void setAway(boolean away) {
        this.away = away;
    }

    public boolean isSitted() {
        return sitted;
    }

    public void setSitted(boolean sitted) {
        if (this.sitted == sitted) {
            return;
        }
        this.sitted = sitted;
        refreshLife(false);
        regenRate = (sitted ? 250 : 500);
        SocketManager.send(this, "ILS" + regenRate);
    }

    public int get_capital() {
        return _capital;
    }

    public void setSpellsPlace(boolean ok) {
        if (ok)
            _sortsPlaces = Constant.getStartSortsPlaces(this.getClasse());
        else
            _sortsPlaces.clear();
        SocketManager.GAME_SEND_SPELL_LIST(this);
    }

    public void learnSpell(int spell, int level, char pos) {
        if (World.world.getSort(spell).getStatsByLevel(level) == null) {
            GameServer.a("LearnSpell " + spell + " level " + level);
            return;
        }

        if (!_sorts.containsKey(spell)) {
            _sorts.put(spell, World.world.getSort(spell).getStatsByLevel(level));
            replace_SpellInBook(pos);
            _sortsPlaces.remove(spell);
            _sortsPlaces.put(spell, pos);
            SocketManager.GAME_SEND_SPELL_LIST(this);
            SocketManager.GAME_SEND_Im_PACKET(this, "03;" + spell);
        }
    }

    public boolean learnSpell(int spellID, int level, boolean save,
                              boolean send, boolean learn) {

        if (World.world.getSort(spellID).getStatsByLevel(level) == null) {
            GameServer.a("Learn Spell " + spellID + " level "+ level + "/ Pas définie");
            return false;
        }

        if (_sorts.containsKey(Integer.valueOf(spellID)) && learn) {
            SocketManager.GAME_SEND_MESSAGE(this, "Tu posséde déjà ce sort.");
            return false;
        } else {
            _sorts.put(Integer.valueOf(spellID), World.world.getSort(spellID).getStatsByLevel(level));
            if (send) {
                SocketManager.GAME_SEND_SPELL_LIST(this);
                SocketManager.GAME_SEND_Im_PACKET(this, "03;" + spellID);
            }
            if (save)
                Database.getStatics().getPlayerData().update(this);
            return true;
        }
    }

    public boolean learnSpell(int spellID, int level) {
        if (World.world.getSort(spellID).getStatsByLevel(level) == null) {
            GameServer.a("Learn Spell " + spellID + " level "+ level + "/ Pas définie");
            return false;
        }

        if (_saveSorts.containsKey(Integer.valueOf(spellID))) {
            return false;
        } else {
            _saveSorts.put(Integer.valueOf(spellID), World.world.getSort(spellID).getStatsByLevel(level));
            return true;
        }
    }

    public boolean unlearnSpell(int spell) {
        if (World.world.getSort(spell) == null) {
            GameServer.a("Learn Spell " + spell +"/ Pas définie");
            return false;
        }

        _sorts.remove(spell);
        this._sortsPlaces.remove(spell);
        SocketManager.GAME_SEND_SPELL_LIST(this);
        SocketManager.GAME_SEND_STATS_PACKET(this);
        Database.getStatics().getPlayerData().update(this);
        return true;
    }

    public boolean unlearnSpell(Player perso, int spellID, int level,
                                int ancLevel, boolean save, boolean send) {
        int spellPoint = 1;
        if (ancLevel == 2)
            spellPoint = 1;
        if (ancLevel == 3)
            spellPoint = 2 + 1;
        if (ancLevel == 4)
            spellPoint = 3 + 3;
        if (ancLevel == 5)
            spellPoint = 4 + 6;
        if (ancLevel == 6)
            spellPoint = 5 + 10;

        if (World.world.getSort(spellID).getStatsByLevel(level) == null) {
            GameServer.a("Learn Spell " + spellID + " level "+ level + "/ Pas définie");
            return false;
        }

        _sorts.put(Integer.valueOf(spellID), World.world.getSort(spellID).getStatsByLevel(level));
        if (send) {
            SocketManager.GAME_SEND_SPELL_LIST(this);
            SocketManager.GAME_SEND_Im_PACKET(this, "0154;" + "<b>" + ancLevel
                    + "</b>" + "~" + "<b>" + spellPoint + "</b>");
            addSpellPoint(spellPoint);
            SocketManager.GAME_SEND_STATS_PACKET(perso);
        }
        if (save)
            Database.getStatics().getPlayerData().update(this);
        return true;
    }

    public boolean boostSpell(int spellID) {
        if (getSortStatBySortIfHas(spellID) == null)
            return false;
        int AncLevel = getSortStatBySortIfHas(spellID).getLevel();
        if (AncLevel == 6)
            return false;
        if (_spellPts >= AncLevel && World.world.getSort(spellID).getStatsByLevel(AncLevel + 1).getReqLevel() <= this.getLevel()) {
            if (learnSpell(spellID, AncLevel + 1, true, false, false)) {
                _spellPts -= AncLevel;
                Database.getStatics().getPlayerData().update(this);
                return true;
            } else {
                return false;
            }
        } else
        //Pas le niveau ou pas les Points
        {
            if (_spellPts < AncLevel)
                if (World.world.getSort(spellID).getStatsByLevel(AncLevel + 1).getReqLevel() > this.getLevel())
                    return false;
        }
        return away;
    }

    public void boostSpellIncarnation() {
        for (Entry<Integer, SpellGrade> i : _sorts.entrySet()) {
            if (getSortStatBySortIfHas(i.getValue().getSpell().getSpellID()) == null)
                continue;
            if (learnSpell(i.getValue().getSpell().getSpellID(), i.getValue().getLevel() + 1, true, false, false))
                Database.getStatics().getPlayerData().update(this);
        }
    }

    public boolean forgetSpell(int spellID) {
        if (getSortStatBySortIfHas(spellID) == null) {
            return false;
        }
        int AncLevel = getSortStatBySortIfHas(spellID).getLevel();
        if (AncLevel <= 1)
            return false;

        if (learnSpell(spellID, 1, true, false, false)) {
            _spellPts += Formulas.spellCost(AncLevel);
            Database.getStatics().getPlayerData().update(this);
            return true;
        } else {
            return false;
        }
    }

    public void demorph() {
        if (this.getMorphMode()) {
            int morphID = this.getClasse() * 10 + this.getSexe();
            this.setGfxId(morphID);
            SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(this.getCurMap(), this.getId());
            SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(this.getCurMap(), this);
        }
        else if(_morphId != this.getClasse() * 10 + this.getSexe())
        {
            this.setGfxId(this.getClasse() * 10 + this.getSexe());
            SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(this.getCurMap(), this.getId());
            SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(this.getCurMap(), this);
        }
    }

    public boolean getMorphMode() {
        return _morphMode;
    }

    public int getMorphId() {
        return _morphId;
    }

    public void setMorphId(int id) {
        this._morphId = id;
    }

    public void setFullMorph(int morphid, boolean isLoad, boolean join) {
        if (this.isOnMount()) this.toogleOnMount();
        if (_morphMode && !join)
            unsetFullMorph();
        if (this.isGhost) {
            SocketManager.send(this, "Im1185");
            return;
        }

        Map<String, String> fullMorph = World.world.getFullMorph(morphid);

        if (fullMorph == null) return;

        if (!join) {
            if (!_morphMode) {
                _saveSpellPts = _spellPts;
                _saveSorts.putAll(_sorts);
                _saveSortsPlaces.putAll(_sortsPlaces);
            }
            if (isLoad) {
                _saveSpellPts = _spellPts;
                _saveSorts.putAll(_sorts);
                _saveSortsPlaces.putAll(_sortsPlaces);
            }
        }

        _morphMode = true;
        _sorts.clear();
        _sortsPlaces.clear();
        _spellPts = 0;

        if( 10 <= Integer.parseInt(fullMorph.get("gfxid")) && Integer.parseInt(fullMorph.get("gfxid")) <= 120) {
            setGfxId(Integer.parseInt(fullMorph.get("gfxid"))+this.getSexe());
        }
        else{
            setGfxId(Integer.parseInt(fullMorph.get("gfxid")));
        }

        //setGfxId(Integer.parseInt(fullMorph.get("gfxid")));
        if (this.fight == null) SocketManager.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(), this);

        if(this.getObjetByPos(Constant.ITEM_POS_ARME) != null) {
            SocketManager.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(this, this.getObjetByPos(Constant.ITEM_POS_ARME));
        }

        this.send("SLo-");
        if(Constant.isGladiatroolMorph(morphid)){
            //GladiatroolSpells gladiatroolSpells = World.world.getGladiatroolSpellsFromPlayer(this, morphid);
            parseSpellsFullMorphGladia(fullMorph.get("spells"));
        }
        else {
            parseSpellsFullMorph(fullMorph.get("spells"));
        }
        setMorphId(morphid);

        if (this.getObjetByPos(Constant.ITEM_POS_ARME) != null)
            if (Constant.isIncarnationWeapon(this.getObjetByPos(Constant.ITEM_POS_ARME).getTemplate().getId()))
                for (int i = 0; i <= this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().get(Constant.STATS_NIVEAU); i++)
                    if (i == 10 || i == 20 || i == 30 || i == 40 || i == 50)
                        boostSpellIncarnation();

        if (this.fight == null && !Constant.isGladiatroolWeapon(this.getObjetByPos(Constant.ITEM_POS_ARME).getTemplate().getId())) {
            SocketManager.GAME_SEND_ASK(this.getGameClient(), this);
            SocketManager.GAME_SEND_Ow_PACKET(this);
            SocketManager.GAME_SEND_SPELL_LIST(this);
        }

        if (this.getObjetByPos(Constant.ITEM_POS_ARME) != null) {
            if (Constant.isGladiatroolWeapon(this.getObjetByPos(Constant.ITEM_POS_ARME).getTemplate().getId())) {
                this.useStats = false;
                this.donjon = fullMorph.get("donjon").equals("1");
                this.useCac = true;
            } else {
                if (fullMorph.get("vie") != null) {
                    try {
                        this.maxPdv = Integer.parseInt(fullMorph.get("vie"));
                        this.setPdv(this.getMaxPdv());
                        this.pa = Integer.parseInt(fullMorph.get("pa"));
                        this.pm = Integer.parseInt(fullMorph.get("pm"));
                        this.vitalite = Integer.parseInt(fullMorph.get("vitalite"));
                        this.sagesse = Integer.parseInt(fullMorph.get("sagesse"));
                        this.terre = Integer.parseInt(fullMorph.get("terre"));
                        this.feu = Integer.parseInt(fullMorph.get("feu"));
                        this.eau = Integer.parseInt(fullMorph.get("eau"));
                        this.air = Integer.parseInt(fullMorph.get("air"));
                        this.initiative = Integer.parseInt(fullMorph.get("initiative")) + this.sagesse + this.terre + this.feu + this.eau + this.air;
                        this.useStats = fullMorph.get("stats").equals("1");
                        this.donjon = fullMorph.get("donjon").equals("1");
                        this.useCac = false;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (this.fight == null) {
            if (!Constant.isGladiatroolWeapon(this.getObjetByPos(Constant.ITEM_POS_ARME).getTemplate().getId())) {
                SocketManager.GAME_SEND_STATS_PACKET(this);
            }
        }

        if (this.fight == null) SocketManager.GAME_SEND_STATS_PACKET(this);
        if (!join)
            Database.getStatics().getPlayerData().update(this);
    }

    public boolean isMorph() {
        return (this.gfxId != (this.getClasse() * 10 + this.getSexe()));
    }

    public boolean canCac() {
        return this.useCac;
    }

    public void unsetMorph() {
        this.setGfxId(this.getClasse() * 10 + this.getSexe());
        SocketManager.GAME_SEND_ALTER_GM_PACKET(this.curMap, this);
        Database.getStatics().getPlayerData().update(this);
    }

    public void unsetFullMorph() {
        if (!_morphMode)
            return;

        int morphID = this.getClasse() * 10 + this.getSexe();
        setGfxId(morphID);

        useStats = false;
        donjon = false;
        _morphMode = false;
        this.useCac = true;
        _sorts.clear();
        _sortsPlaces.clear();
        _spellPts = _saveSpellPts;
        _sorts.putAll(_saveSorts);
        _sortsPlaces.putAll(_saveSortsPlaces);
        parseSpells(parseSpellToDB());

        setMorphId(0);
        if (this.getFight() == null) {
            SocketManager.GAME_SEND_SPELL_LIST(this);
            SocketManager.GAME_SEND_STATS_PACKET(this);
            SocketManager.GAME_SEND_ALTER_GM_PACKET(this.curMap, this);
        }
        Database.getStatics().getPlayerData().update(this);
    }

    public String parseSpellList() {
        StringBuilder packet = new StringBuilder();
        packet.append("SL");
        for (Iterator<SpellGrade> i = _sorts.values().iterator(); i.hasNext(); ) {
            SpellGrade SS = i.next();
            packet.append(SS.getSpellID()).append("~").append(SS.getLevel()).append("~").append(_hashedPositionToHex(_sortsPlaces.get(SS.getSpellID()))).append(";");
        }
        return packet.toString();
    }

    /**
     * Convertit une position de sort encodée en HASH alphabet (a-z, A-Z, 0-9, -, _)
     * vers la représentation HEX attendue par le client 1.43.7 (cf. Spell.initialize
     * ligne 453 : `parseInt(sCompressedPosition, 16)`).
     * Retourne "_" si la position est invalide ou hors range 1-31.
     */
    private static String _hashedPositionToHex(Character c) {
        if (c == null) return "_";
        char ch = c.charValue();
        int idx = -1;
        for (int i = 0; i < common.CryptManager.HASH.length; i++) {
            if (common.CryptManager.HASH[i] == ch) { idx = i; break; }
        }
        if (idx < 1 || idx > 31) return "_"; // hors range visible
        return Integer.toHexString(idx);
    }

    public void set_SpellPlace(int SpellID, char Place) {
        replace_SpellInBook(Place);
        _sortsPlaces.remove(SpellID);
        _sortsPlaces.put(SpellID, Place);
        Database.getStatics().getPlayerData().update(this);
    }

    private void replace_SpellInBook(char Place) {
        for (int key : _sorts.keySet())
            if (_sortsPlaces.get(key) != null)
                if (_sortsPlaces.get(key).equals(Place))
                    _sortsPlaces.remove(key);
    }

    public SpellGrade getSortStatBySortIfHas(int spellID) {
        return _sorts.get(spellID);
    }

    public String parseALK() {
        StringBuilder perso = new StringBuilder();
        perso.append("|");
        perso.append(this.getId()).append(";");
        perso.append(this.getName()).append(";");
        perso.append(this.getLevel()).append(";");
        int gfx = this.gfxId;
        if (this.getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF) != null)
            if (this.getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF).getTemplate().getId() == 10681)
                gfx = 8037;
        perso.append(gfx).append(";");
        int color1 = this.getColor1(), color2 = this.getColor2(), color3 = this.getColor3();
        if (this.getObjetByPos(Constant.ITEM_POS_MALEDICTION) != null)
            if (this.getObjetByPos(Constant.ITEM_POS_MALEDICTION).getTemplate().getId() == 10838) {
                color1 = 16342021;
                color2 = 16342021;
                color3 = 16342021;
            }
        perso.append((color1 != -1 ? Integer.toHexString(color1) : "-1")).append(";");
        perso.append((color2 != -1 ? Integer.toHexString(color2) : "-1")).append(";");
        perso.append((color3 != -1 ? Integer.toHexString(color3) : "-1")).append(";");
        perso.append(getGMStuffString()).append(";");
        perso.append((this.isShowSeller() ? 1 : 0)).append(";");
        perso.append(Config.INSTANCE.getSERVER_ID()).append(";");

        if (this.dead == 1 && Config.INSTANCE.getHEROIC()) {
            perso.append(this.dead).append(";").append(0);
        } else {
            perso.append(0);
        }
        return perso.toString();
    }

    public void removeFromDDB() {
        Database.getStatics().getPlayerData().delete(this);
    }

    public void OnJoinGame() {
        this.account.setCurrentPlayer(this);
        this.setOnline(true);

        if (this.account.getGameClient() == null)
            return;

        GameClient client = this.account.getGameClient();

        if (this.isShowSeller()) {
            this.setShowSeller(false);
            World.world.removeSeller(this.getId(), this.getCurMap().getId());
            SocketManager.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(), this);
        }

        if (this._mount != null)
            SocketManager.GAME_SEND_Re_PACKET(this, "+", this._mount);
        if (this.getClasse() * 10 + this.getSexe() != this.getGfxId())
            this.send("AR3K");

        SocketManager.GAME_SEND_Rx_PACKET(this);
        SocketManager.GAME_SEND_ASK(client, this);

        for (int a = 1; a < World.world.getItemSetNumber(); a++)
            if (this.getNumbEquipedItemOfPanoplie(a) != 0)
                SocketManager.GAME_SEND_OS_PACKET(this, a);

        if (this.fight != null) SocketManager.send(this, "ILF0");
        else SocketManager.send(this, "ILS2000");

        if (this._metiers.size() > 0) {
            ArrayList<JobStat> list = new ArrayList<JobStat>();
            list.addAll(this._metiers.values());
            //packet JS
            SocketManager.GAME_SEND_JS_PACKET(this, list);
            //packet JX
            SocketManager.GAME_SEND_JX_PACKET(this, list);
            //Packet JO (Job Option)
            SocketManager.GAME_SEND_JO_PACKET(this, list);
            GameObject obj = getObjetByPos(Constant.ITEM_POS_ARME);
            if (obj != null)
                for (JobStat sm : list)
                    if (sm.getTemplate().isValidTool(obj.getTemplate().getId()))
                        SocketManager.GAME_SEND_OT_PACKET(account.getGameClient(), sm.getTemplate().getId());
        }

        SocketManager.GAME_SEND_ALIGNEMENT(client, _align);
        SocketManager.GAME_SEND_ADD_CANAL(client, _canaux + "^" + (this.getGroupe() != null ? "@" : ""));
        if (_guildMember != null)
            SocketManager.GAME_SEND_gS_PACKET(this, _guildMember);
        SocketManager.GAME_SEND_ZONE_ALLIGN_STATUT(client);
        SocketManager.GAME_SEND_EMOTE_LIST(this, getCompiledEmote(this.emotes));
        SocketManager.GAME_SEND_RESTRICTIONS(client);
        SocketManager.GAME_SEND_Ow_PACKET(this);
        SocketManager.GAME_SEND_SEE_FRIEND_CONNEXION(client, _showFriendConnection);
        SocketManager.GAME_SEND_SPELL_LIST(this);

        this.account.sendOnline();

        //Messages de bienvenue
        SocketManager.GAME_SEND_Im_PACKET(this, "189");
        if (!this.account.getLastConnectionDate().equals("") && !account.getLastIP().equals(""))
            SocketManager.GAME_SEND_Im_PACKET(this, "0152;" + account.getLastConnectionDate() + "~" + account.getLastIP());

        SocketManager.GAME_SEND_Im_PACKET(this, "0153;" + account.getCurrentIp());

        this.account.setLastIP(this.account.getCurrentIp());

        //Mise a jour du lastConnectionDate
        Date actDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd");
        String jour = dateFormat.format(actDate);
        dateFormat = new SimpleDateFormat("MM");
        String mois = dateFormat.format(actDate);
        dateFormat = new SimpleDateFormat("yyyy");
        String annee = dateFormat.format(actDate);
        dateFormat = new SimpleDateFormat("HH");
        String heure = dateFormat.format(actDate);
        dateFormat = new SimpleDateFormat("mm");
        String min = dateFormat.format(actDate);
        account.setLastConnectionDate(annee + "~" + mois + "~" + jour + "~"
                + heure + "~" + min);
        if (_guildMember != null)
            _guildMember.setLastCo(annee + "~" + mois + "~" + jour + "~"
                    + heure + "~" + min);
        //Affichage des prismes
        World.world.showPrismes(this);
        //Actualisation dans la DB
        Database.getStatics().getAccountData().updateLastConnection(account);
        if (!Config.INSTANCE.getStartMessage().equals(""))//Si le motd est notifi�
            SocketManager.GAME_SEND_MESSAGE(this, Config.INSTANCE.getStartMessage());

        for (GameObject object : this.objects.values()) {
            if (object.getTemplate().getType() == Constant.ITEM_TYPE_FAMILIER) {
                PetEntry p = World.world.getPetsEntry(object.getGuid());
                Pet pets = World.world.getPets(object.getTemplate().getId());

                if (p == null || pets == null) {
                    if (p != null && p.getPdv() > 0)
                        SocketManager.GAME_SEND_Im_PACKET(this, "025");
                    continue;
                }
                if (pets.getType() == 0 || pets.getType() == 1 || pets.getType() == -1)
                    continue;

                p.updatePets(this, Integer.parseInt(pets.getGap().split(",")[1]));
            } else if (object.getTemplate().getId() == 10207) {
                String date = object.getTxtStat().get(Constant.STATS_DATE);
                if (date != null) {
                    if (date.contains("#")) {
                        date = date.split("#")[3];
                    }
                    if (System.currentTimeMillis() - Long.parseLong(date) > 604800000) {
                        object.getTxtStat().clear();
                        object.getTxtStat().putAll(Dopeul.generateStatsTrousseau());
                        SocketManager.GAME_SEND_UPDATE_ITEM(this, object);
                    }
                }
            }
        }

        if (_morphMode)
            setFullMorph(_morphId, true, true);

        if (Config.INSTANCE.getAUTO_REBOOT())
            this.send(Reboot.toStr());
        if(Main.INSTANCE.getFightAsBlocked())
            this.sendServerMessage("You can't fight until new order.");
        EventManager manager = EventManager.getInstance();
        if(manager.getCurrentEvent() != null && manager.getState() == EventManager.State.PROCESSED)
            this.sendMessage("(<b>Infos</b>) : L'événement '" + manager.getCurrentEvent().getName() + "' a démarrer, incrivez-vous à l'aide de <b>.event</b>.");

        //this.checkVote();
        SocketManager.GAME_SEND_SETS_PACKET(this);
        World.world.logger.info("The player " + this.getName() + " come to connect.");

        if(this.needRestat ==1){
            this.Restat_Stats(true);
            this.needRestat = 0;
            Database.getStatics().getPlayerData().update(this);
            SocketManager.GAME_SEND_MESSAGE(this, "Suite à une mise à jour, vos caractéristiques ont été réinitialisées. N'oubliez pas de les replacer", "2997F1");
        }

        if (this.getCurMap().getSubArea() != null) {
            if (this.getCurMap().getSubArea().getId() == 319 || this.getCurMap().getSubArea().getId() == 210)
                TimerWaiter.addNext(() -> Minotoror.sendPacketMap(this), 3, TimeUnit.SECONDS);
            else if (this.getCurMap().getSubArea().getId() == 200)
                TimerWaiter.addNext(() -> PigDragon.sendPacketMap(this), 3, TimeUnit.SECONDS);
        }

        if(this.getCurMap().getId() == 13000 && this.level >= 150){
            this.sendMessage("Vous avez atteint le level maximum pour rester sur cette map");
            this.teleport((short) 7411, 311);
        }

        // permet de géré si le joueurs était en gladia et qu'il n'a pas choisi son tonique et qu'il s'est déconnecté et ou le serveur a rédémarrer
        if(Constant.isInGladiatorDonjon(this.getCurMap().getId())){
            int palier = Constant.getPalierByNewMap(this.getCurMap().getId());
            int toniquePos = 64 + palier;
            // Si il n'avait pas choisi son tonique
            if(this.getObjetByPos(toniquePos) == null ) {
                // Si on lui avait déjà proposé des tonique
                if(this.lastTonicPacket != "")
                    SocketManager.send(this, this.lastTonicPacket);
                else // sinon on en créer des nouveaux
                    SocketManager.GAME_SEND_wr(this,palier);
            }
        }


        if (this.getEnergy() == 0) this.setGhost();
    }

    public void checkVote() {
        String IP = this.getAccount().getLastIP();
        long now = System.currentTimeMillis() / 1000;
        boolean vote = true;
        for (Account account : World.world.getAccounts()) {
            if (account != null && account.getLastVoteIP() != null && !account.getLastVoteIP().equalsIgnoreCase("")) {
                if (account.getLastVoteIP().equalsIgnoreCase(IP)) {
                    if ((account.getHeureVote() + 3600 * 3) > now) {
                        vote = false;
                        break;
                    }
                }
            }
        }

       // if (vote) this.send("Im116;<b>Server</b>~" + Lang.get(this, 13));
    }

    public void SetSeeFriendOnline(boolean bool) {
        _showFriendConnection = bool;
    }

    public void sendGameCreate() {
        this.setOnline(true);
        this.account.setCurrentPlayer(this);

        if (this.account.getGameClient() == null)
            return;

        GameClient client = this.account.getGameClient();
        SocketManager.GAME_SEND_GAME_CREATE(client, this.getName());
        SocketManager.GAME_SEND_STATS_PACKET(this);
        // 1.43.7 : envoi de la shortcut bar d'items (chaque entrée comme OrA<pos>;<id>;)
        String shortcutsPacket = this.parseInventoryShortcutsListPacket();
        if (shortcutsPacket.length() > 0) {
            for (String p : shortcutsPacket.split("\\|")) {
                client.send(p);
            }
        }
        Database.getStatics().getPlayerData().updateLogged(this.id, 1);
        this.verifEquiped();

        if (this.needEndFight() == -1) {
            SocketManager.GAME_SEND_MAPDATA(client, this.curMap.getId(), this.curMap.getDate(), this.curMap.getKey());
            SocketManager.GAME_SEND_MAP_FIGHT_COUNT(client, this.getCurMap());
            if (this.getFight() == null) this.curMap.addPlayer(this);
        } else {
            try {
                client.parsePacket("GI");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public String parseToOa() {
        return "Oa"  + this.getId() + "|"+ getGMStuffString() ;
    }

    public String parseToGM() {
        StringBuilder str = new StringBuilder();
        if (fight == null && curCell != null)// Hors combat
        {
            str.append(curCell.getId()).append(";").append(_orientation).append(";");
            str.append("0").append(";");//FIXME:?
            str.append(this.getId()).append(";").append(this.getName()).append(";").append(this.getClasse());
            str.append((this.get_title() > 0 ? ("," + this.get_title() + ";") : (";")));
            int gfx = gfxId;
            if (this.getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF) != null)
                if (this.getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF).getTemplate().getId() == 10681)
                    gfx = 8037;
            str.append(gfx).append("^").append(_size);//gfxID^size
            if (this.getObjetByPos(Constant.ITEM_POS_PNJ_SUIVEUR) != null)
                str.append(",").append(Constant.getItemIdByMascotteId(this.getObjetByPos(Constant.ITEM_POS_PNJ_SUIVEUR).getTemplate().getId())).append("^100");
            str.append(";").append(this.getSexe()).append(";");
            str.append(_align).append(",");
            str.append("0").append(",");//FIXME:?
            str.append((_showWings ? getGrade() : "0")).append(",");
            str.append(this.getLevel() + this.getId());
            if (_showWings && _deshonor > 0) {
                str.append(",");
                str.append(_deshonor > 0 ? 1 : 0).append(';');
            } else {
                str.append(";");
            }
            int color1 = this.getColor1(), color2 = this.getColor2(), color3 = this.getColor3();
            if (this.getObjetByPos(Constant.ITEM_POS_MALEDICTION) != null)
                if (this.getObjetByPos(Constant.ITEM_POS_MALEDICTION).getTemplate().getId() == 10838) {
                    color1 = 16342021;
                    color2 = 16342021;
                    color3 = 16342021;
                }

            str.append((color1 == -1 ? "-1" : Integer.toHexString(color1))).append(";");
            str.append((color2 == -1 ? "-1" : Integer.toHexString(color2))).append(";");
            str.append((color3 == -1 ? "-1" : Integer.toHexString(color3))).append(";");
            str.append(getGMStuffString()).append(";");
            if (hasEquiped(10054) || hasEquiped(10055) || hasEquiped(10056)
                    || hasEquiped(10058) || hasEquiped(10061)
                    || hasEquiped(10102)) {
                str.append(3).append(";");
                set_title(2);
            } else {
                if (get_title() == 2)
                    set_title(0);
                Group g = this.getGroupe();
                int level = this.getLevel();
                if (g != null)
                    if (!g.isPlayer() || this.get_size() <= 0) // Si c'est un groupe non joueur ou que l'on est invisible on cache l'aura
                        level = 1;
                str.append((level > 99 ? (level > 199 ? (2) : (1)) : (0))).append(";");
            }
            str.append(";");//Emote
            str.append(";");//Emote timer
            if (this._guildMember != null
                    && this._guildMember.getGuild().haveTenMembers())
                str.append(this._guildMember.getGuild().getName()).append(";").append(this._guildMember.getGuild().getEmblem()).append(";");
            else
                str.append(";;");
            if (this.dead == 1 && !this.isGhost)
                str.append("-1");
            str.append(getSpeed()).append(";");//Restriction
            str.append((_onMount && _mount != null ? _mount.getStringColor(parsecolortomount()) : "")).append(";");
            str.append(this.isDead()).append(";");
        }
        return str.toString();
    }

    public String parseToMerchant() {
        StringBuilder str = new StringBuilder();
        str.append(curCell.getId()).append(";");
        str.append(_orientation).append(";");
        str.append("0").append(";");
        str.append(this.getId()).append(";");
        str.append(this.getName()).append(";");
        str.append("-5").append(";");//Merchant identifier
        str.append(gfxId).append("^").append(_size).append(";");
        int color1 = this.getColor1(), color2 = this.getColor2(), color3 = this.getColor3();
        if (this.getObjetByPos(Constant.ITEM_POS_MALEDICTION) != null)
            if (this.getObjetByPos(Constant.ITEM_POS_MALEDICTION).getTemplate().getId() == 10838) {
                color1 = 16342021;
                color2 = 16342021;
                color3 = 16342021;
            }
        str.append((color1 == -1 ? "-1" : Integer.toHexString(color1))).append(";");
        str.append((color2 == -1 ? "-1" : Integer.toHexString(color2))).append(";");
        str.append((color3 == -1 ? "-1" : Integer.toHexString(color3))).append(";");
        str.append(getGMStuffString()).append(";");//acessories
        str.append((_guildMember != null ? _guildMember.getGuild().getName() : "")).append(";");//guildName
        str.append((_guildMember != null ? _guildMember.getGuild().getEmblem() : "")).append(";");//emblem
        str.append("0;");//offlineType
        return str.toString();
    }

    public String getGMStuffString() {
        StringBuilder str = new StringBuilder();

        GameObject object = getObjetByPos(Constant.ITEM_POS_ARME);

        if (object != null)
            str.append(Integer.toHexString(object.getTemplate().getId()));

        str.append(",");

        object = getObjetByPos(Constant.ITEM_POS_COIFFE);

        if (object != null) {
            object.parseStatsString();

            Integer obvi = object.getStats().getEffects().get(970);
            if (obvi == null) {
                String mimibiote = object.getTxtStat().get(915);
                if(mimibiote != null)
                {
                    str.append(Integer.toHexString(Integer.parseInt(mimibiote)));
                }
                else {
                    str.append(Integer.toHexString(object.getTemplate().getId()));
                }
            } else {
                str.append(Integer.toHexString(obvi)).append("~16~").append(object.getObvijevanLook());
            }
        }

        str.append(",");

        object = getObjetByPos(Constant.ITEM_POS_CAPE);

        if (object != null) {
            object.parseStatsString();

            Integer obvi = object.getStats().getEffects().get(970);
            if (obvi == null) {
                String mimibiote = object.getTxtStat().get(915);
                if(mimibiote != null)
                {
                    str.append(Integer.toHexString(Integer.parseInt(mimibiote)));
                }
                else {
                    str.append(Integer.toHexString(object.getTemplate().getId()));
                }
            } else {
                str.append(Integer.toHexString(obvi)).append("~17~").append(object.getObvijevanLook());
            }
        }

        str.append(",");

        object = getObjetByPos(Constant.ITEM_POS_FAMILIER);

        if (object != null) {
            object.parseStatsString();
            String mimibiote = object.getTxtStat().get(915);
            if (mimibiote != null) {
                str.append(Integer.toHexString(Integer.parseInt(mimibiote)));
            } else {
                str.append(Integer.toHexString(object.getTemplate().getId()));
            }
        }

        str.append(",");

        object = getObjetByPos(Constant.ITEM_POS_BOUCLIER);

        if (object != null) {
            object.parseStatsString();
            String mimibiote = object.getTxtStat().get(915);
            if (mimibiote != null) {
                str.append(Integer.toHexString(Integer.parseInt(mimibiote)));
            } else {
                str.append(Integer.toHexString(object.getTemplate().getId()));
            }
        }

        return str.toString();
    }

    public String getAsPacket() {
        refreshStats();
        refreshLife(true);
        StringBuilder ASData = new StringBuilder();
        ASData.append("As").append(xpString(",")).append("|");
        ASData.append(kamas).append("|").append(_capital).append("|").append(_spellPts).append("|");
        ASData.append(_align).append("~").append(_align).append(",").append(_aLvl).append(",").append(getGrade()).append(",").append(_honor).append(",").append(_deshonor).append(",").append((_showWings ? "1" : "0")).append("|");
        int pdv = this.curPdv;
        int pdvMax = this.maxPdv;
        if (fight != null && !fight.isFinish()) {
            Fighter f = fight.getFighterByPerso(this);
            if (f != null) {
                pdv = f.getPdv();
                pdvMax = f.getPdvMax();
            }
        }
        Stats stats = this.getStats(), sutffStats = this.getStuffStats(), donStats = this.getDonsStats(), buffStats = this.getBuffsStats(), totalStats = this.getTotalStats();

        ASData.append(pdv).append(",").append(pdvMax).append("|");
        ASData.append(this.getEnergy()).append(",10000|");
        ASData.append(getInitiative()).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_PROS) + sutffStats.getEffect(EffectConstant.STATS_ADD_PROS) + ((int) Math.ceil(totalStats.getEffect(EffectConstant.STATS_ADD_CHAN) / 10)) + buffStats.getEffect(EffectConstant.STATS_ADD_PROS) + ((int) Math.ceil(buffStats.getEffect(EffectConstant.STATS_ADD_CHAN) / 10))).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_PA)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_PA)).append(",").append(donStats.getEffect(EffectConstant.STATS_ADD_PA)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_PA)).append(",").append(totalStats.getEffect(EffectConstant.STATS_ADD_PA)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_PM)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_PM)).append(",").append(donStats.getEffect(EffectConstant.STATS_ADD_PM)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_PM)).append(",").append(totalStats.getEffect(EffectConstant.STATS_ADD_PM)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_FORC)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_FORC)).append(",").append(donStats.getEffect(EffectConstant.STATS_ADD_FORC)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_FORC)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_VITA)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_VITA)).append(",").append(donStats.getEffect(EffectConstant.STATS_ADD_VITA)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_VITA)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_SAGE)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_SAGE)).append(",").append(donStats.getEffect(EffectConstant.STATS_ADD_SAGE)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_SAGE)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_CHAN)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_CHAN)).append(",").append(donStats.getEffect(EffectConstant.STATS_ADD_CHAN)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_CHAN)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_AGIL)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_AGIL)).append(",").append(donStats.getEffect(EffectConstant.STATS_ADD_AGIL)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_AGIL)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_INTE)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_INTE)).append(",").append(donStats.getEffect(EffectConstant.STATS_ADD_INTE)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_INTE)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_PO)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_PO)).append(",").append(donStats.getEffect(EffectConstant.STATS_ADD_PO)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_PO)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_CREATURE)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_CREATURE)).append(",").append(donStats.getEffect(EffectConstant.STATS_CREATURE)).append(",").append(buffStats.getEffect(EffectConstant.STATS_CREATURE)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_DOMA)+stats.getEffect(EffectConstant.STATS_ADD_DOMA2)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_DOMA)+sutffStats.getEffect(EffectConstant.STATS_ADD_DOMA2)).append(",").append(donStats.getEffect(EffectConstant.STATS_ADD_DOMA)+donStats.getEffect(EffectConstant.STATS_ADD_DOMA2)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_DOMA)+buffStats.getEffect(EffectConstant.STATS_ADD_DOMA2)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_PDOM)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_PDOM)).append(",").append(donStats.getEffect(EffectConstant.STATS_ADD_PDOM)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_PDOM)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_MAITRISE)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_MAITRISE)).append(",").append(donStats.getEffect(EffectConstant.STATS_ADD_MAITRISE)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_MAITRISE)).append("|");//ASData.append("0,0,0,0|");//Maitrise ?
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_PERDOM)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_PERDOM)).append(",").append(donStats.getEffect(EffectConstant.STATS_ADD_PERDOM)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_PERDOM)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_TRAPDOM)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_TRAPDOM)).append(",").append(donStats.getEffect(EffectConstant.STATS_TRAPDOM)).append(",").append(buffStats.getEffect(EffectConstant.STATS_TRAPDOM)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_TRAPPER)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_TRAPPER)).append(",").append(donStats.getEffect(EffectConstant.STATS_TRAPPER)).append(",").append(buffStats.getEffect(EffectConstant.STATS_TRAPPER)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_SOIN)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_SOIN)).append(",").append(donStats.getEffect(EffectConstant.STATS_ADD_SOIN)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_SOIN)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_RETDOM)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_RETDOM)).append(",").append(donStats.getEffect(EffectConstant.STATS_RETDOM)).append(",").append(buffStats.getEffect(EffectConstant.STATS_RETDOM)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_CC)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_CC)).append(",").append(donStats.getEffect(EffectConstant.STATS_ADD_CC)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_CC)).append(",").append(totalStats.getEffect(EffectConstant.STATS_ADD_CC)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_EC)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_EC)).append(",").append(donStats.getEffect(EffectConstant.STATS_ADD_EC)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_EC)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_AFLEE)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_AFLEE)).append(",").append(0).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_AFLEE)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_AFLEE)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_MFLEE)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_MFLEE)).append(",").append(0).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_MFLEE)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_MFLEE)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_R_NEU)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_R_NEU)).append(",").append(0).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_R_NEU)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_R_NEU)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_RP_NEU)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_RP_NEU)).append(",").append(0).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_RP_NEU)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_RP_NEU)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_R_PVP_NEU)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_R_PVP_NEU)).append(",").append(0).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_R_PVP_NEU)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_R_PVP_NEU)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_RP_PVP_NEU)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_RP_PVP_NEU)).append(",").append(0).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_RP_PVP_NEU)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_RP_PVP_NEU)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_R_TER)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_R_TER)).append(",").append(0).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_R_TER)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_R_TER)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_RP_TER)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_RP_TER)).append(",").append(0).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_RP_TER)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_RP_TER)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_R_PVP_TER)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_R_PVP_TER)).append(",").append(0).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_R_PVP_TER)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_R_PVP_TER)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_RP_PVP_TER)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_RP_PVP_TER)).append(",").append(0).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_RP_PVP_TER)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_RP_PVP_TER)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_R_EAU)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_R_EAU)).append(",").append(0).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_R_EAU)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_R_EAU)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_RP_EAU)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_RP_EAU)).append(",").append(0).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_RP_EAU)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_RP_EAU)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_R_PVP_EAU)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_R_PVP_EAU)).append(",").append(0).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_R_PVP_EAU)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_R_PVP_EAU)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_RP_PVP_EAU)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_RP_PVP_EAU)).append(",").append(0).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_RP_PVP_EAU)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_RP_PVP_EAU)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_R_AIR)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_R_AIR)).append(",").append(0).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_R_AIR)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_R_AIR)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_RP_AIR)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_RP_AIR)).append(",").append(0).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_RP_AIR)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_RP_AIR)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_R_PVP_AIR)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_R_PVP_AIR)).append(",").append(0).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_R_PVP_AIR)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_R_PVP_AIR)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_RP_PVP_AIR)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_RP_PVP_AIR)).append(",").append(0).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_RP_PVP_AIR)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_RP_PVP_AIR)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_R_FEU)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_R_FEU)).append(",").append(0).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_R_FEU)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_R_FEU)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_RP_FEU)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_RP_FEU)).append(",").append(0).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_RP_FEU)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_RP_FEU)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_R_PVP_FEU)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_R_PVP_FEU)).append(",").append(0).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_R_PVP_FEU)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_R_PVP_FEU)).append("|");
        ASData.append(stats.getEffect(EffectConstant.STATS_ADD_RP_PVP_FEU)).append(",").append(sutffStats.getEffect(EffectConstant.STATS_ADD_RP_PVP_FEU)).append(",").append(0).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_RP_PVP_FEU)).append(",").append(buffStats.getEffect(EffectConstant.STATS_ADD_RP_PVP_FEU)).append("|");
        return ASData.toString();
    }

    public int getGrade() {
        if (_align == Constant.ALIGNEMENT_NEUTRE)
            return 0;
        if (_honor >= 17500)
            return 10;
        for (int n = 1; n <= 10; n++)
            if (_honor < World.world.getExpLevel(n).pvp)
                return n - 1;
        return 0;
    }

    public String stringStatsComplemento()
    {
        refreshStats();
        refreshLife(true);
        StringBuilder ASData = new StringBuilder();
        ASData.append("As").append(xpString(",")).append("|");
        ASData.append(kamas).append("|").append(_capital).append("|").append(_spellPts).append("|");
        ASData.append(_align).append("~").append(_align).append(",").append(_aLvl).append(",").append(getGrade()).append(",").append(_honor).append(",").append(_deshonor).append(",").append((_showWings ? "1" : "0")).append("|");
        int pdv = this.curPdv;
        int pdvMax = this.maxPdv;
        if (fight != null && !fight.isFinish()) {
            Fighter f = fight.getFighterByPerso(this);
            if (f != null) {
                pdv = f.getPdv();
                pdvMax = f.getPdvMax();
            }
        }
        ASData.append(pdv).append(",").append(pdvMax).append("|");
        ASData.append(this.getEnergy()).append(",10000|");
        ASData.append(getInitiative()).append("|");
        return ASData.toString();
    }

    public String stringStats() {
        final StringBuilder str = new StringBuilder();
        str.append(stringStatsComplemento());
        int base = 0, equipement = 0, bendMald = 0, buff = 0, total = 0;
        Stats stats = this.getStats();
        Stats stuffStats = this.getStuffStats();
        Stats donStats = this.getDonsStats();
        Stats buffStats = this.getBuffsStats();
        Stats totalStats = this.getTotalStats();
        total = (stats.getEffect(EffectConstant.STATS_ADD_PROS) + this.getStuffStats().getEffect(EffectConstant.STATS_ADD_PROS) + (int)(Math.ceil(totalStats.getEffect(EffectConstant.STATS_ADD_CHAN) / 10)) + buffStats.getEffect(EffectConstant.STATS_ADD_PROS) + (int)Math.ceil(buffStats.getEffect(EffectConstant.STATS_ADD_CHAN) / 10));
        // prospeccion
        str.append(total).append("|");
        final int[] statsArray = {111, 128, 118, 125, 124, 123, 119, 126, 117, 182, 112, 142, 165, 138, 178, 225, 226, 220, 115,
                122, 160, 161, 244, 214, 264, 254, 240, 210, 260, 250, 241, 211, 261, 251, 242, 212, 262, 252, 243, 213, 263, 253};
        for (final int s : statsArray) {
                base = stats.getEffect(s);
                equipement = stuffStats.getEffect(s);
                bendMald = donStats.getEffect(s);
                buff = buffStats.getEffect(s);
                total = totalStats.getEffect(s);

            str.append(base).append(",").append(equipement).append(",").append(bendMald).append(",").append(buff).append(",").append(total).append("|");
        }
        return str.toString();
    }

    public String xpString(String c) {
        if (!_morphMode) {
            return World.world.getPersoXpMin(this.getLevel()) + c + this.getExp() + c + World.world.getPersoXpMax(this.getLevel());
        } else {
            if (this.getObjetByPos(Constant.ITEM_POS_ARME) != null)
                if (Constant.isIncarnationWeapon(this.getObjetByPos(Constant.ITEM_POS_ARME).getTemplate().getId()))
                    if (this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().get(Constant.ERR_STATS_XP) != null)
                        return this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().get(Constant.ERR_STATS_XP)
                                + c
                                + World.world.getBanditsXpMin(this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().get(Constant.STATS_NIVEAU))
                                + c
                                + World.world.getBanditsXpMax(this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().get(Constant.STATS_NIVEAU));
        }
        return 1 + c + 1 + c + 1;
    }

    public int emoteActive() {
        return _emoteActive;
    }

    public void setEmoteActive(int emoteActive) {
        this._emoteActive = emoteActive;
    }

    public Stats getStuffStats() {
        if (this.useStats) return new Stats();

        Stats stats = new Stats(false, null);
        ArrayList<Integer> itemSetApplied = new ArrayList<>();

        for (GameObject gameObject : this.getEquippedObjects()) {
            byte position = (byte) gameObject.getPosition();
            if (position != Constant.ITEM_POS_NO_EQUIPED) {
                if (position >= 35 && position <= 48)
                    continue;

                stats = Stats.cumulStat(stats, gameObject.getStats());
                int id = gameObject.getTemplate().getPanoId();

                if (id > 0 && !itemSetApplied.contains(id)) {
                    itemSetApplied.add(id);
                    ObjectSet objectSet = World.world.getItemSet(id);
                    if (objectSet != null) {
                        stats = Stats.cumulStat(stats, objectSet.getBonusStatByItemNumb(this.getNumbEquipedItemOfPanoplie(id)));

                        if(objectSet.getId() ==166){
                            int NumOfPrimal = 0;
                            for (GameObject Dofus : this.getEquippedObjects()) {
                                if (Dofus.getTemplate().getType() == Constant.ITEM_TYPE_DOFUS){
                                    if(Dofus.getRarity() ==5)
                                        NumOfPrimal++;
                                }
                            }
                            if(NumOfPrimal >= 3){
                                stats.addOneStat(EffectConstant.STATS_ADD_PM,1);
                                if(NumOfPrimal >= 6){
                                    stats.addOneStat(EffectConstant.STATS_ADD_PA,1);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (this._mount != null && this._onMount)
            stats = Stats.cumulStat(stats, this._mount.getStats());

        return stats;
    }

    public Stats getBuffsStats() {
        Stats stats = new Stats(false, null);
        if (this.fight != null)
            if (this.fight.getFighterByPerso(this) != null)
                for (Effect entry : this.fight.getFighterByPerso(this).getFightBuff())
                    stats.addOneStat(entry.getEffectID(), entry.getFixvalue());

        for (Entry<Integer, Effect> entry : buffs.entrySet())
            stats.addOneStat(entry.getValue().getEffectID(), entry.getValue().getFixvalue());
        return stats;
    }

    public int get_orientation() {
        return _orientation;
    }

    public void set_orientation(int _orientation) {
        this._orientation = _orientation;
    }

    public int getInitiative() {
        if (!useStats ) {
            if(!this.isInvocControlable && !Constant.isInGladiatorDonjon(this.getCurMap().getId()) && this.getCurMap().getId()!=12277) {
                int fact = 4;
                int maxPdv = this.maxPdv - 55;
                int curPdv = this.curPdv - 55;
                if (this.getClasse() == Constant.CLASS_SACRIEUR)
                    fact = 8;
                double coef = maxPdv / fact;

                coef += getStuffStats().getEffect(EffectConstant.STATS_ADD_INIT);
                coef += getTotalStats().getEffect(EffectConstant.STATS_ADD_AGIL);
                coef += getTotalStats().getEffect(EffectConstant.STATS_ADD_CHAN);
                coef += getTotalStats().getEffect(EffectConstant.STATS_ADD_INTE);
                coef += getTotalStats().getEffect(EffectConstant.STATS_ADD_FORC);

                int init = 1;
                if (maxPdv != 0)
                    init = (int) (coef * ((double) curPdv / (double) maxPdv));
                if (init < 0)
                    init = 0;
                return init;
            }
            else{
                return this.initiative;
            }
        } else {
            return this.initiative;
        }
    }

    public Stats getTotalStats() {
        Stats total = new Stats(false, null);
        if (!useStats) {
            total = Stats.cumulStat(total, this.getStats());
            total = Stats.cumulStat(total, this.getStuffStats());
            total = Stats.cumulStat(total, this.getDonsStats());
            if (fight != null)
                total = Stats.cumulStat(total, this.getBuffsStats());
        } else {
            return newStatsMorph();
        }
        return total;
    }

    public Stats getDonsStats() {
        Stats stats = new Stats(false, null);
        return stats;
    }

    public Stats newStatsMorph() {
        Stats stats = new Stats();
        stats.addOneStat(EffectConstant.STATS_ADD_PA, this.pa);
        stats.addOneStat(EffectConstant.STATS_ADD_PM, this.pm);
        stats.addOneStat(EffectConstant.STATS_ADD_VITA, this.vitalite);
        stats.addOneStat(EffectConstant.STATS_ADD_SAGE, this.sagesse);
        stats.addOneStat(EffectConstant.STATS_ADD_FORC, this.terre);
        stats.addOneStat(EffectConstant.STATS_ADD_INTE, this.feu);
        stats.addOneStat(EffectConstant.STATS_ADD_CHAN, this.eau);
        stats.addOneStat(EffectConstant.STATS_ADD_AGIL, this.air);
        stats.addOneStat(EffectConstant.STATS_ADD_INIT, this.initiative);
        stats.addOneStat(EffectConstant.STATS_ADD_PROS, 100);
        stats.addOneStat(EffectConstant.STATS_CREATURE, 1);
        this.useCac = false;
        return stats;
    }

    public int getPodUsed() {
        int pod = 0;

        for (Entry<Long, GameObject> entry : objects.entrySet()) {
            pod += entry.getValue().getTemplate().getPod()
                    * entry.getValue().getQuantity();
        }

        pod += parseStoreItemsListPods();
        return pod;
    }

    public int getMaxPod() {
        Stats total = new Stats(false, null);
        total = Stats.cumulStat(total, this.getStats());
        total = Stats.cumulStat(total, this.getStuffStats());
        total = Stats.cumulStat(total, this.getDonsStats());
        int pods = total.getEffect(EffectConstant.STATS_ADD_PODS);
        pods += total.getEffect(EffectConstant.STATS_ADD_FORC) * 15;
        for (JobStat SM : _metiers.values()) {
            pods += SM.get_lvl() * Config.INSTANCE.getRATE_JOB();
            if (SM.get_lvl() == 100)
                pods += 1000;
        }
        if (pods < 1000)
            pods = 1000;
        return pods+5000;
    }

    public void refreshLife(boolean refresh) {
        if (get_isClone())
            return;
        long time = (System.currentTimeMillis() - regenTime);
        regenTime = System.currentTimeMillis();
        if (fight != null)
            return;
        if (regenRate == 0)
            return;
        if (this.curPdv > this.maxPdv) {
            this.curPdv = this.maxPdv - 1;
            if (!refresh)
                SocketManager.GAME_SEND_STATS_PACKET(this);
            return;
        }

        int diff = (int) time / regenRate;
        if (diff >= 10 && this.curPdv < this.maxPdv && regenRate == 500)
            SocketManager.send(this, "ILF" + diff);

        setPdv(this.curPdv + diff);
    }

    public byte get_align() {
        return _align;
    }

    public int get_pdvper() {
        refreshLife(false);
        int pdvper = 100;
        pdvper = (100 * this.curPdv) / this.maxPdv;
        if (pdvper > 100)
            return 100;
        return pdvper;
    }

    public void useSmiley(String str) {
        try {
            int id = Integer.parseInt(str);
            GameMap map = curMap;
            if (fight == null)
                SocketManager.GAME_SEND_EMOTICONE_TO_MAP(map, this.getId(), id);
            else
                SocketManager.GAME_SEND_EMOTICONE_TO_FIGHT(fight, 7, this.getId(), id);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public void boostStatFixedCount(int stat, int countVal) {
        for (int i = 0; i < countVal; i++) {
            int value = 0;
            switch (stat) {
                case 10://Force
                    value = this.getStats().getEffect(EffectConstant.STATS_ADD_FORC);
                    break;
                case 13://Chance
                    value = this.getStats().getEffect(EffectConstant.STATS_ADD_CHAN);
                    break;
                case 14://Agilit�
                    value = this.getStats().getEffect(EffectConstant.STATS_ADD_AGIL);
                    break;
                case 15://Intelligence
                    value = this.getStats().getEffect(EffectConstant.STATS_ADD_INTE);
                    break;
            }
            int cout = Constant.getReqPtsToBoostStatsByClass(this.getClasse(), stat, value);
            if (cout <= _capital) {
                switch (stat) {
                    case 11://Vita
                        if (this.getClasse() != Constant.CLASS_SACRIEUR)
                            this.getStats().addOneStat(EffectConstant.STATS_ADD_VITA, 1);
                        else
                            this.getStats().addOneStat(EffectConstant.STATS_ADD_VITA, 2);
                        break;
                    case 12://Sage
                        this.getStats().addOneStat(EffectConstant.STATS_ADD_SAGE, 1);
                        break;
                    case 10://Force
                        this.getStats().addOneStat(EffectConstant.STATS_ADD_FORC, 1);
                        break;
                    case 13://Chance
                        this.getStats().addOneStat(EffectConstant.STATS_ADD_CHAN, 1);
                        break;
                    case 14://Agilit�
                        this.getStats().addOneStat(EffectConstant.STATS_ADD_AGIL, 1);
                        break;
                    case 15://Intelligence
                        this.getStats().addOneStat(EffectConstant.STATS_ADD_INTE, 1);
                        break;
                    default:
                        return;
                }
                _capital -= cout;
            }
        }
        SocketManager.GAME_SEND_STATS_PACKET(this);
        Database.getStatics().getPlayerData().update(this);
    }

    public boolean isMuted() {
        return account.isMuted();
    }

    public String parseObjetsToDB() {
        StringBuilder str = new StringBuilder();
        if (objects.isEmpty())
            return "";
        for (Entry<Long, GameObject> entry : objects.entrySet()) {
            GameObject obj = entry.getValue();
            if (obj == null)
                continue;
            str.append(obj.getGuid()).append("|");
        }

        return str.toString();
    }

    public boolean addObjet(GameObject newObj, boolean stackIfSimilar) {
        if(stackIfSimilar) {
            // Pas sur ca.
            if(newObj.getPosition() != -1 && this.getObjetByPos(newObj.getPosition()) != null)
                newObj.setPosition(-1);

            for (Entry<Long, GameObject> entry : objects.entrySet()) {
                GameObject obj = entry.getValue();
                if (World.world.getConditionManager().stackIfSimilar2(obj, newObj, stackIfSimilar)) {
                    obj.setQuantity(obj.getQuantity() + newObj.getQuantity());//On ajoute QUA item a la quantitê de l'objet existant
                    if (isOnline)
                        SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, obj);
                    Database.getStatics().getObjectData().update(obj);
                    return false;
                }
            }
        }

        if(newObj.getGuid() == -1)
            newObj.setId();

        objects.put(newObj.getGuid(), newObj);
        Database.getStatics().getPlayerData().update(this);
        SocketManager.GAME_SEND_OAKO_PACKET(this, newObj);
        return true;
    }

    public void addObjet(GameObject newObj) {
        objects.put(newObj.getGuid(), newObj);
        Database.getStatics().getPlayerData().updateInventory(this);
        SocketManager.GAME_SEND_OAKO_PACKET(this, newObj);
    }


    public void addObject(GameObject newObj, boolean display) {
        this.objects.put(newObj.getGuid(), newObj);
        if(display) {
            SocketManager.GAME_SEND_OAKO_PACKET(this, newObj);
        }
    }

    public Map<Long, GameObject> getItems() {
        return objects;
    }

    public String parseItemToASK() {

        StringBuilder str = new StringBuilder();
        if (objects.isEmpty())
            return "";
        for (GameObject obj : objects.values()) {
            str.append(obj.parseItem());
        }
        return str.toString();
    }

    public String parseItemEquippedToASK() {
        List<GameObject> equipedObject = this.getEquippedObjects();
        StringBuilder str = new StringBuilder();
        if (equipedObject.isEmpty())
            return "";
        for (GameObject obj : equipedObject) {
            str.append(obj.parseItem());
        }
        return str.toString();
    }

    public String getItemsIDSplitByChar(String splitter) {
        StringBuilder str = new StringBuilder();
        if (objects.isEmpty())
            return "";
        for (long entry : objects.keySet()) {
            if (str.length() != 0)
                str.append(splitter);
            str.append(entry);
        }

        return str.toString();
    }

    public String getStoreItemsIDSplitByChar(String splitter) {
        StringBuilder str = new StringBuilder();
        if (_storeItems.isEmpty())
            return "";
        for (long entry : _storeItems.keySet()) {
            if (str.length() != 0)
                str.append(splitter);
            str.append(entry);
        }
        return str.toString();
    }

    public boolean hasItemGuid(long guid) {
        return objects.get(guid) != null && objects.get(guid).getQuantity() > 0;
    }

    public void sellItem(long guid, int qua) {
        if (qua <= 0)
            return;

        if (objects.get(guid).getQuantity() < qua)//Si il a moins d'item que ce qu'on veut Del
            qua = objects.get(guid).getQuantity();

        int prix = qua * (objects.get(guid).getTemplate().getPrice() / 10);//Calcul du prix de vente (prix d'achat/10)
        int newQua = objects.get(guid).getQuantity() - qua;

        if (newQua <= 0)//Ne devrait pas etre <0, S'il n'y a plus d'item apres la vente
        {
            objects.remove(guid);
            World.world.removeGameObject(guid);
            Database.getStatics().getObjectData().delete(guid);
            SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, guid);
        } else
        //S'il reste des items apres la vente
        {
            objects.get(guid).setQuantity(newQua);
            SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, objects.get(guid));
        }
        kamas = kamas + prix;

        SocketManager.GAME_SEND_STATS_PACKET(this);
        SocketManager.GAME_SEND_Ow_PACKET(this);
        SocketManager.GAME_SEND_ESK_PACKEt(this);
    }

    public void removeItem(Long guid) {
        objects.remove(guid);
    }

    public void removeItem(long guid, int nombre, boolean send,
                           boolean deleteFromWorld) {
        GameObject obj = objects.get(guid);

        if(obj == null) return;

        if (nombre > obj.getQuantity())
            nombre = obj.getQuantity();

        if (obj.getQuantity() >= nombre) {
            int newQua = obj.getQuantity() - nombre;
            if (newQua > 0) {
                obj.setQuantity(newQua);
                if (send && isOnline)
                    SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, obj);
            } else {
                //on supprime de l'inventaire et du Monde
                objects.remove(obj.getGuid());
                if (deleteFromWorld)
                    World.world.removeGameObject(obj.getGuid());
                //on envoie le packet si connect�
                if (send && isOnline)
                    SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, obj.getGuid());
            }
        }

        SocketManager.GAME_SEND_Ow_PACKET(this);
    }

    public void deleteItem(long guid) {
        objects.remove(guid);
        World.world.removeGameObject(guid);
    }

    public GameObject getObjetByPos(int pos) {
        if (pos == Constant.ITEM_POS_NO_EQUIPED)
            return null;

        for (GameObject gameObject : this.objects.values()) {
            if (gameObject.getPosition() == pos && pos == Constant.ITEM_POS_FAMILIER) {
                if (gameObject.getTxtStat().isEmpty()) return null;
                else if (World.world.getPetsEntry(gameObject.getGuid()) == null) return null;
            }
            if (gameObject.getPosition() == pos) return gameObject;
        }

        return null;
    }

    //TODO: Delete s'te fonction.
    public GameObject getObjetByPos2(int pos) {
        if (pos == Constant.ITEM_POS_NO_EQUIPED)
            return null;

        for (Entry<Long, GameObject> entry : objects.entrySet()) {
            GameObject obj = entry.getValue();

            if (obj.getPosition() == pos)
                return obj;
        }
        return null;
    }

    public GameObject getObjetByPos3(int pos,List<GameObject> test) {
        if (pos == Constant.ITEM_POS_NO_EQUIPED)
            return null;

        for (GameObject entry : test) {
            if (entry.getPosition() == pos)
                return entry;
        }
        return null;
    }

    public void refreshStats() {
        double actPdvPer = (100 * (double) this.curPdv) / (double) this.maxPdv;
        if (!useStats)
            this.maxPdv = (this.getLevel() - 1) * 5 + 50 + getTotalStats().getEffect(EffectConstant.STATS_ADD_VITA);
        if(_morphMode && (Constant.isInGladiatorDonjon(this.curMap.getId()) || this.getCurMap().getId() == 12277))
            this.maxPdv = getTotalStats().getEffect(EffectConstant.STATS_ADD_VITA);

        this.curPdv = (int) Math.round(maxPdv * actPdvPer / 100);
    }

    public boolean levelUp(boolean send, boolean addXp) {
        if (this.getLevel() == World.world.getExpLevelSize())
            return false;
        this.level++;
        _capital += 5;
        _spellPts++;
        this.maxPdv += 5;
        this.setPdv(this.getMaxPdv());
        if (this.getLevel() == 100)
            this.getStats().addOneStat(EffectConstant.STATS_ADD_PA, 1);
        if (this.getLevel() == 200)
            this.getStats().addOneStat(EffectConstant.STATS_ADD_PM, 1);
        Constant.onLevelUpSpells(this, this.getLevel());
        if (addXp)
            this.exp = World.world.getExpLevel(this.getLevel()).perso;
        if (send && isOnline) {
            SocketManager.GAME_SEND_STATS_PACKET(this);
            SocketManager.GAME_SEND_SPELL_LIST(this);
        }
        if(this.getCurMap().getId() == 13000 && this.level >= 150){
            this.sendMessage("Vous avez atteint le level maximum pour rester sur cette map");
            this.teleport((short) 7411, 311);
        }

        return true;
    }
    public boolean NerfSpell(int spellID)
    {
        if(getFight() != null)
            return false;
        int antNivel = getSortStatBySortIfHas(spellID).getLevel();
        if (antNivel <= 1)
            return false;
        if (learnSpell(spellID, (antNivel-1), true, false, false)) {
            int total = 0;
            for (int i = (antNivel-1); i < antNivel; i++)
                total += i;
            _spellPts += total;
            Database.getStatics().getPlayerData().update(this);
            SocketManager.GAME_SEND_STATS_PACKET(this);
            SocketManager.GAME_SEND_SPELL_LIST(this);
            return true;
        }
        return false;
    }

    public String stringStats2() {
        final StringBuilder str = new StringBuilder("Ak");
        str.append(stringStatsComplement());
        return str.toString();
    }

    public String stringExperience(final String c) {
        return World.world.getExpLevel(this.level).perso + c + this.exp + c + World.world.getExpLevel(level + 1).perso ;
    }
    public String stringStatsComplement() {
        final StringBuilder str = new StringBuilder();
        str.append(stringExperience(",")).append("|");
        str.append(kamas).append("|");
        if (_morphMode != true) {
            str.append("0|0|");
        } else {
            str.append(_capital).append("|").append(_spellPts).append("|");
        }
        str.append(_align).append("~");
        str.append(_align).append(",");// fake alineacion, si son diferentes se activa haveFakeAlignment
        str.append(_aLvl).append(",");// orden alineacion
        str.append(getGrade()).append(",");// nValue
        str.append(_honor).append(",");// nHonour
        str.append(_deshonor).append(",");// nDisgrace
        str.append(is_showWings() ? "1" : "0").append("|");// bEnabled
        int PDV = getCurPdv();
        int PDVMax = getMaxPdv();
        if (fight != null && fight.getFighterByPerso(this) != null) {
            final Fighter luchador = fight.getFighterByPerso(this);
            if (luchador != null) {
                PDV = luchador.getPdv();
                PDVMax = luchador.getPdvMax();
            }
        }
        str.append(PDV).append(",").append(PDVMax).append("|");
        str.append(energy).append(",10000|");
        return str.toString();
    }

    public boolean addXp(long winxp) {
        boolean up = false;
        this.exp += winxp;
        while (this.getExp() >= World.world.getPersoXpMax(this.getLevel()) && this.getLevel() < World.world.getExpLevelSize()) {
            up = levelUp(true, false);
        }
        if (isOnline) {
            if (up) {
                SocketManager.GAME_SEND_NEW_LVL_PACKET(account.getGameClient(), this.getLevel());
            }
            //SocketManager.ENVIAR_Ak_KAMAS_PDV_EXP_PJ(this);
            SocketManager.GAME_SEND_STATS_PACKET(this);
        }
        return up;
    }

    public boolean levelUpIncarnations(boolean send, boolean addXp) {
        int level = this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().get(Constant.STATS_NIVEAU);

        if (level == 50)
            return false;

        level++;
        this.setPdv(this.getMaxPdv());
        SocketManager.GAME_SEND_STATS_PACKET(this);

        switch (level) {
            case 10:
            case 20:
            case 30:
            case 40:
            case 50:
                boostSpellIncarnation();
                break;
        }

        if (send && isOnline) {
            SocketManager.GAME_SEND_STATS_PACKET(this);
            SocketManager.GAME_SEND_SPELL_LIST(this);
        }

        this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().clear();
        this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().put(Constant.STATS_NIVEAU, level);
        this.getObjetByPos(Constant.ITEM_POS_ARME);
        SocketManager.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(this, this.getObjetByPos(Constant.ITEM_POS_ARME));
        return true;
    }

    public boolean addXpIncarnations(long winxp) {
        boolean up = false;
        int level = this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().get(Constant.STATS_NIVEAU);
        long exp = this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().get(Constant.ERR_STATS_XP);
        exp += winxp;

        if (Constant.isBanditsWeapon(this.getObjetByPos(Constant.ITEM_POS_ARME).getTemplate().getId())) {
            while (exp >= World.world.getBanditsXpMax(level) && level < 50) {
                up = levelUpIncarnations(true, false);
                level = this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().get(Constant.STATS_NIVEAU);
            }
        } else if (Constant.isTourmenteurWeapon(this.getObjetByPos(Constant.ITEM_POS_ARME).getTemplate().getId())) {
            while (exp >= World.world.getTourmenteursXpMax(level) && level < 50) {
                up = levelUpIncarnations(true, false);
                level = this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().get(Constant.STATS_NIVEAU);
            }
        }
        if (isOnline)
            SocketManager.GAME_SEND_STATS_PACKET(this);
        level = this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().get(Constant.STATS_NIVEAU);
        this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().clear();
        this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().put(Constant.STATS_NIVEAU, level);
        this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().put(Constant.ERR_STATS_XP, (int) exp);
        return up;
    }

    public void banAccount(){
        int days = 0;
        if (this == null) {
            if (Logging.USE_LOG) {
                Logging.getInstance().write("BanFail", "Le joueur n'a pas été trouvé pour ban UseFaille");
            }
            return;
        }

        if (this.getAccount() == null)
            Database.getStatics().getAccountData().load(this.getAccID());

        if (this.getAccount() == null) {
            if (Logging.USE_LOG) {
                Logging.getInstance().write("BanFail", "Le compte du joueur n'a pas été trouvé pour ban UseFaille");
            }
            return;
        }

        this.getAccount().setBanned(true);
        Database.getStatics().getAccountData().updateBannedTime(this.getAccount(), (System.currentTimeMillis() + 86400000) * days);

        if (this.getGameClient() != null)
            this.getGameClient().kick();

    }

    public void addKamas(long l) {
        // Si retrait d'argent
        if(l < 0 ){
            // Si le joueur n'avait pas l'argent qu'il a essayer de se faire retirer : USE FAILLE BAN
            if( ( kamas + l) < 0 ) {
                StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                String str = "";
                int i = 0;
                for (StackTraceElement caller : stackTrace ) {
                        i++;
                        str += "["+ i +"] :" + "De " + caller.getMethodName() + "/" + caller.getClassName() + " && ";
                        if(i > 4)
                            break;
                }
                World.sendWebhookMessage(Config.INSTANCE.getDISCORD_CHANNEL_FAILLE(),"BAN : Tentative de retrait de "+l+" kamas alors qu'il n'en n'avait que "+this.getKamas() +" : Trace" + str, this);
                this.banAccount();
                kamas = 0;
            }
            else{
                kamas += l;
            }
        }
        // Si ajout d'argent
        else{
            kamas += l;
        }
    }

    public GameObject getSimilarItem(GameObject exGameObject) {
        if (exGameObject.getTemplate().getId() == 8378)
            return null;

        for (GameObject gameObject : this.objects.values()) {
            if (World.world.getConditionManager().stackIfSimilar2(gameObject, exGameObject, true))
                return gameObject;
        }

        return null;
    }

    public int learnJob(Job m) {
        for (Entry<Integer, JobStat> entry : _metiers.entrySet()) {
            if (entry.getValue().getTemplate().getId() == m.getId())//Si le joueur a d�j� le m�tier
                return -1;
        }
        int pos = 0;

        //Position en fonction du métier
        switch(m.getId()){
            case 2:
                pos = 0;
                break;
            case 24:
                pos = 2;
                break;
            case 28:
                pos = 3;
                break;
            case 25:
                pos = 4;
                break;
            case 36:
                pos = 5;
                break;
            case 58:
                pos = 6;
                break;
            case 41:
                pos = 7;
                break;
            case 56:
                pos = 8;
                break;
            case 26:
                pos = 9;
                break;
            case 15:
                pos = 10;
                break;
            case 16:
                pos = 11;
                break;
            case 27:
                pos = 12;
                break;
            case 11:
                pos = 13;
                break;
            case 14:
                pos = 14;
                break;
            case 17:
                pos = 15;
                break;
            case 20:
                pos = 16;
                break;
            case 31:
                pos = 17;
                break;
            case 13:
                pos = 18;
                break;
            case 18:
                pos = 19;
                break;
            case 19:
                pos = 20;
                break;
            case 60:
                pos = 21;
                break;
            case 65:
                pos = 22;
                break;
            case 62:
                pos = 23;
                break;
            case 63:
                pos = 24;
                break;
            case 64:
                pos = 25;
                break;
            case 43:
                pos = 26;
                break;
            case 44:
                pos = 27;
                break;
            case 45:
                pos = 28;
                break;
            case 46:
                pos = 29;
                break;
            case 47:
                pos = 30;
                break;
            case 48:
                pos = 31;
                break;
            case 49:
                pos = 32;
                break;
            case 50:
                pos = 33;
                break;
        }

        JobStat sm = new JobStat(pos, m, 1, 0);
        _metiers.put(pos, sm);//On apprend le m�tier lvl 1 avec 0 xp
        if (isOnline) {
            //on cr�er la listes des JobStats a envoyer (Seulement celle ci)
            ArrayList<JobStat> list = new ArrayList<JobStat>();
            list.add(sm);

            SocketManager.GAME_SEND_Im_PACKET(this, "02;" + m.getId());
            //packet JS
            SocketManager.GAME_SEND_JS_PACKET(this, list);
            //packet JX
            SocketManager.GAME_SEND_JX_PACKET(this, list);
            //Packet JO (Job Option)
            SocketManager.GAME_SEND_JO_PACKET(this, list);

            //GameObject obj = getObjetByPos(Constant.ITEM_POS_ARME);
            //if (obj != null)
                //if (sm.getTemplate().isValidTool(obj.getTemplate().getId()))
                    SocketManager.GAME_SEND_OT_PACKET(account.getGameClient(), m.getId());
        }
        return pos;
    }

    public int getPosByJob(int jobID){
        int pos =0;
        switch(jobID){
            case 2:
                pos = 0;
                break;
            case 24:
                pos = 2;
                break;
            case 28:
                pos = 3;
                break;
            case 25:
                pos = 4;
                break;
            case 36:
                pos = 5;
                break;
            case 58:
                pos = 6;
                break;
            case 41:
                pos = 7;
                break;
            case 56:
                pos = 8;
                break;
            case 26:
                pos = 9;
                break;
            case 15:
                pos = 10;
                break;
            case 16:
                pos = 11;
                break;
            case 27:
                pos = 12;
                break;
            case 11:
                pos = 13;
                break;
            case 14:
                pos = 14;
                break;
            case 17:
                pos = 15;
                break;
            case 20:
                pos = 16;
                break;
            case 31:
                pos = 17;
                break;
            case 13:
                pos = 18;
                break;
            case 18:
                pos = 19;
                break;
            case 19:
                pos = 20;
                break;
            case 60:
                pos = 21;
                break;
            case 65:
                pos = 22;
                break;
            case 62:
                pos = 23;
                break;
            case 63:
                pos = 24;
                break;
            case 64:
                pos = 25;
                break;
            case 43:
                pos = 26;
                break;
            case 44:
                pos = 27;
                break;
            case 45:
                pos = 28;
                break;
            case 46:
                pos = 29;
                break;
            case 47:
                pos = 30;
                break;
            case 48:
                pos = 31;
                break;
            case 49:
                pos = 32;
                break;
            case 50:
                pos = 33;
                break;
        }
        return pos;
    }

    public void unlearnJob(int m) {
        _metiers.remove(Integer.valueOf(m));
    }

    public void unequipedObjet(GameObject o) {
        o.setPosition(Constant.ITEM_POS_NO_EQUIPED);
        ObjectTemplate oTpl = o.getTemplate();
        int idSetExObj = oTpl.getPanoId();
        if ((idSetExObj >= 81 && idSetExObj <= 92)
                || (idSetExObj >= 201 && idSetExObj <= 212) ) {
            String[] stats = oTpl.getStrTemplate().split(",");
            for (String stat : stats) {
                String[] val = stat.split("#");
                String modifi = Integer.parseInt(val[0], 16) + ";"
                        + Integer.parseInt(val[1], 16) + ";0";
                SocketManager.SEND_SB_SPELL_BOOST(this, modifi);
                this.removeObjectClassSpell(Integer.parseInt(val[1], 16));
            }
        }
        SocketManager.GAME_SEND_OBJET_MOVE_PACKET(this, o);
        if (oTpl.getPanoId() > 0)
            SocketManager.GAME_SEND_OS_PACKET(this, oTpl.getPanoId());
    }

    public void verifEquiped() {
        if (this.getMorphMode())
            return;

        GameObject arme = this.getObjetByPos(Constant.ITEM_POS_ARME);
        GameObject bouclier = this.getObjetByPos(Constant.ITEM_POS_BOUCLIER);
        if (arme != null) {
            if (arme.getTemplate().isTwoHanded() && bouclier != null && !(ArrayUtils.contains(Constant.SHIELD_HANDLING_EXCEPTIONS ,bouclier.getTemplate().getId()) ) ) {
                this.unequipedObjet(arme);
                SocketManager.GAME_SEND_Im_PACKET(this, "119|44");
            }
            else if (!arme.getTemplate().getConditions().equalsIgnoreCase("")
                    && !World.world.getConditionManager().validConditions(this, arme.getTemplate().getConditions())) {
                this.unequipedObjet(arme);
                SocketManager.GAME_SEND_Im_PACKET(this, "119|44");
            }
        }
        if (bouclier != null) {
            if (!bouclier.getTemplate().getConditions().equalsIgnoreCase("")
                    && !World.world.getConditionManager().validConditions(this, bouclier.getTemplate().getConditions())) {
                this.unequipedObjet(bouclier);
                SocketManager.GAME_SEND_Im_PACKET(this, "119|44");
            }
        }
    }

    public boolean hasEquiped(int id) {
        for (Entry<Long, GameObject> entry : objects.entrySet())
            if (entry.getValue().getTemplate().getId() == id
                    && entry.getValue().getPosition() != Constant.ITEM_POS_NO_EQUIPED)
                return true;

        return false;
    }

    public int getPosItem(int id) {
        for (Entry<Long, GameObject> entry : objects.entrySet())
            if (entry.getValue().getTemplate().getId() == id
                    && entry.getValue().getPosition() != Constant.ITEM_POS_NO_EQUIPED)
                return entry.getValue().getPosition();
        return Constant.ITEM_POS_NO_EQUIPED;
    }

    public int getInvitation() {
        return _inviting;
    }

    public void setInvitation(int target) {
        _inviting = target;
    }

    public String parseToPM() {
        StringBuilder str = new StringBuilder();
        str.append(this.getId()).append(";");
        str.append(this.getName()).append(";");
        str.append(gfxId).append(";");
        int color1 = this.getColor1(), color2 = this.getColor2(), color3 = this.getColor3();
        if (this.getObjetByPos(Constant.ITEM_POS_MALEDICTION) != null)
            if (this.getObjetByPos(Constant.ITEM_POS_MALEDICTION).getTemplate().getId() == 10838) {
                color1 = 16342021;
                color2 = 16342021;
                color3 = 16342021;
            }
        str.append(color1).append(";");
        str.append(color2).append(";");
        str.append(color3).append(";");
        str.append(getGMStuffString()).append(";");
        str.append(this.curPdv).append(",").append(this.maxPdv).append(";");
        str.append(this.getLevel()).append(";");
        str.append(getInitiative()).append(";");
        str.append(getTotalStats().getEffect(EffectConstant.STATS_ADD_PROS)
                + ((int) Math.ceil(getTotalStats().getEffect(EffectConstant.STATS_ADD_CHAN) / 10))).append(";");
        str.append("0");//Side = ?
        return str.toString();
    }

    public int getNumbEquipedItemOfPanoplie(int panID) {
        int nb = 0;

        for (Entry<Long, GameObject> i : objects.entrySet()) {
            //On ignore les objets non �quip�s
            if (i.getValue().getPosition() == Constant.ITEM_POS_NO_EQUIPED)
                continue;
            //On prend que les items de la pano demand�e, puis on augmente le nombre si besoin
            if (i.getValue().getTemplate().getPanoId() == panID)
                nb++;
        }
        return nb;
    }

    public void startActionOnCell(GameAction GA) {
        int cellID = -1;
        int action = -1;
        try {
            cellID = Integer.parseInt(GA.args.split(";")[0]);
            action = Integer.parseInt(GA.args.split(";")[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cellID == -1 || action == -1)
            return;
        //Si case invalide

        if (!this.curMap.getCase(cellID).canDoAction(action))
            return;
        this.curMap.getCase(cellID).startAction(this, GA);
    }

    public void finishActionOnCell(GameAction GA) {
        int cellID = -1;
        try {
            cellID = Integer.parseInt(GA.args.split(";")[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cellID == -1)
            return;
        this.curMap.getCase(cellID).finishAction(this, GA);
    }

    public void teleportD(short newMapID, int newCellID) {
        if (this.getFight() != null) return;
        this.curMap = World.world.getMap(newMapID);
        this.curCell = World.world.getMap(newMapID).getCase(newCellID);
        Database.getStatics().getPlayerData().update(this);
    }

    public void teleportLaby(short newMapID, int newCellID) {
        if (this.getFight() != null) return;
        GameClient client = this.getGameClient();
        if (client == null)
            return;

        if (World.world.getMap(newMapID) == null)
            return;

        if (World.world.getMap(newMapID).getCase(newCellID) == null)
            return;

        SocketManager.GAME_SEND_GA2_PACKET(client, this.getId());
        SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(this.curMap, this.getId());

        if (this.getMount() != null)
            if (this.getMount().getFatigue() >= 220)
                this.getMount().setEnergy(this.getMount().getEnergy() - 1);

        if (this.curCell.getPlayers().contains(this))
            this.curCell.removePlayer(this);
        this.curMap = World.world.getMap(newMapID);
        this.curCell = this.curMap.getCase(newCellID);

        SocketManager.GAME_SEND_MAPDATA(client, newMapID, this.curMap.getDate(), this.curMap.getKey());
        this.curMap.addPlayer(this);

        if (!this.follower.isEmpty())// On met a jour la Map des personnages qui nous suivent
        {
            for (Player t : this.follower.values()) {
                if (t.isOnline())
                    SocketManager.GAME_SEND_FLAG_PACKET(t, this);
                else
                    this.follower.remove(t.getId());
            }
        }
    }

    public void teleport(short newMapID, int newCellID) {
        if (this.getFight() != null) return;

        GameClient client = this.account.getGameClient();
        if (client == null)
            return;


            GameMap map = World.world.getMap(newMapID);
            if (map == null) {
                GameServer.a("Map " + newMapID + " null ");
                return;
            }

            if (map.getCase(newCellID) == null) {
                GameServer.a("Cell " + newCellID + " null on map " + newMapID);
                return;
            }

            if (newMapID == this.curMap.getId()) {
                SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(this.curMap, this.getId());
                this.curCell.removePlayer(this);
                this.curCell = curMap.getCase(newCellID);
                this.curMap.addPlayer(this);
                SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(this.curMap, this);
                return;
            }
            this.setAway(false);

            boolean fullmorph = false, deleteGladiaWeapon = false;
            if (Constant.isInMorphDonjon(this.curMap.getId()))
                if (!Constant.isInMorphDonjon(newMapID))
                    fullmorph = true;

            if (Constant.isInGladiatorDonjon(this.curMap.getId()) || this.curMap.getId() == 12277) {

                if (!Constant.isInGladiatorDonjon(newMapID)) {
                    fullmorph = true;
                    deleteGladiaWeapon = true;
                }

                if (Constant.isInGladiatorDonjon(newMapID) && this.curMap.getId() != 12277 ) {
                    this.fullPDV();
                    // Call your function here
                    SocketManager.GAME_SEND_wr(this, Constant.getPalierByNewMap(this.curMap.getId()));

                }
            }

            SocketManager.GAME_SEND_GA2_PACKET(client, this.getId());
            SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(this.curMap, this.getId());


            if (this.getMount() != null)
                if (this.getMount().getFatigue() >= 220)
                    this.getMount().setEnergy(this.getMount().getEnergy() - 1);

            if (this.curCell.getPlayers().contains(this))
                this.curCell.removePlayer(this);

            this.curMap = map;
            this.curCell = this.curMap.getCase(newCellID);
            // Verification de la Map
            // Verifier la validit� du mountpark

            if (this.curMap.getMountPark() != null
                    && this.curMap.getMountPark().getOwner() > 0
                    && this.curMap.getMountPark().getGuild() == null) {

                //if (World.world.getGuild( this.curMap.getMountPark().getGuild().getId() ) == null) {// Ne devrait  pas  arriver
                //GameServer.a();

                this.curMap.getMountPark().setData(0, -1, this.curMap.getMountPark().getPriceBase(), "", "", "", "");
                //Map.MountPark.removeMountPark(curMap.getMountPark().getGuild().getId());
                //}
            }

            // Verifier la validit� du Collector
            Collector col = Collector.getCollectorByMapId(this.curMap.getId());
            if (col != null) {
                if (World.world.getGuild(col.getGuildId()) == null)// Ne devrait pas arriver
                {
                    Collector.removeCollector(col.getGuildId());
                }
            }

            if (this.isInAreaNotSubscribe()) {
                if (!this.isInPrivateArea)
                    SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(this.getGameClient(), 'S');
                this.isInPrivateArea = true;
            } else {
                this.isInPrivateArea = false;
            }

        try {
            SocketManager.GAME_SEND_MAPDATA(client, newMapID, this.curMap.getDate(), this.curMap.getKey());
            this.curMap.addPlayer(this);

            if (fullmorph)
                this.unsetFullMorph();

            if(deleteGladiaWeapon) {
                if ( Constant.isGladiatroolWeapon(this.getObjetByPos(Constant.ITEM_POS_ARME).getTemplate().getId()) ) {
                    this.removeByTemplateID(this.getObjetByPos(Constant.ITEM_POS_ARME).getTemplate().getId(),1);
                }
                for(int i=Constant.ITEM_POS_TONIQUE_EQUILIBRAGE;i<= Constant.ITEM_POS_TONIQUE9;i++){
                    this.removeTonique(i);
                }
            }

            if (this.follower != null && !this.follower.isEmpty())// On met a jour la Map des personnages qui nous suivent
            {
                for (Player t : this.follower.values()) {
                    if (t.isOnline())
                        SocketManager.GAME_SEND_FLAG_PACKET(t, this);
                    else
                        this.follower.remove(t.getId());
                }
            }

            if (this.getInHouse() != null)
                if (this.getInHouse().getMapId() == this.curMap.getId())
                    this.setInHouse(null);

            if (map.getSubArea() != null) {
                if (map.getSubArea().getId() == 200) {
                    TimerWaiter.addNext(() -> PigDragon.sendPacketMap(this), 1000, TimeUnit.MILLISECONDS);
                } else if (map.getSubArea().getId() == 210 || map.getSubArea().getId() == 319) {
                    TimerWaiter.addNext(() -> Minotoror.sendPacketMap(this), 1000, TimeUnit.MILLISECONDS);
                }
            }
        }
        catch (Exception e){
            //e.printStackTrace();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);

            if (Logging.USE_LOG)
                Logging.getInstance().write("error", "tp error 1 " + e.getMessage() + " " + pw);

            return;
        }
    }

    public void teleport(GameMap map, int cell) {

            if (this.getFight() != null) return;
            GameClient PW = null;
            if (account.getGameClient() != null) {
                PW = account.getGameClient();
            }
            if (map == null) {
                // GameServer.a("Map voulu null");
                return;
            }
            if (map.getCase(cell) == null) {
                //GameServer.a();
                return;
            }
            if (!cantTP()) {
                if (this.getCurMap().getSubArea() != null
                        && map.getSubArea() != null) {
                    if (this.getCurMap().getSubArea().getId() == 165
                            && map.getSubArea().getId() == 165) {
                        if (this.hasItemTemplate(997, 1)) {
                            this.removeByTemplateID(997, 1);
                        } else {
                            SocketManager.GAME_SEND_Im_PACKET(this, "14");
                            return;
                        }
                    }
                }
            }

            boolean fullmorph = false;
            if (Constant.isInMorphDonjon(curMap.getId()))
                if (!Constant.isInMorphDonjon(map.getId()))
                    fullmorph = true;

            if (map.getId() == curMap.getId()) {
                SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(curMap, this.getId());
                curCell.removePlayer(this);
                curCell = curMap.getCase(cell);
                curMap.addPlayer(this);
                SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(curMap, this);
                if (fullmorph)
                    this.unsetFullMorph();
                return;
            }
            if (PW != null) {
                SocketManager.GAME_SEND_GA2_PACKET(PW, this.getId());
                SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(curMap, this.getId());
            }

        try {

            if (this.getMount() != null)
                if (this.getMount().getFatigue() >= 220)
                    this.getMount().setEnergy(this.getMount().getEnergy() - 1);
            curCell.removePlayer(this);
            curMap = map;
            curCell = curMap.getCase(cell);
            // Verification de la Map
            // Verifier la validit� du mountpark
            if (curMap.getMountPark() != null
                    && curMap.getMountPark().getOwner() > 0
                    && curMap.getMountPark().getGuild().getId() != -1) {
                if (World.world.getGuild(curMap.getMountPark().getGuild().getId()) == null)// Ne devrait  pas  arriver
                {
                    GameServer.a("LA guilde " + curMap.getMountPark().getGuild().getId() + " semble ne pas exister");
                    //FIXME : Map.MountPark.removeMountPark(curMap.getMountPark().getGuild().getId());
                }
            }
            // Verifier la validit� du Collector
            if (Collector.getCollectorByMapId(curMap.getId()) != null) {
                if (World.world.getGuild(Collector.getCollectorByMapId(curMap.getId()).getGuildId()) == null)// Ne devrait pas arriver
                {
                    GameServer.a("LA guilde " + Collector.getCollectorByMapId(curMap.getId()).getGuildId() + " semble ne pas exister");
                    Collector.removeCollector(Collector.getCollectorByMapId(curMap.getId()).getGuildId());
                }
            }

            if (PW != null) {
                SocketManager.GAME_SEND_MAPDATA(PW, map.getId(), curMap.getDate(), curMap.getKey());
                curMap.addPlayer(this);
                if (fullmorph)
                    this.unsetFullMorph();
            }

            if (!follower.isEmpty())// On met a jour la Map des personnages qui nous suivent
            {
                for (Player t : follower.values()) {
                    if (t.isOnline())
                        SocketManager.GAME_SEND_FLAG_PACKET(t, this);
                    else
                        follower.remove(t.getId());
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
            if (Logging.USE_LOG)
                Logging.getInstance().write("error", "tp error 2 " + e.getMessage() + " " + e.getLocalizedMessage());

            return;

        }
    }

    public void disconnectInFight() {
        //Si en groupe
        if (getParty() != null)
            getParty().leave(this);
        resetVars();
        Database.getStatics().getPlayerData().update(this);
        set_isClone(true);
        World.world.unloadPerso(this.getId());
    }

    public int getBankCost() {
        if(account.getVip() == 0) {
            return account.getBank().size();
        }
        else{
            return 0;
        }
    }

    public void openBank() {
        if(this.getExchangeAction() != null)
            return;
        if (this.getDeshonor() >= 1) {
            SocketManager.GAME_SEND_Im_PACKET(this, "183");
            return;
        }

        final int cost = this.getBankCost();
        Database.getStatics().getPlayerData().update(this);

        if (cost > 0) {
            final long kamas = this.getKamas();
            final long remaining = kamas - cost;
            final long bank = this.getAccount().getBankKamas();
            final long total = bank + kamas;
            if (remaining < 0) {
                if (bank >= cost) {
                    this.setBankKamas(bank - cost);
                } else if (total >= cost) {
                    this.setKamas(0);
                    this.setBankKamas(total - cost);
                    SocketManager.GAME_SEND_STATS_PACKET(this);
                    SocketManager.GAME_SEND_Im_PACKET(this, "020;" + kamas);
                } else {
                    SocketManager.GAME_SEND_MESSAGE_SERVER(this, "10|" + cost);
                    return;
                }
            } else {
                this.setKamas(remaining);
                SocketManager.GAME_SEND_STATS_PACKET(this);
                SocketManager.GAME_SEND_Im_PACKET(this, "020;" + cost);
            }
        }
        SocketManager.GAME_SEND_ECK_PACKET(this.getGameClient(), 5, "");
        SocketManager.GAME_SEND_EL_BANK_PACKET(this);
        this.setAway(true);
        this.setExchangeAction(new ExchangeAction<>(ExchangeAction.IN_BANK, 0));
    }

    public String getStringVar(String str) {
        switch (str) {
            case "name":
                return this.getName();
            case "bankCost":
                return getBankCost() + "";
            case "points":
                if(this.getAccount().getWebAccount() !=null)
                return this.getAccount().getWebAccount().getPoints() + "";
                else{
                    return  -1+"";
                }
            case "nbrOnline":
                return GameServer.getClients().size() + "";
            case "align":
                return World.world.getStatOfAlign();
        }
        return "";
    }

    public void refreshMapAfterFight() {
        SocketManager.send(this, "ILS" + 500);
        this.regenRate = 500;
        this.curMap.addPlayer(this);
        if (this.account.getGameClient() != null)
            SocketManager.GAME_SEND_STATS_PACKET(this);
        this.fight = null;
        this.away = false;
    }

    public long getBankKamas() {

        return account.getBankKamas();
    }

    public void setBankKamas(long i) {
        if(i < 0) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String str = "";
            int j = 0;
            for (StackTraceElement caller : stackTrace ) {
                j++;
                str += "["+ j +"] :" + "De " + caller.getMethodName() + "/" + caller.getClassName() + " && ";
                if(j > 4)
                    break;
            }
            World.sendWebhookMessage(Config.INSTANCE.getDISCORD_CHANNEL_FAILLE(),"BAN : Tentative de retrait de "+i+" kamas alors qu'il n'en n'avait que "+this.getKamas() +" : Trace" + str, this);
            this.banAccount();
        }
        else{
            account.setBankKamas(i);
            Database.getDynamics().getBankData().update(account);
        }
    }

    public String parseBankPacket() {
        StringBuilder packet = new StringBuilder();
        for (GameObject entry : account.getBank())
            packet.append("O").append(entry.parseItem()).append(";");
        if (getBankKamas() != 0)
            packet.append("G").append(getBankKamas());
        return packet.toString();
    }

    public void addCapital(int pts) {
        _capital += pts;
    }

    public void setCaptial(int pts) {
        _capital = pts;
    }

    public void Restat_Stats(Boolean parcho)
    {
        try {
            getStats().addOneStat(125, -getStats().getEffect(125));
            getStats().addOneStat(124, -getStats().getEffect(124));
            getStats().addOneStat(118, -getStats().getEffect(118));
            getStats().addOneStat(123, -getStats().getEffect(123));
            getStats().addOneStat(119, -getStats().getEffect(119));
            getStats().addOneStat(126, -getStats().getEffect(126));
            addCapital((getLevel() - 1) * 5 - get_capital());
            if(parcho) {
                getStatsParcho().getEffects().clear();
            }
            SocketManager.GAME_SEND_STATS_PACKET(this);
        } catch (Exception e) {
            e.printStackTrace();
            GameServer.a(e.getMessage());
        }
    }

    public void addSpellPoint(int pts) {
        if (_morphMode)
            _saveSpellPts += pts;
        else
            _spellPts += pts;
    }

    public void addInBank(long guid, int qua) {
        if (qua <= 0)
            return;
        GameObject PersoObj = World.world.getGameObject(guid);

        if (this.objects == null) return;

        if (objects.get(guid) == null) // Si le joueur n'a pas l'item dans son sac ...
            return;

        if (PersoObj.getPosition() != Constant.ITEM_POS_NO_EQUIPED) // Si c'est un item �quip� ...
            return;

        GameObject BankObj = getSimilarBankItem(PersoObj);
        int newQua = PersoObj.getQuantity() - qua;
        if (BankObj == null) // Ajout d'un nouvel objet dans la banque
        {
            if (newQua <= 0) // Ajout de toute la quantit� disponible
            {
                removeItem(PersoObj.getGuid()); // On enleve l'objet du sac du joueur
                account.getBank().add(PersoObj); // On met l'objet du sac dans la banque, avec la meme quantit�
                String str = "O+" + PersoObj.getGuid() + "|"
                        + PersoObj.getQuantity() + "|"
                        + PersoObj.getTemplate().getId() + "|"
                        + PersoObj.parseStatsString();
                SocketManager.GAME_SEND_EsK_PACKET(this, str);
                SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, guid);
            } else
            //S'il reste des objets au joueur
            {
                PersoObj.setQuantity(newQua); //on modifie la quantit� d'item du sac
                BankObj = GameObject.getCloneObjet(PersoObj, qua); //On ajoute l'objet a la banque et au monde
                World.world.addGameObject(BankObj, true);
                account.getBank().add(BankObj);

                String str = "O+" + BankObj.getGuid() + "|"
                        + BankObj.getQuantity() + "|"
                        + BankObj.getTemplate().getId() + "|"
                        + BankObj.parseStatsString();
                SocketManager.GAME_SEND_EsK_PACKET(this, str); //Envoie des packets
                SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, PersoObj);
            }
        } else
        // S'il y avait un item du meme template
        {
            if (newQua <= 0) //S'il ne reste pas d'item dans le sac
            {
                removeItem(PersoObj.getGuid()); //On enleve l'objet du sac du joueur
                World.world.removeGameObject(PersoObj.getGuid()); //On enleve l'objet du monde
                BankObj.setQuantity(BankObj.getQuantity()
                        + PersoObj.getQuantity()); //On ajoute la quantit� a l'objet en banque
                String str = "O+" + BankObj.getGuid() + "|"
                        + BankObj.getQuantity() + "|"
                        + BankObj.getTemplate().getId() + "|"
                        + BankObj.parseStatsString(); //on envoie l'ajout a la banque de l'objet
                SocketManager.GAME_SEND_EsK_PACKET(this, str);
                SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, guid); //on envoie la supression de l'objet du sac au joueur
            } else
            //S'il restait des objets
            {
                PersoObj.setQuantity(newQua); //on modifie la quantit� d'item du sac
                BankObj.setQuantity(BankObj.getQuantity() + qua);
                String str = "O+" + BankObj.getGuid() + "|"
                        + BankObj.getQuantity() + "|"
                        + BankObj.getTemplate().getId() + "|"
                        + BankObj.parseStatsString();
                SocketManager.GAME_SEND_EsK_PACKET(this, str);
                SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, PersoObj);
            }
        }
        SocketManager.GAME_SEND_Ow_PACKET(this);

        Database.getStatics().getPlayerData().update(this);
        Database.getDynamics().getBankData().update(account);
    }

    private GameObject getSimilarBankItem(GameObject exGameObject) {
        for (GameObject gameObject : this.account.getBank())
            if (World.world.getConditionManager().stackIfSimilar2(gameObject, exGameObject, true))
                return gameObject;
        return null;
    }

    public void removeFromBank(long guid, int qua) {
        if (qua <= 0)
            return;
        GameObject BankObj = World.world.getGameObject(guid);

        //Si le joueur n'a pas l'item dans sa banque ...
        int index = account.getBank().indexOf(BankObj);
        if (index == -1)
            return;

        GameObject PersoObj = getSimilarItem(BankObj);
        int newQua = BankObj.getQuantity() - qua;

        if (PersoObj == null)//Si le joueur n'avait aucun item similaire
        {
            //S'il ne reste rien en banque
            if (newQua <= 0) {
                //On retire l'item de la banque
                account.getBank().remove(index);
                //On l'ajoute au joueur

                objects.put(guid, BankObj);


                //On envoie les packets
                SocketManager.GAME_SEND_OAKO_PACKET(this, BankObj);
                String str = "O-" + guid;
                SocketManager.GAME_SEND_EsK_PACKET(this, str);
            } else
            //S'il reste des objets en banque
            {
                //On cr�e une copy de l'item en banque
                PersoObj = GameObject.getCloneObjet(BankObj, qua);
                //On l'ajoute au monde
                World.world.addGameObject(PersoObj, true);
                //On retire X objet de la banque
                BankObj.setQuantity(newQua);
                //On l'ajoute au joueur

                objects.put(PersoObj.getGuid(), PersoObj);


                //On envoie les packets
                SocketManager.GAME_SEND_OAKO_PACKET(this, PersoObj);
                String str = "O+" + BankObj.getGuid() + "|"
                        + BankObj.getQuantity() + "|"
                        + BankObj.getTemplate().getId() + "|"
                        + BankObj.parseStatsString();
                SocketManager.GAME_SEND_EsK_PACKET(this, str);
            }
        } else {
            //S'il ne reste rien en banque
            if (newQua <= 0) {
                //On retire l'item de la banque
                account.getBank().remove(index);
                World.world.removeGameObject(BankObj.getGuid());
                //On Modifie la quantit� de l'item du sac du joueur
                PersoObj.setQuantity(PersoObj.getQuantity()
                        + BankObj.getQuantity());

                //On envoie les packets
                SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, PersoObj);
                String str = "O-" + guid;
                SocketManager.GAME_SEND_EsK_PACKET(this, str);
            } else
            //S'il reste des objets en banque
            {
                //On retire X objet de la banque
                BankObj.setQuantity(newQua);
                //On ajoute X objets au joueurs
                PersoObj.setQuantity(PersoObj.getQuantity() + qua);

                //On envoie les packets
                SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, PersoObj);
                String str = "O+" + BankObj.getGuid() + "|"
                        + BankObj.getQuantity() + "|"
                        + BankObj.getTemplate().getId() + "|"
                        + BankObj.parseStatsString();
                SocketManager.GAME_SEND_EsK_PACKET(this, str);
            }
        }

        SocketManager.GAME_SEND_Ow_PACKET(this);

        Database.getStatics().getPlayerData().update(this);
        Database.getDynamics().getBankData().update(account);
    }

    /**
     * MountPark *
     */
    public void openMountPark() {
        if (this.getDeshonor() >= 5) {
            SocketManager.GAME_SEND_Im_PACKET(this, "183");
            return;
        }
        if (this.getGuildMember() != null && this.curMap.getMountPark().getGuild() != null) {
            if (this.curMap.getMountPark().getGuild().getId() == this.getGuildMember().getGuild().getId()) {
                if (!this.getGuildMember().canDo(Constant.G_USEENCLOS)) {
                    SocketManager.GAME_SEND_Im_PACKET(this, "1101");
                    return;
                }
            }
        }

        MountPark mountPark = this.curMap.getMountPark();
        this.setExchangeAction(new ExchangeAction<>(ExchangeAction.IN_MOUNTPARK, mountPark));
        this.away = true;

        StringBuilder packet = new StringBuilder();

        if (mountPark.getEtable().size() > 0) {
            for (Mount mount : mountPark.getEtable()) {
                if (mount == null || mount.getSize() == 50) continue;
                if (!packet.toString().isEmpty()) packet.append(";");
                if (mount.getOwner() == this.getId()) packet.append(mount.parse());
            }
        }

        packet.append("~");

        if (mountPark.getListOfRaising().size() > 0) {
            boolean first1 = false;
            for (Integer id : mountPark.getListOfRaising()) {
                Mount mount = World.world.getMountById(id);
                if (mount == null) continue;

                if (mount.getOwner() == this.getId()) {
                    if (first1)
                        packet.append(";");
                    packet.append(mount.parse());
                    first1 = true;
                    continue;
                }
                if (getGuildMember() != null) {
                    if (getGuildMember().canDo(Constant.G_OTHDINDE) && mountPark.getOwner() != -1 && mountPark.getGuild() != null) {
                        if (mountPark.getGuild().getId() == this.getGuild().getId()) {
                            if (first1) packet.append(";");
                            packet.append(mount.parse());
                            first1 = true;
                        }
                    }
                }
            }
        }

        SocketManager.GAME_SEND_ECK_PACKET(this, 16, packet.toString());

        TimerWaiter.addNext(() -> mountPark.getEtable().stream().filter(mount -> mount != null && mount.getSize() == 50 && mount.getOwner() == this.getId()).forEach(mount -> SocketManager.GAME_SEND_Ee_PACKET_WAIT(this, '~', mount.parse())), 500, TimeUnit.MILLISECONDS);
    }

    public void fullPDV() {
        this.setPdv(this.getMaxPdv());
        SocketManager.GAME_SEND_STATS_PACKET(this);
    }

    public void warpToSavePos() {
        try {
            String[] infos = _savePos.split(",");
            this.teleport(Short.parseShort(infos[0]), Integer.parseInt(infos[1]));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeByTemplateID(int tID, int count) {
        //Copie de la liste pour eviter les modif concurrentes
        ArrayList<GameObject> list = new ArrayList<GameObject>();

        list.addAll(objects.values());


        ArrayList<GameObject> remove = new ArrayList<GameObject>();
        int tempCount = count;

        //on verifie pour chaque objet
        for (GameObject obj : list) {
            //Si mauvais TemplateID, on passe
            if (obj.getTemplate().getId() != tID)
                continue;

            if (obj.getQuantity() >= count) {
                int newQua = obj.getQuantity() - count;
                if (newQua > 0) {
                    obj.setQuantity(newQua);
                    if (isOnline)
                        SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, obj);
                } else {
                    //on supprime de l'inventaire et du Monde
                    objects.remove(obj.getGuid());
                    World.world.removeGameObject(obj.getGuid());
                    //on envoie le packet si connect�
                    if (isOnline)
                        SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, obj.getGuid());
                }
                return;
            } else
            //Si pas assez d'objet
            {
                if (obj.getQuantity() >= tempCount) {
                    int newQua = obj.getQuantity() - tempCount;
                    if (newQua > 0) {
                        obj.setQuantity(newQua);
                        if (isOnline)
                            SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, obj);
                    } else
                        remove.add(obj);

                    for (GameObject o : remove) {
                        //on supprime de l'inventaire et du Monde

                        objects.remove(o.getGuid());

                        World.world.removeGameObject(o.getGuid());
                        //on envoie le packet si connect�
                        if (isOnline)
                            SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, o.getGuid());
                    }
                } else {
                    // on r�duit le compteur
                    tempCount -= obj.getQuantity();
                    remove.add(obj);
                }
            }
        }
    }

    public ArrayList<Job> getJobs() {
        ArrayList<Job> list = new ArrayList<>();
        for (JobStat js : _metiers.values())
            if (js.getTemplate() != null)
                list.add(js.getTemplate());
        return (list.isEmpty() ? null : list);
    }

    public Map<Integer, JobStat> getMetiers() {
        return _metiers;
    }

    public void doJobAction(int actionID, InteractiveObject object,
                            GameAction GA, GameCase cell) {
        JobStat SM = getMetierBySkill(actionID);
        if (SM == null) {
            switch (actionID) {
                case 151:
                    new JobAction(151, 4, 0, true, 100, 0).startAction(this, object, GA, cell);
                    return;
                case 121:
                    new JobAction(121, 8, 0, true, 100, 0).startAction(this, object, GA, cell);
                    return;
                case 110:
                    new JobAction(110, 2, 0, true, 100, 0).startAction(this, object, GA, cell);
                    return;
                case 22:
                    new JobAction(22, 1, 0, true, 100, 0).startAction(this, object, GA, cell);
                    return;

            }
            SocketManager.GAME_SEND_MESSAGE(this, "Erreur stats job null.");
            return;
        }
        SM.startAction(actionID, this, object, GA, cell);
    }

    public void finishJobAction(int actionID, InteractiveObject object,
                                GameAction GA, GameCase cell) {
        JobStat SM = getMetierBySkill(actionID);
        if (SM == null)
            return;
        SM.endAction(this, object, GA, cell);
    }

    public String parseJobData() {
        StringBuilder str = new StringBuilder();
        if (_metiers.isEmpty())
            return "";
        for (JobStat SM : _metiers.values()) {
            if (SM == null)
                continue;
            if (str.length() > 0)
                str.append(";");
            str.append(SM.getTemplate().getId()).append(",").append(SM.getXp());
        }
        return str.toString();
    }

    public int totalJobBasic() {
        int i = 0;

        for (JobStat SM : _metiers.values()) {
            // Si c'est un m�tier 'basic' :
            if (SM.getTemplate().getId() == 2 || SM.getTemplate().getId() == 11
                    || SM.getTemplate().getId() == 13
                    || SM.getTemplate().getId() == 14
                    || SM.getTemplate().getId() == 15
                    || SM.getTemplate().getId() == 16
                    || SM.getTemplate().getId() == 17
                    || SM.getTemplate().getId() == 18
                    || SM.getTemplate().getId() == 19
                    || SM.getTemplate().getId() == 20
                    || SM.getTemplate().getId() == 24
                    || SM.getTemplate().getId() == 25
                    || SM.getTemplate().getId() == 26
                    || SM.getTemplate().getId() == 27
                    || SM.getTemplate().getId() == 28
                    || SM.getTemplate().getId() == 31
                    || SM.getTemplate().getId() == 36
                    || SM.getTemplate().getId() == 41
                    || SM.getTemplate().getId() == 56
                    || SM.getTemplate().getId() == 58
                    || SM.getTemplate().getId() == 60
                    || SM.getTemplate().getId() == 65) {
                i++;
            }
        }
        return i;
    }

    public int totalJobFM() {
        int i = 0;

        for (JobStat SM : _metiers.values()) {
            // Si c'est une sp�cialisation 'FM' :
            if (SM.getTemplate().getId() == 43
                    || SM.getTemplate().getId() == 44
                    || SM.getTemplate().getId() == 45
                    || SM.getTemplate().getId() == 46
                    || SM.getTemplate().getId() == 47
                    || SM.getTemplate().getId() == 48
                    || SM.getTemplate().getId() == 49
                    || SM.getTemplate().getId() == 50
                    || SM.getTemplate().getId() == 62
                    || SM.getTemplate().getId() == 63
                    || SM.getTemplate().getId() == 64) {
                i++;
            }
        }
        return i;
    }

    public boolean canAggro() {
        return canAggro;
    }

    public void setCanAggro(boolean canAggro) {
        this.canAggro = canAggro;
    }

    public JobStat getMetierBySkill(int skID) {
        for (JobStat SM : _metiers.values())
            if (SM.isValidMapAction(skID))
                return SM;
        return null;
    }

    public String parseToFriendList(int guid) {
        StringBuilder str = new StringBuilder();
        str.append(";");
        str.append("?;");
        str.append(this.getName()).append(";");
        if (account.isFriendWith(guid)) {
            str.append(this.getLevel()).append(";");
            str.append(_align).append(";");
        } else {
            str.append("?;");
            str.append("-1;");
        }
        str.append(this.getClasse()).append(";");
        str.append(this.getSexe()).append(";");
        str.append(gfxId);
        return str.toString();
    }

    public String parseToEnemyList(int guid) {
        StringBuilder str = new StringBuilder();
        str.append(";");
        str.append("?;");
        str.append(this.getName()).append(";");
        if (account.isFriendWith(guid)) {
            str.append(this.getLevel()).append(";");
            str.append(_align).append(";");
        } else {
            str.append("?;");
            str.append("-1;");
        }
        str.append(this.getClasse()).append(";");
        str.append(this.getSexe()).append(";");
        str.append(gfxId);
        return str.toString();
    }

    public JobStat getMetierByID(int job) {
        for (JobStat SM : _metiers.values())
            if (SM.getTemplate().getId() == job)
                return SM;
        return null;
    }

    public boolean isOnMount() {
        return _onMount;
    }

    public void toogleOnMount() {
        if (_mount == null || this.isMorph() || this.getLevel() < 60)
            return;
        if (Config.INSTANCE.getSubscription()) {
            SocketManager.GAME_SEND_Im_PACKET(this, "1115");
            return;
        }
        if (this.getClasse() * 10 + this.getSexe() != this.getGfxId())
            return;
        if (this.getInHouse() != null) {
            SocketManager.GAME_SEND_Im_PACKET(this, "1117");
            return;
        }
        if (!_onMount && _mount.isMontable() == 0) {
            SocketManager.GAME_SEND_Re_PACKET(this, "Er", null);
            return;
        }

        if (!_onMount && _mount.getEnergy() < Formulas.calculEnergieLooseForToogleMount(_mount.getFatigue())) {
            SocketManager.GAME_SEND_Im_PACKET(this, "1113");
            return;
        }

        if (!_onMount) {
            int EnergyoLose = _mount.getEnergy()
                    - Formulas.calculEnergieLooseForToogleMount(_mount.getFatigue());
            _mount.setEnergy(EnergyoLose);
        }

        _onMount = !_onMount;
        GameObject obj = getObjetByPos(Constant.ITEM_POS_FAMILIER);

        if (_onMount && obj != null) {
            obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
            SocketManager.GAME_SEND_OBJET_MOVE_PACKET(this, obj);
        }

        if (_mount.getEnergy() <= 0) {
            _mount.setEnergy(0);
            SocketManager.GAME_SEND_Im_PACKET(this, "1114");
            return;
        }
        //on envoie les packets
        if (getFight() != null && getFight().getState() == 2) {
            SocketManager.GAME_SEND_ALTER_FIGHTER_MOUNT(getFight(), getFight().getFighterByPerso(this), getId(), getFight().getTeamId(getId()), getFight().getOtherTeamId(getId()));
        } else {
            SocketManager.GAME_SEND_ALTER_GM_PACKET(curMap, this);
        }
        SocketManager.GAME_SEND_Re_PACKET(this, "+", _mount);
        SocketManager.GAME_SEND_Rr_PACKET(this, _onMount ? "+" : "-");
        SocketManager.GAME_SEND_STATS_PACKET(this);

    }

    public int getMountXpGive() {
        return _mountXpGive;
    }

    public Mount getMount() {
        return _mount;
    }

    public void setMount(Mount DD) {
        _mount = DD;
    }

    public void setMountGiveXp(int parseInt) {
        _mountXpGive = parseInt;
    }

    public void resetVars() {
        if (this.getExchangeAction() != null) {
            if (this.getExchangeAction().getValue() instanceof JobAction && ((JobAction) this.getExchangeAction().getValue()).getJobCraft() != null)
                ((JobAction) this.getExchangeAction().getValue()).getJobCraft().jobAction.broke = true;
            this.setExchangeAction(null);
        }
        this._curJobAction = null;
        doAction = false;
        this.setGameAction(null);

        away = false;
        _emoteActive = 0;
        fight = null;
        duelId = 0;
        ready = false;
        party = null;
        _inviting = 0;
        sitted = false;
        _onMount = false;
        _isClone = false;
        _isAbsent = false;
        _isInvisible = false;
        follower.clear();
        follow = null;
        _curHouse = null;
        isGhost = false;
        _livreArti = false;
        _spec = false;
        afterFight = false;
    }

    public void addChanel(String chan) {
        if (_canaux.indexOf(chan) >= 0)
            return;
        _canaux += chan;
        SocketManager.GAME_SEND_cC_PACKET(this, '+', chan);
    }

    public void removeChanel(String chan) {
        _canaux = _canaux.replace(chan, "");
        SocketManager.GAME_SEND_cC_PACKET(this, '-', chan);
    }

    public void modifAlignement(int i) {
        _honor = 0;
        _deshonor = 0;
        _align = (byte) i;
        _aLvl = 1;
        SocketManager.GAME_SEND_ZC_PACKET(this, i);
        SocketManager.GAME_SEND_STATS_PACKET(this);
        if (getGuild() != null)
            Database.getDynamics().getGuildMemberData().update(this);
    }

    public int getDeshonor() {
        return _deshonor;
    }

    public void setDeshonor(int deshonor) {
        _deshonor = deshonor;
    }

    public void setShowWings(boolean showWings) {
        _showWings = showWings;
    }

    public int get_honor() {
        return _honor;
    }

    public void set_honor(int honor) {
        _honor = honor;
    }

    public int getALvl() {
        return _aLvl;
    }

    public void setALvl(int a) {
        _aLvl = a;
    }

    public void toggleWings(char c) {
        if (_align == Constant.ALIGNEMENT_NEUTRE)
            return;
        int hloose = _honor * 5 / 100;
        switch (c) {
            case '*':
                SocketManager.GAME_SEND_GIP_PACKET(this, hloose);
                return;
            case '+':
                setShowWings(true);
                SocketManager.GAME_SEND_ALTER_GM_PACKET(this.curMap, this);
                Database.getStatics().getPlayerData().update(this);
                break;
            case '-':
                setShowWings(false);
                _honor -= hloose;
                SocketManager.GAME_SEND_ALTER_GM_PACKET(this.curMap, this);
                Database.getStatics().getPlayerData().update(this);
                break;
        }
        SocketManager.GAME_SEND_STATS_PACKET(this);
    }

    public void addHonor(int winH) {
        if (_align == 0)
            return;
        int curGrade = getGrade();
        _honor += winH;
        if (_honor > 18000) _honor = 18000;
        SocketManager.GAME_SEND_Im_PACKET(this, "080;" + winH);
        //Changement de grade
        if (getGrade() != curGrade) {
            SocketManager.GAME_SEND_Im_PACKET(this, "082;" + getGrade());
        }
    }

    public void remHonor(int losePH) {
        if (_align == 0)
            return;
        int curGrade = getGrade();
        _honor -= losePH;
        SocketManager.GAME_SEND_Im_PACKET(this, "081;" + losePH);
        //Changement de grade
        if (getGrade() != curGrade) {
            SocketManager.GAME_SEND_Im_PACKET(this, "083;" + getGrade());
        }
    }

    public GuildMember getGuildMember() {
        return _guildMember;
    }

    public void setGuildMember(GuildMember _guild) {
        this._guildMember = _guild;
    }

    public int getAccID() {
        return _accID;
    }

    public String parseZaapList()//Pour le packet WC
    {
        String map = curMap.getId() + "";
        try {
            map = _savePos.split(",")[0];
        } catch (Exception e) {
            e.printStackTrace();
        }

        StringBuilder str = new StringBuilder();
        str.append(map);

        int SubAreaID = curMap.getSubArea().getArea().getSuperArea();

        for (short i : _zaaps) {
            if (World.world.getMap(i) == null)
                continue;
            if (World.world.getMap(i).getSubArea().getArea().getSuperArea() != SubAreaID)
                continue;
            int cost = Formulas.calculZaapCost(curMap, World.world.getMap(i));
            if (i == curMap.getId())
                cost = 0;
            str.append("|").append(i).append(";").append(cost);
        }
        return str.toString();
    }

    public String parsePrismesList() {
        String map = curMap.getId() + "";
        String str = map + "";
        int SubAreaID = curMap.getSubArea().getArea().getSuperArea();
        ArrayList<Prism> finalPrismes = new ArrayList<Prism>();

        for (Prism Prisme : World.world.AllPrisme()) {

            if (Prisme.getAlignement() != _align)
                continue;
            short MapID = Prisme.getMap();
            if (World.world.getMap(MapID) == null)
                continue;
            if (World.world.getMap(MapID).getSubArea().getArea().getSuperArea() != SubAreaID)
                continue;
            finalPrismes.add(Prisme);

        }

        Collections.sort(finalPrismes, new Prism.PrimsXComparator());

        for (Prism Prisme : finalPrismes){
            short MapID = Prisme.getMap();
            if (Prisme.getInFight() == 0 || Prisme.getInFight() == -2) {
                str += "|" + MapID + ";*";

            } else {
                int costo = Formulas.calculZaapCost(curMap, World.world.getMap(MapID));
                if (MapID == curMap.getId())
                    costo = 0;

                str += "|" + MapID + ";" + costo;
            }
        }
        return str;
    }

    public void openZaapMenu() {
        if (this.fight == null) {
            if (!verifOtomaiZaap())
                return;
            if (getDeshonor() >= 3) {
                SocketManager.GAME_SEND_Im_PACKET(this, "183");
                return;
            }

            this.setExchangeAction(new ExchangeAction<>(ExchangeAction.IN_ZAAPING, 0));
            //verifAndAddZaap(curMap.getId());
            SocketManager.GAME_SEND_WC_PACKET(this);
        }
    }

    public void verifAndAddZaap(short mapId) {
        if (!verifOtomaiZaap())
            return;
        if (!_zaaps.contains(mapId)) {
            _zaaps.add(mapId);
            SocketManager.GAME_SEND_Im_PACKET(this, "024");
            Database.getStatics().getPlayerData().update(this);
        }
    }

    public boolean verifOtomaiZaap() {
        return Config.INSTANCE.getALL_ZAAP() || !(this.getCurMap().getId() == 10643 || this.getCurMap().getId() == 11210)
                || World.world.getConditionManager().validConditions(this, "QT=231") && World.world.getConditionManager().validConditions(this, "QT=232");
    }

    public void openPrismeMenu() {
        if (this.fight == null) {
            if (getDeshonor() >= 3) {
                SocketManager.GAME_SEND_Im_PACKET(this, "183");
                return;
            }

            this.setExchangeAction(new ExchangeAction<>(ExchangeAction.IN_PRISM, 0));
            SocketManager.SEND_Wp_MENU_Prisme(this);
        }
    }

    public synchronized void checkDoubleStuff() {
        boolean usingBug = false;
        byte posCoiffe = 0, posCape = 0, posFami = 0, posAnn1 = 0, posAnn2 = 0, posCeinture = 0,
                posBottes = 0, posAmulette = 0, posBouclier = 0, posDofusOne = 0, posDofusTwo = 0, posDofusThree = 0,
                posDofusFour = 0, posDofusFive = 0, posDofusSix = 0, posArme = 0;

        for (GameObject obj : this.objects.values()) {
            if (obj.getPosition() == Constant.ITEM_POS_NO_EQUIPED)
                continue;

            if (obj.getPosition() == Constant.ITEM_POS_COIFFE) {
                posCoiffe++;
                if (posCoiffe > 1) {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                    usingBug = true;
                }
            }

            if (obj.getPosition() == Constant.ITEM_POS_CAPE) {
                posCape++;
                if (posCape > 1) {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                    usingBug = true;
                }
            }

            if (obj.getPosition() == Constant.ITEM_POS_FAMILIER) {
                posFami++;
                if (posFami > 1) {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                    usingBug = true;
                }
            }

            if (obj.getPosition() == Constant.ITEM_POS_ANNEAU1) {
                posAnn1++;
                if (posAnn1 > 1) {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                    usingBug = true;
                }
            }

            if (obj.getPosition() == Constant.ITEM_POS_ANNEAU2) {
                posAnn2++;
                if (posAnn2 > 1) {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                    usingBug = true;
                }
            }

            if (obj.getPosition() == Constant.ITEM_POS_CEINTURE) {
                posCeinture++;
                if (posCeinture > 1) {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                    usingBug = true;
                }
            }

            if (obj.getPosition() == Constant.ITEM_POS_BOTTES) {
                posBottes++;
                if (posBottes > 1) {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                    usingBug = true;
                }
            }

            if (obj.getPosition() == Constant.ITEM_POS_AMULETTE) {
                posAmulette++;
                if (posAmulette > 1) {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                    usingBug = true;
                }
            }

            if (obj.getPosition() == Constant.ITEM_POS_BOUCLIER) {
                posBouclier++;
                if (posBouclier > 1) {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                    usingBug = true;
                }
            }

            if (obj.getPosition() == Constant.ITEM_POS_DOFUS1) {
                posDofusOne++;
                if (posDofusOne > 1) {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                    usingBug = true;
                }
            }

            if (obj.getPosition() == Constant.ITEM_POS_DOFUS2) {
                posDofusTwo++;
                if (posDofusTwo > 1) {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                    usingBug = true;
                }
            }

            if (obj.getPosition() == Constant.ITEM_POS_DOFUS3) {
                posDofusThree++;
                if (posDofusThree > 1) {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                    usingBug = true;
                }
            }

            if (obj.getPosition() == Constant.ITEM_POS_DOFUS4) {
                posDofusFour++;
                if (posDofusFour > 1) {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                    usingBug = true;
                }
            }

            if (obj.getPosition() == Constant.ITEM_POS_DOFUS5) {
                posDofusFive++;
                if (posDofusFive > 1) {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                    usingBug = true;
                }
            }

            if (obj.getPosition() == Constant.ITEM_POS_DOFUS6) {
                posDofusSix++;
                if (posDofusSix > 1) {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                    usingBug = true;
                }
            }

            if (obj.getPosition() == Constant.ITEM_POS_ARME) {
                posArme++;
                if (posArme > 1) {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                    usingBug = true;
                }
            }
        }

        if (usingBug) {
            World.sendWebhookMessage(Config.INSTANCE.getDISCORD_CHANNEL_FAILLE(),"BAN : Utilise une faille critique. Double stuff sur même case. à vérifier et bannir immédiatement !", this );
            this.banAccount();
        }

        this.verifEquiped();
        if (this.isOnMount() && this.getObjetByPos(Constant.ITEM_POS_FAMILIER) != null)
            this.unequipedObjet(this.getObjetByPos(Constant.ITEM_POS_FAMILIER));
    }

    public void useZaap(short id) {
        if (this.getExchangeAction() == null || this.getExchangeAction().getType() != ExchangeAction.IN_ZAAPING)
            return;//S'il n'a pas ouvert l'interface Zaap(hack?)

        if (fight != null)
            return;//Si il combat

        if (!_zaaps.contains(id))
            return;//S'il n'a pas le zaap demand�(ne devrais pas arriver)

        int cost = Formulas.calculZaapCost(curMap, World.world.getMap(id));
        if (kamas < cost || curMap == World.world.getMap(id) )
            return; //S'il n'a pas les kamas (verif cot� client)

        if (cost < 0)
            return;

        short mapID = id;
        int SubAreaID = curMap.getSubArea().getArea().getSuperArea();
        int cellID = World.world.getZaapCellIdByMapId(id);
        if (World.world.getMap(mapID) == null) {
            //GameServer.a();
            SocketManager.GAME_SEND_WUE_PACKET(this);
            return;
        }
        if (World.world.getMap(mapID).getCase(cellID) == null) {
            //GameServer.a();
            SocketManager.GAME_SEND_WUE_PACKET(this);
            return;
        }
        if (!World.world.getMap(mapID).getCase(cellID).isWalkable(true)) {
            //GameServer.a();
            SocketManager.GAME_SEND_WUE_PACKET(this);
            return;
        }
        if (World.world.getMap(mapID).getSubArea().getArea().getSuperArea() != SubAreaID) {
            SocketManager.GAME_SEND_WUE_PACKET(this);
            return;
        }
        if (id == 4263 && this.get_align() == 2)
            return;
        if (id == 5295 && this.get_align() == 1)
            return;
        kamas -= cost;
        teleport(mapID, cellID);
        SocketManager.GAME_SEND_STATS_PACKET(this);//On envoie la perte de kamas
        SocketManager.GAME_SEND_WV_PACKET(this);//On ferme l'interface Zaap
        this.setExchangeAction(null);
    }

    public void usePrisme(String packet) {
        if (this.getExchangeAction() == null || this.getExchangeAction().getType() != ExchangeAction.IN_PRISM)
            return;

        int celdaID = 340;
        short MapID = 7411;
        boolean canGo = false;

        for (Prism Prisme : World.world.AllPrisme()) {
            if (Prisme.getMap() == Short.valueOf(packet.substring(2))) {
                celdaID = Prisme.getCell();
                MapID = Prisme.getMap();
                canGo = true;
                break;
            }
        }

        if (!canGo) {
            //World.sendWebhookMessage(Config.INSTANCE.getDISCORD_CHANNEL_FAILLE(),"**" + this.getName() + "** a tenté d'utiliser une faille lié au TP PRISME. (ID PERSO: **" + this.getId() + "**)",this );
            this.send("Im182");
            return;
        }

        int costo = Formulas.calculZaapCost(curMap, World.world.getMap(MapID));
        if (MapID == curMap.getId())
            costo = 0;

        if (kamas < costo || costo < 0) {
            SocketManager.GAME_SEND_MESSAGE(this, "Vous n'avez pas suffisamment de Kamas pour réaliser cette action.");
            return;
        }

        kamas -= costo;
        SocketManager.GAME_SEND_STATS_PACKET(this);
        this.teleport(MapID, celdaID);


        SocketManager.SEND_Ww_CLOSE_Prisme(this);
        this.setExchangeAction(null);
    }

    public String parseZaaps() {
        StringBuilder str = new StringBuilder();
        boolean first = true;

        if (_zaaps.isEmpty())
            return "";
        for (int i : _zaaps) {
            if (!first)
                str.append(",");
            first = false;
            str.append(i);
        }
        return str.toString();
    }

    public String parsePrisme() {
        String str = "";
        Prism Prisme = World.world.getPrisme(curMap.getSubArea().getPrismId());
        if (Prisme == null)
            str = "-3";
        else if (Prisme.getInFight() == 0) {
            str = "0;" + Prisme.getTurnTime() + ";45000;7";
        } else {
            str = Prisme.getInFight() + "";
        }
        return str;
    }

    public void stopZaaping() {
        if (this.getExchangeAction() == null || this.getExchangeAction().getType() != ExchangeAction.IN_ZAAPING)
            return;

        this.setExchangeAction(null);
        SocketManager.GAME_SEND_WV_PACKET(this);
    }

    public void Zaapi_close() {
        if (this.getExchangeAction() == null || this.getExchangeAction().getType() != ExchangeAction.IN_ZAPPI)
            return;
        this.setExchangeAction(null);
        SocketManager.GAME_SEND_CLOSE_ZAAPI_PACKET(this);
    }

    public void Prisme_close() {
        if (this.getExchangeAction() == null || this.getExchangeAction().getType() != ExchangeAction.IN_PRISM)
            return;
        this.setExchangeAction(null);
        SocketManager.SEND_Ww_CLOSE_Prisme(this);
    }

    public void Zaapi_use(String packet) {
        if (this.getExchangeAction() == null || this.getExchangeAction().getType() != ExchangeAction.IN_ZAPPI)
            return;
        GameMap map = World.world.getMap(Short.valueOf(packet.substring(2)));

        short cell = 100;
        if (map != null) {
            for (GameCase entry : map.getCases()) {
                InteractiveObject obj = entry.getObject();
                if (obj != null) {
                    if (obj.getId() == 7031 || obj.getId() == 7030) {
                        cell = (short) (entry.getId() + 18);
                    }
                }
            }
            if (map.getSubArea() != null && (map.getSubArea().getArea().getId() == 7 || map.getSubArea().getArea().getId() == 11)) {
                int price = 20;
                if (this.get_align() == 1 || this.get_align() == 2)
                    price = 10;
                kamas -= price;
                SocketManager.GAME_SEND_STATS_PACKET(this);
                if ((map.getSubArea().getArea().getId() == 7 && this.getCurMap().getSubArea().getArea().getId() == 7)
                        || (map.getSubArea().getArea().getId() == 11 && this.getCurMap().getSubArea().getArea().getId() == 11)) {
                    this.teleport(Short.valueOf(packet.substring(2)), cell);
                }
                SocketManager.GAME_SEND_CLOSE_ZAAPI_PACKET(this);
                this.setExchangeAction(null);
            }
        }
    }

    public boolean hasItemTemplate(int i, int q) {
        for (GameObject obj : objects.values()) {
            /*if (obj.getPosition() != Constant.ITEM_POS_NO_EQUIPED)
                continue;*/
            if (obj.getTemplate().getId() != i)
                continue;

            if (obj.getQuantity() >= q)
                return true;
        }
        return false;
    }

    public boolean hasItemType(int type) {
        for (GameObject obj : objects.values()) {
            if (obj.getPosition() != Constant.ITEM_POS_NO_EQUIPED)
                continue;
            if (obj.getTemplate().getType() == type)
                return true;
        }

        return false;
    }

    public GameObject getItemTemplate(int i, int q) {
        for (GameObject obj : objects.values()) {
            if (obj.getPosition() != Constant.ITEM_POS_NO_EQUIPED)
                continue;
            if (obj.getTemplate().getId() != i)
                continue;
            if (obj.getQuantity() >= q)
                return obj;
        }
        return null;
    }

    public GameObject getItemTemplate(int i) {

        for (GameObject obj : objects.values()) {
            if (obj.getTemplate().getId() != i)
                continue;
            return obj;
        }

        return null;
    }

    public int getNbItemTemplate(int i) {
        for (GameObject obj : objects.values()) {
            if (obj.getTemplate().getId() != i)
                continue;
            return obj.getQuantity();
        }
        return -1;
    }

    public boolean isDispo(Player sender) {
        return !_isAbsent && (!_isInvisible || account.isFriendWith(sender.getAccount().getId()));

    }

    public boolean get_isClone() {
        return _isClone;
    }

    public void set_isClone(boolean isClone) {
        _isClone = isClone;
    }

    public byte get_title() {
        return _title;
    }

    public void set_title(int i) {
        _title = (byte) i;
    }

    //FIN CLONAGE
    public void VerifAndChangeItemPlace() {
        boolean isFirstAM = true;
        boolean isFirstAN = true;
        boolean isFirstANb = true;
        boolean isFirstAR = true;
        boolean isFirstBO = true;
        boolean isFirstBOb = true;
        boolean isFirstCA = true;
        boolean isFirstCE = true;
        boolean isFirstCO = true;
        boolean isFirstDa = true;
        boolean isFirstDb = true;
        boolean isFirstDc = true;
        boolean isFirstDd = true;
        boolean isFirstDe = true;
        boolean isFirstDf = true;
        boolean isFirstFA = true;

        for (GameObject obj : objects.values()) {
            if (obj.getPosition() == Constant.ITEM_POS_NO_EQUIPED)
                continue;
            if (obj.getPosition() == Constant.ITEM_POS_AMULETTE) {
                if (isFirstAM) {
                    isFirstAM = false;
                } else {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosition() == Constant.ITEM_POS_ANNEAU1) {
                if (isFirstAN) {
                    isFirstAN = false;
                } else {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosition() == Constant.ITEM_POS_ANNEAU2) {
                if (isFirstANb) {
                    isFirstANb = false;
                } else {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosition() == Constant.ITEM_POS_ARME) {
                if (isFirstAR) {
                    isFirstAR = false;
                } else {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosition() == Constant.ITEM_POS_BOTTES) {
                if (isFirstBO) {
                    isFirstBO = false;
                } else {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosition() == Constant.ITEM_POS_BOUCLIER) {
                if (isFirstBOb) {
                    isFirstBOb = false;
                } else {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosition() == Constant.ITEM_POS_CAPE) {
                if (isFirstCA) {
                    isFirstCA = false;
                } else {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosition() == Constant.ITEM_POS_CEINTURE) {
                if (isFirstCE) {
                    isFirstCE = false;
                } else {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosition() == Constant.ITEM_POS_COIFFE) {
                if (isFirstCO) {
                    isFirstCO = false;
                } else {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosition() == Constant.ITEM_POS_DOFUS1) {
                if (isFirstDa) {
                    isFirstDa = false;
                } else {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosition() == Constant.ITEM_POS_DOFUS2) {
                if (isFirstDb) {
                    isFirstDb = false;
                } else {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosition() == Constant.ITEM_POS_DOFUS3) {
                if (isFirstDc) {
                    isFirstDc = false;
                } else {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosition() == Constant.ITEM_POS_DOFUS4) {
                if (isFirstDd) {
                    isFirstDd = false;
                } else {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosition() == Constant.ITEM_POS_DOFUS5) {
                if (isFirstDe) {
                    isFirstDe = false;
                } else {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosition() == Constant.ITEM_POS_DOFUS6) {
                if (isFirstDf) {
                    isFirstDf = false;
                } else {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosition() == Constant.ITEM_POS_FAMILIER) {
                if (isFirstFA) {
                    isFirstFA = false;
                } else {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                }
            }
        }
    }

    public void kick(){
        this.getGameClient().kick();
    }
    //Mariage

    public Stalk get_traque() {
        return _traqued;
    }

    public void set_traque(Stalk traq) {
        _traqued = traq;
    }

    public void setWife(int id) {
        this.wife = id;
        Database.getStatics().getPlayerData().update(this);
    }

    public String get_wife_friendlist() {
        Player wife = World.world.getPlayer(this.wife);
        StringBuilder str = new StringBuilder();
        if (wife != null) {
            int color1 = wife.getColor1(), color2 = wife.getColor2(), color3 = wife.getColor3();
            if (wife.getObjetByPos(Constant.ITEM_POS_MALEDICTION) != null)
                if (wife.getObjetByPos(Constant.ITEM_POS_MALEDICTION).getTemplate().getId() == 10838) {
                    color1 = 16342021;
                    color2 = 16342021;
                    color3 = 16342021;
                }
            str.append(wife.getName()).append("|").append(wife.getGfxId()).append("|").append(color1).append("|").append(color2).append("|").append(color3).append("|");
            if (!wife.isOnline()) {
                str.append("|");
            } else {
                str.append(wife.parse_towife()).append("|");
            }
        } else {
            str.append("|");
        }
        return str.toString();
    }

    public String parse_towife() {
        int f = 0;
        if (fight != null) {
            f = 1;
        }
        return curMap.getId() + "|" + this.getLevel() + "|" + f;
    }

    public void meetWife(Player p)// Se teleporter selon les sacro-saintes autorisations du mariage.
    {
        if (p == null)
            return; // Ne devrait theoriquement jamais se produire.

        if (this.getPodUsed() >= this.getMaxPod()) // Refuser la t�l�portation si on est full pods.
        {
            SocketManager.GAME_SEND_Im_PACKET(this, "170");
            return;
        }

        int dist = (curMap.getX() - p.getCurMap().getX())
                * (curMap.getX() - p.getCurMap().getX())
                + (curMap.getY() - p.getCurMap().getY())
                * (curMap.getY() - p.getCurMap().getY());
        if (dist > 100 || p.getCurMap().getId() == this.getCurMap().getId())// La distance est trop grande...
        {
            if (p.getSexe() == 0)
                SocketManager.GAME_SEND_Im_PACKET(this, "178");
            else
                SocketManager.GAME_SEND_Im_PACKET(this, "179");
            return;
        }

        int cellPositiontoadd = Constant.getNearestCellIdUnused(p);
        if (cellPositiontoadd == -1) {
            if (p.getSexe() == 0)
                SocketManager.GAME_SEND_Im_PACKET(this, "141");
            else
                SocketManager.GAME_SEND_Im_PACKET(this, "142");
            return;
        }

        teleport(p.getCurMap().getId(), cellPositiontoadd);
    }

    public void Divorce() {
        if (isOnline())
            SocketManager.GAME_SEND_Im_PACKET(this, "047;"
                    + World.world.getPlayer(wife).getName());

        wife = 0;
        Database.getStatics().getPlayerData().update(this);
    }

    public int getWife() {
        return wife;
    }

    public int setisOK(int ok) {
        return _isOK = ok;
    }

    public int getisOK() {
        return _isOK;
    }

    public List<GameObject> getEquippedObjects() {
        List<GameObject> objects = new ArrayList<>();
        this.objects.values().stream().filter(object -> object.getPosition() != -1 && object.getPosition() < 75).forEach(objects::add);
        return objects;
    }

    public List<GameObject> getFragmentObject() {
        List<GameObject> objects = new ArrayList<>();
        this.objects.values().stream().filter(object -> object.getTemplate().getId() == 8378).forEach(objects::add);
        return objects;
    }

    public List<GameObject> getParcheminMetierObject() {
        List<GameObject> objects = new ArrayList<>();
        this.objects.values().stream().filter(object -> Constant.getParcheminMetierID().contains(object.getTemplate().getId())).forEach(objects::add);
        return objects;
    }


    public String SetsPacket(){
        String packetToSend = "Os";
        int playerid = this.getId();
        List<QuickSets> sets = World.world.getSetsByPlayer(playerid);
        //int i =0;
        if(sets != null) {
            for (QuickSets set : sets) {
                //if(i!=0){
                //  packetToSend += "*";
                //}
                packetToSend += set.getNb() + "|" + set.getName() + "|" + set.getIcon() + "|" + set.getObjects() + "*";
                //i++;
            }
        }
        else{
            return null;
        }
        return packetToSend;
    }

    public void changeOrientation(int toOrientation) {
        if (this.get_orientation() == 0 || this.get_orientation() == 2
                || this.get_orientation() == 4 || this.get_orientation() == 6) {
            this.set_orientation(toOrientation);
            SocketManager.GAME_SEND_eD_PACKET_TO_MAP(getCurMap(), this.getId(), toOrientation);
        }
    }

    /** Heroic **/
    private byte dead = 0, deadType = 0;
    private long deadTime = 0, killByTypeId = 0, totalKills = 0;

    public byte isDead() {
        return dead;
    }

    public byte getNeedRestat() {
        return needRestat;
    }

    public void setNeedRestat(Byte Needrestat) {
        this.needRestat = Needrestat;
    }

    public void increaseTotalKills() {
        this.totalKills++;
    }

    public long getTotalKills() {
        return totalKills;
    }

    public String getDeathInformation() {
        return dead + "," + deadTime + "," + deadType + "," + killByTypeId;
    }

    public void die(byte type, long id) {
        new ArrayList<>(this.getItems().values()).stream().filter(object -> object != null).forEach(object -> this.removeItem(object.getGuid(), object.getQuantity(), true, false));
        this.setFuneral();
        //this.deathCount++;
        this.deadType = type;
        this.killByTypeId = id;
    }

    public void revive() {
        byte revive = Database.getStatics().getPlayerData().canRevive(this);

        if(revive == 1) {
            this.curMap = World.world.getMap((short) 7411);
            this.curCell = World.world.getMap((short) 7411).getCase(311);
        } else {
            this.getStats().addOneStat(125, -this.getStats().getEffect(125));
            this.getStats().addOneStat(124, -this.getStats().getEffect(124));
            this.getStats().addOneStat(118, -this.getStats().getEffect(118));
            this.getStats().addOneStat(123, -this.getStats().getEffect(123));
            this.getStats().addOneStat(119, -this.getStats().getEffect(119));
            this.getStats().addOneStat(126, -this.getStats().getEffect(126));
            this.addCapital((this.getLevel() - 1) * 5 - this.get_capital());
            this.getStatsParcho().getEffects().clear();
            this._sorts = Constant.getStartSorts(classe);
            this._sortsPlaces = Constant.getStartSortsPlaces(classe);
            this.level = 1;
            this.exp = 0;
            this.curMap = World.world.getMap(Constant.getStartMap(this.classe));
            this.curCell = this.curMap.getCase(Constant.getStartCell(this.classe));
        }
        this._honor = 0;
        this._deshonor = 0;
        this._align = 0;
        this.kamas = 0;
        this._metiers.clear();
        if(this._mount != null) {
            for(GameObject gameObject : this._mount.getObjects().values())
                World.world.removeGameObject(gameObject.getGuid());
            this._mount.getObjects().clear();

            this.setMount(null);
            this.setMountGiveXp(0);
        }
        this.isGhost = false;
        this.dead = 0;
        this.setEnergy(10000);
        this.setGfxId(Integer.parseInt(this.getClasse() + "" + this.getSexe()));
        this.setCanAggro(true);
        this.setAway(false);
        this.setSpeed(0);

        Database.getStatics().getPlayerData().setRevive(this);
    }
    /** End heroic **/

    public boolean isGhost() {
        return this.isGhost;
    }

    public void setFuneral() {
        this.dead = 1;
        this.deadTime = System.currentTimeMillis();
        this.setEnergy(-1);
        if (this.isOnMount())
            this.toogleOnMount();
        if (this.get_orientation() == 2) {
            this.set_orientation(1);
            SocketManager.GAME_SEND_eD_PACKET_TO_MAP(this.getCurMap(), this.getId(), 1);
        }
        this.setGfxId(Integer.parseInt(this.getClasse() + "3"));
        SocketManager.send(this, "AR3K");//Block l'orientation
        SocketManager.send(this, "M112");//T'es mort!!! t'es mort!!! Mouhhahahahahaaaarg
        SocketManager.GAME_SEND_ALTER_GM_PACKET(getCurMap(), this);
    }

    public void setGhost() {
        if (isOnMount())
            toogleOnMount();
        if(Config.INSTANCE.getHEROIC()) {
            this.setGfxId(Integer.parseInt(this.getClasse() + "" + this.getSexe()));
            this.send("GO");
            return;
        }
        if(this.getEnergy() != 0)
            Constant.tpCim(this.getCurMap().getSubArea().getArea().getId(), this);
        this.dead = 0;
        this.isGhost = true;
        this.setEnergy(0);
        setGfxId(8004);
        setCanAggro(false);
        setAway(true);
        setSpeed(-40);
        this.regenRate = 0;
        SocketManager.send(this, "IH" + Constant.ALL_PHOENIX);
    }

    public void setAlive() {
        if (!this.isGhost)
            return;
        this.isGhost = false;
        this.dead = 0;
        this.setEnergy(1000);
        this.setPdv(1);
        this.setGfxId(Integer.parseInt(this.getClasse() + "" + this.getSexe()));
        this.setCanAggro(true);
        this.setAway(false);
        this.setSpeed(0);
        SocketManager.GAME_SEND_MESSAGE(this, "Tu as gagné <b>1000</b> points d'énergie.", "009900");
        SocketManager.GAME_SEND_STATS_PACKET(this);
        SocketManager.GAME_SEND_ALTER_GM_PACKET(this.curMap, this);
        SocketManager.send(this, "IH");
        SocketManager.send(this, "AR6bk");//Block l'orientation
    }

    public Map<Long, Integer> getStoreItems() {
        return _storeItems;
    }

    public int needEndFight() {
        return hasEndFight;
    }

    public Monster.MobGroup hasMobGroup() {
        return hasMobGroup;
    }

    public void setNeededEndFight(int hasEndFight, Monster.MobGroup group) {
        this.endFightAction = null;
        this.hasEndFight = hasEndFight;
        this.hasMobGroup = group;
    }

    public void setNeededEndFightAction(Action endFightAction) {
        this.hasEndFight = -2;
        this.endFightAction = endFightAction;
    }

    public boolean castEndFightAction() {
        if(this.endFightAction != null) {
            this.endFightAction.apply(this, null, -1, -1);
            this.endFightAction = null;
        } else
            return true;
        return false;
    }

    public String parseStoreItemsList() {
        StringBuilder list = new StringBuilder();
        if (_storeItems.isEmpty())
            return "";
        for (Entry<Long, Integer> obj : _storeItems.entrySet()) {
            GameObject O = World.world.getGameObject(obj.getKey());
            if (O == null)
                continue;
            //O.getPoidOfBaseItem(O.getPlayerId());
            list.append(O.getGuid()).append(";").append(O.getQuantity()).append(";").append(O.getTemplate().getId()).append(";").append(O.parseStatsString()).append(";").append(obj.getValue()).append(";").append(O.getRarity()).append("|");
        }

        return (list.length() > 0 ? list.toString().substring(0, list.length() - 1) : list.toString());
    }

    public int parseStoreItemsListPods() {
        if (_storeItems.isEmpty())
            return 0;
        int total = 0;
        for (Entry<Long, Integer> obj : _storeItems.entrySet()) {
            GameObject O = World.world.getGameObject(obj.getKey());
            if (O != null) {
                int qua = O.getQuantity();
                int poidBase1 = O.getTemplate().getPod() * qua;
                total += poidBase1;
            }
        }
        return total;
    }

    public String parseStoreItemstoBD() {
        StringBuilder str = new StringBuilder();
        for (Entry<Long, Integer> _storeObjets : _storeItems.entrySet()) {
            str.append(_storeObjets.getKey()).append(",").append(_storeObjets.getValue()).append("|");
        }

        return str.toString();
    }

    public void addInStore(long ObjID, int price, int qua) {
        GameObject PersoObj = World.world.getGameObject(ObjID);
        //Si le joueur n'a pas l'item dans son sac ...
        if(PersoObj.isAttach()) return;
        if (_storeItems.get(ObjID) != null) {
                _storeItems.remove(ObjID);
                _storeItems.put(ObjID, price);
                SocketManager.GAME_SEND_ITEM_LIST_PACKET_SELLER(this, this);
                return;
        }

        if (objects.get(ObjID) == null) {
            GameServer.a("Pas d'objet " + ObjID);
            return;
        }

        //Si c'est un item �quip� ...
        if (PersoObj.getPosition() != Constant.ITEM_POS_NO_EQUIPED)
            return;

        GameObject SimilarObj = getSimilarStoreItem(PersoObj);
        int newQua = PersoObj.getQuantity() - qua;
        if (SimilarObj == null)//S'il n'y pas d'item du meme Template
        {
            //S'il ne reste pas d'item dans le sac
            if (newQua <= 0) {
                //On enleve l'objet du sac du joueur
                removeItem(PersoObj.getGuid());
                //On met l'objet du sac dans le store, avec la meme quantit�
                _storeItems.put(PersoObj.getGuid(), price);
                SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, PersoObj.getGuid());
                SocketManager.GAME_SEND_ITEM_LIST_PACKET_SELLER(this, this);
            } else
            //S'il reste des objets au joueur
            {
                //on modifie la quantit� d'item du sac
                PersoObj.setQuantity(newQua);
                //On ajoute l'objet a la banque et au monde
                SimilarObj = GameObject.getCloneObjet(PersoObj, qua);
                World.world.addGameObject(SimilarObj, true);
                _storeItems.put(SimilarObj.getGuid(), price);

                //Envoie des packets
                SocketManager.GAME_SEND_ITEM_LIST_PACKET_SELLER(this, this);
                SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, PersoObj);

            }
        } else
        // S'il y avait un item du meme template
        {
            //S'il ne reste pas d'item dans le sac
            if (newQua <= 0) {
                //On enleve l'objet du sac du joueur
                removeItem(PersoObj.getGuid());
                //On enleve l'objet du monde
                World.world.removeGameObject(PersoObj.getGuid());
                //On ajoute la quantit� a l'objet en banque
                SimilarObj.setQuantity(SimilarObj.getQuantity() + PersoObj.getQuantity());

                _storeItems.remove(SimilarObj.getGuid());
                _storeItems.put(SimilarObj.getGuid(), price);

                //on envoie l'ajout a la banque de l'objet
                SocketManager.GAME_SEND_ITEM_LIST_PACKET_SELLER(this, this);
                //on envoie la supression de l'objet du sac au joueur
                SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, PersoObj.getGuid());
            } else
            //S'il restait des objets
            {
                //on modifie la quantit� d'item du sac
                PersoObj.setQuantity(newQua);
                SimilarObj.setQuantity(SimilarObj.getQuantity() + qua);

                _storeItems.remove(SimilarObj.getGuid());
                _storeItems.put(SimilarObj.getGuid(), price);

                SocketManager.GAME_SEND_ITEM_LIST_PACKET_SELLER(this, this);
                SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, PersoObj);

            }
        }
        SocketManager.GAME_SEND_Ow_PACKET(this);
        Database.getStatics().getPlayerData().update(this);
    }

    private GameObject getSimilarStoreItem(GameObject exGameObject) {
        for (Long id : _storeItems.keySet()) {
            GameObject gameObject = World.world.getGameObject(id);
            if (World.world.getConditionManager().stackIfSimilar2(gameObject, exGameObject, true))
                return gameObject;
        }

        return null;
    }

    public void removeFromStore(long guid, int qua) {
        GameObject SimilarObj = World.world.getGameObject(guid);
        //Si le joueur n'a pas l'item dans son store ...
        if (_storeItems.get(guid) == null) {
            GameServer.a("Pas d'objet "+ guid + " chez " + this.getName());
            return;
        }

        GameObject PersoObj = getSimilarItem(SimilarObj);
        int newQua = SimilarObj.getQuantity() - qua;
        if (PersoObj == null)//Si le joueur n'avait aucun item similaire
        {
            //S'il ne reste rien en store
            if (newQua <= 0) {
                //On retire l'item du store
                _storeItems.remove(guid);
                //On l'ajoute au joueur
                objects.put(guid, SimilarObj);

                //On envoie les packets
                SocketManager.GAME_SEND_OAKO_PACKET(this, SimilarObj);
                SocketManager.GAME_SEND_ITEM_LIST_PACKET_SELLER(this, this);
            }
        } else {
            //S'il ne reste rien en store
            if (newQua <= 0) {
                //On retire l'item de la banque
                _storeItems.remove(SimilarObj.getGuid());
                World.world.removeGameObject(SimilarObj.getGuid());
                //On Modifie la quantit� de l'item du sac du joueur
                PersoObj.setQuantity(PersoObj.getQuantity()
                        + SimilarObj.getQuantity());
                //On envoie les packets
                SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, PersoObj);
                SocketManager.GAME_SEND_ITEM_LIST_PACKET_SELLER(this, this);
            }
        }
        SocketManager.GAME_SEND_Ow_PACKET(this);
        Database.getStatics().getPlayerData().update(this);
    }

    public void removeStoreItem(int guid) {
        _storeItems.remove(guid);
    }

    public void addStoreItem(long guid, int price) {
        _storeItems.put(guid, price);
    }

    public int getSpeed() {
        return _Speed;
    }

    public void setSpeed(int _Speed) {
        this._Speed = _Speed;
    }

    public int get_savestat() {
        return this.savestat;
    }

    public void set_savestat(int stat) {
        this.savestat = stat;
    }

    public boolean getMetierPublic() {
        return _metierPublic;
    }

    public void setMetierPublic(boolean b) {
        _metierPublic = b;
    }

    public boolean getLivreArtisant() {
        return _livreArti;
    }

    public void setLivreArtisant(boolean b) {
        _livreArti = b;
    }

    public boolean hasSpell(int spellID) {
        return (getSortStatBySortIfHas(spellID) != null);
    }

    public void leaveEnnemyFaction() {
        if (!isInEnnemyFaction)
            return;//pas en prison on fait pas la commande
        int pGrade = this.getGrade();
        long compar = System.currentTimeMillis()
                - (enteredOnEnnemyFaction + 60000 * pGrade);

        switch (pGrade) {
            case 1:
                if (compar >= 0) {
                    leaveFaction();
                    this.sendMessage("Vous venez d'être libérer de prison après 1 minutes d'attente.");
                } else {
                    long restant = -compar;
                    if (restant <= 1000)
                        restant = 1000;
                    this.sendMessage("Vous devez attendre encore " + restant / 1000 + " secondes en prison.");
                }
                break;
            case 2:
                if (compar >= 0) {
                    leaveFaction();
                    this.sendMessage("Vous venez d'�tre lib�r� de prison apr�s 2 minutes d'attente.");
                } else {
                    long restant = -compar;
                    if (restant <= 1000)
                        restant = 1000;
                    this.sendMessage("Vous devez attendre encore " + restant / 1000 + " secondes en prison.");
                }
                break;
            case 3:
                if (compar >= 0) {
                    leaveFaction();
                    this.sendMessage("Vous venez d'�tre lib�r� de prison apr�s 3 minutes d'attente.");
                } else {
                    long restant = -compar;
                    if (restant <= 1000)
                        restant = 1000;
                    this.sendMessage("Vous devez attendre encore "
                            + restant / 1000 + " secondes en prison.");
                }
                break;
            case 4:
                if (compar >= 0) {
                    leaveFaction();
                    this.sendMessage("Vous venez d'�tre lib�r� de prison apr�s 4 minutes d'attente.");
                } else {
                    long restant = -compar;
                    if (restant <= 1000)
                        restant = 1000;
                    this.sendMessage("Vous devez attendre encore "
                            + restant / 1000 + " secondes en prison.");
                }
                break;
            case 5:
                if (compar >= 0) {
                    leaveFaction();
                    this.sendMessage("Vous venez d'�tre lib�r� de prison apr�s 5 minutes d'attente.");
                } else {
                    long restant = -compar;
                    if (restant <= 1000)
                        restant = 1000;
                    this.sendMessage("Vous devez attendre encore "
                            + restant / 1000 + " secondes en prison.");
                }
                break;
            case 6:
                if (compar >= 0) {
                    leaveFaction();
                    this.sendMessage("Vous venez d'�tre lib�r� de prison apr�s 6 minutes d'attente.");
                } else {
                    long restant = -compar;
                    if (restant <= 1000)
                        restant = 1000;
                    this.sendMessage("Vous devez attendre encore "
                            + restant / 1000 + " secondes en prison.");
                }
                break;
            case 7:
                if (compar >= 0) {
                    leaveFaction();
                    this.sendMessage("Vous venez d'�tre lib�r� de prison apr�s 7 minutes d'attente.");
                } else {
                    long restant = -compar;
                    if (restant <= 1000)
                        restant = 1000;
                    this.sendMessage("Vous devez attendre encore "
                            + restant / 1000 + " secondes en prison.");
                }
                break;
            case 8:
                if (compar >= 0) {
                    leaveFaction();
                    this.sendMessage("Vous venez d'�tre lib�r� de prison apr�s 8 minutes d'attente.");
                } else {
                    long restant = -compar;
                    if (restant <= 1000)
                        restant = 1000;
                    this.sendMessage("Vous devez attendre encore "
                            + restant / 1000 + " secondes en prison.");
                }
                break;
            case 9:
                if (compar >= 0) {
                    leaveFaction();
                    this.sendMessage("Vous venez d'�tre lib�r� de prison apr�s 9 minutes d'attente.");
                } else {
                    long restant = -compar;
                    if (restant <= 1000)
                        restant = 1000;
                    this.sendMessage("Vous devez attendre encore "
                            + restant / 1000 + " secondes en prison.");
                }
                break;
            case 10:
                if (compar >= 0) {
                    leaveFaction();
                    this.sendMessage("Vous venez d'�tre lib�r� de prison apr�s 10 minutes d'attente.");
                } else {
                    long restant = -compar;
                    if (restant <= 1000)
                        restant = 1000;
                    this.sendMessage("Vous devez attendre encore "
                            + restant / 1000 + " secondes en prison.");
                }
                break;
        }
        Database.getStatics().getPlayerData().update(this);
    }

    public void leaveEnnemyFactionAndPay(Player perso) {
        if (!isInEnnemyFaction)
            return;//pas en prison on fait pas la commande
        int pGrade = perso.getGrade();
        long curKamas = perso.getKamas();
        switch (pGrade) {
            case 1:
                if (curKamas < 1000) {
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu ne possédes que "
                            + curKamas
                            + "Kamas. Tu n'as pas assez d'argent pour sortir !", "009900");
                } else {
                    int countKamas = 1000;
                    long newKamas = curKamas - countKamas;
                    if (newKamas < 0)
                        newKamas = 0;
                    perso.setKamas(newKamas);
                    leaveFaction();
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu viens de payer "
                            + countKamas
                            + "Kamas pour sortir. Il te reste maintenant "
                            + newKamas + "Kamas.", "009900");
                }
                break;
            case 2:
                if (curKamas < 2000) {
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu ne possédes que "
                            + curKamas
                            + "Kamas. Tu n'as pas assez d'argent pour sortir !", "009900");
                } else {
                    int countKamas = 2000;
                    long newKamas = curKamas - countKamas;
                    if (newKamas < 0)
                        newKamas = 0;
                    perso.setKamas(newKamas);
                    leaveFaction();
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu viens de payer "
                            + countKamas
                            + "Kamas pour sortir. Il te reste maintenant "
                            + newKamas + "Kamas.", "009900");
                }
                break;
            case 3:
                if (curKamas < 3000) {
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu ne possédes que "
                            + curKamas
                            + "Kamas. Tu n'as pas assez d'argent pour sortir !", "009900");
                } else {
                    int countKamas = 3000;
                    long newKamas = curKamas - countKamas;
                    if (newKamas < 0)
                        newKamas = 0;
                    perso.setKamas(newKamas);
                    leaveFaction();
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu viens de payer "
                            + countKamas
                            + "Kamas pour sortir. Il te reste maintenant "
                            + newKamas + "Kamas.", "009900");
                }
                break;
            case 4:
                if (curKamas < 4000) {
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu ne possédes que "
                            + curKamas
                            + "Kamas. Tu n'as pas assez d'argent pour sortir !", "009900");
                } else {
                    int countKamas = 4000;
                    long newKamas = curKamas - countKamas;
                    if (newKamas < 0)
                        newKamas = 0;
                    perso.setKamas(newKamas);
                    leaveFaction();
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu viens de payer "
                            + countKamas
                            + "Kamas pour sortir. Il te reste maintenant "
                            + newKamas + "Kamas.", "009900");
                }
                break;
            case 5:
                if (curKamas < 5000) {
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu ne possédes que "
                            + curKamas
                            + "Kamas. Tu n'as pas assez d'argent pour sortir !", "009900");
                } else {
                    int countKamas = 5000;
                    long newKamas = curKamas - countKamas;
                    if (newKamas < 0)
                        newKamas = 0;
                    perso.setKamas(newKamas);
                    leaveFaction();
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu viens de payer "
                            + countKamas
                            + "Kamas pour sortir. Il te reste maintenant "
                            + newKamas + "Kamas.", "009900");
                }
                break;
            case 6:
                if (curKamas < 7000) {
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu ne possédes que "
                            + curKamas
                            + "Kamas. Tu n'as pas assez d'argent pour sortir !", "009900");
                } else {
                    int countKamas = 7000;
                    long newKamas = curKamas - countKamas;
                    if (newKamas < 0)
                        newKamas = 0;
                    perso.setKamas(newKamas);
                    leaveFaction();
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu viens de payer "
                            + countKamas
                            + "Kamas pour sortir. Il te reste maintenant "
                            + newKamas + "Kamas.", "009900");
                }
                break;
            case 7:
                if (curKamas < 9000) {
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu ne possédes que "
                            + curKamas
                            + "Kamas. Tu n'as pas assez d'argent pour sortir !", "009900");
                } else {
                    int countKamas = 9000;
                    long newKamas = curKamas - countKamas;
                    if (newKamas < 0)
                        newKamas = 0;
                    perso.setKamas(newKamas);
                    leaveFaction();
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu viens de payer "
                            + countKamas
                            + "Kamas pour sortir. Il te reste maintenant "
                            + newKamas + "Kamas.", "009900");
                }
                break;
            case 8:
                if (curKamas < 12000) {
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu ne possédes que "
                            + curKamas
                            + "Kamas. Tu n'as pas assez d'argent pour sortir !", "009900");
                } else {
                    int countKamas = 12000;
                    long newKamas = curKamas - countKamas;
                    if (newKamas < 0)
                        newKamas = 0;
                    perso.setKamas(newKamas);
                    leaveFaction();
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu viens de payer "
                            + countKamas
                            + "Kamas pour sortir. Il te reste maintenant "
                            + newKamas + "Kamas.", "009900");
                }
                break;
            case 9:
                if (curKamas < 16000) {
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu ne possédes que "
                            + curKamas
                            + "Kamas. Tu n'as pas assez d'argent pour sortir !", "009900");
                } else {
                    int countKamas = 16000;
                    long newKamas = curKamas - countKamas;
                    if (newKamas < 0)
                        newKamas = 0;
                    perso.setKamas(newKamas);
                    leaveFaction();
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu viens de payer "
                            + countKamas
                            + "Kamas pour sortir. Il te reste maintenant "
                            + newKamas + "Kamas.", "009900");
                }
                break;
            case 10:
                if (curKamas < 25000) {
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu ne possédes que "
                            + curKamas
                            + "Kamas. Tu n'as pas assez d'argent pour sortir !", "009900");
                } else {
                    int countKamas = 25000;
                    long newKamas = curKamas - countKamas;
                    if (newKamas < 0)
                        newKamas = 0;
                    perso.setKamas(newKamas);
                    leaveFaction();
                    SocketManager.GAME_SEND_MESSAGE(perso, "Tu viens de payer "
                            + countKamas
                            + "Kamas pour sortir. Il te reste maintenant "
                            + newKamas + "Kamas.", "009900");
                }
                break;
        }
        Database.getStatics().getPlayerData().update(this);
        SocketManager.GAME_SEND_STATS_PACKET(perso);
    }

    public void leaveFaction() {
        try {
            isInEnnemyFaction = false;
            enteredOnEnnemyFaction = 0;
            warpToSavePos();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void teleportWithoutBlocked(short newMapID, int newCellID)//Aucune condition genre <<en_prison>> etc
    {
        GameClient PW = null;
        if (account.getGameClient() != null) {
            PW = account.getGameClient();
        }
        if (World.world.getMap(newMapID) == null) {
            //GameServer.a();
            return;
        }
        if (World.world.getMap(newMapID).getCase(newCellID) == null) {
            //GameServer.a();
            return;
        }
        if (PW != null) {
            SocketManager.GAME_SEND_GA2_PACKET(PW, this.getId());
            SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(curMap, this.getId());
        }
        curCell.removePlayer(this);
        curMap = World.world.getMap(newMapID);
        curCell = curMap.getCase(newCellID);

        //Verification de la Map
        //Verifier la validit� du mountpark
        if (curMap.getMountPark() != null
                && curMap.getMountPark().getOwner() > 0
                && curMap.getMountPark().getGuild().getId() != -1) {
            if (World.world.getGuild(curMap.getMountPark().getGuild().getId()) == null)//Ne devrait pas arriver
            {
                //GameServer.a();
                GameMap.removeMountPark(curMap.getMountPark().getGuild().getId());
            }
        }
        //Verifier la validit� du Collector
        if (Collector.getCollectorByMapId(curMap.getId()) != null) {
            if (World.world.getGuild(Collector.getCollectorByMapId(curMap.getId()).getGuildId()) == null)//Ne devrait pas arriver
            {
                //GameServer.a();
                Collector.removeCollector(Collector.getCollectorByMapId(curMap.getId()).getGuildId());
            }
        }

        if (PW != null) {
            SocketManager.GAME_SEND_MAPDATA(PW, newMapID, curMap.getDate(), curMap.getKey());
            curMap.addPlayer(this);
        }

        if (!follower.isEmpty())//On met a jour la Map des personnages qui nous suivent
        {
            for (Player t : follower.values()) {
                if (t.isOnline())
                    SocketManager.GAME_SEND_FLAG_PACKET(t, this);
                else
                    follower.remove(t.getId());
            }
        }
    }

    public void teleportFaction(int factionEnnemy) {
        short mapID = 0;
        int cellID = 0;
        enteredOnEnnemyFaction = System.currentTimeMillis();
        isInEnnemyFaction = true;

        switch (factionEnnemy) {
            case 1://bonta
                mapID = (short) 6164;
                cellID = 236;
                break;

            case 2://brakmar
                mapID = (short) 6171;
                cellID = 397;
                break;

            case 3://Seriane
                mapID = (short) 1002;
                cellID = 326;
                break;

            default://neutre(WTF? XD)
                mapID = (short) 8534;
                cellID = 297;
                break;
        }
        this.sendMessage("Vous êtes en prison !<br />\nVous devrez donc patientez quelques Minutes avant de pouvoir sortir.<br/>\nParlez au gardien de prison pour obtenir plus d'information.");
        if (this.getEnergy() <= 0) {
            if (isOnMount())
                toogleOnMount();
            this.isGhost = true;
            setGfxId(8004);
            setCanAggro(false);
            setAway(true);
            setSpeed(-40);
        }
        teleportWithoutBlocked(mapID, cellID);
        Database.getStatics().getPlayerData().update(this);
    }

    public String parsecolortomount() {
        int color1 = this.getColor1(), color2 = this.getColor2(), color3 = this.getColor3();
        if (this.getObjetByPos(Constant.ITEM_POS_MALEDICTION) != null)
            if (this.getObjetByPos(Constant.ITEM_POS_MALEDICTION).getTemplate().getId() == 10838) {
                color1 = 16342021;
                color2 = 16342021;
                color3 = 16342021;
            }
        return (color1 == -1 ? "" : Integer.toHexString(color1)) + ","
                + (color2 == -1 ? "" : Integer.toHexString(color2)) + ","
                + (color3 == -1 ? "" : Integer.toHexString(color3));
    }

    public boolean addObjetSimiler(GameObject objet, boolean hasSimiler, int oldID) {
        ObjectTemplate objModelo = objet.getTemplate();
        if (objModelo.getType() == 85 || objModelo.getType() == 18)
            return false;
        if (hasSimiler) {
            for (Entry<Long, GameObject> entry : objects.entrySet()) {
                GameObject obj = entry.getValue();
                if (obj.getPosition() == -1 && obj.getGuid() != oldID
                        && obj.getTemplate().getId() == objModelo.getId()
                        && obj.getStats().isSameStats(objet.getStats())
                        && World.world.getConditionManager().stackIfSimilar2(obj, objet, hasSimiler)) {
                    obj.setQuantity(obj.getQuantity() + objet.getQuantity());
                    SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, obj);
                    return true;
                }
            }
        }
        return false;
    }

    //region Objects class
    public Map<Integer, HashMap<Integer, Integer>> getObjectsClassSpell() {
        return objectsClassSpell;
    }

    public void addObjectClassSpell(int spell, int effect, int value) {
        if (!objectsClassSpell.containsKey(spell)) {
            HashMap<Integer, Integer> newMap = new HashMap<>();
            newMap.put(effect, value);
            objectsClassSpell.put(spell, newMap);
        }
        else
        {
            HashMap<Integer, Integer> map = objectsClassSpell.get(spell);
            if(map.containsKey(effect))
            {
                int newValue = map.get(effect) + value;
                map.remove(effect);
                map.put(effect, newValue);
            }
            else
            {
                map.put(effect, value);
            }
            objectsClassSpell.remove(spell);
            objectsClassSpell.put(spell, map);
        }
    }

    public void removeObjectClassSpell(int spell) {
        if (objectsClassSpell.containsKey(spell)) {
            objectsClassSpell.remove(spell);
        }
    }



    public void refreshObjectsClass() {
        for (int position = 1; position <= 74; position++) {
            if (getObjetByPos(position) == null)
                continue;
            final GameObject obj = getObjetByPos(position);
            final int template = obj.getTemplate().getId();

            if(obj.getSortStats().isEmpty()) continue;

            for (final String stat : obj.getSortStats()) {
                final String[] val = stat.split("#");
                final int effect = Integer.parseInt(val[0], 16);
                final int spell = Integer.parseInt(val[1], 16);
                final int modif = Integer.parseInt(val[3], 16);
                final String modifi = effect + ";" + spell + ";" + modif;
                SocketManager.SEND_SB_SPELL_BOOST(this, modifi);
                refreshItemClasseSpell(spell, effect, modif);
            }
        }
    }

    public int getValueOfClassObject(int spell, int effect) {
        int modif = 0;
        if (objectsClassSpell.containsKey(spell)) {
            if (objectsClassSpell.get(spell).containsKey(effect)) {
                return objectsClassSpell.get(spell).get(effect);
            }
        }
        return modif;
    }
    //endregion

    public int storeAllBuy() {
        int total = 0;
        for (Entry<Long, Integer> value : _storeItems.entrySet()) {
            GameObject O = World.world.getGameObject(value.getKey());
            int multiple = O.getQuantity();
            int add = value.getValue() * multiple;
            total += add;
        }

        return total;
    }

    public void DialogTimer() {
        TimerWaiter.addNext(() -> {
            if (this.getExchangeAction() == null || this.getExchangeAction().getType() != ExchangeAction.TRADING_WITH_COLLECTOR)
                return;
            if ((Integer) this.getExchangeAction().getValue() != 0) {
                Collector collector = World.world.getCollector((Integer) this.getExchangeAction().getValue());
                if (collector == null)
                    return;
                collector.reloadTimer();
                for (Player z : World.world.getGuild(collector.getGuildId()).getPlayers()) {
                    if (z == null)
                        continue;
                    if (z.isOnline()) {
                        SocketManager.GAME_SEND_gITM_PACKET(z, entity.Collector.parseToGuild(z.getGuild().getId()));
                        String str = "G" + collector.getFullName() + "|.|" + World.world.getMap(collector.getMap()).getX() + "|" + World.world.getMap(collector.getMap()).getY() + "|" + getName() + "|" + collector.getXp() + ";";

                        if (!collector.getLogObjects().equals(""))
                            str += collector.getLogObjects();

                        Player.this.getGuildMember().giveXpToGuild(collector.getXp());
                        SocketManager.GAME_SEND_gT_PACKET(z, str);
                    }
                }
                getCurMap().RemoveNpc(collector.getId());
                SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(getCurMap(), collector.getId());
                collector.delCollector(collector.getId());
                Database.getDynamics().getCollectorData().delete(collector.getId());
            }
            Database.getStatics().getPlayerData().update(getAccount().getCurrentPlayer());
            SocketManager.GAME_SEND_EV_PACKET(getGameClient());
            setAway(false);
        }, 5, TimeUnit.MINUTES);
    }

    public long getTimeTaverne() {
        return timeTaverne;
    }

    public void setTimeTaverne(long timeTaverne) {
        this.timeTaverne = timeTaverne;
        Database.getStatics().getPlayerData().updateTimeTaverne(this);
    }

    public GameAction getGameAction() {
        return _gameAction;
    }

    public void setGameAction(GameAction Action) {
        _gameAction = Action;
    }

    public int getAlignMap() {
        if (this.getCurMap().getSubArea() == null)
            return -1;
        if (this.getCurMap().getSubArea().getAlignement() == 0)
            return 1;
        if (this.getCurMap().getSubArea().getAlignement() == this.get_align())
            return 1;
        return -1;
    }

    public List<Integer> getEmotes() {
        return emotes;
    }

    public void addStaticEmote(int emote) {
        if (this.emotes.contains(emote))
            return;
        this.emotes.add(emote);
        if (!isOnline())
            return;
        SocketManager.GAME_SEND_EMOTE_LIST(this, getCompiledEmote(getEmotes()));
        SocketManager.GAME_SEND_STATS_PACKET(this);
        SocketManager.send(this, "eA" + emote);
    }

    public String parseEmoteToDB() {
        StringBuilder str = new StringBuilder();
        boolean isFirst = true;
        for (int i : emotes) {
            if (isFirst)
                str.append(i).append("");
            else
                str.append(";").append(i);
            isFirst = false;
        }
        return str.toString();
    }

    public boolean getBlockMovement() {
        return this.isBlocked;
    }

    public void setBlockMovement(boolean b) {
        this.isBlocked = b;
    }

    public GameClient getGameClient() {
        if(this.getAccount() != null){
            return this.getAccount().getGameClient();
        }
        else {
          if (this.getFight().getFighterByPerso(this).isInvocation()) {
                    return this.getFight().getFighterByPerso(this).getInvocator().getPlayer().getGameClient();
          } else {
                    if (this.getSlaveLeader() != null) {
                        return this.getSlaveLeader().getGameClient();
                    } else {
                        return this.getAccount().getGameClient();
                    }
          }
        }

    }

    public void send(String packet) {
        SocketManager.send(this, packet);
    }

    public void sendMessage(String msg) {
        SocketManager.GAME_SEND_MESSAGE(this, msg);
    }

    public void sendTypeMessage(String name, String msg) {
        this.send("Im116;(<b>" + name + "</b>)~" + msg);
    }

    public void sendServerMessage(String msg) {
        this.sendTypeMessage("Server", msg);
    }

    public boolean isSubscribe() {
        return !Config.INSTANCE.getSubscription() || this.getAccount().isSubscribe();
    }

    public boolean isInAreaNotSubscribe() {
        boolean ok = Config.INSTANCE.getSubscription();

        if (this.curMap == null)
            return false;
        switch (this.curMap.getId()) {
            case 6824:
            case 6825:
            case 6826:
                return false;
        }
        if (this.curMap.getSubArea() == null)
            return false;
        if (this.curMap.getSubArea().getArea() == null)
            return false;
        if (this.curMap.getSubArea().getArea().getSuperArea() == 3
                || this.curMap.getSubArea().getArea().getSuperArea() == 4
                || this.curMap.getSubArea().getArea().getId() == 18)
            ok = false;

        return ok;
    }

    public boolean cantDefie() {
        return getCurMap().mapNoDefie();
    }

    public boolean cantAgro() {
        return getCurMap().mapNoAgression();
    }

    public boolean cantCanal() {
        return getCurMap().noCanal;
    }

    public boolean cantTP() {
        return this.isInPrison() || getCurMap().mapNoTeleport() || EventManager.isInEvent(this);
    }

    public boolean isInPrison() {
        if (this.curMap == null)
            return false;

        switch (this.curMap.getId()) {
            case 666:
            case 8726:
                return true;
        }
        return false;
    }

    public void addQuestPerso(QuestPlayer qPerso) {
        questList.put(qPerso.getId(), qPerso);
    }

    public void delQuestPerso(int key) {
        this.questList.remove(key);
    }

    public Map<Integer, QuestPlayer> getQuestPerso() {
        return questList;
    }

    public QuestPlayer getQuestPersoByQuest(Quest quest) {
        for (QuestPlayer questPlayer : this.questList.values())
            if (questPlayer != null && questPlayer.getQuest().getId() == quest.getId())
                return questPlayer;
        return null;
    }

    public QuestPlayer getQuestPersoByQuestId(int id) {
        for (QuestPlayer qPerso : questList.values())
            if (qPerso.getQuest().getId() == id)
                return qPerso;
        return null;
    }

    public String getQuestGmPacket() {
        StringBuilder packet = new StringBuilder();
        int nb = 0;
        packet.append("+");
        for (QuestPlayer qPerso : questList.values()) {
            packet.append(qPerso.getQuest().getId()).append(";");
            packet.append(qPerso.isFinish() ? 1 : 0);
            if (nb < questList.size() - 1)
                packet.append("|");
            nb++;
        }
        return packet.toString();
    }

    public House getInHouse() {
        return _curHouse;
    }

    public void setInHouse(House h) {
        _curHouse = h;
    }

    public int getIsOnDialogAction() {
        return this.action;
    }

    public void setIsOnDialogAction(int action) {
        this.action = action;
    }

    private ExchangeAction<?> exchangeAction;

    public ExchangeAction<?> getExchangeAction() {
        return exchangeAction;
    }

    public synchronized void setExchangeAction(ExchangeAction<?> exchangeAction) {
        if(exchangeAction == null) this.setAway(false);

        // On force la fermeture si le joueur est déjà dans un échange quelconque, problème avec la méthode chelou actuel
        /*if(this.exchangeAction != null && exchangeAction != null){
            this.getGameClient().leaveExchange(this);
        }*/

        this.exchangeAction = exchangeAction;
    }

    public void refreshCraftSecure(boolean unequip) {
        for (Player player : this.getCurMap().getPlayers()) {
            if(player == null)
                continue;

            ArrayList<Job> jobs = player.getJobs();

            if (jobs != null) {
                GameObject object = player.getObjetByPos(Constant.ITEM_POS_ARME);

                if (object == null) {
                    if (unequip) {
                        for(Player target : this.getCurMap().getPlayers())
                            target.send("EW+" + player.getId() + "|");
                    }
                    continue;
                }

                String packet = "EW+" + player.getId() + "|", data = "";

                for (Job job : jobs) {
                    if (job.getSkills().isEmpty())
                        continue;
                    if (!job.isValidTool(object.getTemplate().getId()))
                        continue;

                    for (GameCase cell : this.getCurMap().getCases()) {
                        if (cell.getObject() != null) {
                            if (cell.getObject().getTemplate() != null) {
                                int io = cell.getObject().getTemplate().getId();
                                ArrayList<Integer> skills = job.getSkills().get(io);

                                if (skills != null)
                                    for (int skill : skills)
                                        if (!data.contains(String.valueOf(skill)))
                                            data += (data.isEmpty() ? skill : ";" + skill);
                            }
                        }
                    }

                    /*if (!data.isEmpty())
                        break;*/
                }

                for(Player target : this.getCurMap().getPlayers())
                    target.send(packet + data);
            }
        }
    }

    public void boostStat(int stat, boolean capital) {
        int value = 0;
        switch (stat) {
            case 10://Force
                value = this.getStats().getEffect(EffectConstant.STATS_ADD_FORC);
                break;
            case 13://Chance
                value = this.getStats().getEffect(EffectConstant.STATS_ADD_CHAN);
                break;
            case 14://Agilit�
                value = this.getStats().getEffect(EffectConstant.STATS_ADD_AGIL);
                break;
            case 15://Intelligence
                value = this.getStats().getEffect(EffectConstant.STATS_ADD_INTE);
                break;
        }
        int cout = Constant.getReqPtsToBoostStatsByClass(this.getClasse(), stat, value);
        if (!capital)
            cout = 0;
        if (cout <= _capital) {
            switch (stat) {
                case 11://Vita
                    if (this.getClasse() != Constant.CLASS_SACRIEUR)
                        this.getStats().addOneStat(EffectConstant.STATS_ADD_VITA, 1);
                    else
                        this.getStats().addOneStat(EffectConstant.STATS_ADD_VITA, capital ? 2 : 1);
                    break;
                case 12://Sage
                    this.getStats().addOneStat(EffectConstant.STATS_ADD_SAGE, 1);
                    break;
                case 10://Force
                    this.getStats().addOneStat(EffectConstant.STATS_ADD_FORC, 1);
                    break;
                case 13://Chance
                    this.getStats().addOneStat(EffectConstant.STATS_ADD_CHAN, 1);
                    break;
                case 14://Agilit�
                    this.getStats().addOneStat(EffectConstant.STATS_ADD_AGIL, 1);
                    break;
                case 15://Intelligence
                    this.getStats().addOneStat(EffectConstant.STATS_ADD_INTE, 1);
                    break;
                default:
                    return;
            }
            _capital -= cout;
            SocketManager.GAME_SEND_STATS_PACKET(this);
            Database.getStatics().getPlayerData().update(this);
        }
    }

    public void boostStats2(final int type, int pointsRequested)
    {
        // Client 1.43.7 envoie "AB<statID>|<nbStatsVoulus>" (cf. AS2 Account.boost).
        // pointsRequested = nombre de POINTS DE STAT à ajouter, pas budget de capital.
        // Le serveur dépense le coût correspondant et ajoute le bon nombre de stats,
        // en tenant compte des paliers (1:1 → 2:1 → 3:1 …).
        if (this.isMorph()) {
            this.sendMessage("Vous êtes incarné, vous ne pouvez donc pas vous ajoutez de point de caractéristique !");
            return;
        }
        if (_capital <= 0 || pointsRequested <= 0) {
            return;
        }
        int statID;
        switch (type) {
            case 10: statID = EffectConstant.STATS_ADD_FORC; break;
            case 11: statID = EffectConstant.STATS_ADD_VITA; break;
            case 12: statID = EffectConstant.STATS_ADD_SAGE; break;
            case 13: statID = EffectConstant.STATS_ADD_CHAN; break;
            case 14: statID = EffectConstant.STATS_ADD_AGIL; break;
            case 15: statID = EffectConstant.STATS_ADD_INTE; break;
            default: return;
        }

        boolean mod = false;
        int totalAdded = 0;
        while (totalAdded < pointsRequested) {
            int valorStat = this.stats.getEffect(statID);
            Classe.BoostStat boost = classeinit.getBoostStat(statID, valorStat);
            if (_capital < boost.cost) break; // plus assez de capital pour le palier suivant
            _capital -= boost.cost;
            this.getStats().addOneStat(statID, boost.puntos);
            totalAdded += boost.puntos;
            mod = true;
        }

        if (statID == EffectConstant.STATS_ADD_VITA) refreshLife(true);
        if (mod) {
            SocketManager.GAME_SEND_STATS_PACKET(this);
            Database.getStatics().getPlayerData().update(this);
        }
    }

    public void setCurJobAction(final JobAction JA) {
        this._curJobAction = JA;
    }

    public JobAction getCurJobAction() {
        return this._curJobAction;
    }

    // pour voir les cellules de combat du perso (sur sa map)
    public void showFightCells() {
        String places = this.getCurMap().getPlaces();
        if (places.indexOf('|') == -1 || places.length() < 2) {
            String mess = "Les places n'ont pas ete definies";
            this.sendMessage(mess);
            return;
        }
        SocketManager.send(this, "GZB"+ places);
        //SocketManager.send(this, "GZB"+ places);
    }

    public void cancelFightCells() {
        SocketManager.send(this, "GZB"+ "");
    }

    public int getProspection () {
        return (getTotalStats().getEffect(EffectConstant.STATS_ADD_PROS) + Math.round(getTotalStats().getEffect(EffectConstant.STATS_ADD_CHAN) / 10));
    }

    public void SwapClasse(int classetochange){

        if(isMorph())
        {
            return;
        }
        if (classetochange < 1) {
            classetochange = 1;
        } else if (classetochange > 12) {
            classetochange = 12;
        }
        if (classetochange == getClasse()) {
            SocketManager.GAME_SEND_BN_OUT(this, "Changement de Classe - Même Classe");
            return;
        }

        Database.getStatics().getPlayerData().update(this);

        parseSpells(parseSpellToDB());

        for( int id : _sorts.keySet() ){
            int AncLevel = getSortStatBySortIfHas(id).getLevel();
            //this.sendMessage("Le sort "+id+" est lvl "+ AncLevel);
            if (getSortStatBySortIfHas(id) != null){
                if (AncLevel <= 1){}
                else{
                    unlearnSpell(this, id, 1, AncLevel, true, true);
                }
            }
            //getGameClient().forgetSpell(id);
        }

        _sorts.clear();
        _sortsPlaces.clear();
        //On garde le tout
        SocketManager.GAME_SEND_STATS_PACKET(this);


        //this.sendMessage("Retrait des sorts terminé");
        //_saveSpellPts = _spellPts;
        //_saveSorts.putAll(_sorts);
        //_saveSortsPlaces.putAll(_sortsPlaces);

        //Pas besoin
        //this.setLevel(1);
        //this.addXp(oldXp);
        //this.sendMessage("Reset des caractéristiques");
        this.getStatsParcho().getMap().clear();
        this.getStats().addOneStat(125,-this.getStats().getEffect(125));
        this.getStats().addOneStat(124,-this.getStats().getEffect(124));
        this.getStats().addOneStat(118,-this.getStats().getEffect(118));
        this.getStats().addOneStat(123,-this.getStats().getEffect(123));
        this.getStats().addOneStat(119,-this.getStats().getEffect(119));
        this.getStats().addOneStat(126,-this.getStats().getEffect(126));
        this.addCapital((this.getLevel() - 1) * 5 - this.get_capital());
        //perso.addCapital((perso.getLevel() * 5) - 5);
        SocketManager.GAME_SEND_STATS_PACKET(this);
        SocketManager.GAME_SEND_Im_PACKET(this,"023;" + (this.getLevel() * 5 - 5));

        this.setClasse(classetochange);

        //this.sendMessage("Nouveau Morph");
        // On morph avec la Nouvelle Classe
        int UnMorphID = this.getClasse() * 10 + this.getSexe();
        this.setGfxId(UnMorphID);
        SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(this.getCurMap(), this.getId());
        SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(this.getCurMap(), this);


        if (this.fight == null) SocketManager.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(), this);

        //this.sendMessage("Ajouts des sort de la nouvelle classe");
        //on ajoute les nouveau sort
        _sorts = Constant.getStartSorts(classetochange);
        for (int a = 1; a <= this.getLevel(); a++)
            Constant.onLevelUpSpells(this, a);
        _sortsPlaces = Constant.getStartSortsPlaces(classetochange);


        //this._sorts.putAll(_saveSorts);
        //_sortsPlaces.putAll(_saveSortsPlaces);


        if (this.fight == null) {
            SocketManager.GAME_SEND_ASK(this.getGameClient(), this);
            SocketManager.GAME_SEND_SPELL_LIST(this);
        }

        //if (this.fight == null) SocketManager.GAME_SEND_STATS_PACKET(this);
        //parseSpells(parseSpellToDB());
        Database.getStatics().getPlayerData().update(this);
    }

    public boolean changeClasse(byte clase) {
        if(isMorph())
        {
            this.sendMessage("Vous ne pouvez pas changer de classe lorsque que vous êtes transformé");
            return false;
        }
        if (clase < 1) {
            clase = 1;
        } else if (clase > 12) {
            clase = 12;
        }
        if (clase == getClasse()) {
            this.sendMessage("Vous ne pouvez pas changer de classe pour la même classe");
            //ocketManager.GAME_SEND_BN_OUT(this, "Changement de Classe - Même Classe");
            return false;
        }

        for (SpellGrade sort : this._sorts.values() ) {
            int point = 0;
            switch (sort.getLevel()){
                case 1:
                    break;
                case 2:
                    point =1;
                    break;
                case 3:
                    point =3;
                    break;
                case 4:
                    point =6;
                    break;
                case 5:
                    point =10;
                    break;
                case 6:
                    point =15;
                    break;
                default:
                    point = 0;
                    break;
            }
            this.set_spellPts(this.get_spellPts() + point);
        }

        this.setClasse(clase);
        //Clase = Mundo.getClase(ClaseID);
        SocketManager.GAME_SEND_AC_CHANGE_CLASSE(this, getClasse());
        this._sorts = Constant.getStartSorts(classe);
        for (int a = 1; a <= this.getLevel(); a++) {
            Constant.onLevelUpSpells(this, a);
        }
        this._sortsPlaces = Constant.getStartSortsPlaces(classe);
        /*int spellpoints = 0;
        for(int i = 2; i < 201; i++)
        {
            spellpoints += 1;
        }
        this._spellPts = spellpoints;*/
        if(isOnline)
        {
            SocketManager.GAME_SEND_SL_LISTE_SORTS(this);
        }
        demorph();
        restat();
        this.stats = new Stats(this.stats.getMap(), true, this);
        if(this.getisParcho()==1){
            this.getStatsParcho().getMap().clear();
            this.getStatsParcho().getEffects().clear();
            this.getStats().addOneStat(125, 101);
            this.getStats().addOneStat(124, 101);
            this.getStats().addOneStat(118, 101);
            this.getStats().addOneStat(126, 101);
            this.getStats().addOneStat(119, 101);
            this.getStats().addOneStat(123, 101);
            this.getStatsParcho().addOneStat(EffectConstant.STATS_ADD_VITA, 101);
            this.getStatsParcho().addOneStat(EffectConstant.STATS_ADD_SAGE, 101);
            this.getStatsParcho().addOneStat(EffectConstant.STATS_ADD_FORC, 101);
            this.getStatsParcho().addOneStat(EffectConstant.STATS_ADD_INTE, 101);
            this.getStatsParcho().addOneStat(EffectConstant.STATS_ADD_CHAN, 101);
            this.getStatsParcho().addOneStat(EffectConstant.STATS_ADD_AGIL, 101);
        }
        SocketManager.GAME_SEND_STATS_PACKET(this);
        refreshToMap();
        Database.getStatics().getPlayerData().updateInfos(this);
        Database.getStatics().getPlayerData().update(this);

        this.sendMessage("Sauvegarde du Personnage terminé");
        this.sendMessage("Bravo ! vous avez changé de classe");
        this.getGameClient().kick();
       // SocketManager.GAME_SEND_Im_PACKET(this, "1CHANGED_CLASSE_SUCCESS");
        return true;

    }

    private void refreshToMap() {
        if (fight == null) {
            SocketManager.GAME_SEND_GM_REFRESH_PL_TO_MAP(getCurMap(), this);
        } else if (fight.getState() == Constant.FIGHT_STATE_PLACE) {
            final Fighter luchador = fight.getFighterByPerso(this);
            if (luchador != null) {
                SocketManager.GAME_SEND_GM_REFRESH_FIGHTER_IN_FIGHT(fight, luchador);
            }
        }
    }

    private void restat() {
        this.getStatsParcho().getMap().clear();
        this.getStats().addOneStat(125,-this.getStats().getEffect(125));
        this.getStats().addOneStat(124,-this.getStats().getEffect(124));
        this.getStats().addOneStat(118,-this.getStats().getEffect(118));
        this.getStats().addOneStat(123,-this.getStats().getEffect(123));
        this.getStats().addOneStat(119,-this.getStats().getEffect(119));
        this.getStats().addOneStat(126,-this.getStats().getEffect(126));
        this.addCapital((this.getLevel() - 1) * 5 - this.get_capital());
        SocketManager.GAME_SEND_STATS_PACKET(this);
        SocketManager.GAME_SEND_Im_PACKET(this,"023;" + (this.getLevel() * 5 - 5));
    }

    public String stringListeSorts() {
        final StringBuilder str = new StringBuilder();
        for (SpellGrade hp : _sorts.values()) {
            if (hp.getSpell() == null) {
                continue;
            }
            if (str.length() > 0) {
                str.append(";");
            }

            str.append(hp.getSpellID()).append("~").append(hp.getLevel()).append("~").append(_hashedPositionToHex(_sortsPlaces.get(hp.getSpellID())));
        }
        return str.toString();
    }

    public void changeColor(String packet) {
        int playerOgrine = getAccount().getWebAccount().getPoints() - Config.INSTANCE.getPRIX_CHANGEMENT_COULEUR();


        getAccount().getWebAccount().setPoints(playerOgrine);
        if(!packet.isEmpty())
        {
            String[] colores = packet.substring(3).split(Pattern.quote("|"));
            setColors(Integer.parseInt(colores[0]), Integer.parseInt(colores[1]), Integer.parseInt(colores[2]));
            refreshToMap();
            SocketManager.GAME_SEND_bV_CLOSE_PANEL(this);
        }
    }

    public void setColors(int color1, int color2, int color3) {
        if (color1 < -1) {
            color1 = -1;
        } else if (color1 > 16777215) {
            color1 = 16777215;
        }
        if (color2 < -1) {
            color2 = -1;
        } else if (color2 > 16777215) {
            color2 = 16777215;
        }
        if (color3 < -1) {
            color3 = -1;
        } else if (color3 > 16777215) {
            color3 = 16777215;
        }
        this.color1 = color1;
        this.color2 = color2;
        this.color3 = color3;
        Database.getStatics().getPlayerData().UPDATE_PLAYER_COLORS(this);
    }

    public void changePlayerName(String packet) {
        if (!packet.isEmpty()) {
            int playerOgrine = getAccount().getWebAccount().getPoints() - Config.INSTANCE.getPRIX_CHANGEMENT_PSEUDO();
            getAccount().getWebAccount().setPoints(playerOgrine);
            String[] params = packet.substring(3).split(";");
            String nombre = params[0];
            /*int colorN = 0;
            try {
                colorN = Integer.parseInt(params[1]);
                if (colorN > 16777215) {
                    colorN = 0;
                }
            } catch (Exception e) {
                return;
            }
            if (nombre.equals(getName())) { // si tiene el mismo nombre y diferente color
                if (colorN == colorNombre) {
                    return
                }
            }*/
            nombre = nombreValido(nombre, false);
            if (nombre == null) {
                SocketManager.ENVIAR_AAE_ERROR_CREAR_PJ(this, "a");
                return;
            }
            if (nombre.isEmpty()) {
                SocketManager.ENVIAR_AAE_ERROR_CREAR_PJ(this, "n");
                return;
            }
            //_perso.colorNombre = colorN
            setName(nombre);
            SocketManager.ENVIAR_bn_CAMBIAR_NOMBRE_CONFIRMADO(this, nombre);
            refreshToMap();
            SocketManager.GAME_SEND_Im_PACKET(this, "1NAME_CHANGED;" + nombre);
        } else {
            //send(this, "bN" + this.colorNombre);
        }
    }

    public static String nombreValido(String nombre, boolean comando) {
        if (World.world.getPlayerPerName(nombre) != null) {
            return null;
        }
        if (nombre.length() < 1 || nombre.length() > 20) {
            return "";
        }
        if (!comando) {
            StringBuilder nombreFinal = new StringBuilder();
            final String nLower = nombre.toLowerCase();
            final String abcMin = "abcdefghijklmnopqrstuvwxyz-";
            int cantSimbol = 0;
            char letra_A = ' ', letra_B = ' ';
            boolean primera = true;
            for (final char letra : nLower.toCharArray()) {
                if (primera && letra == '-' || !abcMin.contains(letra + "") || letra == letra_A && letra == letra_B) {
                    return "";
                }
                if (primera) {
                    nombreFinal.append((letra + "").toUpperCase());
                } else {
                    nombreFinal.append(letra);
                }
                primera = false;
                if (abcMin.contains(letra + "") && letra != '-') {
                    letra_A = letra_B;
                    letra_B = letra;
                } else if (letra == '-') {
                    primera = true;
                    if (cantSimbol >= 1) {
                        return "";
                    }
                    cantSimbol++;
                }
            }
                nombre = nombreFinal.toString();
        }
        return nombre;
    }

    public Guild get_guild() {
        if (_guildMember == null)
            return null;
        return _guildMember.getGuild();
    }

    public void setTonique(int id,int pos, String StatsToAdd) {
        if (getObjetByPos(pos) != null) {
            long guid = getObjetByPos(pos).getGuid();
            SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, guid);
            this.deleteItem(guid);
        }

        String StatsString =  World.world.getObjTemplate(id).getStrTemplate() + "," + StatsToAdd;
        GameObject obj = World.world.getObjTemplate(id).createNewTonique(pos,StatsString);
        if (obj != null)
            if (this.addObjet(obj, false))
                World.world.addGameObject(obj,true);

        SocketManager.GAME_SEND_Im_PACKET(this, "021;" + 1 + "~" + id);

        this.getGameClient().onMovementItemClass(obj, pos);
        //this.equipItem(obj);

        SocketManager.GAME_SEND_Ow_PACKET(this);
        SocketManager.GAME_SEND_STATS_PACKET(this);
        Database.getStatics().getPlayerData().update(this);

        //SocketManager.GAME_SEND_ASK(this.getGameClient(), this);
    }

    public void setToniqueEquilibrage(Stats stats) {
        GameObject obj = World.world.getObjTemplate(16268).createNewToniqueEquilibrage(stats);
        if (obj != null)
            if (this.addObjet(obj, false))
                World.world.addGameObject(obj,true);

        //this.getGameClient().onMovementItemClass(obj, Constant.ITEM_POS_TONIQUE_EQUILIBRAGE);
        SocketManager.GAME_SEND_Ow_PACKET(this);
        SocketManager.GAME_SEND_STATS_PACKET(this);
        Database.getStatics().getPlayerData().update(this);
    }

    public void removeAllsToniques(){
        for(int i=Constant.ITEM_POS_TONIQUE_EQUILIBRAGE;i<= Constant.ITEM_POS_TONIQUE9;i++){
            this.removeTonique(i);
        }
    }

    public void removeTonique(int pos){
        GameObject obj = getObjetByPos(pos);
        if (obj != null) {
            //this.unEquipItem(pos);

            this.getGameClient().onMovementItemClass(obj, -1);
            SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, obj.getGuid());
            this.deleteItem(obj.getGuid());
            SocketManager.GAME_SEND_Im_PACKET(this, "022;" + 1 + "~" + obj.getTemplate().getId());
            SocketManager.GAME_SEND_STATS_PACKET(this);
            Database.getStatics().getPlayerData().update(this);
            //SocketManager.GAME_SEND_ASK(this.getGameClient(), this);
        }
    }

    public Stats generateStatsTonique(Map<String, String> fullMorph) {
        Stats statTonique = new Stats();

        // Vie
        statTonique.equilibreStat(EffectConstant.STATS_ADD_VITA,EffectConstant.STATS_REM_VITA,this,fullMorph,"vie");
        // PA
        if (getTotalStats().getEffect(EffectConstant.STATS_ADD_PA) == Integer.parseInt(fullMorph.get("pa"))) {}
        else if (getTotalStats().getEffect(EffectConstant.STATS_ADD_PA) > Integer.parseInt(fullMorph.get("pa"))) {
            statTonique.addOneStat(EffectConstant.STATS_REM_PA3, getTotalStats().getEffect(EffectConstant.STATS_ADD_PA) - Integer.parseInt(fullMorph.get("pa")));
        } else {
            statTonique.addOneStat(EffectConstant.STATS_ADD_PA2, Integer.parseInt(fullMorph.get("pa")) - getTotalStats().getEffect(EffectConstant.STATS_ADD_PA));
        }
        // PM
        if (getTotalStats().getEffect(EffectConstant.STATS_ADD_PM) == Integer.parseInt(fullMorph.get("pm"))) {}
        else if(getTotalStats().getEffect(EffectConstant.STATS_ADD_PM) > Integer.parseInt(fullMorph.get("pm"))) {
            statTonique.addOneStat(EffectConstant.STATS_REM_PM2, getTotalStats().getEffect(EffectConstant.STATS_ADD_PM) - Integer.parseInt(fullMorph.get("pm")));
        } else {
            statTonique.addOneStat(EffectConstant.STATS_ADD_PM2, Integer.parseInt(fullMorph.get("pm")) - getTotalStats().getEffect(EffectConstant.STATS_ADD_PM));
        }
        // Sagesse
        statTonique.equilibreStat(EffectConstant.STATS_ADD_SAGE,EffectConstant.STATS_REM_SAGE,this,fullMorph,"sagesse");
        // Force
        statTonique.equilibreStat(EffectConstant.STATS_ADD_FORC,EffectConstant.STATS_REM_FORC,this,fullMorph,"terre");
        // Intel
        statTonique.equilibreStat(EffectConstant.STATS_ADD_INTE,EffectConstant.STATS_REM_INTE,this,fullMorph,"feu");
        // Chance
        statTonique.equilibreStat(EffectConstant.STATS_ADD_CHAN,EffectConstant.STATS_REM_CHAN,this,fullMorph,"eau");
        // Agi
        statTonique.equilibreStat(EffectConstant.STATS_ADD_AGIL,EffectConstant.STATS_REM_AGIL,this,fullMorph,"air");
        // Ini
        statTonique.equilibreStat(EffectConstant.STATS_ADD_INIT,EffectConstant.STATS_REM_INIT,this,fullMorph,"initiative");
        // DO
        statTonique.equilibreStat(EffectConstant.STATS_ADD_DOMA,EffectConstant.STATS_REM_DOMA,this,fullMorph,"do");
        // % DO
        statTonique.equilibreStat(EffectConstant.STATS_ADD_PERDOM,EffectConstant.STATS_REM_PERDOM,this,fullMorph,"doper");
        // CreaInvo
        statTonique.equilibreStat(EffectConstant.STATS_CREATURE,EffectConstant.STATS_REM_INVO,this,fullMorph,"invo");
        // resiPerNEU
        statTonique.equilibreStat(EffectConstant.STATS_ADD_RP_NEU,EffectConstant.STATS_REM_RP_NEU,this,fullMorph,"resiNeu");
        // resiPerTER
        statTonique.equilibreStat(EffectConstant.STATS_ADD_RP_TER,EffectConstant.STATS_REM_RP_TER,this,fullMorph,"resiTer");
        // resiPerFEU
        statTonique.equilibreStat(EffectConstant.STATS_ADD_RP_FEU,EffectConstant.STATS_REM_RP_FEU,this,fullMorph,"resiFeu");
        // resiPerEAU
        statTonique.equilibreStat(EffectConstant.STATS_ADD_RP_EAU,EffectConstant.STATS_REM_RP_EAU,this,fullMorph,"resiEau");
        // resiPerAIR
        statTonique.equilibreStat(EffectConstant.STATS_ADD_RP_AIR,EffectConstant.STATS_REM_RP_AIR,this,fullMorph,"resiAir");
        // resiFixNEU
        statTonique.equilibreStat(EffectConstant.STATS_ADD_R_NEU,EffectConstant.STATS_REM_R_NEU,this,fullMorph,"rfixNeu");
        // resiFixTER
        statTonique.equilibreStat(EffectConstant.STATS_ADD_R_TER,EffectConstant.STATS_REM_R_TER,this,fullMorph,"rfixTer");
        // resiFixFEU
        statTonique.equilibreStat(EffectConstant.STATS_ADD_R_FEU,EffectConstant.STATS_REM_R_FEU,this,fullMorph,"rfixFeu");
        // resiFixEAU
        statTonique.equilibreStat(EffectConstant.STATS_ADD_R_EAU,EffectConstant.STATS_REM_R_EAU,this,fullMorph,"rfixEau");
        // resiFixAIR
        statTonique.equilibreStat(EffectConstant.STATS_ADD_R_AIR,EffectConstant.STATS_REM_R_AIR,this,fullMorph,"rfixAir");
        // Soin
        statTonique.equilibreStat(EffectConstant.STATS_ADD_SOIN,EffectConstant.STATS_REM_SOIN,this,fullMorph,"soin");
        // CC
        statTonique.equilibreStat(EffectConstant.STATS_ADD_CC,EffectConstant.STATS_REM_CC,this,fullMorph,"crit");
        // EC
        //statTonique.equilibreStat(EffectConstant.STATS_ADD_CC,EffectConstant.STATS_REM_CC,this,fullMorph,"crit");
        // Créa invo
        //statTonique.equilibreStat(EffectConstant.STATS_ADD_CC,EffectConstant.STATS_REM_CC,this,fullMorph,"crit");
        // PO
        statTonique.equilibreStat(EffectConstant.STATS_ADD_PO,EffectConstant.STATS_REM_PO,this,fullMorph,"PO");
        // Renvoi Do
        statTonique.equilibreStat(EffectConstant.STATS_RETDOM,EffectConstant.STATS_REM_RENVOI,this,fullMorph,"renvoie");
        // Do Pieg
        statTonique.equilibreStat(EffectConstant.STATS_TRAPDOM,EffectConstant.STATS_REM_TRAPDOM,this,fullMorph,"dotrap");
        // %Do Pieg
        statTonique.equilibreStat(EffectConstant.STATS_TRAPPER,EffectConstant.STATS_REM_TRAPPER,this,fullMorph,"perdotrap");
        // %Do Pieg
        statTonique.equilibreStat(EffectConstant.STATS_ADD_PDOM,EffectConstant.STATS_REM_PDOM,this,fullMorph,"dophysique");
        // %Do Pieg
        //statTonique.equilibreStat(EffectConstant.STATS_ADD_MFLEE,EffectConstant.STATS_REM_MFLEE,this,fullMorph,"esPM");
        // %Do Pieg
        //statTonique.equilibreStat(EffectConstant.STATS_ADD_AFLEE,EffectConstant.STATS_REM_AFLEE,this,fullMorph,"esPA");

        this.initiative = Integer.parseInt(fullMorph.get("initiative"));
        return statTonique;
    }


    public ArrayList<Integer> getAllToniqueID() {
        ArrayList<Integer> tableTonic = new ArrayList<>();
        for(int i=Constant.ITEM_POS_TONIQUE1;i<= Constant.ITEM_POS_TONIQUE9;i++) {
            GameObject Obj = this.getObjetByPos(i);
            if(Obj != null)
                tableTonic.add(Obj.getTemplate().getId());
        }
        return tableTonic;
    }

    public String getWrPacket(int palier) {

        String packet = "";
        try {
            StringBuilder WrData = new StringBuilder();
            WrData.append("wr");
            int[] tonics0 = Formulas.getRandomsInt(Constant.TONIQUE1, 7);
            int[] tonics1 = Formulas.getRandomsInt(Constant.TONIQUE2, 7);
            int classeid = Constant.getClasseByMorphWeapon(this.getObjetByPos(Constant.ITEM_POS_ARME).getTemplate().getId());

            ArrayList<Integer> tonics2spell = Constant.getToniques3byclasse(classeid);
            ArrayList<Integer> tonics2final = new ArrayList<>();
            ArrayList<Integer> tonics2toIgnore = this.getAllToniqueID();
            for (int tonicid : tonics2spell) {
                if (!tonics2toIgnore.contains(tonicid)) {
                    tonics2final.add(tonicid);
                }
            }
            ArrayList<Integer> tonics2nospell = new ArrayList<>();
            tonics2nospell.add(16024);
            tonics2nospell.add(16025);
            tonics2nospell.add(16026);
            tonics2final.addAll(tonics2nospell);
            int[] nombresAleatoires = new int[tonics2final.size()];
            int k = 0;
            for(int id : tonics2final){
                nombresAleatoires[k] = id;
                k++;
            }

            int[] tonics2 = Formulas.getRandomsInt( nombresAleatoires, 7);
            int tonic0 = 0, tonic1 = 0, tonic2 = 0;
            for (int i = 0; i < tonics0.length; i++) {
                if (i == 0 || i == tonics0.length - 1) {
                    WrData.append(tonics0[i] + ";");
                    if (tonic0 == 0) {
                        tonic0 = tonics0[i];
                    }
                } else {
                    WrData.append(tonics0[i] + ",");
                }
            }
            WrData.append(Constant.getStatStringbyPalier(palier+1) + "|");
            for (int i = 0; i < tonics1.length; i++) {
                if (i == 0 || i == tonics1.length - 1) {
                    WrData.append(tonics1[i] + ";");
                    if (tonic1 == 0) {
                        tonic1 = tonics1[i];
                    }
                } else {
                    WrData.append(tonics1[i] + ",");
                }
            }
            WrData.append(Constant.getStatStringbyPalier(palier+1) + "|");
            for (int i = 0; i < tonics2.length; i++) {
                if (i == 0 || i == tonics2.length - 1) {
                    WrData.append(tonics2[i] + ";");
                    if (tonic2 == 0) {
                        tonic2 = tonics2[i];
                    }
                } else {
                    WrData.append(tonics2[i] + ",");
                }
            }
            WrData.append(Constant.getStatStringbyPalier(palier+1) + "|");
            WrData.append(palier + "|");
            WrData.append("10;20;40;60;90;120;160;200;250;300");
            packet = WrData.toString();
            this.LastTonicProposed[0] =tonic0;
            this.LastTonicProposed[1] =tonic1;
            this.LastTonicProposed[2] =tonic2;
            this.lastTonicPacket = packet;
        }
        catch (Exception e){
            System.out.println(e);
        }
        return packet;
    }

    public void removeItemClasseSpell(int spell) {
        if (objectsClassSpell.containsKey(spell)) {
            objectsClassSpell.remove(spell);
        }
    }

    public void refreshItemClasseSpell(int spell, int effect, int modif) {
        if (!objectsClassSpell.containsKey(spell)) {
            //objectsClassSpell.put(spell, new World.Couple<Integer, Integer>(effect, modif));
            HashMap<Integer, Integer> newMap = new HashMap<>();
            newMap.put(effect, modif);
            objectsClassSpell.put(spell, newMap);
        }
        else
        {
            HashMap<Integer, Integer> map = objectsClassSpell.get(spell);
            if(map.containsKey(effect))
            {
                //int newValue = map.get(effect) + modif;
                map.remove(effect);
                //map.put(effect, newValue);
                map.put(effect, modif);
            }
            else
            {
                map.put(effect, modif);
            }

            objectsClassSpell.remove(spell);
            objectsClassSpell.put(spell, map);
        }
    }

    public void addItemClasseSpell(int spell, int effect, int modif) {
        if (!objectsClassSpell.containsKey(spell)) {
            //objectsClassSpell.put(spell, new World.Couple<Integer, Integer>(effect, modif));
            HashMap<Integer, Integer> newMap = new HashMap<>();
            newMap.put(effect, modif);
            objectsClassSpell.put(spell, newMap);
        }
        else
        {
            HashMap<Integer, Integer> map = objectsClassSpell.get(spell);
            if(map.containsKey(effect))
            {
                int newValue = map.get(effect) + modif;
                map.remove(effect);
                map.put(effect, newValue);
            }
            else
            {
                map.put(effect, modif);
            }

            objectsClassSpell.remove(spell);
            objectsClassSpell.put(spell, map);
        }
    }


    public void removeSpellEffectofObject(GameObject exObj) {
        String[] stats = exObj.getTemplate().getStrTemplate().split(",");
        for (String stat : stats) {
            String[] val = stat.split("#");
            try {
                int idStat = Integer.parseInt(val[0], 16);
                int idSpell = Integer.parseInt(val[1], 16);
                if (EffectConstant.IS_SPELL_BOOST_EFFECT(idStat)){
                    String modifi = idStat + ";" + idSpell+ ";0";
                    SocketManager.SEND_SB_SPELL_BOOST(this, modifi);
                    this.removeObjectClassSpell(idSpell);
                }

            }
            catch (Exception e){
                continue;
            }
        }
    }

    public GameObject getCAC() {
        GameObject CAC = null;
        for(GameObject Obj : this.getEquippedObjects()){
            if(Obj.getPosition() == 1)
                return Obj;
        }
        return CAC;
    }
}