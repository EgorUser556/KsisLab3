package P2Pchat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer implements Runnable {

    private final PeerNode owner;
    private final String bindIp;
    private final int listenPort;
    private volatile ServerSocket serverSocket;

    public TcpServer(PeerNode owner, String bindIp, int listenPort) {
        this.owner = owner;
        this.bindIp = bindIp;
        this.listenPort = listenPort;
    }

    @Override
    public void run() {
        try (ServerSocket server = new ServerSocket()) {
            server.bind(new InetSocketAddress(bindIp, listenPort));
            this.serverSocket = server;
            while (true) {
                Socket socket = server.accept();

                PeerInfo peer = new PeerInfo(
                        "Unknown",
                        socket.getInetAddress().getHostAddress(),
                        socket.getPort()
                );
                peer.attach(socket);

                Thread handlerThread = new Thread(new PeerConnectionHandler(owner, socket, peer));
                handlerThread.setDaemon(true);
                handlerThread.start();
            }
        } catch (Exception ignored) {
        }
    }

    public void close() {
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {}
    }
}