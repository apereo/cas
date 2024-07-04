package org.apereo.cas.util.serialization;

import org.apereo.cas.configuration.support.TriStateBoolean;
import org.apereo.cas.test.CasTestExtension;
import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JacksonObjectMapperFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("Utility")
@ExtendWith(CasTestExtension.class)
class JacksonObjectMapperFactoryTests {
    @Test
    void verifyYamlFactory() throws Throwable {
        val mapper = JacksonObjectMapperFactory.builder()
            .jsonFactory(new YAMLFactory())
            .build()
            .toObjectMapper();
        val payload = new Payload().setFirstName("Bob").setNumber(1000);
        val results = mapper.writeValueAsString(payload);
        val read = mapper.readValue(results, Payload.class);
        assertEquals(read, payload);
    }

    @Test
    void verifyXMLFactory() throws Throwable {
        val mapper = JacksonObjectMapperFactory.builder()
            .jsonFactory(new XmlFactory())
            .build()
            .toObjectMapper();
        val payload = new Payload().setFirstName("Bob").setNumber(1000);
        val results = mapper.writeValueAsString(payload);
        val read = mapper.readValue(results, Payload.class);
        assertEquals(read, payload);
    }

    @Test
    void verifyInjectableWithoutValue() throws Throwable {
        val mapper = JacksonObjectMapperFactory.builder()
            .defaultTypingEnabled(false)
            .injectableValues(Map.of("firstName", "CAS"))
            .build()
            .toObjectMapper();
        val payload = mapper.readValue("{}", Payload.class);
        assertEquals("CAS", payload.getFirstName());
    }

    @Test
    void verifyInjectableWithValue() throws Throwable {
        val values = new HashMap<String, Object>();
        values.put("firstName", "CAS");
        values.put("status", "true");
        values.put("number", 1000L);

        val mapper = JacksonObjectMapperFactory.builder()
            .defaultTypingEnabled(false)
            .injectableValues(values)
            .build()
            .toObjectMapper();

        val payload = mapper.readValue("{\"firstName\": \"John\"}", Payload.class);
        assertEquals("John", payload.getFirstName());
        assertTrue(payload.getStatus().isTrue());
        assertEquals(1000L, payload.getNumber());
    }

    @Test
    void verifyEmptyInjectable() throws Throwable {
        val mapper = JacksonObjectMapperFactory.builder()
            .defaultTypingEnabled(false)
            .build()
            .toObjectMapper();
        val payload = mapper.readValue("{}", Payload.class);
        assertEquals("Adam", payload.getFirstName());
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @Accessors(chain = true)
    @EqualsAndHashCode
    private static final class Payload implements Serializable {
        @Serial
        private static final long serialVersionUID = -4319570781108105888L;

        @JacksonInject("number")
        private long number;

        @JacksonInject("status")
        private TriStateBoolean status = TriStateBoolean.FALSE;

        @JacksonInject("firstName")
        private String firstName = "Adam";
    }
}
