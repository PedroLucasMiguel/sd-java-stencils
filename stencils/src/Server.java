import calculation.Color;
import calculation.ColorMatrix;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static ServerSocket ss = null;
    private static ClientHandler[] clientQueue;
    private static int nClients = 1;

    private static void initServer(int port, int qtdClients) {
        try {
            ss = new ServerSocket(port);
            nClients = qtdClients;
            clientQueue = new ClientHandler[nClients];
        } catch (IOException e) {
            System.out.println("Error while instantiating ServerSocket");
            e.printStackTrace();
        }
    }

    private static void allowConnections() {
        Socket aux = null;
        System.out.println("Listening to connections...");
        try {
            for (int i = 0; i < nClients; i++) {
                aux = ss.accept();
                System.out.printf("Connection %d/%d accepted from: %s\n", i+1, nClients, aux.getInetAddress().toString());
                clientQueue[i] = new ClientHandler(aux);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void runActivity() {

        ColorMatrix matrix = null;

        try {
            File f = new File("C:\\git\\sd-java-stencils\\stencils\\src\\utils\\test.dat");
            matrix = ColorMatrix.fromFile(f);
            var sMatrix = matrix.splice(0, 7);

            // Enviando mensagens
            for (int i = 0; i < nClients; i++) {
                clientQueue[i].sendMessage(sMatrix);
            }

            // Recebendo mensagens
            for (int i = 0; i < nClients; i++) {
                var aux = clientQueue[i].getResponse();
                matrix.updateLines(aux, 1, 5);
                System.out.println(matrix);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        initServer(1212, 1);
        allowConnections();
        runActivity();
    }

}