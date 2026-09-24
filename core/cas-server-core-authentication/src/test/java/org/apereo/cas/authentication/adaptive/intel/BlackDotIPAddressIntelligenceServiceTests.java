package org.apereo.cas.authentication.adaptive.intel;

import module java.base;
import org.apereo.cas.authentication.BaseAuthenticationTests;
import org.apereo.cas.configuration.model.core.authentication.AdaptiveAuthenticationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
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
 * This is {@link BlackDotIPAddressIntelligenceServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("RestfulApi")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseAuthenticationTests.SharedTestConfiguration.class,
    properties = "cas.authn.adaptive.ip-intel.black-dot.email-address=cas@apereo.org")
class BlackDotIPAddressIntelligenceServiceTests {
    private static final String CLIENT_IP = "37.58.59.181";

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
        assertInstanceOf(BlackDotIPAddressIntelligenceService.class, ipAddressIntelligenceService);
    }

    @Test
    void verifyTooManyRequests() throws Throwable {
        try (val webServer = new MockWebServer(HttpStatus.TOO_MANY_REQUESTS)) {
            webServer.start();
            val result = serviceFor(webServer).examine(
                MockRequestContext.create(applicationContext), CLIENT_IP);
            assertNotNull(result);
            assertTrue(result.isBanned());
        }
    }

    @Test
    void verifyErrorStatus() throws Throwable {
        try (val webServer = new MockWebServer(Map.of("status", "error"))) {
            webServer.start();
            val result = serviceFor(webServer).examine(
                MockRequestContext.create(applicationContext), CLIENT_IP);
            assertNotNull(result);
            assertTrue(result.isBanned());
        }
    }

    @Test
    void verifyBannedByRank() throws Throwable {
        try (val webServer = new MockWebServer(CollectionUtils.wrap("status", "success", "result", 1))) {
            webServer.start();
            val result = serviceFor(webServer).examine(
                MockRequestContext.create(applicationContext), CLIENT_IP);
            assertNotNull(result);
            assertTrue(result.isBanned());
        }
    }

    @Test
    void verifySuccess() throws Throwable {
        try (val webServer = new MockWebServer(CollectionUtils.wrap("status", "success", "result", 0))) {
            webServer.start();
            val result = serviceFor(webServer).examine(
                MockRequestContext.create(applicationContext), CLIENT_IP);
            assertNotNull(result);
            assertTrue(result.isAllowed());
        }
    }

    @Test
    void verifySuccessByRank() throws Throwable {
        try (val webServer = new MockWebServer(CollectionUtils.wrap("status", "success", "result", 0.4351))) {
            webServer.start();
            val result = serviceFor(webServer).examine(
                MockRequestContext.create(applicationContext), CLIENT_IP);
            assertNotNull(result);
            assertTrue(result.isRanked());
        }
    }

    @Test
    void verifyBadResponse() throws Throwable {
        try (val webServer = new MockWebServer("${bad-json$")) {
            webServer.start();
            val properties = propertiesFor(webServer);
            properties.getIpIntel().getBlackDot().setMode("DYNA_CHECK");
            val service = new BlackDotIPAddressIntelligenceService(tenantExtractor, properties);
            val result = service.examine(MockRequestContext.create(applicationContext), CLIENT_IP);
            assertNotNull(result);
            assertTrue(result.isBanned());
        }
    }

    private BlackDotIPAddressIntelligenceService serviceFor(final MockWebServer webServer) {
        return new BlackDotIPAddressIntelligenceService(tenantExtractor, propertiesFor(webServer));
    }

    private AdaptiveAuthenticationProperties propertiesFor(final MockWebServer webServer) {
        val properties = new AdaptiveAuthenticationProperties();
        val blackDot = properties.getIpIntel().getBlackDot();
        blackDot.setEmailAddress("cas@apereo.org");
        blackDot.setUrl("http://localhost:" + webServer.getPort() + "?ip=%s");
        return properties;
    }
}
