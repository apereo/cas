package org.apereo.cas.configuration;

import module java.base;
import org.apereo.cas.configuration.api.SimplePropertySource;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SimplePropertySourceTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
@Tag("CasConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
class SimplePropertySourceTests {
    @Test
    void verifyOperation() {
        val propertySource = new SimplePropertySource();
        assertNotNull(propertySource.getSource());
        assertNotNull(propertySource.getName());
        propertySource.setProperty("cas.test.property", "value1");
        assertEquals("value1", propertySource.getProperty("cas.test.property"));
        propertySource.removeProperty("cas.test.property");
        assertNull(propertySource.getProperty("cas.test.property"));

        propertySource.removeAll();
        assertTrue(propertySource.getSource().isEmpty());
        assertEquals(0, propertySource.getPropertyNames().length);
    }
}
