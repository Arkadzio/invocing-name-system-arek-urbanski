package pl.futurecollars.invoicing.db.file

import pl.futurecollars.invoicing.utils.FilesService
import spock.lang.Specification
import spock.lang.Subject
import java.nio.file.Files
import java.nio.file.Paths

class IdServiceTest extends Specification {

    @Subject
    IdService idService

    def setup() {
        def tempFilePath = Paths.get("temp-id-service-file.txt")
        if (!Files.exists(tempFilePath)) {
            Files.createFile(tempFilePath)
        }
        idService = new IdService(new FilesService(), tempFilePath)
    }

    def cleanup() {
        def tempFilePath = Paths.get("temp-id-service-file.txt")
        Files.deleteIfExists(tempFilePath)
    }

    def "should return incremented id"() {
        when:
        def id = idService.getNextIdAndIncrement()

        then:
        id == 1
    }

    def "should return consecutive incremented ids"() {
        when:
        def firstId = idService.getNextIdAndIncrement()
        def secondId = idService.getNextIdAndIncrement()

        then:
        firstId == 1
        secondId == 2
    }
}