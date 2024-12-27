package org.apereo.cas.util.serialization;

import org.apereo.cas.test.CasTestExtension;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.Ordered;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JacksonObjectMapperCustomizerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("Utility")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
class JacksonObjectMapperCustomizerTests {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void verifyOperation() {
        val customizer = JacksonObjectMapperCustomizer.mappedInjectableValues(Map.of());
        assertEquals(Ordered.LOWEST_PRECEDENCE, customizer.getOrder());
        assertDoesNotThrow(() -> customizer.customize(MAPPER));
        assertTrue(customizer.getInjectableValues().isEmpty());
    }
}
