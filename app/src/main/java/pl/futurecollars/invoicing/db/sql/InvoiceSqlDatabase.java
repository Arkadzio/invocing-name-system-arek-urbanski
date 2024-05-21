package pl.futurecollars.invoicing.db.sql;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.transaction.annotation.Transactional;
import pl.futurecollars.invoicing.db.Database;
import pl.futurecollars.invoicing.model.Car;
import pl.futurecollars.invoicing.model.Company;
import pl.futurecollars.invoicing.model.Invoice;
import pl.futurecollars.invoicing.model.InvoiceEntry;
import pl.futurecollars.invoicing.model.Vat;

public class InvoiceSqlDatabase extends AbstractSqlDatabase implements Database<Invoice> {

  public static final String SELECT_QUERY = "SELECT i.id, i.date, i.number, "
      + "c1.id as buyer_id, c1.tax_identification_number as buyer_tax_identification_number, c1.address as buyer_address, c1.name as buyer_name, "
      + "c1.pension_insurance as buyer_pension_insurance, c1.health_insurance as buyer_health_insurance, "
      + "c2.id as seller_id, c2.tax_identification_number as seller_tax_identification_number, c2.address as seller_address, c2.name as seller_name, "
      + "c2.pension_insurance as seller_pension_insurance, c2.health_insurance as seller_health_insurance "
      + "FROM invoice i "
      + "inner join company c1 on i.buyer = c1.id "
      + "inner join company c2 on i.seller = c2.id";

  public InvoiceSqlDatabase(JdbcTemplate jdbcTemplate) {
    super(jdbcTemplate);
  }

  @Transactional
  @Override
  public long save(Invoice invoice) {

    int buyerId = insertCompany(invoice.getBuyer());
    int sellerId = insertCompany(invoice.getSeller());

    int invoiceId = insertInvoice(invoice, buyerId, sellerId);
    addEntriesRelatedToInvoice(invoiceId, invoice);

    return invoiceId;
  }

  private int insertInvoice(Invoice invoice, int buyerId, int sellerId) {
    GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(connection -> {
      PreparedStatement preparedStatement =
          connection.prepareStatement("INSERT INTO invoice (date, number, buyer, seller) values (?, ?, ?, ?);", new String[] {"id"});
      preparedStatement.setDate(1, Date.valueOf(invoice.getDate()));
      preparedStatement.setString(2, invoice.getNumber());
      preparedStatement.setLong(3, buyerId);
      preparedStatement.setLong(4, sellerId);
      return preparedStatement;
    }, keyHolder);

    int invoiceId = keyHolder.getKey().intValue();
    return invoiceId;
  }

  private Integer insertCarAndGetItId(Car car) {
    if (car == null) {
      return null;
    }

    GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(connection -> {
      PreparedStatement preparedStatement = connection
          .prepareStatement(
              "insert into car (registration_number, personal_use) values (?, ?);",
              new String[] {"id"});
      preparedStatement.setString(1, car.getRegistrationNumber());
      preparedStatement.setBoolean(2, car.isPersonalUse());
      return preparedStatement;
    }, keyHolder);

    return keyHolder.getKey().intValue();
  }

  @Override
  public List<Invoice> getAll() {
    return jdbcTemplate.query(SELECT_QUERY, invoiceRowMapper());
  }

  @Override
  public Optional<Invoice> getById(long id) {
    List<Invoice> invoices = jdbcTemplate.query(SELECT_QUERY + " WHERE i.id = " + id,
        invoiceRowMapper());
    return invoices.isEmpty() ? Optional.empty() : Optional.of(invoices.get(0));
  }

  private RowMapper<Invoice> invoiceRowMapper() {
    return (resultSet, rowNumber) -> {
      long invoiceId = resultSet.getLong("id");

      List<InvoiceEntry> invoiceEntries = jdbcTemplate.query(
          "select * from invoice_invoice_entry iie "
              + "inner join invoice_entry e on iie.invoice_entry_id = e.id "
              + "left outer join car c on e.expense_related_to_car = c.id "
              + "where invoice_id = " + invoiceId,
          (response, ignored) -> InvoiceEntry.builder()
              .description(response.getString("description"))
              .quantity(response.getBigDecimal("quantity"))
              .netPrice(response.getBigDecimal("net_price"))
              .vatValue(response.getBigDecimal("vat_value"))
              .vatRate(Vat.valueOf(response.getString("vat_rate")))
              .expenseRelatedToCar(response.getObject("registration_number") != null
                  ? Car.builder()
                  .registrationNumber(response.getString("registration_number"))
                  .personalUse(response.getBoolean("personal_use"))
                  .build()
                  : null)
              .build());

      return Invoice.builder()
          .id(resultSet.getLong("id"))
          .date(resultSet.getDate("date").toLocalDate())
          .number(resultSet.getString("number"))
          .buyer(Company.builder()
              .id(resultSet.getLong("buyer_id"))
              .taxIdentificationNumber(resultSet.getString("buyer_tax_identification_number"))
              .name(resultSet.getString("buyer_name"))
              .address(resultSet.getString("buyer_address"))
              .pensionInsurance(resultSet.getBigDecimal("buyer_pension_insurance"))
              .healthInsurance(resultSet.getBigDecimal("buyer_health_insurance"))
              .build()
          )
          .seller(Company.builder()
              .id(resultSet.getLong("seller_id"))
              .taxIdentificationNumber(resultSet.getString("seller_tax_identification_number"))
              .name(resultSet.getString("seller_name"))
              .address(resultSet.getString("seller_address"))
              .pensionInsurance(resultSet.getBigDecimal("seller_pension_insurance"))
              .healthInsurance(resultSet.getBigDecimal("seller_health_insurance"))
              .build()
          )
          .entries(invoiceEntries)
          .build();
    };

  }

