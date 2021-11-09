package org.apereo.cas.configuration.support;

import org.apereo.cas.configuration.api.CasConfigurationPropertiesSourceLocator;
import org.apereo.cas.configuration.config.CasCoreConfigurationWatchConfiguration;
import org.apereo.cas.configuration.config.CasCoreEnvironmentConfiguration;
import org.apereo.cas.configuration.config.standalone.CasCoreBootstrapStandaloneConfiguration;
import org.apereo.cas.configuration.loader.ConfigurationPropertiesLoaderFactory;
import org.apereo.cas.util.spring.CasEventListener;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.yaml.snakeyaml.error.YAMLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultCasConfigurationPropertiesSourceLocatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreEnvironmentConfiguration.class,
    CasCoreConfigurationWatchConfiguration.class,
    CasCoreBootstrapStandaloneConfiguration.class
},
    properties = {
        "spring.cloud.config.enabled=false",
        "cas.events.core.track-configuration-modifications=true"
    }
)
@Tag("CasConfiguration")
public class DefaultCasConfigurationPropertiesSourceLocatorTests {
    static {
        System.setProperty("spring.application.name", "cas");
        System.setProperty("spring.profiles.active", CasConfigurationPropertiesSourceLocator.PROFILE_STANDALONE + ",dev");
        System.setProperty("cas.standalone.configuration-directory", "src/test/resources/directory");
        System.setProperty("cas.standalone.configuration-file", "src/test/resources/standalone.properties");
    }

    @Autowired
    @Qualifier("casCoreBootstrapPropertySourceLocator")
    private PropertySourceLocator casCoreBootstrapPropertySourceLocator;

    @Autowired
    @Qualifier("casConfigurationWatchService")
    private CasEventListener casConfigurationWatchService;

    @Autowired
    private ConfigurationPropertiesLoaderFactory configurationPropertiesLoaderFactory;

    @Autowired
    private Environment environment;

    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    public void verifyLocator() {
        val source = casCoreBootstrapPropertySourceLocator.locate(environment);
        assertTrue(source instanceof CompositePropertySource);

        assertNotNull(casConfigurationWatchService);

        val composite = (CompositePropertySource) source;
        assertEquals("https://cas.example.org:9999", composite.getProperty("cas.server.name"));
        assertEquals("https://cas.example.org/something", composite.getProperty("cas.server.prefix"));
    }

    @Test
    public void verifyPriority() {
        val source = casCoreBootstrapPropertySourceLocator.locate(environment);
        assertTrue(source instanceof CompositePropertySource);
        val composite = (CompositePropertySource) source;
        assertEquals("file", composite.getProperty("test.file"));
        assertEquals("dirAppYml", composite.getProperty("test.dir.app"));
        assertEquals("classpathAppYml", composite.getProperty("test.classpath"));
        assertEquals("devProfileProp", composite.getProperty("test.dir.profile"));
        assertEquals("standaloneProfileProp", composite.getProperty("profile.override.me"));
        assertEquals("dirCasProp", composite.getProperty("test.dir.cas"));
    }

    @Test
    public void verifyGroovySlurper() {
        val source = casCoreBootstrapPropertySourceLocator.locate(environment);
        assertTrue(source instanceof CompositePropertySource);
        val composite = (CompositePropertySource) source;
        assertEquals("Static", composite.getProperty("cas.authn.accept.name"));
        assertEquals("test::dev", composite.getProperty("cas.authn.accept.users"));
    }

    @Test
    public void verifyYamlLoaderThrows() {
        val loader = configurationPropertiesLoaderFactory.getLoader(
            resourceLoader.getResource("classpath:/badyaml.yml"), "test");
        assertThrows(YAMLException.class, loader::load);
    }
}
