package org.apereo.cas.authentication.mfa;

import org.apereo.cas.authentication.DefaultMultifactorAuthenticationFailureMode;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationProviderBypass;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
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

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    public void verifyNoRequestedAuthenticationContext() {
        val validator = MultifactorAuthenticationTestUtils
                .mockRequestAuthnContextValidator(Optional.empty(), applicationContext, "UNDEFINED");
        val assertion = mock(Assertion.class);
        val auth = MultifactorAuthenticationTestUtils.getAuthentication("casuser");
        when(assertion.getPrimaryAuthentication())
                .thenReturn(auth);
        val result = validator.validateAuthenticationContext(assertion, mock(HttpServletRequest.class));
        assertTrue(result.getKey());
    }

    @Test
    public void verifyRequestedAuthenticationContextBypassed() {
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val provider = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById("mfa-dummy", applicationContext);
        val props = new MultifactorAuthenticationProviderBypassProperties();
        props.setPrincipalAttributeName("givenName");
        props.setPrincipalAttributeValue("CAS");
        val bypass = new DefaultMultifactorAuthenticationProviderBypass(props);
        ((TestMultifactorAuthenticationProvider) provider.get()).setBypassEvaluator(bypass);
        val validator = MultifactorAuthenticationTestUtils
                .mockRequestAuthnContextValidator(Optional.of("mfa-dummy"), applicationContext, "UNDEFINED");
        val assertion = mock(Assertion.class);
        val principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser", CollectionUtils.wrap("givenName", "CAS"));
        val auth = MultifactorAuthenticationTestUtils.getAuthentication(principal);
        when(assertion.getPrimaryAuthentication())
                .thenReturn(auth);
        val result = validator.validateAuthenticationContext(assertion, mock(HttpServletRequest.class));
        assertTrue(result.getKey());
    }

    @Test
    public void verifyRequestedAuthenticationContextNotBypassed() {
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val provider = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById("mfa-dummy", applicationContext);
        val props = new MultifactorAuthenticationProviderBypassProperties();
        props.setPrincipalAttributeName("givenName");
        props.setPrincipalAttributeValue("Not Bypassed");
        val bypass = new DefaultMultifactorAuthenticationProviderBypass(props);
        ((TestMultifactorAuthenticationProvider) provider.get()).setBypassEvaluator(bypass);
        val validator = MultifactorAuthenticationTestUtils
                .mockRequestAuthnContextValidator(Optional.of("mfa-dummy"), applicationContext, "UNDEFINED");
        val assertion = mock(Assertion.class);
        val principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser", CollectionUtils.wrap("givenName", "CAS"));
        val auth = MultifactorAuthenticationTestUtils.getAuthentication(principal);
        when(assertion.getPrimaryAuthentication())
                .thenReturn(auth);
        val result = validator.validateAuthenticationContext(assertion, mock(HttpServletRequest.class));
        assertNull(result);
    }

    @Test
    public void verifyRequestedAuthenticationContextNoProvider() {
        val validator = MultifactorAuthenticationTestUtils
                .mockRequestAuthnContextValidator(Optional.of("mfa-dummy"), applicationContext, "UNDEFINED");
        val assertion = mock(Assertion.class);
        val principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser", CollectionUtils.wrap("givenName", "CAS"));
        val auth = MultifactorAuthenticationTestUtils.getAuthentication(principal);
        when(assertion.getPrimaryAuthentication())
                .thenReturn(auth);
        val result = validator.validateAuthenticationContext(assertion, mock(HttpServletRequest.class));
        assertNull(result);
    }

    @Test
    public void verifyGlobalFailureModeFailsOpen() {
        TestUnavailableMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val provider = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById("mfa-dummy-unavailable", applicationContext);
        val props = new MultifactorAuthenticationProviderBypassProperties();
        props.setPrincipalAttributeName("givenName");
        props.setPrincipalAttributeValue("CAS");
        val casProperties = new CasConfigurationProperties();
        casProperties.getAuthn().getMfa().setGlobalFailureMode("OPEN");
        val failureEvaluator = new DefaultMultifactorAuthenticationFailureMode(casProperties);
        ((TestUnavailableMultifactorAuthenticationProvider) provider.get()).setFailureModeEvaluator(failureEvaluator);
        val validator = MultifactorAuthenticationTestUtils
                .mockRequestAuthnContextValidator(Optional.of("mfa-dummy-unavailable"), applicationContext, "UNDEFINED");
        val assertion = mock(Assertion.class);
        val principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser", CollectionUtils.wrap("givenName", "CAS"));
        val auth = MultifactorAuthenticationTestUtils.getAuthentication(principal);
        when(assertion.getPrimaryAuthentication())
                .thenReturn(auth);
        val result = validator.validateAuthenticationContext(assertion, mock(HttpServletRequest.class));
        assertTrue(result.getKey());
    }

    @Test
    public void verifyGlobalFailureModeFailsClosed() {
        TestUnavailableMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val provider = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById("mfa-dummy-unavailable", applicationContext);
        val props = new MultifactorAuthenticationProviderBypassProperties();
        props.setPrincipalAttributeName("givenName");
        props.setPrincipalAttributeValue("CAS");
        val casProperties = new CasConfigurationProperties();
        casProperties.getAuthn().getMfa().setGlobalFailureMode("CLOSED");
        val failureEvaluator = new DefaultMultifactorAuthenticationFailureMode(casProperties);
        ((TestUnavailableMultifactorAuthenticationProvider) provider.get()).setFailureModeEvaluator(failureEvaluator);
        val validator = MultifactorAuthenticationTestUtils
                .mockRequestAuthnContextValidator(Optional.of("mfa-dummy-unavailable"), applicationContext, "UNDEFINED");
        val assertion = mock(Assertion.class);
        val principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser", CollectionUtils.wrap("givenName", "CAS"));
        val auth = MultifactorAuthenticationTestUtils.getAuthentication(principal);
        when(assertion.getPrimaryAuthentication())
                .thenReturn(auth);
        val result = validator.validateAuthenticationContext(assertion, mock(HttpServletRequest.class));
        assertNull(result);
    }

    @Test
    public void verifyServiceFailureModeFailsOpen() {
        TestUnavailableMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val provider = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById("mfa-dummy-unavailable", applicationContext);
        val props = new MultifactorAuthenticationProviderBypassProperties();
        props.setPrincipalAttributeName("givenName");
        props.setPrincipalAttributeValue("CAS");
        val casProperties = new CasConfigurationProperties();
        casProperties.getAuthn().getMfa().setGlobalFailureMode("CLOSED");
        val failureEvaluator = new DefaultMultifactorAuthenticationFailureMode(casProperties);
        ((TestUnavailableMultifactorAuthenticationProvider) provider.get()).setFailureModeEvaluator(failureEvaluator);
        val validator = MultifactorAuthenticationTestUtils
                .mockRequestAuthnContextValidator(Optional.of("mfa-dummy-unavailable"), applicationContext, "OPEN");
        val assertion = mock(Assertion.class);
        val service = MultifactorAuthenticationTestUtils.getService("service");
        when(assertion.getService()).thenReturn(service);
        val principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser", CollectionUtils.wrap("givenName", "CAS"));
        val auth = MultifactorAuthenticationTestUtils.getAuthentication(principal);
        when(assertion.getPrimaryAuthentication())
                .thenReturn(auth);
        val result = validator.validateAuthenticationContext(assertion, mock(HttpServletRequest.class));
        assertTrue(result.getKey());
    }

    @Test
    public void verifyServiceFailureModeFailsClosed() {
        TestUnavailableMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val provider = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById("mfa-dummy-unavailable", applicationContext);
        val props = new MultifactorAuthenticationProviderBypassProperties();
        props.setPrincipalAttributeName("givenName");
        props.setPrincipalAttributeValue("CAS");
        val casProperties = new CasConfigurationProperties();
        casProperties.getAuthn().getMfa().setGlobalFailureMode("OPEN");
        val failureEvaluator = new DefaultMultifactorAuthenticationFailureMode(casProperties);
        ((TestUnavailableMultifactorAuthenticationProvider) provider.get()).setFailureModeEvaluator(failureEvaluator);
        val validator = MultifactorAuthenticationTestUtils
                .mockRequestAuthnContextValidator(Optional.of("mfa-dummy-unavailable"), applicationContext, "CLOSED");
        val assertion = mock(Assertion.class);
        val service = MultifactorAuthenticationTestUtils.getService("service");
        when(assertion.getService()).thenReturn(service);
        val principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser", CollectionUtils.wrap("givenName", "CAS"));
        val auth = MultifactorAuthenticationTestUtils.getAuthentication(principal);
        when(assertion.getPrimaryAuthentication())
                .thenReturn(auth);
        val result = validator.validateAuthenticationContext(assertion, mock(HttpServletRequest.class));
        assertNull(result);
    }
}
