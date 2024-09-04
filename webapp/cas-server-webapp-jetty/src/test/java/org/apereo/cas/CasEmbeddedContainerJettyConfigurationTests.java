package org.apereo.cas;

import org.apereo.cas.config.CasEmbeddedContainerJettyAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasEmbeddedContainerJettyConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasEmbeddedContainerJettyAutoConfiguration.class, properties = {
    "server.port=${random.int[8000,9999]}",
    "server.ssl.enabled=false",
    "cas.server.jetty.sni-host-check=false"
}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Getter
@Tag("WebApp")
@ExtendWith(CasTestExtension.class)
class CasEmbeddedContainerJettyConfigurationTests {
    @Autowired
    @Qualifier("casJettyServerCustomizer")
    private JettyServerCustomizer casJettyServerCustomizer;

    @Test
    void verifyOperation() throws Exception {
        assertNotNull(casJettyServerCustomizer);
    }

}
