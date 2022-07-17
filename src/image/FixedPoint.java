package image;

import java.util.Scanner;

public record FixedPoint(int i, int j, int r, int g, int b) {
    static FixedPoint fromScanner(final Scanner s) {
        return new FixedPoint(
                s.nextInt(),
                s.nextInt(),
                s.nextInt(),
                s.nextInt(),
                s.nextInt()
        );
    }
}