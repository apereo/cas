package org.apereo.cas.configuration;

import java.nio.file.Files;
import org.apereo.cas.configuration.api.CasConfigurationPropertiesSourceLocator;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.env.MockEnvironment;
import java.io.File;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasConfigurationPropertiesEnvironmentManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
@Tag("CasConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
class CasConfigurationPropertiesEnvironmentManagerTests {
    @Test
    void verifyOperationByFile() throws Throwable {
        val env = new MockEnvironment();
        val sources = CasConfigurationPropertiesEnvironmentManager.configureEnvironmentPropertySources(env);
        env.getPropertySources().addFirst(sources);

        val file = Files.createTempFile("cas", ".properties").toFile();
        FileUtils.writeStringToFile(file, "server.port=8899", StandardCharsets.UTF_8);
        env.setProperty(CasConfigurationPropertiesSourceLocator.PROPERTY_CAS_STANDALONE_CONFIGURATION_FILE, file.getCanonicalPath());
        assertEquals(file.getCanonicalPath(), CasConfigurationPropertiesSourceLocator.getStandaloneProfileConfigurationFile(env).getCanonicalPath());
    }

    @Test
    void verifyOperationByDir() throws Throwable {
        val env = new MockEnvironment();
        val sources = CasConfigurationPropertiesEnvironmentManager.configureEnvironmentPropertySources(env);
        env.getPropertySources().addFirst(sources);
        val dir = FileUtils.getTempDirectory();
        env.setProperty(CasConfigurationPropertiesSourceLocator.PROPERTY_CAS_STANDALONE_CONFIGURATION_DIRECTORY, dir.getCanonicalPath());
        assertEquals(dir.getCanonicalPath(), CasConfigurationPropertiesSourceLocator.getStandaloneProfileConfigurationDirectory(env).getCanonicalPath());
    }

}
