package org.apereo.cas.configuration.loader;

import org.apereo.cas.config.CasCoreConfigurationWatchAutoConfiguration;
import org.apereo.cas.config.CasCoreEnvironmentBootstrapAutoConfiguration;
import org.apereo.cas.config.CasCoreStandaloneBootstrapAutoConfiguration;
import org.apereo.cas.configuration.api.CasConfigurationPropertiesSourceLocator;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ConfigurationPropertiesLoaderFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTest(classes = {
    CasCoreEnvironmentBootstrapAutoConfiguration.class,
    CasCoreConfigurationWatchAutoConfiguration.class,
    CasCoreStandaloneBootstrapAutoConfiguration.class
})
@ActiveProfiles("dev")
@Tag("Groovy")
@ExtendWith(CasTestExtension.class)
class ConfigurationPropertiesLoaderFactoryTests {
    @Autowired
    private Environment environment;

    @Test
    void verifyOperation() {
        val resource = new ClassPathResource("directory/cas.groovy");
        val configurationLoaders = CasConfigurationPropertiesSourceLocator.getConfigurationPropertiesLoaders();
        val foundLoader = configurationLoaders
            .stream()
            .filter(loader -> loader.supports(resource))
            .findFirst()
            .orElseThrow();
        val source = foundLoader.load(resource, environment, "groovyConfig", CipherExecutor.noOpOfStringToString());
        assertEquals("true", source.getProperty("cas.service-registry.core.init-from-json"));
        assertEquals("test::dev", source.getProperty("cas.authn.accept.users"));
        assertEquals("Static", source.getProperty("cas.authn.accept.name"));
    }
}
