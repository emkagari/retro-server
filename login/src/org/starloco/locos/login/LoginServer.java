package org.starloco.locos.login;

import java.util.List;
import java.util.ArrayList;

import org.starloco.locos.kernel.Config;
import org.starloco.locos.kernel.Console;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class LoginServer {

    public final Map<String, LoginClient> clients = new TreeMap<>();
    private NioSocketAcceptor acceptor;

    public LoginServer() {
        acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF8"), LineDelimiter.NUL, new LineDelimiter("\n\0"))));
        acceptor.setHandler(new LoginHandler());
        acceptor.setCloseOnDeactivation(true);
    }

    public void start() {
        if (acceptor.isActive())
            return;

        try {
            InetSocketAddress bindAddress;
            if (Config.loginIp != null && !Config.loginIp.isEmpty() && !Config.loginIp.equals("0.0.0.0")) {
                bindAddress = new InetSocketAddress(Config.loginIp, Config.loginPort);
            } else {
                bindAddress = new InetSocketAddress(Config.loginPort);
            }
            acceptor.bind(bindAddress);
            Console.instance.write(" > Login server bound on " + bindAddress);
        } catch (IOException e) {
            Console.instance.write(e.toString());
            Console.instance.write(" > Fail to bind acceptor : " + e);
        } finally {
            Console.instance.write(" > Login server started on port "
                    + Config.loginPort);
        }
    }

    public void stop() {
        if (!acceptor.isActive())
            return;

        acceptor.unbind();
        acceptor.getManagedSessions().values().stream().filter(session -> session.isConnected() || !session.isClosing()).forEach(session -> session.close(true));
        acceptor.dispose();

        Console.instance.write(" > Login server stopped.");
    }

    public Map<Long, LoginClient> getClients() {
        Map<Long, LoginClient> clients = new HashMap<>();
        try {
            for (Entry<Long, IoSession> entry : acceptor.getManagedSessions().entrySet()) {
                Long id = entry.getKey();
                IoSession session = entry.getValue();
                if (session == null)
                    continue;
                if (session.getAttribute("client") instanceof LoginClient) {
                    LoginClient client = (LoginClient) session.getAttribute("client");
                    if (client == null)
                        continue;
                    clients.put(id, client);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return clients;
    }

    public LoginClient getClient(long id) {
        IoSession session = acceptor.getManagedSessions().get(id);
        if (session.getAttribute("client") instanceof LoginClient)
            return (LoginClient) session.getAttribute("client");
        return null;
    }
}
