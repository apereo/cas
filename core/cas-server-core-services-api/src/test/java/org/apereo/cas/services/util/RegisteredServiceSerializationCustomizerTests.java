package org.apereo.cas.services.util;

import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RegisteredServiceSerializationCustomizerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("RegisteredService")
public class RegisteredServiceSerializationCustomizerTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder().build().toObjectMapper();

    @Test
    public void verifyOperation() {
        val customizer = RegisteredServiceSerializationCustomizer.noOp();
        assertEquals(Ordered.LOWEST_PRECEDENCE, customizer.getOrder());
        assertDoesNotThrow(() -> customizer.customize(MAPPER));
        assertTrue(customizer.getInjectableValues().isEmpty());
    }
}
