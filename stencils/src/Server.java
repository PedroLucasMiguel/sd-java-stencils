import calculation.ColorMatrix;
import utils.FileManager;

import java.io.File;
import java.io.FileWriter;
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
                System.out.printf("Connection %d/%d accepted from: %s\n", i + 1, nClients, aux.getInetAddress().toString());
                clientQueue[i] = new ClientHandler(aux);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ColorMatrix runActivity() {

        ColorMatrix matrix = null;

        try {
            File f = new File("C:\\git\\sd-java-stencils\\stencils\\src\\utils\\img01.dat");
            matrix = ColorMatrix.fromFile(f);

            var k = matrix.getInnerSize() / nClients;

            for (int n = 0; n < 10000; n++) {
                System.out.println("Iteration: " + (n+1));
                // Enviando os pedaços
                for (int i = 0; i < nClients; i++) {
                    var auxMatrix = matrix.splice(i * k, k + 2);
                    clientQueue[i].sendMessage(auxMatrix);
                }

                // Recebendo mensagens
                for (int i = 0; i < nClients; i++) {
                    var aux = clientQueue[i].getResponse();
                    matrix.updateLines(aux, 1 + (i * k));
                }

            }

            System.out.println(matrix);

            return matrix;

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                ss.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return null;
    }

    public static void main(String[] args) {
        initServer(1212, 8);
        allowConnections();
        var matrix = runActivity();

        FileManager.toFile(matrix, "JOOOOOOOJ.dat");
    }

}