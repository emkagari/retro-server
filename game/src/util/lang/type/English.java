package util.lang.type;

import kernel.Config;
import util.lang.AbstractLang;

/**
 * Created by Locos on 09/12/2015.
 */
public class English extends AbstractLang {

    public final static English singleton = new English();

    public static English getInstance() {
        return singleton;
    }

    public void initialize() {
        int index = 0;
        this.sentences.add(index, "Your overall channel is disabled."); index++;
        this.sentences.add(index, "Some character used in your sentence are disabled."); index++;
        this.sentences.add(index, "You must wait #1 second(s)."); index++;
        this.sentences.add(index, "You have enabled the general channel."); index++;
        this.sentences.add(index, "You have disabled the general channel."); index++;
        this.sentences.add(index, "List of members of staff connected :"); index++;
        this.sentences.add(index, "There is no member of staff connected."); index++;
        this.sentences.add(index, "You are not stuck.."); index++;
        this.sentences.add(index, "<b>" + Config.INSTANCE.getSERVER_NAME() + "</b>\nOnline since : #1j #2h #3m #4s."); index++;
        this.sentences.add(index, "\nPlayers online : #1"); index++;
        this.sentences.add(index, "\nUnique players online : #1"); index++;
        this.sentences.add(index, "\nMost online : #1"); index++;
        //12
        this.sentences.add(index, "Commands available are :\n"
                + "<b>.infos</b> - Get server informations.\n"
                + "<b>.deblo</b> - Teleport on other cell when cell block.\n"
                + "<b>.staff</b> - See Staff member online.\n"
                + "<b>.all</b> - Send message to everyone.\n"
                + "<b>.noall</b> - Stop receiving .all message.\n" +
                "<b>.maitre</b> - Activate Master on player's group (Need to be group with same ip player).\n" +
                (Config.INSTANCE.getTEAM_MATCH() ? "<b>.kolizeum</b> - Vous inscrit/désincrit de la liste d'attente de Kolizeum.\n" : "") +
                (Config.INSTANCE.getDEATH_MATCH() ? "<b>.deathmatch</b> - Vous inscrit/désincrit de la liste d'attente de DeathMatch.\n" : "") +
                "<b>.groupe</b> - group all player with same IP adress."); index++;
        //13
        this.sentences.add(index, "Vous pouvez dès à présent voter, <b><a href='" + Config.INSTANCE.getUrl() + "'>clique ici</a></b> !"); index++;
        //14
        this.sentences.add(index, "Vous ne pouvez plus combattre jusqu'à nouvelle ordre."); index++;
        //15
        this.sentences.add(index, "Vous pouvez désormais combattre."); index++;
        //16
        this.sentences.add(index,"To see all commands, write :\n"
                + "<b>.commande</b> - See all basic commands.\n"
                + "<b>.commandemulti</b> - See all multiPlayer commands.\n"
                + "<b>.commandevip</b> See all VIP commands.\n"
                + "<b>.commandebuy</b> - See all paying commands."); index++;
        //17
        this.sentences.add(index,"<b>***BASIC COMMANDS***</b> available are :\n"
                + "<b>.infos</b> - Get few informations on server.\n"
                + "<b>.deblo</b> - Unblock your player on a walkable cell.\n"
                + "<b>.fightdeblo</b> - Unblock fight (skip turn on active fighter).\n"
                + "<b>.astrub</b> - Teleport to astrub.\n"
                + "<b>.incarnam</b> - Teleport to Incarnam.\n"
                + "<b>.controlinvo</b> - Control/remove control of your summoners.\n"
                + "<b>.mapXP</b> - Teleport to Xp Map (fill with Kanigrou).\n"
                + "<b>.staff</b> - See online staff member.\n"
                + "<b>.save</b> - Force save your inventory.\n"
                + "<b>.all</b> - Send a message to the whole server.\n"
                + "<b>.noall</b> - Block/Unblock seing .all messages .\n"
                + "<b>.banque</b> - Open bank.(cost Kamas if not VIP)\n"
                + "<b>.noitems</b> - Block/Unblock looting Items (expect Dofus).\n"
                + "<b>.noblackitems</b> - Block/Unblock looting randoms Items.\n"
                + "<b>.boutique</b> - Open the shop (Price in PB).\n"
                + "<b>.points</b> - See actual PB account.\n"); index++;
        //18
        this.sentences.add(index,"<b>***MULTIPLAYERS COMMAND***</b> available are :\n"
                + "<b>.multi</b> - Allow you to group/set master/Teleport/and OneWindows all your accounts.\n"
                + "<b>.groupe</b> - Group all your players with same IP.\n"
                + "<b>.tp</b> - Teleport all your players with same IP.\n"
                + "<b>.maitre</b> - Your side accounts will follow you(fight,map,ready).\n"
                + "<b>.onewindows</b> - Allow you to play every account in same Windows in fight.\n"
                + "<b>.ipdrop</b> - You master will get all your side accounts drop.\n"
                + "<b>.getmaster</b> - Show your master.\n"
                + "<b>.getslave</b> - Show your side accounts linked.\n"
                + "<b>.resetmaitre</b> - Free linked side accounts.\n"
                + "<b>.pass</b> - Automatic pass turn.\n"
                + "<b>.slavepass</b> - Automatic pass turn to all your side accounts."); index++;
        //19
        this.sentences.add(index,"<b>***VIP COMMANDS***</b> available are :\n"
                + "<b>.banque</b> - Open your bank (without kamas cost).\n"
                + "<b>.refreshMobs</b> - Resfresh Mobgroup on map.\n"
                + "<b>.zaap</b> - Open zaap menu."); index++;
        // + "<b>.zaap</b> - Permet d'ouvrir la banque."
        //20
        this.sentences.add(index,"<b>***PAYING COMMANDS***</b> available are :\n"
                + "<b>.parcho</b> - (100PB) Add 101 to all Caracteristics.\n"
                + "<b>.spellboost</b> - (50PB) Add 15 Spell points.\n"
                + "<b>.vip</b> - (400PB) Set VIP.\n");
        this.sentences.add(index, "You can now vote on the <b><a href='" + Config.INSTANCE.getUrl() + "'>site</a></b>.");index++;
        this.sentences.add(index, "You can't fight until new order."); index++;
        this.sentences.add(index, "The reboot has been stopped. Now, you can fight.");
    }
}
