package utils;

import calculation.ColorMatrix;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileManager {

    public static void toFile(ColorMatrix matrix, String path) {
        try (FileWriter f = new FileWriter(path)) {
            f.write(matrix.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
