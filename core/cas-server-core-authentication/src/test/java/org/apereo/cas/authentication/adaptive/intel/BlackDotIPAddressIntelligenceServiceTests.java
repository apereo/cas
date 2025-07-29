package org.apereo.cas.authentication.adaptive.intel;

import org.apereo.cas.authentication.BaseAuthenticationTests;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
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
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import java.net.URI;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BlackDotIPAddressIntelligenceServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("RestfulApi")
@ExtendWith(CasTestExtension.class)
class BlackDotIPAddressIntelligenceServiceTests {
    @SpringBootTest(classes = BaseAuthenticationTests.SharedTestConfiguration.class,
        properties = {
            "cas.authn.adaptive.ip-intel.black-dot.email-address=cas@apereo.org",
            "cas.authn.adaptive.ip-intel.black-dot.url=http://localhost:${random.int[3000,9999]}?ip=%s"
        })
    abstract static class BaseTests {
        @Autowired
        protected CasConfigurationProperties casProperties;

        @Autowired
        @Qualifier(IPAddressIntelligenceService.BEAN_NAME)
        protected IPAddressIntelligenceService ipAddressIntelligenceService;

        @Autowired
        protected ConfigurableApplicationContext applicationContext;

        protected int resolvePort() {
            val url = casProperties.getAuthn().getAdaptive().getIpIntel().getBlackDot().getUrl();
            return URI.create(SpringExpressionLanguageValueResolver.getInstance().resolve(url)).getPort();
        }
    }

    @Nested
    class TooManyRequestsTests extends BaseTests {
        @Test
        void verifyOperation() throws Throwable {
            val port = resolvePort();
            try (val webServer = new MockWebServer(port, HttpStatus.TOO_MANY_REQUESTS)) {
                webServer.start();
                val requestContext = MockRequestContext.create(applicationContext);
                val result = ipAddressIntelligenceService.examine(requestContext, "37.58.59.181");
                assertNotNull(result);
                assertTrue(result.isBanned());
            }
        }
    }

    @Nested
    class ErrorStatusTests extends BaseTests {
        @Test
        void verifyOperation() throws Throwable {
            val port = resolvePort();
            try (val webServer = new MockWebServer(port, Map.of("status", "error"))) {
                webServer.start();
                val requestContext = MockRequestContext.create(applicationContext);
                val result = ipAddressIntelligenceService.examine(requestContext, "37.58.59.181");
                assertNotNull(result);
                assertTrue(result.isBanned());
            }
        }
    }

    @Nested
    class BannedByRankTests extends BaseTests {
        @Test
        void verifyOperation() throws Throwable {
            val port = resolvePort();
            try (val webServer = new MockWebServer(port, CollectionUtils.wrap("status", "success", "result", 1))) {
                webServer.start();
                val requestContext = MockRequestContext.create(applicationContext);
                val result = ipAddressIntelligenceService.examine(requestContext, "37.58.59.181");
                assertNotNull(result);
                assertTrue(result.isBanned());
            }
        }
    }

    @Nested
    class SuccessTests extends BaseTests {
        @Test
        void verifyOperation() throws Throwable {
            val port = resolvePort();
            try (val webServer = new MockWebServer(port, CollectionUtils.wrap("status", "success", "result", 0))) {
                webServer.start();
                val requestContext = MockRequestContext.create(applicationContext);
                val result = ipAddressIntelligenceService.examine(requestContext, "37.58.59.181");
                assertNotNull(result);
                assertTrue(result.isAllowed());
            }
        }
    }

    @Nested
    class SuccessByRankTests extends BaseTests {
        @Test
        void verifyOperation() throws Throwable {
            val port = resolvePort();
            try (val webServer = new MockWebServer(port, CollectionUtils.wrap("status", "success", "result", 0.4351))) {
                webServer.start();
                val requestContext = MockRequestContext.create(applicationContext);
                val result = ipAddressIntelligenceService.examine(requestContext, "37.58.59.181");
                assertNotNull(result);
                assertTrue(result.isRanked());
            }
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.adaptive.ip-intel.black-dot.mode=DYNA_CHECK")
    class BadResponseTests extends BaseTests {
        @Test
        void verifyOperation() throws Throwable {
            val port = resolvePort();
            try (val webServer = new MockWebServer(port, "${bad-json$")) {
                webServer.start();
                val requestContext = MockRequestContext.create(applicationContext);
                val result = ipAddressIntelligenceService.examine(requestContext, "37.58.59.181");
                assertNotNull(result);
                assertTrue(result.isBanned());
            }
        }
    }
}
