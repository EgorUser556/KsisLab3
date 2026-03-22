package P2Pchat;

import java.io.*;
import java.net.Socket;

public class PeerInfo {
    private String name;
    private final String ip;
    private int port;
    private ObjectOutputStream out;

    public PeerInfo(String name, String ip, int port) {
        this.name = name; this.ip = ip; this.port = port;
    }

    public synchronized void setSocket(Socket socket) throws IOException {
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
    }

    public String getKey() {
        return ip + ":" + port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public ObjectOutputStream getOut() {
        return out;
    }
}