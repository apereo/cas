package org.apereo.cas.metadata;

import org.apereo.cas.config.CasCoreConfigurationMetadataAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasConfigurationPropertiesValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasConfigurationPropertiesValidatorTests.CasConfigurationPropertiesTestConfiguration.class,
    CasCoreConfigurationMetadataAutoConfiguration.class
},
    properties = {
        "custom.type=TYPE2",
        "custom.flag=true",
        "cas.authn.saml-idp.core.session-storage-type=HTTP",
        "cas.unknown.setting=true",
        "cas.something=else",
        "cas.hello[0]=world"
    })
@EnableConfigurationProperties({CasConfigurationPropertiesValidatorTests.TestCustomProperties.class, CasConfigurationProperties.class})
@Tag("CasConfiguration")
@ExtendWith(CasTestExtension.class)
class CasConfigurationPropertiesValidatorTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private TestCustomProperties customProperties;

    @Test
    void verifyOperation() {
        val validator = new CasConfigurationPropertiesValidator(applicationContext);
        validator.setConfigurationPropertyClasses(List.of(CasConfigurationProperties.class, TestCustomProperties.class));
        val results = validator.validate();
        assertFalse(results.isEmpty());
        validator.printReport(results);
        assertEquals(CustomTypes.TYPE2, customProperties.getType());
    }

    @TestConfiguration(value = "CasConfigurationPropertiesTestConfiguration", proxyBeanMethods = false)
    public static class CasConfigurationPropertiesTestConfiguration {
        @Bean
        public CasConfigurationMetadataRepository casConfigurationMetadataRepository() throws Exception {
            try (val is = new ClassPathResource("META-INF/additional-spring-configuration-metadata.json").getInputStream()) {
                return new CasConfigurationMetadataRepository(List.of(is.readAllBytes()));
            }
        }
    }

    @ConfigurationProperties("custom")
    @Getter
    @Setter
    public static class TestCustomProperties {
        private CustomTypes type;

        @Deprecated
        private boolean flag;
    }

    public enum CustomTypes {
        TYPE1, @Deprecated TYPE2
    }
}
