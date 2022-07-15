package calculation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ColorMatrixTests {
    public static void main(final String[] args) {
        printExampleMatrix();
        printFileReadMatrix();
    }

    private static void printExampleMatrix() {
        final var matrix = new ColorMatrix(5, List.of(
                new FixedPoint(new Color(100, 100, 100), 0, 0),
                new FixedPoint(new Color(255, 0, 0), 1, 2),
                new FixedPoint(new Color(0, 255, 255), 2, 2),
                new FixedPoint(new Color(0, 255, 255), 4, 4)
        ));

        System.out.println(matrix);
    }

    private static void printFileReadMatrix() {
        final File tmp;

        try (final var fw = new FileWriter(tmp = File.createTempFile("test", "txt"))) {
            fw.write("""
                    5 2
                    1 1 255 0 0
                    0 0 0 255 0"""
            );
        } catch (final IOException e) {
            System.out.println("Error creating temporary file:\n" + e);
            return;
        }

        try {
            final var matrix = ColorMatrix.fromFile(tmp);
            System.out.println(matrix);
        } catch (final FileNotFoundException e) {
            System.out.println("File not found when creating matrix:\n" + e);
        }
    }
}
