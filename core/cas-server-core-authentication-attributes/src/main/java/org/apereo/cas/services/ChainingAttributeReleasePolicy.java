package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.val;

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
@ToString
@Setter
@Getter
public class ChainingAttributeReleasePolicy implements RegisteredServiceAttributeReleasePolicy {

    private static final long serialVersionUID = 3795054936775326709L;

    private List<RegisteredServiceAttributeReleasePolicy> policies = new ArrayList<>();

    @Override
    public Map<String, Object> getAttributes(final Principal p, final Service selectedService, final RegisteredService service) {
        val attributes = new HashMap<String, Object>();
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

    /**
     * Size.
     *
     * @return the int
     */
    public int size() {
        return policies.size();
    }
}
