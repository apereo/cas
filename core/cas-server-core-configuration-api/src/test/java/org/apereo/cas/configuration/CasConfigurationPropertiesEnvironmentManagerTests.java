package org.apereo.cas.configuration;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
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
@Tag("CasConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasConfigurationPropertiesEnvironmentManagerTests {
    @Test
    public void verifyOperationByFile() throws Exception {
        val env = new MockEnvironment();
        val file = File.createTempFile("cas", ".properties");
        FileUtils.writeStringToFile(file, "server.port=8899", StandardCharsets.UTF_8);
        env.setProperty(CasConfigurationPropertiesEnvironmentManager.PROPERTY_CAS_STANDALONE_CONFIGURATION_FILE, file.getCanonicalPath());
        val mgr = new CasConfigurationPropertiesEnvironmentManager(new ConfigurationPropertiesBindingPostProcessor(), env);
        assertEquals(file.getCanonicalPath(), mgr.getStandaloneProfileConfigurationFile().getCanonicalPath());
    }

    @Test
    public void verifyOperationByDir() throws Exception {
        val env = new MockEnvironment();
        val dir = FileUtils.getTempDirectory();
        env.setProperty(CasConfigurationPropertiesEnvironmentManager.PROPERTY_CAS_STANDALONE_CONFIGURATION_DIRECTORY, dir.getCanonicalPath());
        val mgr = new CasConfigurationPropertiesEnvironmentManager(new ConfigurationPropertiesBindingPostProcessor(), env);
        assertEquals(dir.getCanonicalPath(), mgr.getStandaloneProfileConfigurationDirectory().getCanonicalPath());
    }

}
