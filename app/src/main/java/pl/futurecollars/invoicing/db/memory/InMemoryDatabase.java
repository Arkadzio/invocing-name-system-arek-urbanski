package pl.futurecollars.invoicing.db.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import pl.futurecollars.invoicing.db.Database;
import pl.futurecollars.invoicing.model.Invoice;

public class InMemoryDatabase implements Database {

  private final Map<Long, Invoice> invoices = new HashMap<>();
  private long nextID = 1;

  @Override
  public long save(Invoice invoice) {
    invoice.setId(nextID);
    invoices.put(nextID, invoice);
    return nextID++;
  }

  @Override
  public Optional<Invoice> getById(long id) {
    return Optional.ofNullable(invoices.get(id));
  }

  @Override
  public List<Invoice> getAll() {
    return new ArrayList<>(invoices.values());
  }

  @Override
  public Optional<Invoice> update(long id, Invoice updatedInvoice) {
    updatedInvoice.setId(id);

    return Optional.ofNullable(invoices.put(id, updatedInvoice));
  }

  @Override
  public Optional<Invoice> delete(long id) {

    return Optional.ofNullable(invoices.remove(id));
  }
}
