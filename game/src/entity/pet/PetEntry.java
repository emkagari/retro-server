package entity.pet;

import client.Player;
import common.Formulas;
import common.SocketManager;
import database.Database;
import game.world.World;
import kernel.Constant;
import object.GameObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

public class PetEntry {

    private Long objectId;
    private int template;
    private long lastEatDate;
    private int quaEat;
    private int pdv;
    private int Poids;
    private int corpulence;
    private boolean isEupeoh;

    public PetEntry(Long Oid, int template, long lastEatDate, int quaEat,
                    int pdv, int corpulence, boolean isEPO) {
        this.objectId = Oid;
        this.template = template;
        this.lastEatDate = lastEatDate;
        this.quaEat = quaEat;
        this.pdv = pdv;
        this.corpulence = corpulence;
        getCurrentStatsPoids();
        this.isEupeoh = isEPO;
    }

    public Long getObjectId() {
        return this.objectId;
    }

    public void setObjectId(long ObjGUID) {
        this.objectId = ObjGUID;
    }

    public int getTemplate() {
        return template;
    }

    public long getLastEatDate() {
        return this.lastEatDate;
    }

    public int getQuaEat() {
        return this.quaEat;
    }

    public int getPdv() {
        return this.pdv;
    }

    public int getCorpulence() {
        return this.corpulence;
    }

    public boolean getIsEupeoh() {
        return this.isEupeoh;
    }

    public void setIsEupeoh(boolean EPO) {
        this.isEupeoh = EPO;
    }


