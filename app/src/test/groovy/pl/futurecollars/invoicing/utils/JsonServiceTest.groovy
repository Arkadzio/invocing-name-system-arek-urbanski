package pl.futurecollars.invoicing.utils

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import spock.lang.Specification


class JsonServiceTest extends Specification {

    def "should convert object to JSON"() {
        given:
        def mapper = new ObjectMapper().registerModule(new JavaTimeModule())
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        def jsonService = new JsonService(mapper)
        def testObject = [name: "Arek", age: 44]

        when:
        def result = jsonService.toJson(testObject)

        then:
        def expectedJson = mapper.writeValueAsString(testObject)
        def actualJson = mapper.writeValueAsString(jsonService.toObject(result, Map))
        expectedJson == actualJson
    }

    def "should convert JSON to object"() {
        given:
        def mapper = new ObjectMapper().registerModule(new JavaTimeModule())
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        def jsonService = new JsonService(mapper)
        def json = '{"name":"Arek","age":44}'

        when:
        def result = jsonService.toObject(json, Map)

        then:
        result == [name: "Arek", age: 44]
    }
}
