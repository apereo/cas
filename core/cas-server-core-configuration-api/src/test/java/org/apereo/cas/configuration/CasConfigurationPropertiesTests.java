package org.apereo.cas.configuration;

import lombok.val;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasConfigurationPropertiesTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTest(classes = {
    CasConfigurationPropertiesTests.CasPropertiesTestConfiguration.class,
    RefreshAutoConfiguration.class
})
public class CasConfigurationPropertiesTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifySerialization() {
        val result = SerializationUtils.serialize(casProperties);
        assertNotNull(result);
        val props = SerializationUtils.deserialize(result);
        assertNotNull(props);
    }

    @TestConfiguration
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasPropertiesTestConfiguration {
        
    }
}
