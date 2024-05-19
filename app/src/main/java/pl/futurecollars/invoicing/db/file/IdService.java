package pl.futurecollars.invoicing.db.file;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import pl.futurecollars.invoicing.utils.FilesService;

public class IdService {

  private final FilesService filesService;
  private final Path idFilePath;
  private long nextId = 1;

  public IdService(Path idFilePath, FilesService filesService) {
    this.idFilePath = idFilePath;
    this.filesService = filesService;

    try {
      List<String> lines = filesService.readAllLines(idFilePath);
      if (lines.isEmpty()) {
        filesService.writeSingleLineToFile(idFilePath, "1");
      } else {
        nextId = Integer.parseInt(lines.get(0));
      }
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  public long getNextIdAndIncrement() {
    try {
      filesService.writeSingleLineToFile(idFilePath, String.valueOf(nextId + 1));
      return nextId++;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
