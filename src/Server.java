import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.stream.IntStream;

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
            this.clients[i] = new ClientHandler(i + 1, socket);
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
            imageDelegate.updateImageFixedPoints();

//            System.out.println("Merged segments (w/ update):");
//            System.out.println(imageDelegate.getImage());
        }

        System.out.println("Procedure finished");
        return imageDelegate.getImage();
    }

    private Image[] readSegmentsFromClients() {
        return Arrays.stream(this.clients)
                .parallel()
                .map(client -> {
                    try {
                        return client.readImage();
                    } catch (IOException | ClassNotFoundException e) {
                        System.out.println("Failure in reading from " + client);
                        throw new RuntimeException(e);
                    }
                }).toArray(Image[]::new);
    }

    private void writeSegmentsToClients(final Image[] images) {
        IntStream.range(0, clientCount)
                .parallel()
                .forEach(i -> {
                    try {
                        this.clients[i].writeImage(images[i]);
                    } catch (IOException e) {
                        System.out.println("Failure in writing to " + this.clients[i]);
                        throw new RuntimeException(e);
                    }
                });
    }

    private static final class ClientHandler {
        private final int id;
        private final Socket socket;
        private final ObjectOutputStream outputStream;
        private final ObjectInputStream inputStream;

        public ClientHandler(int id, final Socket socket) throws IOException {
            this.id = id;
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

        @Override
        public String toString() {
            return "Client #" + id;
        }
    }
}