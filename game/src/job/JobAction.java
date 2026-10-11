package job;

import area.map.GameCase;
import area.map.entity.InteractiveObject;
import client.Player;
import common.Formulas;
import common.PathFinding;
import common.SocketManager;
import database.Database;
import entity.monster.Monster;
import fight.spells.Effect;
import fight.spells.EffectConstant;
import game.action.ExchangeAction;
import game.action.GameAction;
import game.world.World;
import game.world.World.Couple;
import job.maging.Rune;
import kernel.Config;
import kernel.Constant;
import kernel.Logging;
import object.GameObject;
import object.ObjectTemplate;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

@SuppressWarnings("unused")
public class JobAction {

    public Map<Long, Integer> ingredients = new TreeMap<>();
    public Map<Long, Integer> lastCraft = new TreeMap<>();
    public Player player;
    public String data = "";
    public boolean broke = false;
    public boolean broken = false;
    public boolean isRepeat = false;
    public boolean success = true;
    private int id;
    private int min = 1;
    private int max = 1;
    private boolean isCraft;
    private int craftQuantity = 1;
    private int chan = 100;
    private int time = 0;
    private int xpWin = 0;
    private ArrayList<Integer> lastCaftID = new ArrayList<Integer>();
    private JobStat SM;
    private JobCraft jobCraft;
    public JobCraft oldJobCraft;


    public JobAction(int sk, int min, int max, boolean craft, int arg, int xpWin) {
        this.min = 1;
        this.max = 1;
        this.chan = 100;
        this.time = 0;
        this.xpWin = 0;
        this.ingredients = new TreeMap<Long, Integer>();
        this.lastCraft = new TreeMap<Long, Integer>();
        this.lastCaftID = new ArrayList<Integer>();
        this.data = "";
        this.broke = false;
        this.broken = false;
        this.success = true;
        this.reConfigingRunes = -1;
        this.isRepeat = false;
        this.id = sk;
        this.min = min;
        this.max = max;
        this.isCraft = craft;

        if (craft) this.chan = arg;
        else this.time = Math.round((arg)/ 2) ;
        this.xpWin = xpWin;
    }

    public int getId() {
        return this.id;
    }

    public int getMin() {
        return this.min;
    }

    public int getMax() {
        return this.max;
    }

    public boolean isCraft() {
        return this.isCraft;
    }

    public int getChance() {
        return this.chan;
    }

    public int getTime() {
        return this.time;
    }

    public void setCraftQuantity(int quantity) {
        this.craftQuantity = quantity;
    }

    public int getXpWin() {
        return this.xpWin;
    }

    public JobStat getJobStat() {
        return this.SM;
    }

    public JobCraft getJobCraft() {
        return this.jobCraft;
    }

    public void setJobCraft(JobCraft jobCraft) {
        this.jobCraft = jobCraft;
    }

    public void startCraft(Player P) {
        this.jobCraft = new JobCraft(this, P);
    }

    public void startAction(Player P, InteractiveObject IO, GameAction GA, GameCase cell, JobStat SM) {
        this.SM = SM;
        this.player = P;

        if (P.getObjetByPos(Constant.ITEM_POS_ARME) != null && SM.getTemplate().getId() == 36) {
            if (World.world.getMetier(36).isValidTool(P.getObjetByPos(Constant.ITEM_POS_ARME).getTemplate().getId())) {
                int dist = PathFinding.getDistanceBetween(P.getCurMap(), P.getCurCell().getId(), cell.getId());
                int distItem = JobConstant.getDistCanne(P.getObjetByPos(Constant.ITEM_POS_ARME).getTemplate().getId());
                if (distItem < dist) {
                    SocketManager.GAME_SEND_MESSAGE(P, "Vous êtes trop loin pour pouvoir pécher ce poisson !");
                    SocketManager.GAME_SEND_GA_PACKET(P.getGameClient(), "", "0", "", "");
                    P.setCurJobAction(null);
                    P.setExchangeAction(null);
                    P.setDoAction(false);
                    return;
                }
            }
        }
        if (!this.isCraft) {
            P.getGameClient().action = System.currentTimeMillis();
            P.setCurJobAction(this);
            IO.setInteractive(false);
            IO.setState(2);
            SocketManager.GAME_SEND_GA_PACKET_TO_MAP(P.getCurMap(), "" + GA.id, 501, P.getId() + "", cell.getId() + "," + this.time);
            SocketManager.GAME_SEND_GDF_PACKET_TO_MAP(P.getCurMap(), cell);
        } else {
            P.setAway(true);
            IO.setState(2);
            P.setCurJobAction(this);
            P.setExchangeAction(new ExchangeAction<>(ExchangeAction.CRAFTING, this));
            SocketManager.GAME_SEND_ECK_PACKET(P, 3, this.min + ";" + this.id);
            SocketManager.GAME_SEND_GDF_PACKET_TO_MAP(P.getCurMap(), cell);
        }
    }

    public void startAction(Player P, InteractiveObject IO, GameAction GA, GameCase cell) {
        this.player = P;
        P.setAway(true);
        IO.setState(2);//FIXME trouver la bonne valeur
        P.setCurJobAction(this);
        P.setExchangeAction(new ExchangeAction<>(ExchangeAction.CRAFTING, this));
        SocketManager.GAME_SEND_ECK_PACKET(P, 3, this.min + ";" + this.id);//this.min => Nbr de Case de l'interface
        SocketManager.GAME_SEND_GDF_PACKET_TO_MAP(P.getCurMap(), cell);
    }

    public void endAction(Player player, InteractiveObject IO, GameAction GA, GameCase cell) {
        if(!this.isCraft && player.getGameClient().action != 0) {
            if(System.currentTimeMillis() - player.getGameClient().action < this.time - 500) {
                player.getGameClient().kick();//FIXME: Ajouté le ban si aucune plainte.
                return;
            }
        }

        player.setDoAction(false);
        if (IO == null)
            return;
        if (!this.isCraft) {
            IO.setState(3);
            IO.desactive();
            SocketManager.GAME_SEND_GDF_PACKET_TO_MAP(player.getCurMap(), cell);
            //int qua = (this.max > this.min ? Formulas.getRandomValue(this.min, this.max) : this.min); PETIT PLUS
            int qua = Math.round((this.max > this.min ? Formulas.getRandomValue(this.min, this.max) : this.min) * (int)(Config.INSTANCE.getRATE_JOB()/3)) ;

            if (SM.getTemplate().getId() == 36) {
                if (qua > 0)
                    SM.addXp(player, (long) (this.getXpWin() * Config.INSTANCE.getRATE_JOB() * World.world.getConquestBonusNew(player)));
            } else
                SM.addXp(player, (long) (this.getXpWin() * Config.INSTANCE.getRATE_JOB()   * World.world.getConquestBonusNew(player)));

            int tID = JobConstant.getObjectByJobSkill(this.id);

            if (SM.getTemplate().getId() == 36 && qua > 0) {
                if (Formulas.getRandomValue(1, 1000) <= 2) {
                    int _tID = JobConstant.getPoissonRare(tID);
                    if (_tID != -1) {
                        ObjectTemplate _T = World.world.getObjTemplate(_tID);
                        if (_T != null) {
                            GameObject _O = _T.createNewItem(qua, false,0);
                            if (player.addObjet(_O, true))
                                World.world.addGameObject(_O, true);
                        }
                    }
                }
            }


            ObjectTemplate T = World.world.getObjTemplate(tID);
            if (T == null)
                return;
            GameObject O = T.createNewItem(qua, false,0);

            if (player.addObjet(O, true))
                World.world.addGameObject(O, true);
            SocketManager.GAME_SEND_IQ_PACKET(player, player.getId(), qua);
            SocketManager.GAME_SEND_Ow_PACKET(player);

            if (player.getMetierBySkill(this.id).get_lvl() >= 30 && Formulas.getRandomValue(1, 15) > 14) {
                for (int[] protector : JobConstant.JOB_PROTECTORS) {
                    if (tID == protector[1]) {
                        int monsterLvl = JobConstant.getProtectorLvl(player.getLevel());
                        player.getCurMap().startFightVersusProtectors(player, new Monster.MobGroup(player.getCurMap().nextObjectId, cell.getId(), protector[0] + "," + monsterLvl + "," + monsterLvl));
                        break;
                    }
                }
            }
        }
        player.setAway(false);
    }


    public boolean isMagging(){
        return (this.id == 1 || this.id == 113 || this.id == 115 || this.id == 116 || this.id == 117 || this.id == 118 || this.id == 119 || this.id == 120 || (this.id >= 163 && this.id <= 169));
    }

