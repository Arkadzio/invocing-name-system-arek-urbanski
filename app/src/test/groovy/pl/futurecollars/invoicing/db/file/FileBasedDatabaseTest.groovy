package pl.futurecollars.invoicing.db.file

import pl.futurecollars.invoicing.model.Invoice
import pl.futurecollars.invoicing.utils.FilesService
import pl.futurecollars.invoicing.utils.JsonService
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class FileBasedDatabaseTest extends Specification {

    FilesService filesService
    JsonService jsonService
    IdService idService
    Path databasePath
    FileBasedDatabase database

    def setup() {
        filesService = Mock(FilesService)
        jsonService = Mock(JsonService)
        idService = Mock(IdService)
        databasePath = Paths.get("test_database.txt")
        database = new FileBasedDatabase(filesService, jsonService, idService, databasePath)
    }

    def "should handle empty list result from readAllLines()"() {
        given:
        filesService.readAllLines(databasePath) >> []

        when:
        Optional<Invoice> retrievedInvoice = database.getById(1)

        then:
        !retrievedInvoice.isPresent()
    }
}
