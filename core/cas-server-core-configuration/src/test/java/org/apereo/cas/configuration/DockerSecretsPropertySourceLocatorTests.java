package org.apereo.cas.configuration;

import org.apereo.cas.config.CasCoreConfigurationWatchAutoConfiguration;
import org.apereo.cas.config.CasCoreEnvironmentBootstrapAutoConfiguration;
import org.apereo.cas.config.CasCoreStandaloneBootstrapAutoConfiguration;
import org.apereo.cas.configuration.api.CasConfigurationPropertiesSourceLocator;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.Environment;
import java.io.File;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DockerSecretsPropertySourceLocatorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreEnvironmentBootstrapAutoConfiguration.class,
    CasCoreConfigurationWatchAutoConfiguration.class,
    CasCoreStandaloneBootstrapAutoConfiguration.class
}, properties = "spring.cloud.config.enabled=false")
@Tag("CasConfiguration")
@ExtendWith(CasTestExtension.class)
class DockerSecretsPropertySourceLocatorTests {
    @Autowired
    @Qualifier(CasConfigurationPropertiesSourceLocator.BOOTSTRAP_PROPERTY_LOCATOR_BEAN_NAME)
    private PropertySourceLocator casCoreBootstrapPropertySourceLocator;

    @Autowired
    private Environment environment;

    static {
        try {
            val parentFile = new File(FileUtils.getTempDirectory(), "cas-docker");
            parentFile.mkdirs();
            val secretFile = new File(parentFile, "cas.authn.accept.name");
            Files.writeString(secretFile.toPath(), "Static");
            System.setProperty(DockerSecretsPropertySourceLocator.VAR_CAS_DOCKER_SECRETS_DIRECTORY, parentFile.getCanonicalPath());
            System.setProperty(DockerSecretsPropertySourceLocator.VAR_CONTAINER, "true");
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void verifyOperation() {
        val results = casCoreBootstrapPropertySourceLocator.locate(environment);
        val value = results.getProperty("cas.authn.accept.name");
        assertEquals("Static", value);
    }
}
