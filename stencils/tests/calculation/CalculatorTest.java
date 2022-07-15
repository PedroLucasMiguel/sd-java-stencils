package calculation;

import java.util.List;

class CalculatorTest {
    public static void main(String[] args) {
        testCalculation();
    }

    private static void testCalculation() {
        final var colorMatrix = new ColorMatrix(5, List.of(
                new FixedPoint(new Color(255, 0, 0), 2, 2)
        ));

        final var splicedMatrix = colorMatrix.splice(0, 7);
        printMatrix(splicedMatrix, 7, 7);

        final var averagedMatrix = Calculator.innerCellStencilAverage(splicedMatrix, 7, 7);
        printMatrix(averagedMatrix, 5, 5);

        colorMatrix.updateLines(averagedMatrix, 1);
        System.out.println(colorMatrix);
    }

    private static void printMatrix(final Color[][] matrix, final int x, final int y) {
        for (int i = 0; i < x; ++i) {
            for (int j = 0; j < y; ++j) {
                System.out.print(matrix[i][j]);
                System.out.print(' ');
            }
            System.out.println();
        }
        System.out.println();
    }
}