package pl.futurecollars.invoicing.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class FilesService {

  public void appendLineToFile(Path path, String line) throws IOException {
    Files.write(path, (line + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
  }

  public void writeSingleLineToFile(Path path, String line) throws IOException {
    Files.write(path, line.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
  }

  public void writeAllLinesToFile(Path path, List<String> lines) throws IOException {
    Files.write(path, lines, StandardOpenOption.TRUNCATE_EXISTING);
  }

  public List<String> readAllLines(Path path) throws IOException {
    return Files.readAllLines(path, StandardCharsets.ISO_8859_1);
  }
}
