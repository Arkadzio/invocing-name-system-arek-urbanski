package pl.futurecollars.invoicing.utils

import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class FilesServiceTest extends Specification {

    def "should append line to file"() {
        given:
        Path filePath = Paths.get("test_file.txt")
        if (!Files.exists(filePath)) {
            Files.createFile(filePath)
        }
        and:
        def linesBefore = Files.readAllLines(filePath)

        when:
        new FilesService().appendLineToFile(filePath, "test")

        then:
        def linesAfter = Files.readAllLines(filePath)
        linesAfter == linesBefore + "test"
    }

    def "should write single line to file"() {
        given:
        Path filePath = Paths.get("test_file.txt")
        if (!Files.exists(filePath)) {
            Files.createFile(filePath)
        }

        when:
        new FilesService().writeSingleLineToFile(filePath, "test")

        then:
        Files.readAllLines(filePath) == ["test"]
    }

    def "should write all lines to file"() {
        given:
        Path filePath = Paths.get("test_file.txt")
        List<String> lines = ["line1", "line2", "line3"]

        when:
        new FilesService().writeAllLinesToFile(filePath, lines)

        then:
        Files.readAllLines(filePath) == lines
    }

    def "should read all lines from file"() {
        given:
        Path filePath = Paths.get("test_file.txt")
        List<String> lines = ["line1", "line2", "line3"]
        Files.write(filePath, lines)

        expect:
        new FilesService().readAllLines(filePath) == lines
    }
}
