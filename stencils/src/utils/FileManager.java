package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FileManager {

    public static String parse(String path) {

        StringBuilder content = new StringBuilder();
        String aux = "";

        try (FileReader f = new FileReader(path);
             BufferedReader reader = new BufferedReader(f)) {

            while ((aux = reader.readLine()) != null)
                content.append(aux).append('\n');

            // Removendo o último '\n' por "consistência"
            content.deleteCharAt(content.length()-1);

            return content.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return content.toString();
    }

    public static void write() {

    }

    public static void main(String[] args) {
        System.out.println(parse("C:\\git\\sd-java-stencils\\stencils\\src\\utils\\img01.dat"));
    }

}
