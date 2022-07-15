import calculation.ColorMatrix;
import utils.FileManager;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static ServerSocket ss = null;
    private static final int port = 1212;
    private static ClientHandler[] clientQueue;
    private static int nClients = 1;

    private static void initServer(int qtdClients) {
        try {
            ss = new ServerSocket(port);
            nClients = qtdClients;
            clientQueue = new ClientHandler[nClients];
            System.out.printf("Server started!\nExpecting %d connections...\n\n", nClients);
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

    private static ColorMatrix runActivity(String path, int nIteractions) {

        ColorMatrix matrix = null;

        try {
            var f = new File(path).getAbsoluteFile();
            matrix = ColorMatrix.fromFile(f);

            var k = matrix.getInnerSize() / nClients;

            for (int n = 0; n < nIteractions; n++) {
                System.out.println("Iteration: " + (n + 1));
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
        // Todo: Validation to string args?
        if (args.length != 4) {
            System.out.println("Correct use:");
            System.out.println("java server <qtdClients> <inputFilePath> <nIteractions> <outputFilePath>");
        } else {
            initServer(Integer.parseInt(args[0]));
            System.out.println("Initializing action...");
            allowConnections();
            var matrix = runActivity(args[1], Integer.parseInt(args[2]));

            FileManager.toFile(matrix, args[3]);

            System.out.println("Finished");
        }

    }

}