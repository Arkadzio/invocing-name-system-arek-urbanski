package pl.futurecollars.invoicing.db.sql;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;
import lombok.AllArgsConstructor;
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

@AllArgsConstructor
public class SqlDatabase implements Database {

  public static final String SELECT_QUERY = "SELECT i.id, i.date, i.number, "
      +
      "c1.id as buyer_id, c1.tax_identification_number as buyer_tax_identification_number, c1.address as buyer_address, c1.name as buyer_name, c1.pension_insurance as buyer_pension_insurance, c1.health_insurance as buyer_health_insurance, "
      +
      "c2.id as seller_id, c2.tax_identification_number as seller_tax_identification_number, c2.address as seller_address, c2.name as seller_name, c2.pension_insurance as seller_pension_insurance, c2.health_insurance as seller_health_insurance "
      + "FROM invoices i "
      + "inner join company c1 on i.buyer = c1.id "
      + "inner join company c2 on i.seller = c2.id";

  private final JdbcTemplate jdbcTemplate;

  private final Map<Vat, Integer> vatToId = new HashMap<>();
  private final Map<Integer, Vat> idToVat = new HashMap<>();

  @PostConstruct
  void initVatRatesMap() {
    jdbcTemplate.query("SELECT * FROM vat",
        resultSet -> {
          Vat vat = Vat.valueOf("VAT_" + resultSet.getString("name"));
          int id = resultSet.getInt("id");
          vatToId.put(vat, id);
          idToVat.put(id, vat);
        });
  }

  @Transactional
  @Override
  public int save(Invoice invoice) {

    int buyerId = saveCompany(invoice.getBuyer());
    int sellerId = saveCompany(invoice.getSeller());

    int invoiceId = insertInvoice(invoice, buyerId, sellerId);
    addEntriesRelatedToInvoice(invoiceId, invoice);

    return invoiceId;
  }

  private int insertInvoice(Invoice invoice, int buyerId, int sellerId) {
    GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(connection -> {
      PreparedStatement preparedStatement =
          connection.prepareStatement("INSERT INTO invoices (date, number, buyer, seller) values (?, ?, ?, ?);", new String[] {"id"});
      preparedStatement.setDate(1, Date.valueOf(invoice.getDate()));
      preparedStatement.setString(2, invoice.getNumber());
      preparedStatement.setLong(3, buyerId);
      preparedStatement.setLong(4, sellerId);
      return preparedStatement;
    }, keyHolder);

    int invoiceId = keyHolder.getKey().intValue();
    return invoiceId;
  }

  private int saveCompany(Company company) {
    GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

    jdbcTemplate.update(connection -> {
      PreparedStatement preparedStatement =
          connection.prepareStatement(
              "INSERT INTO company (name, address, tax_identification_number, pension_insurance, health_insurance) values (?, ?, ?, ?, ?);",
              new String[] {"id"});
      preparedStatement.setString(1, company.getName());
      preparedStatement.setString(2, company.getAddress());
      preparedStatement.setString(3, company.getTaxIdentificationNumber());
      preparedStatement.setBigDecimal(4, company.getPensionInsurance());
      preparedStatement.setBigDecimal(5, company.getHealthInsurance());
      return preparedStatement;
    }, keyHolder);

    return keyHolder.getKey().intValue();
  }

