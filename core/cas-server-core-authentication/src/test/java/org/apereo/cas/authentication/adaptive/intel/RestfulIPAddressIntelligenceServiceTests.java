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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestfulIPAddressIntelligenceServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("RestfulApi")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseAuthenticationTests.SharedTestConfiguration.class,
    properties = "cas.authn.adaptive.ip-intel.rest.url=https://localhost/ip-intel")
class RestfulIPAddressIntelligenceServiceTests {
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
        assertInstanceOf(RestfulIPAddressIntelligenceService.class, ipAddressIntelligenceService);
    }

    @Test
    void verifyAllowed() throws Throwable {
        try (val webServer = new MockWebServer(HttpStatus.OK)) {
            webServer.start();
            val result = serviceFor(webServer).examine(
                MockRequestContext.create(applicationContext), "1.2.3.4");
            assertNotNull(result);
            assertTrue(result.isAllowed());
        }
    }

    @Test
    void verifyBanned() throws Throwable {
        try (val webServer = new MockWebServer(HttpStatus.FORBIDDEN)) {
            webServer.start();
            val result = serviceFor(webServer).examine(
                MockRequestContext.create(applicationContext), "1.2.3.4");
            assertNotNull(result);
            assertTrue(result.isBanned());
        }
    }

    @Test
    void verifyRanked() throws Throwable {
        try (val webServer = new MockWebServer(
            new ByteArrayResource("12.435".getBytes(StandardCharsets.UTF_8)),
            HttpStatus.PRECONDITION_REQUIRED)) {
            webServer.start();
            val result = serviceFor(webServer).examine(
                MockRequestContext.create(applicationContext), "1.2.3.4");
            assertNotNull(result);
            assertTrue(result.isRanked());
        }
    }

    @Test
    void verifyRejectedIpAddressIsNeverContacted() throws Throwable {
        val properties = new AdaptiveAuthenticationProperties();
        properties.getPolicy().setRejectIpAddresses("123\\..*");
        properties.getIpIntel().getRest().setUrl("https://localhost/ip-intel");
        val service = new RestfulIPAddressIntelligenceService(tenantExtractor, properties);
        val result = service.examine(MockRequestContext.create(applicationContext), "123.4.5.6");
        assertNotNull(result);
        assertTrue(result.isBanned());
    }

    private RestfulIPAddressIntelligenceService serviceFor(final MockWebServer webServer) {
        val properties = new AdaptiveAuthenticationProperties();
        val rest = properties.getIpIntel().getRest();
        rest.setUrl("http://localhost:%s".formatted(webServer.getPort()));
        rest.setMaximumRetryAttempts(0);
        return new RestfulIPAddressIntelligenceService(tenantExtractor, properties);
    }
}
