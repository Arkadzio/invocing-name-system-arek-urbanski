package pl.futurecollars.invoicing.db.memory

import pl.futurecollars.invoicing.db.AbstractDatabaseTest
import pl.futurecollars.invoicing.db.Database
import pl.futurecollars.invoicing.model.Invoice
import spock.lang.Specification

class InMemoryDatabaseTest extends AbstractDatabaseTest {

    @Override
    Database getDatabaseInstance(){
        return new InMemoryDatabase();
    }

}
