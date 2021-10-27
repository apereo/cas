package org.apereo.cas.configuration.model.core.authentication;

/**
 * This is {@link AttributeRepositoryStates}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public enum AttributeRepositoryStates {
    /**
     * Active and enabled repository,
     * and is invoked by default automatically.
     */
    ACTIVE,
    /**
     * Attribute repository is disabled and will not be used
     * to resolve people and attributes.
     */
    DISABLED,
    /**
     * Repository is in a semi-enabled state,
     * waiting to be called only on-demand when explicitly
     * asked for and will not be registered into the resolution plan.
     */
    STANDBY
}
