package client;

import database.Database;
import game.world.World;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class AccountWeb {
    private int id;
    private Map<Integer, Account> gameAccounts = new HashMap<>();
    private String name;
    private String lastIP = "";
    private Timestamp lastConnectionDate = null;
    private String mail = "";
    private int is_banned;
    private int money;
    private int role;

    public AccountWeb(int id, String mail , String name, int is_banned,
                      String lastIp, Timestamp lastConnectionDate, int money, int role) {
        this.id = id;
        this.mail = mail;
        this.name = name;
        this.is_banned = is_banned;
        this.lastIP = lastIP;
        this.lastConnectionDate = lastConnectionDate;
        this.role = role;
    }

    public void setRole(int i) {
        role = i;
    }

    public int getId() {
        return id;
    }

    public void setId(int i) {
        id = i;
    }

    public String getName() {
        return name;
    }

    public void setName(String i) {
        name = i;
    }

    public String getLastIP() {
        return lastIP;
    }

    public void setLastIP(String i) {
        lastIP = i;
    }

    public Timestamp getLastConnectionDate() {
        return lastConnectionDate;
    }

    public int getPoints() {
        money = Database.getSites().getAccountWebData().loadPoints(id);
        return money;
    }

    public int getRole() {
        role = Database.getSites().getAccountWebData().loadRole(id);
        return role;
    }

    public void setPoints(int i) {
        money = i;
        Database.getSites().getAccountWebData().updatePoints(id, money);
    }

    public void addPoints(int i) {
        this.getPoints();
        money += i;
        Database.getSites().getAccountWebData().updatePoints(id, money);
    }

    public void addAccount(Account account){
        if(gameAccounts.get(account.getId()) == null)
            gameAccounts.put(account.getId(),account);
    }

    public Map<Integer, Account> getAccountsId(){
       return this.gameAccounts;
    }


    public Map<Integer, Account> getAccounts() {
        Map<Integer, Account > accounts = new HashMap<>();
        new CopyOnWriteArrayList<>(World.world.getAccounts()).stream().filter(account -> account != null).filter(account -> account.getWebAccount() != null)
                .filter(account -> account.getWebAccount().getId() == this.getId()).forEach(account -> {
            if (account.getGameClient() == null)
                account.setWebaccount(this);
            accounts.put(account.getId(), account);

        });
        return accounts;
    }

   /* public Map<Integer, Account> getAccounts() {
        Map<Integer, Account> accounts = new HashMap<>();
        new CopyOnWriteArrayList<>(World.world.getWebAccounts()).stream().filter(account -> account != null).filter(account -> account.getWebAccount() != null)
                .filter(account -> account.getId() == this.getId()).forEach(account -> {
            if (account.getWebAccount() == null)
                account.getWebAccount(this);
            accounts.put(account.getId(), account);
        });
        return accounts;
    }*/

    public int isBanned() {
        return this.is_banned;
    }

    public void setBanned(int banned) {
        this.is_banned = banned;
    }

}