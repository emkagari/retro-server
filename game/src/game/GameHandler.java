package game;

import kernel.Logging;
import kernel.Main;
import lombok.extern.slf4j.Slf4j;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import game.filter.PacketFilter;
import game.world.World;
import kernel.Config;
import org.apache.mina.filter.FilterEvent;

@Slf4j
public class GameHandler implements IoHandler {

    private final static PacketFilter filter = new PacketFilter().activeSafeMode();

    @Override
    public void sessionCreated(IoSession arg0) throws Exception {
        if (!filter.authorizes(arg0.getRemoteAddress().toString().substring(1).split(":")[0])) {
            arg0.closeNow();
        } else {
            World.world.logger.info("Session " + arg0.getId() + " created");
            arg0.setAttribute("client", new GameClient(arg0));
            Main.refreshTitle();
        }
    }

    @Override
    public void messageReceived(IoSession arg0, Object arg1) throws Exception {
        GameClient client = (GameClient) arg0.getAttribute("client");
        String packet = (String) arg1;

        if (packet.equalsIgnoreCase("<policy-file-request/>")) {
            arg0.write(GameClient.POLICY_FILE);
            arg0.closeOnFlush();
            return;
        }

        boolean is143Packet = false;
        if (packet.contains("ù")) {
            String[] parts = packet.split("ù");
            if (parts.length >= 3) {
                packet = parts[2];
                is143Packet = true;
            }
        }

        if (!is143Packet && Config.INSTANCE.getENCRYPT_PACKET() && !packet.startsWith("AT") && !packet.startsWith("Ak") && !packet.startsWith("CNXN") && !packet.startsWith("Wp") ) {
            try {
                packet = World.world.getCryptManager().decryptMessage(packet, client.getPreparedKeys());
            }
            catch ( Exception e ){
                e.printStackTrace();
                World.world.logger.error("packet " + arg1 + " error to decrypt");
                if (Logging.USE_LOG)
                    Logging.getInstance().write("Error", "packet error to decrypt -" + arg1 + " - " + e.getMessage()+ " - " + (client.getSession() == null ? "No session" : client.getSession().getId()) + " - " + (client.getPlayer() == null ? "" : client.getPlayer().getName()) );
            }
            if (packet != null) packet = packet.replace("\n", "");
            else packet = (String) arg1;
        }

        String[] s = packet.split("\n");

        for(String str : s){
            client.parsePacket(str);
            if (Logging.USE_LOG)
                World.world.logger.trace((client.getPlayer() == null ? "" : client.getPlayer().getName()) + " <-- " + str);
        }
    }

    @Override
    public void sessionClosed(IoSession arg0) {
        if(arg0 != null) {
            this.kick(arg0);
            World.world.logger.info("Session " + arg0.getId() + " closed");
        }
    }

    @Override
    public void exceptionCaught(IoSession arg0, Throwable arg1) {
        arg1.printStackTrace();
        if (Logging.USE_LOG)
            World.world.logger.error("Session " + arg0.getId() + " Exception connexion client : " + arg1.getMessage()  + "");
        this.kick(arg0);
    }

    @Override
    public void messageSent(IoSession arg0, Object arg1) {
        GameClient client = (GameClient) arg0.getAttribute("client");
        if (client != null) {
            if (Logging.USE_LOG){
                String packet = (String) arg1;
                if (Config.INSTANCE.getENCRYPT_PACKET() && !packet.startsWith("AT") && !packet.startsWith("HG"))
                    packet = World.world.getCryptManager().decryptMessage(packet, client.getPreparedKeys()).replace("\n", "");
                if (packet.startsWith("am") || packet.startsWith("GDM") || packet.startsWith("ASK") || packet.startsWith("AB") ) {
                    //if(packet.length() > 16) {
                        //World.world.logger.trace((client.getPlayer() == null ? "" : client.getPlayer().getName()) + " --> " + packet.substring(0, 15) + "...");
                    //}
                    //else{
                        World.world.logger.trace((client.getPlayer() == null ? "" : client.getPlayer().getName()) + " --> " + packet);
                   // }
                }
                else{
                    World.world.logger.trace((client.getPlayer() == null ? "" : client.getPlayer().getName()) + " --> " + packet);
                }

            }
        }
    }

    @Override
    public void inputClosed(IoSession ioSession) throws Exception {
        if(ioSession != null) {
            this.kick(ioSession);
            World.world.logger.info("Input " + ioSession.getId() + " closed");
        }
    }

    @Override
    public void event(IoSession ioSession, FilterEvent filterEvent) throws Exception {

    }

    @Override
    public void sessionIdle(IoSession arg0, IdleStatus arg1) {
        World.world.logger.info("Session " + arg0.getId() + " idle");
        /*GameClient client = (GameClient) arg0.getAttribute("client");
        if(client == null)
            return;
        if(client.getPlayer() == null)
            return;
        if (client.getPlayer().getFight() == null && client.getAccount().getCurrentPlayer() != null) {
            client.ping++;
            client.send("rping");
            if(client.ping == 3)
                client.kick();
        }*/
   }

    @Override
    public void sessionOpened(IoSession arg0) {
        World.world.logger.info("Session " + arg0.getId() + " opened");
    }

    private void kick(IoSession arg0) {
        GameClient client = (GameClient) arg0.getAttribute("client");
        if (client != null) {
            //client.disconnect();
            client.kick();
            arg0.setAttribute("client", null);
            arg0.closeNow();
        }

    }


}
