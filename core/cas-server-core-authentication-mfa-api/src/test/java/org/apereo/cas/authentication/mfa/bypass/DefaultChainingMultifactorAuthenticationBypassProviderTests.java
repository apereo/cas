package org.apereo.cas.authentication.mfa.bypass;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.bypass.DefaultChainingMultifactorAuthenticationBypassProvider;
import org.apereo.cas.authentication.bypass.HttpRequestMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.mfa.MultifactorAuthenticationTestUtils;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultChainingMultifactorAuthenticationBypassProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = AopAutoConfiguration.class)
@Tag("MFA")
public class DefaultChainingMultifactorAuthenticationBypassProviderTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    public void verifyOperation() {
        val request = new MockHttpServletRequest();
        request.addHeader("headerbypass", "true");
        val props = new MultifactorAuthenticationProviderBypassProperties();
        props.setHttpRequestHeaders("headerbypass");

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser");
        val authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal);

        val p = new DefaultChainingMultifactorAuthenticationBypassProvider();
        p.addMultifactorAuthenticationProviderBypassEvaluator(new HttpRequestMultifactorAuthenticationProviderBypassEvaluator(props, provider.getId()));
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
        assertFalse(p.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));

        assertTrue(p.belongsToMultifactorAuthenticationProvider(provider.getId()).isPresent());
        assertFalse(p.filterMultifactorAuthenticationProviderBypassEvaluatorsBy(provider.getId()).isEmpty());
    }

    private static void mockRememberBypass(final TestMultifactorAuthenticationProvider provider, final Authentication authentication) {
        val authnAttributes = new HashMap<String, List<Object>>();
        authnAttributes.put(MultifactorAuthenticationProviderBypassEvaluator.AUTHENTICATION_ATTRIBUTE_BYPASS_MFA, List.of(Boolean.TRUE));
        authnAttributes.put(MultifactorAuthenticationProviderBypassEvaluator.AUTHENTICATION_ATTRIBUTE_BYPASS_MFA_PROVIDER, List.of(provider.getId()));
        when(authentication.getAttributes()).thenReturn(authnAttributes);
    }
}
