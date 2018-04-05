package org.apereo.cas.authentication.mfa;

import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationContextValidator;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationContextValidator;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.util.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.junit.Assert.*;

/**
 * This is {@link DefaultMultifactorAuthenticationContextValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class
})
@DirtiesContext
public class DefaultMultifactorAuthenticationContextValidatorTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    public void verifyContextFailsValidationWithNoProviders() {
        final AuthenticationContextValidator v = new DefaultMultifactorAuthenticationContextValidator("authn_method",
            "OPEN", "trusted_authn", applicationContext);
        final Pair<Boolean, Optional<MultifactorAuthenticationProvider>> result = v.validate(
            MultifactorAuthenticationTestUtils.getAuthentication("casuser"),
            "invalid-context", MultifactorAuthenticationTestUtils.getRegisteredService());
        assertFalse(result.getKey());
    }

    @Test
    public void verifyContextFailsValidationWithMissingProvider() {
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        final AuthenticationContextValidator v = new DefaultMultifactorAuthenticationContextValidator("authn_method",
            "OPEN", "trusted_authn", applicationContext);
        final Pair<Boolean, Optional<MultifactorAuthenticationProvider>> result = v.validate(
            MultifactorAuthenticationTestUtils.getAuthentication("casuser"),
            "invalid-context",
            MultifactorAuthenticationTestUtils.getRegisteredService());
        assertFalse(result.getKey());
    }

    @Test
    public void verifyContextPassesValidationWithProvider() {
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        final AuthenticationContextValidator v = new DefaultMultifactorAuthenticationContextValidator("authn_method",
            "OPEN", "trusted_authn", applicationContext);
        final Authentication authentication = MultifactorAuthenticationTestUtils.getAuthentication(
            MultifactorAuthenticationTestUtils.getPrincipal("casuser"),
            CollectionUtils.wrap("authn_method", "mfa-dummy"));
        final Pair<Boolean, Optional<MultifactorAuthenticationProvider>> result = v.validate(authentication,
            "mfa-dummy", MultifactorAuthenticationTestUtils.getRegisteredService());
        assertTrue(result.getKey());
    }
}
