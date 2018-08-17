package org.apereo.cas.authentication.mfa;

import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link TestMultifactorAuthenticationProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class TestMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {
    /**
     * Provider id.
     */
    public static final String ID = "mfa-dummy";

    private static final long serialVersionUID = -9184556172646207560L;

    /**
     * Register provider into application context.
     *
     * @param applicationContext the application context
     * @return the multifactor authentication provider
     */
    public static MultifactorAuthenticationProvider registerProviderIntoApplicationContext(final ConfigurableApplicationContext applicationContext) {
        return ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext,
            TestMultifactorAuthenticationProvider.class, "provider" + System.currentTimeMillis());
    }

    @Override
    public String getFriendlyName() {
        return getClass().getSimpleName();
    }

    @Override
    public String getId() {
        return ID;
    }
}
