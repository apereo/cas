package org.apereo.cas.authentication.mfa;

import org.apereo.cas.authentication.DefaultMultifactorAuthenticationProviderBypass;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
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
 * This is {@link DefaultRequestedAuthenticationContextValidatorTests}.
 *
 * @author Travis Schmidt
 * @since 6.0.4
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
        val validator = MultifactorAuthenticationTestUtils.mockRequestAuthnContextValidator(applicationContext, Optional.empty());
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
        val validator = MultifactorAuthenticationTestUtils.mockRequestAuthnContextValidator(applicationContext, Optional.of("mfa-dummy"));
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
        val validator = MultifactorAuthenticationTestUtils.mockRequestAuthnContextValidator(applicationContext, Optional.of("mfa-dummy"));
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
        val validator = MultifactorAuthenticationTestUtils.mockRequestAuthnContextValidator(applicationContext, Optional.of("mfa-dummy"));
        val assertion = mock(Assertion.class);
        val principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser", CollectionUtils.wrap("givenName", "CAS"));
        val auth = MultifactorAuthenticationTestUtils.getAuthentication(principal);
        when(assertion.getPrimaryAuthentication())
                .thenReturn(auth);
        val result = validator.validateAuthenticationContext(assertion, mock(HttpServletRequest.class));
        assertNull(result);
    }
}
