package org.apereo.cas.support.saml.services.idp.metadata.plan;

/**
 * This is {@link SamlRegisteredServiceMetadataResolutionPlanConfigurator}, to be implemented
 * by modules that wish to register serializable classes into the plan.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public interface SamlRegisteredServiceMetadataResolutionPlanConfigurator {
    /**
     * configure the plan.
     *
     * @param plan the plan
     */
    default void configureMetadataResolutionPlan(final SamlRegisteredServiceMetadataResolutionPlan plan) {}
}
