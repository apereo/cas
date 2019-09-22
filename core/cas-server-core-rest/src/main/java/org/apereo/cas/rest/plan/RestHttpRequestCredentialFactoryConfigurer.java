package org.apereo.cas.rest.plan;

import org.apereo.cas.rest.factory.ChainingRestHttpRequestCredentialFactory;

/**
 * This is {@link RestHttpRequestCredentialFactoryConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@FunctionalInterface
public interface RestHttpRequestCredentialFactoryConfigurer {

    /**
     * Configure credential factory.
     *
     * @param factory the factory
     */
    void configureCredentialFactory(ChainingRestHttpRequestCredentialFactory factory);
}
