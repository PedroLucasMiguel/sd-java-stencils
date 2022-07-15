package calculation;

public class Calculator {
    public static Color[][] innerCellStencilAverage(final Color[][] matrix) {

        final int newX = matrix.length - 2;
        final int newY = matrix[0].length - 2;

        final var result = new Color[newX][newY];

        for (int i = 0; i < newX; ++i) {
            for (int j = 0; j < newY; ++j) {
                result[i][j] = Color.averageOf(
                        neighborsOf(i + 1, j + 1, matrix)
                );
            }
        }

        return result;
    }

    private static Color[] neighborsOf(int i, int j, final Color[][] matrix) {
        return new Color[]
                {
                        matrix[i][j],
                        matrix[i - 1][j],
                        matrix[i][j - 1],
                        matrix[i + 1][j],
                        matrix[i][j + 1]
                };
    }
}