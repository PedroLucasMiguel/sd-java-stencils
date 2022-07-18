import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import image.ImageDelegate;
import image.ImageDelegate.Image;

public class Server {
    public static final int PRINT_EVERY_NTH_ITERATION = 100;
    private final int port;
    private final int clientCount;
    private final ServerSocket serverSocket;
    private final ClientHandler[] clients;

    public Server(int port, int clientCount) throws IOException {
        this.port = port;
        this.clientCount = clientCount;
        this.serverSocket = new ServerSocket(this.port);
        this.clients = new ClientHandler[clientCount];
    }

    public void openConnections() throws IOException {
        System.out.println("Listening for connections on port " + port);

        for (int i = 0; i < clientCount; ++i) {
            final var socket = serverSocket.accept();
            System.out.printf(
                    "Connection %d/%d accepted from: %s%n",
                    i + 1, clientCount,
                    socket.getInetAddress().toString()
            );
            this.clients[i] = new ClientHandler(socket);
        }
    }

    public void closeConnections() throws IOException {
        System.out.println("Closing client connections");

        for (int i = 0; i < clientCount; ++i) {
            this.clients[i].close();
        }
    }

    public Image runProcedure(final ImageDelegate imageDelegate, final int iterationCount) {
        System.out.println("Starting procedure");

        for (int iter = 1; iter <= iterationCount; ++iter) {
            if (iter % PRINT_EVERY_NTH_ITERATION == 0)
                System.out.printf("Running iteration #%d\n", iter);

            final var images = imageDelegate.split(clientCount);

//            System.out.println("Sending images:");
//            System.out.println('[');
//            for (final var image : images) {
//                System.out.println(image);
//                System.out.println(',');
//            }
//            System.out.println(']');

            writeSegmentsToClients(images);

            final var segments = readSegmentsFromClients();

//            System.out.println("Received images:");
//            System.out.println('[');
//            for (final var image : segments) {
//                System.out.println(image);
//                System.out.println(',');
//            }
//            System.out.println(']');

            imageDelegate.merge(segments);

//            System.out.println("Merged segments (w/ update):");
//            System.out.println(imageDelegate.getImage());
        }

        System.out.println("Procedure finished");
        return imageDelegate.getImage();
    }

    private Image[] readSegmentsFromClients() {
        final var newImages = new Image[clientCount];
        for (int i = 0; i < clientCount; ++i) {
            try {
                newImages[i] = this.clients[i].readImage();
            } catch (IOException | ClassNotFoundException e) {
                System.out.printf("Failure in reading from client #%d\n", i + 1);
                throw new RuntimeException(e);
            }
        }
        return newImages;
    }

    private void writeSegmentsToClients(final Image[] images) {
        for (int i = 0; i < clientCount; ++i) {
            try {
                this.clients[i].writeImage(images[i]);
            } catch (IOException e) {
                System.out.printf("Failure in writing to client #%d\n", i + 1);
                throw new RuntimeException(e);
            }
        }
    }

    private final class ClientHandler {
        private final Socket socket;
        private final ObjectOutputStream outputStream;
        private final ObjectInputStream inputStream;

        public ClientHandler(final Socket socket) throws IOException {
            this.socket = socket;
            this.outputStream = new ObjectOutputStream(this.socket.getOutputStream());
            this.inputStream = new ObjectInputStream(this.socket.getInputStream());
        }

        public Image readImage() throws IOException, ClassNotFoundException {
            return (Image) this.inputStream.readObject();
        }

        public void writeImage(final Image image) throws IOException {
            this.outputStream.writeObject(image);
            this.outputStream.reset();
        }

        public void close() throws IOException {
            this.socket.close();
            this.outputStream.close();
            this.inputStream.close();
        }
    }
}