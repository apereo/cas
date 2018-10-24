package org.apereo.cas.authentication.mfa;

import org.apereo.cas.authentication.DefaultMultifactorAuthenticationContextValidator;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderBypass;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.junit.Assert.*;

/**
 * This is {@link DefaultMultifactorAuthenticationContextValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@DirtiesContext
public class DefaultMultifactorAuthenticationContextValidatorTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    public void verifyContextFailsValidationWithNoProviders() {
        val v = new DefaultMultifactorAuthenticationContextValidator("authn_method",
            "OPEN", "trusted_authn", applicationContext);
        val result = v.validate(
            MultifactorAuthenticationTestUtils.getAuthentication("casuser"),
            "invalid-context", MultifactorAuthenticationTestUtils.getRegisteredService());
        assertFalse(result.getKey());
    }

    @Test
    public void verifyContextFailsValidationWithMissingProvider() {
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val v = new DefaultMultifactorAuthenticationContextValidator("authn_method",
            "OPEN", "trusted_authn", applicationContext);
        val result = v.validate(
            MultifactorAuthenticationTestUtils.getAuthentication("casuser"),
            "invalid-context",
            MultifactorAuthenticationTestUtils.getRegisteredService());
        assertFalse(result.getKey());
    }

    @Test
    public void verifyContextPassesValidationWithProvider() {
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val v = new DefaultMultifactorAuthenticationContextValidator("authn_method",
            "OPEN", "trusted_authn", applicationContext);
        val authentication = MultifactorAuthenticationTestUtils.getAuthentication(
            MultifactorAuthenticationTestUtils.getPrincipal("casuser"),
            CollectionUtils.wrap("authn_method", "mfa-dummy"));
        val result = v.validate(authentication,
            "mfa-dummy", MultifactorAuthenticationTestUtils.getRegisteredService());
        assertTrue(result.getKey());
    }

    @Test
    public void verifyTrustedAuthnFoundInContext() {
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val v = new DefaultMultifactorAuthenticationContextValidator("authn_method",
            "OPEN", "trusted_authn", applicationContext);
        val authentication = MultifactorAuthenticationTestUtils.getAuthentication(
            MultifactorAuthenticationTestUtils.getPrincipal("casuser"),
            CollectionUtils.wrap("authn_method", "mfa-other", "trusted_authn", "mfa-dummy"));
        val result = v.validate(authentication,
            "mfa-dummy", MultifactorAuthenticationTestUtils.getRegisteredService());
        assertTrue(result.getKey());
    }

    @Test
    public void verifyBypassAuthnFoundInContext() {
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val v = new DefaultMultifactorAuthenticationContextValidator("authn_method",
            "OPEN", "trusted_authn", applicationContext);
        val authentication = MultifactorAuthenticationTestUtils.getAuthentication(
            MultifactorAuthenticationTestUtils.getPrincipal("casuser"),
            CollectionUtils.wrap("authn_method", "mfa-other",
                MultifactorAuthenticationProviderBypass.AUTHENTICATION_ATTRIBUTE_BYPASS_MFA, true,
                MultifactorAuthenticationProviderBypass.AUTHENTICATION_ATTRIBUTE_BYPASS_MFA_PROVIDER, "mfa-dummy"));
        val result = v.validate(authentication,
            "mfa-dummy", MultifactorAuthenticationTestUtils.getRegisteredService());
        assertTrue(result.getKey());
    }

    @Test
    public void verifyBypassAuthnNotFoundInContext() {
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val v = new DefaultMultifactorAuthenticationContextValidator("authn_method",
            "OPEN", "trusted_authn", applicationContext);
        val authentication = MultifactorAuthenticationTestUtils.getAuthentication(
            MultifactorAuthenticationTestUtils.getPrincipal("casuser"),
            CollectionUtils.wrap("authn_method", "mfa-other",
                MultifactorAuthenticationProviderBypass.AUTHENTICATION_ATTRIBUTE_BYPASS_MFA, true,
                MultifactorAuthenticationProviderBypass.AUTHENTICATION_ATTRIBUTE_BYPASS_MFA_PROVIDER, "mfa-other"));
        val result = v.validate(authentication,
            "mfa-dummy", MultifactorAuthenticationTestUtils.getRegisteredService());
        assertFalse(result.getKey());
    }

}
