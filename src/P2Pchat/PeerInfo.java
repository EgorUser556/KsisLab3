package P2Pchat;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PeerInfo {

    private String alias;
    private final String host;
    private int tcpPort;
    private ObjectOutputStream output;
    private Socket socket;

    public PeerInfo(String alias, String host, int tcpPort) {
        this.alias = alias;
        this.host = host;
        this.tcpPort = tcpPort;
    }

    public synchronized void attach(Socket socket) throws IOException {
        this.socket = socket;
        ObjectOutputStream stream = new ObjectOutputStream(socket.getOutputStream());
        stream.flush();
        this.output = stream;
    }

    public synchronized void close() {
        try {
            if (output != null) output.close();
        } catch (IOException ignored) {}
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }

    public int getPort() {
        return tcpPort;
    }

    public void setPort(int tcpPort) {
        this.tcpPort = tcpPort;
    }

    public ObjectOutputStream getOutputStream() {
        return output;
    }

    public String key() {
        return host + ":" + tcpPort;
    }

    public String getName() {
        return alias;
    }

    public void setName(String alias) {
        this.alias = alias;
    }

    public String getIp() {
        return host;
    }
}

class PeerNode {

    private final String nodeName;
    private final String bindIp;
    private final int tcpPort;
    private final int udpPort;

    private final ConcurrentMap<String, PeerInfo> peers = new ConcurrentHashMap<>();
    private final History history = new History();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private MessageListener listener;
    private volatile boolean historyRequested = false;
    private TcpServer tcpServer;

    public interface MessageListener {
        void onMessage(String msg);
    }

    public PeerNode(String nodeName, String bindIp, int udpPort, int tcpPort) {
        this.nodeName = nodeName;
        this.bindIp = bindIp;
        this.udpPort = udpPort;
        this.tcpPort = tcpPort;
    }

    public void setListener(MessageListener listener) {
        this.listener = listener;
    }

    public void start() {
        tcpServer = new TcpServer(this, bindIp, tcpPort);
        executor.execute(tcpServer);
        executor.execute(new UdpBroadcastListener(this, udpPort));
        broadcastPresence();
    }

    public void broadcastPresence() {
        try (DatagramSocket socket = new DatagramSocket(new InetSocketAddress(bindIp, 0))) {
            socket.setBroadcast(true);
            String payload = nodeName + ":" + bindIp + ":" + tcpPort;
            byte[] data = payload.getBytes();
            DatagramPacket packet = new DatagramPacket(
                    data,
                    data.length,
                    InetAddress.getByName("255.255.255.255"),
                    udpPort
            );
            socket.send(packet);
        } catch (Exception ignored) {
        }
    }

    public synchronized void connectToRemotePeer(String ip, int port, String remoteName) {
        String key = ip + ":" + port;
        if (ip.equals(bindIp) && port == tcpPort) {
            return;
        }

        if (peers.containsKey(key)) {
            return;
        }
        try {
            Socket socket = new Socket();
            socket.bind(new InetSocketAddress(bindIp, 0));
            socket.connect(new InetSocketAddress(ip, port), 2000);

            PeerInfo info = new PeerInfo(remoteName, ip, port);
            info.attach(socket);
            peers.put(key, info);

            executor.execute(new PeerConnectionHandler(this, socket, info));

            sendMessage(info, new Message(
                    Type.INTRO,
                    nodeName,
                    nodeName,
                    bindIp,
                    tcpPort
            ));

            notifyPeerConnected(info);
        } catch (Exception ignored) {
        }
    }

    public void handlePeerIntroduction(PeerInfo peer, Message msg) {
        peer.setName(msg.getAuthorName());
        peer.setPort(msg.getAuthorTcpPort());

        String key = peer.key();
        PeerInfo existing = peers.putIfAbsent(key, peer);
        if (existing != null && existing != peer) {
            // Дубликат — уже есть исходящее соединение к этому пиру.
            // Закрываем входящий дубль.
            peer.close();
            return;
        }

        notifyPeerConnected(peer);

        if (!historyRequested) {
            historyRequested = true;
            sendMessage(peer, new Message(
                    Type.HISTORY_GET,
                    "",
                    nodeName,
                    bindIp,
                    tcpPort
            ));
        }
    }

    public void sendChatMessage(String text) {
        Message msg = new Message(
                Type.TEXT,
                text,
                nodeName,
                bindIp,
                tcpPort
        );
        history.add(msg);

        if (listener != null) {
            listener.onMessage(msg.getFormattedMessage());
        }

        peers.values().forEach(peer -> sendMessage(peer, msg));
    }

    public void sendMessage(PeerInfo peer, Message msg) {
        try {
            peer.getOutputStream().writeObject(msg);
            peer.getOutputStream().flush();
        } catch (Exception ignored) {
        }
    }

    public void notifyPeerConnected(PeerInfo peer) {
        Message m = new Message(
                Type.NODE_JOIN,
                "",
                peer.getName(),
                peer.getIp(),
                peer.getPort()
        );
        history.add(m);
        if (listener != null) {
            listener.onMessage(m.getFormattedMessage());
        }
    }

    public void handleDisconnect(PeerInfo peer) {
        if (peer != null && peers.remove(peer.key()) != null) {
            Message m = new Message(
                    Type.NODE_LEAVE,
                    "",
                    peer.getName(),
                    peer.getIp(),
                    peer.getPort()
            );
            history.add(m);
            if (listener != null) {
                listener.onMessage(m.getFormattedMessage());
            }
        }
    }

    public void shutdown() {
        for (PeerInfo peer : peers.values()) {
            peer.close();
        }
        peers.clear();
        if (tcpServer != null) tcpServer.close();
        executor.shutdownNow();
    }

    public void sendHistory(PeerInfo peer) {
        Message m = new Message(
                Type.HISTORY_DATA,
                history.toString(),
                nodeName,
                bindIp,
                tcpPort
        );
        sendMessage(peer, m);
    }

    public void receiveChatHistory(String historyText, String fromName, String fromIp) {
        if (listener != null) {
            listener.onMessage(
                    String.format("            История загружена с %s (%s)              ",
                            fromName, fromIp)
            );
            listener.onMessage("\n================================================================");
        }

        if (historyText != null && !historyText.isEmpty()) {
            String[] lines = historyText.split("\\r?\\n");
            for (String line : lines) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                Message m = new Message(
                        Type.TEXT,
                        line,
                        fromName,
                        fromIp,
                        0
                );
                history.add(m);

                if (listener != null) {
                    listener.onMessage(line);
                }
            }
        } else {
            if (listener != null) {
                listener.onMessage("Empty history received");
            }
        }

        if (listener != null) {
            listener.onMessage("================================================================\n");
        }
    }

    public void appendMessageToHistory(Message m) {
        history.add(m);
        if (listener != null) {
            listener.onMessage(m.getFormattedMessage());
        }
    }

    public String getLocalIp() {
        return bindIp;
    }

    public int getTcpPort() {
        return tcpPort;
    }
}