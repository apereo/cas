package org.apereo.cas.authentication.mfa;

import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import org.springframework.context.ConfigurableApplicationContext;

import java.io.Serial;

/**
 * This is {@link TestUnavailableMultifactorAuthenticationProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class TestUnavailableMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {
    /**
     * Provider id.
     */
    public static final String ID = "mfa-dummy-unavailable";

    @Serial
    private static final long serialVersionUID = -9184556172646207560L;

    /**
     * Register provider into application context.
     *
     * @param applicationContext the application context
     * @return the multifactor authentication provider
     */
    public static TestUnavailableMultifactorAuthenticationProvider registerProviderIntoApplicationContext(
        final ConfigurableApplicationContext applicationContext) {
        return ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext,
            TestUnavailableMultifactorAuthenticationProvider.class, "unavailable-provider%d".formatted(System.currentTimeMillis()));
    }

    @Override
    public String getFriendlyName() {
        return getClass().getSimpleName();
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean isAvailable(final RegisteredService service) {
        return false;
    }
}
