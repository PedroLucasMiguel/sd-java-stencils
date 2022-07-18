package image;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.IntStream;

/*
* Essa classe é responsável por todo processo lógico realizado pelos clientes e pelo servidor.
* Ela fornece os métodos para a criação/manipulação de imagens.
* */

public class ImageDelegate {
    private static final int GRAY = 127; // Cor da borda
    private final int innerSize; // Tamanho da imagem desconsiderando a borda
    private final List<FixedPoint> fixedPoints; // Lista de pontos fixos
    private final Image image; // Matriz

    public ImageDelegate(final int imageSize, final List<FixedPoint> fixedPoints) {
        this.innerSize = imageSize;
        this.fixedPoints = fixedPoints;
        this.image = Image.grayBorderedImage(imageSize + 2);
        updateImageFixedPoints();
    }

    // Inicializa o a matriz a partir do "Scanner" fornecido pelo Java para leitura de arquivos
    public static ImageDelegate fromScanner(final Scanner s) {
        final var imageSize = s.nextInt();
        final var fixedPointCount = s.nextInt();
        final var fixedPoints = IntStream.range(0, fixedPointCount)
                .mapToObj(i -> FixedPoint.fromScanner(s))
                .toList();

        return new ImageDelegate(imageSize, fixedPoints);
    }

    // Realiza a inserção dos pontos fixos na imagem
    public void updateImageFixedPoints() {
        fixedPoints.parallelStream().forEach(fp -> {
            // TODO: Check if should have offset or not
            int i = fp.i();
            int j = fp.j();
            for (int ch = 0; ch < 3; ++ch) {
                image.channels[ch][i][j] = fp.getChannelFor(ch);
            }
        });
    }

    // Realiza a "divisão" da imagem em segmentos de mesmo tamanho, cada um contendo linhas extras
    // para o cálulo de estêncil.
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

    // Realiza a "junção" dos segmentos fornecidos após os cálculos dos clientes
    public void merge(final Image[] segments) {
        final int count = segments.length;
        final int offset = innerSize / count;

        IntStream.range(0, count)
                .parallel()
                .forEach(im -> updateImageWithSegment(segments[im], offset * im));
    }

    // Método auxiliar para merge()
    private void updateImageWithSegment(final Image segment, final int iOffset) {
        IntStream.range(0, 3)
                .parallel()
                .forEach(ch -> updateChannelWithSegment(ch, segment, iOffset));
    }

    // Método auxiliar para merge()
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

    // Classe que representa a imagem como uma matriz de inteiros
    public static final class Image implements Serializable {
        final int[][][] channels; // Canais R-G-B

        public Image(final int[][][] channels) {
            this.channels = channels;
        }

        public Image(final int[][] redChannel, final int[][] greenChannel, final int[][] blueChannel) {
            this(new int[][][]{redChannel, greenChannel, blueChannel});
        }

        // Realiza o cálculo da média de um ponto X
        private static int neighborsAverageOrBorder(final int[][] src, final int i, final int j) {
            if (coordinateIsBorder(src, i, j))
                return src[i][j];

            return (src[i][j]
                    + src[i - 1][j]
                    + src[i][j - 1]
                    + src[i + 1][j]
                    + src[i][j + 1]) / 5;
        }

        // Auxiliar para neighborsAverageOrBorder()
        private static boolean coordinateIsBorder(int[][] src, int i, int j) {
            return i == 0 || j == 0 || i == src.length - 1 || j == src[0].length - 1;
        }

        // Inicializa a borda da matriz
        private static int[][] grayBorderedChannel(final int size) {
            final var matrix = new int[size][size];
            Arrays.fill(matrix[0], GRAY);
            Arrays.fill(matrix[size - 1], GRAY);
            Arrays.stream(matrix).parallel()
                    .forEach(row -> row[0] = row[size - 1] = GRAY);
            return matrix;
        }

        // Cria uma "imagem" com borda cinza
        private static Image grayBorderedImage(final int imageSize) {
            return new Image(
                    grayBorderedChannel(imageSize),
                    grayBorderedChannel(imageSize),
                    grayBorderedChannel(imageSize)
            );
        }

        // Realiza uma iteração do cálculo de estêncil
        public void doStencilIteration() {
            for (int i = 0; i < 3; ++i) {
                this.channels[i] = doStencilIterationForChannel(this.channels[i]);
            }
        }

        // Auxiliar de doStencilIteration()
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