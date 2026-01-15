package org.apereo.cas.util;

import module java.base;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.api.CasConfigurationPropertiesSourceLocator;
import org.apereo.cas.configuration.loader.GroovyConfigurationPropertiesLoader;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyConfigurationPropertiesLoaderTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("Groovy")
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasCoreScriptingAutoConfiguration.class, properties = "spring.profiles.active=test")
@EnableConfigurationProperties(CasConfigurationProperties.class)
class GroovyConfigurationPropertiesLoaderTests {

    @Autowired
    private Environment environment;
    
    @Test
    void verifyOperation() {
        val loaders = CasConfigurationPropertiesSourceLocator.getConfigurationPropertiesLoaders();
        val groovyLoader = loaders
            .stream()
            .filter(loader -> loader.getName()
            .equals(GroovyConfigurationPropertiesLoader.class.getSimpleName()))
            .findFirst()
            .orElseThrow();
        val resource = new ClassPathResource("configuration.groovy");
        assertTrue(groovyLoader.supports(resource));
        val properties = groovyLoader.load(resource, environment, "test", CipherExecutor.noOpOfStringToString());
        assertEquals("test::alone", properties.getProperty("cas.authn.accept.users"));
        assertEquals("true", properties.getProperty("cas.service-registry.core.init-from-json"));
        assertEquals("Static", properties.getProperty("cas.authn.accept.name"));
    }
    
}
