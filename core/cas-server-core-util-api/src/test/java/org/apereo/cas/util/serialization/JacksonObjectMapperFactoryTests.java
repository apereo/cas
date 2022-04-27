package org.apereo.cas.util.serialization;

import org.apereo.cas.util.model.TriStateBoolean;

import com.fasterxml.jackson.annotation.JacksonInject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

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
public class JacksonObjectMapperFactoryTests {
    @Test
    public void verifyInjectableWithoutValue() throws Exception {
        val mapper = JacksonObjectMapperFactory.builder()
            .defaultTypingEnabled(false)
            .injectableValues(Map.of("firstName", "CAS"))
            .build()
            .toObjectMapper();
        val payload = mapper.readValue("{}", Payload.class);
        assertEquals("CAS", payload.getFirstName());
    }

    @Test
    public void verifyInjectableWithValue() throws Exception {
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
    public void verifyEmptyInjectable() throws Exception {
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
    private static class Payload implements Serializable {
        private static final long serialVersionUID = -4319570781108105888L;

        @JacksonInject("number")
        private long number;

        @JacksonInject("status")
        private TriStateBoolean status = TriStateBoolean.FALSE;

        @JacksonInject("firstName")
        private String firstName = "Adam";
    }
}
