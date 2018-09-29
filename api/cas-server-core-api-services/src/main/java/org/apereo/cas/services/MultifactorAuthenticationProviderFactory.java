package org.apereo.cas.services;

import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorProviderProperties;

/**
 * Interface for provider a factory that can create a MultifactorAuthenticationProvider.
 *
 * @author Travis Schmidt
 * @since 6.0
 */
public interface MultifactorAuthenticationProviderFactory<T extends MultifactorAuthenticationProvider,
                                                          P extends BaseMultifactorProviderProperties> {

    /**
     * Default suffix to add to the bean name.
     */
    String PROVIDER_SUFFIX = "-provider";

    /**
     * Create an instance of MultifactorAuthenticationProvider based on passed properties.
     *
     * @param properties - the properties
     * @return - the provider
     */
    T create(P properties);

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
