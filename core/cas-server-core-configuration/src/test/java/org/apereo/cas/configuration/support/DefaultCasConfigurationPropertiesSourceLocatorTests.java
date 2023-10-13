package org.apereo.cas.configuration.support;

import org.apereo.cas.config.CasCoreConfigurationWatchConfiguration;
import org.apereo.cas.config.CasCoreEnvironmentBootstrapConfiguration;
import org.apereo.cas.config.CasCoreStandaloneBootstrapConfiguration;
import org.apereo.cas.configuration.api.CasConfigurationPropertiesSourceLocator;
import org.apereo.cas.configuration.loader.ConfigurationPropertiesLoaderFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.env.MockEnvironment;
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
    WebMvcAutoConfiguration.class,
    CasCoreEnvironmentBootstrapConfiguration.class,
    CasCoreConfigurationWatchConfiguration.class,
    CasCoreStandaloneBootstrapConfiguration.class
},
    properties = {
        "spring.cloud.config.enabled=false",
        "cas.events.core.track-configuration-modifications=true"
    }
)
@Tag("CasConfiguration")
class DefaultCasConfigurationPropertiesSourceLocatorTests {
    static {
        System.setProperty("spring.application.name", "cas");
        System.setProperty("spring.profiles.active", CasConfigurationPropertiesSourceLocator.PROFILE_STANDALONE + ",dev");
        System.setProperty("cas.standalone.configuration-directory", "src/test/resources/directory");
        System.setProperty("cas.standalone.configuration-file", "src/test/resources/standalone.properties");
        System.setProperty("test.overridden-by-system-property", "from-system-properties");
    }

    @Autowired
    @Qualifier(CasConfigurationPropertiesSourceLocator.BOOTSTRAP_PROPERTY_LOCATOR_BEAN_NAME)
    private PropertySourceLocator casCoreBootstrapPropertySourceLocator;

    @Autowired
    @Qualifier("casConfigurationWatchService")
    private InitializingBean casConfigurationWatchService;

    @Autowired
    private ConfigurationPropertiesLoaderFactory configurationPropertiesLoaderFactory;

    @Autowired
    private Environment environment;

    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    void verifyLocator() throws Throwable {
        val source = casCoreBootstrapPropertySourceLocator.locate(environment);
        assertInstanceOf(CompositePropertySource.class, source);

        assertNotNull(casConfigurationWatchService);

        val composite = (CompositePropertySource) source;
        assertEquals("https://cas.example.org:9999", composite.getProperty("cas.server.name"));
        assertEquals("https://cas.example.org/something", composite.getProperty("cas.server.prefix"));
    }

    @Test
    void verifyPriority() throws Throwable {
        val source = casCoreBootstrapPropertySourceLocator.locate(environment);
        assertInstanceOf(CompositePropertySource.class, source);
        val composite = (CompositePropertySource) source;
        assertEquals("file", composite.getProperty("test.file"));
        assertEquals("dirAppYml", composite.getProperty("test.dir.app"));
        assertEquals("classpathAppYml", composite.getProperty("test.classpath"));
        assertEquals("devProfileProp", composite.getProperty("test.dir.profile"));
        assertEquals("standaloneProfileProp", composite.getProperty("profile.override.me"));
        assertEquals("dirCasProp", composite.getProperty("test.dir.cas"));
    }

    @Test
    void verifyNoneProfile() throws Throwable {
        val mockEnv =new MockEnvironment();
        mockEnv.setActiveProfiles(CasConfigurationPropertiesSourceLocator.PROFILE_NONE);
        val source = CasConfigurationPropertiesSourceLocator.getStandaloneProfileConfigurationDirectory(mockEnv);
        assertNull(source);
    }


    @Test
    void verifyGroovySlurper() throws Throwable {
        val source = casCoreBootstrapPropertySourceLocator.locate(environment);
        assertInstanceOf(CompositePropertySource.class, source);
        val composite = (CompositePropertySource) source;
        assertEquals("Static", composite.getProperty("cas.authn.accept.name"));
        assertEquals("test::dev", composite.getProperty("cas.authn.accept.users"));
    }

    @Test
    void verifyYamlLoaderThrows() throws Throwable {
        val loader = configurationPropertiesLoaderFactory.getLoader(
            resourceLoader.getResource("classpath:/badyaml.yml"), "test");
        assertThrows(YAMLException.class, loader::load);
    }

    @Test
    void verifySystemPropertiesOverrideCasConfiguration() throws Throwable {
        val source = casCoreBootstrapPropertySourceLocator.locate(environment);
        assertInstanceOf(CompositePropertySource.class, source);

        val composite = (CompositePropertySource) source;
        assertEquals("from-system-properties", composite.getProperty("test.overridden-by-system-property"));
    }
}
