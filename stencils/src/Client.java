import calculation.Calculator;
import calculation.Color;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class Client {

    private static Socket s = null;
    private static ObjectOutputStream outputStream = null;
    private static ObjectInputStream inputStream = null;

    private static void initializeClient(int port) {
        try{
            s = new Socket("localhost", port);
            outputStream = new ObjectOutputStream(s.getOutputStream());
            inputStream = new ObjectInputStream(s.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Color[][] receiveMessage() {
        try {
            Color[][] aux = (Color[][]) inputStream.readObject();
            var aux2 = Calculator.innerCellStencilAverage(aux, 7, 7);
            return aux2;
        } catch (IOException e) {
            return null;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static void sendMessage(Color[][] matrix) {
        try {
            outputStream.writeObject(matrix);
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
        initializeClient(1212);
        sendMessage(receiveMessage());
    }

}