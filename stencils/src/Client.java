import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

    private static Socket s = null;
    private static InputStreamReader isr = null;
    private static OutputStreamWriter isw = null;
    private static BufferedReader br = null;
    private static BufferedWriter bw = null;

    private static void initSocketAndStreams() {
        try{
            s = new Socket("localhost", 1212);
            isr = new InputStreamReader(s.getInputStream());
            isw = new OutputStreamWriter(s.getOutputStream());
            br = new BufferedReader(isr);
            bw = new BufferedWriter(isw);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private static void sendMessage(String msg) {
        try {
            bw.write(msg);
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void waitForAnswer(String msg2send) {
        try {
            System.out.println(msg2send);
            sendMessage(msg2send);

            while (true){
                String msg = br.readLine();

                if (msg.equalsIgnoreCase("CLOSE")) {
                    System.out.println("Closing connection...");
                    break;
                }

                System.out.println(msg);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void terminateClient() {
        try {
            if (s != null)
                s.close();
            if (br != null)
                br.close();
            if (bw != null)
                bw.close();
            if (isr != null)
                isr.close();
            if (isw != null)
                isw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // init stuff
        initSocketAndStreams();

        waitForAnswer("Hello Socket!");
        waitForAnswer("Hello Socket1!");
        waitForAnswer("Hello Socket3!");

        terminateClient();
    }

}
