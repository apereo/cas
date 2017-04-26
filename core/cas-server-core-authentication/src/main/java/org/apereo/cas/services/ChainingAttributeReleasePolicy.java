package org.apereo.cas.services;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link ChainingAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class ChainingAttributeReleasePolicy implements RegisteredServiceAttributeReleasePolicy {
    private static final long serialVersionUID = 3795054936775326709L;

    private List<RegisteredServiceAttributeReleasePolicy> policies = new ArrayList<>();

    public List<RegisteredServiceAttributeReleasePolicy> getPolicies() {
        return policies;
    }

    public void setPolicies(final List<RegisteredServiceAttributeReleasePolicy> policies) {
        this.policies = policies;
    }

    @Override
    public Map<String, Object> getAttributes(final Principal p, final Service selectedService, final RegisteredService service) {
        final Map<String, Object> attributes = new HashMap<>();
        policies.forEach(policy -> attributes.putAll(policy.getAttributes(p, selectedService, service)));
        return attributes;
    }

    /**
     * Add policy.
     *
     * @param policy the policy
     */
    public void addPolicy(final RegisteredServiceAttributeReleasePolicy policy) {
        this.policies.add(policy);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("policies", policies)
                .toString();
    }
}
