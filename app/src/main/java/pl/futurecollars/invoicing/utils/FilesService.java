package pl.futurecollars.invoicing.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class FilesService {

  public void appendLineToFile(Path path, List<String> lines) throws IOException {
    Files.write(path, lines, StandardOpenOption.APPEND);
  }

  public void writeSingleLineToFile(Path path, String line) throws IOException {
    Files.write(path, (line + System.lineSeparator()).getBytes());
  }

  public void writeAllLinesToFile(Path path, List<String> lines) throws IOException {
    Files.write(path, lines, StandardOpenOption.TRUNCATE_EXISTING);
  }

  public List<String> readAllLines(Path path) throws IOException {
    return Files.readAllLines(path);
  }
}
