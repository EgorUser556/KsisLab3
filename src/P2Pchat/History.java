package P2Pchat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class History {

    private final List<Message> entries =
            Collections.synchronizedList(new ArrayList<>());

    public void add(Message message) {
        entries.add(message);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        synchronized (entries) {
            for (Message m : entries) {
                builder.append(m.getFormattedMessage())
                        .append(System.lineSeparator());
            }
        }
        return builder.toString();
    }
}
