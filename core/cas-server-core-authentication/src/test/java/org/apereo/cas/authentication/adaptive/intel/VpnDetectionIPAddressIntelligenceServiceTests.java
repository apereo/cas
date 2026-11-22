package org.apereo.cas.authentication.adaptive.intel;

import module java.base;
import org.apereo.cas.authentication.BaseAuthenticationTests;
import org.apereo.cas.configuration.model.core.authentication.AdaptiveAuthenticationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.MockWebServer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link VpnDetectionIPAddressIntelligenceServiceTests}.
 *
 * @author Uman Shahzad
 * @since 8.1.0
 */
@Tag("RestfulApi")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseAuthenticationTests.SharedTestConfiguration.class,
    properties = "cas.authn.adaptive.ip-intel.vpn-detection.enabled=true")
class VpnDetectionIPAddressIntelligenceServiceTests {
    private static final String CLIENT_IP = "185.65.135.230";

    @Autowired
    @Qualifier(IPAddressIntelligenceService.BEAN_NAME)
    private IPAddressIntelligenceService ipAddressIntelligenceService;

    @Autowired
    @Qualifier(TenantExtractor.BEAN_NAME)
    private TenantExtractor tenantExtractor;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyServiceIsResolvedFromConfiguration() {
        assertInstanceOf(VpnDetectionIPAddressIntelligenceService.class, ipAddressIntelligenceService);
    }

    @Test
    void verifyVpnIsBanned() throws Throwable {
        assertTrue(examine(response(true, false, false), false).isBanned());
    }

    @Test
    void verifyTorIsBanned() throws Throwable {
        assertTrue(examine(response(false, true, false), false).isBanned());
    }

    @Test
    void verifyRelayIsAllowedByDefault() throws Throwable {
        assertTrue(examine(response(false, false, true), false).isAllowed());
    }

    @Test
    void verifyRelayIsBannedWhenConfigured() throws Throwable {
        assertTrue(examine(response(false, false, true), true).isBanned());
    }

    @Test
    void verifyAllowed() throws Throwable {
        assertTrue(examine(response(false, false, false), true).isAllowed());
    }

    @Test
    void verifyErrorStatus() throws Throwable {
        try (val webServer = new MockWebServer(HttpStatus.BAD_REQUEST, "{\"error\":\"not a valid IP address\"}")) {
            webServer.start();
            assertTrue(examine(webServer, false).isBanned());
        }
    }

    @Test
    void verifyUnexpectedResponse() throws Throwable {
        try (val webServer = new MockWebServer(Map.of("ip", CLIENT_IP))) {
            webServer.start();
            assertTrue(examine(webServer, false).isBanned());
        }
    }

    @Test
    void verifyBadResponse() throws Throwable {
        try (val webServer = new MockWebServer("${bad-json$")) {
            webServer.start();
            assertTrue(examine(webServer, false).isBanned());
        }
    }

    private static Map<String, Object> response(final boolean vpn, final boolean tor, final boolean relay) {
        return Map.of("ip", CLIENT_IP, "is_bogon", false, "is_vpn", vpn, "is_tor", tor, "is_relay", relay);
    }

    private IPAddressIntelligenceResponse examine(final Map<String, Object> body, final boolean banRelays) throws Throwable {
        try (val webServer = new MockWebServer(body)) {
            webServer.start();
            return examine(webServer, banRelays);
        }
    }

    private IPAddressIntelligenceResponse examine(final MockWebServer webServer, final boolean banRelays) throws Throwable {
        val properties = new AdaptiveAuthenticationProperties();
        properties.getIpIntel().getVpnDetection()
            .setEnabled(true)
            .setBanRelays(banRelays)
            .setUrl("http://localhost:" + webServer.getPort() + "/%s");
        val service = new VpnDetectionIPAddressIntelligenceService(tenantExtractor, properties);
        val result = service.examine(MockRequestContext.create(applicationContext), CLIENT_IP);
        assertNotNull(result);
        return result;
    }
}
