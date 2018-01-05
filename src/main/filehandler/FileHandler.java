package filehandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class FileHandler {

    /*
     * Opens and reads lines from file, by file @filePath.
     */
    public List<String> getLines(String filePath) {
        List<String> lines = new ArrayList<>();
        try {
            lines = Files.readAllLines(Paths.get(filePath));

        } catch (IOException e) {
            System.out.println("Could not open " + filePath);
            e.printStackTrace();
            System.exit(1);
        }
        return lines;
    }

}
