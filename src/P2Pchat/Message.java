package P2Pchat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message implements Serializable {

    private final Type type;
    private final String body;
    private final String authorName;
    private final String authorIp;
    private final int authorTcpPort;
    private final LocalDateTime createdAt;

    public Message(Type type,
                   String body,
                   String author,
                   String authorIp,
                   int authorTcpPort) {
        this.type = type;
        this.body = body;
        this.authorName = author;
        this.authorIp = authorIp;
        this.authorTcpPort = authorTcpPort;
        this.createdAt = LocalDateTime.now();
    }

    public String getFormattedMessage() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String time = createdAt.format(formatter);

        return switch (type) {
            case TEXT -> String.format("[%s] %s (%s): %s",
                    time, authorName, authorIp, body);
            case NODE_JOIN -> String.format("[%s] %s (%s) подключился",
                    time, authorName, authorIp);
            case NODE_LEAVE -> String.format("[%s] %s (%s) отключился",
                    time, authorName, authorIp);
            case HISTORY_GET -> String.format("[%s] [История запрошена у %s (%s)]",
                    time, authorName, authorIp);
            case HISTORY_DATA -> String.format("[%s] [История получена от %s (%s)]",
                    time, authorName, authorIp);
            case INTRO -> String.format("[%s] [Intro from %s (%s)]",
                    time, authorName, authorIp);
        };
    }

    public Type getType() {
        return type;
    }

    public String getBody() {
        return body;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getSenderIp() {
        return authorIp;
    }

    public int getAuthorTcpPort() {
        return authorTcpPort;
    }
}
