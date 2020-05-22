package org.apereo.cas.configuration;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.*;

/**
 * This is {@link CasConfigurationWatchServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    AopAutoConfiguration.class
})
@Tag("Simple")
public class CasConfigurationWatchServiceTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    public void verifyOperationByDirectory() {
        val manager = mock(CasConfigurationPropertiesEnvironmentManager.class);
        when(manager.getStandaloneProfileConfigurationDirectory()).thenReturn(FileUtils.getTempDirectory());
        val service = new CasConfigurationWatchService(manager, applicationContext);
        service.runPathWatchServices(mock(ApplicationReadyEvent.class));
        service.close();
    }

    @Test
    public void verifyOperationByFile() throws Exception {
        val manager = mock(CasConfigurationPropertiesEnvironmentManager.class);
        val cas = File.createTempFile("cas", ".properties");
        FileUtils.writeStringToFile(cas, "server.port=0", StandardCharsets.UTF_8);
        when(manager.getStandaloneProfileConfigurationFile()).thenReturn(cas);
        val service = new CasConfigurationWatchService(manager, applicationContext);
        service.runPathWatchServices(mock(ApplicationReadyEvent.class));
        service.close();
    }
}
