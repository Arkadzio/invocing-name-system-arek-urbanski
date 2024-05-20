package pl.futurecollars.invoicing.service.invoice

import pl.futurecollars.invoicing.db.Database
import pl.futurecollars.invoicing.model.Invoice
import pl.futurecollars.invoicing.service.invoice.InvoiceService
import spock.lang.Specification

class InvoiceServiceTest extends Specification {

    private InvoiceService service
    private Database database

    def setup() {
        database = Mock()
        service = new InvoiceService(database);
    }

    def "calling save() should delegate to database save() method"() {
        given:
        Database database = Mock(Database)
        InvoiceService service = new InvoiceService(database)
        Invoice invoice = new Invoice()

        when:
        service.save(invoice)

        then:
        1 * database.save(invoice) >> 1
    }

    def "calling delete() should delegate to database delete() method"() {
        given:
        def invoiceId = 123

        when:
        service.delete(invoiceId)

        then:
        1 * database.delete(invoiceId)
    }

    def "calling getById() should delegate to database getById() method"() {
        given:
        def invoiceId = 321

        when:
        service.getById(invoiceId)

        then:
        1 * database.getById(invoiceId)
    }

    def "calling getAll() should delegate to database getAll() method"() {
        when:
        service.getAll()

        then:
        1 * database.getAll()
    }

    def "calling update() should delegate to database update() method"() {
        given:
        Database database = Mock(Database)
        InvoiceService service = new InvoiceService(database)
        Invoice updatedInvoice = new Invoice()
        updatedInvoice.id = 1

        when:
        service.update(1, updatedInvoice)
        
        then:
        1 * database.update(1, updatedInvoice)
    }
}
