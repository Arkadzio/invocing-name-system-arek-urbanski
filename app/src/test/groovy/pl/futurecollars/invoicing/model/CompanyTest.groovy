package pl.futurecollars.invoicing.model

import spock.lang.Specification

class CompanyTest extends Specification {

    def "constructor should correctly initialize fields"() {
        given:
        def taxIdentificationNumber = "1234567890"
        def address = "Capital City"
        def name = "Arkadius Company"

        when:
        def company = new Company(taxIdentificationNumber, address, name)

        then:
        company.taxIdentificationNumber == taxIdentificationNumber
        company.address == address
        company.name == name
    }

    def "getter and setter methods should work correctly"() {
        given:
        def company = new Company("123", "Test Street", "Test Company")

        when:
        company.setTaxIdentificationNumber("0123456789")
        company.setAddress("Capital City")
        company.setName("Arkadius Company")

        then:
        company.getTaxIdentificationNumber() == "0123456789"
        company.getAddress() == "Capital City"
        company.getName() == "Arkadius Company"
    }
}
