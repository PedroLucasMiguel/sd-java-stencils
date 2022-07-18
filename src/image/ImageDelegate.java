package image;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.IntStream;

public class ImageDelegate {
    private static final int GRAY = 127;
    private final int innerSize;
    private final List<FixedPoint> fixedPoints;
    private final Image image;

    public ImageDelegate(final int imageSize, final List<FixedPoint> fixedPoints) {
        this.innerSize = imageSize;
        this.fixedPoints = fixedPoints;
        this.image = Image.grayBorderedImage(imageSize + 2);
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

    public void updateImageFixedPoints() {
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

        return IntStream.range(0, n).parallel()
                .mapToObj(i -> {
                    final int startIndex = i * offset;
                    final int endIndex = startIndex + offset + 2;
                    return new Image(
                            Arrays.copyOfRange(image.channels[0], startIndex, endIndex),
                            Arrays.copyOfRange(image.channels[1], startIndex, endIndex),
                            Arrays.copyOfRange(image.channels[2], startIndex, endIndex)
                    );
                }).toArray(Image[]::new);
    }

    public void merge(final Image[] segments) {
        final int count = segments.length;
        final int offset = innerSize / count;

        IntStream.range(0, count)
                .parallel()
                .forEach(im -> updateImageWithSegment(segments[im], offset * im));
    }

    private void updateImageWithSegment(final Image segment, final int iOffset) {
        IntStream.range(0, 3)
                .parallel()
                .forEach(ch -> updateChannelWithSegment(ch, segment, iOffset));
    }

    private void updateChannelWithSegment(final int ch, final Image segment, final int iOffset) {
        System.arraycopy(
                segment.channels[ch], 1, // Source, offset
                this.image.channels[ch], iOffset + 1, // Destination, offset
                segment.getWidth() - 2 // How many arrays to copy (leave borders)
        );
    }

    public Image getImage() {
        return image;
    }

    public static final class Image implements Serializable {
        final int[][][] channels;

        public Image(final int[][][] channels) {
            this.channels = channels;
        }

        public Image(final int[][] redChannel, final int[][] greenChannel, final int[][] blueChannel) {
            this(new int[][][]{redChannel, greenChannel, blueChannel});
        }

        private static int neighborsAverageOrBorder(final int[][] src, final int i, final int j) {
            if (coordinateIsBorder(src, i, j))
                return src[i][j];

            return (src[i][j]
                    + src[i - 1][j]
                    + src[i][j - 1]
                    + src[i + 1][j]
                    + src[i][j + 1]) / 5;
        }

        private static boolean coordinateIsBorder(int[][] src, int i, int j) {
            return i == 0 || j == 0 || i == src.length - 1 || j == src[0].length - 1;
        }

        private static int[][] grayBorderedChannel(final int size) {
            final var matrix = new int[size][size];
            Arrays.fill(matrix[0], GRAY);
            Arrays.fill(matrix[size - 1], GRAY);
            Arrays.stream(matrix).parallel()
                    .forEach(row -> row[0] = row[size - 1] = GRAY);
            return matrix;
        }

        private static Image grayBorderedImage(final int imageSize) {
            return new Image(
                    grayBorderedChannel(imageSize),
                    grayBorderedChannel(imageSize),
                    grayBorderedChannel(imageSize)
            );
        }

        public void doStencilIteration() {
            for (int i = 0; i < 3; ++i) {
                this.channels[i] = doStencilIterationForChannel(this.channels[i]);
            }
        }

        private int[][] doStencilIterationForChannel(int[][] channel) {
            final var width = getWidth();
            final var height = getHeight();

            final var newMatrix = new int[width][height];

            for (int i = 0; i < width; ++i) {
                for (int j = 0; j < height; ++j) {
                    newMatrix[i][j] = neighborsAverageOrBorder(channel, i, j);
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