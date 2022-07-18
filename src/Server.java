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
    public static final int PRINT_EVERY_NTH_ITERATION = 100; // N° de iterações necessária entre prints de debug
    private final int port;
    private final int clientCount; // Quantidade de clientes esperada
    private final ServerSocket serverSocket;
    private final ClientHandler[] clients; // "Lista" de clientes

    public Server(int port, int clientCount) throws IOException {
        this.port = port;
        this.clientCount = clientCount;
        this.serverSocket = new ServerSocket(this.port);
        this.clients = new ClientHandler[clientCount];
    }

    public void openConnections() throws IOException {
        // Inicia o processo de espera por conexões por parte do servidor
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
        // Finaliza todas as conexões após a execução do processo
        System.out.println("Closing client connections");

        for (int i = 0; i < clientCount; ++i) {
            this.clients[i].close();
        }
    }

    public Image runProcedure(final ImageDelegate imageDelegate, final int iterationCount) {
        // Inicializa o procedimento
        System.out.println("Starting procedure");

        for (int iter = 1; iter <= iterationCount; ++iter) {
            if (iter % PRINT_EVERY_NTH_ITERATION == 0)
                System.out.printf("Running iteration #%d\n", iter);

            // Realiza o "split" da imagem e envia os pedaços para os clientes conectados
            final var images = imageDelegate.split(clientCount);
            writeSegmentsToClients(images);

            // Espera a resposta dos clientes
            final var segments = readSegmentsFromClients();

            // Junta as respostas e atualiza a matriz
            imageDelegate.merge(segments);
            imageDelegate.updateImageFixedPoints();

        }

        System.out.println("Procedure finished");
        return imageDelegate.getImage();
    }

    private Image[] readSegmentsFromClients() {
        // Salva as respostas dos clientes em um "Java Array" para futuras manipulações
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
        // Reliza o envio dos "pedaços" da matriz para cada cliente
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

    /*
    * A classe "ClientHandler" é utilizada para realizar a manipulação
    * de clientes que posteriormente se conectatem ao servidor.
    *
    * Essa classe salva o socket responsável pela comunicação, além de
    * criar os canais de comunicação em si.
    * */

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