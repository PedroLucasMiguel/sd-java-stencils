package image;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.IntStream;

public class ImageDelegate {
    private static final int GRAY = 127;
    private final int innerSize;
    private final int outerSize;
    private final List<FixedPoint> fixedPoints;
    private final Image image;

    public ImageDelegate(final int imageSize, final List<FixedPoint> fixedPoints) {
        this.innerSize = imageSize;
        this.outerSize = imageSize + 2;
        this.fixedPoints = fixedPoints;
        this.image = initializeImage(outerSize);
        updateImageFixedPoints();
    }

    private static int[][] borderedMatrix(final int size) {
        final var matrix = new int[size][size];
        Arrays.fill(matrix[0], GRAY);
        Arrays.fill(matrix[size - 1], GRAY);
        Arrays.stream(matrix).parallel()
                .forEach(row -> row[0] = row[size - 1] = GRAY);
        return matrix;
    }

    public static ImageDelegate fromScanner(final Scanner s) {
        final var imageSize = s.nextInt();
        final var fixedPointCount = s.nextInt();
        final var fixedPoints = IntStream.range(0, fixedPointCount)
                .mapToObj(i -> FixedPoint.fromScanner(s))
                .toList();

        return new ImageDelegate(imageSize, fixedPoints);
    }

    private void updateImageFixedPoints() {
        fixedPoints.parallelStream().forEach(fp -> {
            int i = fp.i() + 1;
            int j = fp.j() + 1;
            image.reds[i][j] = fp.r();
            image.greens[i][j] = fp.g();
            image.blues[i][j] = fp.b();
        });
    }

    private Image initializeImage(final int imageSize) {
        final var reds = borderedMatrix(imageSize);
        final var greens = borderedMatrix(imageSize);
        final var blues = borderedMatrix(imageSize);
        return new Image(reds, greens, blues);
    }

    public Image[] split(final int n) {
        final int offset = innerSize / n;
        final var images = new Image[n];

        for (int i = 0; i < n; ++i) {
            final int startIndex = i * offset;
            final int finalIndex = startIndex + outerSize;
            images[i] = new Image(
                    Arrays.copyOfRange(image.reds, startIndex, finalIndex),
                    Arrays.copyOfRange(image.greens, startIndex, finalIndex),
                    Arrays.copyOfRange(image.blues, startIndex, finalIndex)
            );
        }

        return images;
    }

    public void merge(final Image[] segments) {
        final int count = segments.length;
        final int offset = innerSize / count;

        final int width = segments[0].reds.length;

        for (int i = 0; i < count; ++i) {
            // Can't parallelize (for now), needs to be in order to overwrite wrong values
            copyPartialContents(i * offset, segments[i], width);
        }

        updateImageFixedPoints();
    }

    private void copyPartialContents(final int start, Image theImage, int rowCount) {
        // Avoid overwriting correct values from above
        System.arraycopy(theImage.reds, 1, image.reds, start + 1, rowCount - 1);
        System.arraycopy(theImage.greens, 1, image.greens, start + 1, rowCount - 1);
        System.arraycopy(theImage.blues, 1, image.blues, start + 1, rowCount - 1);
    }

    public Image getImage() {
        return image;
    }

    public static final class Image implements Serializable {
        int[][] reds;
        int[][] greens;
        int[][] blues;

        public Image(final int[][] reds, final int[][] greens, final int[][] blues) {
            this.reds = reds;
            this.greens = greens;
            this.blues = blues;
        }

        private static void averageNeighbors(final int[][] matrix, final int i, final int j) {
            matrix[i][j] = (matrix[i][j]
                    + matrix[i - 1][j]
                    + matrix[i][j - 1]
                    + matrix[i + 1][j]
                    + matrix[i][j + 1]) / 5;
        }

        public void doStencilIteration() {
            for (int i = 1; i < reds.length - 1; ++i) {
                for (int j = 1; j < reds[0].length - 1; ++j) {
                    averageNeighbors(reds, i, j);
                    averageNeighbors(greens, i, j);
                    averageNeighbors(blues, i, j);
                }
            }
        }
    }
}