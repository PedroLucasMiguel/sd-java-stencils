import calculation.Calculator;
import calculation.Color;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class Client {

    private static Socket s = null;

    private static final int port = 1212;
    private static ObjectOutputStream outputStream = null;
    private static ObjectInputStream inputStream = null;

    private static void initializeClient(String svAddr) {
        try {
            System.out.printf("Connecting to %s using port %d\n", svAddr, port);
            s = new Socket(svAddr, port);
            outputStream = new ObjectOutputStream(s.getOutputStream());
            inputStream = new ObjectInputStream(s.getInputStream());
            System.out.println("Connected.");
        } catch (IOException e) {
            System.out.println("ERROR: Failed to create socket!");
            e.printStackTrace();
        }
    }

    private static Color[][] receiveMessage() throws SocketException {
        try {
            Color[][] aux = (Color[][]) inputStream.readObject();
            return Calculator.innerCellStencilAverage(aux);
        } catch (IOException e) {
            return null;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static void sendMessage(Color[][] matrix) throws SocketException {
        try {
            outputStream.writeObject(matrix);
        } catch (SocketException e) {
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printMatrix(final Color[][] matrix, final int x, final int y) {
        for (int i = 0; i < x; ++i) {
            for (int j = 0; j < y; ++j) {
                System.out.print(matrix[i][j]);
                System.out.print(' ');
            }
            System.out.println();
        }
        System.out.println();
    }

    public static void main(String[] args) {
        // Todo: Validation to string args?
        if (args.length != 1) {
            System.out.println("Correct use: ");
            System.out.println("java client <ip>");
        } else {
            initializeClient(args[0]);

            try {
                System.out.println("Initializing work....");
                while (true) {
                    sendMessage(receiveMessage());
                }
            } catch (SocketException e) {
                System.out.println("Finished...");
            }
        }

    }

}