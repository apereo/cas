package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This is {@link RegisteredServiceConsentPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public interface RegisteredServiceConsentPolicy extends Serializable {

    /**
     * Indicate whether consent is enabled.
     *
     * @return the boolean
     */
    default boolean isEnabled() {
        return true;
    }

    /**
     * Gets excluded attributes.
     * Excludes the set of specified attributes from consent.
     *
     * @return the excluded attributes
     */
    default Set<String> getExcludedAttributes() {
        return new LinkedHashSet<>(0);
    }

    /**
     * Gets include-only attributes.
     * If specified, consent should only be applied to the listed attributes
     * and not everything the attribute release policy may indicate.
     *
     * @return the include-only attributes. If the return collection is null or empty, 
     * attribute release policy is consulted to determine all of included attributes.
     */
    default Set<String> getIncludeOnlyAttributes() {
        return new LinkedHashSet<>(0);
    }
}
