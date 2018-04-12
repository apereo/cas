package org.apereo.cas.authentication.mfa;

import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link TestMultifactorAuthenticationProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class TestMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {
    private static final long serialVersionUID = -9184556172646207560L;

    @Override
    public String getFriendlyName() {
        return getClass().getSimpleName();
    }

    @Override
    public String getId() {
        return "mfa-dummy";
    }

    /**
     * Register provider into application context.
     *
     * @param applicationContext the application context
     * @return the multifactor authentication provider
     */
    public static MultifactorAuthenticationProvider registerProviderIntoApplicationContext(final ConfigurableApplicationContext applicationContext) {
        final ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
        final TestMultifactorAuthenticationProvider provider = beanFactory.createBean(TestMultifactorAuthenticationProvider.class);
        beanFactory.initializeBean(provider, "provider" + System.currentTimeMillis());
        beanFactory.autowireBean(provider);
        beanFactory.registerSingleton("provider" + System.currentTimeMillis(), provider);
        return provider;
    }
}
