package database.statics.data;

import client.Account;
import client.Player;
import com.zaxxer.hikari.HikariDataSource;
import command.administration.Group;
import database.Database;
import database.statics.AbstractDAO;
import fight.spells.EffectConstant;
import game.world.World;
import kernel.Config;
import kernel.Main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class PlayerData extends AbstractDAO<Player> {

    public PlayerData(HikariDataSource dataSource) {
        super(dataSource);
    }

    public int getNextId() {
        String query = "SELECT id FROM players ORDER BY id DESC LIMIT 1";
        try (Result result = getData(query)) {
            if (result != null && result.getResultSet() != null) {
                ResultSet RS = result.getResultSet();
                if (!RS.first()) {
                    return 1;
                } else {
                    return RS.getInt("id") + 1;
                }
            }
        } catch (SQLException e) {
            sendError("PlayerData getNextId", e);
        }
        return 0;
    }

    public void load() {
        String query = "SELECT * FROM players";
        try (Result result = getData(query)) {
            if (result != null && result.getResultSet() != null) {
                ResultSet RS = result.getResultSet();
                while (RS.next()) {
                    if (RS.getInt("server") != Config.INSTANCE.getSERVER_ID())
                        continue;

                    HashMap<Integer, Integer> stats = new HashMap<>();
                    stats.put(EffectConstant.STATS_ADD_VITA, RS.getInt("vitalite"));
                    stats.put(EffectConstant.STATS_ADD_FORC, RS.getInt("force"));
                    stats.put(EffectConstant.STATS_ADD_SAGE, RS.getInt("sagesse"));
                    stats.put(EffectConstant.STATS_ADD_INTE, RS.getInt("intelligence"));
                    stats.put(EffectConstant.STATS_ADD_CHAN, RS.getInt("chance"));
                    stats.put(EffectConstant.STATS_ADD_AGIL, RS.getInt("agilite"));

                    Player perso = new Player(
                            RS.getInt("id"), RS.getString("name"), RS.getInt("groupe"), RS.getInt("sexe"),
                            RS.getInt("class"), RS.getInt("color1"), RS.getInt("color2"), RS.getInt("color3"),
                            RS.getLong("kamas"), RS.getInt("spellboost"), RS.getInt("capital"), RS.getInt("energy"),
                            RS.getInt("level"), RS.getLong("xp"), RS.getInt("size"), RS.getInt("gfx"), RS.getByte("alignement"),
                            RS.getInt("account"), stats, RS.getByte("seeFriend"), RS.getByte("seeAlign"), RS.getByte("seeSeller"),
                            RS.getString("canaux"), RS.getShort("map"), RS.getInt("cell"), RS.getString("objets"),
                            RS.getString("storeObjets"), RS.getInt("pdvper"), RS.getString("spells"), RS.getString("savepos"),
                            RS.getString("jobs"), RS.getInt("mountxpgive"), RS.getInt("mount"), RS.getInt("honor"),
                            RS.getInt("deshonor"), RS.getInt("alvl"), RS.getString("zaaps"), RS.getByte("title"),
                            RS.getInt("wife"), RS.getString("morphMode"), RS.getString("allTitle"), RS.getString("emotes"),
                            RS.getLong("prison"), false, RS.getString("parcho"), RS.getLong("timeDeblo"), RS.getBoolean("noall"),
                            RS.getString("deadInformation"), RS.getByte("needRestat"), RS.getLong("totalKills"), RS.getInt("isParcho")
                    );

                    try { perso.parseInventoryShortcuts(RS.getString("inventory_shortcuts")); }
                    catch (SQLException ignored) {}
                    World.world.addPlayer(perso);
                    if (perso.isShowSeller())
                        World.world.addSeller(perso);

                    if (RS.getInt("vitalite") > 2091 || RS.getInt("force") > 465 || RS.getInt("sagesse") > 432 ||
                            RS.getInt("intelligence") > 465 || RS.getInt("chance") > 465 || RS.getInt("agilite") > 465) {
                        //perso.banAccount();
                        //World.sendWebhookMessage(Config.INSTANCE.getDISCORD_CHANNEL_FAILLE(), "TO BAN : Suspicion de Tentative de faille statistique, supérieurs aux jets maximum", perso);
                    }
                }
            }
        } catch (SQLException e) {
            sendError("PlayerData load", e);
            Main.INSTANCE.stop("unknown");
        }
    }

    public Player load(int obj) {
        String query = "SELECT * FROM players WHERE id = '" + obj + "'";
        try (Result result = getData(query)) {
            if (result != null && result.getResultSet() != null) {
                ResultSet RS = result.getResultSet();
                while (RS.next()) {
                    if (RS.getInt("server") != Config.INSTANCE.getSERVER_ID())
                        continue;

                    HashMap<Integer, Integer> stats = new HashMap<>();
                    stats.put(EffectConstant.STATS_ADD_VITA, RS.getInt("vitalite"));
                    stats.put(EffectConstant.STATS_ADD_FORC, RS.getInt("force"));
                    stats.put(EffectConstant.STATS_ADD_SAGE, RS.getInt("sagesse"));
                    stats.put(EffectConstant.STATS_ADD_INTE, RS.getInt("intelligence"));
                    stats.put(EffectConstant.STATS_ADD_CHAN, RS.getInt("chance"));
                    stats.put(EffectConstant.STATS_ADD_AGIL, RS.getInt("agilite"));

                    Player oldPlayer = World.world.getPlayer((int) obj);
                    Player player = new Player(
                            RS.getInt("id"), RS.getString("name"), RS.getInt("groupe"), RS.getInt("sexe"),
                            RS.getInt("class"), RS.getInt("color1"), RS.getInt("color2"), RS.getInt("color3"),
                            RS.getLong("kamas"), RS.getInt("spellboost"), RS.getInt("capital"), RS.getInt("energy"),
                            RS.getInt("level"), RS.getLong("xp"), RS.getInt("size"), RS.getInt("gfx"), RS.getByte("alignement"),
                            RS.getInt("account"), stats, RS.getByte("seeFriend"), RS.getByte("seeAlign"), RS.getByte("seeSeller"),
                            RS.getString("canaux"), RS.getShort("map"), RS.getInt("cell"), RS.getString("objets"),
                            RS.getString("storeObjets"), RS.getInt("pdvper"), RS.getString("spells"), RS.getString("savepos"),
                            RS.getString("jobs"), RS.getInt("mountxpgive"), RS.getInt("mount"), RS.getInt("honor"),
                            RS.getInt("deshonor"), RS.getInt("alvl"), RS.getString("zaaps"), RS.getByte("title"),
                            RS.getInt("wife"), RS.getString("morphMode"), RS.getString("allTitle"), RS.getString("emotes"),
                            RS.getLong("prison"), false, RS.getString("parcho"), RS.getLong("timeDeblo"), RS.getBoolean("noall"),
                            RS.getString("deadInformation"), RS.getByte("needRestat"), RS.getLong("totalKills"), RS.getInt("isParcho")
                    );

                    if (oldPlayer != null)
                        player.setNeededEndFight(oldPlayer.needEndFight(), oldPlayer.hasMobGroup());

                    player.VerifAndChangeItemPlace();
                    // Inventory shortcuts (1.43.7) — colonne optionnelle, on protège des
                    // anciennes DB sans cette colonne.
                    try { player.parseInventoryShortcuts(RS.getString("inventory_shortcuts")); }
                    catch (SQLException ignored) {}
                    World.world.addPlayer(player);
                    int guild = Database.getDynamics().getGuildMemberData().isPersoInGuild(RS.getInt("id"));

                    if (guild >= 0)
                        player.setGuildMember(World.world.getGuild(guild).getMember(RS.getInt("id")));

                    return player; // Return here as we found the player
                }
            }
        } catch (SQLException e) {
            sendError("PlayerData load id", e);
            Main.INSTANCE.stop("unknown");
        }
        return null;
    }

    public void loadByAccountId(int id) {
        Account account = World.world.getAccount(id);
        if (account != null && account.getPlayers() != null) {
            account.getPlayers().values().stream()
                    .filter(p -> p != null)
                    .forEach(World.world::verifyClone);
        }

        String query = "SELECT * FROM players WHERE account = '" + id + "'";
        try (Result result = getData(query)) {
            if (result != null && result.getResultSet() != null) {
                ResultSet RS = result.getResultSet();
                while (RS.next()) {
                    if (RS.getInt("server") != Config.INSTANCE.getSERVER_ID())
                        continue;

                    Player p = World.world.getPlayer(RS.getInt("id"));
                    if (p != null && p.getFight() != null) {
                        continue;
                    }

                    HashMap<Integer, Integer> stats = new HashMap<>();
                    stats.put(EffectConstant.STATS_ADD_VITA, RS.getInt("vitalite"));
                    stats.put(EffectConstant.STATS_ADD_FORC, RS.getInt("force"));
                    stats.put(EffectConstant.STATS_ADD_SAGE, RS.getInt("sagesse"));
                    stats.put(EffectConstant.STATS_ADD_INTE, RS.getInt("intelligence"));
                    stats.put(EffectConstant.STATS_ADD_CHAN, RS.getInt("chance"));
                    stats.put(EffectConstant.STATS_ADD_AGIL, RS.getInt("agilite"));

                    Player player = new Player(
                            RS.getInt("id"), RS.getString("name"), RS.getInt("groupe"), RS.getInt("sexe"),
                            RS.getInt("class"), RS.getInt("color1"), RS.getInt("color2"), RS.getInt("color3"),
                            RS.getLong("kamas"), RS.getInt("spellboost"), RS.getInt("capital"), RS.getInt("energy"),
                            RS.getInt("level"), RS.getLong("xp"), RS.getInt("size"), RS.getInt("gfx"), RS.getByte("alignement"),
                            RS.getInt("account"), stats, RS.getByte("seeFriend"), RS.getByte("seeAlign"), RS.getByte("seeSeller"),
                            RS.getString("canaux"), RS.getShort("map"), RS.getInt("cell"), RS.getString("objets"),
                            RS.getString("storeObjets"), RS.getInt("pdvper"), RS.getString("spells"), RS.getString("savepos"),
                            RS.getString("jobs"), RS.getInt("mountxpgive"), RS.getInt("mount"), RS.getInt("honor"),
                            RS.getInt("deshonor"), RS.getInt("alvl"), RS.getString("zaaps"), RS.getByte("title"),
                            RS.getInt("wife"), RS.getString("morphMode"), RS.getString("allTitle"), RS.getString("emotes"),
                            RS.getLong("prison"), false, RS.getString("parcho"), RS.getLong("timeDeblo"), RS.getBoolean("noall"),
                            RS.getString("deadInformation"), RS.getByte("needRestat"), RS.getLong("totalKills"), RS.getInt("isParcho")
                    );

                    if (p != null)
                        player.setNeededEndFight(p.needEndFight(), p.hasMobGroup());

                    player.VerifAndChangeItemPlace();
                    try { player.parseInventoryShortcuts(RS.getString("inventory_shortcuts")); }
                    catch (SQLException ignored) {}
                    World.world.addPlayer(player);
                    int guild = Database.getDynamics().getGuildMemberData().isPersoInGuild(RS.getInt("id"));
                    if (guild >= 0)
                        player.setGuildMember(World.world.getGuild(guild).getMember(RS.getInt("id")));
                }
            }
        } catch (SQLException e) {
            sendError("PlayerData loadByAccountId", e);
            Main.INSTANCE.stop("unknown");
        }
    }

    public String loadTitles(int guid) {
        String query = "SELECT * FROM players WHERE id = '" + guid + "';";
        try (Result result = getData(query)) {
            if (result != null && result.getResultSet() != null) {
                ResultSet RS = result.getResultSet();
                if (RS.next()) {
                    return RS.getString("allTitle");
                }
            }
        } catch (SQLException e) {
            sendError("PlayerData loadTitles", e);
        }
        return "";
    }

    public boolean add(Player perso) {
        String query = "INSERT INTO players(`id`, `name`, `sexe`, `class`, `color1`, `color2`, `color3`, `kamas`, `spellboost`, `capital`, `energy`, `level`, `xp`, `size`, `gfx`, `account`, `cell`, `map`, `spells`, `objets`, `storeObjets`, `morphMode`, `server`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,'','','0',?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement p = conn.prepareStatement(query) ) {
            p.setInt(1, perso.getId());
            p.setString(2, perso.getName());
            p.setInt(3, perso.getSexe());
            p.setInt(4, perso.getClasse());
            p.setInt(5, perso.getColor1());
            p.setInt(6, perso.getColor2());
            p.setInt(7, perso.getColor3());
            p.setLong(8, perso.getKamas());
            p.setInt(9, perso.get_spellPts());
            p.setInt(10, perso.get_capital());
            p.setInt(11, perso.getEnergy());
            p.setInt(12, perso.getLevel());
            p.setLong(13, perso.getExp());
            p.setInt(14, perso.get_size());
            p.setInt(15, perso.getGfxId());
            p.setInt(16, perso.getAccID());
            p.setInt(17, perso.getCurCell().getId());
            p.setInt(18, perso.getCurMap().getId());
            p.setString(19, perso.parseSpellToDB());
            p.setInt(20, Config.INSTANCE.getSERVER_ID());
            executeUpdate(p);
            return true;
        } catch (SQLException e) {
            sendError("PlayerData add", e);
        }
        return false;
    }

    public boolean delete(Player perso) {
        String query = "DELETE FROM players WHERE id = ?";
        try (Connection conn = dataSource.getConnection() ; PreparedStatement p = conn.prepareStatement(query) ) {
            p.setInt(1, perso.getId());
            executeUpdate(p);

            if (!perso.getItemsIDSplitByChar(",").isEmpty()) {
                for (String id : perso.getItemsIDSplitByChar(",").split(",")) {
                    Database.getStatics().getObjectData().delete(Long.parseLong(id));
                }
            }
            if (!perso.getStoreItemsIDSplitByChar(",").isEmpty()) {
                for (String id : perso.getStoreItemsIDSplitByChar(",").split(",")) {
                    Database.getStatics().getObjectData().delete(Long.parseLong(id));
                }
            }
            if (perso.getMount() != null) {
                Database.getStatics().getMountData().delete(perso.getMount().getId());
            }
            return true;
        } catch (SQLException e) {
            sendError("PlayerData delete", e);
        }
        return false;
    }

    @Override
    public void load(Object obj) {}

    @Override
    public boolean update(Player player) {
        if (player == null) {
            sendError("PlayerData update", new Exception("perso is null"));
            return false;
        }

        String query = "UPDATE `players` SET `kamas`= ?, `spellboost`= ?, `capital`= ?, `energy`= ?, `level`= ?, `xp`= ?, `size` = ?, `gfx`= ?, `alignement`= ?, `honor`= ?, `deshonor`= ?, `alvl`= ?, `vitalite`= ?, `force`= ?, `sagesse`= ?, `intelligence`= ?, `chance`= ?, `agilite`= ?, `seeFriend`= ?, `seeAlign`= ?, `seeSeller`= ?, `canaux`= ?, `map`= ?, `cell`= ?, `pdvper`= ?, `spells`= ?, `objets`= ?, `storeObjets`= ?, `savepos`= ?, `zaaps`= ?, `jobs`= ?, `mountxpgive`= ?, `mount`= ?, `title`= ?, `wife`= ?, `morphMode`= ?, `allTitle` = ?, `emotes` = ?, `prison` = ?, `parcho` = ?, `timeDeblo` = ?, `noall` = ?, `deadInformation` = ?, `needRestat` = ?, `totalKills` = ?, `isParcho` = ? WHERE `players`.`id` = ? LIMIT 1";
        try (Connection conn = dataSource.getConnection() ; PreparedStatement p = conn.prepareStatement(query) ) {
            p.setLong(1, player.getKamas());
            p.setInt(2, player.get_spellPts());
            p.setInt(3, player.get_capital());
            p.setInt(4, player.getEnergy());
            p.setInt(5, player.getLevel());
            p.setLong(6, player.getExp());
            p.setInt(7, player.get_size());
            p.setInt(8, player.getGfxId());
            p.setInt(9, player.get_align());
            p.setInt(10, player.get_honor());
            p.setInt(11, player.getDeshonor());
            p.setInt(12, player.getALvl());
            p.setInt(13, player.stats.getEffect(EffectConstant.STATS_ADD_VITA));
            p.setInt(14, player.stats.getEffect(EffectConstant.STATS_ADD_FORC));
            p.setInt(15, player.stats.getEffect(EffectConstant.STATS_ADD_SAGE));
            p.setInt(16, player.stats.getEffect(EffectConstant.STATS_ADD_INTE));
            p.setInt(17, player.stats.getEffect(EffectConstant.STATS_ADD_CHAN));
            p.setInt(18, player.stats.getEffect(EffectConstant.STATS_ADD_AGIL));
            p.setInt(19, (player.is_showFriendConnection() ? 1 : 0));
            p.setInt(20, (player.is_showWings() ? 1 : 0));
            p.setInt(21, (player.isShowSeller() ? 1 : 0));
            p.setString(22, player.get_canaux());
            if (player.getCurMap() != null)
                p.setInt(23, player.getCurMap().getId());
            else
                p.setInt(23, 7411);
            if (player.getCurCell() != null)
                p.setInt(24, player.getCurCell().getId());
            else
                p.setInt(24, 311);
            p.setInt(25, player.get_pdvper());
            p.setString(26, player.parseSpellToDB());
            p.setString(27, player.parseObjetsToDB());
            p.setString(28, player.parseStoreItemstoBD());
            p.setString(29, player.getSavePosition());
            p.setString(30, player.parseZaaps());
            p.setString(31, player.parseJobData());
            p.setInt(32, player.getMountXpGive());
            p.setInt(33, (player.getMount() != null ? player.getMount().getId() : -1));
            p.setByte(34, (player.get_title()));
            p.setInt(35, player.getWife());
            p.setString(36, (player.getMorphMode() ? 1 : 0) + ";"
                    + player.getMorphId());
            p.setString(37, player.getAllTitle());
            p.setString(38, player.parseEmoteToDB());
            p.setLong(39, (player.isInEnnemyFaction ? player.enteredOnEnnemyFaction : 0));
            p.setString(40, player.parseStatsParcho());
            p.setLong(41, player.getTimeTaverne());
            p.setBoolean(42, player.noall);
            p.setString(43, player.getDeathInformation());
            p.setByte(44, player.getNeedRestat());
            p.setLong(45, player.getTotalKills());
            p.setInt(46, player.getisParcho());
            p.setInt(47, player.getId());
            executeUpdate(p);

            if (player.getGuildMember() != null)
                Database.getDynamics().getGuildMemberData().update(player);
            if (player.getMount() != null)
                Database.getStatics().getMountData().update(player.getMount());
        } catch (SQLException e) {
            sendError("PlayerData update", e);
        }

        if (player.getQuestPerso() != null && !player.getQuestPerso().isEmpty())
            player.getQuestPerso().values().stream().filter(QP -> QP != null).forEach(QP -> Database.getStatics().getQuestPlayerData().update(QP, player));

        return true;
    }

    public void updateInventory(Player perso){
        String query = "UPDATE `players` SET `objets` = ? WHERE `id`= ?";
        try (Connection conn = dataSource.getConnection() ; PreparedStatement p = conn.prepareStatement(query) ) {
            p.setString(1, perso.parseObjetsToDB());
            p.setInt(2, perso.getId());
            executeUpdate(p);
        } catch (SQLException e) {
            sendError("PlayerData updateInfos", e);
        }
    }

    /** Persiste uniquement la shortcut bar d'items (1.43.7). */
    public void updateInventoryShortcuts(Player perso) {
        String query = "UPDATE `players` SET `inventory_shortcuts` = ? WHERE `id` = ?";
        try (Connection conn = dataSource.getConnection() ; PreparedStatement p = conn.prepareStatement(query) ) {
            p.setString(1, perso.parseInventoryShortcutsToDB());
            p.setInt(2, perso.getId());
            executeUpdate(p);
        } catch (SQLException e) {
            sendError("PlayerData updateInventoryShortcuts", e);
        }
    }

    public void updateInfos(Player perso) {
        String query = "UPDATE `players` SET `name` = ?, `sexe`=?, `class`= ?, `spells`= ? WHERE `id`= ?";
        try (Connection conn = dataSource.getConnection() ; PreparedStatement p = conn.prepareStatement(query) ) {
            p.setString(1, perso.getName());
            p.setInt(2, perso.getSexe());
            p.setInt(3, perso.getClasse());
            p.setString(4, perso.parseSpellToDB());
            p.setInt(5, perso.getId());
            executeUpdate(p);
        } catch (SQLException e) {
            sendError("PlayerData updateInfos", e);
        }
    }

    public void UPDATE_PLAYER_COLORS(Player player) {
        String query = "UPDATE `players` SET `color1` = ?, `color2` = ?, `color3` = ? WHERE `id` = ?;";
        try (Connection conn = dataSource.getConnection() ; PreparedStatement p = conn.prepareStatement(query) ) {
            p.setInt(1, player.getColor1());
            p.setInt(2, player.getColor2());
            p.setInt(3, player.getColor3());
            p.setInt(4, player.getId());
            executeUpdate(p);
        } catch (SQLException e) {
            sendError("PlayerData UPDATE_PLAYER_COLORS", e);
        }
    }

    public void updateGroupe(int group, String name) {
        String query = "UPDATE `players` SET `groupe` = ? WHERE `name` = ?;";
        try (Connection conn = dataSource.getConnection() ; PreparedStatement p = conn.prepareStatement(query) ) {
            p.setInt(1, group);
            p.setString(2, name);
            executeUpdate(p);
        } catch (SQLException e) {
            sendError("PlayerData updateGroupe", e);
        }
    }

    public void updateGroupe(Player perso) {
        String query = "UPDATE `players` SET `groupe` = ? WHERE `id`= ?";
        try (Connection conn = dataSource.getConnection() ; PreparedStatement p = conn.prepareStatement(query) ) {
            int id = (perso.getGroupe() != null) ? perso.getGroupe().getId() : -1;
            p.setInt(1, id);
            p.setInt(2, perso.getId());
            executeUpdate(p);
        } catch (SQLException e) {
            sendError("PlayerData updateGroupe", e);
        }
    }

    public void updateTimeTaverne(Player player) {
        String query = "UPDATE players SET `timeDeblo` = ? WHERE `id` = ?";
        try (Connection conn = dataSource.getConnection() ; PreparedStatement p = conn.prepareStatement(query) ) {
            p.setLong(1, player.getTimeTaverne());
            p.setInt(2, player.getId());
            executeUpdate(p);
        } catch (SQLException e) {
            sendError("PlayerData updateTimeDeblo", e);
        }
    }

    public void updateTitles(int guid, String title) {
        String query = "UPDATE players SET `allTitle` = ? WHERE `id` = ?";
        try (Connection conn = dataSource.getConnection() ; PreparedStatement p = conn.prepareStatement(query) ) {
            p.setString(1, title);
            p.setInt(2, guid);
            executeUpdate(p);
        } catch (SQLException e) {
            sendError("PlayerData updateTitles", e);
        }
    }

    public void updateLogged(int guid, int logged) {
        String query = "UPDATE players SET `logged` = ? WHERE `id` = ?";
        try (Connection conn = dataSource.getConnection() ; PreparedStatement p = conn.prepareStatement(query) ) {
            p.setInt(1, logged);
            p.setInt(2, guid);
            executeUpdate(p);
        } catch (SQLException e) {
            sendError("PlayerData updateLogged", e);
        }
    }

    public void updateAllLogged(int guid, int logged) {
        String query = "UPDATE `players` SET `logged` = ? WHERE `account` = ?";
        try (Connection conn = dataSource.getConnection() ; PreparedStatement p = conn.prepareStatement(query) ) {
            p.setInt(1, logged);
            p.setInt(2, guid);
            executeUpdate(p);
        } catch (SQLException e) {
            sendError("PlayerData updateAllLogged", e);
        }
    }

    public boolean exist(String name) {
        String query = "SELECT COUNT(*) AS exist FROM players WHERE name LIKE '" + name + "';";
        try (Result result = getData(query)) {
            if (result != null && result.getResultSet() != null) {
                ResultSet RS = result.getResultSet();
                if (RS.next() && RS.getInt("exist") > 0) {
                    return true;
                }
            }
        } catch (SQLException e) {
            sendError("PlayerData exist", e);
        }
        return false;
    }

    public void reloadGroup(Player p) {
        String query = "SELECT groupe FROM players WHERE id = '" + p.getId() + "'";
        try (Result result = getData(query)) {
            if (result != null && result.getResultSet() != null) {
                ResultSet RS = result.getResultSet();
                if (RS.next()) {
                    int group = RS.getInt("groupe");
                    Group g = Group.getGroupeById(group);
                    p.setGroupe(g, false);
                }
            }
        } catch (SQLException e) {
            sendError("PlayerData reloadGroup", e);
        }
    }

    public byte canRevive(Player player) {
        String query = "SELECT id, revive FROM players WHERE `id` = '" + player.getId() + "';";
        try (Result result = getData(query)) {
            if (result != null && result.getResultSet() != null) {
                ResultSet RS = result.getResultSet();
                if (RS.next()) {
                    return RS.getByte("revive");
                }
            }
        } catch (SQLException e) {
            sendError("PlayerData canRevive", e);
        }
        return 0;
    }

    public void setRevive(Player player) {
        String query = "UPDATE players SET `revive` = 0 WHERE `id` = '" + player.getId() + "';";
        try (Connection conn = dataSource.getConnection(); PreparedStatement p = conn.prepareStatement(query)) {
            executeUpdate(p);
        } catch (SQLException e) {
            sendError("PlayerData setRevive", e);
        }
    }
}
