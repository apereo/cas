package org.apereo.cas.configuration.loader;

import org.apereo.cas.config.CasCoreConfigurationWatchAutoConfiguration;
import org.apereo.cas.config.CasCoreEnvironmentBootstrapAutoConfiguration;
import org.apereo.cas.config.CasCoreStandaloneBootstrapAutoConfiguration;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
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
public class ConfigurationPropertiesLoaderFactoryTests {
    @Autowired
    @Qualifier(ConfigurationPropertiesLoaderFactory.BEAN_NAME)
    private ConfigurationPropertiesLoaderFactory configurationPropertiesLoaderFactory;

    @Test
    void verifyOperation() throws Exception {
        val loader = configurationPropertiesLoaderFactory.getLoader(
            new ClassPathResource("directory/cas.groovy"), "groovyConfig");
        val source = loader.load();
        assertEquals("true", source.getProperty("cas.service-registry.core.init-from-json"));
        assertEquals("test::dev", source.getProperty("cas.authn.accept.users"));
        assertEquals("Static", source.getProperty("cas.authn.accept.name"));
    }
}
