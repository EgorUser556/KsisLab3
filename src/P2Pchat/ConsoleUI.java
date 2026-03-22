package P2Pchat;

import java.util.Scanner;

public class ConsoleUI implements PeerNode.MessageListener {
    private final PeerNode node;
    public ConsoleUI(PeerNode node) { this.node = node; this.node.setListener(this); }

    public void start() {
        System.out.println("                  LAB3\n");
        System.out.println("                  P2P CHAT\n");

        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            if (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.equalsIgnoreCase("/exit") || line.equalsIgnoreCase("/quit")) System.exit(0);
                if (!line.isEmpty()) node.sendChatMessage(line);
            }
        }
    }

    @Override
    public void onMessage(String msg) {
        System.out.println(msg);
    }
}