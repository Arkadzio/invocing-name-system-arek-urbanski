package pl.futurecollars.invoicing.db.file

import pl.futurecollars.invoicing.utils.FilesService
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
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

    def "should initialize nextId with 1 if file is empty"() {
        given:
        Path filePath = Paths.get("empty_file.txt")
        Files.deleteIfExists(filePath)
        Files.createFile(filePath)
        def filesService = new FilesService()

        when:
        new IdService(filesService, filePath)

        then:
        Files.readAllLines(filePath).first() == "1"
    }
}
