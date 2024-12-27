package org.apereo.cas.configuration;

import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasConfigurationPropertiesValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = AopAutoConfiguration.class, properties = {"cas.something=else", "cas.hello[0]=world"})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("CasConfiguration")
@ExtendWith(CasTestExtension.class)
class CasConfigurationPropertiesValidatorTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() {
        val validator = new CasConfigurationPropertiesValidator(applicationContext);
        assertFalse(validator.validate().isEmpty());
    }
}
