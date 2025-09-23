package org.apereo.cas.authentication.adaptive.intel;

import org.apereo.cas.authentication.BaseAuthenticationTests;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestfulIPAddressIntelligenceServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("RestfulApi")
@ExtendWith(CasTestExtension.class)
class RestfulIPAddressIntelligenceServiceTests {

    @SpringBootTest(classes = BaseAuthenticationTests.SharedTestConfiguration.class,
        properties = "cas.authn.adaptive.ip-intel.rest.url=http://localhost:${random.int[3000,9999]}")
    abstract static class BaseTests {
        @Autowired
        protected CasConfigurationProperties casProperties;

        @Autowired
        @Qualifier(IPAddressIntelligenceService.BEAN_NAME)
        protected IPAddressIntelligenceService ipAddressIntelligenceService;

        @Autowired
        protected ConfigurableApplicationContext applicationContext;

        protected int resolvePort() throws Exception {
            val url = casProperties.getAuthn().getAdaptive().getIpIntel().getRest().getUrl();
            return new URI(SpringExpressionLanguageValueResolver.getInstance().resolve(url)).getPort();
        }
    }

    @Nested
    class AllowedTests extends BaseTests {
        @Test
        void verifyOperation() throws Throwable {
            val port = resolvePort();
            try (val webServer = new MockWebServer(port, HttpStatus.OK)) {
                webServer.start();

                val requestContext = MockRequestContext.create(applicationContext);
                val result = ipAddressIntelligenceService.examine(requestContext, "1.2.3.4");
                assertNotNull(result);
                assertTrue(result.isAllowed());
            }
        }
    }

    @Nested
    class BannedTests extends BaseTests {
        @Test
        void verifyOperation() throws Throwable {
            val port = resolvePort();
            try (val webServer = new MockWebServer(port, HttpStatus.FORBIDDEN)) {
                webServer.start();

                val requestContext = MockRequestContext.create(applicationContext);
                val result = ipAddressIntelligenceService.examine(requestContext, "1.2.3.4");
                assertNotNull(result);
                assertTrue(result.isBanned());
            }
        }
    }

    @Nested
    class RankedTests extends BaseTests {
        @Test
        void verifyOperation() throws Throwable {
            val port = resolvePort();
            try (val webServer = new MockWebServer(port, new ByteArrayResource("12.435".getBytes(StandardCharsets.UTF_8)), HttpStatus.PRECONDITION_REQUIRED)) {
                webServer.start();

                val requestContext = MockRequestContext.create(applicationContext);
                val result = ipAddressIntelligenceService.examine(requestContext, "1.2.3.4");
                assertNotNull(result);
                assertTrue(result.isRanked());
            }
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.adaptive.policy.reject-ip-addresses=123\\..*")
    class RejectedTests extends BaseTests {
        @Test
        void verifyOperation() throws Throwable {
            val port = resolvePort();
            try (val webServer = new MockWebServer(port, HttpStatus.PRECONDITION_REQUIRED)) {
                webServer.start();

                val requestContext = MockRequestContext.create(applicationContext);
                val result = ipAddressIntelligenceService.examine(requestContext, "1.2.3.4");
                assertNotNull(result);
                assertTrue(result.isBanned());
            }
        }
    }

}
