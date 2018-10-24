package org.apereo.cas.configuration;

import lombok.val;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.junit.Assert.*;

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
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();
    
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
