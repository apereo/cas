package org.apereo.cas.authentication.attribute;

/**
 * This is {@link AttributeDefinitionStoreConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@FunctionalInterface
public interface AttributeDefinitionStoreConfigurer {
    /**
     * Configure.
     *
     * @param store the store
     */
    void configure(AttributeDefinitionStore store);
}
