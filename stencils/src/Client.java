import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private static Socket s = null;
    private static InputStreamReader inputStream = null;
    private static OutputStreamWriter outputStream = null;
    private static BufferedReader bufferedReader = null;
    private static BufferedWriter bufferedWriter = null;

    private static void initializeClient(int port) {
        try{
            s = new Socket("localhost", port);
            inputStream = new InputStreamReader(s.getInputStream());
            outputStream = new OutputStreamWriter(s.getOutputStream());
            bufferedReader = new BufferedReader(inputStream);
            bufferedWriter = new BufferedWriter(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String receiveMessage() {
        try {
            String aux = bufferedReader.readLine();

            if (aux.equals("Predo")){
                System.out.println("WILL SLEEP, GOOD BYE!");
                Thread.sleep(10000);
                System.out.println("WAKE UP SAMURAI!");
            }

            System.out.println("Server: YOUR NAME IS: " + aux);
            return aux;
        } catch (IOException e) {
            return "ERROR";
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void sendMessage(String msg) {
        try {
            bufferedWriter.write(msg+'\n');
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        initializeClient(1212);
        while (true){
            sendMessage(receiveMessage());
        }
    }

}