package guild;

import area.map.entity.House;
import client.Player;
import database.Database;
import entity.Collector;
import fight.spells.EffectConstant;
import fight.spells.SpellGrade;
import game.world.World;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class Guild {

    private int id;
    private long xp, date;
    private String name = "", emblem = "", announce = "";
    private int lvl, capital = 0, nbCollectors = 0;
    private Map<Integer, GuildMember> members = new TreeMap<>();
    private Map<Integer, SpellGrade> spells = new HashMap<>(); // <Id, Level>
    private Map<Integer, Integer> stats = new HashMap<>(); // <Effect, Quantity>
    private Map<Short, Long> delayCollectors = new HashMap<>();

    public Guild(String name, String emblem) {
        this.id = Database.getStatics().getGuildData().getNextId();
        this.name = name;
        this.emblem = emblem;
        this.lvl = 1;
        this.xp = 0;
        this.date = System.currentTimeMillis();
        this.decompileSpell("462;0|461;0|460;0|459;0|458;0|457;0|456;0|455;0|454;0|453;0|452;0|451;0|");
        this.decompileStats("176;100|158;1000|124;100|");
        this.announce = "";
    }

    public Guild(int id, String name, String emblem, int lvl, long xp, int capital, int nbCollectors, String sorts, String stats, long date, String announce) {
        this.id = id;
        this.name = name;
        this.emblem = emblem;
        this.xp = xp;
        this.lvl = lvl;
        this.capital = capital;
        this.nbCollectors = nbCollectors;
        this.date = date;
        this.decompileSpell(sorts);
        this.decompileStats(stats);
        this.announce = announce;
    }

    public GuildMember addMember(int id, int r, byte pXp, long x, int ri, String lastCo) {
        Player player = World.world.getPlayer(id);
        if (player == null) return null;
        GuildMember guildMember = new GuildMember(player, this, r, x, pXp, ri, lastCo);
        this.members.put(id, guildMember);
        player.setGuildMember(guildMember);
        return guildMember;
    }

    public GuildMember addNewMember(Player player) {
        GuildMember guildMember = new GuildMember(player, this, 0, 0, (byte) 0, 0, player.getAccount().getLastConnectionDate());
        this.members.put(player.getId(), guildMember);
        player.setGuildMember(guildMember);
        return guildMember;
    }

    public int getId() {
        return this.id;
    }

    public String getAnnounce() {return  this.announce;}

    public void setAnnounce(String texte) { this.announce = texte;}

    public int getNbCollectors() {
        return this.nbCollectors;
    }

    public void setNbCollectors(int nbr) {
        this.nbCollectors = nbr;
    }

    public int getCapital() {
        return this.capital;
    }

    public void setCapital(int nbr) {
        this.capital = nbr;
    }

    public Map<Integer, SpellGrade> getSpells() {
        return this.spells;
    }

    public Map<Integer, Integer> getStats() {
        return stats;
    }

    public long getDate() {
        return date;
    }

    public void boostSpell(int id) {
        SpellGrade SS = this.spells.get(id);
        if (SS != null && SS.getLevel() == 5)
            return;
        this.spells.put(id, ((SS == null) ? World.world.getSort(id).getStatsByLevel(1) : World.world.getSort(id).getStatsByLevel(SS.getLevel() + 1)));
    }

    public boolean unBoostSpell(int id) {
        SpellGrade SS = this.spells.get(id);
        if (SS != null) {
            this.capital += 5 * SS.getLevel();
            this.spells.put(id, null);
            return true;
        }
        return false;
    }

    public Map<Short, Long> getDelayCollectors() {
        return delayCollectors;
    }


    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmblem() {
        return this.emblem;
    }

    public long getXp() {
        return this.xp;
    }

    public int getLvl() {
        return this.lvl;
    }

    public boolean haveTenMembers() {
        return this.id == 1 || this.id == 2 || (this.members.size() >= 10);
    }

    public List<Player> getPlayers() {
        List<Player> players = this.members.values().stream().filter(guildMember -> guildMember.getPlayer() != null).map(GuildMember::getPlayer).collect(Collectors.toList());
        return players;
    }

    public Collection<GuildMember> getMembers() {
        Collection<GuildMember> players =  this.members.values();
        return players;
    }

    public GuildMember getMember(int id) {
        GuildMember guildMember = this.members.get(id);
        if(guildMember == null)
            Database.getDynamics().getGuildMemberData().load(id);
        return this.members.get(id) == null ? this.members.get(id) : guildMember;
    }

    public void removeMember(Player player) {
        House house = World.world.getHouseManager().getHouseByPerso(player);
        if (house != null)
            if (World.world.getHouseManager().houseOnGuild(this.id) > 0)
                Database.getDynamics().getHouseData().updateGuild(house, 0, 0);

        this.members.remove(player.getId());
        Database.getDynamics().getGuildMemberData().delete(player.getId());
    }

    public void addXp(long xp) {
        this.xp += xp;
        while (this.xp >= World.world.getGuildXpMax(this.lvl) && this.lvl < 200) this.levelUp();
    }

    private void levelUp() {
        this.lvl++;
        this.capital += 5;
    }

    private void decompileSpell(String spells) {
        for (String split : spells.split("\\|"))
            this.spells.put(Integer.parseInt(split.split(";")[0]), World.world.getSort(Integer.parseInt(split.split(";")[0])).getStatsByLevel(Integer.parseInt(split.split(";")[1])));
    }

    public String compileSpell() {
        if (this.spells.isEmpty())
            return "";

        StringBuilder toReturn = new StringBuilder();
        boolean isFirst = true;

        for (Entry<Integer, SpellGrade> curSpell : this.spells.entrySet()) {
            if (!isFirst)
                toReturn.append("|");
            toReturn.append(curSpell.getKey()).append(";").append(((curSpell.getValue() == null) ? 0 : curSpell.getValue().getLevel()));
            isFirst = false;
        }

        return toReturn.toString();
    }

    private void decompileStats(String statsStr) {
        for (String split : statsStr.split("\\|"))
            this.stats.put(Integer.parseInt(split.split(";")[0]), Integer.parseInt(split.split(";")[1]));
    }

    public String compileStats() {
        if (this.stats.isEmpty())
            return "";

        StringBuilder toReturn = new StringBuilder();
        boolean isFirst = true;

        for (Entry<Integer, Integer> curStats : this.stats.entrySet()) {
            if (!isFirst)
                toReturn.append("|");

            toReturn.append(curStats.getKey()).append(";").append(curStats.getValue());

            isFirst = false;
        }

        return toReturn.toString();
    }

    public void upgradeStats(int id, int add) {
        this.stats.put(id, (this.stats.get(id) + add));
    }

    public int resetStats(int id) {
        int quantity = this.stats.get(id);
        this.stats.put(id, 0);
        return quantity;
    }

    public int getStats(int id) {
        return stats.get(id);
    }

    //region Parse packet
    public String parseCollectorToGuild() {
        return String.valueOf(getNbCollectors()) + "|" + Collector.countCollectorGuild(getId()) + "|" + 100 * getLvl() + "|" + getLvl() + "|" + getStats(158) + "|" + getStats(176) + "|" + getStats(124) + "|" + getNbCollectors() + "|" + getCapital() + "|" + (1000 + (10 * getLvl())) + "|" + compileSpell();
    }

    public String parseQuestionTaxCollector() {
        return "1" + ';' + getName() + ',' + getStats(EffectConstant.STATS_ADD_PODS) + ',' + getStats(EffectConstant.STATS_ADD_PROS) + ',' + getStats(EffectConstant.STATS_ADD_SAGE) + ',' + getNbCollectors();
    }

    public String parseMembersToGM() {
        StringBuilder str = new StringBuilder();
        for (GuildMember GM : this.members.values()) {
            String online = "0";
            if (GM.getPlayer() != null)
                if (GM.getPlayer().isOnline())
                    online = "1";
            if (str.length() != 0)
                str.append("|");
            str.append(GM.getPlayerId()).append(";");
            str.append(GM.getName()).append(";");
            str.append(GM.getLvl()).append(";");
            str.append(GM.getGfx()).append(";");
            str.append(GM.getRank()).append(";");
            str.append(GM.getXpGave()).append(";");
            str.append(GM.getXpGive()).append(";");
            str.append(GM.getRights()).append(";");
            str.append(online).append(";");
            str.append(GM.getAlign()).append(";");
            str.append(GM.getHoursFromLastCo());
        }
        return str.toString();
    }

    //endregion
}
