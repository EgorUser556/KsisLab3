package P2Pchat;

import java.net.*;

public class TcpServer implements Runnable {
    private final PeerNode node;
    private final String ip;
    private final int port;

    public TcpServer(PeerNode node, String ip, int port) { this.node = node; this.ip = ip; this.port = port; }

    @Override
    public void run() {
        try (ServerSocket ss = new ServerSocket()) {
            ss.bind(new InetSocketAddress(ip, port));
            while (true) {
                Socket s = ss.accept();
                PeerInfo p = new PeerInfo("Unknown", s.getInetAddress().getHostAddress(), s.getPort());
                p.setSocket(s);
                new Thread(new PeerConnectionHandler(node, s, p)).start();
            }
        } catch (Exception ignored) {}
    }
}