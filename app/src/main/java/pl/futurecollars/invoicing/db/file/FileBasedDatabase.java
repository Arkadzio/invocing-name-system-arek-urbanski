package pl.futurecollars.invoicing.db.file;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import pl.futurecollars.invoicing.db.Database;
import pl.futurecollars.invoicing.model.Invoice;
import pl.futurecollars.invoicing.utils.FilesService;
import pl.futurecollars.invoicing.utils.JsonService;

public class FileBasedDatabase implements Database {

  private final FilesService filesService;
  private final JsonService jsonService;
  private final IdService idService;
  private final Path databasePath;

  public FileBasedDatabase(FilesService filesService, JsonService jsonService, IdService idService, Path databasePath) {
    this.filesService = filesService;
    this.jsonService = jsonService;
    this.idService = idService;
    this.databasePath = databasePath;
  }

  @Override
  public int save(Invoice invoice) {
    try {
      invoice.setId(idService.getNextIdAndIncrement());
      filesService.appendLineToFile(databasePath, jsonService.toJson(invoice));
      return invoice.getId();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Optional<Invoice> getById(int id) {
    try {
      return filesService.readAllLines(databasePath)
          .stream()
          .filter(line -> containsId(line, id))
          .map(line -> jsonService.toObject(line, Invoice.class))
          .findFirst();
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public List<Invoice> getAll() {
    try {
      return filesService.readAllLines(databasePath)
          .stream()
          .map(line -> jsonService.toObject(line, Invoice.class))
          .collect(Collectors.toList());
    } catch (IOException ex) {
      throw new RuntimeException("Failed to read invoices from file", ex);
    }
  }

  @Override
  public void update(int id, Invoice updatedInvoice) {
    try {
      List<String> allInvoices = filesService.readAllLines(databasePath);
      var listWithoutInvoiceWithGivenId = allInvoices
          .stream()
          .filter(line -> !containsId(line, id))
          .collect(Collectors.toList());

      if (allInvoices.size() == listWithoutInvoiceWithGivenId.size()) {
        throw new IllegalArgumentException("Id " + id + " does not exist");
      }

      updatedInvoice.setId(id);
      listWithoutInvoiceWithGivenId.add(jsonService.toJson(updatedInvoice));

      filesService.writeAllLinesToFile(databasePath, listWithoutInvoiceWithGivenId);

    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void delete(int id) {
    try {
      var updatedList = filesService.readAllLines(databasePath)
          .stream()
          .filter(line -> !containsId(line, id))
          .collect(Collectors.toList());

      filesService.writeAllLinesToFile(databasePath, updatedList);

    } catch (IOException ex) {
      throw new RuntimeException("Failed to delete invoice with id: " + id, ex);
    }
  }

  private boolean containsId(String line, int id) {
    return line.contains("\"id\":" + id + ",");
  }
}
