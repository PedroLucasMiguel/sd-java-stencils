package calculation;

import java.util.Scanner;

public record FixedPoint(Color color, int i, int j) {
    static FixedPoint scanFixedPoint(final Scanner s) {
        final int xPos = s.nextInt();
        final int yPos = s.nextInt();
        final int r = s.nextInt();
        final int g = s.nextInt();
        final int b = s.nextInt();
        return new FixedPoint(new Color(r, g, b), xPos, yPos);
    }
}