    public synchronized void craft(boolean isRepeat, int repeat) {
        if (!this.isCraft) return;


        if (this.id == 1 || this.id == 113 || this.id == 115 || this.id == 116 || this.id == 117 || this.id == 118 || this.id == 119 || this.id == 120 || (this.id >= 163 && this.id <= 169)) {
            this.craftMaging(isRepeat, repeat);
            boolean isMassiveCraft = false;
            return;
        }
        boolean isMassiveCraft = false;
        if(repeat == -1){
            repeat = this.craftQuantity;
        }
        boolean craftLoop = !isRepeat && repeat > 1;
        Map<Integer, Integer> items = new HashMap<>();
        //on ajoutes les ingrédient a une liste
        for (Entry<Long, Integer> e : this.ingredients.entrySet()) {
                if (!this.player.hasItemGuid(e.getKey())) {
                    this.cancelCraft();
                    return;
                }
                    GameObject obj = World.world.getGameObject(e.getKey());
                    items.put(obj.getTemplate().getId(), e.getValue());
        }
        boolean signed = false;

        if (items.containsKey(7508)) {
            signed = true;
            items.remove(7508);
        }

        SocketManager.GAME_SEND_Ow_PACKET(this.player);

        boolean isUnjobSkill = this.getJobStat() == null;
        if (!isUnjobSkill) {
            JobStat SM = this.player.getMetierBySkill(this.id);
            int templateId = World.world.getObjectByIngredientForJob(SM.getTemplate().getListBySkill(this.id), items);
            //Recette non existante ou pas adaptï¿½ au mï¿½tier
            if (templateId == -1 || !SM.getTemplate().canCraft(this.id, templateId)) {
                this.lastCaftID.add(templateId);
                this.cancelCraft();
                SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "-");
                return;
            }
            boolean canlaunch = true;
            // on les supprime que si c'est bon pour tous
            for (Entry<Long, Integer> e : this.ingredients.entrySet()) {
                    GameObject obj = World.world.getGameObject(e.getKey());
                    if (obj == null) {
                        this.cancelCraft();
                        return;
                    }
                    if (obj.getQuantity() < e.getValue()*repeat) {
                        this.player.sendMessage("Il te manque "+Math.abs(obj.getQuantity() - e.getValue()*repeat) + "*"+ obj.getTemplate().getName() + " pour fabriquer " + repeat + "*"+ World.world.getObjTemplate(templateId).getName() );
                        this.success = false;
                        this.cancelCraft();
                        return;
                    }
            }

            // C'est bon pour tous
            for (Entry<Long, Integer> e : this.ingredients.entrySet()) {
                GameObject obj = World.world.getGameObject(e.getKey());
                if (obj == null) {
                    SocketManager.GAME_SEND_Ec_PACKET(this.player, "EI");
                    return;
                }
                if (obj.getQuantity() < e.getValue()*repeat) {
                    SocketManager.GAME_SEND_Ec_PACKET(this.player, "EI");
                    return;
                }

                int newQua = obj.getQuantity() - e.getValue()*repeat;
                if (newQua < 0) return;

                if (newQua == 0) {
                    this.player.removeItem(e.getKey());
                    World.world.removeGameObject(e.getKey());
                    SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this.player, e.getKey());
                } else {
                    obj.setQuantity(newQua);
                    SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, obj);
                }

            }

            int chan = JobConstant.getChanceByNbrCaseByLvl(SM.get_lvl(), this.ingredients.size());
            int chan2 = JobConstant.getChanceByNbrCaseByLvlnormal(SM.get_lvl(), this.ingredients.size());

                if(repeat > 1){
                    isMassiveCraft = true;
                }

                if (craftLoop) SocketManager.GAME_SEND_Ed_PACKET(this.player, repeat);
                int failed = 0;
                for(int j=1;j<=repeat;j++){
                    boolean success = this.id == 109 || chan >= Formulas.getRandomValue(1, 100);
                    if (!success) {
                        failed++;
                    } else {

                        GameObject newObj = World.world.getObjTemplate(templateId).createNewItemWithoutDuplicationForJobs(this.player.getItems().values(), 1, false, chan2);
                        long guid = newObj.getGuid();//FIXME: Ne pas recrée un item pour l'empiler après

                        if (guid == -1) { // Don't exist
                            guid = newObj.setId();
                            this.player.getItems().put(guid, newObj);
                           SocketManager.GAME_SEND_OAKO_PACKET(this.player, newObj);
                            World.world.addGameObject(newObj, true);
                        } else {
                            newObj.setQuantity(newObj.getQuantity() + 1);
                            SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, newObj);
                        }
                        SocketManager.GAME_SEND_Ow_PACKET(this.player);

                        if (signed) newObj.addTxtStat(Constant.STATS_SIGNATURE, this.player.getName());


                        SocketManager.GAME_SEND_Em_PACKET(this.player, "KO+" + guid + "|1|" + templateId + "|" + newObj.parseStatsString().replace(";", "#"));

                    }

                    int winXP = 0;
                    if (success)
                        winXP = Formulas.calculXpWinCraft(SM.get_lvl(), this.ingredients.size()) * Config.INSTANCE.getRATE_JOB();
                    else if (!SM.getTemplate().isMaging())
                        winXP = Formulas.calculXpWinCraft(SM.get_lvl(), this.ingredients.size()) * Config.INSTANCE.getRATE_JOB();


                    if (winXP > 0) {
                        SM.addXp(this.player, winXP);
                        ArrayList<JobStat> SMs = new ArrayList<>();
                        SMs.add(SM);
                        SocketManager.GAME_SEND_JX_PACKET(this.player, SMs);
                    }
                }

                if (Logging.USE_LOG)
                    Logging.getInstance().write("Craft", this.player.getName() + " à crafter l'item " + templateId + " (" + World.world.getObjTemplate(templateId).getName() + ") " + (repeat - failed) + "/" + repeat + " fois");

                if (craftLoop) this.endCraftLoop(repeat, failed);

                if (failed == repeat) {
                    SocketManager.GAME_SEND_Ec_PACKET(this.player, "EF");
                    SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "-" + templateId);
                    SocketManager.GAME_SEND_Im_PACKET(this.player, "0118");
                } else {
                    SocketManager.GAME_SEND_Ec_PACKET(this.player, "K;" + templateId);
                    SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "+" + templateId);
                }

        } else {

            int templateId = World.world.getObjectByIngredientForJob(World.world.getMetier(this.id).getListBySkill(this.id), items);

            if (templateId == -1 || !World.world.getMetier(this.id).canCraft(this.id, templateId)) {
                    this.cancelCraft();
                    SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "-");
                    return;
            }

                boolean canlaunch = true;
                // on les supprime que si c'est bon pour tous
                for (Entry<Long, Integer> e : this.ingredients.entrySet()) {

                    GameObject obj = World.world.getGameObject(e.getKey());
                    if (obj == null) {
                        this.cancelCraft();
                        return;
                    }
                    if (obj.getQuantity() < e.getValue()*repeat) {
                        this.player.sendMessage("Il te manque "+Math.abs(obj.getQuantity() - e.getValue()*repeat) + "*"+ obj.getTemplate().getName() + " pour fabriquer " + repeat + "*"+ World.world.getObjTemplate(templateId).getName() );
                        this.success = false;
                        this.cancelCraft();
                        return;
                    }
                }

                // C'est bon pour tous
                for (Entry<Long, Integer> e : this.ingredients.entrySet()) {

                    GameObject obj = World.world.getGameObject(e.getKey());
                    if (obj == null) {
                        SocketManager.GAME_SEND_Ec_PACKET(this.player, "EI");
                        return;
                    }
                    if (obj.getQuantity() < e.getValue()*repeat) {
                        SocketManager.GAME_SEND_Ec_PACKET(this.player, "EI");
                        return;
                    }

                    int newQua = obj.getQuantity() - e.getValue()*repeat;
                    if (newQua < 0) return;

                    if (newQua == 0) {
                        this.player.removeItem(e.getKey());
                        World.world.removeGameObject(e.getKey());
                        SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this.player, e.getKey());
                    } else {
                        obj.setQuantity(newQua);
                        SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, obj);
                    }

                }


            if(repeat > 1){
                isMassiveCraft = true;
            }
            if (Logging.USE_LOG)
                Logging.getInstance().write("Craft", this.player.getName() + " à crafter avec " + (success ? "SUCCES" : "ECHEC") + " l'item " + templateId + " (" + World.world.getObjTemplate(templateId).getName() + ") " + repeat + "fois");

            if (craftLoop) SocketManager.GAME_SEND_Ed_PACKET(this.player, repeat);
            for(int j=1;j<=repeat;j++) {
                GameObject newObj = null;

                    newObj = World.world.getObjTemplate(templateId).createNewItemWithoutDuplication(this.player.getItems().values(), 1, false);
                    if (newObj != null) {
                        if (this.player.addObjet(newObj, true))
                            World.world.addGameObject(newObj, true);
                    }
                if (ArrayUtils.contains(Constant.FILTER_EQUIPEMENT, World.world.getObjTemplate(templateId).getType())) {
                    if (signed) newObj.addTxtStat(Constant.STATS_SIGNATURE, this.player.getName());
                }


                long guid = newObj.getGuid();
                SocketManager.GAME_SEND_Em_PACKET(this.player, "KO+" + guid + "|1|" + templateId + "|" + newObj.parseStatsString().replace(";", "#"));
            }

                SocketManager.GAME_SEND_Ow_PACKET(this.player);
                if (craftLoop) this.endCraftLoop(repeat, 0);
                SocketManager.GAME_SEND_Ec_PACKET(this.player, "K;" + templateId);
                SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "+" + templateId);
            /*}
            else{
                if(repeat > 1){
                    isMassiveCraft = true;
                }

                for (Entry<Integer, Integer> e : this.ingredients.entrySet()) {
                    GameObject obj = World.world.getGameObject(e.getKey());
                    if (obj == null) {
                        SocketManager.GAME_SEND_Ec_PACKET(this.player, "EI");
                        return;
                    }
                    if (obj.getQuantity() < e.getValue()*repeat) {
                        SocketManager.GAME_SEND_Ec_PACKET(this.player, "EI");
                        return;
                    }

                    int newQua = obj.getQuantity() - e.getValue()*repeat;
                    if (newQua < 0) return;

                    if (newQua == 0) {
                        this.player.removeItem(e.getKey());
                        World.world.removeGameObject(e.getKey());
                        SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this.player, e.getKey());
                    } else {
                        obj.setQuantity(newQua);
                        SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, obj);
                    }
                }


                GameObject newObj = World.world.getObjTemplate(templateId).createNewItemWithoutDuplication(this.player.getItems().values(), repeat, false);
                int guid = newObj.getGuid();//FIXME: Ne pas recrée un item pour l'empiler après

                if (guid == -1) { // Don't exist
                    guid = newObj.setId();
                    this.player.getItems().put(guid, newObj);
                    SocketManager.GAME_SEND_OAKO_PACKET(this.player, newObj);
                    World.world.addGameObject(newObj, true);
                } else {
                    newObj.setQuantity(newObj.getQuantity() + repeat);
                    SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, newObj);
                }

                if (signed) newObj.addTxtStat(Constant.STATS_SIGNATURE, this.player.getName());

                SocketManager.GAME_SEND_Ow_PACKET(this.player);
                SocketManager.GAME_SEND_Em_PACKET(this.player, "KO+" + guid + "|1|" + templateId + "|" + newObj.parseStatsString().replace(";", "#"));
                SocketManager.GAME_SEND_Ec_PACKET(this.player, "K;" + templateId);
                SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "+" + templateId);

            }*/
        }


        this.lastCraft.clear();
        this.lastCraft.putAll(this.ingredients);
        this.ingredients.clear();

        if(!isRepeat || isMassiveCraft) {
            this.oldJobCraft = this.jobCraft;
            this.jobCraft = null;
        }
    }

    private void cancelCraft() {
        for (Long id : this.ingredients.keySet())
            if (this.player.hasItemGuid(id))
                SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.player, 'O', "-", id + "");
        this.ingredients.clear();
        SocketManager.GAME_SEND_Ec_PACKET(this.player, "EI");
    }

    private void endCraftLoop(int repeat, int failed) {
        SocketManager.GAME_SEND_EA_PACKET(this.player, repeat + ";" + failed + ";" + repeat + ";" + repeat);
        SocketManager.GAME_SEND_Ea_PACKET(this.player, "1");
        this.craftQuantity = 1;
    }

    private long addCraftObject(Player player, GameObject newObj) {
        for (Entry<Long, GameObject> entry : player.getItems().entrySet()) {
            GameObject obj = entry.getValue();
            if (obj.getTemplate().getId() == newObj.getTemplate().getId() && obj.getTxtStat().equals(newObj.getTxtStat())
                    && obj.getStats().isSameStats(newObj.getStats()) && obj.isSameStats(newObj) && obj.isSametxtStats(newObj) && obj.getRarity() == newObj.getRarity() && obj.getMimibiote() == newObj.getMimibiote() && obj.getPosition() == Constant.ITEM_POS_NO_EQUIPED) {
                obj.setQuantity(obj.getQuantity() + newObj.getQuantity());//On ajoute QUA item a la quantitï¿½ de l'objet existant
                SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(player, obj);
                return obj.getGuid();
            }
        }

        player.getItems().put(newObj.getGuid(), newObj);
        SocketManager.GAME_SEND_OAKO_PACKET(player, newObj);
        World.world.addGameObject(newObj, true);
        return -1;
    }

    public boolean craftPublicMode(Player crafter, Player receiver, Map<Player, ArrayList<Couple<Long, Integer>>> list) {
        if (!this.isCraft) return false;

        this.player = crafter;
        boolean signed = false;

        Map<Integer, Integer> items = new HashMap<>();

        for (Entry<Player, ArrayList<Couple<Long, Integer>>> entry : list.entrySet()) {
            Player player = entry.getKey();
            Map<Long, Integer> playerItems = new HashMap<>();

            for (Couple<Long, Integer> couple : entry.getValue())
                playerItems.put(couple.first, couple.second);

            for (Entry<Long, Integer> e : playerItems.entrySet()) {
                if (!player.hasItemGuid(e.getKey())) {
                    SocketManager.GAME_SEND_Ec_PACKET(player, "EI");
                    SocketManager.GAME_SEND_Ec_PACKET(this.player, "EI");
                    return false;
                }

                GameObject gameObject = World.world.getGameObject(e.getKey());
                if (gameObject == null) {
                    SocketManager.GAME_SEND_Ec_PACKET(player, "EI");
                    SocketManager.GAME_SEND_Ec_PACKET(this.player, "EI");
                    return false;
                }
                if (gameObject.getQuantity() < e.getValue()) {
                    SocketManager.GAME_SEND_Ec_PACKET(player, "EI");
                    SocketManager.GAME_SEND_Ec_PACKET(this.player, "EI");
                    return false;
                }

                int newQua = gameObject.getQuantity() - e.getValue();

                if (newQua < 0)
                    return false;

                if (newQua == 0) {
                    player.removeItem(e.getKey());
                    World.world.removeGameObject(e.getKey());
                    SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(player, e.getKey());
                } else {
                    gameObject.setQuantity(newQua);
                    SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(player, gameObject);
                }

                items.put(gameObject.getTemplate().getId(), e.getValue());
            }
        }

        SocketManager.GAME_SEND_Ow_PACKET(this.player);
        JobStat SM = this.player.getMetierBySkill(this.id);

        //Rune de signature
        if (items.containsKey(7508))
            if (SM.get_lvl() == 100)
                signed = true;
        items.remove(7508);

        int template = World.world.getObjectByIngredientForJob(SM.getTemplate().getListBySkill(this.id), items);

        if (template == -1 || !SM.getTemplate().canCraft(this.id, template)) {
            SocketManager.GAME_SEND_Ec_PACKET(this.player, "EI");
            receiver.send("EcEI");
            SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "-");
            items.clear();
            return false;
        }

        boolean success = JobConstant.getChanceByNbrCaseByLvl(SM.get_lvl(), items.size()) >= Formulas.getRandomValue(1, 100);

        //if (Logging.USE_LOG)
            //Logging.getInstance().write("SecureCraft", this.player.getName() + " Ã  crafter avec " + (success ? "SUCCES" : "ECHEC") + " l'item " + template + " (" + World.world.getObjTemplate(template).getName() + ") pour " + receiver.getName());
        if (!success) {
            SocketManager.GAME_SEND_Ec_PACKET(this.player, "EF");
            SocketManager.GAME_SEND_Ec_PACKET(receiver, "EF");
            SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "-" + template);
            SocketManager.GAME_SEND_Im_PACKET(this.player, "0118");
        } else {
            GameObject newObj = World.world.getObjTemplate(template).createNewItem(1, false,0);
            if (signed) newObj.addTxtStat(Constant.STATS_SIGNATURE, this.player.getName());
            long guid = this.addCraftObject(receiver, newObj);
            if(guid == -1) guid = newObj.getGuid();
            String stats = newObj.parseStatsString();

            this.player.send("ErKO+" + guid + "|1|" + template + "|" + stats);
            receiver.send("ErKO+" + guid + "|1|" + template + "|" + stats);
            this.player.send("EcK;" + template + ";T" + receiver.getName() + ";" + stats);
            receiver.send("EcK;" + template + ";B" + crafter.getName() + ";" + stats);

            SocketManager.GAME_SEND_Ow_PACKET(this.player);
            SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "+" + template);
        }

        int winXP = Formulas.calculXpWinCraft(SM.get_lvl(), this.ingredients.size()) * Config.INSTANCE.getRATE_JOB();
        if (SM.getTemplate().getId() == 28 && winXP == 1)
            winXP = 10;
        if (success) {
            SM.addXp(this.player, winXP);
            ArrayList<JobStat> SMs = new ArrayList<>();
            SMs.add(SM);
            SocketManager.GAME_SEND_JX_PACKET(this.player, SMs);
        }

        this.ingredients.clear();
        return success;
    }

    public void addIngredient(Player player, long id, int quantity) {
        int oldQuantity = this.ingredients.get(id) == null ? 0 : this.ingredients.get(id);
        if(quantity < 0) if(- quantity > oldQuantity) return;

        this.ingredients.remove(id);
        oldQuantity += quantity;

        if (oldQuantity > 0) {
            this.ingredients.put(id, oldQuantity);
            int rarity = 0;
            if(World.world.getGameObject(id) != null)
            {
                GameObject object = World.world.getGameObject(id);
                rarity = object.getRarity();
            }
            SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(player, 'O', "+", id + "|" + oldQuantity + "|" + rarity);
        } else {
            SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(player, 'O', "-", id + "");
        }
    }

    public byte sizeList(Map<Player, ArrayList<Couple<Long, Integer>>> list) {
        byte size = 0;

        for (ArrayList<Couple<Long, Integer>> entry : list.values()) {
            for (Couple<Long, Integer> couple : entry) {
                GameObject object = World.world.getGameObject(couple.first);
                if (object != null) {
                    ObjectTemplate objectTemplate = object.getTemplate();
                    if (objectTemplate != null && objectTemplate.getId() != 7508) size++;
                }
            }
        }
        return size;
    }

    public void putLastCraftIngredients() {
        if (this.player == null || this.lastCraft == null || !this.ingredients.isEmpty()) return;

        this.ingredients.clear();
        this.ingredients.putAll(this.lastCraft);
        this.ingredients.entrySet().stream().filter(e -> World.world.getGameObject(e.getKey()) != null)
                .filter(e -> !(World.world.getGameObject(e.getKey()).getQuantity() < e.getValue()))
                .forEach(e -> SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.player, 'O', "+", e.getKey() + "|" + e.getValue()));
    }

    public void resetCraft() {
        this.ingredients.clear();
        this.lastCraft.clear();
        this.oldJobCraft = null;
        this.jobCraft = null;
        this.craftQuantity = 1;
    }

    //FM commence a etre OK

    private int reConfigingRunes = -1;

    public void modifIngredient(Player P, long guid, int qua) {
        //on prend l'ancienne valeur
        int q = this.ingredients.get(guid) == null ? 0 : this.ingredients.get(guid);
        if(qua < 0) if(-qua > q) return;
        //on enleve l'entrï¿½e dans la Map
        this.ingredients.remove(guid);
        //on ajoute (ou retire, en fct du signe) X objet
        q += qua;
        if (q > 0) {
            this.ingredients.put(guid, q);
            int rarity = 0;
            if(World.world.getGameObject(guid) != null)
            {
                GameObject object = World.world.getGameObject(guid);
                rarity = object.getRarity();
            }
            SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(P, 'O', "+", guid + "|"
                    + q + "|" + rarity);
        } else
            SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(P, 'O', "-", guid + "");
    }

    public void modifIngredient2(final Player P, final long guid, final int qua) {
        int q = (this.ingredients.get(guid) == null) ? 0 : this.ingredients.get(guid);
        this.ingredients.remove(guid);
        q += qua;
        if (q > 0) {
            this.ingredients.put(guid, q);
            int rarity = 0;
            if(World.world.getGameObject(guid) != null)
            {
                GameObject object = World.world.getGameObject(guid);
                rarity = object.getRarity();
            }
            SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(P, 'O', "+", guid + "|" + q + "|" + rarity);
        } else {
            SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(P, 'O', "-", String.valueOf(guid));
        }
    }
    public int lastDigit(int number) { return Math.abs(number) % 10; }

    // TODO : Changé le Fm surtout les cas d'echec quand impossible ca fait moche de pas remettre les items en gros juste rajouté (EmKO+6313970|...)
    private synchronized void craftMaging(boolean isReapeat, int repeat) {
        // On commence le fm
        //Definition des variables
        int limitPerLigne = 151; // Max par ligne
        boolean isRestating = false;
        boolean isSigningRune = false; // Si on utilise une rune de signature
        GameObject objectFm = null, signingRune = null, runeOrPotion = null, objectSave = null; // Initalisation pour potentiellement les récuperer derriere et vide le cache
        int lvlElementRune = 0; // De meme
        int lvlDurabilityRune = 0;
        int lvlQuaStatsRune = 0;

        int statsID = -1;
        int statsAdd = 0;
        long deleteID = -1;
        float poidRune = 0;
        long idRune = 0;
        boolean bonusRune = false; // Rune "Bonus" je suppose que c'est pour les serveurs cheat
        String statsObjectFm = "-1";  // La stats que l'on veut FM
        String loi = ""; // La loi de FM qui va être utilisé par l'algo (Potentiellement inutile si corrigé totalement)


        for (long idIngredient : this.ingredients.keySet()) { // On boucle sur Les ingrédients Pour différencié la Rune de l'objet
            GameObject ing = World.world.getGameObject(idIngredient); // On récupÃ¨re l'ingrédient
            if (ing == null || !this.player.hasItemGuid(idIngredient)) { // On check s'il existe et si le User a bien l'item
                SocketManager.GAME_SEND_Ec_PACKET(this.player, "EI"); // PACKET DE RETOUR POUR AFFICHER L'ERREUR CHEZ LE JOUEUR
                SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "-");
                this.ingredients.clear();
                return; // ECHEC car pas d'ingrédient ou inexistant
            }
            int templateID = ing.getTemplate().getId(); // On récupÃ¨re le template de la rune

            if(ing.getTemplate().getType() == 78 || ing.getTemplate().getType() == 26){
                idRune = idIngredient;
                // Rune
                switch (templateID) {
                    case 17197:
                    case 17198:
                    case 17199:
                    case 17200:
                    case 17202:
                    case 17203:
                    case 17204:
                    case 17205:
                        runeOrPotion = ing;
                        isRestating = true;
                        break;
                    // Ca c'est les potion de tempete (Pour FM Arme)
                    case 1333:
                    case 1343:
                    case 1345:
                        statsID = 99;
                        lvlElementRune = ing.getTemplate().getLevel();
                        runeOrPotion = ing;
                        break;
                    case 1335:
                    case 1341:
                    case 1346:
                        statsID = 96;
                        lvlElementRune = ing.getTemplate().getLevel();
                        runeOrPotion = ing;
                        break;
                    case 1337:
                    case 1342:
                    case 1347:
                        statsID = 98;
                        lvlElementRune = ing.getTemplate().getLevel();
                        runeOrPotion = ing;
                        break;
                    case 2529:
                    case 2538:
                    case 2541:
                        statsObjectFm = World.world.getObjTemplate(ing.getTemplate().getId()).getStrTemplate().split("#")[0];
                        lvlDurabilityRune = Integer.parseInt(World.world.getObjTemplate(ing.getTemplate().getId()).getStrTemplate().split("#")[2], 16);
                        statsAdd = Integer.parseInt(World.world.getObjTemplate(ing.getTemplate().getId()).getStrTemplate().split("#")[2], 16);
                        runeOrPotion = ing;
                        return;
                        //Rajouter les potions de durabilités
                    case 2539:
                    case 2540:
                    case 2543:
                        statsObjectFm = World.world.getObjTemplate(ing.getTemplate().getId()).getStrTemplate().split("#")[0];
                        statsAdd = Integer.parseInt(World.world.getObjTemplate(ing.getTemplate().getId()).getStrTemplate().split("#")[2], 16);
                        lvlDurabilityRune = Integer.parseInt(World.world.getObjTemplate(ing.getTemplate().getId()).getStrTemplate().split("#")[2], 16);
                        runeOrPotion = ing;
                        return;
                    case 1338:
                    case 1340:
                    case 1348:
                        statsID = 97;
                        lvlElementRune = ing.getTemplate().getLevel();
                        runeOrPotion = ing;
                        break;
                    default:
                        Rune runetype = Rune.getRuneById(ing.getTemplate().getId());
                        runeOrPotion = ing;
                        statsObjectFm = runetype.getCharacteristicString();
                        statsAdd = runetype.getBonus();
                        poidRune = runetype.getWeight();
                        lvlQuaStatsRune = ing.getTemplate().getLevel();
                        break;
                }
            }
            else if (ing.getTemplate().getId() == 7508){
                // Rune de signature
                isSigningRune = true;
                signingRune = ing;

            }
            else{
                // Objet
                int type = ing.getTemplate().getType(); // On récupÃ¨re son type


                if (((type >= 1 && type <= 11) || (type >= 16 && type <= 22)
                        || type == 81 || type == 102 || type == 114
                        || ing.getTemplate().getPACost() > 0) && type != Constant.ITEM_TYPE_FAMILIER && type != Constant.ITEM_TYPE_BOUCLIER && ing.getPosition() == -1 && ing.getObvijevanPos() == 0) { // Si c'est un obj avec des stats ou avec des PA d'utilisation
                    objectFm = ing;

                    //SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK_FM(this.player.getGameClient(), 'O', "+", objectFm.getGuid()  + "|" + 1); // On envoie un packet de validation au joueur il me semble pas besoin cette merde
                    deleteID = idIngredient; // On récupÃ¨re l'id de l'ingrédient pour le supprimer plus tard
                    GameObject newObj = GameObject.getCloneObjet(objectFm, 1); // Crï¿½ation d'un clone avec un nouveau identifiant
                    if (objectFm.getQuantity() > 1) { // S'il y avait plus d'un objet
                        int newQuant = objectFm.getQuantity() - 1; // On supprime le cloné
                        objectFm.setQuantity(newQuant); // on actualise les objets du serveur allumé
                        SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, objectFm); // on actualise les objets sur le joueur
                    } else {
                        World.world.removeGameObject(idIngredient);
                        this.player.removeItem(idIngredient);
                        SocketManager.GAME_SEND_DELETE_STATS_ITEM_FM(this.player, idIngredient);
                    }
                    objectFm = newObj; // Tout neuf avec un nouveau identifiant

                }
                else{
                    objectSave = ing;
                }

            }
        }

        if(lvlDurabilityRune > 0){

        }
         //System.out.println("La :" + objectFm.getTemplate().getId() + " " + runeOrPotion.getTemplate().getName() + " " + SM );
        //runeOrPotion.getTemplate().getName()
        if( objectFm == null || runeOrPotion == null ) { // pas de runes
            this.player.sendMessage("Aucun Objet Modifiable ou Aucune rune détecté");
            if (objectFm != null) {
                World.world.addGameObject(objectFm, true);
                this.player.addObjet(objectFm);
                //SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, objectFm);
                final String data = String.valueOf(objectFm.getGuid()) + "|1|" + objectFm.getTemplate().getId() + "|"
                        + objectFm.parseStatsString()+ "|"+objectFm.getRarity();
                SocketManager.GAME_SEND_EXCHANGE_MOVE_OK_FM(this.player, 'O', "+", data);
            }
            else{
                if (objectSave != null) {
                    World.world.addGameObject(objectFm, true);
                    this.player.addObjet(objectFm);
                    //SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, objectFm);
                }
            }
            SocketManager.GAME_SEND_Ec_PACKET(this.player, "EI"); // packet d'echec
            SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "-");  // packet d'icone rouge je crois
            // On nettoie les ingrédients


            this.ingredients.clear();
            return;
        } // Pas de rune
        if( runeOrPotion.getTemplate().getId() >= 17197 &&  runeOrPotion.getTemplate().getId() <= 17200 ){
            if (SM == null || objectFm == null) {
                this.player.sendMessage("Vous ne possedez pas Ou de metier approprié, Ou de rune, ou d'objet");
                if (objectFm != null) {
                    World.world.addGameObject(objectFm, true);
                    this.player.addObjet(objectFm);
                    //SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, objectFm);
                }
                SocketManager.GAME_SEND_Ec_PACKET(this.player, "EI"); // packet d'echec
                SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "-");  // packet d'icone rouge je crois


                this.ingredients.clear(); // On nettoie les ingrédients


                return;
            }

            int maxLvlItem = 1;
            switch (runeOrPotion.getTemplate().getId()){
                case 17197:
                    maxLvlItem = 100;
                    break;
                case 17198:
                    maxLvlItem = 150;
                    break;
                case 17199:
                    maxLvlItem = 180;
                    break;
                case 17200:
                    maxLvlItem = 200;
                    break;
            }

            int rarity = objectFm.getRarity();
            int templateID = objectFm.getMimibiote();

            ObjectTemplate objTemplate = objectFm.getTemplate();
            if(objTemplate.getLevel() > maxLvlItem){
                this.player.sendMessage("La rune de réinitialisation utilisée ne convient pas pour un item de ce level, Level maximum : "+maxLvlItem);
                if (objectFm != null) {
                    World.world.addGameObject(objectFm, true);
                    this.player.addObjet(objectFm);
                    //SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, objectFm);
                }
                SocketManager.GAME_SEND_Ec_PACKET(this.player, "EI"); // packet d'echec
                SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "-");  // packet d'icone rouge je crois

                this.ingredients.clear(); // On nettoie les ingrédients

                return;
            }

            GameObject newObj = World.world.getObjTemplate(objTemplate.getId()).createNewItemWithoutDuplicationWithrarity(this.player.getItems().values(), 1, false,rarity);
            //GameObject newObj = new GameObject(id, getId(), 1, Constant.ITEM_POS_NO_EQUIPED, ObjectTemplate.generateNewStatsFromTemplate(objTemplate.getStrTemplate(), false,rarity), ObjectTemplate.getEffectTemplate(objTemplate.getStrTemplate()), new HashMap<Integer, Integer>(), Stat, 0,rarity);
            objectFm = newObj;
            long guid = newObj.getGuid();//FIXME: Ne pas recrée un item pour l'empiler après

            if(guid == -1) { // Don't exist
                    guid = newObj.setId();
                    World.world.addGameObject(newObj, true);
                    this.player.addObjet(newObj);
            } else {
                    newObj.setQuantity(newObj.getQuantity() + 1);
                    SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, newObj);
            }

            SocketManager.GAME_SEND_Ow_PACKET(this.player);

            final String data = String.valueOf(newObj.getGuid()) + "|1|" + newObj.getTemplate().getId() + "|"
                    + newObj.parseStatsString()+ "|"+newObj.getRarity();


            if (deleteID != -1) {
                //this.player.sendMessage("On retire l'ingrédient (Rune) :"+deleteID);
                this.ingredients.remove(deleteID);
            }
            if (runeOrPotion != null) {
                int newQua = runeOrPotion.getQuantity() - 1;
                if (newQua <= 0) {
                    this.player.removeItem(runeOrPotion.getGuid());
                    World.world.removeGameObject(runeOrPotion.getGuid());
                    SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this.player, runeOrPotion.getGuid());
                } else {
                    runeOrPotion.setQuantity(newQua);
                    SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, runeOrPotion);
                }
            }

            if (!this.isRepeat) {
                this.reConfigingRunes = -1;
            }
            if (this.reConfigingRunes != 0 || this.broken) {
                SocketManager.GAME_SEND_EXCHANGE_MOVE_OK_FM(this.player, 'O', "+", data);
            }

            SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "+" + objTemplate.getId());
            SocketManager.GAME_SEND_Ec_PACKET(this.player, "K;" + objTemplate.getId());

        } // Rune de pouvoir
        else if(runeOrPotion.getTemplate().getId() == 17202 || runeOrPotion.getTemplate().getId() == 17203
                || runeOrPotion.getTemplate().getId() == 17204 || runeOrPotion.getTemplate().getId() == 17205){

            if (SM == null || objectFm == null) {
                this.player.sendMessage("Vous ne possedez pas Ou de metier approprié, Ou de rune, ou d'objet");
                if (objectFm != null) {
                    World.world.addGameObject(objectFm, true);
                    this.player.addObjet(objectFm);
                    //SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, objectFm);
                }
                SocketManager.GAME_SEND_Ec_PACKET(this.player, "EI"); // packet d'echec
                SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "-");  // packet d'icone rouge je crois


                this.ingredients.clear(); // On nettoie les ingrédients


                return;
            }
            int rarity = objectFm.getRarity();
            ObjectTemplate objTemplate = objectFm.getTemplate();
            int lastDigit = lastDigit(runeOrPotion.getTemplate().getId());
            //System.out.println("lastDigit" + lastDigit);

            if(lastDigit <= rarity){

                this.player.sendMessage("L'objet est déjà de rareté supérieure ou égale");
                if (objectFm != null) {
                    World.world.addGameObject(objectFm, true);
                    this.player.addObjet(objectFm);
                    //SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, objectFm);
                }
                SocketManager.GAME_SEND_Ec_PACKET(this.player, "EI"); // packet d'echec
                SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "-");  // packet d'icone rouge je crois


                this.ingredients.clear(); // On nettoie les ingrédients
                return;
            }

            if(lastDigit-1 != rarity){
                this.player.sendMessage("L'objet ne possède pas la rareté suffisante pour être augmenté avec cette rune");
                if (objectFm != null) {
                    World.world.addGameObject(objectFm, true);
                    this.player.addObjet(objectFm);
                    //SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, objectFm);
                }
                SocketManager.GAME_SEND_Ec_PACKET(this.player, "EI"); // packet d'echec
                SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "-");  // packet d'icone rouge je crois


                this.ingredients.clear(); // On nettoie les ingrédients

                return;
            }


            GameObject newObj = World.world.getObjTemplate(objTemplate.getId()).createNewItemWithoutDuplicationWithrarity(this.player.getItems().values(), 1, false,lastDigit);
            //GameObject newObj = new GameObject(id, getId(), 1, Constant.ITEM_POS_NO_EQUIPED, ObjectTemplate.generateNewStatsFromTemplate(objTemplate.getStrTemplate(), false,rarity), ObjectTemplate.getEffectTemplate(objTemplate.getStrTemplate()), new HashMap<Integer, Integer>(), Stat, 0,rarity);
            objectFm = newObj;
            long guid = newObj.getGuid();//FIXME: Ne pas recrée un item pour l'empiler après

            if(guid == -1) { // Don't exist
                guid = newObj.setId();
                World.world.addGameObject(newObj, true);
                this.player.addObjet(newObj);
            } else {
                newObj.setQuantity(newObj.getQuantity() + 1);
                SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, newObj);
            }
            SocketManager.GAME_SEND_Ow_PACKET(this.player);

            final String data = String.valueOf(newObj.getGuid()) + "|1|" + newObj.getTemplate().getId() + "|"
                    + newObj.parseStatsString()+ "|"+newObj.getRarity();


            if (deleteID != -1) {
                //this.player.sendMessage("On retire l'ingrédient (Rune) :"+deleteID);
                this.ingredients.remove(deleteID);
            }
            if (runeOrPotion != null) {
                int newQua = runeOrPotion.getQuantity() - 1;
                if (newQua <= 0) {
                    this.player.removeItem(runeOrPotion.getGuid());
                    World.world.removeGameObject(runeOrPotion.getGuid());
                    SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this.player, runeOrPotion.getGuid());
                } else {
                    runeOrPotion.setQuantity(newQua);
                    SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, runeOrPotion);
                }
            }
            //World.world.addGameObject(objectFm, true);
            //this.player.addObjet(objectFm);
            //SocketManager.GAME_SEND_Ow_PACKET(this.player);
            //final String data = String.valueOf(objectFm.getGuid()) + "|1|" + objectFm.getTemplate().getId() + "|"
            //		+ objectFm.parseStatsString()+ "|"+objectFm.getRarity();
            if (!this.isRepeat) {
                this.reConfigingRunes = -1;
            }
            if (this.reConfigingRunes != 0 || this.broken) {
                SocketManager.GAME_SEND_EXCHANGE_MOVE_OK_FM(this.player, 'O', "+", data);
            }

            //this.player.sendMessage(""+data);
            //this.data = data;
            SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "+" + objTemplate.getId());
            SocketManager.GAME_SEND_Ec_PACKET(this.player, "K;" + objTemplate.getId());

        } //Rune de pouvoir rareté
        else{

            int StatEnInt = Integer.parseInt(statsObjectFm, 16);
            double poidUnitaire = getPwrPerEffet(StatEnInt); // On calcul le poid unitaire de la rune
            double poidStatsAfter =0;
            double poidStatsBefore =0;

            /*if (poidUnitaire > 0.0) {
                poidRune = (int)Math.round(statsAdd * poidUnitaire); // On le multiplie par sa valur logiquement pour avoir le poid total
            }*/

            //this.player.sendMessage("poid de la stat a l'unité :"+poidUnitaire);
            //this.player.sendMessage("poid de la rune :"+poidRune);

            if (SM == null || objectFm == null || runeOrPotion == null) { // On check avant de commencer le traitement si tous les pré-requis et recupération ont fonctionné
                this.player.sendMessage("Vous ne possédez pas ou de metier approprié, ou de rune, ou d'objet");

                //System.out.println("On rentre forcement la " );
                if (objectFm != null) {
                    World.world.addGameObject(objectFm, true);
                    this.player.addObjet(objectFm);
                }

                SocketManager.GAME_SEND_Ec_PACKET(this.player, "EI"); // packet d'echec
                SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "-");  // packet d'icone rouge je crois
                //this.ingredients.clear(); // On nettoie les ingrédients
                return;
            }

            if (SM == null || objectFm == null || runeOrPotion == null) { // On check avant de commencer le traitement si tous les pré-requis et recupération ont fonctionné
                this.player.sendMessage("Vous ne possédez pas ou de metier approprié, ou de rune, ou d'objet");

                //System.out.println("On rentre forcement la " );
                if (objectFm != null) {
                    World.world.addGameObject(objectFm, true);
                    this.player.addObjet(objectFm);
                }

                SocketManager.GAME_SEND_Ec_PACKET(this.player, "EI"); // packet d'echec
                SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "-");  // packet d'icone rouge je crois
                //this.ingredients.clear(); // On nettoie les ingrédients
                return;
            }

            int rarity = objectFm.getRarity();
            ObjectTemplate objTemplate = objectFm.getTemplate(); // On récupÃ¨re le template de l'objet
            int chance = 0;
            int lvlJob = SM.get_lvl();	// le level du métier
            int PoidTotItemActuel = 0;
            int pwrPerte = 0;
            java.util.ArrayList<Integer> chances = new ArrayList<Integer>(); // Le tableau des chances
            int objTemplateID = objTemplate.getId(); 	// L'id du template
            String statStringObj = objectFm.parseStatsString();  // Les stats de l'objects en format String

            if (lvlElementRune > 0 && lvlQuaStatsRune == 0) { // Le cas des runes élémentaire est unique, Le level du métier défini la réussite
                //this.player.sendMessage("C'est une rune élémentaire (pour CAC)"+lvlElementRune);
                chance = Formulas.calculChanceByElement(lvlJob, objTemplate.getLevel(), lvlElementRune);
                if (chance > 100 - (lvlJob / 20))
                    chance = 100 - (lvlJob / 20);
                if (chance < (lvlJob / 20))
                    chance = (lvlJob / 20);
                chances.add(0, chance);
                chances.add(1, 0);
                chances.add(2, 100 - chance);
            }
            else if (lvlQuaStatsRune > 0 && lvlElementRune == 0) {

                // Le cas du FM normal est trÃ¨s complÃ¨xe !

                int PoidActuelStatAFm = 1;
                int PoidTotStatsExoItemActuel = 1;

                if (!statStringObj.isEmpty()) {

                    PoidTotItemActuel = currentTotalWeigthBase(statStringObj, objectFm); // Poids total de l'objet
                    PoidTotStatsExoItemActuel = currentWeithStatsExo(statStringObj, objectFm); // Poid des stats EXO (car si ca dépasse 151 ca echec)
                }
                else {
                    PoidActuelStatAFm = 0;
                    PoidTotStatsExoItemActuel = 0;
                }

                int PoidMaxItem = 1;
                if(rarity>3){
                    PoidMaxItem = (int) WeithTotalBaseLegendary(objTemplateID); // Poids de l'objet en stat Max et légendaire (donc théoriquement le poid max)
                }
                else{
                    PoidMaxItem = (int) WeithTotalBase(objTemplateID); // Poids de l'objet en stat Max (donc théoriquement le poid max)
                }
                int PoidMiniItem = WeithTotalBaseMin(objTemplateID); // Poids de l'objet en stat Mini (donc théoriquement le poid minimum)
                // TODO : Rajouter le poid mini en Légendaire (je sais pas si c'est utile)

                // Bon si les poid sont négatifs, on les positionne a 0 pour éviter les bugs, meme si faudrait gérer le cas différemment
                if (PoidMaxItem <= 0) {
                    PoidMaxItem = 1;
                }
                if (PoidMiniItem <= 0) {
                    PoidMiniItem = 1;
                }
                if (PoidTotItemActuel < 0) {
                    PoidTotItemActuel = 0;
                }
                if (PoidActuelStatAFm < 0) {
                    PoidActuelStatAFm = 0;
                }
                if (PoidTotStatsExoItemActuel < 0) {
                    PoidTotStatsExoItemActuel = 0;
                }

                float coef = 1;
                int statJetActuel = 0;

                // Gestion des dommages bizarres
                if(statsObjectFm.equals("70") || statsObjectFm.equals("79") ) {
                    int statJetActuel1 = getActualJet(objectFm, "79");
                    int statJetActuel2 = getActualJet(objectFm, "70");// Jet actuel de l'item pour rendre plus compliqué si on approche du poid théorique ou de la stats max si ca dépasse le poid théorique
                    if(statJetActuel1 > statJetActuel2) {
                        statsObjectFm = "79";
                        statJetActuel = statJetActuel1;

                    }
                    else {
                        statsObjectFm = "70";
                        statJetActuel = statJetActuel2;
                    }
                    //System.out.println("On a choisit "+ statsObjectFm + " " +statJetActuel1 + " et " + statJetActuel2);
                }

                int CheckStatItemTemplate = viewBaseStatsItem(objectFm, statsObjectFm); // Check si La stats est sur l'item de base
                int CheckStatItemActuel = viewActualStatsItem(objectFm, statsObjectFm); // Check si La stats est sur l'item 1 = oui 2 = oui 0 = non

                // Cas des DO, est-ce que c'est présent sur l'item ?
                if(statsObjectFm.equals("70") || statsObjectFm.equals("79") ) {
                    int statJetBase1 = viewBaseStatsItem(objectFm, "79");
                    int statJetBase2 = viewBaseStatsItem(objectFm, "70");// Jet actuel de l'item pour rendre plus compliqué si on approche du poid théorique ou de la stats max si ca dépasse le poid théorique
                    if(statJetBase1 > statJetBase2) {
                        statsObjectFm = "79";
                        CheckStatItemTemplate = statJetBase1;

                    }
                    else {
                        statsObjectFm = "70";
                        CheckStatItemTemplate = statJetBase2;
                    }

                    int statJetActuel1 = viewActualStatsItem(objectFm, statsObjectFm);
                    CheckStatItemActuel = statJetActuel1;
                    //System.out.println("On a choisit "+ statsObjectFm + " " +statJetActuel1 + " et " + statJetActuel2);
                }
                // utile ? je ne crois pas
                /*if(statsObjectFm.equals("70") || statsObjectFm.equals("79") ) {
                    int statJetActuel1 = viewActualStatsItem(objectFm, "79");
                    int statJetActuel2 = viewActualStatsItem(objectFm, "70");// Jet actuel de l'item pour rendre plus compliqué si on approche du poid théorique ou de la stats max si ca dépasse le poid théorique
                    if(statJetActuel1 > statJetActuel2) {
                        //statsObjectFm = "79";
                        CheckStatItemTemplate = statJetActuel1;

                    }
                    else {
                        //statsObjectFm = "70";
                        CheckStatItemTemplate = statJetActuel2;
                    }
                    //System.out.println("On a choisit "+ statsObjectFm + " " +statJetActuel1 + " et " + statJetActuel2);
                }*/

                // TODO : Trouver un moment pour diminuer une stats négative plutot que la considéré comme un stat différente
                int statMax = 0 ;

                if(rarity > 3){
                    statMax = getStatBaseMaxLegendaire(objTemplate, statsObjectFm);// stat maximum de l'obj Legendaire intéressant pour les cas ou les stats dépasse le poid théorique max
                    /*if(statMax == 0 && (statsObjectFm.equals("70") || statsObjectFm.equals("79")) ) {
                        int statJetActuel1 = getStatBaseMaxLegendaire(objTemplate, "79");
                        int statJetActuel2 = getStatBaseMaxLegendaire(objTemplate, "70");// Jet actuel de l'item pour rendre plus compliqué si on approche du poid théorique ou de la stats max si ca dépasse le poid théorique
                        if(statJetActuel1 > statJetActuel2) {
                            //statsObjectFm = "79";
                            statMax = statJetActuel1;

                        }
                        else {
                            //statsObjectFm = "70";
                            statMax = statJetActuel2;
                        }
                        //System.out.println("On a choisit "+ statsObjectFm + " " +statJetActuel1 + " et " + statJetActuel2);
                    }*/
                }
                else{
                    statMax = getStatBaseMax(objTemplate, statsObjectFm); // stat maximum de l'obj intéressant pour les cas ou les stats dépasse le poid théorique max
                    /*if(statMax == 0 && (statsObjectFm.equals("70") || statsObjectFm.equals("79")) ) {
                        int statJetActuel1 = getStatBaseMaxLegendaire(objTemplate, "79");
                        int statJetActuel2 = getStatBaseMaxLegendaire(objTemplate, "70");// Jet actuel de l'item pour rendre plus compliqué si on approche du poid théorique ou de la stats max si ca dépasse le poid théorique
                        if(statJetActuel1 > statJetActuel2) {
                            //statsObjectFm = "79";
                            statMax = statJetActuel1;

                        }
                        else {
                            //statsObjectFm = "70";
                            statMax = statJetActuel2;
                        }
                        //System.out.println("On a choisit "+ statsObjectFm + " " +statJetActuel1 + " et " + statJetActuel2);
                    }*/
                }

                int statMin = getStatBaseMin(objTemplate, statsObjectFm); // stat Minimum de l'obj intéressant pour les cas ou les stats dépasse le poid théorique max
                /*if(statMin == 0 && (statsObjectFm.equals("70") || statsObjectFm.equals("79")) ) {
                    int statJetActuel1 = getStatBaseMaxLegendaire(objTemplate, "79");
                    int statJetActuel2 = getStatBaseMaxLegendaire(objTemplate, "70");// Jet actuel de l'item pour rendre plus compliqué si on approche du poid théorique ou de la stats max si ca dépasse le poid théorique
                    if(statJetActuel1 > statJetActuel2) {
                        //statsObjectFm = "79";
                        statMin = statJetActuel1;

                    }
                    else {
                        //statsObjectFm = "70";
                        statMin = statJetActuel2;
                    }
                    //System.out.println("On a choisit "+ statsObjectFm + " " +statJetActuel1 + " et " + statJetActuel2);
                }*/

                 statJetActuel = getActualJet(objectFm, statsObjectFm);

                int statJetFutur = statJetActuel + statsAdd;


                PoidActuelStatAFm = (int)Math.floor(statJetActuel*poidUnitaire); // Poid des stats de base

                // La on fait des controles pour savoir si le FM théorique est possible ou non
                boolean canFM = true;
                float x = 1;

                if(statMax*poidUnitaire > limitPerLigne){ // Si le poid de la ligne de stats de base de l'item supérieur au maximum théorique par ligne de 101(151 en leg)
                    // On autorise quand même le fm si on dépasse pas le jet max
                    if (statJetActuel+statsAdd > statMax) { // On compare en statistique car le poid ne compte pas pour ces cas
                        this.player.sendMessage("Cette statistique ne montra pas plus haut");
                        canFM = false;
                    }
                }
                else {
                    // Si la stats qui veut faire passer, dépasse la limite théorique
                    if( (statJetActuel*poidUnitaire)+poidRune > limitPerLigne ) {
                        this.player.sendMessage("Cette statistique ne montra pas plus haut");
                        canFM = false;
                    }
                }

                if((CheckStatItemTemplate == 0 && CheckStatItemActuel == 0) || (CheckStatItemTemplate == 0 && CheckStatItemActuel == 1)) {
                    // La stat qu'on ajoute est un over et son poid est de PoidRune
                    if( PoidTotStatsExoItemActuel + poidRune > limitPerLigne) {
                        canFM = false;
                        this.player.sendMessage("Tu ne peux pas ajouter plus d'Exo");
                        this.player.sendMessage("Le poid des Exo :"+PoidTotStatsExoItemActuel);
                    }
                }
                else {
                    // TIEN ici on va utiliser le X pour plutot simplifier si on est loin de la limite théorique mais que c'est pas un exo bien entendu
                    if( statJetActuel != 0 ) {
                        x = (float) (limitPerLigne / (statJetActuel*poidUnitaire));
                        if(x > 5.0f) {
                            x = 5.0f;
                        }
                    }
                    else {
                        x = 5.0f;
                    }
                    //this.player.sendMessage("On passe dans la simplification ?");
                }

                // La c'est les contraintes de métier ETC
                if (lvlJob < (int) Math.floor(objTemplate.getLevel() / 2)) {
                    this.player.sendMessage("Ton métier n'est pas suffisant pour améliorer cet objet ! Ressaie quand tu sera niveau " +  ((int) Math.floor(objTemplate.getLevel() / 2)+1) );
                    canFM = false; // On rate le FM si le mï¿½tier n'est pas suffidant
                }
                //System.out.println( statMax + " pour " + statJetFutur + " Jet actuel " + statJetActuel);
                // La notien de loi + permet de cibler le coef
                if(poidRune > 30) {
                    if( statMax < statJetFutur) {
                        loi = "exo";
                        coef = 0.25f;
                    }
                    else {
                        loi = "normal";
                        coef = 1.0f;
                    }
                }
                else {
                    if ( statMax < statJetFutur) {
                        loi = "over";
                        coef = 0.8f;
                    }
                    else {
                        loi = "normal";
                        coef = 1.0f;
                    }
                }


                if (canFM) {
                    chances = Formulas.chanceFM2(PoidMaxItem, PoidMiniItem, PoidTotItemActuel, PoidActuelStatAFm,PoidTotStatsExoItemActuel , poidRune, statMax, statMin, statJetActuel,statsAdd,poidUnitaire,statJetFutur, x , coef, this.player, objectFm.getPuit(), loi );
                    // On retire la rune car on peut FM
                    if (deleteID != -1) {
                        //this.player.sendMessage("On retire l'ingrédient (Rune) :"+deleteID);
                        this.ingredients.remove(deleteID);
                    }
                }
                else
                {	// CORRIGE UN TRUC LA POUR QUE L'UTILISATEUR PERDRE PAS SES ITEMS MALGRES UNE TENTATIVE DE FM NON LEGAL
                    World.world.addGameObject(objectFm, true);
                    this.player.addObjet(objectFm);


                    int nbRunes = this.ingredients.get(idRune);

                    this.ingredients.clear(); // ON RAFRAICHIT LES INGREDIENTS (enleve)

                    if (nbRunes > 0) // On remet la rune
                        this.modifIngredient(this.player, idRune, nbRunes); // Rajout des runes moins une

                    try {
                        this.player.getCurJobAction().modifIngredient2(this.player, objectFm.getGuid(), 1); // On remet l"item
                    }
                    catch(Exception e){
                        //this.player.sendMessage("On est la  :"+ e );
                        ((JobAction) this.player.getExchangeAction().getValue()).modifIngredient2(this.player, objectFm.getGuid(), 1); // On remet l'item dans la case de FM
                    }

                    return;
                }
            }

            int aleatoryChance = Formulas.getRandomValue(1, 100);
            int SC = chances.get(0);
            int SN = chances.get(1);
            boolean successC = (aleatoryChance <= SC);
            boolean successN = (aleatoryChance <= (SC + SN));

            //this.player.sendMessage("le Jet :"+aleatoryChance);
            if (successC || successN) {
                int winXP = Formulas.calculXpWinFm(objectFm.getTemplate().getLevel(), poidRune)
                        * Config.INSTANCE.getRATE_JOB();
                if (winXP > 0) {
                    SM.addXp(this.player, winXP);
                    ArrayList<JobStat> SMs = new ArrayList<JobStat>();
                    SMs.add(SM);
                    SocketManager.GAME_SEND_JX_PACKET(this.player, SMs);
                }
            }

            SocketManager.GAME_SEND_MESSAGE(this.player,"Chances [SC "+SC + "%| SN " + SN+ "%| EC " + (100 - (SC + SN)) + "%]",Constant.COULEUR_INFO);
            String Result = "";
            if (successC) // SC
            {
                Result= "Succes Critique";
                int coef = 0;
                pwrPerte = 0;
                if (lvlElementRune == 1)
                    coef = 50;
                else if (lvlElementRune == 25)
                    coef = 65;
                else if (lvlElementRune == 50)
                    coef = 85;
                if (isSigningRune) {
                    objectFm.addTxtStat(Constant.STATS_CHANGE_BY, this.player.getName()+"");
                }
                if (lvlElementRune > 0 && lvlQuaStatsRune == 0) {
                    for (Effect effect : objectFm.getEffects()) {
                        if (effect.getEffectID() != 100)
                            continue;

                        //String[] infos = effect.getArgs().split(";");

                        try {
                            int min = effect.getArgs1();
                            int max = effect.getArgs2();
                            int newMin = (min * coef) / 100;
                            int newMax = (max * coef) / 100;
                            if (newMin == 0)
                                newMin = 1;
                            String newRange = "1d" + (newMax - newMin + 1) + "+" + (newMin - 1);

                            //System.out.println(objectFm.getGuid() );
                            effect.setArgs1(newMin);
                            effect.setArgs2(newMax);
                            effect.setJet(newRange);
                            effect.setEffectID(statsID);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    //System.out.println(objectSave.getEffects().get(0).getArgs() );
                    //System.out.println(objectFm.getEffects().get(0).getArgs() );

                    //System.out.println("CAC Fm");
                } else if (lvlQuaStatsRune > 0 && lvlElementRune == 0) {
                    objectFm.setNewStats(statsObjectFm,statsAdd);
                    int StatID = Integer.parseInt(statsObjectFm, 16);
                    SocketManager.GAME_SEND_MESSAGE(this.player, "+ "+statsAdd +" "+getEffetName(StatID),Constant.COULEUR_SUCCES);
                    objectFm.setModification();
                }
                if (signingRune != null) {
                    int newQua = signingRune.getQuantity() - 1;
                    if (newQua <= 0) {
                        this.player.removeItem(signingRune.getGuid());
                        World.world.removeGameObject(signingRune.getGuid());
                        SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this.player, signingRune.getGuid());
                    } else {
                        signingRune.setQuantity(newQua);
                        SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, signingRune);
                    }
                }
                if (runeOrPotion != null) {
                    //System.out.println(runeOrPotion + " quantité " + runeOrPotion.getQuantity() );
                    int newQua = runeOrPotion.getQuantity() - 1;
                    if (newQua <= 0) {
                        this.player.removeItem(runeOrPotion.getGuid());
                        World.world.removeGameObject(runeOrPotion.getGuid());
                        SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this.player, runeOrPotion.getGuid());
                    } else {
                        runeOrPotion.setQuantity(newQua);
                        SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, runeOrPotion);
                    }
                }

                World.world.addGameObject(objectFm, true);
                this.player.addObjet(objectFm);
                SocketManager.GAME_SEND_Ow_PACKET(this.player);
                final String data = String.valueOf(objectFm.getGuid()) + "|1|" + objectFm.getTemplate().getId() + "|"
                        + objectFm.parseStatsString()+ "|"+objectFm.getRarity();
                if (!this.isRepeat) {
                    this.reConfigingRunes = -1;
                }
                if (this.reConfigingRunes != 0 || this.broken) {
                    SocketManager.GAME_SEND_EXCHANGE_MOVE_OK_FM(this.player, 'O', "+", data);
                }
                //this.player.sendMessage(""+data);
                this.data = data;
                SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "+" + objTemplateID);
                SocketManager.GAME_SEND_Ec_PACKET(this.player, "K;" + objTemplateID);

            } else if (successN) {
                Result= "Succes Neutre";
                pwrPerte = 0;
                if (isSigningRune) {
                    objectFm.addTxtStat(Constant.STATS_CHANGE_BY, this.player.getName()+"");
                }

                // GESTION DES STATS NEGATIVE A RERENDRE
                objectFm = CalculPerteAndPuit(successN , statsAdd , statsObjectFm, objectFm, poidRune, poidUnitaire, loi,PoidTotItemActuel, this.player);

                if (signingRune != null) {
                    int newQua = signingRune.getQuantity() - 1;
                    if (newQua <= 0) {
                        this.player.removeItem(signingRune.getGuid());
                        World.world.removeGameObject(signingRune.getGuid());
                        SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this.player, signingRune.getGuid());
                    } else {
                        signingRune.setQuantity(newQua);
                        SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, signingRune);
                    }
                }
                if (runeOrPotion != null) {
                    int newQua = runeOrPotion.getQuantity() - 1;
                    if (newQua <= 0) {
                        this.player.removeItem(runeOrPotion.getGuid());
                        World.world.removeGameObject(runeOrPotion.getGuid());
                        SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this.player, runeOrPotion.getGuid());
                    } else {
                        runeOrPotion.setQuantity(newQua);
                        SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, runeOrPotion);
                    }
                }

                World.world.addGameObject(objectFm, true);
                this.player.addObjet(objectFm);

                SocketManager.GAME_SEND_Ow_PACKET(this.player);
                String data = objectFm.getGuid() + "|1|" + objectFm.getTemplate().getId() + "|"+ objectFm.parseStatsString();
                if (!this.isRepeat)
                    this.reConfigingRunes = -1;
                if (this.reConfigingRunes != 0 || this.broken)
                    SocketManager.GAME_SEND_EXCHANGE_MOVE_OK_FM(this.player, 'O', "+", data);
                this.data = data;
                SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "+"
                        + objTemplateID);
                if (pwrPerte > 0) {
                    SocketManager.GAME_SEND_Ec_PACKET(this.player, "EF");
                    SocketManager.GAME_SEND_Im_PACKET(this.player, "0194");
                } else
                    SocketManager.GAME_SEND_Ec_PACKET(this.player, "K;"
                            + objTemplateID);
            } else
            // EC
            {
                Result= "Echec";
                pwrPerte = 0;
                if (signingRune != null) { // On perd la rune signature
                    int newQua = signingRune.getQuantity() - 1;
                    if (newQua <= 0) {
                        this.player.removeItem(signingRune.getGuid());
                        World.world.removeGameObject(signingRune.getGuid());
                        SocketManager.GAME_SEND_DELETE_STATS_ITEM_FM(this.player, signingRune.getGuid());
                    } else {
                        signingRune.setQuantity(newQua);
                        SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, signingRune);
                    }
                }
                if (runeOrPotion != null) { // On perd la potion
                    int newQua = runeOrPotion.getQuantity() - 1;
                    if (newQua <= 0) {
                        this.player.removeItem(runeOrPotion.getGuid());
                        World.world.removeGameObject(runeOrPotion.getGuid());
                        SocketManager.GAME_SEND_DELETE_STATS_ITEM_FM(this.player, runeOrPotion.getGuid());
                    } else {
                        runeOrPotion.setQuantity(newQua);
                        SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, runeOrPotion);
                    }
                }

                objectFm = CalculPerteAndPuit(successN , statsAdd , statsObjectFm, objectFm, poidRune, poidUnitaire, loi,PoidTotItemActuel, this.player);

                //this.player.sendMessage("On est la l'objet a pas perdu de stat :"+ objectFm.getPuit() );
                World.world.addGameObject(objectFm, true); // On ajoute l'obj a la map
                this.player.addObjet(objectFm); // On ajoute l'obj au joueur
                SocketManager.GAME_SEND_Ow_PACKET(this.player); // Ca je vois pas trop a part l'echec

                String data = objectFm.getGuid() + "|1|"+ objectFm.getTemplate().getId() + "|"+ objectFm.parseStatsString(); // Ca non plus mais ca met un obj undefined ?

                if (!this.isRepeat)
                    this.reConfigingRunes = -1;

                if (this.reConfigingRunes != 0 || this.broken)
                    SocketManager.GAME_SEND_EXCHANGE_MOVE_OK_FM(this.player, 'O', "+", data); // Ca je ne sais pas

                this.data = data;

                SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "-"
                        + objTemplateID); // Ca c'est l'echec sur l'icone je opense
                SocketManager.GAME_SEND_Ec_PACKET(this.player, "EF"); // Ca c'est l'echec sur le joueur ?

                if (pwrPerte > 0)
                    SocketManager.GAME_SEND_Im_PACKET(this.player, "0117"); // Ca c'est gain de reliquat
                else
                    SocketManager.GAME_SEND_Im_PACKET(this.player, "0183"); // Ca c'est perte ?

            }
            SocketManager.GAME_SEND_MESSAGE(this.player,"Résultat -> " +Result +" | Puit restant : "+objectFm.getPuit(),Constant.COULEUR_INFO);
        } //Rune de FM

        this.lastCraft.clear();
        this.lastCraft.putAll(this.ingredients);
        this.lastCraft.put(objectFm.getGuid(), 1);
        int nbRunes = 0;
        //System.out.println("On est la " + idRune );
        if (!this.ingredients.isEmpty() && this.ingredients.get(idRune) != null) {
            //System.out.println("On est la +"+ this.ingredients.get(idRune) );
            if (this.isRepeat) {
                nbRunes = this.ingredients.get(idRune) - 1;
                //System.out.println("On est la +"+ this.ingredients);
            } else {
                nbRunes = this.ingredients.get(idRune) - 1;
            }
        }

        Database.getStatics().getPlayerData().update(this.player);
        this.ingredients.clear(); // ON RAFRAICHIT LES INGREDIENTS (enleve)

        if (nbRunes > 0) // On remet la rune
            this.modifIngredient(this.player, idRune, nbRunes); // Rajout des runes moins une

        try {
            this.player.getCurJobAction().modifIngredient2(this.player, objectFm.getGuid(), 1); // On remet l"item
        }
        catch(Exception e){
            //this.player.sendMessage("On est la  :"+ e );

            ((JobAction) this.player.getExchangeAction().getValue()).modifIngredient2(this.player, objectFm.getGuid(), 1); // On remet l'item dans la case de FM
        }
    }

    // NOUVELLE FONCTION DE GESTON PERTE + PUITS ENCORE A CORRIGER UN PEU
    public static GameObject CalculPerteAndPuit (boolean succesN , int statsAdd , String statsObjectFm ,GameObject objectFm, float poidTotal, double poidUnitaire, String loi, int currentWeightTotal, Player player ) {
        String statsStr = "";
        int StatID = Integer.parseInt(statsObjectFm, 16);
        String statStringObj = objectFm.parseStatsString() ;
        int pwrPerte = 0;
        int puit = objectFm.getPuit();
        switch (loi) {
            case "exo" :
            case "normal" :
            case "over" : {
                if(succesN)
                {
                    // le cas de l'over dépend du puit restant
                    if(puit >= poidTotal) { // Si le puit peut absorber le SN il le prend
                        objectFm.setPuit(Math.round(objectFm.getPuit() - poidTotal));
                        SocketManager.GAME_SEND_MESSAGE(player, "- "+poidTotal+" puit",Constant.COULEUR_ECHEC);
                        objectFm.setNewStats(statsObjectFm,statsAdd);
                        SocketManager.GAME_SEND_MESSAGE(player, "+ "+statsAdd +" "+getEffetName(StatID),Constant.COULEUR_SUCCES);
                        return objectFm;
                    }
                    else {
                        // S'il reste du puits malgré que ca soit pas suffisant on prend dedans pour limité la perte
                        if(puit > 0) {
                            poidTotal -= puit;
                            SocketManager.GAME_SEND_MESSAGE(player, "- "+puit+" puit",Constant.COULEUR_ECHEC);
                            objectFm.setPuit(0);
                        }

                        // On perd des caractéristiques également (si c'est possible, avant d'ajouté la stats) (Caract adapté avec le puit restant)
                        if (!statStringObj.isEmpty()) {
                            statsStr = objectFm.parseStringStatsEC_FM(objectFm, poidTotal, Integer.parseInt(statsObjectFm, 16), player);
                            objectFm.clearStats();
                            objectFm.refreshStatsObjet(statsStr);
                            objectFm.setNewStats(statsObjectFm,statsAdd);
                            SocketManager.GAME_SEND_MESSAGE(player, "+ "+statsAdd +" "+getEffetName(StatID),Constant.COULEUR_SUCCES);
                        }
                        else { // Si c'est pas possible, on ajouté uniquement le nombre de stat avec le puit, arrondi au sup
                            int StatToAdd = (int)  Math.floor( (poidTotal/poidUnitaire)*statsAdd  ); // PAS FINI
                            if(StatToAdd ==0)
                                StatToAdd=1;
                            // On ajoute la stats du coup
                            objectFm.setNewStats(statsObjectFm,StatToAdd);
                            SocketManager.GAME_SEND_MESSAGE(player, "+ "+StatToAdd +" "+getEffetName(StatID),Constant.COULEUR_SUCCES);
                        }

                        return objectFm;
                    }
                }
                else {
                    // le cas de l'over dépend du puit restant
                    if(puit >= poidTotal) { // Si le puit peut absorber l'echec il le prend
                        //player.sendMessage("On a du puit, on prend dedant");
                        objectFm.setPuit(Math.round(objectFm.getPuit() - poidTotal));
                        SocketManager.GAME_SEND_MESSAGE(player, "- "+poidTotal+" puit",Constant.COULEUR_ECHEC);
                    }
                    else {
                        // Sinon on retire le puit + On perd des caractéristiques (Caract adapté avec le puit restant)
                        poidTotal -= puit;
                        SocketManager.GAME_SEND_MESSAGE(player, "- "+poidTotal+" puit",Constant.COULEUR_ECHEC);
                        objectFm.setPuit(0);

                        // Si les caracteristique ne sont pas vide
                        if (!statStringObj.isEmpty()) {
                            statsStr = objectFm.parseStringStatsEC_FM(objectFm, poidTotal, -1,player);
                            objectFm.clearStats();
                            objectFm.refreshStatsObjet(statsStr);
                            /*pwrPerte = currentWeightTotal
                                    - currentTotalWeigthBase(statsStr, objectFm);*/

                        }
                    }

                }
                break;
            }
            case "autre" : {
                // LE cas de l'exo c'est critique obligatoire ou echec, si les stats ne peuevent pas absorber l'echec on retire toutes les stats mais on ne change pas le puit
                //player.sendMessage("Pas de perte de puit car EXO tenté");

                // On perd quand meme des caractérique s'il y en as
                if (!statStringObj.isEmpty()) {
                    statsStr = objectFm.parseStringStatsEC_FM(objectFm, poidTotal, -1,player);
                    objectFm.clearStats();
                    objectFm.refreshStatsObjet(statsStr);
                }

            }
            break;
        }
        return objectFm;

    }

    // FONCTION PAS UTILISE PEUT ETRE A RETIRER
    public static void setNewPuit(boolean success, String loi, Player player, GameObject objectFm, int poid, int pwrPerte) {        // La gestion du puit était codé avec le cu

        if( !success ) {
            switch (loi) {
                case "over" : {
                    // le cas de l'over dépend du puit restant
                    if(objectFm.getPuit() > poid) { // Si le puit peut absorber l'echec ou le SN il le prend
                        objectFm.setPuit(objectFm.getPuit() - poid);
                    }
                    else { // Sinon on retire le puit + Les caractéristiques perdu (Caract adapté avec le puit restant)
                        if ( ((objectFm.getPuit() + pwrPerte) - poid) < 0) { // Si le puit + carac est tombé en dessous de 0 on le met a 0
                            objectFm.setPuit(0);
                        }
                        else {
                            objectFm.setPuit((objectFm.getPuit() + pwrPerte) - poid); // Si le puit + Carac est positif on recupÃ¨re le puit restant
                        }
                    }
                }
                case "exo" : {
                    // LE cas de l'exo c'est critique obligatoire ou echec, si les stats ne peuevent pas absorber l'echec on retire toutes les stats mais on ne change pas le puit
                    //player.sendMessage("Pas de perte de puit car EXO tenté");

                }
                case "normal" : {
                    // le cas normal dépend du puit restant aussi
                    if(objectFm.getPuit() > poid) { // Si le puit peut absorber l'echec ou le SN il le prend
                        objectFm.setPuit(objectFm.getPuit() - poid);
                    }
                    else { // Sinon on retire le puit + Les caractéristiques perdu (Caract adapté avec le puit restant)
                        if ( ((objectFm.getPuit() + pwrPerte) - poid) < 0) { // Si le puit + carac est tombé en dessous de 0 on le met a 0
                            objectFm.setPuit(0);
                        }
                        else {
                            objectFm.setPuit((objectFm.getPuit() + pwrPerte) - poid); // Si le puit + Carac est positif on recupÃ¨re le puit restant
                        }
                    }
                }
            }
        }
        else {
            player.sendMessage("Pas de perte de puit car SC");
        }
    }

    // On donne le max pour une ligne (Attention a bien gerer les négatif dans le futur)
    public static int getStatBaseMax(ObjectTemplate objMod, String statsModif) {
        String[] split = objMod.getStrTemplate().split(",");
        for (String s : split) {
            String[] stats = s.split("#");
            if (stats[0].toLowerCase().compareTo(statsModif.toLowerCase()) > 0) {
            } else if (stats[0].toLowerCase().compareTo(statsModif.toLowerCase()) == 0) {
                //World.world.logger.trace(stats[0].toLowerCase()+" ICI/ "+statsModif.toLowerCase());
                int max = Integer.parseInt(stats[2], 16);
                if (max == 0)
                    max = Integer.parseInt(stats[1], 16);
                return max;
            }
        }
        return 0;
    }

    // On donne le max pour une ligne avec le legendaire (Attention a bien gerer les négatif dans le futur)
    public static int getStatBaseMaxLegendaire(ObjectTemplate objMod, String statsModif) {
        String[] split = objMod.getStrTemplate().split(",");
        for (String s : split) {
            String[] stats = s.split("#");
            if (stats[0].toLowerCase().compareTo(statsModif.toLowerCase()) > 0) {
            } else if (stats[0].toLowerCase().compareTo(statsModif.toLowerCase()) == 0) {
                int max = 0;
                try{
                     max = Integer.parseInt(stats[2], 16);
                }
                catch(Exception e){
                    max = 0;
                    //System.out.println("1-"+ e.getMessage() + " " + objMod.getId() + " " + statsModif);
                }

                if (max == 0){
                    try{
                        max = Integer.parseInt(stats[1], 16);
                    }
                    catch(Exception e){
                        max = 0;
                        //System.out.println("2-"+ e.getMessage() + " " + objMod.getId() + " " + statsModif);
                    }
                }

                max = (int) Math.floor(max *1.5);
                return max;
            }
        }
        return 0;
    }

    // On donne le min pour une ligne de stat donné (Attention a bien gerer les négatif dans le futur)
    public static int getStatBaseMin(ObjectTemplate objMod, String statsModif) {
        String[] split = objMod.getStrTemplate().split(",");
        for (String s : split) {
            String[] stats = s.split("#");
            if (stats[0].toLowerCase().compareTo(statsModif.toLowerCase()) > 0) {
            } else if (stats[0].toLowerCase().compareTo(statsModif.toLowerCase()) == 0) {
                return Integer.parseInt(stats[1], 16);
            }
        }
        return 0;
    }

    public static int WeithTotalBaseMin(int objTemplateID) {  // Le poid de l'item en Mini
        int weight = 0;
        int alt = 0;
        String statsTemplate = "";
        statsTemplate = World.world.getObjTemplate(objTemplateID).getStrTemplate();
        if (statsTemplate == null || statsTemplate.isEmpty())
            return 0;
        String[] split = statsTemplate.split(",");
        for (String s : split) {
            String[] stats = s.split("#");
            int statID = Integer.parseInt(stats[0], 16);
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
                try {
                    int min = Integer.parseInt(stats[1], 16);
                    value = min;
                } catch (Exception e) {
                    value = Formulas.getRandomJet(jet);
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            double coef = getPwrPerEffet(statID);   // On recupÃ¨re le poid de la stat a l'unité
            weight = (int) Math.floor(value * coef);
            alt += weight;
        }
        return alt;
    }

    public static float WeithTotalBase(int objTemplateID) {   // Poid max de l'item de base
        int weight = 0;
        int alt = 0;
        String statsTemplate = "";
        statsTemplate = World.world.getObjTemplate(objTemplateID).getStrTemplate();
        if (statsTemplate == null || statsTemplate.isEmpty())
            return 0;
        String[] split = statsTemplate.split(",");
        for (String s : split) {
            String[] stats = s.split("#");
            int statID = Integer.parseInt(stats[0], 16);
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
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("L'objet "+ objTemplateID + " a une stats de base "+ statID +" qui n'a pas une valeur de jet correcte " + s + " cas a gérer");
                //value = Integer.parseInt(stats[2], 16);
            }
            double coef = getPwrPerEffet(statID);   // On recupÃ¨re le poid de la stat a l'unité
            weight = (int) Math.floor(value * coef);
            alt += weight;
        }
        return alt;
    }

    public static float WeithTotalBaseLegendary(int objTemplateID) {   // Poid max de l'item de base
        int weight = 0;
        int alt = 0;
        String statsTemplate = "";
        statsTemplate = World.world.getObjTemplate(objTemplateID).getStrTemplate();
        if (statsTemplate == null || statsTemplate.isEmpty())
            return 0;
        String[] split = statsTemplate.split(",");
        for (String s : split) {
            String[] stats = s.split("#");
            int statID = Integer.parseInt(stats[0], 16);
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
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("L'objet "+ objTemplateID + " a une stats de base "+ statID +" qui n'a pas une valeur de jet correcte " + s + " cas a gérer");
            }
            double coef = getPwrPerEffet(statID);   // On recupÃ¨re le poid de la stat a l'unité
            weight = (int) Math.round( (value + ((value)*0.5)) * coef);
            alt += weight;
        }
        return alt;
    }

    public static int currentWeithStatsExo(String statsModelo,GameObject obj) {	 // Poid des exos
        if (statsModelo.equalsIgnoreCase(""))
            return 0;
        int Weigth = 0;
        int Alto = 0;
        int rarity = obj.getRarity();
        String[] split = statsModelo.split(",");
        for (String s : split) { // On boucle sur toutes les stats de l'item de base
            String[] stats = s.split("#");
            if (stats[0].length() > 0) {
                int statID = Integer.parseInt(stats[0], 16);


                String StatsHex = Integer.toHexString(statID);
                //if (StatsHex.equals("79")) {
                //    StatsHex = "70";
                //}
                //System.out.println("On test :" + StatsHex);
                int BaseStats = viewBaseStatsItem(obj, StatsHex);

                if( (StatsHex.equals("79") || StatsHex.equals("70")) && BaseStats ==0 ) {
                    //    StatsHex = "70";
                    int stats1 = viewBaseStatsItem(obj, "79");
                    int stats2 = viewBaseStatsItem(obj, "70");

                    if(stats1>stats2){
                        BaseStats = viewBaseStatsItem(obj, "79");
                    }
                    else{
                        BaseStats = viewBaseStatsItem(obj, "70");
                    }
                }

                if (BaseStats == 2 || BaseStats == 1) {
                    continue;
                }

                if (statID == 985 || statID == 988)
                    continue;
                boolean xy = false;
                for (int a : Constant.ARMES_EFFECT_IDS)
                    if (a == statID)
                        xy = true;
                if (xy)
                    continue;
                String jet = "";
                int qua = 1;

                //System.out.println("La stat :" + statID + "considéré comme exo");
                try {
                    jet = stats[4];
                    try {
                        int min = Integer.parseInt(stats[1], 16);
                        int max = Integer.parseInt(stats[2], 16);
                        qua = min;
                        if (max != 0)
                            qua = max; // on prend la statMAX
                    } catch (Exception e) {
                        e.printStackTrace();
                        qua = Formulas.getRandomJet(jet);
                    }
                } catch (Exception e) {
                    // Ok :/
                }
                //World.world.logger.trace("Etrange cette stats"+statID);
                double coef = getPwrPerEffet(statID);   // On recupÃ¨re le poid de la stat a l'unité
                Weigth = (int) Math.round(qua * coef); // On multiplie par le jet
                Alto += Weigth;
            }
            else{
                continue;
            }
        }
        return Alto;
    }

    public static int currentTotalWeigthBase(String statsModelo, GameObject obj) { // On récupÃ¨re le poid total de l'item actuel
        if (statsModelo.equalsIgnoreCase(""))
            return 0;
        int Weigth = 0;
        int Alto = 0;
        String[] split = statsModelo.split(",");
        for (String s : split) { // On boucle sur toutes les stats de l'item de base
            String[] stats = s.split("#");
            if(stats[0].length() > 0) {
                int statID = Integer.parseInt(stats[0], 16);
                if (statID == 985 || statID == 988)
                    continue;
                boolean xy = false;
                for (int a : Constant.ARMES_EFFECT_IDS)
                    if (a == statID)
                        xy = true;
                if (xy)
                    continue;
                String jet = "";
                int qua = 1;
                try {
                    jet = stats[4];
                    try {
                        int min = Integer.parseInt(stats[1], 16);
                        int max = Integer.parseInt(stats[2], 16);
                        qua = min;
                        if (max != 0)
                            qua = max; // on prend la statMAX
                    } catch (Exception e) {
                        e.printStackTrace();
                        qua = Formulas.getRandomJet(jet);
                    }
                } catch (Exception e) {
                    // Ok :/
                }


                double coef = getPwrPerEffet(statID);   // On recupÃ¨re le poid de la stat a l'unité
                Weigth = (int) Math.floor(qua * coef); // On multiplie par le jet
                Alto += Weigth;
            }
            else{
                continue;
            }
        }
        return Alto;
    }

    public static double getPwrPerEffet(int effect) {
        double r = 0.0;
        switch (effect) {
            case EffectConstant.STATS_ADD_PA:
            case EffectConstant.STATS_ADD_PA2:
            case EffectConstant.STATS_MULTIPLY_DOMMAGE:
                r = 100.0;
                break;
            case EffectConstant.STATS_ADD_PM2:
            case EffectConstant.STATS_ADD_PM:
                r = 90.0;
                break;
            case EffectConstant.STATS_ADD_PO:
                r = 51.0;
                break;
            case EffectConstant.STATS_ADD_CC:
            case EffectConstant.STATS_CREATURE:
                r = 30.0;
                break;
            case EffectConstant.STATS_ADD_FORC:
            case EffectConstant.STATS_ADD_AGIL:
            case EffectConstant.STATS_ADD_CHAN:
            case EffectConstant.STATS_ADD_INTE:
            case EffectConstant.STATS_ADD_EC:
            case EffectConstant.STATS_ADD_AFLEE:
            case EffectConstant.STATS_ADD_MFLEE:
                r = 1.0;
                break;
            case EffectConstant.STATS_ADD_DOMA:
            case EffectConstant.STATS_ADD_DOMA2:
            case EffectConstant.STATS_RETDOM:
            case EffectConstant.STATS_ADD_SOIN:
                r = 20.0;
                break;
            case EffectConstant.STATS_TRAPDOM:
                r = 15.0;
                break;
            case EffectConstant.STATS_TRAPPER:
            case EffectConstant.STATS_ADD_PERDOM:
            case EffectConstant.STATS_ADD_PDOM:
                r = 2.0;
                break;
            case EffectConstant.STATS_ADD_VIE:
            case EffectConstant.STATS_ADD_VITA:
                r = 0.25;
                break;
            case EffectConstant.STATS_ADD_INIT:
            case EffectConstant.STATS_ADD_PODS:
                r = 0.1;
                break;
            case EffectConstant.STATS_ADD_SAGE:
            case EffectConstant.STATS_ADD_PROS:
                r = 3.0;
                break;
            case EffectConstant.STATS_ADD_RP_TER:
            case EffectConstant.STATS_ADD_RP_FEU:
            case EffectConstant.STATS_ADD_RP_NEU:
            case EffectConstant.STATS_ADD_RP_AIR:
            case EffectConstant.STATS_ADD_RP_EAU:
            case EffectConstant.STATS_ADD_RP_PVP_TER:
            case EffectConstant.STATS_ADD_RP_PVP_NEU:
            case EffectConstant.STATS_ADD_RP_PVP_EAU:
            case EffectConstant.STATS_ADD_RP_PVP_AIR:
            case EffectConstant.STATS_ADD_RP_PVP_FEU:
                r = 6.0;
                break;
            case EffectConstant.STATS_ADD_R_TER:
            case EffectConstant.STATS_ADD_R_EAU:
            case EffectConstant.STATS_ADD_R_AIR:
            case EffectConstant.STATS_ADD_R_FEU:
            case EffectConstant.STATS_ADD_R_NEU:
            case EffectConstant.STATS_ADD_R_PVP_TER:
            case EffectConstant.STATS_ADD_R_PVP_EAU:
            case EffectConstant.STATS_ADD_R_PVP_AIR:
            case EffectConstant.STATS_ADD_R_PVP_FEU:
            case EffectConstant.STATS_ADD_R_PVP_NEU:
                r = 2.0;
                break;
        }
        //System.out.println("On retourne "+r);
        return r;
    }


    public static String getEffetName(int effect) {
        String r = "";
        switch (effect) {
            case EffectConstant.STATS_ADD_PA:
            case EffectConstant.STATS_ADD_PA2:
                r = "PA";
                break;
            case EffectConstant.STATS_ADD_PM2:
            case EffectConstant.STATS_ADD_PM:
                r = "PM";
                break;
            case EffectConstant.STATS_ADD_PO:
                r = "PO";
                break;
            case EffectConstant.STATS_ADD_CC:
                r = "CC";
                break;
            case EffectConstant.STATS_CREATURE:
                r = "Créature invocable";
                break;
            case EffectConstant.STATS_ADD_FORC:
                r = "Force";
                break;
            case EffectConstant.STATS_ADD_AGIL:
                r = "Agilité";
                break;
            case EffectConstant.STATS_ADD_CHAN:
                r = "Chance";
                break;
            case EffectConstant.STATS_ADD_INTE:
                r = "Intelligence";
                break;
            case EffectConstant.STATS_ADD_EC:
                r = "Echec Critique";
                break;
            case EffectConstant.STATS_ADD_AFLEE:
                r = "% Esquive PA";
                break;
            case EffectConstant.STATS_ADD_MFLEE:
                r = "% Esquive PM";
                break;
            case EffectConstant.STATS_ADD_DOMA:
            case EffectConstant.STATS_ADD_DOMA2:
            case EffectConstant.STATS_RETDOM:
                r = "Dommage";
                break;
            case EffectConstant.STATS_ADD_SOIN:
                r = "Soin";
                break;
            case EffectConstant.STATS_TRAPDOM:
                r = "Dommage piège";
                break;
            case EffectConstant.STATS_TRAPPER:
                r = "% Dommage piège";
                break;
            case EffectConstant.STATS_ADD_PERDOM:
            case EffectConstant.STATS_ADD_PDOM:
                r = "% Dommage";
                break;
            case EffectConstant.STATS_ADD_VIE:
            case EffectConstant.STATS_ADD_VITA:
                r = "Vitalité";
                break;
            case EffectConstant.STATS_ADD_INIT:
                r = "Initiative";
                break;
            case EffectConstant.STATS_ADD_PODS:
                r = "Pods";
                break;
            case EffectConstant.STATS_ADD_SAGE:
                r = "Sagesse";
                break;
            case EffectConstant.STATS_ADD_PROS:
                r = "Prospection";
                break;
            case EffectConstant.STATS_ADD_RP_TER:
                r = "% Résistance Terre";
                break;
            case EffectConstant.STATS_ADD_RP_FEU:
                r = "% Résistance Feu";
                break;
            case EffectConstant.STATS_ADD_RP_NEU:
                r = "% Résistance Neutre";
                break;
            case EffectConstant.STATS_ADD_RP_AIR:
                r = "% Résistance Air";
                break;
            case EffectConstant.STATS_ADD_RP_EAU:
                r = "% Résistance Eau";
                break;
            case EffectConstant.STATS_ADD_RP_PVP_TER:
                r = "% Résistance Terre contre combattants";
                break;
            case EffectConstant.STATS_ADD_RP_PVP_NEU:
                r = "% Résistance Neutre contre combattants";
                break;
            case EffectConstant.STATS_ADD_RP_PVP_EAU:
                r = "% Résistance Eau contre combattants";
                break;
            case EffectConstant.STATS_ADD_RP_PVP_AIR:
                r = "% Résistance Air contre combattants";
                break;
            case EffectConstant.STATS_ADD_RP_PVP_FEU:
                r = "% Résistance Feu contre combattants";
                break;
            case EffectConstant.STATS_ADD_R_TER:
                r = "Résistance fixe Terre";
                break;
            case EffectConstant.STATS_ADD_R_EAU:
                r = "Résistance fixe Eau";
                break;
            case EffectConstant.STATS_ADD_R_AIR:
                r = "Résistance fixe Air";
                break;
            case EffectConstant.STATS_ADD_R_FEU:
                r = "Résistance fixe Feu";
                break;
            case EffectConstant.STATS_ADD_R_NEU:
                r = "Résistance fixe Neutre";
                break;
            case EffectConstant.STATS_ADD_R_PVP_TER:
                r = "Résistance fixe Terre contre combattants";
                break;
            case EffectConstant.STATS_ADD_R_PVP_EAU:
                r = "Résistance fixe Eau contre combattants";
                break;
            case EffectConstant.STATS_ADD_R_PVP_AIR:
                r = "Résistance fixe Air contre combattants";
                break;
            case EffectConstant.STATS_ADD_R_PVP_FEU:
                r = "Résistance fixe Feu contre combattants";
                break;
            case EffectConstant.STATS_ADD_R_PVP_NEU:
                r = "Résistance fixe Neutre contre combattants";
                break;
        }
        //System.out.println("On retourne "+r);
        return r;
    }


    // Nul a chier cette fonction, on va utiliser le poid max d'une ligne
   /* public static double getOverPerEffet(int effect) {
        double r = 0.0;
        switch (effect) {
            case EffectConstant.STATS_ADD_PA:
            case EffectConstant.STATS_ADD_EC:
            case EffectConstant.STATS_ADD_PM:
            case EffectConstant.STATS_ADD_AFLEE:
            case EffectConstant.STATS_ADD_MFLEE:
            case EffectConstant.STATS_ADD_PA2:
            case EffectConstant.STATS_ADD_PO:
            case EffectConstant.STATS_MULTIPLY_DOMMAGE:
                r = 0.0;
                break;
            case EffectConstant.STATS_ADD_PM2:
            case EffectConstant.STATS_ADD_VITA:
            case EffectConstant.STATS_ADD_PODS:
            case EffectConstant.STATS_ADD_VIE:
                r = 404.0;
                break;
            case EffectConstant.STATS_ADD_CC:
            case EffectConstant.STATS_CREATURE:
                r = 3.0;
                break;
            case EffectConstant.STATS_ADD_FORC:
            case EffectConstant.STATS_ADD_CHAN:
            case EffectConstant.STATS_ADD_INTE:
            case EffectConstant.STATS_ADD_AGIL:
                r = 101.0;
                break;
            case EffectConstant.STATS_ADD_DOMA:
            case EffectConstant.STATS_ADD_DOMA2:
            case EffectConstant.STATS_ADD_SOIN:
            case EffectConstant.STATS_RETDOM:
                r = 5.0;
                break;
            case EffectConstant.STATS_ADD_SAGE:
            case EffectConstant.STATS_ADD_PROS:
                r = 33.0;
                break;
            case EffectConstant.STATS_ADD_PERDOM:
            case Constant.STATS_ADD_PDOM:
            case Constant.STATS_TRAPPER:
            case Constant.STATS_ADD_R_FEU:
            case Constant.STATS_ADD_R_NEU:
            case Constant.STATS_ADD_R_TER:
            case Constant.STATS_ADD_R_EAU:
            case Constant.STATS_ADD_R_AIR:
            case Constant.STATS_ADD_R_PVP_EAU:
            case Constant.STATS_ADD_R_PVP_AIR:
            case Constant.STATS_ADD_R_PVP_FEU:
            case Constant.STATS_ADD_R_PVP_NEU:
            case Constant.STATS_ADD_R_PVP_TER:
                r = 50.0;
                break;
            case EffectConstant.STATS_ADD_INIT:
                r = 1010.0;
                break;
            case Constant.STATS_ADD_RP_TER:
            case Constant.STATS_ADD_RP_PVP_FEU:
            case Constant.STATS_ADD_RP_PVP_AIR:
            case Constant.STATS_ADD_RP_PVP_NEU:
            case Constant.STATS_ADD_RP_PVP_TER:
            case Constant.STATS_ADD_RP_PVP_EAU:
            case Constant.STATS_ADD_RP_NEU:
            case Constant.STATS_ADD_RP_FEU:
            case Constant.STATS_ADD_RP_AIR:
            case Constant.STATS_ADD_RP_EAU:
                r = 16.0;
                break;
            case Constant.STATS_TRAPDOM:
                r = 6.0;
                break;
        }
        //System.out.println("LE poid trouvé "+r);
        return r;
    }
    */
    public static int getBaseMaxJet(int templateID, String statsModif) {
        ObjectTemplate t = World.world.getObjTemplate(templateID);
        String[] splitted = t.getStrTemplate().split(",");
        for (String s : splitted) {
            String[] stats = s.split("#");
            if (stats[0].compareTo(statsModif) > 0)//Effets n'existe pas de base
            {
            } else if (stats[0].compareTo(statsModif) == 0)//L'effet existe bien !
            {

                int max = Integer.parseInt(stats[2], 16);

                if (max == 0)
                    max = Integer.parseInt(stats[1], 16);//Pas de jet maximum on prend le minimum
                return max;
            }
        }
        return 0;
    }

    public static int getActualJet(GameObject obj, String statsModif) {
        for (Entry<Integer, Integer> entry : obj.getStats().getMap().entrySet()) {
            //World.world.logger.trace(Integer.toHexString(entry.getKey())+" / "+statsModif);

            if (Integer.toHexString(entry.getKey()).compareTo(statsModif) > 0)//Effets inutiles
            {

            }
            else if (Integer.toHexString(entry.getKey()).compareTo(statsModif) == 0)//L'effet existe bien !
            {
                int JetActual = entry.getValue();
                return JetActual;
            }
        }
        return 0;
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

            //System.out.println("On compare :" + stats[0] + "a " + ItemStats);

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
                    return 2; // Retourne oui mais c'est un négatif
                } else if (stats[0].compareTo("99") == 0
                        && ItemStats.compareTo("7d") == 0) {
                    return 2; // Retourne oui mais c'est un négatif
                } else {
                }
            } else if (stats[0].compareTo(ItemStats) == 0)//L'effet existe bien !
            {
                return 1;
            }
        }
        return 0; // Retourne faux
    }
}