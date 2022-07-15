package calculation;

import java.io.Serializable;

public record Color(int r, int g, int b) implements Serializable {
    public Color() {
        this(0, 0, 0);
    }

    public static Color averageOf(Color... colors) {
        // TODO: Maybe streams?

        int sumR = 0;
        int sumG = 0;
        int sumB = 0;

        for (var color : colors) {
            sumR += color.r;
            sumG += color.g;
            sumB += color.b;
        }

        sumR /= colors.length;
        sumG /= colors.length;
        sumB /= colors.length;

        return new Color(sumR, sumG, sumB);
    }

    @Override
    public String toString() {
        return "< %d, %d, %d >".formatted(this.r, this.g, this.b);
    }
}