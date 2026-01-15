package org.apereo.cas;

import module java.base;
import org.apereo.cas.config.CasEmbeddedContainerJettyAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jetty.JettyServerCustomizer;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasEmbeddedContainerJettyConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasEmbeddedContainerJettyAutoConfiguration.class, properties = {
    "server.ssl.enabled=false",
    "cas.server.jetty.sni-host-check=false"
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Getter
@Tag("WebApp")
@ExtendWith(CasTestExtension.class)
class CasEmbeddedContainerJettyConfigurationTests {
    @Autowired
    @Qualifier("casJettyServerCustomizer")
    private JettyServerCustomizer casJettyServerCustomizer;

    @Test
    void verifyOperation() {
        assertNotNull(casJettyServerCustomizer);
    }

}
