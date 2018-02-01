package org.apereo.cas.services;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.ToString;
import lombok.Setter;

/**
 * This is {@link ChainingAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@ToString
@Setter
@Getter
public class ChainingAttributeReleasePolicy implements RegisteredServiceAttributeReleasePolicy {

    private static final long serialVersionUID = 3795054936775326709L;

    private List<RegisteredServiceAttributeReleasePolicy> policies = new ArrayList<>();

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
}
