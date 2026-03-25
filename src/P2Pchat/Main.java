package P2Pchat;

import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class Main {

    public static void main(String[] args) {
        LaunchConfig cfg = buildConfig(args);

        if (!checkPort(cfg.ipAddress, cfg.tcpPort)) {
            System.err.println("Ошибка: адрес " + cfg.ipAddress + ":" + cfg.tcpPort + " занят.");
            System.exit(1);
        }

        showConfig(cfg);

        PeerNode node = new PeerNode(cfg.userName, cfg.ipAddress, cfg.udpPort, cfg.tcpPort);
        Log ui = new Log(node);

        node.start();
        ui.start();
    }

    private static LaunchConfig buildConfig(String[] args) {
        LaunchConfig cfg = new LaunchConfig();

        for (String arg : args) {
            if (arg.startsWith("--name=")) {
                cfg.userName = arg.substring("--name=".length());
            } else if (arg.startsWith("--ip=")) {
                cfg.ipAddress = arg.substring("--ip=".length());
            } else if (arg.startsWith("--tcp=")) {
                cfg.tcpPort = Integer.parseInt(arg.substring("--tcp=".length()));
            } else if (arg.startsWith("--udp=")) {
                cfg.udpPort = Integer.parseInt(arg.substring("--udp=".length()));
            }
        }

        return cfg;
    }

    private static boolean checkPort(String ip, int port) {
        try (ServerSocket server = new ServerSocket()) {
            server.bind(new InetSocketAddress(ip, port));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void showConfig(LaunchConfig cfg) {
        System.out.println("\nТекущая конфигурация узла:");
        System.out.println("Name: " + cfg.userName);
        System.out.println("IP: " + cfg.ipAddress);
        System.out.println("TCP port: " + cfg.tcpPort);
        System.out.println("UDP port: " + cfg.udpPort);
    }

    private static class LaunchConfig {
        String userName = "User";
        String ipAddress = "127.0.0.1";
        int tcpPort = 9000;
        int udpPort = 8888;
    }
}
