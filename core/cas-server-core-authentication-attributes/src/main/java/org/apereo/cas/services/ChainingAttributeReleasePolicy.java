package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.consent.ChainingRegisteredServiceConsentPolicy;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is {@link ChainingAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@ToString
@Setter
@Getter
@Slf4j
@EqualsAndHashCode
public class ChainingAttributeReleasePolicy implements RegisteredServiceAttributeReleasePolicy {

    private static final long serialVersionUID = 3795054936775326709L;

    private List<RegisteredServiceAttributeReleasePolicy> policies = new ArrayList<>();

    private String mergingPolicy = "replace";

    private int order;

    @Override
    public RegisteredServiceConsentPolicy getConsentPolicy() {
        AnnotationAwareOrderComparator.sortIfNecessary(policies);
        val policy = new ChainingRegisteredServiceConsentPolicy();
        val consentPolicies = this.policies
            .stream()
            .map(RegisteredServiceAttributeReleasePolicy::getConsentPolicy)
            .distinct()
            .collect(Collectors.toList());
        policy.addPolicies(consentPolicies);
        return policy;
    }

    @Override
    public Map<String, List<Object>> getAttributes(final Principal p, final Service selectedService, final RegisteredService service) {
        AnnotationAwareOrderComparator.sortIfNecessary(policies);

        val merger = CoreAuthenticationUtils.getAttributeMerger(mergingPolicy);
        val attributes = new HashMap<String, List<Object>>();
        policies.forEach(policy -> {
            LOGGER.trace("Fetching attributes from policy [{}] for principal [{}]", policy.getName(), p.getId());
            val policyAttributes = policy.getAttributes(p, selectedService, service);
            merger.mergeAttributes(attributes, policyAttributes);
            LOGGER.trace("Attributes that remain, after the merge with attribute policy results, are [{}]", attributes);
        });
        return attributes;
    }

    @Override
    public Map<String, List<Object>> getConsentableAttributes(final Principal principal, final Service selectedService, final RegisteredService service) {
        AnnotationAwareOrderComparator.sortIfNecessary(policies);

        val merger = CoreAuthenticationUtils.getAttributeMerger(mergingPolicy);
        val attributes = new HashMap<String, List<Object>>();
        policies.forEach(policy -> {
            LOGGER.trace("Fetching consentable attributes from policy [{}] for principal [{}]", policy.getName(), principal.getId());
            val policyAttributes = policy.getConsentableAttributes(principal, selectedService, service);
            merger.mergeAttributes(attributes, policyAttributes);
            LOGGER.trace("Attributes that remain, after the merge with consentable attribute policy results, are [{}]", attributes);
        });
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
     * Add policies.
     *
     * @param policies the policies
     */
    public void addPolicies(final RegisteredServiceAttributeReleasePolicy... policies) {
        this.policies.addAll(Arrays.stream(policies).collect(Collectors.toList()));
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
