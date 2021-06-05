package org.apereo.cas.authentication.mfa;

import org.apereo.cas.authentication.DefaultChainingMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationContextValidator;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultMultifactorAuthenticationContextValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("MFA")
public class DefaultMultifactorAuthenticationContextValidatorTests {
    private static ConfigurableApplicationContext getStaticApplicationContext() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        return applicationContext;
    }

    @Test
    public void verifyContextFailsValidationWithNoProviders() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        val v = new DefaultMultifactorAuthenticationContextValidator("authn_method",
            "trusted_authn", applicationContext);
        val result = v.validate(
            MultifactorAuthenticationTestUtils.getAuthentication("casuser"),
            "invalid-context", Optional.of(MultifactorAuthenticationTestUtils.getRegisteredService()));
        assertFalse(result.isSuccess());
    }

    @Test
    public void verifyContextFailsValidationWithMissingProvider() {
        val applicationContext = getStaticApplicationContext();
        val v = new DefaultMultifactorAuthenticationContextValidator("authn_method",
            "trusted_authn", applicationContext);
        val result = v.validate(
            MultifactorAuthenticationTestUtils.getAuthentication("casuser"),
            "invalid-context",
            Optional.of(MultifactorAuthenticationTestUtils.getRegisteredService()));
        assertFalse(result.isSuccess());
    }

    @Test
    public void verifyContextPassesValidationWithProvider() {
        val applicationContext = getStaticApplicationContext();
        val v = new DefaultMultifactorAuthenticationContextValidator("authn_method",
            "trusted_authn", applicationContext);
        val authentication = MultifactorAuthenticationTestUtils.getAuthentication(
            MultifactorAuthenticationTestUtils.getPrincipal("casuser"),
            CollectionUtils.wrap("authn_method", List.of("mfa-dummy")));
        val result = v.validate(authentication,
            "mfa-dummy", Optional.of(MultifactorAuthenticationTestUtils.getRegisteredService()));
        assertTrue(result.isSuccess());
    }

    @Test
    public void verifyContextPassesValidationWithChainProvider() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val casProperties = new CasConfigurationProperties();
        casProperties.getAuthn().getMfa().getCore()
            .setGlobalFailureMode(BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.OPEN);
        val failureEvaluator = new DefaultMultifactorAuthenticationFailureModeEvaluator(casProperties);
        val chainProvider = new DefaultChainingMultifactorAuthenticationProvider(failureEvaluator);

        val provider1 = new TestMultifactorAuthenticationProvider("mfa-first");
        provider1.setOrder(10);
        val provider2 = new TestMultifactorAuthenticationProvider("mfa-second");
        provider2.setOrder(20);

        chainProvider.addMultifactorAuthenticationProvider(provider1);
        chainProvider.addMultifactorAuthenticationProvider(provider2);
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext, chainProvider);
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext, provider1);
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext, provider2);

        val v = new DefaultMultifactorAuthenticationContextValidator("authn_method",
            "trusted_authn", applicationContext);
        val authentication = MultifactorAuthenticationTestUtils.getAuthentication(
            MultifactorAuthenticationTestUtils.getPrincipal("casuser"),
            CollectionUtils.wrap("authn_method", List.of(provider2.getId())));
        val result = v.validate(authentication, provider2.getId(),
            Optional.of(MultifactorAuthenticationTestUtils.getRegisteredService()));
        assertTrue(result.isSuccess());
    }

    @Test
    public void verifyTrustedAuthnFoundInContext() {
        val applicationContext = getStaticApplicationContext();
        val v = new DefaultMultifactorAuthenticationContextValidator("authn_method",
            "trusted_authn", applicationContext);
        val authentication = MultifactorAuthenticationTestUtils.getAuthentication(
            MultifactorAuthenticationTestUtils.getPrincipal("casuser"),
            CollectionUtils.wrap("authn_method", List.of("mfa-other"), "trusted_authn", List.of("mfa-dummy")));
        val result = v.validate(authentication,
            "mfa-dummy", Optional.of(MultifactorAuthenticationTestUtils.getRegisteredService()));
        assertTrue(result.isSuccess());
    }

    @Test
    public void verifyTrustedAuthnFoundFromContext() {
        val applicationContext = getStaticApplicationContext();
        val v = new DefaultMultifactorAuthenticationContextValidator("authn_method",
            "trusted_authn", applicationContext);
        val authentication = MultifactorAuthenticationTestUtils.getAuthentication(
            MultifactorAuthenticationTestUtils.getPrincipal("casuser"),
            CollectionUtils.wrap("authn_method", List.of("mfa-other")));
        var result = v.validate(authentication,
            "mfa-dummy", Optional.of(MultifactorAuthenticationTestUtils.getRegisteredService()));
        assertFalse(result.isSuccess());

        val otherProvider = new TestMultifactorAuthenticationProvider();
        otherProvider.setId("mfa-other");
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext, otherProvider);
        result = v.validate(authentication,
            "mfa-dummy", Optional.of(MultifactorAuthenticationTestUtils.getRegisteredService()));
        assertTrue(result.isSuccess());
    }
}
