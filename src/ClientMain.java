import java.io.IOException;

public class ClientMain {
    public static int PORT;

    public static String IP;

    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("ClientMain help:");
            System.out.println("\tjava ClientMain <IP> <PORT>");
            return;
        } else {
            IP = args[0];
            PORT = Integer.parseInt(args[1]);
        }

        System.out.println("Starting client");

        try {
            final var client = new Client(IP, PORT);
            client.listenAndProcess();
            client.closeConnection();
        } catch (IOException e) {
            System.out.println("Error in client");
            throw new RuntimeException(e);
        }
    }
}
