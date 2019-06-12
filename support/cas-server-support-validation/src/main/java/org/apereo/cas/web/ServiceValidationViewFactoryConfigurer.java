package org.apereo.cas.web;

/**
 * This is {@link ServiceValidationViewFactoryConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@FunctionalInterface
public interface ServiceValidationViewFactoryConfigurer {

    /**
     * Configure view factory.
     *
     * @param factory the factory
     */
    void configureViewFactory(ServiceValidationViewFactory factory);
}
