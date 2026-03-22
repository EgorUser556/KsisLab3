package P2Pchat;

import java.io.*;
import java.net.Socket;

public record PeerConnectionHandler(PeerNode node, Socket s, PeerInfo p) implements Runnable {

    @Override
    public void run() {
        try (ObjectInputStream in = new ObjectInputStream(s.getInputStream())) {
            while (true) {
                Message m = (Message) in.readObject();
                switch (m.getType()) {
                    case NAME_TRANSFER -> node.onPeerIdentified(p, m);
                    case CHAT_MESSAGE -> node.addMsg(m);
                    case HISTORY_REQUEST -> node.sendHistory(p);
                    case HISTORY_RESPONSE -> node.receiveHistory(m.getContent(), m.getSenderName(), m.getSenderIp());
                }
            }
        } catch (Exception e) {
            node.handleDisconnect(p);
        }
    }
}