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
        this.image = Image.grayBorderedImage(this.outerSize);
        updateImageFixedPoints();
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
            for (int ch = 0; ch < 3; ++ch) {
                image.channels[ch][i][j] = fp.getChannelFor(ch);
            }
        });
    }

    public Image[] split(final int n) {
        final int offset = innerSize / n;
        final var images = new Image[n];

        for (int i = 0; i < n; ++i) {
            final int startIndex = i * offset;
            final int finalIndex = startIndex + offset + 2;
            images[i] = new Image(new int[][][]{
                    Arrays.copyOfRange(image.channels[0], startIndex, finalIndex),
                    Arrays.copyOfRange(image.channels[1], startIndex, finalIndex),
                    Arrays.copyOfRange(image.channels[2], startIndex, finalIndex)
            });
        }

        return images;
    }

    public void merge(final Image[] segments) {
        final int count = segments.length;
        final int offset = innerSize / count;
        final int width = segments[0].getWidth();
        final int height = segments[0].getHeight();

        for (int im = 0; im < count; ++im) {
            for (int ch = 0; ch < 3; ++ch) {
                for (int i = 1; i < width - 1; ++i) {
                    for (int j = 1; j < height - 1; ++j) {
                        this.image.channels[ch][offset * im + i][j] = segments[im].channels[ch][i][j];
                    }
                }
            }
        }

        updateImageFixedPoints();
    }

    public Image getImage() {
        return image;
    }

    public static final class Image implements Serializable {
        final int[][][] channels;

        public Image(final int[][][] channels) {
            this.channels = channels;
        }

        private static int neighborsAverageOrBorder(final int[][] src, final int i, final int j) {
            if (i == 0 || j == 0 || i == src.length - 1 || j == src[0].length - 1)
                return src[i][j];

            return (src[i][j]
                    + src[i - 1][j]
                    + src[i][j - 1]
                    + src[i + 1][j]
                    + src[i][j + 1]) / 5;
        }

        private static int[][] borderedMatrix(final int size) {
            final var matrix = new int[size][size];
            Arrays.fill(matrix[0], GRAY);
            Arrays.fill(matrix[size - 1], GRAY);
            Arrays.stream(matrix).parallel()
                    .forEach(row -> row[0] = row[size - 1] = GRAY);
            return matrix;
        }

        private static Image grayBorderedImage(final int imageSize) {
            final var reds = borderedMatrix(imageSize);
            final var greens = borderedMatrix(imageSize);
            final var blues = borderedMatrix(imageSize);
            return new Image(new int[][][]{reds, greens, blues});
        }

        public void doStencilIteration() {
            for (int i = 0; i < 3; ++i) {
                this.channels[i] = doStencilIterationForMatrix(this.channels[i]);
            }
        }

        private int[][] doStencilIterationForMatrix(int[][] target) {
            final var width = getWidth();
            final var height = getHeight();

            final var newMatrix = new int[width][height];

            for (int i = 0; i < width; ++i) {
                for (int j = 0; j < height; ++j) {
                    newMatrix[i][j] = neighborsAverageOrBorder(target, i, j);
                }
            }

            return newMatrix;
        }

        public int getWidth() {
            return channels[0].length;
        }

        public int getHeight() {
            return channels[0][0].length;
        }

        @Override
        public String toString() {
            final var sb = new StringBuilder();
            // Do not print borders
            for (int i = 1; i < getWidth() - 1; ++i) {
                for (int j = 1; j < getHeight() - 1; ++j) {
                    sb.append("< %d, %d, %d > ".formatted(
                            channels[0][i][j],
                            channels[1][i][j],
                            channels[2][i][j]
                    ));
                }
                sb.append('\n');
            }
            return sb.toString();
        }
    }
}