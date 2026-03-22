package P2Pchat;

import java.net.*;

public class UdpBroadcastListener implements Runnable {
    private final PeerNode node;
    private final int port;

    public UdpBroadcastListener(PeerNode node, int port) { this.node = node; this.port = port; }

    @Override
    public void run() {
        try {
            DatagramSocket ds = new DatagramSocket(null);
            ds.setReuseAddress(true);
            ds.bind(new InetSocketAddress(node.getLocalIp(), port));
            byte[] buf = new byte[1024];

            while (true) {
                DatagramPacket dp = new DatagramPacket(buf, buf.length);
                ds.receive(dp);
                String data = new String(dp.getData(), 0, dp.getLength());
                String[] parts = data.split(":");

                if (parts.length == 3) {
                    String name = parts[0], ip = parts[1];
                    int tPort = Integer.parseInt(parts[2]);

                    if (!ip.equals(node.getLocalIp()) || tPort != node.getTcpPort()) {
                        node.connectToPeer(ip, tPort, name);
                    }
                }
            }
        } catch (Exception ignored) {}
    }
}