    public String parseLastEatDate() {
        String hexDate = "#";
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            String date = formatter.format(this.lastEatDate);
            String[] split = date.split("\\s");
            String[] split0 = split[0].split("-");
            hexDate += Integer.toHexString(Integer.parseInt(split0[0])) + "#";
            int mois = Integer.parseInt(split0[1]) - 1;
            int jour = Integer.parseInt(split0[2]);
            hexDate += Integer.toHexString(Integer.parseInt((mois < 10 ? "0" + mois : mois)
                    + "" + (jour < 10 ? "0" + jour : jour)))
                    + "#";

            String[] split1 = split[1].split(":");
            String heure = split1[0] + split1[1];
            hexDate += Integer.toHexString(Integer.parseInt(heure));
        }
        catch(Exception e){
            String date = formatter.format(System.currentTimeMillis());
            String[] split = date.split("\\s");
            String[] split0 = split[0].split("-");
            hexDate += Integer.toHexString(Integer.parseInt(split0[0])) + "#";
            int mois = Integer.parseInt(split0[1]) - 1;
            int jour = Integer.parseInt(split0[2]);
            hexDate += Integer.toHexString(Integer.parseInt((mois < 10 ? "0" + mois : mois)
                    + "" + (jour < 10 ? "0" + jour : jour)))
                    + "#";

            String[] split1 = split[1].split(":");
            String heure = split1[0] + split1[1];
            hexDate += Integer.toHexString(Integer.parseInt(heure));
        }
        return hexDate;
    }

    public int parseCorpulence() {
        if (corpulence > 0 || corpulence < 0)
            return 7;
        return 0;
    }

    public int getCurrentStatsPoids() {
        /*
         * d6,d5,d4,d3,d2 = 4U de poids 8a = 2U de poids 7c = 2U de poids POUR
         * PETIT WABBIT = 3U de poids b2 = 8U de poids 70 = 8U de poids le reste
         * a 1U de poids
         */
        GameObject obj = World.world.getGameObject(this.objectId);
        if (obj == null)
            return 0;
        int cumul = 0;
        for (Entry<Integer, Integer> entry : obj.getStats().getEffects().entrySet()) {
            cumul = (int) (cumul + Math.round((double)Formulas.getWeightByStat(entry.getKey()) * (double)entry.getValue()));
        }
        this.Poids = cumul;
        return this.Poids;
    }

    public int getMaxStat() {
        return World.world.getPets(this.template).getMax();
    }

    public void looseFight(Player player) {
        GameObject obj = World.world.getGameObject(this.objectId);
        if (obj == null)
            return;
        Pet pets = World.world.getPets(obj.getTemplate().getId());
        if (pets == null)
            return;

        this.pdv--;
        obj.getTxtStat().remove(Constant.STATS_PETS_PDV);
        obj.getTxtStat().put(Constant.STATS_PETS_PDV, Integer.toHexString((this.pdv > 0 ? (this.pdv) : 0)));

        if (this.pdv <= 0) {
            this.pdv = 0;
            obj.getTxtStat().remove(Constant.STATS_PETS_PDV);
            obj.getTxtStat().put(Constant.STATS_PETS_PDV, Integer.toHexString(0));//Mise a 0 des pdv

            if (pets.getDeadTemplate() == 0)// Si Pets DeadTemplate = 0 remove de l'item et pet entry
            {
                World.world.removeGameObject(obj.getGuid());
                player.removeItem(obj.getGuid());
                SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(player, obj.getGuid());
                if (player.addObjet(obj, true))//Si le joueur n'avait pas d'item similaire
                    World.world.addGameObject(obj, true);
            } else {
                obj.setTemplate(pets.getDeadTemplate());
                if (obj.getPosition() == Constant.ITEM_POS_FAMILIER) {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                    SocketManager.GAME_SEND_OBJET_MOVE_PACKET(player, obj);
                }
            }
            SocketManager.GAME_SEND_Im_PACKET(player, "154");
        }
        SocketManager.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(player, obj);
        Database.getStatics().getPetData().update(this);
    }

    public void eat(Player p, int min, int max, int statsID, GameObject feed) {
        GameObject obj = World.world.getGameObject(this.objectId);
        if (obj == null)
            return;
        Pet pets = World.world.getPets(obj.getTemplate().getId());
        if (pets == null)
            return;

        //Update du petsEntry
        this.lastEatDate = System.currentTimeMillis();
        this.corpulence = 0;
        if (statsID != 0)
            this.quaEat++;
        else
            return;
        if (this.quaEat >= 1) {
            //Update de l'item
            if ((this.getIsEupeoh() ? pets.getMax() * 1.5 : pets.getMax()) > this.getCurrentStatsPoids())//Si il est sous l'emprise d'EPO on augmente de +50% le jet maximum
            {
                if (obj.getStats().getEffects().containsKey(statsID)) {
                    int value = obj.getStats().getEffects().get(statsID)
                            + World.world.getPets(World.world.getGameObject(this.objectId).getTemplate().getId()).getGain();
                    if (value > this.getMaxStat())
                        value = this.getMaxStat();
                    obj.getStats().getEffects().remove(statsID);
                    obj.getStats().addOneStat(statsID, value);
                } else
                    obj.getStats().addOneStat(statsID, pets.getGain());
            }
            this.quaEat = 0;
        }
        SocketManager.GAME_SEND_Im_PACKET(p, "032");

        if (this.pdv <= 0) {
            this.pdv = 0;
            obj.getTxtStat().remove(Constant.STATS_PETS_PDV);
            obj.getTxtStat().put(Constant.STATS_PETS_PDV, Integer.toHexString((this.pdv > 0 ? (this.pdv) : 0)));//Mise a 0 des pdv
            if (pets.getDeadTemplate() == 0)// Si Pets DeadTemplate = 0 remove de l'item et pet entry
            {
                World.world.removeGameObject(obj.getGuid());
                p.removeItem(obj.getGuid());
                SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(p, obj.getGuid());
            } else {
                obj.setTemplate(pets.getDeadTemplate());

                if (obj.getPosition() == Constant.ITEM_POS_FAMILIER) {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                    SocketManager.GAME_SEND_OBJET_MOVE_PACKET(p, obj);
                }
            }
            SocketManager.GAME_SEND_Im_PACKET(p, "154");
        }
        if (obj.getTxtStat().containsKey(Constant.STATS_PETS_REPAS)) {
            obj.getTxtStat().remove(Constant.STATS_PETS_REPAS);
            obj.getTxtStat().put(Constant.STATS_PETS_REPAS, Integer.toHexString(feed.getTemplate().getId()));
        } else {
            obj.getTxtStat().put(Constant.STATS_PETS_REPAS, Integer.toHexString(feed.getTemplate().getId()));
        }
        SocketManager.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(p, obj);
        Database.getStatics().getObjectData().update(obj);
        Database.getStatics().getPetData().update(this);
    }

    public void eatSouls(Player p, Map<Integer, Integer> souls) {
        GameObject obj = World.world.getGameObject(this.objectId);
        if (obj == null)
            return;
        Pet pet = World.world.getPets(obj.getTemplate().getId());
        if (pet == null || (pet.getType() != 1))
            return;

        //Ajout a l'item les SoulStats tu�s
        try {
                for (Entry<Integer, Integer> entry : souls.entrySet()) {
                    int soul = entry.getKey();
                    int count = entry.getValue();
                    if (pet.canEat(-1, -1, soul)) {
                        int statsID = pet.statsIdByEat(-1, -1, soul);
                        if (statsID == 0)
                            return;
                        int soulCount = (obj.getSoulStat().get(soul) != null ? obj.getSoulStat().get(soul) : 0);
                        if (soulCount > 0) {
                            obj.getSoulStat().remove(soul);
                            obj.getSoulStat().put(soul, count + soulCount);
                        } else {
                            obj.getSoulStat().put(soul, count);
                        }
                    }
                }
                //Re-Calcul des points gagn�s
                for (Entry<Integer, ArrayList<Map<Integer, Integer>>> ent : pet.getMonsters().entrySet()) {
                    for (Map<Integer, Integer> entry : ent.getValue()) {
                        for (Entry<Integer, Integer> monsterEntry : entry.entrySet()) {
                            if (pet.getNumbMonster(ent.getKey(), monsterEntry.getKey()) != 0) {
                                int pts = 0;

                                for (Entry<Integer, Integer> list : obj.getSoulStat().entrySet())
                                    pts += ((int) Math.floor(list.getValue() / pet.getNumbMonster(ent.getKey(), list.getKey())) * (pet.getGain()*3));

                                if (pts > 0) {
                                    if (pts > Math.round(this.getMaxStat()/Formulas.getWeightByStat(ent.getKey())) )
                                        pts = (int) Math.round(this.getMaxStat()/Formulas.getWeightByStat(ent.getKey()));

                                    if (obj.getStats().getEffects().containsKey(ent.getKey())) {
                                        int nbr = obj.getStats().getEffects().get(ent.getKey());
                                        if (nbr - pts > 0)
                                            pts += (nbr - pts);
                                        obj.getStats().getEffects().remove(ent.getKey());
                                    }
                                    obj.getStats().getEffects().put(ent.getKey(), pts);
                                }
                            }
                        }
                    }
                }

        } catch(Exception e) {
            e.printStackTrace();
            //System.out.println("Error : " + e.getMessage());
        }
        SocketManager.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(p, obj);
        Database.getStatics().getObjectData().update(obj);
        Database.getStatics().getPetData().update(this);
    }

    public void updatePets(Player p, int max) {
        GameObject obj = World.world.getGameObject(this.objectId);
        //System.out.println(this.objectId  + "deès le debut " + obj.parseStatsString()  );
        if (obj == null)
            return;
        Pet pets = World.world.getPets(obj.getTemplate().getId());
        if (pets == null)
            return;
        if (this.pdv <= 0
                && obj.getTemplate().getId() == pets.getDeadTemplate())
            return;//Ne le met pas a jour si deja mort

        if (this.lastEatDate + (max * 3600000) < System.currentTimeMillis())//Oublier de le nourrir
        {
            //On calcul le nombre de repas oublier arrondi au sup�rieur :
            //int nbrepas = (int) Math.floor((System.currentTimeMillis() - this.lastEatDate)
            //        / (max * 3600000));

            //Perte corpulence
            //this.corpulence = this.corpulence - nbrepas;

            //if (nbrepas != 0) {
            //    obj.getTxtStat().remove(Constant.STATS_PETS_POIDS);
            //    obj.getTxtStat().put(Constant.STATS_PETS_POIDS, Integer.toString(this.corpulence));
            //}
            //Perte pdv
            //this.pdv--;
            SocketManager.GAME_SEND_Im_PACKET(p, "025");
            //obj.getTxtStat().remove(Constant.STATS_PETS_PDV);
            //obj.getTxtStat().put(Constant.STATS_PETS_PDV, Integer.toHexString((this.pdv > 0 ? (this.pdv) : 0)));
            this.lastEatDate = System.currentTimeMillis();
        } else {
            if (this.pdv > 0)
                SocketManager.GAME_SEND_Im_PACKET(p, "025");
        }

        if (this.pdv <= 0) {
            this.pdv = 0;
            obj.getTxtStat().remove(Constant.STATS_PETS_PDV);
            obj.getTxtStat().put(Constant.STATS_PETS_PDV, Integer.toHexString((this.pdv > 0 ? (this.pdv) : 0)));

            if (pets.getDeadTemplate() == 0)//Si Pets DeadTemplate = 0 remove de l'item et pet entry
            {
                World.world.removeGameObject(obj.getGuid());
                p.removeItem(obj.getGuid());
                SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(p, obj.getGuid());
            } else {
                obj.setTemplate(pets.getDeadTemplate());
                if (obj.getPosition() == Constant.ITEM_POS_FAMILIER) {
                    obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
                    SocketManager.GAME_SEND_OBJET_MOVE_PACKET(p, obj);
                }
            }
            SocketManager.GAME_SEND_Im_PACKET(p, "154");
        }


        SocketManager.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(p, obj);

        Database.getStatics().getObjectData().update(obj);
        Database.getStatics().getPetData().update(this);
    }

    public void resurrection() {
        GameObject obj = World.world.getGameObject(this.objectId);
        if (obj == null)
            return;

        obj.setTemplate(this.template);

        this.pdv = 1;
        this.corpulence = 0;
        this.quaEat = 0;
        this.lastEatDate = System.currentTimeMillis();

        obj.getTxtStat().remove(Constant.STATS_PETS_PDV);
        obj.getTxtStat().put(Constant.STATS_PETS_PDV, Integer.toHexString(this.pdv));
        Database.getStatics().getObjectData().update(obj);
        Database.getStatics().getPetData().update(this);
    }

    public void restoreLife(Player p) {
        GameObject obj = World.world.getGameObject(this.objectId);
        if (obj == null)
            return;
        Pet pets = World.world.getPets(obj.getTemplate().getId());
        if (pets == null)
            return;

        if (this.pdv >= 10) {
            //Il la mange pas de pdv en plus
            SocketManager.GAME_SEND_Im_PACKET(p, "032");
        } else if (this.pdv < 10 && this.pdv > 0) {
            this.pdv++;

            obj.getTxtStat().remove(Constant.STATS_PETS_PDV);
            obj.getTxtStat().put(Constant.STATS_PETS_PDV, Integer.toHexString(this.pdv));

            //this.lastEatDate = System.currentTimeMillis();
            SocketManager.GAME_SEND_Im_PACKET(p, "032");
        } else {
            return;
        }
        Database.getStatics().getObjectData().update(obj);
        Database.getStatics().getPetData().update(this);
    }

    public void giveEpo(Player p) {
        GameObject obj = World.world.getGameObject(this.objectId);
        if (obj == null)
            return;
        Pet pets = World.world.getPets(obj.getTemplate().getId());
        if (pets == null)
            return;
        if (this.isEupeoh)
            return;

        this.setIsEupeoh(true);
        obj.getTxtStat().put(Constant.STATS_PETS_EPO, Integer.toHexString(1));
        SocketManager.GAME_SEND_Im_PACKET(p, "032");
        SocketManager.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(p, obj);
        Database.getStatics().getPetData().update(this);
    }
}