  private Integer insertCarAndGetItId(Car car) {
    if (car == null) {
      return null;
    }

    GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(connection -> {
      PreparedStatement preparedStatement = connection
          .prepareStatement(
              "insert into car (registration_number, personal_user) values (?, ?);",
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
  public Optional<Invoice> getById(int id) {
    List<Invoice> invoices = jdbcTemplate.query(SELECT_QUERY + " WHERE i.id = " + id,
        invoiceRowMapper());
    return invoices.isEmpty() ? Optional.empty() : Optional.of(invoices.get(0));
  }

  private RowMapper<Invoice> invoiceRowMapper() {
    return (resultSet, rowNumber) -> {
      int invoiceId = resultSet.getInt("id");

      List<InvoiceEntry> invoiceEntries = jdbcTemplate.query(
          "select * from invoice_invoice_entry iie "
              + "inner join invoice_entry ie on iie.invoice_entry_id = ie.id "
              + "left outer join car c on ie.expense_related_to_car = c.id "
              + "where invoice_id = " + invoiceId,
          (response, ignored) -> InvoiceEntry.builder()
              .description(response.getString("description"))
              .quantity(response.getInt("quantity"))
              .netPrice(response.getBigDecimal("net_price"))
              .vatValue(response.getBigDecimal("vat_value"))
              .vatRate(idToVat.get(response.getInt("vat_rate")))
              .expenseRelatedToCar(response.getObject("registration_number") != null
                  ? Car.builder()
                  .registrationNumber(response.getString("registration_number"))
                  .personalUse(response.getBoolean("personal_user"))
                  .build()
                  : null)
              .build());

      return Invoice.builder()
          .id(resultSet.getInt("id"))
          .date(resultSet.getDate("date").toLocalDate())
          .number(resultSet.getString("number"))
          .buyer(Company.builder()
              .id(resultSet.getInt("buyer_id"))
              .taxIdentificationNumber(resultSet.getString("buyer_tax_identification_number"))
              .name(resultSet.getString("buyer_name"))
              .address(resultSet.getString("buyer_address"))
              .pensionInsurance(resultSet.getBigDecimal("buyer_pension_insurance"))
              .healthInsurance(resultSet.getBigDecimal("buyer_health_insurance"))
              .build()
          )
          .seller(Company.builder()
              .id(resultSet.getInt("seller_id"))
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
  public Optional<Invoice> update(int id, Invoice updatedInvoice) {
    Optional<Invoice> originalInvoice = getById(id);

    if (originalInvoice.isEmpty()) {
      return originalInvoice;
    }

    updateCompany(updatedInvoice.getBuyer(), originalInvoice.get().getBuyer());
    updateCompany(updatedInvoice.getSeller(), originalInvoice.get().getSeller());

    jdbcTemplate.update(connection -> {
      PreparedStatement preparedStatement =
          connection.prepareStatement(
              "update invoices "
                  + "set date=?, "
                  + "number=? "
                  + "where id=?"
          );
      preparedStatement.setDate(1, Date.valueOf(updatedInvoice.getDate()));
      preparedStatement.setString(2, updatedInvoice.getNumber());
      preparedStatement.setInt(3, id);
      return preparedStatement;
    });

    deleteEntriesAndCarsRelatedToInvoice(id);
    addEntriesRelatedToInvoice(id, updatedInvoice);

    return originalInvoice;
  }

  private void updateCompany(Company buyer, Company buyer2) {
    jdbcTemplate.update(connection -> {
      PreparedStatement preparedStatement = connection.prepareStatement(
          "update company "
              + "set name=?, "
              + "address=?, "
              + "tax_identification_number=?, "
              + "health_insurance=?, "
              + "pension_insurance=? "
              + "where id=?"
      );
      preparedStatement.setString(1, buyer.getName());
      preparedStatement.setString(2, buyer.getAddress());
      preparedStatement.setString(3, buyer.getTaxIdentificationNumber());
      preparedStatement.setBigDecimal(4, buyer.getHealthInsurance());
      preparedStatement.setBigDecimal(5, buyer.getPensionInsurance());
      preparedStatement.setInt(6, buyer2.getId());
      return preparedStatement;
    });
  }

  private void addEntriesRelatedToInvoice(int invoiceId, Invoice invoice) {
    GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
    invoice.getEntries().forEach(entry -> {
      jdbcTemplate.update(connection -> {
        PreparedStatement preparedStatement = connection
            .prepareStatement(
                "insert into invoice_entry (description, quantity, net_price, vat_value, vat_rate, expense_related_to_car) "
                    + "values (?, ?, ?, ?, ?, ?);",
                new String[] {"id"});
        preparedStatement.setString(1, entry.getDescription());
        preparedStatement.setInt(2, entry.getQuantity());
        preparedStatement.setBigDecimal(3, entry.getNetPrice());
        preparedStatement.setBigDecimal(4, entry.getVatValue());
        preparedStatement.setInt(5, vatToId.get(entry.getVatRate()));
        preparedStatement.setObject(6, insertCarAndGetItId(entry.getExpenseRelatedToCar()));
        return preparedStatement;
      }, keyHolder);

      int invoiceEntryId = keyHolder.getKey().intValue();

      jdbcTemplate.update(connection -> {
        PreparedStatement preparedStatement = connection.prepareStatement(
            "insert into invoice_invoice_entry (invoice_id, invoice_entry_id) values (?, ?);");
        preparedStatement.setInt(1, invoiceId);
        preparedStatement.setInt(2, invoiceEntryId);
        return preparedStatement;
      });
    });
  }

  @Override
  @Transactional
  public Optional<Invoice> delete(int id) {
    Optional<Invoice> invoiceOptional = getById(id);
    if (invoiceOptional.isEmpty()) {
      return invoiceOptional;
    }

    Invoice invoice = invoiceOptional.get();

    deleteEntriesAndCarsRelatedToInvoice(id);

    jdbcTemplate.update(connection -> {
      PreparedStatement preparedStatement = connection.prepareStatement(
          "delete from invoices where id = ?;");
      preparedStatement.setInt(1, id);
      return preparedStatement;
    });

    jdbcTemplate.update(connection -> {
      PreparedStatement preparedStatement = connection.prepareStatement(
          "delete from company where id in (?, ?);");
      preparedStatement.setInt(1, invoice.getBuyer().getId());
      preparedStatement.setInt(2, invoice.getSeller().getId());
      return preparedStatement;
    });

    return invoiceOptional;
  }

  private void deleteEntriesAndCarsRelatedToInvoice(int id) {
    jdbcTemplate.update(connection -> {
      PreparedStatement preparedStatement = connection.prepareStatement("delete from car where id in ("
          + "select expense_related_to_car from invoice_entry where id in ("
          + "select invoice_entry_id from invoice_invoice_entry where invoice_id=?));");
      preparedStatement.setInt(1, id);
      return preparedStatement;
    });

    jdbcTemplate.update(connection -> {
      PreparedStatement ps = connection.prepareStatement(
          "delete from invoice_entry where id in (select invoice_entry_id from invoice_invoice_entry where invoice_id=?);");
      ps.setInt(1, id);
      return ps;
    });
  }
}
