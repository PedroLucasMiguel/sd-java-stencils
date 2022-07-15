import calculation.Color;

import java.io.*;
import java.net.Socket;

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

    private static Color receiveMessage() {
        try {
            Color aux = (Color) inputStream.readObject();
            System.out.println("The color is: " + aux.r() + " " + aux.g() + " " + aux.b());
            return aux;
        } catch (IOException e) {
            return null;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static void sendMessage() {
        try {
            outputStream.writeObject(new Color(20,30,40));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        initializeClient(1212);
        sendMessage();
        while (true) {
            receiveMessage();
        }
    }

}