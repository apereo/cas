package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import net.bull.javamelody.MonitoringSpringAdvisor;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasJavaMelodyConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("CasConfiguration")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    CasJavaMelodyAutoConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
class CasJavaMelodyConfigurationTests {

    @Autowired
    @Qualifier("monitoringSpringServiceAdvisor")
    private MonitoringSpringAdvisor monitoringSpringServiceAdvisor;

    @Autowired
    @Qualifier("monitorableComponentsAdvisor")
    private MonitoringSpringAdvisor monitorableComponentsAdvisor;

    @Test
    void verifyOperation() throws Throwable {
        assertNotNull(monitoringSpringServiceAdvisor);
        assertNotNull(monitorableComponentsAdvisor);
    }
}
