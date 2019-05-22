package org.apereo.cas.authentication.mfa;

import org.apereo.cas.authentication.DefaultMultifactorAuthenticationFailureMode;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationProviderBypass;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.validation.Assertion;

import lombok.val;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultMultifactorAuthenticationContextValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.5
 */
@DirtiesContext
public class DefaultRequestedAuthenticationContextValidatorTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    public static final String CASUSER = "casuser";
    public static final Map<String, Object> PRINCIPAL = CollectionUtils.wrap("givenName", "CAS");

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    public void verifyNoRequestedAuthenticationContext() {
        val validator = MultifactorAuthenticationTestUtils
                .mockRequestAuthnContextValidator(Optional.empty(),
                        applicationContext, RegisteredServiceMultifactorPolicy.FailureModes.UNDEFINED.toString());
        val assertion = mock(Assertion.class);
        val auth = MultifactorAuthenticationTestUtils.getAuthentication(CASUSER);
        when(assertion.getPrimaryAuthentication())
                .thenReturn(auth);
        val result = validator.validateAuthenticationContext(assertion, mock(HttpServletRequest.class));
        assertTrue(result.getKey());
    }

    @Test
    public void verifyRequestedAuthenticationContextBypassed() {
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val provider = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(TestMultifactorAuthenticationProvider.ID, applicationContext);
        val props = MultifactorAuthenticationTestUtils.getBypassProperties();
        val bypass = new DefaultMultifactorAuthenticationProviderBypass(props);
        ((TestMultifactorAuthenticationProvider) provider.get()).setBypassEvaluator(bypass);
        val validator = MultifactorAuthenticationTestUtils
                .mockRequestAuthnContextValidator(Optional.of(TestMultifactorAuthenticationProvider.ID),
                        applicationContext, RegisteredServiceMultifactorPolicy.FailureModes.UNDEFINED.toString());
        val assertion = mock(Assertion.class);
        val principal = MultifactorAuthenticationTestUtils.getPrincipal(CASUSER, PRINCIPAL);
        val auth = MultifactorAuthenticationTestUtils.getAuthentication(principal);
        when(assertion.getPrimaryAuthentication())
                .thenReturn(auth);
        val result = validator.validateAuthenticationContext(assertion, mock(HttpServletRequest.class));
        assertTrue(result.getKey());
    }

    @Test
    public void verifyRequestedAuthenticationContextNotBypassed() {
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val provider = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(TestMultifactorAuthenticationProvider.ID, applicationContext);
        val props = new MultifactorAuthenticationProviderBypassProperties();
        props.setPrincipalAttributeName("givenName");
        props.setPrincipalAttributeValue("Not Bypassed");
        val bypass = new DefaultMultifactorAuthenticationProviderBypass(props);
        ((TestMultifactorAuthenticationProvider) provider.get()).setBypassEvaluator(bypass);
        val validator = MultifactorAuthenticationTestUtils
                .mockRequestAuthnContextValidator(Optional.of(TestMultifactorAuthenticationProvider.ID),
                        applicationContext, RegisteredServiceMultifactorPolicy.FailureModes.UNDEFINED.toString());
        val assertion = mock(Assertion.class);
        val principal = MultifactorAuthenticationTestUtils.getPrincipal(CASUSER, PRINCIPAL);
        val auth = MultifactorAuthenticationTestUtils.getAuthentication(principal);
        when(assertion.getPrimaryAuthentication())
                .thenReturn(auth);
        val result = validator.validateAuthenticationContext(assertion, mock(HttpServletRequest.class));
        assertNull(result);
    }

    @Test
    public void verifyRequestedAuthenticationContextNoProvider() {
        val validator = MultifactorAuthenticationTestUtils
                .mockRequestAuthnContextValidator(Optional.of(TestMultifactorAuthenticationProvider.ID),
                        applicationContext, RegisteredServiceMultifactorPolicy.FailureModes.UNDEFINED.toString());
        val assertion = mock(Assertion.class);
        val principal = MultifactorAuthenticationTestUtils.getPrincipal(CASUSER, PRINCIPAL);
        val auth = MultifactorAuthenticationTestUtils.getAuthentication(principal);
        when(assertion.getPrimaryAuthentication())
                .thenReturn(auth);
        val result = validator.validateAuthenticationContext(assertion, mock(HttpServletRequest.class));
        assertNull(result);
    }

    @Test
    public void verifyGlobalFailureModeFailsOpen() {
        TestUnavailableMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val provider = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(TestUnavailableMultifactorAuthenticationProvider.ID, applicationContext);
        val props = MultifactorAuthenticationTestUtils.getBypassProperties();
        val casProperties = new CasConfigurationProperties();
        casProperties.getAuthn().getMfa().setGlobalFailureMode(RegisteredServiceMultifactorPolicy.FailureModes.OPEN.toString());
        val failureEvaluator = new DefaultMultifactorAuthenticationFailureMode(casProperties);
        ((TestUnavailableMultifactorAuthenticationProvider) provider.get()).setFailureModeEvaluator(failureEvaluator);
        val validator = MultifactorAuthenticationTestUtils
                .mockRequestAuthnContextValidator(Optional.of(TestUnavailableMultifactorAuthenticationProvider.ID),
                        applicationContext, RegisteredServiceMultifactorPolicy.FailureModes.UNDEFINED.toString());
        val assertion = mock(Assertion.class);
        val principal = MultifactorAuthenticationTestUtils.getPrincipal(CASUSER, PRINCIPAL);
        val auth = MultifactorAuthenticationTestUtils.getAuthentication(principal);
        when(assertion.getPrimaryAuthentication())
                .thenReturn(auth);
        val result = validator.validateAuthenticationContext(assertion, mock(HttpServletRequest.class));
        assertTrue(result.getKey());
    }

    @Test
    public void verifyGlobalFailureModeFailsClosed() {
        TestUnavailableMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val provider = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(TestUnavailableMultifactorAuthenticationProvider.ID, applicationContext);
        val props = MultifactorAuthenticationTestUtils.getBypassProperties();
        val casProperties = new CasConfigurationProperties();
        casProperties.getAuthn().getMfa().setGlobalFailureMode(RegisteredServiceMultifactorPolicy.FailureModes.CLOSED.toString());
        val failureEvaluator = new DefaultMultifactorAuthenticationFailureMode(casProperties);
        ((TestUnavailableMultifactorAuthenticationProvider) provider.get()).setFailureModeEvaluator(failureEvaluator);
        val validator = MultifactorAuthenticationTestUtils
                .mockRequestAuthnContextValidator(Optional.of(TestUnavailableMultifactorAuthenticationProvider.ID),
                        applicationContext, RegisteredServiceMultifactorPolicy.FailureModes.UNDEFINED.toString());
        val assertion = mock(Assertion.class);
        val principal = MultifactorAuthenticationTestUtils.getPrincipal(CASUSER, PRINCIPAL);
        val auth = MultifactorAuthenticationTestUtils.getAuthentication(principal);
        when(assertion.getPrimaryAuthentication())
                .thenReturn(auth);
        val result = validator.validateAuthenticationContext(assertion, mock(HttpServletRequest.class));
        assertNull(result);
    }

    @Test
    public void verifyServiceFailureModeFailsOpen() {
        TestUnavailableMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val provider = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(TestUnavailableMultifactorAuthenticationProvider.ID, applicationContext);
        val props = MultifactorAuthenticationTestUtils.getBypassProperties();
        val casProperties = new CasConfigurationProperties();
        casProperties.getAuthn().getMfa().setGlobalFailureMode(RegisteredServiceMultifactorPolicy.FailureModes.CLOSED.toString());
        val failureEvaluator = new DefaultMultifactorAuthenticationFailureMode(casProperties);
        ((TestUnavailableMultifactorAuthenticationProvider) provider.get()).setFailureModeEvaluator(failureEvaluator);
        val validator = MultifactorAuthenticationTestUtils
                .mockRequestAuthnContextValidator(Optional.of(TestUnavailableMultifactorAuthenticationProvider.ID),
                        applicationContext, RegisteredServiceMultifactorPolicy.FailureModes.OPEN.toString());
        val assertion = mock(Assertion.class);
        val service = MultifactorAuthenticationTestUtils.getService("service");
        when(assertion.getService()).thenReturn(service);
        val principal = MultifactorAuthenticationTestUtils.getPrincipal(CASUSER, PRINCIPAL);
        val auth = MultifactorAuthenticationTestUtils.getAuthentication(principal);
        when(assertion.getPrimaryAuthentication())
                .thenReturn(auth);
        val result = validator.validateAuthenticationContext(assertion, mock(HttpServletRequest.class));
        assertTrue(result.getKey());
    }

    @Test
    public void verifyServiceFailureModeFailsClosed() {
        TestUnavailableMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val provider = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(TestUnavailableMultifactorAuthenticationProvider.ID, applicationContext);
        val props = MultifactorAuthenticationTestUtils.getBypassProperties();
        val casProperties = new CasConfigurationProperties();
        casProperties.getAuthn().getMfa().setGlobalFailureMode(RegisteredServiceMultifactorPolicy.FailureModes.OPEN.toString());
        val failureEvaluator = new DefaultMultifactorAuthenticationFailureMode(casProperties);
        ((TestUnavailableMultifactorAuthenticationProvider) provider.get()).setFailureModeEvaluator(failureEvaluator);
        val validator = MultifactorAuthenticationTestUtils
                .mockRequestAuthnContextValidator(Optional.of(TestUnavailableMultifactorAuthenticationProvider.ID), applicationContext,
                        RegisteredServiceMultifactorPolicy.FailureModes.CLOSED.toString());
        val assertion = mock(Assertion.class);
        val service = MultifactorAuthenticationTestUtils.getService("service");
        when(assertion.getService()).thenReturn(service);
        val principal = MultifactorAuthenticationTestUtils.getPrincipal(CASUSER, PRINCIPAL);
        val auth = MultifactorAuthenticationTestUtils.getAuthentication(principal);
        when(assertion.getPrimaryAuthentication())
                .thenReturn(auth);
        val result = validator.validateAuthenticationContext(assertion, mock(HttpServletRequest.class));
        assertNull(result);
    }
}
