package P2Pchat;

import java.net.*;
import java.util.concurrent.*;

public class PeerNode {
    private final String name;
    private final String localIp;
    private final int tcpPort;
    private final int udpPort;
    private final ConcurrentHashMap<String, PeerInfo> peers = new ConcurrentHashMap<>();
    private final HistoryManager historyManager = new HistoryManager();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private MessageListener listener;
    private boolean historyRequested = false;

    public interface MessageListener {
        void onMessage(String msg);
    }

    public PeerNode(String name, String localIp, int udpPort, int tcpPort) {
        this.name = name;
        this.localIp = localIp;
        this.udpPort = udpPort;
        this.tcpPort = tcpPort;
    }

    public void setListener(MessageListener listener) {
        this.listener = listener;
    }

    public void start() {
        executor.execute(new TcpServer(this, localIp, tcpPort));
        executor.execute(new UdpBroadcastListener(this, udpPort));
        sendDiscovery();
    }

    public void sendDiscovery() {
        try (DatagramSocket ds = new DatagramSocket(new InetSocketAddress(localIp, 0))) {
            ds.setBroadcast(true);
            String data = name + ":" + localIp + ":" + tcpPort;
            byte[] buf = data.getBytes();
            ds.send(new DatagramPacket(buf, buf.length, InetAddress.getByName("255.255.255.255"), udpPort));
        } catch (Exception e) {}
    }

    public synchronized void connectToPeer(String ip, int port, String pName) {
        String key = ip + ":" + port;
        if (peers.containsKey(key)) {
            return;
        }
        try {
            Socket s = new Socket();
            s.bind(new InetSocketAddress(localIp, 0));
            s.connect(new InetSocketAddress(ip, port), 2000);
            PeerInfo p = new PeerInfo(pName, ip, port);
            p.setSocket(s);
            peers.put(key, p);
            executor.execute(new PeerConnectionHandler(this, s, p));
            sendMessage(p, new Message(MessageType.NAME_TRANSFER, name, name, localIp, tcpPort));
            onPeerConnected(p);
        } catch (Exception e) {}
    }

    public void onPeerIdentified(PeerInfo p, Message msg) {
        p.setName(msg.getSenderName());
        p.setPort(msg.getSenderTcpPort());
        peers.put(p.getKey(), p);
        if (!historyRequested) {
            historyRequested = true;
            sendMessage(p, new Message(MessageType.HISTORY_REQUEST, "", name, localIp, tcpPort));
        }
    }

    public void sendChatMessage(String text) {
        Message msg = new Message(MessageType.CHAT_MESSAGE, text, name, localIp, tcpPort);
        historyManager.add(msg);
        if (listener != null) {
            listener.onMessage(msg.getFormattedMessage());
        }
        peers.values().forEach(p -> sendMessage(p, msg));
    }

    public void sendMessage(PeerInfo p, Message m) {
        try {
            p.getOut().writeObject(m);
            p.getOut().flush();
        } catch (Exception ignored) {}
    }

    public void onPeerConnected(PeerInfo p) {
        Message m = new Message(MessageType.PEER_CONNECTED, "", p.getName(), p.getIp(), p.getPort());
        historyManager.add(m);
        if (listener != null) {
            listener.onMessage(m.getFormattedMessage());
        }
    }

    public void handleDisconnect(PeerInfo p) {
        if (p != null && peers.remove(p.getKey()) != null) {
            Message m = new Message(MessageType.PEER_DISCONNECTED, "", p.getName(), p.getIp(), p.getPort());
            historyManager.add(m);
            if (listener != null) {
                listener.onMessage(m.getFormattedMessage());
            }
        }
    }

    public void sendHistory(PeerInfo p) {
        sendMessage(p, new Message(MessageType.HISTORY_RESPONSE, historyManager.toString(), name, localIp, tcpPort));
    }

    public void receiveHistory(String historyText, String fromName, String fromIp) {
        if (listener != null) {
            listener.onMessage("\n***********************************************************");
            listener.onMessage(String.format("            History loaded from %s (%s)              ", fromName, fromIp));
            listener.onMessage("------------------------------------------------------------");
        }

        if (historyText != null && !historyText.isEmpty()) {
            String[] lines = historyText.split("\\r?\\n");
            for (String line : lines) {
                if (!line.trim().isEmpty()) {

                    Message m = new Message(
                            MessageType.CHAT_MESSAGE,
                            line,
                            fromName,
                            fromIp,
                            0
                    );
                    historyManager.add(m);

                    if (listener != null) listener.onMessage(line);
                }
            }
        } else {
            if (listener != null) listener.onMessage("  [Empty history]                                          ");
        }

        if (listener != null) listener.onMessage("***********************************************************\n");
    }

    public void addMsg(Message m) {
        historyManager.add(m);
        if (listener != null) listener.onMessage(m.getFormattedMessage());
    }

    public String getLocalIp() {
        return localIp;
    }

    public int getTcpPort() {
        return tcpPort;
    }
}