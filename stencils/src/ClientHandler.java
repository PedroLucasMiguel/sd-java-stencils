import calculation.Color;

import java.io.*;
import java.net.Socket;

/*
* Classe responsável por armazenar cada cliente que é conectado ao servidor.
* Essa classe fornece uma interface que faz com que o servidor se torne capaz de ouvir
* e responder o cliente
* */

public class ClientHandler {

    private Socket s = null;
    private ObjectOutputStream outputStream = null;
    private ObjectInputStream inputStream = null;


    public ClientHandler(Socket s) {
        try {
            this.s = s;
            this.outputStream = new ObjectOutputStream(s.getOutputStream());
            this.inputStream = new ObjectInputStream(s.getInputStream());
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Color[][] getResponse() {
        try {
            return (Color[][]) this.inputStream.readObject();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(Color[][] msg) {
        try {
            this.outputStream.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
