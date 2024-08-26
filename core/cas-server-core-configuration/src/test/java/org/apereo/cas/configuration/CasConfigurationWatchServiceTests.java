package org.apereo.cas.configuration;

import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * This is {@link CasConfigurationWatchServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    EndpointAutoConfiguration.class,
    WebEndpointAutoConfiguration.class,
    AopAutoConfiguration.class
})
@Tag("CasConfiguration")
@ExtendWith(CasTestExtension.class)
class CasConfigurationWatchServiceTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperationByFile() throws Throwable {
        val cas = File.createTempFile("cas", ".properties");
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
