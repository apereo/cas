package org.apereo.cas.authentication;

import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorProviderProperties;

/**
 * Interface for beans that can be considered a Factory that can create instances of specific
 * {@link MultifactorAuthenticationProvider} when passed an instance of {@link BaseMultifactorProviderProperties}
 * Used by MFA providers that can be configured with multiple instances at runtime such as Duo.
 *
 * @author Travis Schmidt
 * @since 6.0
 */
@FunctionalInterface
public interface MultifactorAuthenticationProviderFactoryBean<T extends MultifactorAuthenticationProvider,
                                                              P extends BaseMultifactorProviderProperties> {

    /**
     * Default suffix to add to the bean name.
     */
    String PROVIDER_SUFFIX = "-provider";

    /**
     * Create an instance of {@link MultifactorAuthenticationProvider} based on passed properties.
     *
     * @param properties - the properties
     * @return - the provider
     */
    T createProvider(P properties);

    /**
     * Method returns the generated bean name for the provider.
     *
     * @param id - the id
     * @return - the beanName
     */
    default String beanName(final String id) {
        return id.concat(PROVIDER_SUFFIX);
    }
}
