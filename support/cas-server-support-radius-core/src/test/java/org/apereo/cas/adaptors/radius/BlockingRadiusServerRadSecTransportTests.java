package org.apereo.cas.adaptors.radius;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.config.CasCoreEnvironmentBootstrapAutoConfiguration;
import org.apereo.cas.config.CasCoreMultitenancyAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.configuration.model.support.radius.RadiusClientProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BlockingRadiusServerRadSecTransportTests}.
 * Runs test cases against a radius server running on "<a href="https://console.ironwifi.com/">here</a>".
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("Radius")
@ExtendWith(CasTestExtension.class)
@EnabledOnOs(OS.LINUX)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreEnvironmentBootstrapAutoConfiguration.class,
    CasCoreMultitenancyAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class
},
    properties = "cas.http-client.trust-store.file=classpath:truststore.jks")
class BlockingRadiusServerRadSecTransportTests {
    @Autowired
    @Qualifier(CasSSLContext.BEAN_NAME)
    private CasSSLContext casSslContext;

    @Test
    void verifyOperation() {
        val factory = RadiusClientFactory.builder()
            .authenticationPort(1234)
            .authenticationPort(5678)
            .socketTimeout(1)
            .inetAddress("localhost")
            .sharedSecret("secret")
            .transportType(RadiusClientProperties.RadiusClientTransportTypes.RADSEC)
            .sslContext(casSslContext)
            .build();
        assertNotNull(factory.toString());
        assertNotNull(factory.newInstance());
    }
}
