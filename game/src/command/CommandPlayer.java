package command;

import area.map.GameMap;
import client.Player;
import client.other.Party;
import common.SocketManager;
import database.Database;
import event.EventManager;
import exchange.ExchangeClient;
import fight.Fight;
import fight.Fighter;
import fight.arena.FightManager;
import fight.arena.TeamMatch;
import fight.spells.EffectConstant;
import game.GameClient;
import game.GameServer;
import game.action.ExchangeAction;
import game.world.World;
import hdv.Hdv;
import kernel.Boutique;
import kernel.Config;
import kernel.Constant;
import kernel.Logging;
import object.GameObject;
import object.ObjectTemplate;
import object.entity.Fragment;
import org.apache.commons.lang3.ArrayUtils;
import util.TimerWaiter;
import util.lang.Lang;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class CommandPlayer {

    public final static String canal = "Aegnor";
    public static boolean canalMute = false;

    public static boolean analyse(Player player, String msg) {
        if (msg.charAt(0) == '.' && msg.charAt(1) != '.') {
            if (command(msg, "all") && msg.length() > 5) {
                if (player.isInPrison())
                    return true;
                if(canalMute && player.getGroupe() == null) {
                    player.sendMessage("Le canal est indisponible pour une durée indéterminée.");
                    return true;
                }
                if (player.noall) {
                    player.sendMessage(Lang.get(player, 0));
                    return true;
                }
                if (player.getGroupe() == null && System.currentTimeMillis() - player.getGameClient().timeLastTaverne < 10000) {
                    player.sendMessage(Lang.get(player, 2).replace("#1", String.valueOf(10 - ((System.currentTimeMillis() - player.getGameClient().timeLastTaverne) / 1000))));
                    return true;
                }

                player.getGameClient().timeLastTaverne = System.currentTimeMillis();

                String prefix = "<font color='#C35617'>[" + (new SimpleDateFormat("HH:mm").format(new Date(System.currentTimeMillis()))) + "] (" + canal + ") (" + (Config.INSTANCE.getSERVER_NAME().isEmpty() ? getNameServerById(Config.INSTANCE.getSERVER_ID()) : Config.INSTANCE.getSERVER_NAME()) + ") <b><a href='asfunction:onHref,ShowPlayerPopupMenu," + player.getName() + "'>" + player.getName() + "</a></b>";

                Logging.getInstance().write("AllMessage", "[" + (new SimpleDateFormat("HH:mm").format(new Date(System.currentTimeMillis()))) + "] : " + player.getName() + " : " + msg.substring(5, msg.length() - 1));

                final String message = "Im116;" + prefix + "~" + msg.substring(5, msg.length() - 1).replace(";", ":").replace("~", "").replace("|", "").replace("<", "").replace(">", "") + "</font>";

                World.world.getOnlinePlayers().stream().filter(p -> !p.noall).forEach(p -> p.send(message));
                ExchangeClient.INSTANCE.send("DM" + player.getName() + "|" + getNameServerById(Config.INSTANCE.getSERVER_ID()) + "|" + msg.substring(5, msg.length() - 1).replace("\n", "").replace("\r", "").replace(";", ":").replace("~", "").replace("|", "").replace("<", "").replace(">", "") + "|");
                return true;
            }
            else if(command(msg, "commandemulti")) {
                SocketManager.GAME_SEND_MESSAGE(player, Lang.get(player, 18));
                return true;
            }
            else if(command(msg, "commandevip")) {
                SocketManager.GAME_SEND_MESSAGE(player, Lang.get(player, 19));
                return true;
            }
            else if(command(msg, "multi")) {
                if (player.getFight() != null) {
                    SocketManager.GAME_SEND_MESSAGE(player,"<b>(Erreur)</b> Commande non-utilisable en combat");
                    return true;
                }

                //Création du groupe
                for (Player z : World.world.getOnlinePlayers()) {
                    if(z != player){
                        if (player.getAccount().getCurrentIp().toString().equalsIgnoreCase(z.getAccount().getCurrentIp().toString())){

                            if (z == null || !z.isOnline()) {
                                SocketManager.GAME_SEND_GROUP_INVITATION_ERROR(player.getGameClient(), "n" + z.getName());
                                continue;
                            }
                            if( (z.getParty() != null || player.getParty() != null) && ( z.getParty() == player.getParty())) {
                                SocketManager.GAME_SEND_GROUP_INVITATION_ERROR(player.getGameClient(), "a" + z.getName());
                                SocketManager.GAME_SEND_MESSAGE(player,"<b>(Warning)</b> "+ z.getName()+ " ne peut pas vous rejoindre car il est déjà dans votre groupe");
                                continue;
                            }
                            if (z.getParty() != null) {
                                SocketManager.GAME_SEND_GROUP_INVITATION_ERROR(player.getGameClient(), "a" + z.getName());
                                SocketManager.GAME_SEND_MESSAGE(z,"<b>(Warning)</b> Vous ne pouvez pas rejoindre le groupe de "+ player.getName()+ " car vous avez déjà un groupe");
                                        SocketManager.GAME_SEND_MESSAGE(player,"<b>(Warning)</b> "+ z.getName()+ " ne peut pas vous rejoindre car il est déjà dans un groupe");

                                continue;
                            }
                            if (player.getParty() != null && player.getParty().getPlayers().size() >= 8) {
                                SocketManager.GAME_SEND_GROUP_INVITATION_ERROR(player.getGameClient(), "f");
                                SocketManager.GAME_SEND_MESSAGE(player,"<b>(Warning)</b> Vous ne pouvez pas ajouter de joueur supplémentaire à votre groupe");
                                continue;
                            }


                            Party party = player.getParty();
                            if (party == null) {
                                party = new Party(player, z);
                                SocketManager.GAME_SEND_GROUP_CREATE(player.getGameClient(), party);
                                SocketManager.GAME_SEND_PL_PACKET(player.getGameClient(), party);
                                SocketManager.GAME_SEND_GROUP_CREATE(z.getGameClient(), party);
                                SocketManager.GAME_SEND_PL_PACKET(z.getGameClient(), party);
                                player.setParty(party);
                                SocketManager.GAME_SEND_ALL_PM_ADD_PACKET(player.getGameClient(), party);
                            }
                            else{
                                SocketManager.GAME_SEND_GROUP_CREATE(z.getGameClient(), party);
                                SocketManager.GAME_SEND_PL_PACKET(z.getGameClient(), party);
                                SocketManager.GAME_SEND_PM_ADD_PACKET_TO_GROUP(party, z);
                                party.addPlayer(z);
                            }
                            z.setParty(party);
                            SocketManager.GAME_SEND_ALL_PM_ADD_PACKET(z.getGameClient(), party);
                            SocketManager.GAME_SEND_PR_PACKET(player);


                        }
                    }
                }

                //TP sur la meme map
                if(player.getParty() != null)
                {
                    List<Player> Players = player.getParty().getPlayers();
                    for(final Player groupPlayer : Players)
                    {
                        if (groupPlayer.getName().equals(player.getName()))
                        {}
                        else if (groupPlayer.getFight() != null) {
                            SocketManager.GAME_SEND_MESSAGE(player,"<b>(Erreur)</b> Commande non-utilisable  pour les personnages en combat");
                            return true;
                        }
                        else{
                            final short mappid = player.getCurMap().getId();
                            final int cellid = player.getCurCell().getId();
                            if (player.getAccount().getCurrentIp().toString().equalsIgnoreCase(groupPlayer.getAccount().getCurrentIp().toString())){
                                //SocketManager.GAME_SEND_MESSAGE(player, message2);

                                if(groupPlayer.getCurMap().getId() == player.getCurMap().getId()){

                                }
                                else {
                                    boolean cantp = true;
                                    if (player.cantTP()) {
                                        SocketManager.GAME_SEND_MESSAGE(player,"<b>(Erreur)</b> Vous ne pouvez pas téléporter <b>"+ groupPlayer.getName()+ "</b>");
                                        cantp = false;
                                    }
                                    if(GameMap.IsInDj(player.getCurMap()) || player.getCurMap().isDungeon()){
                                        SocketManager.GAME_SEND_MESSAGE(player,"<b>(Erreur)</b> Vous ne pouvez pas téléporter <b>"+ groupPlayer.getName()+ "</b> sur cette carte");
                                        cantp = false;
                                    }
                                    if (groupPlayer.cantTP()) {
                                        SocketManager.GAME_SEND_MESSAGE(player, "<b>(Warning) " + groupPlayer.getName() + "</b> ne peut pas etre TP");
                                        cantp = false;
                                    }

                                    if(cantp) {
                                        groupPlayer.teleport(mappid, cellid);
                                        SocketManager.GAME_SEND_MESSAGE(player, "<b>(Information) " + groupPlayer.getName() + " </b> a été tp vers vous !");
                                    }
                                }
                            }
                            else {
                                SocketManager.GAME_SEND_MESSAGE(player, "<b>(Warning) " + groupPlayer.getName() + " </b> n'a pas la même IP !");
                            }
                        }
                    }
                }
                else{
                    SocketManager.GAME_SEND_MESSAGE(player,"<b>(Erreur)</b> Commande non-utilisable sans groupe");
                    return true;
                }

                // Maitre
                if(player.getSlaveLeader() != null) {
                    SocketManager.GAME_SEND_MESSAGE(player,"<b>(Erreur)</b> Un esclave ne peut pas devenir maitre");
                    return true;
                }

                  for (Player p : player.getParty().getPlayers()) {
                      if (p == null) {
                          continue;
                      }
                      if (!p.getAccount().getCurrentIp().equals(player.getAccount().getCurrentIp())) {
                          continue;
                      }
                      if (p.getCurMap() != player.getCurMap()) {
                          SocketManager.GAME_SEND_MESSAGE(player, "<b>(Warning) " + p.getName() + " </b> n'est pas sur votre map !");
                          continue;
                      }
                      if (p.getFight() != null) {
                          SocketManager.GAME_SEND_MESSAGE(player, "<b>(Warning) " + p.getName() + " </b> est en combat !");
                          continue;
                      }

                      if (p.getId() == player.getId()) {
                          continue;
                      }
                      if (!p.isOnline()) {
                          SocketManager.GAME_SEND_MESSAGE(player, "<b>(Warning) " + p.getName() + " </b> semble pas joignable !");
                          continue;
                      }
                      if (p.getAccount().getGameClient() == null) {
                          continue;
                      }
                      try {
                          if (p.getSlaveLeader() != null) {

                              SocketManager.GAME_SEND_MESSAGE(player, "<b>(Warning) " + p.getName() + " </b> avait déjà un maitre :" + p.getSlaveLeader().getName() + " - Il va être remplacé");
                              p.setSlaveLeader(null);
                              //continue;
                          }
                          p.setSlaveLeader(player);
                          // Le joueur principal deviens le chef pour les esclaves  !
                          if (player.PlayerList1.contains(p)) {
                              SocketManager.GAME_SEND_MESSAGE(p, "<b>(Information) " + player.getName() + " </b> est déjà votre maitre !");
                              SocketManager.GAME_SEND_MESSAGE(player, "<b>(Information) " + p.getName() + " </b> est déjà votre esclave !");
                              continue;
                          } else {
                              player.PlayerList1.add(p);
                          }
                          p.teleport(player.getCurMap().getId(), player.getCurCell().getId());
                          //On envoie le message a l'esclave
                          SocketManager.GAME_SEND_MESSAGE(p, "<b>(Information) " + player.getName() + " </b> est désormais votre maitre !");
                          SocketManager.GAME_SEND_MESSAGE(player, "<b>(Information) " + p.getName() + " </b> vous suivras & entreras dans vos combats !");
                      } catch (Exception e) {

                          e.printStackTrace();
                      }

                  }

                //One Windows
                if (!player.PlayerList1.isEmpty()) {
                    if(player.oneWindows){
                        //player.oneWindows = false;
                        SocketManager.GAME_SEND_MESSAGE(player,"<b>(Information)</b> Vous avez déjà activé le mode one windows");
                    }
                    else{
                        player.oneWindows = true;
                        SocketManager.GAME_SEND_MESSAGE(player,"<b>(Information)</b> Vous avez activé le mode one windows");
                    }
                }
                else {
                    SocketManager.GAME_SEND_MESSAGE(player,"<b>(Erreur)</b> Il faut être maitre d'un groupe pour utiliser cette commande");
                }

                return true;
            }
            else if(command(msg, "commandebuy")) {
                SocketManager.GAME_SEND_MESSAGE(player, Lang.get(player, 20));
                return true;
            }
            else if(command(msg, "commande")) {
                SocketManager.GAME_SEND_MESSAGE(player, Lang.get(player, 17));
                return true;
            }
            else if(command(msg, "fightdeblo")) {//180min
                if (player.getFight() == null){
                    player.sendMessage("Vous n'êtes pas en combat");
                    return true;
                }
                else{

                    Fight playerFight = player.getFight();
                    Fighter fighter = playerFight.getFighterByPerso(player);
                    int lol2 = playerFight.getFighterByOrdreJeu().getTeam();

                    if(lol2 == fighter.getTeam()) {
                        if(playerFight.getFighterByOrdreJeu().getPlayer() != null) {
                            player.sendMessage("Vous avez passez le tour de " + playerFight.getFighterByOrdreJeu().getPlayer().getName() + " pour débloquer le combat");
                        }
                        else{
                            player.sendMessage("Vous avez passez le tour de " + playerFight.getFighterByOrdreJeu().getMob().getTemplate().getName() + " pour débloquer le combat");
                        }

                        playerFight.setCurAction("");
                        playerFight.endTurn(false,  playerFight.getFighterByOrdreJeu());
                    }
                    else{
                        if(playerFight.startedTimerPass){
                            player.sendMessage("Un passe tour a déjà été enclanché");
                            return true;
                        }
                        player.sendMessage("Le tour passera automatiquement si l'adversaire joue toujours dans 30 sec");
                        Fighter monstrequijoue = playerFight.getFighterByOrdreJeu();
                        int Turn = playerFight.getTurnTotal();
                        playerFight.startedTimerPass = true;

                        TimerWaiter.addNext(() -> {
                            if ( monstrequijoue == playerFight.getFighterByOrdreJeu() && Turn == playerFight.getTurnTotal() ) {
                                if(playerFight.getFighterByOrdreJeu().getMob() != null) {
                                    player.sendMessage("Le monstre " + playerFight.getFighterByOrdreJeu().getMob().getTemplate().getName() + " passe son tour");
                                }
                                else if(playerFight.getFighterByOrdreJeu().getCollector() != null)
                                {
                                    player.sendMessage("Le Percepteur " + playerFight.getFighterByOrdreJeu().getCollector().getFullName() + " passe son tour");
                                }
                                if(monstrequijoue.getPlayer() != null){
                                    monstrequijoue.getPlayer().sendMessage(player + " a passé ton tour car il semblait buggé, si c'est un abus publie ce message sur discord")  ;
                                }
                                playerFight.setCurAction("");
                                playerFight.endTurn(false, monstrequijoue);
                            } else {
                                player.sendMessage("Le combat n'était pas bloqué "+ Turn+"/"+playerFight.getTurnTotal());

                            }
                            playerFight.setStartedTimerPass(false);
                        }, 32, TimeUnit.SECONDS);
                    }
                    return true;
                }
            }
            else if(command(msg, "demorph")) {
                if(player.getGfxId() != player.getClasse() * 10)
                {
                    player.setGfxId(player.getClasse() * 10);
                }
                else{
                    SocketManager.GAME_SEND_MESSAGE(player, "Vous avez déjà le bon skin.");
                }
            }
            else if(command(msg, "sellitem")) {
                if (player.getFight() != null){
                    player.sendMessage("Impossible d'utiliser cette commande en combat");
                    return true;
                }
                if(player.getGroupe().getId() <= 0){
                    player.sendMessage("Commande désactivé");
                    return false;
                }

                if(player.getItems() == null)
                {
                    SocketManager.GAME_SEND_MESSAGE(player, "Votre Inventaire est vide");
                    return false;
                } else if(player.getItems().size() < 1)
                {
                    SocketManager.GAME_SEND_MESSAGE(player, "Votre Inventaire est vide");
                    return false;
                }
                List<GameObject> EquipedObject = player.getEquippedObjects();
                int kamastoGive = 0;
                Map<GameObject, Integer> ObjectToKill = new HashMap<>();
                for(GameObject object : player.getItems().values())
                {
                    if(!object.isAttach() && !EquipedObject.contains(object) && Constant.ITEM_TYPE_TO_SELL.contains(object.getTemplate().getType()))
                    {
                        kamastoGive += (object.getTemplate().getPrice() / 10) * object.getQuantity();
                        ObjectToKill.put(object, object.getQuantity());
                        /*player.removeByTemplateID(object.getTemplate().getId(), object.getQuantity());
                        SocketManager.GAME_SEND_Ow_PACKET(player);
                        SocketManager.GAME_SEND_Im_PACKET(player, "022;"
                                + -object.getQuantity() + "~" + object.getTemplate().getId());*/
                    }
                }
                for(Map.Entry<GameObject, Integer> entry : ObjectToKill.entrySet())
                {
                    player.removeByTemplateID(entry.getKey().getTemplate().getId(), entry.getValue());
                    SocketManager.GAME_SEND_Ow_PACKET(player);
                    SocketManager.GAME_SEND_Im_PACKET(player, "022;"
                            + -entry.getValue() + "~" + entry.getKey().getTemplate().getId());
                }
                player.setKamas(player.getKamas() + kamastoGive);
                Database.getStatics().getPlayerData().update(player);
                SocketManager.GAME_SEND_STATS_PACKET(player);
                SocketManager.GAME_SEND_MESSAGE(player,"Votre inventaire a été vidé ! Vous avez gagné " + kamastoGive + " kamas !");

                return true;
            }
            else if(command(msg, "noall")) {
                if (player.noall) {
                    player.noall = false;
                    player.sendMessage(Lang.get(player, 3));
                } else {
                    player.noall = true;
                    player.sendMessage(Lang.get(player, 4));
                }
                return true;
            }
            else if(command(msg, "noxp")) {
                if (player.noxp) {
                    player.noxp = false;
                    player.sendMessage("Vous reprendrez désormais de l'xp");
                } else {
                    player.noxp = true;
                    player.sendMessage("Vous ne prendrez désormais plus d'xp");
                }
                return true;
            }
            else if(command(msg, "openfragment") || command(msg, "ofrag")) {
                if (player.getFight() != null){
                    player.sendMessage("Impossible d'utiliser cette commande en combat");
                    return true;
                }
                if(player.getExchangeAction() != null) GameClient.leaveExchange(player);

                List<GameObject> fragments = player.getFragmentObject();

                if(fragments != null){
                    for(GameObject fragment : fragments){
                        for (World.Couple<Integer, Integer> couple : ((Fragment) fragment).getRunes()) {
                            ObjectTemplate objectTemplate = World.world.getObjTemplate(couple.first);

                            if (objectTemplate == null)
                                continue;

                            GameObject newGameObject = objectTemplate.createNewItem(couple.second, true,0);

                            if (newGameObject == null)
                                continue;

                            boolean test = player.addObjet(newGameObject, true);
                            World.world.addGameObject(newGameObject, test);

                            /*if (!player.addObjetSimiler(newGameObject, true, -1)) {
                                World.world.addGameObject(newGameObject, true);
                                player.addObjet(newGameObject);
                            }*/
                        }
                        if (fragment.getGuid() != -1) {
                            SocketManager.GAME_SEND_Im_PACKET(player, "022;" + 1 + "~" + World.world.getGameObject(fragment.getGuid()).getTemplate().getId());
                            if (World.world.getGameObject(fragment.getGuid()) != null) {
                                player.removeItem(fragment.getGuid(), 1, true, true);
                            }
                        }
                    }

                }
                return true;
            }
            else if(command(msg, "difficulty0") || command(msg, "diff0")) {
                if (player.difficulty != 0) {
                    player.difficulty = 0;
                    player.sendMessage("A partir de maintenant vous lancerez les combats en difficulté : Normale");
                } else {

                    player.sendMessage("Déjà en difficulté normale");
                }
                return true;
            }
            else if(command(msg, "difficulty1") || command(msg, "diff1")) {

                if (player.difficulty != 1) {
                    player.difficulty = 1;
                    player.sendMessage("A partir de maintenant vous lancerez les combats en difficulté : Difficile");
                } else {

                    player.sendMessage("Déjà en difficulté difficile");
                }
                return true;
            }
            else if(command(msg, "difficulty2") || command(msg, "diff2")) {
                  if (player.difficulty != 2) {
                    player.difficulty = 2;
                    player.sendMessage("A partir de maintenant vous lancerez les combats en difficulté : Très difficile");
                } else {

                    player.sendMessage("Déjà en difficulté monstreuse");
                }
                return true;
            }
            else if(command(msg, "difficulty3") || command(msg, "diff3")) {
                if (player.difficulty != 3) {
                    player.difficulty = 3;
                    player.sendMessage("A partir de maintenant vous lancerez les combats en difficulté : Monstreuse");
                } else {

                    player.sendMessage("Déjà en difficulté monstreuse");
                }
                return true;
            }
            else if(command(msg, "staff")) {
                String message = Lang.get(player, 5);
                boolean vide = true;
                for (Player target : World.world.getOnlinePlayers()) {
                    if (target == null)
                        continue;
                    if (target.getGroupe() == null || target.isInvisible())
                        continue;

                    message += "\n- <b><a href='asfunction:onHref,ShowPlayerPopupMenu," + target.getName() + "'>[" + target.getGroupe().getName() + "] " + target.getName() + "</a></b>";
                    vide = false;
                }
                if (vide)
                    message = Lang.get(player, 6);
                player.sendMessage(message);
                return true;
            }
            else if(command(msg, "shop") || command(msg, "boutique")){
                if (player.getFight() != null){
                    player.sendMessage("Impossible d'utiliser cette commande en combat");
                    return true;
                }

                if(player.isInPrison() ){
                    return true;
                }
                // FAILLE : Correction faille dupplication kamas en échange
                if (player.getExchangeAction() != null){
                    player.sendMessage("Impossible d'utiliser cette commande, tu es déjà en échange");
                    return true;
                }

                 Boutique.open(player);

                return true;
            }
            else if(command(msg, "parcho")) {
                int prix = 100;
                if(player.getAccount().getWebAccount() == null && Config.INSTANCE.getAZURIOM()){
                    String mess = "Tu ne peux pas charger tes points boutique car tu n'as pas affilié ton compte à un compte web.";
                    player.sendMessage(mess);
                    return true;
                }
                int points = 0;
                if(Config.INSTANCE.getAZURIOM()) {
                    points = player.getAccount().getWebAccount().getPoints();
                }
                else{
                    points = player.getAccount().getOldpoints();
                }

                if(player.getisParcho() != 1){
                    if(points < prix) {
                        player.sendMessage("Il vous manque <b>" + (prix - points) + "</b> points boutique pour effectuer cet achat");
                        return true;
                    }
                    else{


                        if (player.getFight() == null) {

                            int val = player.getStats().get(124);
                            int val1 = player.getStats().get(125);
                            int val2 = player.getStats().get(118);
                            int val3 = player.getStats().get(126);
                            int val4 = player.getStats().get(119);
                            int val5 = player.getStats().get(123);

                            if (val + val1 + val2 +val3+val4+val5+val5 != 0){
                                player.sendMessage("Il faut restat vos stats avant de vous parcho");
                                return true;
                            }

                            player.getStatsParcho().getMap().clear();
                            player.getStatsParcho().getEffects().clear();
                            player.getStats().addOneStat(125, 101);
                            player.getStats().addOneStat(124, 101);
                            player.getStats().addOneStat(118, 101);
                            player.getStats().addOneStat(126, 101);
                            player.getStats().addOneStat(119, 101);
                            player.getStats().addOneStat(123, 101);
                            player.getStatsParcho().addOneStat(EffectConstant.STATS_ADD_VITA, 101);
                            player.getStatsParcho().addOneStat(EffectConstant.STATS_ADD_SAGE, 101);
                            player.getStatsParcho().addOneStat(EffectConstant.STATS_ADD_FORC, 101);
                            player.getStatsParcho().addOneStat(EffectConstant.STATS_ADD_INTE, 101);
                            player.getStatsParcho().addOneStat(EffectConstant.STATS_ADD_CHAN, 101);
                            player.getStatsParcho().addOneStat(EffectConstant.STATS_ADD_AGIL, 101);
                        }

                        SocketManager.GAME_SEND_STATS_PACKET(player);
                        if(Config.INSTANCE.getAZURIOM()) {
                            player.getAccount().getWebAccount().setPoints(points - prix);
                        }
                        else{
                            player.getAccount().setOldpoints(points - prix);
                        }

                        player.sendMessage("Il vous reste <b>" + (points - prix) + "</b> après cet achat");
                        player.setisParcho(1);
                    }
                }
                else{
                    if (player.getFight() == null) {

                        int val = player.getStats().get(124);
                        int val1 = player.getStats().get(125);
                        int val2 = player.getStats().get(118);
                        int val3 = player.getStats().get(126);
                        int val4 = player.getStats().get(119);
                        int val5 = player.getStats().get(123);

                        if (val + val1 + val2 +val3+val4+val5+val5 != 0){
                            player.sendMessage("Il faut restat vos stats avant de vous parcho");
                            return true;
                        }
                        player.getStatsParcho().getMap().clear();
                        player.getStatsParcho().getEffects().clear();
                        player.getStats().addOneStat(125, 101);
                        player.getStats().addOneStat(124, 101);
                        player.getStats().addOneStat(118, 101);
                        player.getStats().addOneStat(126, 101);
                        player.getStats().addOneStat(119, 101);
                        player.getStats().addOneStat(123, 101);
                        player.getStatsParcho().addOneStat(EffectConstant.STATS_ADD_VITA, 101);
                        player.getStatsParcho().addOneStat(EffectConstant.STATS_ADD_SAGE, 101);
                        player.getStatsParcho().addOneStat(EffectConstant.STATS_ADD_FORC, 101);
                        player.getStatsParcho().addOneStat(EffectConstant.STATS_ADD_INTE, 101);
                        player.getStatsParcho().addOneStat(EffectConstant.STATS_ADD_CHAN, 101);
                        player.getStatsParcho().addOneStat(EffectConstant.STATS_ADD_AGIL, 101);
                    }

                    SocketManager.GAME_SEND_STATS_PACKET(player);
                    player.sendMessage("Vous êtes de nouveau parchoté");
                }
                return true;
            }
            else if(command(msg, "mapXP")){
                 if (player.isInPrison())
                     return true;
                 if (player.cantTP())
                     return true;
                 if (player.getFight() != null)
                     return true;
                if (player.getLevel() >= 150) {
                    player.sendMessage("Vous êtes trop haut niveau pour aller sur cette map");
                    return true;
                }
                 player.teleport((short) 13000, 222);
                 return true;
             }
            else if(command(msg, "hdv")) {
                if(player.getExchangeAction() != null) GameClient.leaveExchange(player);

                if (player.getFight() != null){
                    player.sendMessage("Impossible d'utiliser cette commande en combat");
                    return true;
                }

                if (player.getDeshonor() >= 5) {
                    SocketManager.GAME_SEND_Im_PACKET(player, "183");
                    return true;
                }

                Hdv hdv = World.world.getHdv(-1);
                if (hdv != null) {
                    String info = "1,10,100;" + hdv.getStrCategory() + ";" + hdv.parseTaxe() + ";" + hdv.getLvlMax() + ";" + hdv.getMaxAccountItem() + ";-1;" + hdv.getSellTime();
                    SocketManager.GAME_SEND_ECK_PACKET(player, 11, info);
                    ExchangeAction<Integer> exchangeAction = new ExchangeAction<>(ExchangeAction.AUCTION_HOUSE_BUYING, -player.getCurMap().getId()); //Rï¿½cupï¿½re l'ID de la map et rend cette valeur nï¿½gative
                    player.setExchangeAction(exchangeAction);
                }
                return true;
            }
            else if(command(msg, "points")){
                if(player.getAccount().getWebAccount() == null && Config.INSTANCE.getAZURIOM()){
                    String mess = "Tu ne peux pas charger tes points boutique car tu n'as pas affilié ton compte à un compte web.";
                    player.sendMessage(mess);
                    return true;
                }

                int points = 0;
                if(Config.INSTANCE.getAZURIOM()){
                     points = player.getAccount().getWebAccount().getPoints();
                }
                else {
                     points = player.getAccount().getOldpoints();
                }
                player.sendMessage("Vous avez <b>" + points + "</b> points boutique");
                return true;
            }
            else if(command(msg, "transfertpoints")){
                if(player.getAccount().getWebAccount() == null){
                    String mess = "Tu ne peux pas charger tes points boutique car tu n'as pas affilié ton compte à un compte web.";
                    player.sendMessage(mess);
                    return true;
                }
                int points = player.getAccount().getOldpoints();
                if(points>0) {
                    player.sendMessage("Vous aviez <b>" + points + "</b> points boutique sur votre compte de jeu");

                    int points2 = player.getAccount().getWebAccount().getPoints();
                    player.getAccount().setOldpoints(0);
                    player.getAccount().getWebAccount().setPoints(points2 + points);
                    player.sendMessage("Vous avez maintenant <b>" + (points2 + points) + "</b> points boutique sur votre compte web");
                }


                return true;
            }
            else if(command(msg, "ipdrop")){
                if (!player.PlayerList1.isEmpty()) {
                    if(player.ipdrop){
                        player.ipdrop = false;
                        SocketManager.GAME_SEND_MESSAGE(player,"<b>(Information)</b> Vous ne recuperez plus désormais les drops de vos esclaves");
                    }
                    else{
                        player.ipdrop = true;
                        SocketManager.GAME_SEND_MESSAGE(player,"<b>(Information)</b> Vous recuperez désormais les drops de vos esclaves");
                    }
                }
                else {
                    SocketManager.GAME_SEND_MESSAGE(player,"<b>(Erreur)</b> Il faut être maitre d'un groupe pour utiliser cette commande");
                }
                return true;
            }
            else if(command(msg, "oneWindows")){
                 if (!player.PlayerList1.isEmpty()) {
                     if(player.oneWindows){
                         player.oneWindows = false;
                         SocketManager.GAME_SEND_MESSAGE(player,"<b>(Information)</b> Vous avez désactivé le mode one windows");
                     }
                     else{
                         player.oneWindows = true;
                         SocketManager.GAME_SEND_MESSAGE(player,"<b>(Information)</b> Vous avez activé le mode one windows");
                     }
                 }
                 else {
                     SocketManager.GAME_SEND_MESSAGE(player,"<b>(Erreur)</b> Il faut être maitre d'un groupe pour utiliser cette commande");
                 }
                 return true;
             }
            else if(command(msg, "noitems")){
                if (!player.PlayerList1.isEmpty() || player.getSlaveLeader()==null) {
                    if(!player.noitems){
                        player.noitems = true;
                        SocketManager.GAME_SEND_MESSAGE(player,"<b>(Information)</b> Vous et vos esclaves ne droperez plus d'items");
                    }
                    else{
                        player.noitems = false;
                        SocketManager.GAME_SEND_MESSAGE(player,"<b>(Information)</b> Vous et vos esclaves re-dropez désormais des items");
                    }
                }
                else {
                    SocketManager.GAME_SEND_MESSAGE(player,"<b>(Erreur)</b> Il faut être maitre d'un groupe ou solo pour utiliser cette commande");
                }
                return true;
            }
            else if(command(msg, "noblackitems")){
                 if (!player.PlayerList1.isEmpty() || player.getSlaveLeader()==null) {
                     if(!player.noblackitems){
                         player.noblackitems = true;
                         SocketManager.GAME_SEND_MESSAGE(player,"<b>(Information)</b> Vous et vos esclaves ne droperez plus d'items hors panoplies");
                     }
                     else{
                         player.noblackitems = false;
                         SocketManager.GAME_SEND_MESSAGE(player,"<b>(Information)</b> Vous et vos esclaves re-dropez désormais des items hors panoplies");
                     }
                 }
                 else {
                     SocketManager.GAME_SEND_MESSAGE(player,"<b>(Erreur)</b> Il faut être maitre d'un groupe ou solo pour utiliser cette commande");
                 }
                 return true;
             }
            else if(command(msg, "getmaster")) {
                if (player.getFight() != null){
                    player.sendMessage("Impossible d'utiliser cette commande en combat");
                    return true;
                }
                if(!player.PlayerList1.isEmpty()){
                    player.disposeSlavery();
                    SocketManager.GAME_SEND_MESSAGE(player, "<b>(Information)</b> Votre liste de suiveur à  été réinitialisée, si vous souhaitez la recréer faites .maitre");
                }
                return true;
            }
            else if(command(msg, "getslave")) {
                if (player.getFight() != null){
                    player.sendMessage("Impossible d'utiliser cette commande en combat");
                    return true;
                }
                if(!player.PlayerList1.isEmpty()){
                    player.disposeSlavery();
                    SocketManager.GAME_SEND_MESSAGE(player, "<b>(Information)</b> Votre liste de suiveur à  été réinitialisée, si vous souhaitez la recréer faites .maitre");
                }
                return true;
            }
            else if(command(msg, "resetmaitre")) {
                if (player.getFight() != null){
                    player.sendMessage("Impossible d'utiliser cette commande en combat");
                    return true;
                }
                if(!player.PlayerList1.isEmpty()){
                    player.disposeSlavery();
                    SocketManager.GAME_SEND_MESSAGE(player, "<b>(Information)</b> Votre liste de suiveur à  été réinitialisée, si vous souhaitez la recréer faites .maitre");
                }
                else {
                    SocketManager.GAME_SEND_MESSAGE(player, "<b>(Information)</b> Seul un maitre peut relacher ses esclaves");
                }
                return true;
            }
            else if (command(msg, "deblo")) {
                if (player.cantTP())
                    return true;
                if (player.getFight() != null){
                    player.sendMessage("Impossible d'utiliser cette commande en combat, Utilisez .fightdeblo");
                    return true;
                }
                if (player.getCurCell().isWalkable(true)) {
                    player.sendMessage(Lang.get(player, 7));
                    return true;
                }
                player.teleport(player.getCurMap().getId(), player.getCurMap().getRandomFreeCellId());
                return true;
            }
            else if(command(msg, "astrub")){
                if (player.isInPrison())
                    return true;
                if (player.getFight() != null){
                    player.sendMessage("Impossible d'utiliser cette commande en combat");
                    return true;
                }

                player.teleport((short) 7411, 311);
                return true;
            }
            else if(command(msg, "xpoffi")){
                if(player.isXpOffilike){
                    player.isXpOffilike = false;
                    SocketManager.GAME_SEND_MESSAGE(player,"<b>(Information)</b> Vous avez désactivé le mode xp offilike");
                }
                else{
                    player.isXpOffilike = true;
                    SocketManager.GAME_SEND_MESSAGE(player,"<b>(Information)</b> Vous avez activé le mode xp offilike");
                }

                if (!player.PlayerList1.isEmpty()) {
                    for (Player p : player.PlayerList1){
                        if (p.isXpOffilike) {
                            p.isXpOffilike = false;
                            SocketManager.GAME_SEND_MESSAGE(p,"<b>(Information)</b> Votre maitre a désactivé le mode xp offilike");
                            SocketManager.GAME_SEND_MESSAGE(player,"<b>(Information)</b> Vous avez désactivé le mode xp offilike de <b>" + p.getName() +"</b>" );
                        } else {
                            p.isXpOffilike = true;
                            SocketManager.GAME_SEND_MESSAGE(p,"<b>(Information)</b> Votre maitre a activé le mode xp offilike");
                            SocketManager.GAME_SEND_MESSAGE(player,"<b>(Information)</b> Vous avez activé le mode xp offilike de <b>" + p.getName() +"</b>");
                        }
                    }
                }
                return true;
            }
            else if(command(msg, "incarnam")){
                if (player.isInPrison())
                    return true;
                if (player.getFight() != null){
                    player.sendMessage("Impossible d'utiliser cette commande en combat");
                    return true;
                }

                player.teleport((short) 10295, 340);
                return true;
            }
            else if(command(msg, "save")){
                Database.getStatics().getPlayerData().update(player);
                String	message = "Sauvegarde de l'inventaire terminé";
                SocketManager.GAME_SEND_MESSAGE(player, message);
                return true;
            }
            else if(command(msg, "infos")) {
                long uptime = System.currentTimeMillis()
                        - Config.INSTANCE.getStartTime();
                int jour = (int) (uptime / (1000 * 3600 * 24));
                uptime %= (1000 * 3600 * 24);
                int hour = (int) (uptime / (1000 * 3600));
                uptime %= (1000 * 3600);
                int min = (int) (uptime / (1000 * 60));
                uptime %= (1000 * 60);
                int sec = (int) (uptime / (1000));
                int nbPlayer = GameServer.getClients().size();
                int nbPlayerIp = GameServer.getPlayersNumberByIp();

                String mess = Lang.get(player, 8).replace("#1", String.valueOf(jour)).replace("#2", String.valueOf(hour)).replace("#3", String.valueOf(min)).replace("#4", String.valueOf(sec));
                if (nbPlayer > 0)
                    mess += Lang.get(player, 9).replace("#1", String.valueOf(nbPlayer));
                if (nbPlayerIp > 0)
                    mess += Lang.get(player, 10).replace("#1", String.valueOf(nbPlayerIp));
                player.sendMessage(mess);
                return true;
            }
            else if(command(msg, "groupe")){
                if (player.getFight() != null){
                    player.sendMessage("Impossible d'utiliser cette commande en combat");
                    return true;
                }
                for (Player z : World.world.getOnlinePlayers()) {
                    if(z != player){

                        if (player.getAccount().getCurrentIp().toString().equalsIgnoreCase(z.getAccount().getCurrentIp().toString())){

                            if (z == null || !z.isOnline()) {
                                SocketManager.GAME_SEND_GROUP_INVITATION_ERROR(player.getGameClient(), "n" + z.getName());
                                continue;
                            }
                            if( (z.getParty() != null || player.getParty() != null) && ( z.getParty() == player.getParty())) {
                                SocketManager.GAME_SEND_GROUP_INVITATION_ERROR(player.getGameClient(), "a" + z.getName());
                                SocketManager.GAME_SEND_MESSAGE(player,"<b>(Warning)</b> "+ z.getName()+ " ne peut pas vous rejoindre car il est déjà dans votre groupe");
                                continue;
                            }
                            if (z.getParty() != null) {
                                SocketManager.GAME_SEND_GROUP_INVITATION_ERROR(player.getGameClient(), "a" + z.getName());
                                SocketManager.GAME_SEND_MESSAGE(z,"<b>(Warning)</b> Vous ne pouvez pas rejoindre le groupe de "+ player.getName()+ " car vous avez déjà un groupe");
                                SocketManager.GAME_SEND_MESSAGE(player,"<b>(Warning)</b> "+ z.getName()+ " ne peut pas vous rejoindre car il est déjà dans un groupe");

                                continue;
                            }
                            if (player.getParty() != null && player.getParty().getPlayers().size() >= 8) {
                                SocketManager.GAME_SEND_GROUP_INVITATION_ERROR(player.getGameClient(), "f");
                                SocketManager.GAME_SEND_MESSAGE(player,"<b>(Warning)</b> Vous ne pouvez pas ajouter de joueur supplémentaire à votre groupe");
                                continue;
                            }


                            Party party = player.getParty();
                            if (party == null) {
                                party = new Party(player, z);
                                SocketManager.GAME_SEND_GROUP_CREATE(player.getGameClient(), party);
                                SocketManager.GAME_SEND_PL_PACKET(player.getGameClient(), party);
                                SocketManager.GAME_SEND_GROUP_CREATE(z.getGameClient(), party);
                                SocketManager.GAME_SEND_PL_PACKET(z.getGameClient(), party);
                                player.setParty(party);
                                SocketManager.GAME_SEND_ALL_PM_ADD_PACKET(player.getGameClient(), party);
                            }
                            else{
                                SocketManager.GAME_SEND_GROUP_CREATE(z.getGameClient(), party);
                                SocketManager.GAME_SEND_PL_PACKET(z.getGameClient(), party);
                                SocketManager.GAME_SEND_PM_ADD_PACKET_TO_GROUP(party, z);
                                party.addPlayer(z);
                            }
                            z.setParty(party);
                            SocketManager.GAME_SEND_ALL_PM_ADD_PACKET(z.getGameClient(), party);
                            SocketManager.GAME_SEND_PR_PACKET(player);


                        }
                    }
                }
                //player.sendMessage("Le groupe ï¿½ ï¿½tï¿½ crï¿½ï¿½ avec " + player.getAccount().getName() + " comme chef");
                return true;
            }
            else if(command(msg, "tp")){
                if (player.getFight() != null){
                    player.sendMessage("Impossible d'utiliser cette commande en combat");
                    return true;
                }
                if(player.getParty() != null)
                {
                    List<Player> Players = player.getParty().getPlayers();
                    if (player.cantTP()) {
                        SocketManager.GAME_SEND_MESSAGE(player,"<b>(Erreur)</b> Vous ne pouvez pas téléporter votre équipe sur cette carte");
                        return true;
                    }
                    if(GameMap.IsInDj(player.getCurMap()) || player.getCurMap().isDungeon()){
                        SocketManager.GAME_SEND_MESSAGE(player,"<b>(Erreur)</b> Vous ne pouvez pas téléporter votre équipe sur cette carte");
                        return true;
                    }

                    for(final Player groupPlayer : Players)
                    {
                        if (groupPlayer.getName().equals(player.getName()))
                        {}
                        else if (groupPlayer.getFight() != null) {
                            SocketManager.GAME_SEND_MESSAGE(player,"<b>(Erreur)</b> Commande non-utilisable  pour les personnages en combat");
                            return true;
                        }
                        else{
                            final short mappid = player.getCurMap().getId();
                            final int cellid = player.getCurCell().getId();
                            if (player.getAccount().getCurrentIp().toString().equalsIgnoreCase(groupPlayer.getAccount().getCurrentIp().toString())){
                                //SocketManager.GAME_SEND_MESSAGE(player, message2);
                                if (groupPlayer.cantTP()) {
                                    SocketManager.GAME_SEND_MESSAGE(player, "<b>(Warning) " + groupPlayer.getName() + " ne peut pas etre TP sur la map actuel");
                                    return true;
                                }
                                groupPlayer.teleport(mappid, cellid);
                                SocketManager.GAME_SEND_MESSAGE(player, "<b>(Information) " + groupPlayer.getName() + " </b> a été tp vers vous !");
                            }
                            else {
                                SocketManager.GAME_SEND_MESSAGE(player, "<b>(Warning) " + groupPlayer.getName() + " </b> n'a pas la même IP !");
                            }
                        }
                    }
                }
                else{
                    SocketManager.GAME_SEND_MESSAGE(player,"<b>(Erreur)</b> Commande non-utilisable sans groupe");
                }
                return true;
            }
            else if(command(msg, "banque")) {

                int cost =0;
                if(player.getExchangeAction() != null){
                    player.sendMessage("Vous ne pouvez pas ouvrir votre banque pendant un échange/craft.");
                    return true;
                }
                if (player.getFight() != null){
                    player.sendMessage("Impossible d'utiliser cette commande en combat");
                    return true;
                }

                player.openBank();
                return true;
            }
            else if(command(msg, "refreshMobs") || command(msg, "rmobs")) {
                if (player.getFight() != null){
                    player.sendMessage("Impossible d'utiliser cette commande en combat");
                    return true;
                }
                if(player.getAccount().getVip() == 1) {
                    if(ArrayUtils.contains(Constant.HOTOMANI_MAPID,player.getCurMap().getId())){
                        player.sendMessage("Vous ne pouvez pas rafraichir les monstres de cette map.");
                        return true;
                    }
                    if(ArrayUtils.contains(Constant.HOTOMANIDJ_MAPID,player.getCurMap().getId())){
                        player.sendMessage("Vous ne pouvez pas rafraichir les monstres de cette map.");
                        return true;
                    }
                    if(Constant.isInGladiatorDonjon(player.getCurMap().getId())){
                        player.sendMessage("Vous ne pouvez pas rafraichir les monstres de cette map.");
                        return true;
                    }
                    if(player.getCurMap().haveMobFix() || ArrayUtils.contains(Constant.ARENA_MAPID,player.getCurMap().getId())){
                        player.sendMessage("Vous ne pouvez pas rafraichir les monstres de cette map.");
                    }
                    else{
                        player.getCurMap().refreshSpawnsWithMaxStars();
                        player.sendMessage("Les monstres sur la map ont été rafraichits.");
                    }
                }
                else {
                    player.sendMessage("Il faut etre VIP pour lancer cette commande.");
                }
                return true;
            }
            else if(command(msg, "zaap")) {
                if (player.getFight() != null){
                    player.sendMessage("Impossible d'utiliser cette commande en combat");
                    return true;
                }
                if (player.isInPrison())
                    return true;

                if(player.getAccount().getVip() == 1) {
                    player.openZaapMenu();
                }
                else {
                    player.sendMessage("Il faut etre VIP pour lancer cette commande.");
                }


                //player.getGameClient().removeAction();
                return true;
            }
            else if(command(msg, "spellboost")) {
                int prix = 50;
                if(player.getAccount().getWebAccount() == null && Config.INSTANCE.getAZURIOM()){
                    String mess = "Tu ne peux pas charger tes points boutique car tu n'as pas affilié ton compte à un compte web.";
                    player.sendMessage(mess);
                    return true;
                }

                int points = 0;
                if(Config.INSTANCE.getAZURIOM()) {
                    points = player.getAccount().getWebAccount().getPoints();
                }
                else{
                    points = player.getAccount().getOldpoints();
                }

                if(points < prix) {
                    player.sendMessage("Il vous manque <b>" + (prix - points) + "</b> points boutique pour effectuer cet achat");
                    return true;
                }
                else{
                    player.set_spellPts(player.get_spellPts() + 15);
                    SocketManager.GAME_SEND_STATS_PACKET(player);
                    if(Config.INSTANCE.getAZURIOM()) {
                        player.getAccount().getWebAccount().setPoints(points - prix);
                    }
                    else{
                        player.getAccount().setOldpoints(points - prix);
                    }
                    player.sendMessage("Il vous reste <b>" + (points - prix) + "</b> après cet achat");
                }
                return true;
            }
            else if(command(msg, "vip")  ) {
                if(player.getAccount().getVip() == 0) {
                    int prix = 400;
                    if(player.getAccount().getWebAccount() == null && Config.INSTANCE.getAZURIOM() ){
                        String mess = "Tu ne peux pas charger tes points boutique car tu n'as pas affilié ton compte à un compte web.";
                        player.sendMessage(mess);
                        return true;
                    }
                    int points = 0;
                    if(Config.INSTANCE.getAZURIOM()) {
                        points = player.getAccount().getWebAccount().getPoints();
                    }
                    else{
                        points = player.getAccount().getOldpoints();
                    }
                    points = points - prix;
                    if(points < 0) {
                        if(Config.INSTANCE.getAZURIOM()) {
                            player.sendMessage("Il vous manque <b>" + (prix - player.getAccount().getWebAccount().getPoints()) + "</b> points boutique pour effectuer cet achat");
                        }
                        else {
                            player.sendMessage("Il vous manque <b>" + (prix - player.getAccount().getOldpoints()) + "</b> points boutique pour effectuer cet achat");
                        }
                        return true;
                    }
                    else{
                        player.getAccount().setVip(1);
                        if(Config.INSTANCE.getAZURIOM()) {
                            player.getAccount().getWebAccount().setPoints(points);
                        }
                        else{
                            player.getAccount().setOldpoints(points);
                        }
                        Database.getStatics().getAccountData().update(player.getAccount());
                        player.sendMessage("Vous êtes maintenant VIP ! Il vous reste <b>" + player.getAccount().getWebAccount().getPoints() + "</b> après cet achat");
                    }
                }
                else{
                    player.sendMessage("Vous êtes déjà VIP");
                }
                return true;
            }
            else if(command(msg, "transfert")) {
                if (player.getAccount().getVip() == 0) {
                    player.sendMessage("Tu n'es pas VIP.");
                    return true;
                }
                if (player.isInPrison() || player.getFight() != null )
                    return true;
                if(player.getExchangeAction() == null || player.getExchangeAction().getType() != ExchangeAction.IN_BANK) {
                    player.sendMessage("Tu n'es pas dans ta banque.");
                    return true;
                }

                player.sendTypeMessage("Bank", "Veuillez patienter quelques instants..");
                int count = 0;

                for (GameObject object : new ArrayList<>(player.getItems().values())) {
                    if (object == null || object.getTemplate() == null || !object.getTemplate().getStrTemplate().isEmpty())
                        continue;
                    switch (object.getTemplate().getType()) {
                        case Constant.ITEM_TYPE_OBJET_VIVANT:case Constant.ITEM_TYPE_PRISME:
                        case Constant.ITEM_TYPE_FILET_CAPTURE:case Constant.ITEM_TYPE_CERTIF_MONTURE:
                        case Constant.ITEM_TYPE_OBJET_UTILISABLE:case Constant.ITEM_TYPE_OBJET_ELEVAGE:
                        case Constant.ITEM_TYPE_CADEAUX:case Constant.ITEM_TYPE_PARCHO_RECHERCHE:
                        case Constant.ITEM_TYPE_PIERRE_AME:case Constant.ITEM_TYPE_BOUCLIER:
                        case Constant.ITEM_TYPE_SAC_DOS:case Constant.ITEM_TYPE_OBJET_MISSION:
                        case Constant.ITEM_TYPE_BOISSON:case Constant.ITEM_TYPE_CERTIFICAT_CHANIL:
                        case Constant.ITEM_TYPE_FEE_ARTIFICE:case Constant.ITEM_TYPE_MAITRISE:
                        case Constant.ITEM_TYPE_POTION_SORT:case Constant.ITEM_TYPE_POTION_METIER:
                        case Constant.ITEM_TYPE_POTION_OUBLIE:case Constant.ITEM_TYPE_BONBON:
                        case Constant.ITEM_TYPE_PERSO_SUIVEUR:case Constant.ITEM_TYPE_RP_BUFF:
                        case Constant.ITEM_TYPE_MALEDICTION:case Constant.ITEM_TYPE_BENEDICTION:
                        case Constant.ITEM_TYPE_TRANSFORM:case Constant.ITEM_TYPE_DOCUMENT:
                        case Constant.ITEM_TYPE_QUETES:
                            break;
                        default:
                            count++;
                            player.addInBank(object.getGuid(), object.getQuantity());
                            break;
                    }
                }

                player.sendTypeMessage("Bank", "Le transfert a été effectué, " + count + " objet(s) ont été déplacés.");
                return true;
            }
            else if(Config.INSTANCE.getTEAM_MATCH() && command(msg, "kolizeum")) {
                if (player.kolizeum != null) {
                    if (player.getParty() != null) {
                        if (player.getParty().isChief(player.getId())) {
                            player.kolizeum.unsubscribe(player.getParty());
                            return true;
                        }
                        player.kolizeum.unsubscribe(player);
                        player.sendMessage("Vous venez de vous désincrire de la file d'attente.");
                    } else {
                        player.kolizeum.unsubscribe(player);
                        player.sendMessage("Vous venez de vous désincrire de la file d'attente.");
                    }
                    return true;
                } else {
                    if (player.getParty() != null) {
                        if (player.getParty().getPlayers().size() < 2) {
                            player.setParty(null);
                            SocketManager.GAME_SEND_PV_PACKET(player.getGameClient(), "");
                            CommandPlayer.analyse(player, ".kolizeum");
                            return true;
                        }
                        if (!player.getParty().isChief(player.getId())) {
                            player.sendMessage("Vous ne pouvez pas inscrire votre groupe, vous n'en êtes pas le chef.");
                            return true;
                        } else if (player.getParty().getPlayers().size() != TeamMatch.PER_TEAM) {
                            player.sendMessage("Pour vous inscrire, vous devez être exactement " + TeamMatch.PER_TEAM
                                    + " joueurs dans votre groupe.");
                            return true;
                        }
                        FightManager.subscribeKolizeum(player, true);
                    } else {
                        FightManager.subscribeKolizeum(player, false);
                    }
                }
                return true;
            }
            else if(Config.INSTANCE.getDEATH_MATCH() && command(msg, "deathmatch")) {
                if(player.cantTP()) return true;
                if (player.deathMatch != null) {
                    FightManager.removeDeathMatch(player.deathMatch);
                    player.deathMatch = null;
                    player.sendMessage("Vous venez de vous désincrire de la file d'attente.");
                } else {
                    if(player.getEquippedObjects().size() == 0) {
                        player.sendMessage("Vous devez avoir des objets équipés.");
                    } else {
                        FightManager.subscribeDeathMatch(player);
                    }
                }
                return true;
            }
            else if(command(msg, "maitre")){
                if (player.getFight() != null) {
                    SocketManager.GAME_SEND_MESSAGE(player,"<b>(Erreur)</b> Commande non-utilisable en combat");
                    return true;
                }
                if(player.getParty() == null) {
                    SocketManager.GAME_SEND_MESSAGE(player,"<b>(Erreur)</b> Commande non-utilisable sans groupe");
                    return true;
                }
                if(player.getSlaveLeader() != null) {
                    SocketManager.GAME_SEND_MESSAGE(player,"<b>(Erreur)</b> Un esclave ne peut pas devenir maitre");
                    return true;
                }
                for (Player p : player.getParty().getPlayers()) {
                    if (p == null) {
                        continue;
                    }
                    if (!p.getAccount().getCurrentIp().equals(player.getAccount().getCurrentIp())) {
                        continue;
                    }
                    if (p.getCurMap() != player.getCurMap()) {
                        SocketManager.GAME_SEND_MESSAGE(player,"<b>(Warning) " + p.getName() + " </b> n'est pas sur votre map !");
                        continue;
                    }
                    if (p.getFight() != null) {
                        SocketManager.GAME_SEND_MESSAGE(player,"<b>(Warning) " + p.getName() + " </b> est en combat !");
                        continue;
                    }

                    if (p.getId() == player.getId()) {
                        continue;
                    }
                    if (!p.isOnline()) {
                        SocketManager.GAME_SEND_MESSAGE(player,"<b>(Warning) " + p.getName() + " </b> semble pas joignable !");
                        continue;
                    }
                    if (p.getAccount().getGameClient() == null) {
                        continue;
                    }
                    try {
                        if (p.getSlaveLeader() != null) {

                            SocketManager.GAME_SEND_MESSAGE(player,"<b>(Warning) " + p.getName() + " </b> a déjà un maitre :" + p.getSlaveLeader().getName());
                            p.setSlaveLeader(null) ;
                            //continue;
                        }
                        p.setSlaveLeader(player);
                        // Le joueur principal deviens le chef pour les esclaves  !
                        if(player.PlayerList1.contains(p) ) {
                            SocketManager.GAME_SEND_MESSAGE(p,"<b>(Information) " + player.getName() + " </b> est déjà votre maitre !");
                            SocketManager.GAME_SEND_MESSAGE(player,"<b>(Information) " + p.getName() + " </b> est déjà votre esclave !");
                            continue;
                        }
                        else {
                            player.PlayerList1.add(p);
                        }
                        p.teleport(player.getCurMap().getId(), player.getCurCell().getId());
                        //On envoie le message a l'esclave
                        SocketManager.GAME_SEND_MESSAGE(p,"<b>(Information) " + player.getName() + " </b> est désormais votre maitre !");
                        SocketManager.GAME_SEND_MESSAGE(player,"<b>(Information) " + p.getName() + " </b> vous suivras & entreras dans vos combats !");
                    } catch (Exception e) {

                        e.printStackTrace();
                    }

                }

                return true;
            }
            else if(command(msg, "pass")){
                if(player.passturn){
                    player.passturn = false;
                    SocketManager.GAME_SEND_MESSAGE(player,"<b>(Information)</b> Vous ne passerez plus vos tours automatiquement");
                }
                else{
                    player.passturn = true;
                    SocketManager.GAME_SEND_MESSAGE(player,"<b>(Information)</b> Vous passerez vos tours automatiquement");
                }
                return true;
            }
            else if(command(msg, "controlinvo")){
                if(player.controleinvo){
                    player.controleinvo = false;
                    SocketManager.GAME_SEND_MESSAGE(player,"<b>(Information)</b> Vous ne controllez plus vos invocations");
                }
                else{
                    player.controleinvo = true;
                    SocketManager.GAME_SEND_MESSAGE(player,"<b>(Information)</b> Vous controllez vos invocations");
                }
                return true;
             }
            else if(command(msg, "slavepass")){
                if(player.getSlaveLeader() == null) {
                    for (Player p : player.PlayerList1) {
                        if (p.passturn) {
                            p.passturn = false;
                            SocketManager.GAME_SEND_MESSAGE(p, "<b>(Information)</b> Vous ne passerez plus vos tours automatiquement");
                        } else {
                            p.passturn = true;
                            SocketManager.GAME_SEND_MESSAGE(p, "<b>(Information)</b> Vous passerez vos tours automatiquement");
                        }
                    }
                    return true;
                }
                else{
                    SocketManager.GAME_SEND_MESSAGE(player, "<b>(Information)</b> Vous n'êtes pas le maître.");
                    return true;
                }
            }
            else if(command(msg, "event")) {
                if(player.cantTP()) return true;
                return EventManager.getInstance().subscribe(player) == 1;
            }
            else {
                SocketManager.GAME_SEND_MESSAGE(player, Lang.get(player, 16));
                return true;
            }
        }
        return false;
    }

    private static boolean command(String msg, String command) {
        return msg.length() > command.length() && msg.substring(1, command.length() + 1).equalsIgnoreCase(command);
    }

    private static String getNameServerById(int id) {
        switch (id) {
            case 2 :
            case 1:
                return "Aegnor";
            case 13:
                return "Silouate";
            case 19:
                return "Allister";
            case 22:
                return "Oto Mustam";
            case 37:
                return "Nostalgy";
            case 4001:
                return "Alma";
            case 4002:
                return "Aguabrial";
        }
        return "Unknown";
    }
}