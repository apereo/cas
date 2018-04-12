package org.apereo.cas.configuration.support;

import org.apereo.cas.configuration.api.CasConfigurationPropertiesSourceLocator;
import org.apereo.cas.configuration.config.CasCoreBootstrapStandaloneConfiguration;
import org.apereo.cas.configuration.config.CasCoreBootstrapStandaloneLocatorConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * This is {@link DefaultCasConfigurationPropertiesSourceLocatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreBootstrapStandaloneLocatorConfiguration.class,
    CasCoreBootstrapStandaloneConfiguration.class
})
@TestPropertySource(properties = {"spring.cloud.config.enabled=false", "spring.application.name=CAS"})
public class DefaultCasConfigurationPropertiesSourceLocatorTests {

    @Autowired
    @Qualifier("casConfigurationPropertiesSourceLocator")
    private CasConfigurationPropertiesSourceLocator casConfigurationPropertiesSourceLocator;

    @Autowired
    private Environment environment;

    @Autowired
    private ResourceLoader resourceLoader;

    static {
        System.setProperty("spring.profiles.active", "standalone");
        System.setProperty("cas.standalone.configurationFile", "src/test/resources/standalone.properties");
    }

    @Test
    public void verifyLocator() {
        final PropertySource source = casConfigurationPropertiesSourceLocator.locate(environment, resourceLoader);
        assertTrue(source instanceof CompositePropertySource);

        final CompositePropertySource composite = (CompositePropertySource) source;
        assertEquals("https://cas.example.org:9999", composite.getProperty("cas.server.name"));
        assertEquals("https://cas.example.org/something", composite.getProperty("cas.server.prefix"));
    }
}
