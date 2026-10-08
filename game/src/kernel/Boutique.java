package kernel;

import client.Player;
import client.other.Stats;
import common.SocketManager;
import game.action.ExchangeAction;
import object.ObjectTemplate;

import java.util.ArrayList;
import java.util.List;

public class Boutique {
    public static final List<ObjectTemplate> items = new ArrayList<>();
    private static String packet;

    public static void initPacket() {
        packet = getObjectList();
    }

    public static void open(Player player) {
        if(player.getExchangeAction() != null){
            player.sendMessage("Tu ne peux pas accéder a la boutique, tu es déjà en échange");
            return;
        }
        //player.boutique = true;
        player.setExchangeAction(new ExchangeAction<>(ExchangeAction.TRADING_WITH_BOUTIQUE, 0));
        SocketManager.send(player, "ECK20|1");
        SocketManager.send(player, "EY" + packet);
    }

    private static String getObjectList() {
        StringBuilder items = new StringBuilder();
        for (ObjectTemplate obj : Boutique.items) {
            //Stats stats = obj.generateNewStatsFromTemplate(obj.getStrTemplate(), true,3);
            Stats stats;
            if(obj.getId()==9624){
                stats = new Stats();
            }
            else{
                stats = obj.generateNewStatsFromTemplate(obj.getStrTemplate(), true,3);
            }
            items.append(obj.getId()).append(";").append(stats.parseToItemSetStats()).append(";").append(obj.getPoints()).append("|");
        }
        return items.toString();
    }
}
