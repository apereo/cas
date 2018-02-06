package org.apereo.cas.rest.plan;

import org.apereo.cas.rest.factory.ChainingRestHttpRequestCredentialFactory;

/**
 * This is {@link RestHttpRequestCredentialFactoryConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public interface RestHttpRequestCredentialFactoryConfigurer {

    /**
     * Configure credential factory.
     *
     * @param factory the factory
     */
    default void configureCredentialFactory(final ChainingRestHttpRequestCredentialFactory factory) {
    }
}
