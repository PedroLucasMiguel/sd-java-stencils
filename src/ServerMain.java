import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import image.ImageDelegate;
import image.ImageDelegate.Image;

public class ServerMain {
    static final int ITERATION_COUNT = 1;
    public static int PORT;
    public static int CLIENT_COUNT;
    private static String INPUT_FILE_PATH;
    private static String OUTPUT_FILE_PATH;

    public static void main(String[] args) {

        if (args.length != 4) {
            System.out.println("ServerMain help:");
            System.out.println("\tjava ServerMain <PORT> <N_CLIENTS> <INPUT_FILE_PATH> <OUTPUT_FILE_PATH>");
            return;
        } else {
            PORT = Integer.parseInt(args[0]);
            CLIENT_COUNT = Integer.parseInt(args[1]);
            INPUT_FILE_PATH = args[2];
            OUTPUT_FILE_PATH = args[3];
        }

        System.out.println("Starting program");

        final var server = initAndConnectServer();
        final var imageDelegate = getImageFromFile();
        final var finalImage = server.runProcedure(imageDelegate, ITERATION_COUNT);

        outputImageToFile(finalImage);

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
        try (final var s = new Scanner(new File(INPUT_FILE_PATH))) {
            return ImageDelegate.fromScanner(s);
        } catch (FileNotFoundException e) {
            System.out.printf("File %s not found\n", INPUT_FILE_PATH);
            throw new RuntimeException(e);
        } catch (NumberFormatException e) {
            System.out.println("Error reading file");
            throw new RuntimeException(e);
        }
    }

    private static void outputImageToFile(final Image finalImage) {
        try (final var fw = new FileWriter(OUTPUT_FILE_PATH)) {
            System.out.println("Output image to " + OUTPUT_FILE_PATH);
            fw.write(finalImage.toString());
        } catch (IOException e) {
            System.out.println("Unable to write to file");
            throw new RuntimeException(e);
        }
    }
}