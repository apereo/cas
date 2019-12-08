package org.apereo.cas.support.saml.services.idp.metadata.plan;

/**
 * This is {@link SamlRegisteredServiceMetadataResolutionPlanConfigurer}, to be implemented
 * by modules that wish to register serializable classes into the plan.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@FunctionalInterface
public interface SamlRegisteredServiceMetadataResolutionPlanConfigurer {
    /**
     * configure the plan.
     *
     * @param plan the plan
     */
    void configureMetadataResolutionPlan(SamlRegisteredServiceMetadataResolutionPlan plan);

    /**
     * Gets name.
     *
     * @return the name
     */
    default String getName() {
        return getClass().getSimpleName();
    }
}
