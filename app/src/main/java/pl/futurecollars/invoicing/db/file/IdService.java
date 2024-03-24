package pl.futurecollars.invoicing.db.file;

import java.io.IOException;
import java.nio.file.Path;
import pl.futurecollars.invoicing.utils.FilesService;

public class IdService {

  private final FilesService filesService;
  private final Path filePath;

  public IdService(FilesService filesService, Path filePath) {
    this.filesService = filesService;
    this.filePath = filePath;
  }

  private int nextId = 1;


  public int getNextIdAndIncrement() {
    try {
      filesService.writeSingleLineToFile(filePath, String.valueOf(nextId + 1));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return nextId++;
  }
}
