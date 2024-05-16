package pl.futurecollars.invoicing.model

import spock.lang.Specification

import java.time.LocalDate

class InvoiceTest extends Specification {

    def "should create invoice with provided data"() {
        given:
        def id = 1
        def date = LocalDate.of(2024, 3, 25)
        def number = "3"
        def buyer = new Company(name: "Buyer Company", taxIdentificationNumber: "1234567890")
        def seller = new Company(name: "Seller Company", taxIdentificationNumber: "0987654321")
        def entries = [
                new InvoiceEntry(description: "Item 1", netPrice: 20, vatValue: 4.0, vatRate: Vat.VAT_8),
                new InvoiceEntry(description: "Item 2", netPrice: 10, vatValue:  2.0, vatRate: Vat.VAT_8)
        ]

        when:
        def invoice = new Invoice(id, date, number, buyer, seller, entries)

        then:
        invoice.date == date
        invoice.buyer == buyer
        invoice.seller == seller
        invoice.entries == entries
    }

    def "should create invoice with default constructor"() {
        when:
        def invoice = new Invoice()

        then:
        invoice != null
    }
}
