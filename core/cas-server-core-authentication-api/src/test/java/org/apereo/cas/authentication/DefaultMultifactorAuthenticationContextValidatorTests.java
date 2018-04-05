package org.apereo.cas.authentication;

import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.util.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
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
        final Pair<Boolean, Optional<MultifactorAuthenticationProvider>> result = v.validate(CoreAuthenticationTestUtils.getAuthentication(),
            "invalid-context", CoreAuthenticationTestUtils.getRegisteredService());
        assertFalse(result.getKey());
    }

    @Test
    public void verifyContextFailsValidationWithMissingProvider() {
        registerProviderIntoApplicationContext();
        final AuthenticationContextValidator v = new DefaultMultifactorAuthenticationContextValidator("authn_method",
            "OPEN", "trusted_authn", applicationContext);
        final Pair<Boolean, Optional<MultifactorAuthenticationProvider>> result = v.validate(CoreAuthenticationTestUtils.getAuthentication(),
            "invalid-context", CoreAuthenticationTestUtils.getRegisteredService());
        assertFalse(result.getKey());
    }

    @Test
    public void verifyContextPassesValidationWithProvider() {
        registerProviderIntoApplicationContext();
        final AuthenticationContextValidator v = new DefaultMultifactorAuthenticationContextValidator("authn_method",
            "OPEN", "trusted_authn", applicationContext);
        final Authentication authentication = CoreAuthenticationTestUtils.getAuthentication(CoreAuthenticationTestUtils.getPrincipal(),
            CollectionUtils.wrap("authn_method", "mfa-dummy"));
        final Pair<Boolean, Optional<MultifactorAuthenticationProvider>> result = v.validate(authentication,
            "mfa-dummy", CoreAuthenticationTestUtils.getRegisteredService());
        assertTrue(result.getKey());
    }

    private void registerProviderIntoApplicationContext() {
        final ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
        final DummyMultifactorAuthenticationProvider provider = beanFactory.createBean(DummyMultifactorAuthenticationProvider.class);
        beanFactory.initializeBean(provider, "provider" + System.currentTimeMillis());
        beanFactory.autowireBean(provider);
        beanFactory.registerSingleton("provider" + System.currentTimeMillis(), provider);
    }

    private static class DummyMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {
        private static final long serialVersionUID = -9184556172646207560L;

        @Override
        public String getFriendlyName() {
            return getClass().getSimpleName();
        }

        @Override
        public String getId() {
            return "mfa-dummy";
        }
    }
}
