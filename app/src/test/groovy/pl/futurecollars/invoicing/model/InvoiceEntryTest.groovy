package pl.futurecollars.invoicing.model

import spock.lang.Specification

class InvoiceEntryTest extends Specification {

//    def "should create invoice entry with provided data"() {
//        given:
//        def description = "Test entry"
//        def quantity = 1
//        def netPrice = new BigDecimal("100.00")
//        def vatValue = new BigDecimal("23.00")
//        def vatRate = Vat.VAT_23
//
//        when:
//        def invoiceEntry = new InvoiceEntry(description, quantity, netPrice, vatValue, vatRate)
//
//        then:
//        invoiceEntry.description == description
//        invoiceEntry.quantity == quantity
//        invoiceEntry.netPrice == netPrice
//        invoiceEntry.vatValue == vatValue
//        invoiceEntry.vatRate == vatRate
//    }

    def "should create invoice entry with default constructor"() {
        when:
        def invoiceEntry = new InvoiceEntry()

        then:
        invoiceEntry != null
    }
}
