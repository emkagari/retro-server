package other;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import client.Account;
import client.Player;
import common.SocketManager;
import database.Database;
import fight.Fight;
import game.GameClient;
import game.GameServer;
import game.scheduler.entity.WorldSave;
import game.world.World;
import kernel.Config;
import kernel.Main;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.LoggerFactory;
import util.lang.Lang;

import javax.security.auth.login.LoginException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Set;

public class DiscordBot extends ListenerAdapter {
    private String token = Config.INSTANCE.getDISCORD_KEY();
    private JDA jda;
    private String targetChannelId = Config.INSTANCE.getDISCORD_CHANNEL_COMMAND();

    public DiscordBot() { }

    public void start() throws LoginException {
        jda =JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT) // Enable MESSAGE_CONTENT intent
                .addEventListeners(this)
                .build();

        refreshActivity();
        setLogLevel(Level.ERROR);
    }

    private static void setLogLevel(Level level) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("net.dv8tion.jda");
        rootLogger.setLevel(level);
    }


    public void refreshActivity (){
        long nbOfPlayer = World.world.getOnlinePlayers().stream().count();
        this.jda.getPresence().setActivity(
                Activity.of(Activity.ActivityType.PLAYING,
                        "Aegnor [Retro] - "+nbOfPlayer)

        );
    }

    private static String getFormattedTime() {
        long uptime = System.currentTimeMillis() - Config.INSTANCE.getStartTime();

        // Convert milliseconds to Instant
        Instant instant = Instant.ofEpochMilli(uptime);

        // Convert Instant to LocalDateTime in a specific time zone
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime dateTime = instant.atZone(zoneId).toLocalDateTime();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedDateTime = dateTime.format(formatter);

        return "En ligne depuis: " + formattedDateTime;
    }

    private void analyseCommand(String command, MessageReceivedEvent event) {
        boolean canGo = false;
        String mess = "";
        String[] commandParts = command.split("\\s+");
        String mainCommand = commandParts.length > 1 ? commandParts[0] : command;
        switch (mainCommand.toUpperCase()) {
            case "HELP":
                canGo = true;
                mess = "__Commandes disponibles :__\n";
                mess += "1. Donner des cadeaux à tout le monde : **ALLGIFTS [templateid] [quantity] [jp=1 ou 0]**.\n";
                mess += "2. Donner des cadeaux à un personnage spécifique : **GIFT [name] [templateid] [quantity] [jp=1 ou 0]**.\n";
                mess += "3. Informations générales à propos du serveur : **INFOS**.\n";
                mess += "4. Obtenir des informations à propos d'un joueur : **PLAYER [Name]**.\n";
                mess += "5. Lancer une sauvegarde du jeu : **SAVE**.\n";
                mess += "6. Redémarrer le serveur : **EXIT**.\n";
                mess += "7. Envoyer un message annonce au serveur : **A [Message]**.\n";
                mess += "8. Envoyer un message avec ton pseudo au serveur : **ANAME [Message]**.\n";
                mess += "9. Activer/Désactiver les combats en jeu : **BLOCKFIGHT [1/0]**.\n";
                mess += "10. Donner des points boutiques à un personnage : **POINTS [points] [name]**.\n";
                mess += "11. Bannir l'IP d'un personnage : **BANIP [IP/NOM JOUEUR]**.\n";
                mess += "12. Bannir un joueur pendant x jours : **BANTIME [NOM JOUEUR]**.\n";
                mess += "13. Bannir un joueur de façon permanente : **BAN [NOM JOUEUR]**.\n";
                mess += "14. Débannir un joueur : **UNBAN [NOM JOUEUR]**.\n";
                mess += "15. Terminer tous les combats : **ENDFIGHTALL**";
                break;
            case "ENDFIGHTALL":
                canGo = true;
                try {
                    for (GameClient client : GameServer.getClients()) {
                        Player player = client.getPlayer();
                        if (player == null)
                            continue;
                        Fight f = player.getFight();
                        if (f == null)
                            continue;
                        try {
                            if (f.getLaunchTime() > 1)
                                continue;
                            f.endFight(true);
                            mess += "Le combat de **" + player.getName() + "** a été terminé.\n";
                        } catch (Exception e) {
                            // ok
                            mess += "Le combat de "
                                    + player.getName() + " a déjà été terminé.\n";
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mess +="Erreur lors de la commande **endfightall** : "
                            + e.getMessage() + ".";
                } finally {
                    mess +="Tous les combats ont ete termines.";
                }
                break;
            case "BANIP":
                canGo = true;

                break;
            case "UNBAN":
                canGo = true;
                if (commandParts.length >= 2) {
                    Player look = World.world.getPlayerByName(commandParts[1]);
                    if (look != null) {
                        look.getAccount().setBanned(false);
                        Database.getStatics().getBanIpData().delete(look.getAccount().getCurrentIp());
                        mess = "Le joueur **" + look.getName() + "** a été débanni avec succès.";
                    } else {
                        mess = "Aucun personnage n'a été trouvé avec ce nom !";
                    }
                } else {
                    mess = "Erreur de syntaxe : **UNBAN NOM**.";
                }
                break;
            case "BAN":
                canGo = true;
                short days = 0;

                try {
                    days = Short.parseShort(commandParts[2]);
                } catch(Exception ignored) {

                }

                if (commandParts.length >= 2) {
                    Player look = World.world.getPlayerByName(commandParts[1]);
                    if (look != null) {
                        look.getAccount().setBanned(true);
                        Database.getStatics().getAccountData().updateBannedTime(look.getAccount(), System.currentTimeMillis() + 86400000 * days);

                        mess = "Le joueur **" + look.getName() + "** a été banni de façon permanente et déconnecté du jeu s'il est connecté !";
                        if (look.getFight() == null) {
                            if (look.getGameClient() != null)
                                look.getGameClient().kick();
                        } else {
                            SocketManager.send(look, "Im1201;" + "Arwase");
                        }

                    } else {
                        mess = "Aucun personnage n'a été trouvé avec ce nom !";
                    }
                } else {
                    mess = "Erreur de syntaxe : **BAN NOM**.";
                }
                break;
            case "GIFT":
                canGo = true;
                boolean allParam = true;
                String name = "";
                int template = -1, quantity = 0, jp = 0;
                try {
                    name = commandParts[1];
                    template = Integer.parseInt(commandParts[2]);
                    quantity = Integer.parseInt(commandParts[3]);
                    jp = Integer.parseInt(commandParts[4]);
                } catch (Exception e) {
                    mess = "Paramètres incorrects : GIFT [name] [templateid] [quantity] [jp=1 ou 0]";
                    break;
                }
                String gift = template + "," + quantity + "," + jp;

                Player receiver = World.world.getPlayerByName(name);

                if (receiver == null) {
                    mess = "Aucun personnage n'a été trouvé avec le nom **" + name + "** !";
                    break;
                }


                String gifts = Database.getDynamics().getGiftData().getByAccount(receiver.getAccount().getId());
                if (gifts.isEmpty()) {
                    Database.getDynamics().getGiftData().update(receiver.getAccount().getId(), gift);
                } else {
                    Database.getDynamics().getGiftData().update(receiver.getAccount().getId(), gifts
                            + ";" + gift);
                }
                mess = "Le joueur **" + name + " (" + receiver.getAccount().getPseudo() + ")** a reçu le cadeau **" + template + "** x **" + quantity + "**.";
                break;
            case "ALLGIFTS":
                canGo = true;
                allParam = true;
                try {
                    template = Integer.parseInt(commandParts[1]);
                    quantity = Integer.parseInt(commandParts[2]);
                    jp = Integer.parseInt(commandParts[3]);
                } catch (Exception e) {
                    mess = "Paramètres incorrects : ALLGIFTS [templateid] [quantity] [jp=1 ou 0]";
                    break;
                }
                gift = template + "," + quantity + "," + jp;

                for (Account account : World.world.getAccounts()) {
                    gifts = Database.getDynamics().getGiftData().getByAccount(account.getId());
                    if (gifts.isEmpty()) {
                        Database.getDynamics().getGiftData().update(account.getId(), gift);
                    } else {
                        Database.getDynamics().getGiftData().update(account.getId(), gifts
                                + ";" + gift);
                    }
                }
                mess = World.world.getAccounts().size() + " comptes ont reçu le cadeau : **" + template + "** x **" + quantity + "**.";
                break;
            case "INFOS":
                canGo = true;
                long uptime = System.currentTimeMillis() - Config.INSTANCE.getStartTime();
                int day = (int) (uptime / (1000 * 3600 * 24));
                uptime %= (1000 * 3600 * 24);
                int hour = (int) (uptime / (1000 * 3600));
                uptime %= (1000 * 3600);
                int min = (int) (uptime / (1000 * 60));
                uptime %= (1000 * 60);
                int sec = (int) (uptime / (1000));

                String message = "\n<u><b>Global informations system of the Aegnor emulator :</b></u>\n\n<u>Uptime :</u> " + day + "j " + hour + "h " + min + "m " + sec + "s.\n";
                message += "Online players         : " + GameServer.getClients().size() + "\n";
                message += "Unique online players  : " + GameServer.getPlayersNumberByIp() + "\n";
                message += "Online clients         : " + GameServer.getClients().size() + "\n";


                int mb = 1024 * 1024;
                Runtime instance = Runtime.getRuntime();

                message += "\n<u>Heap utilization statistics :</u>";
                message += "\nTotal Memory : " + instance.totalMemory() / mb + " Mo.";
                message += "\nFree Memory  : " + instance.freeMemory() / mb + " Mo.";
                message += "\nUsed Memory  : " + (instance.totalMemory() - instance.freeMemory()) / mb + " Mo.";
                message += "\nMax Memory   : " + instance.maxMemory() / mb + " Mo.";
                message += "\n\n<u>Available processor :</u> " + instance.availableProcessors();
                Set<Thread> list = Thread.getAllStackTraces().keySet();

                int news = 0, running = 0, blocked = 0, waiting = 0, sleeping = 0, terminated = 0;
                for(Thread thread : list) {
                    switch(thread.getState()) {
                        case NEW: news++; break;
                        case RUNNABLE: running++; break;
                        case BLOCKED: blocked++; break;
                        case WAITING: waiting++; break;
                        case TIMED_WAITING: sleeping++; break;
                        case TERMINATED: news++; break;
                    }
                }

                message +="\n\n<u>Informations of " + list.size() + " threads :</u> ";
                message += "\nNEW           : " + news;
                message += "\nRUNNABLE      : " + running;
                message += "\nBLOCKED       : " + blocked;
                message += "\nWAITING       : " + waiting;
                message += "\nTIMED_WAITING : " + sleeping;
                message += "\nTERMINATED    : " + terminated;

                if(commandParts.length > 1) {
                    message += "List of all threads :\n";
                    for(Thread thread : list)
                        message += "- " + thread.getId() + " -> " + thread.getName() + " -> " + thread.getState().name().toUpperCase() + "" + (thread.isDaemon() ? " (Daemon)" : "") + ".\n";
                }
                mess += message;

                break;
            case "PLAYER":
                canGo = true;
                if (commandParts.length >= 2) {
                    Player look = World.world.getPlayerByName(commandParts[1]);
                    if (look != null) {
                        mess = "__Informations à propos du joueur **" + look.getName() + "**__\n";
                        mess += "**Nom du compte and Pseudo** : " + look.getAccount().getName() + "("+look.getAccount().getPseudo()+")\n";
                        mess += "**Adresse IP**  : " + look.getAccount().getCurrentIp() + "\n";
                        mess += "**Dernière Connexion** : " + look.getAccount().getLastConnectionDate() + "\n";
                        mess += "**Niveau**  : " + look.getLevel() + "\n";
                        mess += "**Position Actuelle**  : " + look.getCurMap() + "\n";
                        mess += "**Kamas sur Inventaire**  : " + look.getKamas() + "\n";
                        mess += "**Kamas sur Banque**  : " + look.getBankKamas() + "\n";
                        mess += "**Points boutiques**  : " + look.getAccount().getWebAccount().getPoints() + "\n";
                        mess += "**Guilde**  : " + look.getGuild().getName() + "\n";
                        mess += "**Capital & Points de sorts**  : " + look.get_capital() + " - "+look.get_spellPts()+"\n";
                    } else {
                        mess = "Aucun personnage n'a été trouvé avec ce nom !";
                    }
                } else {
                    mess = "Erreur de syntaxe : **PLAYER NOM**.";
                }
                break;
            case "SAVE":
                canGo = true;
                WorldSave.cast(1);
                mess = "Sauvegarde lancée!";
                break;
            case "BLOCKFIGHT":
                canGo = true;
                if (commandParts.length >= 2) {
                    try {
                        int i = Integer.parseInt(commandParts[1]);
                        if (i == 0) {
                            Main.INSTANCE.setFightAsBlocked(false);
                            for(Player player : World.world.getOnlinePlayers())
                                player.sendServerMessage(Lang.get(player, 15));
                            mess = "Les combats ont été débloqués.";
                        } else if (i == 1) {
                            for(Player player : World.world.getOnlinePlayers())
                                player.sendServerMessage(Lang.get(player, 14));
                            mess = "Les combats ont été bloqués.";
                        } else {
                            mess = "Les combats ont été bloqués.";
                        }
                    } catch (NumberFormatException e) {
                        mess = "Erreur de syntaxe : **BLOCKFIGHT 1 ou BLOCKFIGHT 0**.";
                    }
                } else {
                    mess = "Erreur de syntaxe : **BLOCKFIGHT 1 ou BLOCKFIGHT 0**.";
                }
                break;
            case "REBOOT":
            case "EXIT":
                canGo = true;
                int time = 10;
                int lanch = 1;
                if (commandParts.length > 2) {
                    lanch = Integer.parseInt(commandParts[1]);
                    try {
                        time = Integer.parseInt(commandParts[2]);
                    }
                    catch (Exception ignore){
                        mess = "Pas de temps renseigné 10 par défaut :";
                        time = 10;
                        break;
                    }
                }

                if(lanch == 1) {
                    mess = "Lancement du reboot dans "+ time + " minutes";
                    World.world.reboot(lanch,time);
                }
                else{
                    mess = "Cancel du reboot";
                    World.world.reboot(0,time);
                }

                break;
            case "A":
                canGo = true;
                if (commandParts.length >= 2) {
                    String[] infos = command.split(" ", 2);
                    String prefix = "<b>Annonce</b>";
                    SocketManager.GAME_SEND_Im_PACKET_TO_ALL("116;" + prefix + "~" + infos[1]);
                    mess = "Vous avez envoyé un message à tout le serveur.";
                } else {
                    mess = "Erreur de syntaxe : **A message**";
                }
                break;
            case "ANAME":
                canGo = true;
                if (commandParts.length >= 2) {
                    String[] infos = command.split(" ", 2);
                    String prefix = "<b>" + event.getAuthor().getName() + "</b>";
                    SocketManager.GAME_SEND_Im_PACKET_TO_ALL("116;" + prefix + "~" + infos[1]);
                    mess = "Vous avez envoyé un message à tout le serveur avec votre nom.";
                } else {
                    mess = "Erreur de syntaxe : **ANAME message**";
                }
                break;
        }

        MessageChannel channel = event.getChannel();
        if (!canGo) {
            channel.sendMessage("Commande inconnue ou inexistante : **" + command + "**!").queue();
        } else {
            channel.sendMessage(mess).queue();
        }
    }

    public static boolean isNumber(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // On ignore les messages des Bots
        if (event.getAuthor().isBot()) {
            return;
        }

        // On recupère le message et l'ID du channel ou il a été posté
        String command = event.getMessage().getContentRaw();
        String channelId = event.getChannel().getId();

        // On verifie que le message est envoyer sur le bon channel
        if (channelId.equals(targetChannelId)) {
            analyseCommand(command, event);
        }

    }


    public void notifyPrivateDiscord(String message) {
        if (jda != null) {
            TextChannel channel = jda.getTextChannelById("1136688256323428542");
            if (channel != null) {
                channel.sendMessage(message).queue();
            }
        }
    }

    public void notifyInformationDiscord(String message) {
        if (jda != null) {
            TextChannel channel = jda.getTextChannelById("825153415423197185");
            if (channel != null) {
                channel.sendMessage(message).queue();
            }
        }
    }


}