import java.io.IOException;

public class ClientMain {
    public static final int PORT = 5000;

    public static void main(String[] args) {
        System.out.println("Starting client");

        try {
            final var client = new Client("localhost", PORT);
            client.listenAndProcess();
        } catch (IOException e) {
            System.out.println("Error in client");
            throw new RuntimeException(e);
        }
    }
}
