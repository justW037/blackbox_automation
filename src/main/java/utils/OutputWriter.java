package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class OutputWriter {
    public void writeUpdatedLinesToFile(String csvFilePath, List<String> lines) {
        File file = new File(getClass().getClassLoader().getResource(csvFilePath).getFile());
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(file.toURI()), StandardCharsets.UTF_8)) {
            for (String updatedLine : lines) {
                bw.write(updatedLine);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
