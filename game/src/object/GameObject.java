package object;

import area.map.entity.Trunk;
import client.Account;
import client.Player;
import client.other.Stats;
import common.Formulas;
import common.SocketManager;
import database.Database;
import entity.mount.Mount;
import entity.pet.Pet;
import entity.pet.PetEntry;
import fight.spells.Effect;
import fight.spells.EffectConstant;
import game.world.World;
import game.world.World.Couple;
import hdv.HdvEntry;
import job.JobAction;
import kernel.Constant;
import kernel.Logging;
import object.entity.Fragment;
import object.entity.SoulStone;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameObject {

    protected ObjectTemplate template;
    protected int quantity = 1;
    protected int position = Constant.ITEM_POS_NO_EQUIPED;
    protected long guid;
    protected int obvijevanPos;
    protected int obvijevanLook;
    protected int obvijevanId;
    protected int puit;
    protected int rarity;
    private Stats Stats = new Stats();
    private ArrayList<Effect> Effects = new ArrayList<>();
    private ArrayList<String> SortStats = new ArrayList<>();
    private Map<Integer, String> txtStats = new HashMap<>();
    private Map<Integer, Integer> SoulStats = new HashMap<>();
    private int mimibiote =-1;

    public byte modification = -1;

    public GameObject(long Guid, int template, int qua, int pos, String strStats, int puit,int rarity, int mimibiote) {
        this.guid = Guid;
        this.template = World.world.getObjTemplate(template);
        this.quantity = qua;
        this.position = pos;
        this.puit = puit;
        this.rarity = rarity;
        this.mimibiote = mimibiote;
        this.Stats = new Stats();
        this.parseStringToStats(strStats, true);
        this.getStatsClass();
    }

    public GameObject(long Guid) {
        this.guid = Guid;
        this.template = World.world.getObjTemplate(8378);
        this.quantity = 1;
        this.position = -1;
        this.puit = 0;
        this.rarity = 0;
    }

    public GameObject(long Guid, int template, int qua, int pos, Stats stats, ArrayList<Effect> effects, Map<Integer, Integer> _SoulStat, Map<Integer, String> _txtStats, int puit, int rarity, int mimibiote) {
        this.guid = Guid;
        this.template = World.world.getObjTemplate(template);
        this.quantity = qua;
        this.position = pos;
        this.Stats = stats;
        this.Effects = effects;
        this.SoulStats = _SoulStat;
        this.txtStats = _txtStats;
        this.obvijevanPos = 0;
        this.obvijevanLook = 0;
        this.obvijevanId = 0;
        this.puit = puit;
        this.rarity = rarity;
        this.mimibiote = mimibiote;
        this.getStatsClass();
    }

    /*private static ArrayList<SpellEffect> getEffectTemplate(String statsTemplate) {
        ArrayList<SpellEffect> Effets = new ArrayList<SpellEffect>();
        if (statsTemplate.equals(""))
            return Effets;

        String[] splitted = statsTemplate.split(",");
        for (String s : splitted) {
            String[] stats = s.split("#");
            int statID = Integer.parseInt(stats[0], 16);
            for (int a : Constant.ARMES_EFFECT_IDS) {
                if (a == statID) {
                    int id = statID;
                    String min = stats[1];
                    String max = stats[2];
                    String jet = stats[4];
                    String args = min + ";" + max + ";-1;-1;0;" + jet;
                    Effets.add(new SpellEffect(id, args, 0, -1));
                }
            }
            switch (statID) {
                case 110:
                case 139:
                case 605:
                case 614:
                    String min = stats[1];
                    String max = stats[2];
                    String jet = stats[4];
                    String args = min + ";" + max + ";-1;-1;0;" + jet;
                    Effets.add(new SpellEffect(statID, args, 0, -1));
                    break;
            }
        }
        return Effets;
    }*/

    public static GameObject getCloneObjet(GameObject obj, int qua) {

        Map<Integer, Integer> maps = new HashMap<>();
        ArrayList<Effect> statsCAC = new ArrayList<>();
        Map<Integer, String> statsTXT = new HashMap<>();
        maps.putAll(obj.getStats().getMap());
        statsTXT.putAll(obj.getTxtStat());
        statsCAC.addAll(obj.CopyEffectStat());
        Stats newStats = new Stats(maps);

        GameObject ob = null;

        switch (obj.getTemplate().getType()){
            case Constant.ITEM_TYPE_PIERRE_AME_PLEINE_ARCHI:
            case Constant.ITEM_TYPE_PIERRE_AME_PLEINE:
            case Constant.ITEM_TYPE_PIERRE_AME_PLEINE_BOSS:
                ob = new SoulStone(Database.getStatics().getObjectData().getNextId(), qua , obj.getTemplate().getId(), Constant.ITEM_POS_NO_EQUIPED, obj.parseStatsString() );
                break;
            default:
                ob = new GameObject(Database.getStatics().getObjectData().getNextId(), obj.getTemplate().getId(), qua, Constant.ITEM_POS_NO_EQUIPED, newStats, statsCAC, obj.getSoulStat(), statsTXT, obj.getPuit(), obj.getRarity(), obj.getMimibiote());
                break;
        }
        ob.modification = 0;
        return ob;
    }

    public int getMimibiote()
    {
        return mimibiote;
    }

    public void setMimibiote(int mimibiote)
    {
        this.mimibiote = mimibiote;
        this.setModification();
    }

    public long setId() {
        this.guid = Database.getStatics().getObjectData().getNextId();
        return this.getGuid();
    }

    public int getPuit() {
        return this.puit;
    }

    public void setPuit(int puit) {
        this.puit = puit;
    }

    public int getObvijevanPos() {
        return obvijevanPos;
    }

    public void setObvijevanPos(int pos) {
        obvijevanPos = pos;
        this.setModification();
    }

    public int getObvijevanLook() {
        return obvijevanLook;
    }

    public void setObvijevanLook(int look) {
        obvijevanLook = look;
        this.setModification();
    }

    public void setObvijevanId(int id) {
        this.obvijevanId = id;
    }

    public int getObvijevanId() {
        return this.obvijevanId;
    }

    public void setModification() {
        if(this.modification == -1) {
            this.modification = 1;
        }
    }

    public String stringObjetoConGuino() {
        StringBuilder str = new StringBuilder();
        try {
            str.append(Long.toHexString(guid)).append("~")
                    .append(Integer.toHexString(getTemplate().getId())).append("~").append(
                    Integer.toHexString(quantity))
                    .append("~");
                            if (position == Constant.ITEM_POS_NO_EQUIPED)
                                str.append("");
                            else
                            {
                                str.append(Integer.toHexString(position));
                            }
                    str.append("~").append(parseStatsString()).append("~").append(getTemplate().getPrice() / 10);
            str.append(";");
        } catch (Exception e) {
            //System.out.println("OBJETO BUG stringObjetoConGuiño " + guid + " Exception: " + e.toString());
        }
        return str.toString();
    }

    public void parseStringToStats(String strStats, boolean save) {
        if(this.template != null & (this.template.getId() == 7010 || this.template.getId() == 10417 || this.template.getId() == 10418) ) return;
        if(ArrayUtils.contains(Constant.SPIRITGEMID,this.template.getId())) return;

        final StringBuilder statsToOrder = new StringBuilder(); // for fm
        boolean isFirst = true; // for fm

        String dj1 = "";
        if (!strStats.equalsIgnoreCase("")) {
            for (String split : strStats.split(",")) {

                try {
                    if (split.equalsIgnoreCase(""))
                        continue;
                    if (split.substring(0, 3).equalsIgnoreCase("325") && (this.getTemplate().getId() == 10207 || this.getTemplate().getId() == 10601)) {
                        txtStats.put(Constant.STATS_DATE, split.substring(3) + "");
                        continue;
                    }

                    if (split.substring(0, 3).equalsIgnoreCase("3dc")) {// Si c'est une rune de signature crï¿½e
                        txtStats.put(Constant.STATS_SIGNATURE, split.split("#")[4]);
                        continue;
                    }
                    if (split.substring(0, 3).equalsIgnoreCase("3d9")) {// Si c'est une rune de signature modifiï¿½
                        txtStats.put(Constant.STATS_CHANGE_BY, split.split("#")[4]);
                        continue;
                    }

                    String[] stats = split.split("#");
                    int id = Integer.parseInt(stats[0], 16);

                    if (id == Constant.STATS_PETS_DATE
                            && this.getTemplate().getType() == Constant.ITEM_TYPE_CERTIFICAT_CHANIL) {
                        txtStats.put(id, split.substring(3));
                        continue;
                    }
                    if(id == Constant.STATS_PETS_PDV || id == Constant.STATS_PETS_POIDS)
                    {
                        if(stats[3].contains("1d"))
                        {
                            txtStats.put(id, stats[3].substring(0,2));
                        }
                        else {
                            txtStats.put(id, stats[3]);
                        }
                        continue;
                    }
                    if (id == Constant.STATS_CHANGE_BY || id == Constant.STATS_NAME_TRAQUE || id == Constant.STATS_OWNER_1) {
                        txtStats.put(id, stats[4]);
                        continue;
                    }
                    if (id == Constant.STATS_GRADE_TRAQUE || id == Constant.STATS_ALIGNEMENT_TRAQUE || id == Constant.STATS_NIVEAU_TRAQUE) {
                        txtStats.put(id, stats[3]);
                        continue;
                    }
                    if (id == Constant.STATS_PETS_SOUL) {
                        SoulStats.put(Integer.parseInt(stats[1], 16), Integer.parseInt(stats[3], 16)); // put(id_monstre, nombre_tuï¿½)
                        continue;
                    }
                    if (id == Constant.STATS_NAME_DJ) {
                        dj1 += (!dj1.isEmpty() ? "," : "") + stats[3];
                        txtStats.put(Constant.STATS_NAME_DJ, dj1);
                        continue;
                    }
                    if(id == Constant.COMPATIBLE_AVEC) {
                        txtStats.put(Constant.COMPATIBLE_AVEC, stats[4]);
                        continue;
                    }

                    if (id == 997 || id == 996) {
                        txtStats.put(id, stats[4]);
                        continue;
                    }
                    if (this.template != null && this.template.getId() == 77 && id == Constant.STATS_PETS_DATE) {
                        txtStats.put(id, split.substring(3));
                        continue;
                    }
                    if (id == Constant.STATS_DATE) {
                        txtStats.put(id, stats[3]);
                        continue;
                    }
                    if(id == Constant.APPARAT_ITEM) {
                        txtStats.put(id, stats[3]);
                        continue;
                    }
                    if(id == Constant.APPARAT_NAME) {
                        txtStats.put(id, stats[3]);
                        continue;
                    }

                    // Ici c'est les stats des consommables
                    boolean follow1 = true;
                    switch (id) {
                        case EffectConstant.STATS_ADD_VIE: // Ahhh ca c'est pour ce heal (genre le pain etc)
                        case EffectConstant.STATS_ADD_ENERGIE:  // Ahhh ca c'est pour l'energie (genre le pain etc)
                        case EffectConstant.STATS_ADD_XP: // Parchemin XP
                        case EffectConstant.STATS_ADD_XPJOB: // Parchemin XP JOB
                            String min = stats[1];
                            String max = stats[2];
                            String args3 = stats[3];
                            String jet = "";
                            if(stats.length >= 5)
                                jet = stats[4];
                            String args = min + ";" + max + ";"+args3+";" + jet;
                            Effects.add(new Effect(id, args, 0, -1,true));
                            follow1 = false;
                            break;
                    }
                    if (!follow1) {
                        continue;
                    }
                    //Si stats avec Texte (Signature, apartenance, etc)//FIXME Ca c'est de la merde
                    if (id != Constant.STATS_RESIST  && (id < 970 || id > 974) && (!stats[3].equals("") && (!stats[3].equals("0") || id == Constant.STATS_PETS_DATE || id == Constant.STATS_PETS_PDV || id == Constant.STATS_PETS_POIDS || id == Constant.STATS_PETS_EPO || id == Constant.STATS_PETS_REPAS))) {//Si le stats n'est pas vide et (n'est pas ï¿½gale ï¿½ 0 ou est de type familier)
                        if (!(this.getTemplate().getType() == Constant.ITEM_TYPE_CERTIFICAT_CHANIL && id == Constant.STATS_PETS_DATE)) {

                            txtStats.put(id, stats[3]);
                            continue;
                        }
                    }
                    if (id == Constant.STATS_RESIST && this.getTemplate() != null && this.getTemplate().getType() == 93) {
                        txtStats.put(id, stats[4]);
                        continue;
                    }
                    if (id == Constant.STATS_RESIST) {
                        txtStats.put(id, stats[4]);
                        continue;
                    }

                    if (id >= 281 && id <= 294) {
                        int jet = Integer.parseInt(stats[1], 16);
                        Stats.addOneStat(id, jet);
                        continue;
                    }



                    if(id >= 970 && id <= 974)
                    {
                        int jet = Integer.parseInt(stats[3]);
                        if(id == Constant.OBVIJEVANT_SKIN)
                        {
                            setObvijevanLook(Integer.parseInt(stats[3], 16));
                        } else if(id == Constant.OBVIJEVANT_TYPE)
                        {
                            setObvijevanPos(Integer.parseInt(stats[3], 16));
                        } else if(id == 970)
                        {
                            setObvijevanId(Integer.parseInt(stats[3], 16));
                        }
                        txtStats.put(id, String.valueOf(jet));
                        Stats.addOneStat(id, jet);
                        continue;
                    }


                    boolean follow2 = true;
                    for (int a : Constant.ARMES_EFFECT_IDS) {
                        if (a == id) {
                            int min = 0;
                            int max = 0;
                            try {
                                min = Integer.parseInt(stats[1], 16);
                                max = Integer.parseInt(stats[2], 16);
                            }
                            catch (Exception e) {
                                min = Integer.parseInt(stats[1]);
                                max = Integer.parseInt(stats[2]);
                            }
                            String jet = stats[4];

                            int weaponType = this.getTemplate().getType();
                            int weaponId = this.getTemplate().getId();

                            Effects.add(new Effect(id,weaponId,weaponType,jet,min,max));
                            follow2 = false;
                        }
                    }

                    if (!follow2)
                        continue;//Si c'ï¿½tait un effet Actif d'arme ou une signature

                    String statsString = stats[0];

                    // Gestion des dommages bizarres
                    int statMax = JobAction.getStatBaseMaxLegendaire(this.template, statsString );

                    /*if(this.getTemplate().getType() == Constant.ITEM_TYPE_FAMILIER){
                            Pet pets = World.world.getPets(this.template.getId() );
                            statMax = pets.getMax();
                            if (statMax < Integer.parseInt(stats[1], 16) && this.getTemplate().getId() != 6894) {
                                txtStats.put(Constant.STATS_CHANGE_BY, "Arwase [corrigé]");
                                Stats.addOneStat(id, statMax);
                                this.setModification();
                            }
                            else{
                                Stats.addOneStat(id, Integer.parseInt(stats[1], 16) );
                            }
                    }*/
                    if(this.getTemplate().getType() == Constant.ITEM_TYPE_FAMILIER){

                        Pet pets = World.world.getPets(this.getTemplate().getId() );
                        PetEntry petentry = World.world.getPetsEntry(this.getGuid());
                        statMax = pets.getMax();
                        double poid = Formulas.getWeightByStat(id);
                        if (statMax < Math.round(poid* (Integer.parseInt(stats[1], 16)) )  && this.getTemplate().getId() != 6894) {
                            txtStats.put(Constant.STATS_CHANGE_BY, "Arwase [corrigé]");
                            Stats.addOneStat(id, (int) Math.round(statMax/poid));
                            this.setModification();
                        }
                        else{
                            Stats.addOneStat(id, Integer.parseInt(stats[1], 16) );
                        }
                    }

                    if( statMax == 0 && statsString.equals("70")){
                        statsString = "79";
                        statMax = JobAction.getStatBaseMaxLegendaire(this.template, statsString );
                        id=121;
                    }

                    if( statMax == 0 && statsString.equals("79")  ) {
                        statsString = "70";
                        statMax = JobAction.getStatBaseMaxLegendaire(this.template, statsString );
                        id=112;
                    }

                    int poidmax = 151;
                    double poiduni = JobAction.getPwrPerEffet(id);
                    int poidligne = 0;
                    try{
                        poidligne = (int) Math.round( Integer.parseInt(stats[1], 16) *poiduni);
                    }
                    catch (Exception e){
                        poidligne = 0;
                    }

                    if( this.getTemplate().getType() == Constant.ITEM_TYPE_CERTIF_MONTURE || this.getTemplate().getType() == Constant.ITEM_TYPE_DRAGODINDE){
                        if(id == Constant.STATS_DD_ID) {
                            Stats.addOneStat(id, (int) Long.parseLong(stats[1], 16));
                            continue;
                        }
                       //
                    }


                    if(this.getTemplate().getType() != Constant.ITEM_TYPE_FAMILIER &&  this.getTemplate().getType() != Constant.ITEM_TYPE_FANTOME_FAMILIER  &&  this.getTemplate().getType() != Constant.ITEM_TYPE_CERTIFICAT_CHANIL) {
                        if(poidligne <= poidmax) {
                            try {
                                Stats.addOneStat(id, Integer.parseInt(stats[1], 16));
                            }
                            catch (Exception e){
                                //e.printStackTrace();
                                this.setModification();
                                System.out.println("Stats id = " + id + " la valeur " + stats[1] + " du coup ca bug item " + this.getTemplate().getName() + " guid " + this.getGuid() );
                                continue;
                            }
                        }
                        else {
                            if(statMax >= Integer.parseInt(stats[1], 16) ) {
                                Stats.addOneStat(id, Integer.parseInt(stats[1], 16));
                            }
                            else {
                                Stats.addOneStat(id, statMax);

                                System.out.println("["+ this.getGuid() +"]"+ "!! Item illégal :"+  this.template.getName() + " Stat "+ id + " valeur " + Integer.parseInt(stats[1], 16) + " Donc ligne a " + poidligne + " alors que max " + statMax);
                                this.txtStats.put(Constant.STATS_CHANGE_BY, "Arwase [Correction]");
                                this.setModification();
                            }
                        }
                    }
                    else {
                        if(this.getTemplate().getType() != Constant.ITEM_TYPE_FAMILIER  ) {
                            Stats.addOneStat(id, Integer.parseInt(stats[1], 16));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(this.getTemplate().getName() + " Stats pas géré " + strStats);
                }
            }
        }

        final String stringOrder = statsToOrder.toString();

        if(!stringOrder.isEmpty())
        {
            for(final String stat : Formulas.sortStatsByOrder(stringOrder).split(",")) {
                final String[] split = stat.split("#");
                final int id = Integer.parseInt(split[0], 16);
                final int value = Integer.parseInt(split[1], 16);
                Stats.addOneStat(id, value);
            }
        }

        if(save) {
            this.setModification();
            Database.getStatics().getObjectData().update(this);
        }
    }

    public void addTxtStat(int id, String stringName) {
        this.txtStats.put(id, stringName);
        //this.setModification();
    }

    private void getStatsClass() {
        for(final String s : this.getTemplate().getStrTemplate().split(",")) {
            if(s.isEmpty()) continue;
            String[] stats = s.split("#");
            final int statID = Integer.parseInt(stats[0], 16);
            if (statID < EffectConstant.STATS_SPELL_ADD_PO || statID > EffectConstant.STATS_SPELL_REM_PO)
                continue;
            this.SortStats.add(s);
        }
    }

    public static String whoGotObject(GameObject gameObject) {
        Long id = gameObject.getGuid();

        Collection<Account> compte = World.world.getAccounts();
        Player Owner = null;
        String Origine = null;


        //Pour tous les comptes
        for (Account s : compte) {
            boolean test = false;
            // On test la banques du compte
            if (s.getBank().contains(gameObject)) {
                Owner = Account.getFirstPlayer(s);
                Origine = "banque du joueur :" + s.getId();
                continue;
            }

            // On test les hvds du compte
            Map<Integer, ArrayList<HdvEntry>> hdvs = World.world.getMyItems(s.getId());
            for (Entry<Integer, ArrayList<HdvEntry>> iteminhdv : hdvs.entrySet()) {
                ArrayList<HdvEntry> items = iteminhdv.getValue();
                if (items.contains(gameObject)) {
                    Owner = Account.getFirstPlayer(s);
                    Origine = "En vente hdv :" + iteminhdv.getKey()  + " joueur " + Owner.getId() ;
                    continue;
                }
            }

            if (Origine != null) {
                continue;
            }
            // On test les  joueurs du compte
            Map<Integer, Player> players = s.getPlayers();
            for (Entry<Integer, Player> joueurs : players.entrySet()) {
                Player joueur = joueurs.getValue();

                Map<Long, GameObject> objectsOfPlayer = joueur.getItems();
                for (Entry<Long, GameObject> item : objectsOfPlayer.entrySet()) {
                    if (item.getValue() == gameObject) {
                        Owner = joueur;
                        Origine = "inventaire du joueur" + joueur.getId();
                        continue;
                    }

                }

                Map<Long, Integer> objectsventeofPlayer = joueur.getStoreItems();
                for (Entry<Long, Integer> itemenvente : objectsventeofPlayer.entrySet()) {
                    if (itemenvente.getValue() == gameObject.getGuid()) {
                        Owner = joueur;
                        Origine = "inventaire de vente du joueur";
                        continue;
                    }
                }

            }

        }

        // Pour tous les coffres
        if (Origine == null){
            Map<Integer, Trunk> alltrunks = World.world.getTrunks();
            for (Entry<Integer, Trunk> trunks : alltrunks.entrySet()) {
                int idOwner = trunks.getValue().getOwnerId();
                int idcoffre = trunks.getValue().getId();

                Map<Long, GameObject> ObjectinTrunks = trunks.getValue().getObject();

                for (Entry<Long, GameObject> object : ObjectinTrunks.entrySet()) {
                    if (object.getValue() == gameObject) {
                        Owner = World.world.getPlayer(idOwner);
                        Origine = "Dans un coffre " + idcoffre + " Appartenant a " + idOwner;
                        continue;
                    }
                }
            }
       }
        //Pour tous les percepteurs



        return Origine;
    }

    public static Collection<GameObject> getunkownOriginObject(){
        Collection<GameObject> objCOll = new ArrayList();
        CopyOnWriteArrayList<GameObject> allobjct = World.world.getGameObjects();

        for (GameObject object : allobjct) {
            String lol = whoGotObject(object);

            if(lol == null){
                //System.out.println( object.getGuid() +" - Pas trouvé" );
                objCOll.add(object);
            }
            else{
                //System.out.println( object.getGuid() +" - " +lol);
            }
        }

        return  objCOll;
    }

    public String getTraquedName() {
        for (Entry<Integer, String> entry : txtStats.entrySet()) {
            if (Integer.toHexString(entry.getKey()).compareTo("3dd") == 0) {
                return entry.getValue();
            }
        }
        return null;
    }

    public Stats getStats() {
        return Stats;
    }

    public void setStats(Stats SS) {
        Stats = SS;
        this.setModification();
    }

    public String stringObjectMimibiote()
    {
        StringBuilder str = new StringBuilder(String.valueOf(Long.toHexString(this.guid))
        + "~" + Integer.toHexString(getTemplate().getId())
        + "~" + Integer.toHexString(getQuantity()) + "~"
                + ((getPosition() == -1) ? "" : Integer.toHexString(getPosition()) + "~"
        + this.parseStatsString()));

        return str.toString();
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            quantity = 0;
        }
        if (quantity >= 100000)
        {
            if (Logging.USE_LOG)
                Logging.getInstance().write("Object", "Objet guid : " + guid + " a dépassé 100 000 qua (" + quantity + ") avec comme template : " + template.getName() + " (" + template.getId() + ")");
            quantity = 99999;
        }
        this.quantity = quantity;
        Database.getStatics().getObjectData().update(this);
        this.setModification();
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
        Database.getStatics().getObjectData().update(this);
        this.setModification();
    }

    public ObjectTemplate getTemplate() {
        return template;
    }

    public void setTemplate(int Tid) {
        this.template = World.world.getObjTemplate(Tid);
        Database.getStatics().getObjectData().update(this);
        this.setModification();
    }

    public long getGuid() {
        return guid;
    }

    public void setGuid(long guid) {
        this.guid= guid;
    }

    public Map<Integer, Integer> getSoulStat() {
        return SoulStats;
    }

    public Map<Integer, String> getTxtStat() {
        return this.txtStats;
    }

    public Map<Integer, String> CopyTxtStat() {
        Map<Integer, String> effetText = new HashMap<>();
        for (Map.Entry<Integer, String> entry : this.txtStats.entrySet()) {
            effetText.put(entry.getKey(),
                    entry.getValue());
        }
        return effetText;
    }

    public ArrayList<Effect> CopyEffectStat() {
        ArrayList<Effect> effectsCac = new ArrayList<>();
        for ( Effect test : this.getEffects() ) {
            switch (test.getEffectID()) {
                case EffectConstant.STATS_ADD_VIE: // Ahhh ca c'est pour ce heal (genre le pain etc)
                case EffectConstant.STATS_ADD_ENERGIE:  // Ahhh ca c'est pour l'energie (genre le pain etc)
                case EffectConstant.STATS_ADD_XP: // Parchemin XP
                case EffectConstant.STATS_ADD_XPJOB: // Parchemin XP JOB
                    int min = test.getArgs1();
                    int max = test.getArgs2();
                    int args3 = test.getArgs3();
                    String jet = "";
                    if(test.getJet() != null)
                        jet = test.getJet();

                    String args = min + ";" + max + ";" + args3 + ";" + jet;
                    effectsCac.add(new Effect(test.getEffectID(), args, 0, -1,false));
                    break;
                default:
                    int iddd = test.getEffectID();
                    int newmin = test.getArgs1();
                    int newmax = test.getArgs2();
                    String newjet = test.getJet();
                    int neWweaponType = this.getTemplate().getType();
                    int neWweaponId = this.getTemplate().getId();
                    effectsCac.add(new Effect(iddd,neWweaponId,neWweaponType,newjet,newmin,newmax));
                    break;
            }

        }
        return effectsCac;
    }

    public Mount setMountStats(Player player, Mount mount, boolean castrated) {
        if(mount == null)
            mount = new Mount(Constant.getMountColorByParchoTemplate(this.getTemplate().getId()), player.getId(), false);
        if(castrated) mount.setCastrated();

        this.clearStats();
        this.getStats().addOneStat(995, - ((int)mount.getId()));
        this.getTxtStat().put(996, player.getName());
        this.getTxtStat().put(997, mount.getName());
        this.setModification();
        return mount;
    }

    public void attachToPlayer(Player player) {
        this.getTxtStat().put(Constant.STATS_OWNER_1, player.getName());
        SocketManager.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(player, this);
        this.setModification();
        Database.getStatics().getObjectData().update(this);
    }

    public void dettachToPlayer(Player player) {
        this.getTxtStat().remove(Constant.STATS_OWNER_1);
        SocketManager.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(player, this);
        this.setModification();
        Database.getStatics().getObjectData().update(this);
    }

    public boolean isAttach() {
        boolean ok = this.getTxtStat().containsKey(Constant.STATS_OWNER_1);

        if(ok) {
            Player player = World.world.getPlayerByName(this.getTxtStat().get(Constant.STATS_OWNER_1));
            if(player != null) player.send("BN");
        }

        return ok;
    }

    public String parseItem() {
        String posi = position == Constant.ITEM_POS_NO_EQUIPED ? "" : Integer.toHexString(position);
        return Long.toHexString(guid) + "~"
                + Integer.toHexString(template.getId()) + "~"
                + Integer.toHexString(quantity) + "~" + posi + "~"
                + parseStatsString() +  "~"
                + rarity + ";";
    }

    public String parseItemNormal() {
        String posi = position == Constant.ITEM_POS_NO_EQUIPED ? "" : Integer.toHexString(position);
        return guid + "~"
                + template.getId() + "~"
                + quantity + "~" + posi + "~"
                + parseStatsString() +  "~"
                + rarity + ";";
    }

    public String getParamStatTexto(int stat, int parametro) {
        try {
            String s = getTxtStat().get(stat);
            if (!s.isEmpty()) {
                if (s.split("#").length > parametro - 1) {
                    return s.split("#")[parametro - 1];
                }
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    public String parseStatsString() {
        if (getTemplate().getType() == Constant.ITEM_TYPE_PIERRE_AME) //Si c'est une pierre d'ï¿½me vide
            return getTemplate().getStrTemplate();

        StringBuilder stats = new StringBuilder();
        boolean isFirst = true;

        // Effet d'arme en premier en général
        if(Effects != null) {
            for (Effect SE : Effects) {
                if (!isFirst)
                    stats.append(",");

                try {
                    switch (SE.getEffectID()) {
                        //case EffectConstant.STATS_ADD_XPJOB: // Pk c'est le seul cas différent, a étudier
                        //    stats.append(Integer.toHexString(SE.getEffectID())).append("#").append(Integer.toHexString(SE.getArgs1())).append("#").append(Integer.toHexString(SE.getArgs2())).append("#").append(Integer.toHexString(SE.getArgs3()));

                        //   break;
                        default:
                            if(SE.getJet() != "" && SE.getJet() != null) {
                                if (SE.getArgs3() == 0)
                                    stats.append(Integer.toHexString(SE.getEffectID())).append("#").append(Integer.toHexString(SE.getArgs1())).append("#").append(Integer.toHexString(SE.getArgs2())).append("#").append(0).append("#").append(SE.getJet());
                                else
                                    stats.append(Integer.toHexString(SE.getEffectID())).append("#").append(Integer.toHexString(SE.getArgs1())).append("#").append(Integer.toHexString(SE.getArgs2())).append("#").append(Integer.toHexString(SE.getArgs3())).append("#").append(SE.getJet());
                            }
                            else {
                                stats.append(Integer.toHexString(SE.getEffectID())).append("#").append(Integer.toHexString(SE.getArgs1())).append("#").append(Integer.toHexString(SE.getArgs2())).append("#").append(Integer.toHexString(SE.getArgs3()));
                            }
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
                isFirst = false;
            }
        }

        // Stats de texte en second
        if(txtStats != null) {
            for (Entry<Integer, String> entry : txtStats.entrySet()) {
                if (!isFirst)
                    stats.append(",");
                if (template.getType() == 77 || template.getType() == 90) {
                    if (entry.getKey() == Constant.STATS_PETS_PDV)
                        stats.append(Integer.toHexString(entry.getKey())).append("#").append(entry.getValue()).append("#0#").append(entry.getValue());
                    if (entry.getKey() == Constant.STATS_PETS_EPO)
                        stats.append(Integer.toHexString(entry.getKey())).append("#").append(entry.getValue()).append("#0#").append(entry.getValue());
                    if (entry.getKey() == Constant.STATS_PETS_REPAS)
                        stats.append(Integer.toHexString(entry.getKey())).append("#").append(entry.getValue()).append("#0#").append(entry.getValue());
                    if (entry.getKey() == Constant.STATS_PETS_POIDS) {
                        int corpu = 0;
                        int corpulence = 0;
                        String c = entry.getValue();
                        if (c != null && !c.equalsIgnoreCase("")) {
                            try {
                                corpulence = Integer.parseInt(c);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if (corpulence != 0)
                            corpu = 7;
                        stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(corpu)).append("#").append(corpulence > 0 ? corpu : 0).append("#").append(Integer.toHexString(corpu));
                    }
                    if (entry.getKey() == Constant.STATS_PETS_DATE && template.getType() == 77) {
                        if (entry.getValue().contains("#"))
                            stats.append(Integer.toHexString(entry.getKey())).append(entry.getValue());
                        else {
                            stats.append(Integer.toHexString(entry.getKey())).append(Formulas.convertToDate(Long.parseLong(entry.getValue())));
                            // System.out.println("date " + Formulas.convertToDate(Long.parseLong(entry.getValue())) + " " + Integer.toHexString(entry.getKey()));
                        }
                    }
                } else if (entry.getKey() == Constant.APPARAT_ITEM) {
                    stats.append(Integer.toHexString(entry.getKey())).append("#0#0#").append(Integer.toHexString(Integer.parseInt(entry.getValue())));
                } else if (entry.getKey() == Constant.APPARAT_NAME) {
                    stats.append(Integer.toHexString(entry.getKey())).append("#0#0#").append(entry.getValue());
                } else if ((entry.getKey() == 970) || (entry.getKey() == 971) || (entry.getKey() == 972) || (entry.getKey() == 973) || (entry.getKey() == 974)) {
                    int value = Integer.parseInt(entry.getValue());
                    int value2 = Integer.parseInt(entry.getValue(), 16);
                    int statid = entry.getKey();
                    if (statid == 970) {
                        stats.append(Integer.toHexString(statid)).append("#0#0#").append(Integer.toHexString(Integer.parseInt(entry.getValue())));
                    } else {
                        stats.append(Integer.toHexString(statid)).append("#0#0#").append(value);
                    }
                    if (statid == 973) setObvijevanPos(value2);
                    if (statid == 972) setObvijevanLook(value);
                    if (statid == 970) setObvijevanId(value);
                } else if (entry.getKey() == Constant.STATS_CHANGE_BY || entry.getKey() == Constant.STATS_NAME_TRAQUE || entry.getKey() == Constant.STATS_OWNER_1) {
                    stats.append(Integer.toHexString(entry.getKey())).append("#0#0#0#").append(entry.getValue());
                } else if (entry.getKey() == Constant.STATS_GRADE_TRAQUE || entry.getKey() == Constant.STATS_ALIGNEMENT_TRAQUE || entry.getKey() == Constant.STATS_NIVEAU_TRAQUE) {
                    stats.append(Integer.toHexString(entry.getKey())).append("#0#0#").append(entry.getValue()).append("#0");
                } else if (entry.getKey() == Constant.STATS_NAME_DJ) {
                    if (entry.getValue().equals("0d0+0"))
                        continue;
                    for (String i : entry.getValue().split(",")) {
                        stats.append(",").append(Integer.toHexString(entry.getKey())).append("#0#0#").append(i);
                    }
                    continue;
                } else if (entry.getKey() == Constant.STATS_DATE) {
                    String item = entry.getValue();
                    if (item.contains("#")) {
                        String date = item.split("#")[3];
                        if (date != null && !date.equalsIgnoreCase(""))
                            stats.append(Integer.toHexString(entry.getKey())).append(Formulas.convertToDate(Long.parseLong(date)));
                    } else
                        stats.append(Integer.toHexString(entry.getKey())).append(Formulas.convertToDate(Long.parseLong(item)));
                } else if (entry.getKey() == Constant.CAPTURE_MONSTRE || entry.getKey() == Constant.CAPTURE_MONSTRE1 || entry.getKey() == Constant.CAPTURE_MONSTRE2) {
                    stats.append(Integer.toHexString(entry.getKey())).append("#0#0#").append(entry.getValue());
                } else if (entry.getKey() == Constant.STATS_PETS_PDV
                        || entry.getKey() == Constant.STATS_PETS_POIDS
                        || entry.getKey() == Constant.STATS_PETS_DATE
                        || entry.getKey() == Constant.STATS_PETS_REPAS) {
                    PetEntry p = World.world.getPetsEntry(this.getGuid());
                    if (p == null) {
                        if (entry.getKey() == Constant.STATS_PETS_PDV)
                            stats.append(Integer.toHexString(entry.getKey())).append("#").append("a").append("#0#a");
                        if (entry.getKey() == Constant.STATS_PETS_POIDS)
                            stats.append(Integer.toHexString(entry.getKey())).append("#").append("0").append("#0#0");
                        if (entry.getKey() == Constant.STATS_PETS_DATE)
                            stats.append(Integer.toHexString(entry.getKey())).append("#").append("0").append("#0#0");
                        if (entry.getKey() == Constant.STATS_PETS_REPAS)
                            stats.append(Integer.toHexString(entry.getKey())).append("#").append("0").append("#0#0");
                    } else {
                        if (entry.getKey() == Constant.STATS_PETS_PDV)
                            stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(p.getPdv())).append("#0#").append(Integer.toHexString(p.getPdv()));
                        if (entry.getKey() == Constant.STATS_PETS_POIDS)
                            stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toString(p.parseCorpulence())).append("#").append(p.getCorpulence() > 0 ? p.parseCorpulence() : 0).append("#").append(Integer.toString(p.parseCorpulence()));
                        if (entry.getKey() == Constant.STATS_PETS_DATE) {
                            stats.append(Integer.toHexString(entry.getKey())).append(p.parseLastEatDate());
                        }
                        if (entry.getKey() == Constant.STATS_PETS_REPAS)
                            stats.append(Integer.toHexString(entry.getKey())).append("#").append(entry.getValue()).append("#0#").append(entry.getValue());
                        if (p.getIsEupeoh()
                                && entry.getKey() == Constant.STATS_PETS_EPO)
                            stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(p.getIsEupeoh() ? 1 : 0)).append("#0#").append(Integer.toHexString(p.getIsEupeoh() ? 1 : 0));
                    }
                } else if (entry.getKey() == Constant.STATS_RESIST && getTemplate().getType() == 93) {
                    stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(getResistanceMax(getTemplate().getStrTemplate()))).append("#").append(entry.getValue()).append("#").append(Integer.toHexString(getResistanceMax(getTemplate().getStrTemplate())));
                } else if (entry.getKey() == Constant.STATS_RESIST) {
                    stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(getResistanceMax(getTemplate().getStrTemplate()))).append("#").append(entry.getValue()).append("#").append(Integer.toHexString(getResistanceMax(getTemplate().getStrTemplate())));
                } else {
                    stats.append(Integer.toHexString(entry.getKey())).append("#0#0#0#").append(entry.getValue());
                }
                isFirst = false;
            }
        }

        // Stats de Pierre d'ames
        if(txtStats != null) {
            for (Entry<Integer, Integer> entry : SoulStats.entrySet()) {
                if (!isFirst)
                    stats.append(",");

                if (this.getTemplate().getType() == 18)
                    stats.append(Integer.toHexString(Constant.STATS_PETS_SOUL)).append("#").append(Integer.toHexString(entry.getKey())).append("#").append("0").append("#").append(Integer.toHexString(entry.getValue()));
                if (entry.getKey() == Constant.STATS_NIVEAU)
                    stats.append(Integer.toHexString(Constant.STATS_NIVEAU)).append("#").append(Integer.toHexString(entry.getKey())).append("#").append("0").append("#").append(Integer.toHexString(entry.getValue()));
                isFirst = false;
            }
        }

        // Stats tout genre autre
        if(txtStats != null) {
            for (Entry<Integer, Integer> entry : Stats.getMap().entrySet()) {

                int statID = entry.getKey();
                // Dans le cas d'un effet de spell
                if (EffectConstant.IS_SPELL_BOOST_EFFECT(statID)) {
                    String[] modificable = template.getStrTemplate().split(",");
                    int cantMod = modificable.length;
                    for (int j = 0; j < cantMod; j++) {
                        String[] mod = modificable[j].split("#");
                        try{
                            Integer.parseInt(mod[0], 16);
                        }
                        catch (Exception e){
                            continue;
                        }
                        if (Integer.parseInt(mod[0], 16) == statID) {
                            String jet = "0d0+" + Integer.parseInt(mod[1], 16);
                            if (!isFirst) {
                                stats.append(",");
                            } else {
                                if (stats.length() > 0) {
                                    stats = new StringBuilder();
                                }
                            }
                            stats.append(mod[0]).append("#").append(mod[1]).append("#0#").append(mod[3]).append("#").append(jet);
                            isFirst = false;
                        }
                    }
                } else {

                    if (!isFirst)
                        stats.append(",");
                    if (statID == 615) {
                        stats.append(Integer.toHexString(statID)).append("#0#0#").append(Integer.toHexString(entry.getValue()));
                    } else if ((statID == 970) || (statID == 971) || (statID == 972)
                            || (statID == 973) || (statID == 974)) {
                        int jet = entry.getValue();
                        if (statID == 970) {
                            stats.append(Integer.toHexString(statID)).append("#0#0#").append(Integer.toHexString(jet));
                        } else {
                            stats.append(Integer.toHexString(statID)).append("#0#0#").append(jet);
                        }
                        if (statID == 973)
                            setObvijevanPos(Integer.parseInt(String.valueOf(jet), 16));
                        if (statID == 972)
                            setObvijevanLook(jet);
                        if (statID == 970)
                            setObvijevanId(jet);
                    } else if (statID == Constant.STATS_TURN) {
                        String jet = "0d0+" + entry.getValue();
                        stats.append(Integer.toHexString(statID)).append("#");
                        stats.append("0#0#").append(Integer.toHexString(entry.getValue())).append("#").append(jet);
                    } else {
                        String jet = "0d0+" + entry.getValue();
                        stats.append(Integer.toHexString(statID)).append("#");
                        stats.append(Integer.toHexString(entry.getValue())).append("#0#0#").append(jet);
                        isFirst = false;
                    }
                }
            }
        }

        return stats.toString();
    }

    public String parseStatsStringSansUserObvi() {
        if (getTemplate().getType() == 83) //Si c'est une pierre d'ï¿½me vide
            return getTemplate().getStrTemplate();

        StringBuilder stats = new StringBuilder();
        boolean isFirst = true;

        if (this instanceof Fragment) {
            Fragment fragment = (Fragment) this;
            for (Couple<Integer, Integer> couple : fragment.getRunes()) {
                stats.append((stats.toString().isEmpty() ? couple.first : ";"
                        + couple.first)).append(":").append(couple.second);
            }
            return stats.toString();
        }

        for (Entry<Integer, String> entry : txtStats.entrySet()) {
            if (!isFirst)
                stats.append(",");
            if (template.getType() == 77) {
                if (entry.getKey() == Constant.STATS_PETS_PDV)
                    stats.append(Integer.toHexString(entry.getKey())).append("#").append(entry.getValue()).append("#0#").append(entry.getValue());
                if (entry.getKey() == Constant.STATS_PETS_POIDS)
                    stats.append(Integer.toHexString(entry.getKey())).append("#").append(entry.getValue()).append("#").append(entry.getValue()).append("#").append(entry.getValue());
                if (entry.getKey() == Constant.STATS_PETS_DATE) {
                    if (entry.getValue().contains("#")){
                        stats.append(Integer.toHexString(entry.getKey())).append(entry.getValue());
                    }
                    else{
                        stats.append(Integer.toHexString(entry.getKey())).append(Formulas.convertToDate(Long.parseLong(entry.getValue())));
                    }
                }
                //stats.append(Integer.toHexString(entry.getKey())).append(Formulas.convertToDate(Long.parseLong(entry.getValue())));
            } else if (entry.getKey() == Constant.STATS_DATE) {
                if (entry.getValue().contains("#"))
                    stats.append(Integer.toHexString(entry.getKey())).append(entry.getValue());
                else
                    stats.append(Integer.toHexString(entry.getKey())).append("#0#0#").append(Long.parseLong(entry.getValue()));
            } else if (entry.getKey() == Constant.STATS_CHANGE_BY || entry.getKey() == Constant.STATS_NAME_TRAQUE || entry.getKey() == Constant.STATS_OWNER_1) {
                stats.append(Integer.toHexString(entry.getKey())).append("#0#0#0#").append(entry.getValue());
            } else if (entry.getKey() == Constant.STATS_GRADE_TRAQUE || entry.getKey() == Constant.STATS_ALIGNEMENT_TRAQUE || entry.getKey() == Constant.STATS_NIVEAU_TRAQUE) {
                stats.append(Integer.toHexString(entry.getKey())).append("#0#0#").append(entry.getValue()).append("#0");
            } else if (entry.getKey() == Constant.STATS_NAME_DJ) {
                for (String i : entry.getValue().split(","))
                    stats.append(",").append(Integer.toHexString(entry.getKey())).append("#0#0#").append(i);
            } else if (entry.getKey() == Constant.CAPTURE_MONSTRE) {
                stats.append(Integer.toHexString(entry.getKey())).append("#0#0#").append(entry.getValue());
            } else if(entry.getKey() == Constant.APPARAT_ITEM) {
                stats.append(Integer.toHexString(entry.getKey())).append("#0#0#").append(entry.getValue());
            }
            else if(entry.getKey() == Constant.APPARAT_NAME) {
                stats.append(Integer.toHexString(entry.getKey())).append("#0#0#").append(entry.getValue());
            }
            else if(entry.getKey() >= 970 && entry.getKey() <= 974)
            {
                continue;
            } else if (entry.getKey() == Constant.STATS_PETS_PDV
                    || entry.getKey() == Constant.STATS_PETS_POIDS
                    || entry.getKey() == Constant.STATS_PETS_DATE) {



                PetEntry p = World.world.getPetsEntry(this.getGuid());
                if (p == null) {
                    if (entry.getKey() == Constant.STATS_PETS_PDV)
                        stats.append(Integer.toHexString(entry.getKey())).append("#").append("a").append("#0#a");
                    if (entry.getKey() == Constant.STATS_PETS_POIDS)
                        stats.append(Integer.toHexString(entry.getKey())).append("#").append("0").append("#0#0");
                    if (entry.getKey() == Constant.STATS_PETS_DATE) {
                        stats.append(Integer.toHexString(entry.getKey())).append("#").append("0").append("#0#0");
                        //System.out.println(" l'item " + this.getGuid() + " : " + this.getTemplate().getName() );
                    }
                } else {
                    if (entry.getKey() == Constant.STATS_PETS_PDV)
                        stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(p.getPdv())).append("#0#").append(Integer.toHexString(p.getPdv()));
                    if (entry.getKey() == Constant.STATS_PETS_POIDS)
                        stats.append(Integer.toHexString(entry.getKey())).append("#").append(p.parseCorpulence()).append("#").append(p.getCorpulence() > 0 ? p.parseCorpulence() : 0).append("#").append(p.parseCorpulence());
                    if (entry.getKey() == Constant.STATS_PETS_DATE) {
                        stats.append(Integer.toHexString(entry.getKey())).append(p.parseLastEatDate());
                    }
                    if (p.getIsEupeoh()
                            && entry.getKey() == Constant.STATS_PETS_EPO)
                        stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(p.getIsEupeoh() ? 1 : 0)).append("#0#").append(Integer.toHexString(p.getIsEupeoh() ? 1 : 0));
                }
            } else {
                    stats.append(Integer.toHexString(entry.getKey())).append("#0#0#0#").append(entry.getValue());
            }
            isFirst = false;
        }
        /*
         * if(isCertif && !db) return stats.toString();
         */

        for (Effect SE : Effects) {
            if (!isFirst)
                stats.append(",");

            try {
                switch (SE.getEffectID()) {
                    //case EffectConstant.STATS_ADD_XPJOB: // Pk c'est le seul cas différent, a étudier
                        //stats.append(Integer.toHexString(SE.getEffectID())).append("#0#0#").append(Integer.toHexString(SE.getArgs1())).append("#").append(SE.getJet());
                        //stats.append(Integer.toHexString(SE.getEffectID())).append("#0#0#").append(Integer.toHexString(SE.getArgs1())).append("#").append(SE.getJet());
                        //stats.append(Integer.toHexString(SE.getEffectID())).append("#0#0#").append(Integer.toHexString(SE.getArgs1())).append("#").append(SE.getJet());
                        //break;

                    default:
                        if(SE.getJet() != "" && SE.getJet() != null) {
                            if (SE.getArgs3() == 0)
                                stats.append(Integer.toHexString(SE.getEffectID())).append("#").append(Integer.toHexString(SE.getArgs1())).append("#").append(Integer.toHexString(SE.getArgs2())).append("#").append(0).append("#").append(SE.getJet());
                            else
                                stats.append(Integer.toHexString(SE.getEffectID())).append("#").append(Integer.toHexString(SE.getArgs1())).append("#").append(Integer.toHexString(SE.getArgs2())).append("#").append(Integer.toHexString(SE.getArgs3())).append("#").append(SE.getJet());
                        }
                        else {
                            stats.append(Integer.toHexString(SE.getEffectID())).append("#").append(Integer.toHexString(SE.getArgs1())).append("#").append(Integer.toHexString(SE.getArgs2())).append("#").append(Integer.toHexString(SE.getArgs3()));
                        }
                         break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            isFirst = false;
        }
        for (Entry<Integer, Integer> entry : SoulStats.entrySet()) {
            if (!isFirst)
                stats.append(",");
            stats.append(Integer.toHexString(Constant.STATS_PETS_SOUL)).append("#").append(Integer.toHexString(entry.getKey())).append("#").append("0").append("#").append(Integer.toHexString(entry.getValue()));
            isFirst = false;
        }
        for (Entry<Integer, Integer> entry : Stats.getMap().entrySet()) {
                if (!isFirst)
                    stats.append(",");

                if (entry.getKey() == 615) {
                    stats.append(Integer.toHexString(entry.getKey())).append("#0#0#").append(Integer.toHexString(entry.getValue()));
                }
                else if (entry.getKey() >= 970 && entry.getKey() <= 974)
                {
                    stats.append(Integer.toHexString(entry.getKey())).append('#').append(entry.getValue()).append("#0#").append(entry.getValue());
                }
                else {
                        String jet = "0d0+" + entry.getValue();
                        stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(entry.getValue()));
                        stats.append("#0#0#").append(jet);
                }
                isFirst = false;
            }
        return stats.toString();
    }

    public String parseToSave() {
        return parseStatsStringSansUserObvi();
    }

    public String obvijevanOCO_Packet(int pos) {
        String strPos = String.valueOf(pos);
        if (pos == -1)
            strPos = "";
        String upPacket = "OCO";
        upPacket = upPacket + Long.toHexString(getGuid()) + "~";
        upPacket = upPacket + Integer.toHexString(getTemplate().getId()) + "~";
        upPacket = upPacket + Integer.toHexString(getQuantity()) + "~";
        upPacket = upPacket + strPos + "~";
        upPacket = upPacket + parseStatsString();
        this.setModification();
        return upPacket;
    }

    public void obvijevanNourir(GameObject obj) {
        if (obj == null)
            return;
        for (Entry<Integer, Integer> entry : Stats.getMap().entrySet()) {
            if (entry.getKey() != 974) // on ne boost que la stat de l'expï¿½rience de l'obvi
                continue;
            if (entry.getValue() > 500) // si le boost a une valeur supï¿½rieure ï¿½ 500 (irrï¿½aliste)
                return;
            entry.setValue(entry.getValue()
                    + obj.getTemplate().getLevel() / 3);
        }
        for (Entry<Integer, String> entry : txtStats.entrySet()) {
            if(entry.getKey() != 974)
            {
                continue;
            }
            if(Integer.parseInt(entry.getValue()) > 500)
            {
                return;
            }
            entry.setValue(String.valueOf(Integer.parseInt(entry.getValue()) + obj.getTemplate().getLevel() / 3));
        }
        this.setModification();
    }

    public void obvijevanChangeStat(int statID, int val) {
        for (Entry<Integer, Integer> entry : Stats.getMap().entrySet()) {
            if (entry.getKey() != statID)
                continue;
            entry.setValue(val);
        }

        for (Entry<Integer, String> entry : txtStats.entrySet()) {
            if(entry.getKey() != statID)
            {
                continue;
            }
            entry.setValue(String.valueOf(val));
        }
        this.setModification();
    }

    public void removeAllObvijevanStats() {
        setObvijevanPos(0);
        setObvijevanId(0);
        setObvijevanLook(0);
        client.other.Stats StatsSansObvi = new Stats();
        for (Entry<Integer, Integer> entry : Stats.getMap().entrySet()) {
            int statID = entry.getKey();
            if ((statID == 970) || (statID == 971) || (statID == 972)
                    || (statID == 973) || (statID == 974))
                continue;
            StatsSansObvi.addOneStat(statID, entry.getValue());
        }
        Map<Integer, String> newTxtStats = new HashMap<>();
        for (Entry<Integer, String> entry : txtStats.entrySet()) {
            int statID = entry.getKey();
            if ((statID == 970) || (statID == 971) || (statID == 972)
                    || (statID == 973) || (statID == 974)) {
                continue;
            }
            newTxtStats.put(entry.getKey(), entry.getValue());
        }
        txtStats = newTxtStats;
        Stats = StatsSansObvi;
        this.setModification();
    }

    public void removeAll_ExepteObvijevanStats() {
        setObvijevanPos(0);
        client.other.Stats StatsSansObvi = new Stats();
        for (Entry<Integer, Integer> entry : Stats.getMap().entrySet()) {
            int statID = entry.getKey();
            if ((statID != 971) && (statID != 972) && (statID != 973)
                    && (statID != 974))
                continue;
            StatsSansObvi.addOneStat(statID, entry.getValue());
        }
        Stats = StatsSansObvi;
        this.setModification();
    }

    public String getObvijevanStatsOnly() {
        GameObject obj = getCloneObjet(this, 1);
        obj.removeAll_ExepteObvijevanStats();
        this.setModification();
        return obj.parseStatsStringSansUserObvi();
    }

    /* *********FM SYSTEM********* */

    public Stats generateNewStatsFromTemplate(String statsTemplate,
                                              boolean useMax) {
        Stats itemStats = new Stats(false, null);
        //Si stats Vides
        if (statsTemplate.equals("") || statsTemplate == null)
            return itemStats;

        String[] splitted = statsTemplate.split(",");
        for (String s : splitted) {
            String[] stats = s.split("#");
            int statID = Integer.parseInt(stats[0], 16);
            boolean follow = true;

            for (int a : Constant.ARMES_EFFECT_IDS)
                //Si c'est un Effet Actif
                if (a == statID)
                    follow = false;
            if (!follow)
                continue;//Si c'ï¿½tait un effet Actif d'arme

            String jet = "";
            int value = 1;
            try {
                jet = stats[4];
                value = Formulas.getRandomJet(jet);
                if (useMax) {
                    try {
                        //on prend le jet max
                        int min = Integer.parseInt(stats[1], 16);
                        int max = Integer.parseInt(stats[2], 16);
                        value = min;
                        if (max != 0)
                            value = max;
                    } catch (Exception e) {
                        e.printStackTrace();
                        value = Formulas.getRandomJet(jet);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(value > 0) {
                itemStats.addOneStat(statID, value);
            }
        }
        return itemStats;
    }

    public ArrayList<Effect> getEffects() {
        return Effects;
    }

    public ArrayList<Effect> getCritEffects() {

        ArrayList<Effect> effets = new ArrayList<Effect>();
        for (Effect SE : Effects) {
            try {
                boolean boost = true;
                for (int i : Constant.NO_BOOST_CC_IDS)
                    if (i == SE.getEffectID())
                        boost = false;

                //String[] infos = SE.getArgs().split(";");
                if (!boost) {
                    effets.add(SE);
                    continue;
                }

                int min = SE.getArgs1() + (boost ? template.getBonusCC() : 0);
                int max = SE.getArgs2() + (boost ? template.getBonusCC() : 0);

                String jet = "1d" + (max - min + 1) + "+" + (min - 1);
                //String newArgs = Integer.toHexString(min)+";"+Integer.toHexString(max)+";-1;-1;0;"+jet;
                effets.add(new Effect(SE.getEffectID(), template.getId(),template.getType(),jet, min, max));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return effets;
    }

    public void clearStats() {
        //On vide l'item de tous ces effets
        Stats = new Stats();
        Effects.clear();
        txtStats.clear();
        SortStats.clear();
        SoulStats.clear();
        this.setModification();
    }

    public void refreshStatsObjet(String newsStats) {
        parseStringToStats(newsStats, true);
        this.setModification();
    }

    public int getResistance(String statsTemplate) {
        int Resistance = 0;

        String[] splitted = statsTemplate.split(",");
        for (String s : splitted) {
            String[] stats = s.split("#");
            if (Integer.parseInt(stats[0], 16) == Constant.STATS_RESIST) {
                Resistance = Integer.parseInt(stats[2], 16);
            }
        }
        return Resistance;
    }

    public int getResistanceMax(String statsTemplate) {
        int ResistanceMax = 0;

        String[] splitted = statsTemplate.split(",");
        for (String s : splitted) {
            String[] stats = s.split("#");
            if (Integer.parseInt(stats[0], 16) == Constant.STATS_RESIST) {
                ResistanceMax = Integer.parseInt(stats[1], 16);
            }
        }
        return ResistanceMax;
    }


    public int getRandomValue(String statsTemplate, int statsId) {
        if (statsTemplate.equals(""))
            return 0;

        String[] splitted = statsTemplate.split(",");
        int value = 0;
        for (String s : splitted) {
            String[] stats = s.split("#");
            int statID = Integer.parseInt(stats[0], 16);
            if (statID != statsId)
                continue;
            String jet;
            try {
                int min = Integer.parseInt(stats[1],16);
                int max = Integer.parseInt(stats[2],16);
                if(max == -1 || max == 0)
                    return min;
                if(max <= min)
                    return min;

                Random random = new Random();
                return random.nextInt(max - min) + min;

                //value = Formulas.getRandomJet(jet);
            } catch (Exception e) {
                e.printStackTrace();
                return Integer.parseInt(stats[1]);
            }
        }
        return value;
    }

    public String parseFMStatsString(String statsstr, int add, boolean negatif) {
        String stats = "";
        boolean isFirst = true;
        for (Effect SE : this.Effects) {
            if (!isFirst)
                stats += ",";
            try {
                stats += Integer.toHexString(SE.getEffectID()) + "#" + Integer.toHexString(SE.getArgs1()) + "#" + Integer.toHexString(SE.getArgs2()) + "#0#" + SE.getJet();
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            isFirst = false;
        }
        for (Entry<Integer, Integer> entry : this.Stats.getMap().entrySet()) {
            if (!isFirst)
                stats += ",";

            if(entry.getKey() < 970 | entry.getKey() > 974) {
                if (Integer.toHexString(entry.getKey()).compareTo(statsstr) == 0) {
                    int newstats = 0;
                    @SuppressWarnings("unused")
                    int newstats2 = 0;
                    if (negatif) {
                        newstats = entry.getValue() - add;
                        if (add < 1)
                            continue;
                    } else {
                        newstats = entry.getValue() + add;
                    }
                    String jet = "0d0+" + newstats;
                    stats += Integer.toHexString(entry.getKey()) + "#"
                            + Integer.toHexString(newstats) + "#0#0#"
                            + jet;
                } else {
                    String jet = "0d0+" + entry.getValue();
                    stats += Integer.toHexString(entry.getKey()) + "#"
                            + Integer.toHexString(entry.getValue()) + "#0#0#" + jet;
                }
            } else
            {
                stats += Integer.toHexString(entry.getKey()) + "#0#0#" + entry.getValue();
            }
            isFirst = false;
        }
        for (Entry<Integer, String> entry : this.txtStats.entrySet()) {
            if (!isFirst)
                stats += ",";
                if(entry.getKey() < 970 ||entry.getKey() > 974) {
                    if(entry.getKey() == Constant.APPARAT_ITEM || entry.getKey() == Constant.APPARAT_NAME )
                    {
                        stats += Integer.toHexString(entry.getKey()) + "#0#0#" + entry.getValue();
                    }
                    else {
                        stats += Integer.toHexString(entry.getKey()) + "#0#0#0#"
                                + entry.getValue();
                    }
                }
                else{
                    stats += Integer.toHexString(entry.getKey()) + "#0#0#" + entry.getValue();
                }
                isFirst = false;
        }
        return stats;
    }

    public static byte viewActualStatsItem(GameObject obj, String stats)//retourne vrai si le stats est actuellement sur l'item
    {
        if (!obj.parseStatsString().isEmpty()) { // si l'obj est pas vide
            for (Entry<Integer, Integer> entry : obj.getStats().getMap().entrySet()) { // Toutes les entrées
                if (Integer.toHexString(entry.getKey()).compareTo(stats) > 0)//Effets inutiles
                {
                    if (Integer.toHexString(entry.getKey()).compareTo("98") == 0  // C'est un cas négatif
                            && stats.compareTo("7b") == 0) {
                        return 2;
                    } else if (Integer.toHexString(entry.getKey()).compareTo("9a") == 0
                            && stats.compareTo("77") == 0) {
                        return 2;
                    } else if (Integer.toHexString(entry.getKey()).compareTo("9b") == 0
                            && stats.compareTo("7e") == 0) {
                        return 2;
                    } else if (Integer.toHexString(entry.getKey()).compareTo("9d") == 0
                            && stats.compareTo("76") == 0) {
                        return 2;
                    } else if (Integer.toHexString(entry.getKey()).compareTo("74") == 0
                            && stats.compareTo("75") == 0) {
                        return 2;
                    } else if (Integer.toHexString(entry.getKey()).compareTo("99") == 0
                            && stats.compareTo("7d") == 0) {
                        return 2;
                    }
                }
                else if (Integer.toHexString(entry.getKey()).compareTo(stats) == 0)//L'effet existe bien !
                {
                    return 1;
                }
            }
            return 0;
        } else {
            return 0;
        }
    }

    public static byte viewBaseStatsItem(GameObject obj, String ItemStats)//retourne vrai si le stats existe de base sur l'item
    {

        String[] splitted = obj.getTemplate().getStrTemplate().split(",");
        for (String s : splitted) {
            String[] stats = s.split("#");
            if (stats[0].compareTo(ItemStats) > 0)//Effets n'existe pas de base
            {
                if (stats[0].compareTo("98") == 0
                        && ItemStats.compareTo("7b") == 0) {
                    return 2; //
                } else if (stats[0].compareTo("9a") == 0
                        && ItemStats.compareTo("77") == 0) {
                    return 2;
                } else if (stats[0].compareTo("9b") == 0
                        && ItemStats.compareTo("7e") == 0) {
                    return 2;
                } else if (stats[0].compareTo("9d") == 0
                        && ItemStats.compareTo("76") == 0) {
                    return 2;
                } else if (stats[0].compareTo("74") == 0
                        && ItemStats.compareTo("75") == 0) {
                    return 2;
                } else if (stats[0].compareTo("99") == 0
                        && ItemStats.compareTo("7d") == 0) {
                    return 2;
                } else {
                }
            } else if (stats[0].compareTo(ItemStats) == 0)//L'effet existe bien !
            {
                return 1;
            }
        }
        return 0;
    }

    public void setNewStats(String statsObjectFm,int statsAdd) {
        int ItemCurrentStats = viewActualStatsItem(this, statsObjectFm);
        String statsStr = "";
        String statStringObj = this.parseStatsString() ;
        boolean negative = false;
        if (ItemCurrentStats == 2) {
            if (statsObjectFm.compareTo("7b") == 0) {
                statsObjectFm = "98";
                negative = true;
            }
            if (statsObjectFm.compareTo("77") == 0) {
                statsObjectFm = "9a";
                negative = true;
            }
            if (statsObjectFm.compareTo("7e") == 0) {
                statsObjectFm = "9b";
                negative = true;
            }
            if (statsObjectFm.compareTo("76") == 0) {
                statsObjectFm = "9d";
                negative = true;
            }
            if (statsObjectFm.compareTo("7c") == 0) {
                statsObjectFm = "9c";
                negative = true;
            }
            if (statsObjectFm.compareTo("7d") == 0) {
                statsObjectFm = "99";
                negative = true;
            }
        }
        if (ItemCurrentStats == 1 || ItemCurrentStats == 2) {
            if (statStringObj.isEmpty()) {
                statsStr = statsObjectFm + "#"
                        + Integer.toHexString(statsAdd) + "#0#0#0d0+"
                        + statsAdd;
                this.clearStats();
                this.refreshStatsObjet(statsStr);

            } else {
                statsStr = this.parseFMStatsString(statsObjectFm, statsAdd, negative);
                this.clearStats();
                this.refreshStatsObjet(statsStr);
            }
        } else {
            if (statStringObj.isEmpty()) {
                statsStr = statsObjectFm + "#"
                        + Integer.toHexString(statsAdd) + "#0#0#0d0+"
                        + statsAdd;
                this.clearStats();
                this.refreshStatsObjet(statsStr);
            } else {
                statsStr = this.parseFMStatsString(statsObjectFm, statsAdd, negative)
                        + ","
                        + statsObjectFm
                        + "#"
                        + Integer.toHexString(statsAdd)
                        + "#0#0#0d0+"
                        + statsAdd;
                this.clearStats();
                this.refreshStatsObjet(statsStr);
            }
        }
        this.setModification();
    }

    public String parseStringStatsEC_FM(GameObject obj, double poid, int carac, Player player) { // Ca c'est pas mal mais peut etre vori le jet perdu en soit
        String stats = "";
        boolean first = false;
        double perte = 0.0;

        // Degat d'arme on ne devrait pas s'en soucier
        for (Effect EH : obj.Effects) {
            if (first)
                stats += ",";

            try {
                stats += Integer.toHexString(EH.getEffectID()) + "#" + Integer.toHexString(EH.getArgs1())+ "#" + Integer.toHexString(EH.getArgs2()) + "#0#" + EH.getJet();
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            first = true;
        }

        java.util.Map<Integer, Integer> statsObj = new java.util.HashMap<Integer, Integer>(obj.Stats.getMap());
        java.util.ArrayList<Integer> keys = new ArrayList<Integer>(obj.Stats.getMap().keySet());
        Collections.shuffle(keys);
        int p = 0;
        int key = 0;

        // On trie les stats
        if (keys.size() > 1) {
            for (Integer i : keys) // On cherche un OverFM
            {
                if(i == 121){
                    i = 112;
                }

                if(i < 970 | i > 974) {
                    int value = statsObj.get(i);
                    if (this.isOverFm(i, value)) {
                        key = i;
                        //System.out.println(value + " On est over" + i);
                        break;
                    }
                    p++;
                }
            }

            if (key > 0) // On place l'OverFm en tï¿½te de liste pour ï¿½tre niquï¿½
            {
                keys.remove(p);
                keys.add(p, keys.get(0));
                keys.remove(0);
                keys.add(0, key);
            }
        }

        // On boucle mais faudrait géré la perte différemment genre aléatoire une fois avoir taper les OverFm/Exo
        for (Integer i : keys) {
            if(i == 121){
            	i = 112;
            }

            int newstats = 0;
            int statID = i;
            int value = statsObj.get(i);

            if (perte >= poid || statID == carac) {
                newstats = value;
            } else if ((statID == 152) || (statID == 154) || (statID == 155) // GESTION DES STATS NEGATIVE, faudrait les faire perdre plus dans l'idée
                    || (statID == 157) || (statID == 116) || (statID == 153)) {

                float a = (float) (value * poid / 100.0D);

                if (a < 1.0F)
                    a = 1.0F;

                float chute = value + a;
                newstats = (int) Math.floor(chute);

                if (newstats > JobAction.getBaseMaxJet(obj.getTemplate().getId(), Integer.toHexString(i)))
                    newstats = JobAction.getBaseMaxJet(obj.getTemplate().getId(), Integer.toHexString(i));

            } else {
                if ((statID == 127) || (statID == 101)) // On ignore les REM PA REM PM
                    continue;

                float chute;

                if (this.isOverFm(statID, value)){ // Gros kick dans la gueulle de l'over FM
                    chute = (float) (value - value
                            * (poid - (int) Math.floor(perte)) * 2 / 100.0D);
                }
                else{
                    chute = (float) (value - value
                            * (poid - (int) Math.floor(perte)) / 100.0D);
                }

                if ((chute / (float) value) < 0.75){
                    chute = ((float) value) * 0.75F; // On ne peut pas perdre plus de 25% d'une stat d'un coup
                }

                chute = (float) Math.floor(chute);
                int chute2 =(int)(value - chute);

                SocketManager.GAME_SEND_MESSAGE(player, "- "+chute2+" "+ JobAction.getEffetName(statID),Constant.COULEUR_ECHEC);
                double chutePwr = (value - chute)
                        * JobAction.getPwrPerEffet(statID);
                perte += chutePwr;
                newstats = (int) Math.floor(chute);
            }
            if (newstats < 1)
                continue;
            String jet = "0d0+" + newstats;
            if (first)
                stats += ",";
            stats += Integer.toHexString(statID) + "#"
                    + Integer.toHexString(newstats) + "#0#0#" + jet;
            first = true;
        }

        int puitdelaperte = (int) (perte - poid);
        if ( puitdelaperte < 0) {
            puitdelaperte = 0;
        }

        // On met le nouveau poid après la perte
        int puitobj = obj.getPuit();
        if(puitobj + puitdelaperte > 0){
            //SocketManager.GAME_SEND_MESSAGE(player, "+ "+(perte-poid)+" puit",Constant.COULEUR_SUCCES);
            obj.setPuit((int) (obj.getPuit() + (perte-poid)));
        }
        else{
            obj.setPuit(0);
        }

        // On laisse les txtStats
        for (Entry<Integer, String> entry : obj.txtStats.entrySet()) {
            if (first)
                stats += ",";
            if(entry.getKey() < 970 | entry.getKey() > 974) {
                if(entry.getKey() == Constant.APPARAT_ITEM || entry.getKey() == Constant.APPARAT_NAME)
                {
                    stats += Integer.toHexString(entry.getKey()) + "#0#0#" + entry.getValue();
                }
                else {
                    stats += Integer.toHexString((entry.getKey())) + "#0#0#0#"
                            + entry.getValue();
                }
            }
            else{
                stats += Integer.toHexString(entry.getKey()) + "#0#0#" + entry.getValue();
            }
            first = true;
        }

        return stats;
    }

    public boolean isOverFm(int stat, int val) {
        boolean trouve = false;
        String statsTemplate = "";
        statsTemplate = this.template.getStrTemplate();

        if (statsTemplate == null || statsTemplate.isEmpty())
            return false;
        String[] split = statsTemplate.split(",");
        for (String s : split) {
            String[] stats = s.split("#");

            int statID = Integer.parseInt(stats[0], 16);
            boolean PositiveStat = true;
            if(ArrayUtils.contains(EffectConstant.STATS_NEGATIVE,statID)){
                PositiveStat = false;
            }
            if(statID == 121){
                statID = 112;
            }

            if (statID != stat)
                continue;

            trouve = true;
            boolean sig = true;
            for (int a : Constant.ARMES_EFFECT_IDS)
                if (a == statID)
                    sig = false;
            if (!sig)
                continue;
            String jet = "";
            int value = 1;
            try {


                jet = stats[4];
                value = Formulas.getRandomJet(jet);

                if(rarity <= 3){
                    try {
                        int min = Integer.parseInt(stats[1], 16);
                        int max = Integer.parseInt(stats[2], 16);
                        value = min;
                        if (max != 0)
                            value = max;
                    } catch (Exception e) {
                        e.printStackTrace();
                        value = Formulas.getRandomJet(jet);
                    }
                }
                else{
                    try {
                        int min = Integer.parseInt(stats[1], 16);
                        int max = Integer.parseInt(stats[2], 16);

                        value = min;
                        if (max != 0)
                            value = Formulas.getRandomJetWithRarity(min,max,5,PositiveStat);
                    } catch (Exception e) {
                        e.printStackTrace();
                        value = Formulas.getRandomJet(jet);
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
            if (val > value){
                return true;
            }

        }
        return !trouve;
    }

    public void setRarity(int rarity) {
        this.rarity = rarity;
    }

    public int getRarity() {
        return rarity;
    }

    public static int getAleaRarity() {
        int rarity = 1;
        int seuil = 50;
        int chance = 0;

        for (int i = 0; i <= 4; i++) {
            chance = Formulas.getRandomValue(0, 100);
            if(chance>seuil) {
                rarity++;
            }
            else {
                break;
            }
        }

        if(rarity>5){
            rarity = 5;
        }
        return rarity;
    }

    public static int getRarityAlea(int difficulty) {
        int rarity = 1;
        switch (difficulty){
            case 0 :
                break;
            case 1:
                rarity = 2;
                break;
            case 2 :
                rarity = 3;
                break;
            case 3 :
            case 4 :
                rarity = 4;
                break;
            default:
                rarity = 1;
                break;
        }

        int seuil = 66;
        int chance = 0;

        for (int i = 0; i <= 4-difficulty; i++) {
            chance = Formulas.getRandomValue(0, 100);
            if(chance>seuil) {
                rarity++;
            }
            else {
                break;
            }
        }

        if(rarity>5){
            rarity = 5;
        }
        return rarity;
    }

    public static int getAleaRarity(long chanceimpact) {
        int rarity = 1;
        int seuil = 50;
        int chance = 0;

        float perc = (float)(chanceimpact*0.01);
        int seuilimpact = (int)Math.floor(seuil * perc);
        for (int i = 0; i <= 4; i++) {
            chance = Formulas.getRandomValue(0, 100);
            if(chance<seuilimpact) {
                rarity++;
            }
            else {
                break;
            }
        }

        if(rarity>5){
            rarity = 5;
        }
        return rarity;
    }

    public ArrayList<String> getSpellStats() {
        return SortStats;
    }

    public boolean isSameStats(GameObject other) {
        if(this.getEffects().size() == 0 && other.getEffects().size() == 0)
        {return true;}
        else if(this.getEffects().size() != other.getEffects().size()){
            return false;
        }
        else {
            ArrayList<Effect> test1 = new ArrayList<>();
            ArrayList<Effect> test2 = new ArrayList<>();
            test1.addAll(this.getEffects());
            test2.addAll(other.getEffects());
            Collections.sort(test1, Comparator.comparing(Effect::getEffectID));
            Collections.sort(test2, Comparator.comparing(Effect::getEffectID));

            for (int i = 0; i < test1.size(); i++ ){
                if( test1.get(i).getEffectID() != test2.get(i).getEffectID() ){
                    return false;
                }
                if( (test1.get(i).getArgs1() != test2.get(i).getArgs1())){
                        return false;
                }
                if( (test1.get(i).getArgs2() != test2.get(i).getArgs2())){
                    return false;
                }
                if( (test1.get(i).getArgs3() != test2.get(i).getArgs3())){
                    return false;
                }
                if((test1.get(i).getJet() != null) && test2.get(i).getJet() != null )
                    if( !(test1.get(i).getJet().equals(test2.get(i).getJet())))
                        return false;
            }
            return true;
        }
    }

    public boolean isSametxtStats(GameObject other) { //Compare uniquement les stats de l'item sans les degat de l'arme
        if(this.getTxtStat().size() == 0 && other.getTxtStat().size() == 0)
        {
            return true;
        }
        else {
            for (Entry<Integer, String> entry : this.getTxtStat().entrySet()) {
                //Si la stat n'existe pas dans l'autre map
                if (other.getTxtStat().get(entry.getKey()) == null)
                    return false;
                //Si la stat existe mais n'a pas la m�me valeur
                if (other.getTxtStat().get(entry.getKey()).compareTo(entry.getValue()) != 0)
                    return false;
            }
            for (Entry<Integer, String> entry : other.getTxtStat().entrySet()) {
                //Si la stat n'existe pas dans l'autre map
                if (this.getTxtStat().get(entry.getKey()) == null)
                    return false;
                //Si la stat existe mais n'a pas la m�me valeur
                if (this.getTxtStat().get(entry.getKey()).compareTo(entry.getValue()) != 0)
                    return false;
            }
            return true;
        }
    }

    public ArrayList<String> getSortStats() {
        return this.SortStats;
    }
}
