package fight;

import common.PathFinding;
import fight.spells.*;
import org.apache.commons.lang.ArrayUtils;
import area.map.GameCase;
import client.Player;
import client.other.Stats;
import common.Formulas;
import common.SocketManager;
import entity.Collector;
import entity.Prism;
import entity.monster.Monster;
import game.world.World;
import guild.Guild;
import kernel.Constant;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class Fighter implements Comparable<Fighter> {
    public int nbrInvoc;
    public boolean inLancer = false;
    public boolean isStatique = false;
    public boolean toRebuff =false;
    private int id = 0;
    private boolean canPlay = false;
    private Fight fight;
    private int type = 0;                                // 1 : Personnage, 2 : Mob, 5 : Perco
    private Monster.MobGrade mob = null;
    private Player perso = null;
    private Player _double = null;
    private Collector collector = null;
    private Prism prism = null;
    private int team = -2;
    private GameCase cell;
    private int pdvMax;
    private int pdv;
    private boolean isDead;
    private boolean hasLeft;
    private int gfxId;
    private Fighter isHolding;
    private Fighter holdedBy;
    private Fighter oldCible = null;
    private Fighter invocator;
    private boolean levelUp = false;
    private boolean isDeconnected = false;
    private int turnRemaining = 0;
    private int nbrDisconnection = 0;
    private boolean isTraqued = false;
    private Stats stats;
    private int curRemovedPa=0, curRemovedPm=0;

    private Map<Integer, Integer> state = new HashMap<Integer, Integer>();

    private ArrayList<Effect> fightBuffs = new ArrayList<Effect>();
    private ArrayList<BuffOnHitEffect> onHitBuffs = new ArrayList<BuffOnHitEffect>();

    private Map<Integer, Integer> chatiValue = new HashMap<Integer, Integer>();

    private ArrayList<LaunchedSpell> launchedSpell = new ArrayList<LaunchedSpell>();

    public World.Couple<Byte, Long> killedBy;
    public boolean isControllable = false;

    public Fighter(Fight f, Monster.MobGrade mob) {
        this.fight = f;
        this.type = 2;
        this.mob = mob;
        setId(mob.getInFightID());
        this.pdvMax = mob.getPdvMax();
        this.pdv = mob.getPdv();
        this.gfxId = getDefaultGfx();
    }

    public Fighter(Fight f, Player player) {
        this.fight = f;
        if (player._isClone) {
            this.type = 10;
            setDouble(player);
        } else {
            this.type = 1;
            this.perso = player;
        }
        setId(player.getId());
        this.pdvMax = player.getMaxPdv();
        this.pdv = player.getCurPdv();
        this.gfxId = getDefaultGfx();
    }

    public Fighter(Fight f, Collector collector) {
        this.fight = f;
        this.type = 5;
        setCollector(collector);
        setId(-1);
        this.pdvMax = (World.world.getGuild(collector.getGuildId()).getLvl() * 100);
        this.pdv = (World.world.getGuild(collector.getGuildId()).getLvl() * 100);
        this.gfxId = 6000;
    }

    public Fighter(Fight Fight, Prism Prisme) {
        this.fight = Fight;
        this.type = 7;
        setPrism(Prisme);
        setId(-1);
        this.pdvMax = Prisme.getLevel() * 10000;
        this.pdv = Prisme.getLevel() * 10000;
        this.gfxId = Prisme.getAlignement() == 1 ? 8101 : 8100;
        Prisme.refreshStats();
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean canPlay() {
        return this.canPlay;
    }

    public void setCurRemovedPa(int remove) {
        this.curRemovedPa = remove;
    }

    public void setCurRemovedPm(int remove) {
        this.curRemovedPm = remove;
    }

    public int getCurRemovedPa() {
        return this.curRemovedPa;
    }

    public int getCurRemovedPm() {
        return this.curRemovedPm;
    }

    public void setCanPlay(boolean canPlay) {
        this.canPlay = canPlay;
    }

    public Fight getFight() {
        return this.fight;
    }

    public int getType() {
        return this.type;
    }

    public Monster.MobGrade getMob() {
        if (this.type == 2)
            return this.mob;
        return null;
    }

    public boolean isMob() {
        return (this.mob != null);
    }

    public Player getPlayer() {
        if (this.type == 1)
            return this.perso;
        return null;
    }

    public Player getDouble() {
        return _double;
    }

    public boolean isDouble() {
        return (this._double != null);
    }

    public void setDouble(Player _double) {
        this._double = _double;
    }

    public Collector getCollector() {
        if (this.type == 5)
            return this.collector;
        return null;
    }

    public void setControllable(boolean state)
    {
        isControllable = state;
    }

    public boolean isControllable() {return isControllable;}

    public boolean isCollector() {
        return (this.collector != null);
    }

    public void setCollector(Collector collector) {
        this.collector = collector;
    }

    public Prism getPrism() {
        if (this.type == 7)
            return this.prism;
        return null;
    }

    public void setPrism(Prism prism) {
        this.prism = prism;
    }

    public boolean isPrisme() {
        return (this.prism != null);
    }

    public int getTeam() {
        return this.team;
    }


    public int getDistanceBetween(Fighter tocompare) {
       int distance = 0;
        distance = PathFinding.getDistanceBetween(fight.getMap(), this.getCell().getId(), tocompare.getCell().getId());
        return distance;
    }



    public void setTeam(int i) {
        this.team = i;
    }

    public int getTeam2() {
        return this.fight.getTeamId(getId());
    }

    public int getOtherTeam() {
        return this.fight.getOtherTeamId(getId());
    }

    public GameCase getCell() {
        return this.cell;
    }

    public void setCell(GameCase cell) {
        this.cell = cell;
    }

    public int getPdvMax() {
        return this.pdvMax + getBuffValue(EffectConstant.STATS_ADD_VITA);
    }

    public String getResiString() {
        String Neutre = this.stats.get(EffectConstant.STATS_ADD_RP_NEU)+"," ;
        String Terre = this.stats.get(EffectConstant.STATS_ADD_RP_TER)+",";
        String Feu =this.stats.get(EffectConstant.STATS_ADD_RP_FEU)+",";
        String Eau =this.stats.get(EffectConstant.STATS_ADD_RP_EAU)+",";
        String Agi =this.stats.get(EffectConstant.STATS_ADD_RP_AIR)+",";
        String EsquiPA =this.stats.get(EffectConstant.STATS_ADD_AFLEE)+",";
        String EsquiPM =this.stats.get(EffectConstant.STATS_ADD_MFLEE) +"";

        return Neutre+Terre+Feu+Eau+Agi+EsquiPA+EsquiPM;
    }


    public String getEsquivePM() {
        String Agi =0+"";
        return Agi;
    }

    public void removePdvMax(int pdv) {
        this.pdvMax = this.pdvMax - pdv;
        if (this.pdv > this.pdvMax)
            this.pdv = this.pdvMax;
    }

    public int getPdv() {
        return (this.pdv + getBuffValue(EffectConstant.STATS_ADD_VITA));
    }

    public void setPdvMax(int pdvMax) {
        this.pdvMax = pdvMax;
    }

    public void setPdv(int pdv) {
        this.pdv = pdv;
        if(this.pdv > this.pdvMax)
            this.pdv = this.pdvMax;
    }

    public void removePdv(Fighter caster, int pdv) {
        //pdv = Math.abs(pdv);
        if (pdv > 0)
            this.getFight().getAllChallenges().values().stream().filter(challenge -> challenge != null).forEach(challenge -> challenge.onFighterAttacked(caster, this));
        this.pdv -= pdv;
    }

    public void addPdv(Fighter caster, int pdv) {
        pdv = Math.abs(pdv);
        /*if (pdv < 0)
            this.getFight().getAllChallenges().values().stream().filter(challenge -> challenge != null).forEach(challenge -> challenge.onFighterAttacked(caster, this));*/
        this.pdv += pdv;
    }


    public boolean isMultiman() {
        if(this.getPlayer() != null)
            return this.getPlayer().isMultiman();
        return false;
    }

    public void fullPdv() {
        this.pdv = this.pdvMax;
    }

    public boolean isFullPdv() {
        return this.pdv == this.pdvMax;
    }

    public boolean isDead() {
        return this.isDead;
    }

    public void setIsDead(boolean isDead) {
        this.isDead = isDead;
    }

    public boolean hasLeft() {
        return this.hasLeft;
    }

    public void setLeft(boolean hasLeft) {
        this.hasLeft = hasLeft;
    }

    public Fighter getIsHolding() {
        return this.isHolding;
    }

    public void setIsHolding(Fighter isHolding) {
        this.isHolding = isHolding;
    }

    public Fighter getHoldedBy() {
        return this.holdedBy;
    }

    public void setHoldedBy(Fighter holdedBy) {
        this.holdedBy = holdedBy;
    }

    public Fighter getOldCible() {
        return this.oldCible;
    }

    public void setOldCible(Fighter cible) {
        this.oldCible = cible;
    }

    public Fighter getInvocator() {
        return this.invocator;
    }

    public void setInvocator(Fighter invocator) {
        this.invocator = invocator;
    }

    public boolean isInvocation() {
        return (this.invocator != null);
    }

    public boolean getLevelUp() {
        return this.levelUp;
    }

    public void setLevelUp(boolean levelUp) {
        this.levelUp = levelUp;
    }

    public void Disconnect() {
        if (this.isDeconnected)
            return;
        this.isDeconnected = true;
        this.turnRemaining = Constant.FIGHT_MAXIMAL_TURN_DECO;
        this.nbrDisconnection++;
    }

    public void Reconnect() {
        this.isDeconnected = false;
        this.turnRemaining = 0;
    }

    public boolean isDeconnected() {
        return !this.hasLeft && this.isDeconnected;
    }

    public int getTurnRemaining() {
        return this.turnRemaining;
    }

    public void setTurnRemaining() {
        this.turnRemaining--;
    }

    public int getNbrDisconnection() {
        return this.nbrDisconnection;
    }

    public boolean getTraqued() {
        return this.isTraqued;
    }

    public void setTraqued(boolean isTraqued) {
        this.isTraqued = isTraqued;
    }

    public void setState(int id, int t) {
        this.state.remove(id);
        if (t != 0) this.state.put(id, t);
    }

    public void setState(int id, int t, int casterId)
    {
        if(t!=0)
        {
            if(state.get(id)!=null) //fighter already has same state
            {
                if(state.get(id)==-1||state.get(id)>t) //infite duration state or current state lasts longer than parameter state
                    return;
                else //current state lasts shorter than parameter state, refresh state
                {
                    state.remove(id);
                    state.put(id,t);
                }
            }
            else //fighter does not have parameter state
            {
                state.put(id,t);
            }
        }
        else //t=0 removes state
        {
            this.state.remove(id);
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,950,new StringBuilder(String.valueOf(casterId)).toString(),String.valueOf(this.getId())+","+id+",0");
        }
    }

    public int getState(int id) {
        return this.state.get(id) != null ? this.state.get(id) : -1;
    }

    public boolean haveState(int id) {
        return this.state.get(id) != null && this.state.get(id) != 0;
    }

    public void sendState(Player p) {
        if (p.getAccount() != null && p.getGameClient() != null)
            for (Entry<Integer, Integer> state : this.state.entrySet())
                SocketManager.GAME_SEND_GA_PACKET(p.getGameClient(), 7 + "", 950 + "", getId() + "", getId() + "," + state.getKey() + ",1");
    }

    public boolean haveInvocation() {
        for (Entry<Integer, Fighter> entry : this.getFight().getTeam(this.getTeam2()).entrySet()) {
            Fighter f = entry.getValue();
            if (f.isInvocation())
                if (f.getInvocator() == this)
                    return true;
        }
        return false;
    }

    public int nbInvocation() {
        int i = 0;
        for (Entry<Integer, Fighter> entry : this.getFight().getTeam(this.getTeam2()).entrySet()) {
            Fighter f = entry.getValue();
            if (f.isInvocation() && !f.isStatique)
                if (f.getInvocator() == this)
                    i++;
        }
        return i;
    }

    public ArrayList<Effect> getFightBuff() {
        return this.fightBuffs;
    }

    public ArrayList<BuffOnHitEffect> getOnHitBuff() {
        return this.onHitBuffs;
    }

    private Stats getFightBuffStats() {
        Stats stats = new Stats();
        for (Effect entry : this.fightBuffs)
            stats.addOneStat(entry.getEffectID(), entry.getFixvalue());
        return stats;
    }

    public int getBuffValue(int id) {
        int value = 0;
        for (Effect entry : this.fightBuffs)
            if (entry.getEffectID() == id)
                value += entry.getFixvalue();
        return value;
    }

    public Effect getBuff(int id) {
        for (Effect entry : this.fightBuffs)
            if (entry.getEffectID() == id && entry.getDuration() > 0)
                return entry;
        return null;
    }


    public ArrayList<Effect> getBuffsByEffectID(int effectID) {
        ArrayList<Effect> buffs = new ArrayList<Effect>();
        buffs.addAll(this.fightBuffs.stream().filter(buff -> buff.getEffectID() == effectID).collect(Collectors.toList()));
        return buffs;
    }

    public Stats getTotalStatsLessBuff() {
        Stats stats = new Stats(new LinkedHashMap<>());
        if (this.type == 1)
            stats = this.perso.getTotalStats();
        if (this.type == 2)
            if (this.stats == null) {
                this.stats = this.mob.getStats();
                stats = this.mob.getStats();
            }

        if (this.type == 5)
            stats = new Stats(World.world.getGuild(getCollector().getGuildId()));
        if (this.type == 7)
            stats = getPrism().getStats();
        if (this.type == 10)
            stats = getDouble().getTotalStats();
        return stats;
    }

    public boolean hasBuff(int id) {
        for (Effect entry : this.fightBuffs)
            if (entry.getEffectID() == id && entry.getDuration() > 0)
                return true;
        return false;
    }


    public void buffMobByDiff(int fightDiff){
        int StateId = 100 + fightDiff;
        this.setState(StateId, -1);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this.getFight(), 7, 950, this.getId() + "", this.getId() + "," + StateId + ",1", StateId);
        this.generateBuffByDiff(fightDiff);
    }

    public void generateBuffByDiff(int fightDiff){
        if(fightDiff == 0)
            return;

        Monster.MobGrade MG5 = this.getMob().getTemplate().getGrade(5);
        Monster.MobGrade MG1 = this.getMob().getTemplate().getGrade(1);

        // On génére les HP
        int pdvMin = MG1.getPdv();
        int pdvMax = MG5.getPdv();
        int pdvmoyen = (pdvMax - pdvMin);
        if(pdvmoyen <= 0)
            pdvmoyen = 50*MG5.getLevel();

        // On génére les stats
        Map<Integer, Integer> newStats = new HashMap<Integer, Integer>();
        newStats.putAll(MG5.stats);
        newStats.forEach((key, value) -> {
            int  StatsMini = MG1.stats.get(key);
            int  StatsMaxi = value;

            if( (key >= EffectConstant.STATS_ADD_RP_TER && key <= EffectConstant.STATS_REM_RP_NEU) || key == EffectConstant.STATS_CREATURE) {

            }
            else {
                if (StatsMini == StatsMaxi) {
                    value = Math.round(StatsMaxi * 1.5f);
                } else {
                    value = ((StatsMaxi - StatsMini));
                }
                this.addBuffStats(key, value, Constant.SPELL_BOOSTBYDIFF, this);
                // System.out.println( "Stats "+ key + " : "+ value);
            }
        });

        // On génère les PA
        int paMin = MG1.getPa();
        int paMax = MG5.getPa();
        int pamoyen;
        if(paMax > paMin){
            pamoyen = (paMax - paMin);
        }
        else{
            pamoyen = 1;
        }

        // On génère les PM
        int pmMin = MG1.getPa();
        int pmMax = MG5.getPa();
        int pmmoyen;
        if(pmMax > pmMin){
            pmmoyen = (pmMax - pmMin);
        }
        else{
            pmmoyen = 1;
        }

        if(fightDiff == 1){
            this.addBuffStats(EffectConstant.STATS_ADD_DOMA,30*fightDiff,Constant.SPELL_BOOSTBYDIFF,this);
            this.addBuffStats(EffectConstant.STATS_ADD_VITA,pdvmoyen,Constant.SPELL_BOOSTBYDIFF,this);
        }
        else {
            this.addBuffStats(EffectConstant.STATS_ADD_DOMA,100*fightDiff,Constant.SPELL_BOOSTBYDIFF,this);
            this.addBuffStats(EffectConstant.STATS_ADD_VITA,pdvmoyen + (Math.abs(1600 - this.getLvl() )) * fightDiff,Constant.SPELL_BOOSTBYDIFF,this);
        }

        this.addBuffStats(EffectConstant.STATS_ADD_RP_FEU,5*fightDiff,Constant.SPELL_BOOSTBYDIFF,this);
        this.addBuffStats(EffectConstant.STATS_ADD_RP_AIR,5*fightDiff,Constant.SPELL_BOOSTBYDIFF,this);
        this.addBuffStats(EffectConstant.STATS_ADD_RP_EAU,5*fightDiff,Constant.SPELL_BOOSTBYDIFF,this);
        this.addBuffStats(EffectConstant.STATS_ADD_RP_TER,5*fightDiff,Constant.SPELL_BOOSTBYDIFF,this);
        this.addBuffStats(EffectConstant.STATS_ADD_RP_NEU,5*fightDiff,Constant.SPELL_BOOSTBYDIFF,this);
        if(!this.isInvocation()) {
            this.addBuffStats(EffectConstant.STATS_ADD_PM,pmmoyen +fightDiff-1,Constant.SPELL_BOOSTBYDIFF,this);
            this.addBuffStats(EffectConstant.STATS_ADD_PA, pamoyen + fightDiff - 1, Constant.SPELL_BOOSTBYDIFF, this);
        }
        this.addBuffStats(EffectConstant.STATS_ADD_PERDOM,10*fightDiff,Constant.SPELL_BOOSTBYDIFF,this);
        this.addBuffStats(EffectConstant.STATS_ADD_PO,fightDiff,Constant.SPELL_BOOSTBYDIFF,this);
        this.addBuffStats(EffectConstant.STATS_ADD_CC,4*fightDiff,Constant.SPELL_BOOSTBYDIFF,this);
        this.addBuffStats(EffectConstant.STATS_ADD_SOIN,fightDiff*20,Constant.SPELL_BOOSTBYDIFF,this);

    }

    public void addBuffStats(int id, int val, int spellID, Fighter caster){
        if(val > 0) {

            this.fightBuffs.add(new Effect(id, val, -1, -1,-1,-1, caster, spellID, false));
            SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight, 7, id, getId(), val, "", "", "", -1, spellID);
            switch (id){
                case EffectConstant.STATS_ADD_MFLEE:
                case EffectConstant.STATS_ADD_AFLEE:
                case EffectConstant.STATS_ADD_RP_EAU:
                case EffectConstant.STATS_ADD_RP_TER:
                case EffectConstant.STATS_ADD_RP_NEU:
                case EffectConstant.STATS_ADD_RP_FEU:
                case EffectConstant.STATS_ADD_RP_AIR:
                case EffectConstant.STATS_ADD_PM:
                case EffectConstant.STATS_ADD_PA:
                    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this.fight, 7, id, caster.getId() + "", caster.getId() + "," + val + "," + -1, id);
            }
        }
    }

    public void resentBuff(int id, int val, int duration, int turns, boolean debuff, int spellID, String args, Fighter caster, boolean addingTurnIfCanPlay) {
        switch(id) {
            case 6://Renvoie de sort
                SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight, 7, id, getId(), -1, val+"", "10", "", duration, spellID);
                break;

            case 79://Chance éca
                val = Integer.parseInt(args.split(";")[0]);
                String valMax = args.split(";")[1];
                String chance = args.split(";")[2];
                SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight, 7, id, getId(), val, valMax, chance, "", duration, spellID);
                break;

            case 606:
            case 607:
            case 608:
            case 609:
            case 611:
                // de X sur Y tours
                String jet = args.split(";")[5];
                int min = Formulas.getMinJet(jet);
                int max = Formulas.getMaxJet(jet);
                SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight, 7, id, getId(), min, "" + max, "" + max, "", duration,spellID);
                break;

            case 788://Fait apparaitre message le temps de buff sacri Chatiment de X sur Y tours
                val = Integer.parseInt(args.split(";")[1]);
                String valMax2 = args.split(";")[2];
                if(Integer.parseInt(args.split(";")[0]) == 108)
                    return;
                SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight, 7, id, getId(), val, ""+val, ""+valMax2, "", duration, spellID);

                break;
            case 98://Poison insidieux
            case 107://Mot d'épine (2à3), Contre(3)
            case 100://Flèche Empoisonnée, Tout ou rien
            case 108://Mot de Régénération, Tout ou rien
            case 165://Maîtrises
            case 781://MAX
            case 782://MIN
                val = Integer.parseInt(args.split(";")[0]);
                String valMax1 = args.split(";")[1];
                if(valMax1.compareTo("-1") == 0 || spellID == 82 || spellID == 94)
                    SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight, 7, id, getId(), val, "", "", "", duration, spellID);
                else if(valMax1.compareTo("-1") != 0)
                    SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight, 7, id, getId(), val, valMax1, "", "", duration, spellID);
                break;
            case Constant.SPELL_BOOSTBYDIFF:
                SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight, 7, id, getId(), val, "", "", "", -1, spellID);
                break;
            default:
                SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight, 7, id, getId(), val, "", "", "", duration, spellID);
        }
    }

    public void addBuffOnHit(int effectID,int args1,int args2,int args3,int chance,int turn, EffectTrigger onHitTrigger,Effect s, Fighter caster,int spellID,int impactedTarget){
        BuffOnHitEffect newBuffOnHit = new BuffOnHitEffect(effectID,turn,args1,args2,args3,chance,caster,spellID,s, onHitTrigger,impactedTarget);
        this.onHitBuffs.add(newBuffOnHit);
    }


    // On refait la fonction AddBuff
    public void addBuff(int effectID, int val, int duration, int args1, int args2, int args3, boolean isUnbuffable, int spellID, Fighter caster, boolean addingTurnIfCanPlay){
        // si c'est une invo statique ca bouge pas
        if(this.mob != null){
            if(ArrayUtils.contains(Constant.STATIC_INVOCATIONS,this.mob.getTemplate().getId()))
                return;
        }

        // On mettrai pas un truc , si c'est le tour du joueur on rajoute un tour (au lieu de addingTurnIfCanPlay)?

        // TODO : a supprimer - On force mais en vrai faut juste faire gaffe a l'appel
        switch(spellID) {
            case 431:
            case 433:
            case 437:
            case 443:
            case 441:
            case Constant.SPELL_BOOSTBYDIFF:
                isUnbuffable = false;
            break;
        }

        String sValMin = args1+"";
        String sValMax = args2+"";
        if(sValMax.equals("-1")){
            sValMax.equals("");
        }
        String svalInfo = args3+"";

        // Alors pourquoi on les gère différemment je sais pas encore (Je crois que c'est les dégats de début de tour donc pas encore de valeur)
        if(ArrayUtils.contains(Constant.BEGIN_TURN_BUFF,effectID)){
            SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight, 7, effectID, getId(), sValMin,  sValMax , duration,spellID, caster.getId());
        }
        else{
            switch (effectID) {
                case 6://Renvoie de sort
                    SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight, 7, effectID, getId(), -1, val + "", "10", "", duration, spellID);
                    break;
                case 79://Chance éca
                    val = args1;
                    String valMax = args2+"";
                    String chance = args3+"";
                    SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight, 7, effectID, getId(), val, valMax, chance, "", duration, spellID);
                    break;
                case 606:
                case 607:
                case 608:
                case 609:
                case 611:
                    // PERSONNE UTILISE CETTE MERDE : de X sur Y tours (bon appaemment 2/3 sorts de monstres)
                    int min = args1;
                    int max = args2;
                    SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight, 7, effectID, getId(), min, "" + max, "" + max, "", duration, spellID);
                    break;
                case 788://Fait apparaitre message le temps de buff sacri Chatiment de X sur Y tours
                    // Apparemment pas utile pour le chatiment vitalesque
                    if (args1 == 108)
                        return;
                    SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight, 7, effectID, id, args2,  sValMax,  svalInfo, "", duration, spellID);
                    break;
                case 283://Les truc de boost Spell
                case 284:
                case 285:
                case 286:
                case 287:
                case 288:
                case 289:
                case 290:
                case 291:
                case 292:
                case 293:
                case 294:
                    SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight, 7, effectID, id, sValMin, "", "" + args2, "", duration, spellID, caster.getId());
                    break;
                case 140://Pass tour
                case 131://Poison de PA
                case 107://Mot d'épine (2à3), Contre(3)
                case 108://Mot de Régénération, Tout ou rien
                case 165://Maîtrises
                case 781://MAX
                case 782://MIN
                    SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight, 7, effectID, id, args1, sValMax, "", "", duration, spellID);
                    break;
                case Constant.SPELL_BOOSTBYDIFF:
                    SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight, 7, effectID, id, val, "", "", "", -1, spellID);
                    break;
                case 950://MAX
                case 951://MIN
                case 265:
                    //SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight, 7, effectID, id, args1, sValMax, "", "", duration, spellID);
                    break;
                default:
                    if(val == 0 || val == -1)
                        SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight, 7, effectID, getId(), args1, "", "", "", duration, spellID);
                    else
                        SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight, 7, effectID, getId(), val, "", "", "", duration, spellID);
                    break;
            }
        }

        //Si c'est le jouer actif qui s'autoBuff, on ajoute 1 a la durée

        this.fightBuffs.add(new Effect(effectID,val,(addingTurnIfCanPlay && this.canPlay && duration!= -1 ?duration+1:duration),args1,args2,args3,caster,spellID,isUnbuffable));

    }

    public void debuff(int Spell) {
        Iterator<Effect> it = this.fightBuffs.iterator();
        while (it.hasNext()) {
            Effect spellEffect = it.next();
            if(spellEffect.getSpell() == Spell) {
                it.remove();
            }
        }
    }

    public void debuffState(int stateID) {
        Iterator<Effect> it = this.fightBuffs.iterator();
        while (it.hasNext()) {
            Effect spellEffect = it.next();
            if(spellEffect.getFixvalue() == stateID) {
                it.remove();
            }
        }
    }

    public void debuff() {
        Iterator<Effect> it = this.fightBuffs.iterator();

        if (this.isHide())
            this.unHide(-1);

        while (it.hasNext()) {
            Effect spellEffect = it.next();

            switch (spellEffect.getSpell()) {
                case 437:
                case 431:
                case 433:
                case 443:
                case 441://Châtiments
                case Constant.SPELL_BOOSTBYDIFF://SpellDiff
                case EffectConstant.STATS_SPELL_ADD_DOM://SpellDiff
                case EffectConstant.STATS_SPELL_ADD_BASE_DAMAGE://SpellDiff
                    continue;
                case 197://Puissance sylvestre
                case 52://Cupidité
                case 228://Etourderie mortelle (DC)
                    it.remove();
                    continue;
            }



            if (spellEffect.isUnbuffable()) {
                //On envoie les Packets si besoin
                switch (spellEffect.getEffectID()) {
                    case EffectConstant.STATS_ADD_PA:
                    case EffectConstant.STATS_ADD_PA2:
                        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this.fight, 7, 101, getId()
                                + "", getId() + ",-" + spellEffect.getFixvalue(), 101);
                        if(this.canPlay())
                            this.setCurPa(this.getFight(),-(spellEffect.getFixvalue()));
                        break;

                    case EffectConstant.STATS_ADD_PM:
                    case EffectConstant.STATS_ADD_PM2:
                        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this.fight, 7, 127, getId()
                                + "", getId() + ",-" + spellEffect.getFixvalue(), 127);
                        if(this.canPlay())
                            this.setCurPm(this.getFight(),-(spellEffect.getFixvalue()));
                        break;
                    case EffectConstant.STATS_REM_PM:
                    case EffectConstant.STATS_REM_PM2:
                        int value = 0;
                        if(this.getTotalStats().getEffect(EffectConstant.STATS_ADD_PM) < 0 )
                            value = this.getTotalStats().getEffect(EffectConstant.STATS_ADD_PM) + spellEffect.getFixvalue();
                        else{
                            value = spellEffect.getFixvalue();
                        }

                        if(value > 0) {
                            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this.fight, 7, 127, getId()
                                    + "", getId() + "," + value, 127);

                            if(this.canPlay())
                                this.setCurPm(this.getFight(),value);
                        }
                        break;
                    case EffectConstant.STATS_REM_PA:
                    case EffectConstant.STATS_REM_PA2:
                    case EffectConstant.STATS_REM_PA3:
                        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this.fight, 7, 101, getId()
                                + "", getId() + "," + spellEffect.getFixvalue(), 101);
                        if(this.canPlay())
                            this.setCurPa(this.getFight(),(spellEffect.getFixvalue()));
                        break;
                }
                it.remove();
            }
            else {
                continue;
            }

        }
        ArrayList<Effect> array = new ArrayList<>(this.fightBuffs);
        if (!array.isEmpty()) {
            // On renvoie les effets qui aurait disparu ?
            this.rebuff();
            //this.fightBuffs.clear();
            //array.stream().filter(spellEffect -> spellEffect != null).forEach(spellEffect -> this.addBuff(spellEffect.getEffectID(), spellEffect.getFixvalue(), spellEffect.getDuration(),spellEffect.getArgs1(),spellEffect.getArgs2(),spellEffect.getArgs3(), spellEffect.isUnbuffable(), spellEffect.getSpell(), this, true));
        }

        Iterator<BuffOnHitEffect> it2 = this.onHitBuffs.iterator();
        while (it2.hasNext()) {
            BuffOnHitEffect entry = it2.next();
            if (entry == null || entry.getCaster().isDead)
                continue;
            if (entry.getBuffOnHitConditions().getUnbuffable()) {
                it2.remove();
            }
        }

        if (this.perso != null && !this.hasLeft) // Envoie les stats au joueurs
            SocketManager.GAME_SEND_STATS_PACKET(this.perso);
    }

    public void rebuff() {
        ArrayList<Effect> array = new ArrayList<>(this.fightBuffs);
        if (!array.isEmpty()) {
            // On renvoie les effets qui aurait disparu ?
            this.fightBuffs.clear();
            array.stream().filter(spellEffect -> spellEffect != null).forEach(spellEffect -> this.addBuff(spellEffect.getEffectID(), spellEffect.getFixvalue(), spellEffect.getDuration(),spellEffect.getArgs1(),spellEffect.getArgs2(),spellEffect.getArgs3(), spellEffect.isUnbuffable(), spellEffect.getSpell(), this, true));
        }
    }

    public void refreshEndTurnBuff() {
        Iterator<Effect> it = this.fightBuffs.iterator();
        while (it.hasNext()) {
            Effect entry = it.next();
            if (entry == null || entry.getCaster().isDead)
                continue;
            if (entry.decrementDuration() == 0) {
                it.remove();
                switch (entry.getEffectID()) {
                    case 108:
                        if (entry.getSpell() == 441) {
                            //Baisse des pdvs max
                            this.pdvMax = (this.pdvMax - entry.getFixvalue());

                            //Baisse des pdvs actuel
                            int pdv = 0;
                            if (this.pdv - entry.getFixvalue() <= 0) {
                                pdv = 0;
                                this.fight.onFighterDie(this, this.holdedBy);
                                this.fight.verifIfTeamAllDead();
                            } else
                                pdv = (this.pdv - entry.getFixvalue());
                            this.pdv = pdv;
                        }
                        break;

                    case 150://Invisibilit�
                        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this.fight, 7, 150, this.getId() + "", getId() + ",0");
                        break;

                    case 950: // On retire l'état sur la duration est terminé
                        int id = entry.getFixvalue();
                        if (id == -1)
                            return;
                        setState(id, 0);
                        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this.fight, 7, 950, this.getId() + "", this.getId() + "," + id + ",0");
                        break;
                }
            }
        }

        Iterator<BuffOnHitEffect> it2 = this.onHitBuffs.iterator();
        while (it2.hasNext()) {
            BuffOnHitEffect entry = it2.next();
            if (entry == null || entry.getCaster().isDead)
                continue;
            if (entry.decrementDuration() <= 0) {
                it2.remove();
            }
        }

    }

    public void initBuffStats() {
        if (this.type == 1)
            this.fightBuffs.addAll(this.perso.get_buff().values().stream().collect(Collectors.toList()));
    }

    public void applyBeginningTurnBuff(Fight fight) {
        for (int effectID : Constant.BEGIN_TURN_BUFF) {
            ArrayList<Effect> buffs = new ArrayList<>(this.fightBuffs);
            buffs.stream().filter(entry -> entry.getEffectID() == effectID).forEach(entry -> entry.applyBeginingBuff(fight, this));
        }
    }

    public ArrayList<LaunchedSpell> getLaunchedSorts() {
        return this.launchedSpell;
    }

    public void refreshLaunchedSort() {
        ArrayList<LaunchedSpell> copie = new ArrayList<>(this.launchedSpell);

        int i = 0;
        for (LaunchedSpell S : copie) {
            S.actuCooldown();
            if (S.getCooldown() <= 0) {
                this.launchedSpell.remove(i);
                i--;
            }
            i++;
        }
    }

    public void addLaunchedSort(Fighter target, SpellGrade sort, Fighter fighter) {
        LaunchedSpell launched = new LaunchedSpell(target, sort, fighter);
        this.launchedSpell.add(launched);
    }

    public Stats getTotalStats() {
        Stats stats = new Stats(new LinkedHashMap<>());
        if (this.type == 1)
            stats = this.perso.getTotalStats();
        if (this.type == 2) {
            stats = this.mob.getStats();
        }
        if (this.type == 5)
            stats = new Stats(World.world.getGuild(getCollector().getGuildId()));
        if (this.type == 7)
            stats = this.getPrism().getStats();
        if (this.type == 10)
            stats = this.getDouble().getTotalStats();

        if(this.type != 1)
            stats = Stats.cumulStatFight(stats, getFightBuffStats());

        return stats;
    }

    public int getMaitriseDmg(int id) {
        int value = 0;
        for (Effect entry : this.fightBuffs)
            if (entry.getSpell() == id)
                value += entry.getFixvalue();
        return value;
    }

    public boolean getSpellValueBool(int id) {
        for (Effect entry : this.fightBuffs)
            if (entry.getSpell() == id)
                return true;
        return false;
    }

    public boolean testIfCC(int tauxCC) {
        if (tauxCC < 2)
            return false;

        if(this.hasBuff(EffectConstant.EFFECTID_MINIMUMDAMAGE))
            return false;

        int agi = getTotalStats().getEffect(EffectConstant.STATS_ADD_AGIL);
        if (agi < 0)
            agi = 0;
        tauxCC -= getTotalStats().getEffect(EffectConstant.STATS_ADD_CC);
        tauxCC = (int) ((tauxCC * 2.9901) / Math.log(agi + 12));//Influence de l'agi
        if (tauxCC < 2)
            tauxCC = 2;
        int jet = Formulas.getRandomValue(1, tauxCC);
        return (jet == tauxCC);
    }

    public boolean testIfCC(int porcCC, SpellGrade sSort) {
        Player perso = this.getPlayer();
        if (porcCC < 2)
            return false;

        if(this.hasBuff(EffectConstant.EFFECTID_MINIMUMDAMAGE))
            return false;

        int agi = getTotalStats().getEffect(EffectConstant.STATS_ADD_AGIL);
        if (agi < 0)
            agi = 0;
        porcCC -= getTotalStats().getEffect(EffectConstant.STATS_ADD_CC);

        int valueCC = porcCC;

        if (this.getType() == 1 && perso.getObjectsClassSpell().containsKey(sSort.getSpellID())) {
            int value = perso.getValueOfClassObject(sSort.getSpellID(), EffectConstant.STATS_SPELL_ADD_CRIT);
            valueCC -= value;
        }

        if(this.hasBuff(EffectConstant.STATS_SPELL_ADD_CRIT)){
            if(sSort.getSpellID() == this.getBuff(EffectConstant.STATS_SPELL_ADD_CRIT).getFixvalue()) {
                int value =  this.getBuff(EffectConstant.STATS_SPELL_ADD_CRIT).getArgs3();
                valueCC -= value;
            }
        }

        valueCC = (int) ((valueCC * 2.9901) / Math.log(agi + 12));
        if (valueCC < 2)
            valueCC = 2;
        int jet = Formulas.getRandomValue(1, valueCC);
        return (jet == valueCC);
    }

    public int getInitiative() {
        if (this.type == 1)
            return this.perso.getInitiative();
        if (this.type == 2)
            return this.mob.getInit();
        if (this.type == 5)
            return World.world.getGuild(getCollector().getGuildId()).getLvl();
        if (this.type == 7)
            return 0;
        if (this.type == 10)
            return getDouble().getInitiative();
        return 0;
    }

    public int getPa() {
        int PA = 0;

        switch (this.type) {
            case 1:
                PA = (getTotalStats().getEffect(EffectConstant.STATS_ADD_PA) - getCurRemovedPa());
                break;
            case 2:
                PA = (getTotalStats().getEffect(EffectConstant.STATS_ADD_PA) + this.mob.getPa()) - getCurRemovedPa();
                break;
            case 5:
                PA = getTotalStats().getEffect(EffectConstant.STATS_ADD_PA) + 6 - getCurRemovedPa();
                break;
            case 7:
                PA = getTotalStats().getEffect(EffectConstant.STATS_ADD_PA) + 6 - getCurRemovedPa();
                break;
            case 10:
                PA = getTotalStats().getEffect(EffectConstant.STATS_ADD_PA) - getCurRemovedPa();
                break;
        }
        if(PA < 0)
            PA = 0;

        return PA;
    }

    public int getPm() {
        int PM = 0;

        switch (this.type) {
            case 1: // personnage
            case 7: // prisme
            case 10: // clone
                PM = getTotalStats().getEffect(EffectConstant.STATS_ADD_PM) - getCurRemovedPm();
                break;
            case 2: // mob
                PM =getTotalStats().getEffect(EffectConstant.STATS_ADD_PM) + this.mob.getPm() - getCurRemovedPm();
                break;
            case 5: // perco
                PM = getTotalStats().getEffect(EffectConstant.STATS_ADD_PM) + 4 - getCurRemovedPm();
                break;
        }
        if(PM < 0)
            PM = 0;

        return PM;
    }

    public int getPros() {
        switch (this.type) {
            case 1: // personnage
                return (getTotalStats().getEffect(EffectConstant.STATS_ADD_PROS) + Math.round(getTotalStats().getEffect(EffectConstant.STATS_ADD_CHAN) / 10) + Math.round(getBuffValue(EffectConstant.STATS_ADD_CHAN) / 10));
            case 2: // mob
                if (this.isInvocation()) // Si c'est un coffre anim�, la chance est �gale � 1000*(1+lvlinvocateur/100)
                    return (getTotalStats().getEffect(EffectConstant.STATS_ADD_PROS) + (1000 * (1 + this.getInvocator().getLvl() / 100)) / 10);
                else
                    return (getTotalStats().getEffect(EffectConstant.STATS_ADD_PROS) + Math.round(getBuffValue(EffectConstant.STATS_ADD_CHAN) / 10));
        }
        return 0;
    }

    public int getCurPa(Fight fight) {
        return fight.getCurFighterPa();
    }

    public void setCurPa(Fight fight, int pa) {
        fight.setCurFighterPa(fight.getCurFighterPa() + pa);
    }

    public int getCurPm(Fight fight) {
        return fight.getCurFighterPm();
    }

    public void setCurPm(Fight fight, int pm) {
        fight.setCurFighterPm(fight.getCurFighterPm() + pm);
    }

    public boolean canLaunchSpell(int spellID) {
        return this.getPlayer().hasSpell(spellID) && LaunchedSpell.cooldownGood(this, spellID);
    }

    public void unHide(int spellid) {
       //on retire le buff invi
        if (spellid != -1)// -1 : CAC
        {
            switch (spellid) {
                case 66:
                case 71:
                case 181:
                case 196:
                case 200:
                case 219:
                    return;
            }
        }
        ArrayList<Effect> buffs = new ArrayList<Effect>();
        buffs.addAll(getFightBuff());
        for (Effect SE : buffs) {
            if (SE.getEffectID() == EffectConstant.EFFECTID_INVISIBLE) {
                getFightBuff().remove(SE);
                break;
            }
        }
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this.fight, 7, EffectConstant.EFFECTID_INVISIBLE, getId()+ "", getId() + ",0");
        //On actualise la position
        SocketManager.GAME_SEND_GIC_PACKET_TO_FIGHT(this.fight, 7, this);
    }

    public boolean isHide() {
        return hasBuff(EffectConstant.EFFECTID_INVISIBLE);
    }

    public int getPdvMaxOutFight() {
        if (this.perso != null)
            return this.perso.getMaxPdv();
        if (this.mob != null)
            return this.mob.getPdvMax();
        return 0;
    }

    public Map<Integer, Integer> getChatiValue() {
        return this.chatiValue;
    }

    public int getDefaultGfx() {
        if (this.perso != null)
            return this.perso.getGfxId();
        if (this.mob != null)
            return this.mob.getTemplate().getGfxId();
        return 0;
    }

    public int getLvl() {
        if (this.type == 1)
            return this.perso.getLevel();
        if (this.type == 2)
            return this.mob.getLevel();
        if (this.type == 5)
            return World.world.getGuild(getCollector().getGuildId()).getLvl();
        if (this.type == 7)
            return getPrism().getLevel();
        if (this.type == 10)
            return getDouble().getLevel();
        return 0;
    }


    public String xpString(String str) {
        if (this.perso != null) {
            int max = this.perso.getLevel() + 1;
            if (max > World.world.getExpLevelSize())
                max = World.world.getExpLevelSize();
            return World.world.getExpLevel(this.perso.getLevel()).perso + str
                    + this.perso.getExp() + str + World.world.getExpLevel(max).perso;
        }
        return "0" + str + "0" + str + "0";
    }

    public String getPacketsName() {
        if (this.type == 1)
            return this.perso.getName();
        if (this.type == 2)
            return this.mob.getTemplate().getId() + "";
        if (this.type == 5)
            return this.getCollector().getFullName();
        if (this.type == 7)
            return (getPrism().getAlignement() == 1 ? 1111 : 1112) + "";
        if (this.type == 10)
            return getDouble().getName();

        return "";
    }

    public String getGmPacket(char c, boolean withGm) {
        StringBuilder str = new StringBuilder();
        str.append(withGm ? "GM|" : "").append(c);
        str.append(getCell().getId()).append(";");
        str.append("1;0;");//1; = Orientation
        str.append(getId()).append(";");
        str.append(getPacketsName()).append(";");

        switch (this.type) {
            case 1://Perso
                str.append(this.perso.getClasse()).append(";");
                str.append(this.perso.getGfxId()).append("^").append(this.perso.get_size()).append(";");
                str.append(this.perso.getSexe()).append(";");
                str.append(this.perso.getLevel()).append(";");
                str.append(this.perso.get_align()).append(",");
                str.append("0").append(",");
                str.append((this.perso.is_showWings() ? this.perso.getGrade() : "0")).append(",");
                str.append(this.perso.getLevel() + this.perso.getId());
                if (this.perso.is_showWings() && this.perso.getDeshonor() > 0) {
                    str.append(",");
                    str.append(this.perso.getDeshonor() > 0 ? 1 : 0).append(';');
                } else {
                    str.append(";");
                }
                int color1 = this.perso.getColor1(),
                        color2 = this.perso.getColor2(),
                        color3 = this.perso.getColor3();
                if (this.perso.getObjetByPos(Constant.ITEM_POS_MALEDICTION) != null)
                    if (this.perso.getObjetByPos(Constant.ITEM_POS_MALEDICTION).getTemplate().getId() == 10838) {
                        color1 = 16342021;
                        color2 = 16342021;
                        color3 = 16342021;
                    }
                str.append((color1 == -1 ? "-1" : Integer.toHexString(color1))).append(";");
                str.append((color2 == -1 ? "-1" : Integer.toHexString(color2))).append(";");
                str.append((color3 == -1 ? "-1" : Integer.toHexString(color3))).append(";");
                str.append(this.perso.getGMStuffString()).append(";");
                str.append(getPdv()).append(";");
                str.append(getTotalStats().getEffect(EffectConstant.STATS_ADD_PA)).append(";");
                str.append(getTotalStats().getEffect(EffectConstant.STATS_ADD_PM)).append(";");
                str.append(getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_NEU)).append(";");
                str.append(getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_TER)).append(";");
                str.append(getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_FEU)).append(";");
                str.append(getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_EAU)).append(";");
                str.append(getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_AIR)).append(";");
                str.append(getTotalStats().getEffect(EffectConstant.STATS_ADD_AFLEE) ).append(";");
                str.append(getTotalStats().getEffect(EffectConstant.STATS_ADD_MFLEE) ).append(";");
                str.append(this.team).append(";");
                if (this.perso.isOnMount() && this.perso.getMount() != null)
                    str.append(this.perso.getMount().getStringColor(this.perso.parsecolortomount()));
                str.append(";");
                break;
            case 2://Mob
                str.append("-2;");
                str.append(this.mob.getTemplate().getGfxId()).append("^").append(this.mob.getSize()).append(";");
                str.append(this.mob.getGrade()).append(";");
                str.append(this.mob.getTemplate().getColors().replace(",", ";")).append(";");
                if (this.mob.getTemplate().getId()==534) // Pandawa Ivre
                    str.append("0,1C3C,1C40,0;");
                else if (this.mob.getTemplate().getId()==547) // Pandalette ivre
                    str.append("0,1C3C,1C40,0;");
                else if (this.mob.getTemplate().getId()==1213) // Mage Céleste
                    str.append("0,2BA,847,0;");
                else
                    str.append("0,0,0,0;");
                str.append(this.getPdvMax()).append(";");
                str.append(this.mob.getPa()).append(";");
                str.append(this.mob.getPm()).append(";");
                str.append(this.mob.getResiString()).append(";");
                str.append(this.team);
                break;
            case 5://Perco
                str.append("-6;");//Perco
                str.append("6000^100;");//GFXID^Size
                Guild G = World.world.getGuild(this.collector.getGuildId());
                str.append(G.getLvl()).append(";");
                str.append("1;");
                str.append("2;4;");
                str.append((int) Math.floor(G.getLvl() / 2)).append(";").append((int) Math.floor(G.getLvl() / 2)).append(";").append((int) Math.floor(G.getLvl() / 2)).append(";").append((int) Math.floor(G.getLvl() / 2)).append(";").append((int) Math.floor(G.getLvl() / 2)).append(";").append((int) Math.floor(G.getLvl() / 2)).append(";").append((int) Math.floor(G.getLvl() / 2)).append(";");//R�sistances
                str.append(this.team);
                break;
            case 7://Prisme
                str.append("-2;");
                str.append(getPrism().getAlignement() == 1 ? 8101 : 8100).append("^100;");
                str.append(getPrism().getLevel()).append(";");
                str.append("-1;-1;-1;");
                str.append("0,0,0,0;");
                str.append(this.getPdvMax()).append(";");
                str.append(getTotalStats().getEffect(EffectConstant.STATS_ADD_PA)).append(";");
                str.append(getTotalStats().getEffect(EffectConstant.STATS_ADD_PM)).append(";");
                str.append(getTotalStats().getEffect(214)).append(";");
                str.append(getTotalStats().getEffect(210)).append(";");
                str.append(getTotalStats().getEffect(213)).append(";");
                str.append(getTotalStats().getEffect(211)).append(";");
                str.append(getTotalStats().getEffect(212)).append(";");
                str.append(getTotalStats().getEffect(160)).append(";");
                str.append(getTotalStats().getEffect(161)).append(";");
                str.append(this.team);
                break;
            case 10://Double
                str.append(getDouble().getClasse()).append(";");
                str.append(getDouble().getGfxId()).append("^").append(getDouble().get_size()).append(";");
                str.append(getDouble().getSexe()).append(";");
                str.append(getDouble().getLevel()).append(";");
                str.append(getDouble().get_align()).append(",");
                str.append("1,");//FIXME
                str.append((getDouble().is_showWings() ? getDouble().getALvl() : "0")).append(",");
                str.append(getDouble().getId()).append(";");

                str.append((getDouble().getColor1() == -1 ? "-1" : Integer.toHexString(getDouble().getColor1()))).append(";");
                str.append((getDouble().getColor2() == -1 ? "-1" : Integer.toHexString(getDouble().getColor2()))).append(";");
                str.append((getDouble().getColor3() == -1 ? "-1" : Integer.toHexString(getDouble().getColor3()))).append(";");
                str.append(getDouble().getGMStuffString()).append(";");
                str.append(getPdv()).append(";");
                str.append(getTotalStats().getEffect(EffectConstant.STATS_ADD_PA)).append(";");
                str.append(getTotalStats().getEffect(EffectConstant.STATS_ADD_PM)).append(";");
                str.append(getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_NEU)).append(";");
                str.append(getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_TER)).append(";");
                str.append(getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_FEU)).append(";");
                str.append(getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_EAU)).append(";");
                str.append(getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_AIR)).append(";");
                str.append(getTotalStats().getEffect(EffectConstant.STATS_ADD_AFLEE)).append(";");
                str.append(getTotalStats().getEffect(EffectConstant.STATS_ADD_MFLEE)).append(";");
                str.append(this.team).append(";");
                if (getDouble().isOnMount() && getDouble().getMount() != null)
                    str.append(getDouble().getMount().getStringColor(getDouble().parsecolortomount()));
                str.append(";");
                break;
        }

        return str.toString();
    }

    @Override
    public int compareTo(Fighter t) {
        return ((this.getPros() > t.getPros() && !this.isInvocation()) ? 1 : 0);
    }

    public ArrayList<LaunchedSpell> getLaunchedSpell() {
        return this.launchedSpell;
    }

}