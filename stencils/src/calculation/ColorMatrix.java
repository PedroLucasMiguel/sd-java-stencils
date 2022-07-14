package calculation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
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

        for (final var arr : matrix)
            Arrays.fill(arr, new Color());

        fillBorder();
        refreshFixedPoints();
    }

    private void fillBorder() {
        for (int i = 0, j = 0; j < this.outerSize; ++j) // TOP ROW
            this.matrix[i][j] = ColorMatrix.gray;
        for (int i = 0, j = 0; i < this.outerSize; ++i) // LEFT COL
            this.matrix[i][j] = ColorMatrix.gray;
        for (int i = this.outerSize - 1, j = 0; j < this.outerSize; ++j) // BOTTOM ROW
            this.matrix[i][j] = ColorMatrix.gray;
        for (int i = 0, j = this.outerSize - 1; i < this.outerSize; ++i) // RIGHT COL
            this.matrix[i][j] = ColorMatrix.gray;
    }

    private void refreshFixedPoints() {
        this.fixedPoints.parallelStream()
                .forEach(this::setFixedPoint);
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
                            .collect(Collectors.toList());

            return new ColorMatrix(matrixSize, fixedPoints);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.outerSize; ++i) {
            for (int j = 0; j < this.outerSize; ++j) {
                sb.append(this.matrix[i][j]).append(' ');
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}