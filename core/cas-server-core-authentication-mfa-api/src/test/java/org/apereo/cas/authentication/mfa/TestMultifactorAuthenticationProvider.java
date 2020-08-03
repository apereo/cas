package org.apereo.cas.authentication.mfa;

import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link TestMultifactorAuthenticationProvider}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class TestMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {
    public static final String ID = "mfa-dummy";

    private static final long serialVersionUID = -9184556172646207560L;

    public TestMultifactorAuthenticationProvider() {
        this(ID);
    }

    public TestMultifactorAuthenticationProvider(final String id) {
        setId(id);
    }

    public static TestMultifactorAuthenticationProvider registerProviderIntoApplicationContext(final ConfigurableApplicationContext applicationContext) {
        return registerProviderIntoApplicationContext(applicationContext, "provider" + RandomUtils.randomAlphabetic(8));
    }

    public static MultifactorAuthenticationProvider registerProviderIntoApplicationContext(final ConfigurableApplicationContext applicationContext,
                                                                                           final MultifactorAuthenticationProvider provider) {
        return ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext, provider,
            "provider" + RandomUtils.randomAlphabetic(8));
    }

    public static TestMultifactorAuthenticationProvider registerProviderIntoApplicationContext(
        final ConfigurableApplicationContext applicationContext, final String beanId) {
        return ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext,
            TestMultifactorAuthenticationProvider.class, beanId);
    }

    @Override
    public String getFriendlyName() {
        return getClass().getSimpleName();
    }
}
