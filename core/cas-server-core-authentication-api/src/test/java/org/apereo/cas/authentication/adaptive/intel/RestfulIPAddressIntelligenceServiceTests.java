package org.apereo.cas.authentication.adaptive.intel;

import org.apereo.cas.configuration.model.core.authentication.AdaptiveAuthenticationProperties;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.MockWebServer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestfulIPAddressIntelligenceServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("RestfulApi")
class RestfulIPAddressIntelligenceServiceTests {
    @Test
    void verifyAllowedOperation() throws Throwable {
        try (val webServer = new MockWebServer(HttpStatus.OK)) {
            webServer.start();

            val props = new AdaptiveAuthenticationProperties();
            props.getIpIntel().getRest().setUrl("http://localhost:%s".formatted(webServer.getPort()));
            val service = new RestfulIPAddressIntelligenceService(props);
            val result = service.examine(new MockRequestContext(), "1.2.3.4");
            assertNotNull(result);
            assertTrue(result.isAllowed());
        }
    }

    @Test
    void verifyBannedOperation() throws Throwable {
        try (val webServer = new MockWebServer(HttpStatus.FORBIDDEN)) {
            webServer.start();
            val props = new AdaptiveAuthenticationProperties();
            props.getIpIntel().getRest().setUrl("http://localhost:%s".formatted(webServer.getPort()));
            val service = new RestfulIPAddressIntelligenceService(props);
            val result = service.examine(new MockRequestContext(), "1.2.3.4");
            assertNotNull(result);
            assertTrue(result.isBanned());
        }
    }

    @Test
    void verifyRankedOperation() throws Throwable {
        try (val webServer = new MockWebServer(new ByteArrayResource("12.435".getBytes(StandardCharsets.UTF_8)), HttpStatus.PRECONDITION_REQUIRED)) {
            webServer.start();
            val props = new AdaptiveAuthenticationProperties();
            props.getIpIntel().getRest().setUrl("http://localhost:%s".formatted(webServer.getPort()));
            var service = new RestfulIPAddressIntelligenceService(props);
            var result = service.examine(new MockRequestContext(), "1.2.3.4");
            assertNotNull(result);
            assertTrue(result.isRanked());

            props.getPolicy().setRejectIpAddresses("123\\..*");
            service = new RestfulIPAddressIntelligenceService(props);
            result = service.examine(new MockRequestContext(), "123.1.2.3");
            assertNotNull(result);
            assertTrue(result.isBanned());
        }
    }
}
