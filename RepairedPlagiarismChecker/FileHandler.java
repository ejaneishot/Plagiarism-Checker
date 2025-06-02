import java.io.*;
import java.nio.file.*;

public class FileHandler {
    public static String readFile(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            System.out.println("Failed to read file: " + path);
            return "";
        }
    }
}