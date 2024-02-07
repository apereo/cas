package org.apereo.cas.authentication.attribute;

import java.util.Map;

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
     * @return the list
     */
    Map<String, AttributeDefinition> load();
}
