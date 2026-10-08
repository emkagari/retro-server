package database.statics.data;

import client.Player;
import com.zaxxer.hikari.HikariDataSource;
import database.statics.AbstractDAO;
import exchange.transfer.DataQueue;
import game.world.World;
import kernel.Main;
import quest.QuestPlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class QuestPlayerData extends AbstractDAO<QuestPlayer> {

    public QuestPlayerData(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void load(Object obj) {}

    @Override
    public boolean update(QuestPlayer qp) {
        String query = "UPDATE `world.entity.players.quests` SET `finish` = ?, `stepsValidation` = ? WHERE `id` = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement p = conn.prepareStatement(query)) {
            p.setInt(1, qp.isFinish() ? 1 : 0);
            p.setString(2, qp.getQuestStepString());
            p.setInt(3, qp.getId());
            executeUpdate(p);
            return true;
        } catch (SQLException e) {
            sendError("QuestPlayerData update", e);
        }
        return false;
    }

    public void update(QuestPlayer questPlayer, Player player) {
        String query = "UPDATE `world.entity.players.quests` SET `quest`= ?, `finish`= ?, `player` = ?, `stepsValidation` = ? WHERE `id` = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement p = conn.prepareStatement(query)) {
            p.setInt(1, questPlayer.getQuest().getId());
            p.setInt(2, questPlayer.isFinish() ? 1 : 0);
            p.setInt(3, player.getId());
            p.setString(4, questPlayer.getQuestStepString());
            p.setInt(5, questPlayer.getId());
            executeUpdate(p);
        } catch (SQLException e) {
            sendError("QuestPlayerData update", e);
        }
    }

    public void loadAll() {
        String query = "SELECT * FROM `world.entity.players.quests`;";
        try (Result result = getData(query)) {
            if (result != null && result.getResultSet() != null) {
                ResultSet RS = result.getResultSet();
                while (RS.next()) {
                    int playerId = RS.getInt("player");
                    Player player = World.world.getPlayer(playerId);
                    if (player != null) {
                        player.addQuestPerso(new QuestPlayer(RS.getInt("id"), RS.getInt("quest"), RS.getInt("finish") == 1, RS.getInt("player"), RS.getString("stepsValidation")));
                    }
                }
            }
        } catch (SQLException e) {
            sendError("QuestPlayerData loadAll", e);
        }
    }

    public void loadPerso(Player player) {
        String query = "SELECT * FROM `world.entity.players.quests` WHERE `player` = " + player.getId() + ";";
        try (Result result = getData(query)) {
            if (result != null && result.getResultSet() != null) {
                ResultSet RS = result.getResultSet();
                while (RS.next()) {
                    player.addQuestPerso(new QuestPlayer(RS.getInt("id"), RS.getInt("quest"), RS.getInt("finish") == 1, RS.getInt("player"), RS.getString("stepsValidation")));
                }
            }
        } catch (SQLException e) {
            sendError("QuestPlayerData loadPerso", e);
        }
    }

    public boolean delete(int id) {
        String query = "DELETE FROM `world.entity.players.quests` WHERE `id` = ?;";
        try (Connection conn = dataSource.getConnection(); PreparedStatement p = conn.prepareStatement(query)) {
            p.setInt(1, id);
            executeUpdate(p);
            return true;
        } catch (SQLException e) {
            sendError("QuestPlayerData delete", e);
        }
        return false;
    }

    public boolean add(QuestPlayer questPlayer) {
        String query = "INSERT INTO `world.entity.players.quests` VALUES (?, ?, ?, ?, ?);";
        try (Connection conn = dataSource.getConnection(); PreparedStatement p = conn.prepareStatement(query)) {
            p.setInt(1, questPlayer.getId());
            p.setInt(2, questPlayer.getQuest().getId());
            p.setInt(3, questPlayer.isFinish() ? 1 : 0);
            p.setInt(4, questPlayer.getPlayer().getId());
            p.setString(5, questPlayer.getQuestStepString());
            executeUpdate(p);
            return true;
        } catch (SQLException e) {
            sendError("QuestPlayerData add", e);
        }
        return false;
    }

    public int getNextId() {
        final DataQueue.Queue<Integer> queue = new DataQueue.Queue<>((byte) 3);
        try {
            synchronized(queue) {
                long count = DataQueue.count();
                DataQueue.queues.put(count, queue);
                Main.exchangeClient.send("DI" + queue.getType() + count);
                queue.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return queue.getValue();
    }
}
