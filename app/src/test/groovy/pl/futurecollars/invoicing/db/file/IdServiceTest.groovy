package pl.futurecollars.invoicing.db.file

import pl.futurecollars.invoicing.utils.FilesService
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Paths

class IdServiceTest extends Specification {

    IdService idService = new IdService(new FilesService(), Paths.get("test_id.txt"))

    def "should increment next ID and return it"() {
        given:
        int initialId = idService.getNextIdAndIncrement()

        when:
        int nextId = idService.getNextIdAndIncrement()

        then:
        nextId == initialId + 1
    }

    def "should read last ID from file and increment it"() {
        given:
        int initialId = 0
        Files.write(Paths.get("test_id.txt"), String.valueOf(initialId).bytes)

        when:
        int nextId = idService.getNextIdAndIncrement()

        then:
        nextId == initialId + 1
    }
}
