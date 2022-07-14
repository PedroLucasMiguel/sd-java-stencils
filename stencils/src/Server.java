import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static Socket s = null;
    private static ServerSocket ss = null;
    private static InputStreamReader isr = null;
    private static OutputStreamWriter isw = null;
    private static BufferedReader br = null;
    private static BufferedWriter bw = null;

    private static void initServerSocket() {
        try{
            ss = new ServerSocket(1212);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private static void initSocketAndStreams(Socket ssAccepted) {
        try{
            s = ssAccepted;
            isr = new InputStreamReader(s.getInputStream());
            isw = new OutputStreamWriter(s.getOutputStream());
            br = new BufferedReader(isr);
            bw = new BufferedWriter(isw);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private static void runServer() {

        int c = 0;

        System.out.println("Starting...");

        while(true) {
            try {
                initSocketAndStreams(ss.accept());

                while (true) {
                    String rMsg = br.readLine();
                    System.out.println("RECEIVED: " + rMsg);
                    bw.write("ACK");
                    bw.newLine();
                    bw.flush();

                    bw.write("CLOSE");
                    bw.newLine();
                    bw.flush();

                    c++;

                    if (c == 3)
                        break;
                }

                s.close();
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void terminateServer() {
        try {
            if (ss != null)
                ss.close();
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
        initServerSocket();
        runServer();
        terminateServer();
    }
}
