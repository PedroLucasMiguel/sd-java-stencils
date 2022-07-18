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

    public int getChannelFor(final int index) {
        return switch (index) {
            case 0 -> this.r();
            case 1 -> this.g();
            case 2 -> this.b();
            default -> -1;
        };
    }
}