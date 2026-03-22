package P2Pchat;

import java.net.ServerSocket;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) {
        String name = "User";
        String ip = "127.0.0.1";
        int tcpPort = 9000;
        int udpPort = 8888;

        for (String arg : args) {
            if (arg.startsWith("--name=")) name = arg.substring(7);
            if (arg.startsWith("--ip=")) ip = arg.substring(5);
            if (arg.startsWith("--tcp-port=")) tcpPort = Integer.parseInt(arg.substring(11));
            if (arg.startsWith("--udp-port=")) udpPort = Integer.parseInt(arg.substring(11));
        }

        if (!isPortAvailable(ip, tcpPort)) {
            System.err.println("Ошибка: Адрес " + ip + ":" + tcpPort + " уже занят другим узлом.");
            System.exit(1);
        }

        System.out.println("\nStarting peer:");
        System.out.println("Name: " + name);
        System.out.println("IP: " + ip);
        System.out.println("TCP port: " + tcpPort);
        System.out.println("UDP port: " + udpPort);

        PeerNode node = new PeerNode(name, ip, udpPort, tcpPort);
        ConsoleUI ui = new ConsoleUI(node);
        node.start();
        ui.start();
    }

    private static boolean isPortAvailable(String ip, int port) {
        try (ServerSocket ss = new ServerSocket()) {
            ss.bind(new InetSocketAddress(ip, port));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}