package org.apereo.cas.authentication.adaptive.intel;

import org.apereo.cas.category.RestfulApiCategory;
import org.apereo.cas.configuration.model.core.authentication.AdaptiveAuthenticationProperties;
import org.apereo.cas.util.MockWebServer;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.test.MockRequestContext;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestfulIPAddressIntelligenceServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Category(RestfulApiCategory.class)
public class RestfulIPAddressIntelligenceServiceTests {
    @Test
    public void verifyAllowedOperation() {
        try (val webServer = new MockWebServer(9300,
            new ByteArrayResource(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.OK)) {
            webServer.start();

            val props = new AdaptiveAuthenticationProperties();
            props.getIpIntel().getRest().setUrl("http://localhost:9300");
            val service = new RestfulIPAddressIntelligenceService(props);
            val result = service.examine(new MockRequestContext(), "1.2.3.4");
            assertNotNull(result);
            assertTrue(result.isAllowed());
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }

    @Test
    public void verifyBannedOperation() {
        try (val webServer = new MockWebServer(9304,
            new ByteArrayResource(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.FORBIDDEN)) {
            webServer.start();
            val props = new AdaptiveAuthenticationProperties();
            props.getIpIntel().getRest().setUrl("http://localhost:9304");
            val service = new RestfulIPAddressIntelligenceService(props);
            val result = service.examine(new MockRequestContext(), "1.2.3.4");
            assertNotNull(result);
            assertTrue(result.isBanned());
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }

    }
}
