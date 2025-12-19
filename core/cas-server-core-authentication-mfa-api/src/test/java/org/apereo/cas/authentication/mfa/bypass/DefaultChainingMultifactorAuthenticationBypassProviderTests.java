package org.apereo.cas.authentication.mfa.bypass;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.bypass.DefaultChainingMultifactorAuthenticationBypassProvider;
import org.apereo.cas.authentication.bypass.HttpRequestMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.NeverAllowMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.mfa.MultifactorAuthenticationTestUtils;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultChainingMultifactorAuthenticationBypassProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("MFATrigger")
class DefaultChainingMultifactorAuthenticationBypassProviderTests {

    private static void mockRememberBypass(final TestMultifactorAuthenticationProvider provider, final Authentication authentication) {
        val authnAttributes = new HashMap<String, List<Object>>();
        authnAttributes.put(MultifactorAuthenticationProviderBypassEvaluator.AUTHENTICATION_ATTRIBUTE_BYPASS_MFA, List.of(Boolean.TRUE));
        authnAttributes.put(MultifactorAuthenticationProviderBypassEvaluator.AUTHENTICATION_ATTRIBUTE_BYPASS_MFA_PROVIDER, List.of(provider.getId()));
        when(authentication.getAttributes()).thenReturn(authnAttributes);
    }

    @Test
    void verifyChain() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        val provider = new DefaultChainingMultifactorAuthenticationBypassProvider(applicationContext);
        provider.addMultifactorAuthenticationProviderBypassEvaluator(
            new MultifactorAuthenticationProviderBypassEvaluator[]{new NeverAllowMultifactorAuthenticationProviderBypassEvaluator(applicationContext)});
        assertFalse(provider.isEmpty());
    }

    @Test
    void verifyEmptyChainOperation() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val p = new DefaultChainingMultifactorAuthenticationBypassProvider(applicationContext);
        val res = p.filterMultifactorAuthenticationProviderBypassEvaluatorsBy("unknown");

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser");
        val authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal);
        val service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertTrue(res.shouldMultifactorAuthenticationProviderExecute(authentication, service,
            provider, new MockHttpServletRequest(), MultifactorAuthenticationTestUtils.getService(service.getServiceId())));
    }

    @Test
    void verifyOperation() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val request = new MockHttpServletRequest();
        request.addHeader("headerbypass", "true");
        val props = new MultifactorAuthenticationProviderBypassProperties();
        props.setHttpRequestHeaders("headerbypass");

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser");
        val authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal);

        val p = new DefaultChainingMultifactorAuthenticationBypassProvider(applicationContext);
        p.addMultifactorAuthenticationProviderBypassEvaluator(
            new HttpRequestMultifactorAuthenticationProviderBypassEvaluator(props, provider.getId(), applicationContext));
        assertFalse(p.isEmpty());
        assertNotNull(p.getId());
        assertNotNull(p.getProviderId());
        assertEquals(1, p.size());

        assertFalse(p.isMultifactorAuthenticationBypassed(authentication, provider.getId()));
        p.rememberBypass(authentication, provider);
        mockRememberBypass(provider, authentication);
        assertTrue(p.isMultifactorAuthenticationBypassed(authentication, provider.getId()));
        when(authentication.getAttributes()).thenReturn(new HashMap<>());
        p.forgetBypass(authentication);
        assertFalse(p.isMultifactorAuthenticationBypassed(authentication, provider.getId()));

        val service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertFalse(p.shouldMultifactorAuthenticationProviderExecute(authentication, service,
            provider, request, MultifactorAuthenticationTestUtils.getService(service.getServiceId())));

        assertTrue(p.belongsToMultifactorAuthenticationProvider(provider.getId()).isPresent());
        assertFalse(p.filterMultifactorAuthenticationProviderBypassEvaluatorsBy(provider.getId()).isEmpty());
    }
}
