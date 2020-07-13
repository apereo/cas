package org.apereo.cas.authentication.mfa;

import org.apereo.cas.authentication.DefaultMultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.bypass.AuthenticationMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicyFailureModes;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.validation.Assertion;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultMultifactorAuthenticationContextValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.5
 */
@Tag("MFA")
public class DefaultRequestedAuthenticationContextValidatorTests {

    private static final String CASUSER = "casuser";
    private static final Map<String, List<Object>> AUTH_ATTRIBUTES = CollectionUtils.wrap("givenName", "CAS");


    @Test
    public void verifyNoRequestedAuthenticationContext() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        val validator = MultifactorAuthenticationTestUtils.mockRequestAuthnContextValidator(Optional.empty(),
            applicationContext, RegisteredServiceMultifactorPolicyFailureModes.UNDEFINED.toString());
        val assertion = mock(Assertion.class);
        val auth = MultifactorAuthenticationTestUtils.getAuthentication(CASUSER);
        when(assertion.getPrimaryAuthentication()).thenReturn(auth);
        val result = validator.validateAuthenticationContext(assertion, new MockHttpServletRequest());
        assertTrue(result.getKey());
    }

    @Test
    public void verifyRequestedAuthenticationContextBypassed() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val provider = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(TestMultifactorAuthenticationProvider.ID, applicationContext);
        val props = MultifactorAuthenticationTestUtils.getAuthenticationBypassProperties();
        val bypass = new AuthenticationMultifactorAuthenticationProviderBypassEvaluator(props, provider.get().getId());
        ((TestMultifactorAuthenticationProvider) provider.get()).setBypassEvaluator(bypass);
        val validator = MultifactorAuthenticationTestUtils.mockRequestAuthnContextValidator(Optional.of(TestMultifactorAuthenticationProvider.ID),
            applicationContext, RegisteredServiceMultifactorPolicyFailureModes.UNDEFINED.toString());
        val assertion = mock(Assertion.class);
        val principal = MultifactorAuthenticationTestUtils.getPrincipal(CASUSER, CollectionUtils.wrap(CASUSER, AUTH_ATTRIBUTES));
        val auth = MultifactorAuthenticationTestUtils.getAuthentication(principal, AUTH_ATTRIBUTES);
        when(assertion.getPrimaryAuthentication()).thenReturn(auth);
        val result = validator.validateAuthenticationContext(assertion, new MockHttpServletRequest());
        assertTrue(result.getKey());
    }

    @Test
    public void verifyRequestedAuthenticationContextNotBypassed() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val provider = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(TestMultifactorAuthenticationProvider.ID, applicationContext);
        val props = new MultifactorAuthenticationProviderBypassProperties();
        props.setAuthenticationAttributeName("givenName");
        props.setAuthenticationAttributeValue("Not Bypassed");
        val bypass = new AuthenticationMultifactorAuthenticationProviderBypassEvaluator(props, TestMultifactorAuthenticationProvider.ID);
        ((TestMultifactorAuthenticationProvider) provider.get()).setBypassEvaluator(bypass);
        val validator = MultifactorAuthenticationTestUtils.mockRequestAuthnContextValidator(Optional.of(TestMultifactorAuthenticationProvider.ID),
            applicationContext, RegisteredServiceMultifactorPolicyFailureModes.UNDEFINED.toString());
        val assertion = mock(Assertion.class);
        val principal = MultifactorAuthenticationTestUtils.getPrincipal(CASUSER);
        val auth = MultifactorAuthenticationTestUtils.getAuthentication(principal, AUTH_ATTRIBUTES);
        when(assertion.getPrimaryAuthentication())
            .thenReturn(auth);
        val result = validator.validateAuthenticationContext(assertion, new MockHttpServletRequest());
        assertNull(result);
    }

    @Test
    public void verifyRequestedAuthenticationContextNoProvider() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        val validator = MultifactorAuthenticationTestUtils
            .mockRequestAuthnContextValidator(Optional.of(TestMultifactorAuthenticationProvider.ID),
                applicationContext, RegisteredServiceMultifactorPolicyFailureModes.UNDEFINED.toString());
        val assertion = mock(Assertion.class);
        val principal = MultifactorAuthenticationTestUtils.getPrincipal(CASUSER);
        val auth = MultifactorAuthenticationTestUtils.getAuthentication(principal, AUTH_ATTRIBUTES);
        when(assertion.getPrimaryAuthentication()).thenReturn(auth);
        val result = validator.validateAuthenticationContext(assertion, new MockHttpServletRequest());
        assertNull(result);
    }

    @Test
    public void verifyGlobalFailureModeFailsOpen() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        TestUnavailableMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val provider = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(TestUnavailableMultifactorAuthenticationProvider.ID, applicationContext);
        val casProperties = new CasConfigurationProperties();
        casProperties.getAuthn().getMfa().setGlobalFailureMode(RegisteredServiceMultifactorPolicyFailureModes.OPEN.toString());
        val failureEvaluator = new DefaultMultifactorAuthenticationFailureModeEvaluator(casProperties);
        ((TestUnavailableMultifactorAuthenticationProvider) provider.get()).setFailureModeEvaluator(failureEvaluator);
        val validator = MultifactorAuthenticationTestUtils
            .mockRequestAuthnContextValidator(Optional.of(TestUnavailableMultifactorAuthenticationProvider.ID),
                applicationContext, RegisteredServiceMultifactorPolicyFailureModes.UNDEFINED.toString());
        val assertion = mock(Assertion.class);
        val principal = MultifactorAuthenticationTestUtils.getPrincipal(CASUSER);
        val auth = MultifactorAuthenticationTestUtils.getAuthentication(principal, AUTH_ATTRIBUTES);
        when(assertion.getPrimaryAuthentication()).thenReturn(auth);
        val result = validator.validateAuthenticationContext(assertion, new MockHttpServletRequest());
        assertTrue(result.getKey());
    }

    @Test
    public void verifyGlobalFailureModeFailsClosed() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        TestUnavailableMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val provider = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(TestUnavailableMultifactorAuthenticationProvider.ID, applicationContext);
        val casProperties = new CasConfigurationProperties();
        casProperties.getAuthn().getMfa().setGlobalFailureMode(RegisteredServiceMultifactorPolicyFailureModes.CLOSED.toString());
        val failureEvaluator = new DefaultMultifactorAuthenticationFailureModeEvaluator(casProperties);
        ((TestUnavailableMultifactorAuthenticationProvider) provider.get()).setFailureModeEvaluator(failureEvaluator);
        val validator = MultifactorAuthenticationTestUtils
            .mockRequestAuthnContextValidator(Optional.of(TestUnavailableMultifactorAuthenticationProvider.ID),
                applicationContext, RegisteredServiceMultifactorPolicyFailureModes.UNDEFINED.toString());
        val assertion = mock(Assertion.class);
        val principal = MultifactorAuthenticationTestUtils.getPrincipal(CASUSER);
        val auth = MultifactorAuthenticationTestUtils.getAuthentication(principal, AUTH_ATTRIBUTES);
        when(assertion.getPrimaryAuthentication()).thenReturn(auth);
        val result = validator.validateAuthenticationContext(assertion, new MockHttpServletRequest());
        assertNull(result);
    }

    @Test
    public void verifyServiceFailureModeFailsOpen() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        TestUnavailableMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val provider = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(TestUnavailableMultifactorAuthenticationProvider.ID, applicationContext);
        val casProperties = new CasConfigurationProperties();
        casProperties.getAuthn().getMfa().setGlobalFailureMode(RegisteredServiceMultifactorPolicyFailureModes.CLOSED.toString());
        val failureEvaluator = new DefaultMultifactorAuthenticationFailureModeEvaluator(casProperties);
        ((TestUnavailableMultifactorAuthenticationProvider) provider.get()).setFailureModeEvaluator(failureEvaluator);
        val validator = MultifactorAuthenticationTestUtils
            .mockRequestAuthnContextValidator(Optional.of(TestUnavailableMultifactorAuthenticationProvider.ID),
                applicationContext, RegisteredServiceMultifactorPolicyFailureModes.OPEN.toString());
        val assertion = mock(Assertion.class);
        val service = MultifactorAuthenticationTestUtils.getService("service");
        when(assertion.getService()).thenReturn(service);
        val principal = MultifactorAuthenticationTestUtils.getPrincipal(CASUSER);
        val auth = MultifactorAuthenticationTestUtils.getAuthentication(principal, AUTH_ATTRIBUTES);
        when(assertion.getPrimaryAuthentication()).thenReturn(auth);
        val result = validator.validateAuthenticationContext(assertion, new MockHttpServletRequest());
        assertTrue(result.getKey());
    }

    @Test
    public void verifyServiceFailureModeFailsClosed() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        TestUnavailableMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val provider = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(TestUnavailableMultifactorAuthenticationProvider.ID, applicationContext);
        val casProperties = new CasConfigurationProperties();
        casProperties.getAuthn().getMfa().setGlobalFailureMode(RegisteredServiceMultifactorPolicyFailureModes.OPEN.toString());
        val failureEvaluator = new DefaultMultifactorAuthenticationFailureModeEvaluator(casProperties);
        ((TestUnavailableMultifactorAuthenticationProvider) provider.get()).setFailureModeEvaluator(failureEvaluator);
        val validator = MultifactorAuthenticationTestUtils
            .mockRequestAuthnContextValidator(Optional.of(TestUnavailableMultifactorAuthenticationProvider.ID), applicationContext,
                RegisteredServiceMultifactorPolicyFailureModes.CLOSED.toString());
        val assertion = mock(Assertion.class);
        val service = MultifactorAuthenticationTestUtils.getService("service");
        when(assertion.getService()).thenReturn(service);
        val principal = MultifactorAuthenticationTestUtils.getPrincipal(CASUSER);
        val auth = MultifactorAuthenticationTestUtils.getAuthentication(principal, AUTH_ATTRIBUTES);
        when(assertion.getPrimaryAuthentication()).thenReturn(auth);
        val result = validator.validateAuthenticationContext(assertion, new MockHttpServletRequest());
        assertNull(result);
    }
}
