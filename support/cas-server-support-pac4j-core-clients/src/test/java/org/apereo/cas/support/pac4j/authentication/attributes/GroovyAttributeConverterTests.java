package org.apereo.cas.support.pac4j.authentication.attributes;

import org.apereo.cas.config.CasCoreMultitenancyAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyAttributeConverterTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class,
    CasCoreMultitenancyAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Groovy")
@ExtendWith(CasTestExtension.class)
class GroovyAttributeConverterTests {
    @Test
    void verifyUnknownType() {
        val converter = new GroovyAttributeConverter();
        assertFalse(converter.accept("unknown"));
        assertEquals("value", converter.convert("value"));
    }

    @Test
    void verifyScript() {
        val converter = new GroovyAttributeConverter();
        assertTrue(converter.accept("groovy { return attribute.toString() + '-test' }"));
        assertEquals("value-test", converter.convert("value"));
    }
}
