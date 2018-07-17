package org.apereo.cas.util.junit;

/**
 * This is {@link IgnoreCondition}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@FunctionalInterface
public interface IgnoreCondition {
    /**
     * Is satisfied.
     *
     * @return the boolean
     */
    Boolean isSatisfied();
}
