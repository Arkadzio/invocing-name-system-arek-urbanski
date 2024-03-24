package pl.futurecollars.invoicing.db.file;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import pl.futurecollars.invoicing.db.Database;
import pl.futurecollars.invoicing.model.Invoice;
import pl.futurecollars.invoicing.utils.FilesService;
import pl.futurecollars.invoicing.utils.JsonService;

public class FileBasedDatabase implements Database {

  private final FilesService filesService;

  private final JsonService jsonService;

  public FileBasedDatabase(FilesService filesService, JsonService jsonService) {
    this.filesService = filesService;
    this.jsonService = jsonService;
  }

  @Override
  public int save(Invoice invoice) {
    try {
      filesService.appendLineToFile(Path.of("databasePath"), Collections.singletonList(jsonService.toJason(invoice)));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return 0;
  }

  @Override
  public Optional<Invoice> getById(int id) {
    return Optional.empty();
  }

  @Override
  public List<Invoice> getAll() {
    return null;
  }

  @Override
  public void update(int id, Invoice updatedInvoice) {
  }

  @Override
  public void delete(int id) {
  }
}
