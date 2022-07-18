import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import image.ImageDelegate.Image;

public class Client {
    private final Socket socket;
    private final ObjectOutputStream outputStream;
    private final ObjectInputStream inputStream;

    public Client(final String path, final int port) throws IOException {
        socket = new Socket(path, port);
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        inputStream = new ObjectInputStream(socket.getInputStream());
    }

    public void listenAndProcess() {
        // Inicializa o precesso de espera de mensagens pelo servidor, calculo e envio do resultado.
        System.out.println("Running...");

        while (true) {
            final var image = readImage();

            if (image == null) {
                System.out.println("Server sent no more objects, closing");
                return;
            }

            image.doStencilIteration();

            writeImage(image);
        }
    }

    private Image readImage() {
        // Lê a mensagem enviada pelo servidor
        try {
            return (Image) this.inputStream.readObject();
        } catch (EOFException e) {
            return null;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error reading from server");
            throw new RuntimeException(e);
        }
    }

    private void writeImage(Image image) {
        // Envia a matriz "processada" de volta ao servidor
        try {
            this.outputStream.writeObject(image);
            this.outputStream.reset();
        } catch (IOException e) {
            System.out.println("Error writing to server");
            throw new RuntimeException(e);
        }
    }

    public void closeConnection() throws IOException {
        // Fecha as conexões
        socket.close();
        outputStream.close();
        inputStream.close();
    }
}
