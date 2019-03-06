package org.apereo.cas.authentication;

import lombok.val;
import org.apereo.services.persondir.support.merger.IAttributeMerger;

/**
 * This is {@link AttributeMergingStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public enum AttributeMergingStrategy {
    /**
     * Replace attributes.
     */
    REPLACE,
    /**
     * Add attributes.
     */
    ADD,
    /**
     * No merging.
     */
    NONE,
    /**
     * Multivalued attributes.
     */
    MULTIVALUED;

    /**
     * Get attribute merger.
     *
     * @return the attribute merger
     */
    public IAttributeMerger getAttributeMerger() {
        val name = this.name().toUpperCase();
        return CoreAuthenticationUtils.getAttributeMerger(name);
    }
}
