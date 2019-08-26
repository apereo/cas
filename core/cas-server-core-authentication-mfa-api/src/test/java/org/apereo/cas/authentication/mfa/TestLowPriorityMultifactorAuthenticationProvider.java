package org.apereo.cas.authentication.mfa;

import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;

/**
 * This is {@link TestLowPriorityMultifactorAuthenticationProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class TestLowPriorityMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {
    /**
     * Provider id.
     */
    public static final String ID = "mfa-dummy-lowpriority";

    public TestLowPriorityMultifactorAuthenticationProvider() {
        setOrder(Ordered.LOWEST_PRECEDENCE);
    }

    /**
     * Register provider into application context.
     *
     * @param applicationContext the application context
     * @return the multifactor authentication provider
     */
    public static TestLowPriorityMultifactorAuthenticationProvider registerProviderIntoApplicationContext(final ConfigurableApplicationContext applicationContext) {
        return ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext,
            TestLowPriorityMultifactorAuthenticationProvider.class,
            "provider" + RandomStringUtils.randomAlphabetic(8));
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
