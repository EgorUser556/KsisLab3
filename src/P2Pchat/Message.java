package P2Pchat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message implements Serializable {
    private final MessageType type;
    private final String content;
    private final String senderName;
    private final String senderIp;
    private final int senderTcpPort;
    private final LocalDateTime timestamp;

    public Message(MessageType type, String content, String senderName, String senderIp, int senderTcpPort) {
        this.type = type;
        this.content = content;
        this.senderName = senderName;
        this.senderIp = senderIp;
        this.senderTcpPort = senderTcpPort;
        this.timestamp = LocalDateTime.now();
    }

    public String getFormattedMessage() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String timeStr = timestamp.format(formatter);

        return switch (type) {
            case CHAT_MESSAGE -> String.format("[%s] %s (%s): %s", timeStr, senderName, senderIp, content);
            case PEER_CONNECTED -> String.format("[%s] +++ %s (%s) connected", timeStr, senderName, senderIp);
            case PEER_DISCONNECTED -> String.format("[%s] --- %s (%s) disconnected", timeStr, senderName, senderIp);
            case HISTORY_REQUEST -> String.format("[%s] [History requested from %s (%s)]", timeStr, senderName, senderIp);
            case HISTORY_RESPONSE -> String.format("[%s] [History received from %s (%s)]", timeStr, senderName, senderIp);
            default -> String.format("[%s] %s", timeStr, content);
        };
    }

    public MessageType getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getSenderIp() {
        return senderIp;
    }

    public int getSenderTcpPort() {
        return senderTcpPort;
    }
}