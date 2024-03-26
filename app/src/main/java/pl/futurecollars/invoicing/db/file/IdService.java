package pl.futurecollars.invoicing.db.file;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import pl.futurecollars.invoicing.utils.FilesService;

public class IdService {

  private final FilesService filesService;
  private final Path filePath;
  private int nextId = 1;

  public IdService(FilesService filesService, Path filePath) {
    this.filesService = filesService;
    this.filePath = filePath;

    try {
      List<String> lines = filesService.readAllLines(filePath);
      if (lines.isEmpty()) {
        filesService.writeSingleLineToFile(filePath, "1");
      } else {
        nextId = Integer.parseInt(lines.get(0));
      }
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  public int getNextIdAndIncrement() {
    try {
      filesService.writeSingleLineToFile(filePath, String.valueOf(nextId + 1));
      return nextId++;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