  @Override
  @Transactional
  public Optional<Invoice> update(long id, Invoice updatedInvoice) {
    Optional<Invoice> originalInvoice = getById(id);

    if (originalInvoice.isEmpty()) {
      return originalInvoice;
    }

    updatedInvoice.getBuyer().setId(originalInvoice.get().getBuyer().getId());
    updateCompany(updatedInvoice.getBuyer());

    updatedInvoice.getSeller().setId(originalInvoice.get().getSeller().getId());
    updateCompany(updatedInvoice.getSeller());

    jdbcTemplate.update(connection -> {
      PreparedStatement preparedStatement =
          connection.prepareStatement(
              "update invoice "
                  + "set date=?, "
                  + "number=? "
                  + "where id=?"
          );
      preparedStatement.setDate(1, Date.valueOf(updatedInvoice.getDate()));
      preparedStatement.setString(2, updatedInvoice.getNumber());
      preparedStatement.setLong(3, id);
      return preparedStatement;
    });

    deleteEntriesAndCarsRelatedToInvoice(id);
    addEntriesRelatedToInvoice(id, updatedInvoice);

    return originalInvoice;
  }

  private void addEntriesRelatedToInvoice(long invoiceId, Invoice invoice) {
    GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
    invoice.getEntries().forEach(entry -> {
      jdbcTemplate.update(connection -> {
        PreparedStatement preparedStatement = connection
            .prepareStatement(
                "insert into invoice_entry (description, quantity, net_price, vat_value, vat_rate, expense_related_to_car) "
                    + "values (?, ?, ?, ?, ?, ?);",
                new String[] {"id"});
        preparedStatement.setString(1, entry.getDescription());
        preparedStatement.setBigDecimal(2, entry.getQuantity());
        preparedStatement.setBigDecimal(3, entry.getNetPrice());
        preparedStatement.setBigDecimal(4, entry.getVatValue());
        preparedStatement.setString(5, entry.getVatRate().name());
        preparedStatement.setObject(6, insertCarAndGetItId(entry.getExpenseRelatedToCar()));
        return preparedStatement;
      }, keyHolder);

      int invoiceEntryId = keyHolder.getKey().intValue();

      jdbcTemplate.update(connection -> {
        PreparedStatement preparedStatement = connection.prepareStatement(
            "insert into invoice_invoice_entry (invoice_id, invoice_entry_id) values (?, ?);");
        preparedStatement.setLong(1, invoiceId);
        preparedStatement.setLong(2, invoiceEntryId);
        return preparedStatement;
      });
    });
  }

  @Override
  @Transactional
  public Optional<Invoice> delete(long id) {
    Optional<Invoice> invoiceOptional = getById(id);
    if (invoiceOptional.isEmpty()) {
      return invoiceOptional;
    }

    Invoice invoice = invoiceOptional.get();

    deleteEntriesAndCarsRelatedToInvoice(id);

    jdbcTemplate.update(connection -> {
      PreparedStatement preparedStatement = connection.prepareStatement(
          "delete from invoice where id = ?;");
      preparedStatement.setLong(1, id);
      return preparedStatement;
    });

    jdbcTemplate.update(connection -> {
      PreparedStatement preparedStatement = connection.prepareStatement(
          "delete from company where id in (?, ?);");
      preparedStatement.setLong(1, invoice.getBuyer().getId());
      preparedStatement.setLong(2, invoice.getSeller().getId());
      return preparedStatement;
    });

    return invoiceOptional;
  }

  private void deleteEntriesAndCarsRelatedToInvoice(long id) {
    jdbcTemplate.update(connection -> {
      PreparedStatement preparedStatement = connection.prepareStatement("delete from car where id in ("
          + "select expense_related_to_car from invoice_entry where id in ("
          + "select invoice_entry_id from invoice_invoice_entry where invoice_id=?));");
      preparedStatement.setLong(1, id);
      return preparedStatement;
    });

    jdbcTemplate.update(connection -> {
      PreparedStatement ps = connection.prepareStatement(
          "delete from invoice_entry where id in (select invoice_entry_id from invoice_invoice_entry where invoice_id=?);");
      ps.setLong(1, id);
      return ps;
    });
  }
}
