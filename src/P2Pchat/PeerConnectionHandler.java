package P2Pchat;

import java.io.ObjectInputStream;
import java.net.Socket;

public record PeerConnectionHandler(PeerNode node, Socket socket, PeerInfo peer) implements Runnable {

    @Override
    public void run() {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            while (true) {
                Object obj = in.readObject();
                if (!(obj instanceof Message msg)) {
                    continue;
                }

                switch (msg.getType()) {
                    case INTRO -> node.handlePeerIntroduction(peer, msg);
                    case TEXT -> node.appendMessageToHistory(msg);
                    case HISTORY_GET -> node.sendHistory(peer);
                    case HISTORY_DATA -> node.receiveChatHistory(
                            msg.getBody(),
                            msg.getAuthorName(),
                            msg.getSenderIp()
                    );
                }
            }
        } catch (Exception e) {
            node.handleDisconnect(peer);
        }
    }
}
