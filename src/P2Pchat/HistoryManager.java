package P2Pchat;

import java.util.*;

public class HistoryManager {
    private final List<Message> history = Collections.synchronizedList(new ArrayList<>());

    public void add(Message msg) {
        history.add(msg);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        synchronized (history) {
            for (Message m : history) sb.append(m.getFormattedMessage()).append("\n");
        }
        return sb.toString();
    }
}