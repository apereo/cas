package org.apereo.cas.authentication;

import org.apereo.cas.authentication.mfa.MultifactorAuthenticationTestUtils;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.category.RestfulApiCategory;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.util.MockWebServer;

import lombok.val;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * This is {@link RestMultifactorAuthenticationProviderBypassTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Category(RestfulApiCategory.class)
public class RestMultifactorAuthenticationProviderBypassTests {
    @Test
    public void verifyOperationShouldProceed() {
        try (val webServer = new MockWebServer(9316,
            new ByteArrayResource("Y".getBytes(StandardCharsets.UTF_8), "REST Output"), HttpStatus.ACCEPTED)) {
            webServer.start();

            val props = new MultifactorAuthenticationProviderBypassProperties();
            props.getRest().setUrl("http://localhost:9316");
            val r = new RestMultifactorAuthenticationProviderBypass(props);
            val res = r.shouldMultifactorAuthenticationProviderExecute(MultifactorAuthenticationTestUtils.getAuthentication("casuser"),
                MultifactorAuthenticationTestUtils.getRegisteredService(), new TestMultifactorAuthenticationProvider(),
                new MockHttpServletRequest());
            assertTrue(res);
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
