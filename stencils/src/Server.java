import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {

    private static ServerSocket ss = null;
    private static ClientHandler[] clientQueue;
    private static int queueSize = 1;

    private static void initServer(int port, int qtdClients) {
        try {
            ss = new ServerSocket(port);
            queueSize = qtdClients;
            clientQueue = new ClientHandler[queueSize];
        } catch (IOException e) {
            System.out.println("Error while instantiating ServerSocket");
            e.printStackTrace();
        }
    }

    private static void allowConnections() {
        Socket aux = null;
        System.out.println("Listening to connections...");
        try {
            for (int i = 0; i < queueSize; i++) {
                aux = ss.accept();
                System.out.println("Connection " + (i+1) + "/" + queueSize + " accepted from: " + aux.getInetAddress());
                clientQueue[i] = new ClientHandler(aux);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void runActivity() {
        Scanner sc = new Scanner(System.in);

        while (true) {
            // Enviando nomes
            for (int i = 0; i < queueSize; i++) {
                System.out.println("Name: ");
                clientQueue[i].sendMessage(sc.nextLine());
            }

            // Recebendo nomes
            for (int i = 0; i < queueSize; i++) {
                System.out.println(clientQueue[i].getResponse());
            }
        }
    }

    public static void main(String[] args) {
        initServer(1212, 2);
        allowConnections();
        runActivity();
    }

}