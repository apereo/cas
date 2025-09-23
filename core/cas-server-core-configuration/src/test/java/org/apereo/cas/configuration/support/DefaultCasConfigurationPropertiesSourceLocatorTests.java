package org.apereo.cas.configuration.support;

import org.apereo.cas.config.CasCoreConfigurationWatchAutoConfiguration;
import org.apereo.cas.config.CasCoreEnvironmentBootstrapAutoConfiguration;
import org.apereo.cas.config.CasCoreStandaloneBootstrapAutoConfiguration;
import org.apereo.cas.configuration.api.CasConfigurationPropertiesSourceLocator;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
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
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreEnvironmentBootstrapAutoConfiguration.class,
    CasCoreConfigurationWatchAutoConfiguration.class,
    CasCoreStandaloneBootstrapAutoConfiguration.class
},
    properties = {
        "spring.cloud.config.enabled=false",
        "cas.events.core.track-configuration-modifications=true"
    }
)
@Tag("CasConfiguration")
@ExtendWith(CasTestExtension.class)
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
    private Environment environment;

    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    void verifyLocator() {
        val source = casCoreBootstrapPropertySourceLocator.locate(environment);
        assertInstanceOf(CompositePropertySource.class, source);

        assertNotNull(casConfigurationWatchService);

        val composite = (CompositePropertySource) source;
        assertEquals("https://cas.example.org:9999", composite.getProperty("cas.server.name"));
        assertEquals("https://cas.example.org/something", composite.getProperty("cas.server.prefix"));
    }

    @Test
    void verifyPriority() {
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
    void verifyNoneProfile() {
        val mockEnv = new MockEnvironment();
        mockEnv.setActiveProfiles(CasConfigurationPropertiesSourceLocator.PROFILE_NONE);
        val source = CasConfigurationPropertiesSourceLocator.getStandaloneProfileConfigurationDirectory(mockEnv);
        assertNull(source);
    }


    @Test
    void verifyGroovySlurper() {
        val source = casCoreBootstrapPropertySourceLocator.locate(environment);
        assertInstanceOf(CompositePropertySource.class, source);
        val composite = (CompositePropertySource) source;
        assertEquals("Static", composite.getProperty("cas.authn.accept.name"));
        assertEquals("test::dev", composite.getProperty("cas.authn.accept.users"));
    }

    @Test
    void verifyYamlLoaderThrows() {
        val resource = resourceLoader.getResource("classpath:/badyaml.yml");
        val configurationLoaders = CasConfigurationPropertiesSourceLocator.getConfigurationPropertiesLoaders();
        val foundLoader = configurationLoaders
            .stream()
            .filter(loader -> loader.supports(resource))
            .findFirst()
            .orElseThrow();
        assertThrows(YAMLException.class, () -> foundLoader.load(resource, environment, "test", CipherExecutor.noOpOfStringToString()));
    }

    @Test
    void verifySystemPropertiesOverrideCasConfiguration() {
        val source = casCoreBootstrapPropertySourceLocator.locate(environment);
        assertInstanceOf(CompositePropertySource.class, source);

        val composite = (CompositePropertySource) source;
        assertEquals("from-system-properties", composite.getProperty("test.overridden-by-system-property"));
    }
}
