package org.apereo.cas.configuration;

import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * This is {@link CasConfigurationWatchServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = AopAutoConfiguration.class)
@Tag("CasConfiguration")
@ExtendWith(CasTestExtension.class)
class CasConfigurationWatchServiceTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperationByFile() throws Throwable {
        val cas = Files.createTempFile("cas", ".properties").toFile();
        FileUtils.writeStringToFile(cas, "server.port=0", StandardCharsets.UTF_8);
        val service = new CasConfigurationWatchService(applicationContext);
        service.initialize();

        val newFile = new File(cas.getParentFile(), "something");
        FileUtils.writeStringToFile(newFile, "helloworld", StandardCharsets.UTF_8);
        FileUtils.writeStringToFile(newFile, "helloworld-update", StandardCharsets.UTF_8, true);
        FileUtils.deleteQuietly(newFile);
        service.close();
    }
}
