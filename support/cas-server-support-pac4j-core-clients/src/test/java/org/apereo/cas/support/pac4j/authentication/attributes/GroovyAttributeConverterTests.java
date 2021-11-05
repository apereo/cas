package org.apereo.cas.support.pac4j.authentication.attributes;

import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
    CasCoreUtilConfiguration.class,
    CasCoreHttpConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Groovy")
public class GroovyAttributeConverterTests {
    @Test
    public void verifyUnknownType() {
        val c = new GroovyAttributeConverter();
        assertFalse(c.accept("unknown"));
        assertEquals("value", c.convert("value"));
    }

    @Test
    public void verifyScript() {
        val c = new GroovyAttributeConverter();
        assertTrue(c.accept("groovy { return attribute.toString() + '-test' }"));
        assertEquals("value-test", c.convert("value"));
    }
}
