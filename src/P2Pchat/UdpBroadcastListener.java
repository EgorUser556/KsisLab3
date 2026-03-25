package P2Pchat;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class UdpBroadcastListener implements Runnable {

    private final PeerNode owner;
    private final int udpPort;

    public UdpBroadcastListener(PeerNode owner, int udpPort) {
        this.owner = owner;
        this.udpPort = udpPort;
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket(null)) {
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(owner.getLocalIp(), udpPort));

            byte[] buffer = new byte[1024];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String payload = new String(packet.getData(), 0, packet.getLength());
                handleDiscoveryPacket(payload);
            }
        } catch (Exception ignored) {
        }
    }

    private void handleDiscoveryPacket(String payload) {
        String[] parts = payload.split(":");
        if (parts.length != 3) {
            return;
        }

        String remoteName = parts[0];
        String remoteIp = parts[1];

        int remoteTcpPort;
        try {
            remoteTcpPort = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            return;
        }
        if (remoteIp.equals(owner.getLocalIp()) && remoteTcpPort == owner.getTcpPort()) {
            return;
        }

        owner.connectToRemotePeer(remoteIp, remoteTcpPort, remoteName);
    }
}
