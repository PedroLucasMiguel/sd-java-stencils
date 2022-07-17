import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import image.ImageDelegate;
import image.ImageDelegate.Image;

public class Server {
    private final int port;
    private final int clientCount;
    private final ServerSocket serverSocket;
    private final Socket[] clientSockets;

    public Server(int port, int clientCount) throws IOException {
        this.port = port;
        this.clientCount = clientCount;
        this.serverSocket = new ServerSocket(this.port);
        this.clientSockets = new Socket[clientCount];
    }

    public void allowConnections() throws IOException {
        System.out.println("Listening for connections on port " + port);

        for (int i = 0; i < clientCount; ++i) {
            final var socket = serverSocket.accept();
            System.out.printf(
                    "Connection %d/%d accepted from: %s%n",
                    i + 1, clientCount,
                    socket.getInetAddress().toString()
            );
            this.clientSockets[i] = socket;
        }
    }

    public Image runProcedure(final ImageDelegate imageDelegate, final int iterationCount) {
        System.out.println("Starting procedure");

        for (int iter = 1; iter <= iterationCount; ++iter) {
            System.out.printf("Running iteration #%d\n", iter);

            final var images = imageDelegate.split(clientCount);
            writeSegmentsToClients(images);

            final var segments = readSegmentsFromClients();
            imageDelegate.merge(segments);
        }

        System.out.println("Procedure finished");
        return imageDelegate.getImage();
    }

    private Image[] readSegmentsFromClients() {
        final var newImages = new Image[clientCount];
        for (int i = 0; i < clientCount; ++i) {
            try (final var is = new ObjectInputStream(this.clientSockets[i].getInputStream())) {
                newImages[i] = (Image) is.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.out.printf("Failure in reading from client #%d\n", i + 1);
                throw new RuntimeException(e);
            }
        }
        return newImages;
    }

    private void writeSegmentsToClients(Image[] images) {
        for (int i = 0; i < clientCount; ++i) {
            try (final var os = new ObjectOutputStream(this.clientSockets[i].getOutputStream())) {
                os.writeObject(images[i]);
            } catch (IOException e) {
                System.out.printf("Failure in writing to client #%d\n", i + 1);
                throw new RuntimeException(e);
            }
        }
    }
}