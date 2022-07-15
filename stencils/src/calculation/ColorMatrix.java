package calculation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.IntStream;

public class ColorMatrix {
    private final static Color gray = new Color(127, 127, 127);
    private final List<FixedPoint> fixedPoints;
    private final int outerSize;
    private final Color[][] matrix;

    public ColorMatrix(final int innerSize, final List<FixedPoint> fixedPoints) {
        this.fixedPoints = fixedPoints.parallelStream()
                .map(fp -> new FixedPoint(fp.color(), fp.i() + 1, fp.j() + 1))
                .toList();
        this.outerSize = innerSize + 2;
        this.matrix = new Color[this.outerSize][this.outerSize];

        this.fillMatrix();
        this.setFixedPoints();
    }

    private void fillMatrix() {
        for (final var arr : matrix)
            Arrays.fill(arr, new Color());

        this.fillBorders();
    }

    private void setFixedPoints() {
        this.fixedPoints.parallelStream()
                .forEach(this::setFixedPoint);
    }

    private void fillBorders() {
        // Fills top and bottom rows
        Arrays.fill(this.matrix[0], ColorMatrix.gray);
        Arrays.fill(this.matrix[this.outerSize - 1], ColorMatrix.gray);

        // Fills first and last element of each row (i.e., fills the columns)
        Arrays.stream(this.matrix)
                .forEach(row -> row[0] = row[this.outerSize - 1] = ColorMatrix.gray);
    }

    private void setFixedPoint(final FixedPoint fp) {
        this.matrix[fp.i()][fp.j()] = fp.color();
    }

    public static ColorMatrix fromFile(final File file) throws FileNotFoundException {
        try (final var s = new Scanner(file)) {
            final int matrixSize = s.nextInt();
            final int fixedPointCount = s.nextInt();

            final var fixedPoints =
                    IntStream.range(0, fixedPointCount)
                            .mapToObj(i -> FixedPoint.scanFixedPoint(s))
                            .toList();

            return new ColorMatrix(matrixSize, fixedPoints);
        }
    }

    public Color[][] splice(final int startInclusive, final int endExclusive) {
        final int len = endExclusive - startInclusive;
        final var result = new Color[len][this.outerSize];

        for (int i = 0; i < len; ++i) {
            result[i] = Arrays.copyOf(this.matrix[startInclusive + i], this.outerSize);
        }

        return result;
    }

    public void updateLines(final Color[][] matrix, final int startInclusive, final int lineCount) {
        for (int i = 0; i < lineCount; ++i) {
            System.arraycopy(
                    matrix[i], 0,
                    this.matrix[i + startInclusive], 1,
                    this.outerSize - 2
            );
        }
    }

    public int getOuterSize() {
        return this.outerSize;
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder();
        for (int i = 0; i < this.outerSize; ++i) {
            for (int j = 0; j < this.outerSize; ++j) {
                sb.append(this.matrix[i][j]).append(' ');
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}