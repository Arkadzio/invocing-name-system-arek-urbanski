package pl.futurecollars.invoicing.service

import java.time.LocalDate;
import pl.futurecollars.invoicing.db.Database;
import pl.futurecollars.invoicing.db.memory.InMemoryDatabase;
import pl.futurecollars.invoicing.model.Invoice;
import pl.futurecollars.invoicing.model.Company;
import spock.lang.Specification;

class InvoiceServiceIntegrationTest extends Specification {

    def "should save and retrieve invoice from database"()
    {
        given:
        Database database = new InMemoryDatabase()
        InvoiceService invoiceService = new InvoiceService(database)
        Invoice invoice = new Invoice(date: LocalDate.now(), buyer: new Company(), seller: new Company(), entries: [])

        when:
        int savedId = invoiceService.save(invoice)
        Optional<Invoice> retrievedInvoice = invoiceService.getById(savedId)

        then:
        savedId != null
        retrievedInvoice.isPresent()
        retrievedInvoice.get().id == savedId
    }

    def "should update invoice in database"()
    {
        given:
        Database database = new InMemoryDatabase()
        InvoiceService invoiceService = new InvoiceService(database)
        Invoice invoice = new Invoice(date: LocalDate.now(), buyer: new Company(), seller: new Company(), entries: [])
        int savedId = invoiceService.save(invoice)
        Invoice updatedInvoice = new Invoice(date: LocalDate.now(), buyer: new Company(), seller: new Company(), entries: [])

        when:
        invoiceService.update(savedId, updatedInvoice)
        Optional<Invoice> retrievedInvoice = invoiceService.getById(savedId)

        then:
        retrievedInvoice.isPresent()
        retrievedInvoice.get().id == savedId
        retrievedInvoice.get().date == updatedInvoice.date
    }

    def "should delete invoice from database"()
    {
        given:
        Database database = new InMemoryDatabase()
        InvoiceService invoiceService = new InvoiceService(database)
        Invoice invoice = new Invoice(date: LocalDate.now(), buyer: new Company(), seller: new Company(), entries: [])
        int savedId = invoiceService.save(invoice)

        when:
        invoiceService.delete(savedId)
        Optional<Invoice> retrievedInvoice = invoiceService.getById(savedId)

        then:
        !retrievedInvoice.isPresent()
    }
}
