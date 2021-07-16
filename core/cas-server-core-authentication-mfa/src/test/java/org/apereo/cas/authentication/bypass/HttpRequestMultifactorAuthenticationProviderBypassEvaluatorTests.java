package org.apereo.cas.authentication.bypass;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link HttpRequestMultifactorAuthenticationProviderBypassEvaluatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("MFATrigger")
public class HttpRequestMultifactorAuthenticationProviderBypassEvaluatorTests {

    @Test
    public void verifyShouldProceed() {
        val properties = new MultifactorAuthenticationProviderBypassProperties();

        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val eval = new HttpRequestMultifactorAuthenticationProviderBypassEvaluator(properties, provider.getId());

        val principal = CoreAuthenticationTestUtils.getPrincipal(Map.of("cn", List.of("example")));
        val authentication = CoreAuthenticationTestUtils.getAuthentication(principal);
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();

        val request = new MockHttpServletRequest();
        assertTrue(eval.shouldMultifactorAuthenticationProviderExecute(authentication, registeredService, provider, request));

    }

}
