package org.apereo.cas.services;

/**
 * This is {@link RegisteredServiceChainOperatorTypes}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public enum RegisteredServiceChainOperatorTypes {
    /**
     * Operator to {@code AND} expressions together.
     * All conditions must pass.
     */
    AND,
    /**
     * Operator to {@code OR} expressions together.
     * At least one condition must pass.
     */
    OR
}
