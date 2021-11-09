package org.apereo.cas.adaptors.radius;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.configuration.model.support.radius.RadiusClientProperties;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BlockingRadiusServerRadSecTransportTests}.
 * Runs test cases against a radius server running on "https://console.ironwifi.com/".
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("Radius")
@EnabledOnOs(OS.LINUX)
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreHttpConfiguration.class
}, properties = "cas.http-client.trust-store.file=classpath:truststore.jks")
public class BlockingRadiusServerRadSecTransportTests {
    @Autowired
    @Qualifier("casSslContext")
    private CasSSLContext casSslContext;

    @Test
    public void verifyOperation() {
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
