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
        System.out.println("Running...");

        while (true) {
            final Image image;

            try {
//                System.out.println("Receiving object from server");
                image = (Image) this.inputStream.readObject();
//                System.out.println("Success");
            } catch (EOFException e) {
                System.out.println("Server sent no more objects, closing");
                return;
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error reading from server");
                throw new RuntimeException(e);
            }

            image.doStencilIteration();

            try {
//                System.out.println("Sending object back to server");
                this.outputStream.writeObject(image);
                this.outputStream.reset();
//                System.out.println("Sucess");
            } catch (IOException e) {
                System.out.println("Error writing to server");
                throw new RuntimeException(e);
            }
        }
    }
}
