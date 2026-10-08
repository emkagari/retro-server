package database.site.data;

import ch.qos.logback.classic.Level;
import client.Account;
import client.AccountWeb;
import com.zaxxer.hikari.HikariDataSource;
import database.Database;
import database.site.AbstractDAO;
import game.world.World;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountWebData extends AbstractDAO<AccountWeb> {

    public AccountWebData(HikariDataSource source) {
        super(source);
        logger.setLevel(Level.ERROR);
    }

    public void load(Object accountid) {
        String query = "SELECT * from users where id = " + accountid;
        try (Result result = super.getData(query)) {
            if (result != null && result.getResultSet() != null) {
                ResultSet RS = result.getResultSet();
                while (RS.next()) {
                    AccountWeb test = World.world.getWebAccount(RS.getInt("id"));
                    if (test == null) {
                        if (RS.getString("name").contains("Deleted")) continue;
                        if (RS.getString("email").isEmpty()) continue;

                        AccountWeb C = new AccountWeb(RS.getInt("id"), RS.getString("email").toLowerCase(), RS.getString("name"), RS.getInt("is_banned"), RS.getString("last_login_ip"), RS.getTimestamp("last_login_at"), RS.getInt("money"), RS.getInt("role_id"));
                        World.world.addWebAccount(C);
                    }
                }
            } else {
                System.out.println("Pas de compte web associé à " + accountid);
            }
        } catch (Exception e) {
            super.sendError("AccountWebData load by account id", e);
        }
    }

    public void loadWebAccountFromGameAccount(Account account) {
        if (account == null) {
            return;
        }

        String query = "SELECT * from users RIGHT JOIN dofus129_in_game_web_account_relations on dofus129_in_game_web_account_relations.azuriom_id = users.id WHERE dofus129_in_game_web_account_relations.dofus_id = " + account.getId();
        try (Result result = super.getData(query)) {
            if (result != null && result.getResultSet() != null) {
                ResultSet RS = result.getResultSet();
                while (RS.next()) {
                    AccountWeb test = World.world.getWebAccount(RS.getInt("id"));
                    if (test == null) {
                        if (RS.getString("name").contains("Deleted")) continue;
                        if (RS.getString("email").isEmpty()) continue;

                        test = new AccountWeb(RS.getInt("id"), RS.getString("email").toLowerCase(), RS.getString("name"), RS.getInt("is_banned"), RS.getString("last_login_ip"), RS.getTimestamp("last_login_at"), RS.getInt("money"), RS.getInt("role_id"));
                        World.world.addWebAccount(test);
                    }
                    test.addAccount(account);
                    account.setWebaccount(test);
                }
            } else {
                System.out.println("Pas de compte web associé à " + account.getId());
            }
        } catch (SQLException e) {
            super.sendError("AccountWebData loadWebAccountFromGameAccount", e);
        }
    }

    @Override
    public boolean update(AccountWeb obj) {
        return false;
    }

    public void load() {
        String query = "SELECT * from users";
        try (Result result = super.getData(query)) {
            if (result != null && result.getResultSet() != null) {
                ResultSet RS = result.getResultSet();
                while (RS.next()) {
                    if (RS.getString("email") == null) continue;
                    if (RS.getString("name").contains("Deleted")) continue;

                    AccountWeb a = new AccountWeb(RS.getInt("id"), RS.getString("email").toLowerCase(), RS.getString("name"), RS.getInt("is_banned"), RS.getString("last_login_ip"), RS.getTimestamp("last_login_at"), RS.getInt("money"), RS.getInt("role_id"));
                    World.world.addWebAccount(a);
                }
            }
        } catch (Exception e) {
            super.sendError("AccountWebData load", e);
        }
    }

    public void syncGameAccountWithWebAccount() {
        String query = "SELECT * from dofus129_in_game_web_account_relations";
        try (Result result = super.getData(query)) {
            if (result != null && result.getResultSet() != null) {
                ResultSet RS = result.getResultSet();
                while (RS.next()) {
                    if (RS.getInt("azuriom_id") == 0 || RS.getInt("dofus_id") == 0) continue;

                    AccountWeb test = World.world.getWebAccount(RS.getInt("azuriom_id"));
                    if (test == null) {
                        load(RS.getInt("azuriom_id"));
                        test = World.world.getWebAccount(RS.getInt("azuriom_id"));
                        if (test == null) {
                            // Compte Web non trouvé ou supprimé pour compte Game
                            continue;
                        }
                    }

                    Account a = World.world.getAccount(RS.getInt("dofus_id"));
                    if (a == null) {
                        Database.getStatics().getAccountData().load(RS.getInt("dofus_id"));
                        a = World.world.getAccount(RS.getInt("dofus_id"));
                        if (a == null) {
                            System.out.println("Compte Game non trouvé ou supprimé pour compte azuriom :" + RS.getInt("azuriom_id"));
                            continue;
                        }
                    }
                    test.addAccount(a);
                    a.setWebaccount(test);
                }
            }
        } catch (Exception e) {
            super.sendError("AccountWebData syncGameAccountWithWebAccount", e);
        }
    }

    public boolean delete(int accountID) {
        String query = "DELETE from dofus129_in_game_web_account_relations WHERE dofus_id = " + accountID;
        try (PreparedStatementWrapper wrapper = getPreparedStatement(query)) {
            executeUpdate(wrapper);
            return true;
        } catch (SQLException e) {
            super.sendError("AccountWebData delete", e);
        }
        return false;
    }

    /** Points **/
    public int loadPoints(int user) {
        return Database.getSites().getAccountWebData().loadPointsWithUsersDb(user);
    }

    public int loadRole(int user) {
        return Database.getSites().getAccountWebData().loadRoleWithUsersDb(user);
    }

    public void updatePoints(int id, int points) {
        Database.getSites().getAccountWebData().updatePointsWithoutUsersDb(id, points);
    }

    public void updatePointsWithoutUsersDb(int id, int points) {
        String query = "UPDATE users SET money = ? WHERE id = ?";
        try (PreparedStatementWrapper wrapper = getPreparedStatement(query)) {
            PreparedStatement p = wrapper.getPreparedStatement();
            p.setInt(1, points);
            p.setInt(2, id);
            executeUpdate(wrapper);
        } catch (SQLException e) {
            super.sendError("AccountWebData updatePoints", e);
        }
    }

    public int loadRoleWithUsersDb(int accountID) {
        String query = "SELECT role_id FROM users WHERE id = " + accountID;
        try (Result result = super.getData(query)) {
            if (result != null && result.getResultSet() != null) {
                ResultSet RS = result.getResultSet();
                if (RS.next()) {
                    return RS.getInt("role_id");
                }
            }
        } catch (SQLException e) {
            super.sendError("AccountWebData loadRoleWithUsersDb", e);
        }
        return 1; // Valeur par défaut si aucune donnée n'est trouvée ou en cas d'erreur
    }

    public int loadPointsWithUsersDb(int accountID) {
        String query = "SELECT money FROM users WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, accountID);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("money");
                }
            }
        } catch (SQLException e) {
            super.sendError("AccountWebData loadPointsWithUsersDb", e);
        }
        return 0;
    }
}