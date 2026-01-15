package org.apereo.cas.configuration;

import module java.base;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasConfigurationPropertiesTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    AopAutoConfiguration.class,
    CasConfigurationPropertiesTests.CasPropertiesTestConfiguration.class
})
@Tag("CasConfiguration")
@ExtendWith(CasTestExtension.class)
class CasConfigurationPropertiesTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    void verifySerialization() {
        val result = SerializationUtils.serialize(casProperties);
        assertNotNull(result);
        val props = SerializationUtils.deserialize(result);
        assertNotNull(props);
    }

    @TestConfiguration(value = "CasPropertiesTestConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasPropertiesTestConfiguration {
    }
}
