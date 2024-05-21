package pl.futurecollars.invoicing.model

import spock.lang.Specification

class InvoiceEntryTest extends Specification {

    def "should create invoice entry with default constructor"() {
        when:
        def invoiceEntry = new InvoiceEntry()

        then:
        invoiceEntry != null
    }
}
