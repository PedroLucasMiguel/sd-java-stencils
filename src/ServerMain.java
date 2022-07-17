import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import image.ImageDelegate;

public class ServerMain {
    public static final int PORT = 5000;
    public static final int CLIENT_COUNT = 2;
    static final int ITERATION_COUNT = 10000;
    private static final String INPUT_FILE_PATH = "/home/mathrpg/College/DistributedStencil/resources/testImage256.dat";
    private static final String OUTPUT_FILE_PATH = "out-testImage256.dat";

    public static void main(String[] args) {
        System.out.println("Starting program");

        final var server = initAndConnectServer();
        final var imageDelegate = getImageFromFile();
        final var finalImage = server.runProcedure(imageDelegate, ITERATION_COUNT);
        outputImageToFile(finalImage, OUTPUT_FILE_PATH);

        try {
            server.closeConnections();
        } catch (IOException e) {
            System.out.println("Fatal error");
            throw new RuntimeException(e);
        }
    }

    private static Server initAndConnectServer() {
        System.out.println("Initializing server");
        try {
            final var server = new Server(PORT, CLIENT_COUNT);
            server.openConnections();
            System.out.println("Server initialized successfully");
            return server;
        } catch (IOException e) {
            System.out.println("Critial error, aborting");
            throw new RuntimeException(e);
        }
    }

    private static ImageDelegate getImageFromFile() {
        final ImageDelegate imageDelegate;

        try (final var s = new Scanner(new File(INPUT_FILE_PATH))) {
            imageDelegate = ImageDelegate.fromScanner(s);
        } catch (FileNotFoundException e) {
            System.out.printf("File %s not found\n", INPUT_FILE_PATH);
            throw new RuntimeException(e);
        } catch (NumberFormatException e) {
            System.out.println("Error reading file");
            throw new RuntimeException(e);
        }
        return imageDelegate;
    }

    private static void outputImageToFile(final ImageDelegate.Image finalImage, final String outputFilePath) {
        System.out.printf("Outputing image to %s\n", outputFilePath);
    }
}
