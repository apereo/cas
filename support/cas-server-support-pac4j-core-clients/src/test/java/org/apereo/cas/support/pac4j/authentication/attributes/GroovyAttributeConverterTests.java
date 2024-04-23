package org.apereo.cas.support.pac4j.authentication.attributes;

import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyAttributeConverterTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Groovy")
class GroovyAttributeConverterTests {
    @Test
    void verifyUnknownType() throws Throwable {
        val converter = new GroovyAttributeConverter();
        assertFalse(converter.accept("unknown"));
        assertEquals("value", converter.convert("value"));
    }

    @Test
    void verifyScript() throws Throwable {
        val converter = new GroovyAttributeConverter();
        assertTrue(converter.accept("groovy { return attribute.toString() + '-test' }"));
        assertEquals("value-test", converter.convert("value"));
    }
}
