package org.apereo.cas.rest;

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
    default void registerCredentialFactory(final ChainingRestHttpRequestCredentialFactory factory) {
    }
}
