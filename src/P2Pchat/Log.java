package P2Pchat;

import java.util.Scanner;

public class Log implements PeerNode.MessageListener {

    private final PeerNode peer;
    private final Scanner input = new Scanner(System.in);

    public Log(PeerNode peer) {
        this.peer = peer;
        this.peer.setListener(this);
    }

    public void start() {
        System.out.println("============== LAB 3 ==============");
        System.out.println("============= P2P CHAT ============");
        System.out.println("Введите /exit для завершения работы\n");

        while (true) {
            System.out.print("> ");
            String line = input.nextLine();

            if (line == null) {
                continue;
            }

            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            if ("/exit".equalsIgnoreCase(line)) {
                System.exit(0);
            }

            peer.sendChatMessage(line);
        }
    }

    @Override
    public void onMessage(String msg) {
        System.out.println();
        System.out.println("[LOG] " + msg);
        System.out.print("> ");
    }
}
