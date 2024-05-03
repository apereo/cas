package org.apereo.cas;

import org.apereo.cas.config.CasEmbeddedContainerJettyAutoConfiguration;
import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasEmbeddedContainerJettyConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@SpringBootTest(classes = {
    CasEmbeddedContainerJettyAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    ServletWebServerFactoryAutoConfiguration.class,
    RefreshAutoConfiguration.class,
}, properties = {
    "server.port=${random.int[8000,9999]}",
    "cas.server.jetty.sni-host-check=false"
},
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Getter
@Tag("WebApp")
public class CasEmbeddedContainerJettyConfigurationTests {
    @Autowired
    @Qualifier("casJettyServerCustomizer")
    private JettyServerCustomizer casJettyServerCustomizer;

    @Test
    void verifyOperation() throws Exception {
        assertNotNull(casJettyServerCustomizer);
    }

}
