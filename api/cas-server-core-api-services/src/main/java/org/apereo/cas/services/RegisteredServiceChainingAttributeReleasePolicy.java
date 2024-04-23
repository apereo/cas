package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.util.Collection;
import java.util.List;

/**
 * Chaining multiple attribute release policies together.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredServiceChainingAttributeReleasePolicy extends RegisteredServiceAttributeReleasePolicy {

    /**
     * Gets policies.
     *
     * @return the policies
     */
    List<RegisteredServiceAttributeReleasePolicy> getPolicies();

    /**
     * Add policies.
     *
     * @param policies the policies
     * @return the registered service chaining attribute release policy
     */
    RegisteredServiceChainingAttributeReleasePolicy addPolicies(RegisteredServiceAttributeReleasePolicy... policies);

    /**
     * Add policies.
     *
     * @param policies the policies
     * @return the registered service chaining attribute release policy
     */
    @CanIgnoreReturnValue
    default RegisteredServiceChainingAttributeReleasePolicy addPolicies(final Collection<RegisteredServiceAttributeReleasePolicy> policies) {
        addPolicies(policies.toArray(new RegisteredServiceAttributeReleasePolicy[0]));
        return this;
    }

    /**
     * Size of the policies.
     *
     * @return the int
     */
    int size();
}
