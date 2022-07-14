import java.io.*;
import java.net.Socket;

/*
* Classe responsável por armazenar cada cliente que é conectado ao servidor.
* Essa classe fornece uma interface que faz com que o servidor se torne capaz de ouvir
* e responder o cliente
* */

public class ClientHandler {

    private Socket s = null;
    private InputStreamReader inputStream = null;
    private OutputStreamWriter outputStream = null;
    private BufferedReader bufferedReader = null;
    private BufferedWriter bufferedWriter = null;


    public ClientHandler(Socket s) {
        try {
            this.s = s;
            inputStream = new InputStreamReader(s.getInputStream());
            outputStream = new OutputStreamWriter(s.getOutputStream());
            bufferedReader = new BufferedReader(inputStream);
            bufferedWriter = new BufferedWriter(outputStream);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getResponse() {
        try {
            return bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void sendMessage(String msg) {
        try {
            bufferedWriter.write(msg+'\n');
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
