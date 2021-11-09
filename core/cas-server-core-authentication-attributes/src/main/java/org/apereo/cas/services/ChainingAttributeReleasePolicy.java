package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.ChainingPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.RegisteredServicePrincipalAttributesRepository;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;
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
import java.util.LinkedHashSet;
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

    private List<RegisteredServiceAttributeReleasePolicy> policies = new ArrayList<>(0);

    private PrincipalAttributesCoreProperties.MergingStrategyTypes mergingPolicy =
        PrincipalAttributesCoreProperties.MergingStrategyTypes.REPLACE;

    private int order;

    @Override
    public RegisteredServiceConsentPolicy getConsentPolicy() {
        val chainingConsentPolicy = new ChainingRegisteredServiceConsentPolicy();
        val newConsentPolicies = policies
            .stream()
            .map(policy -> policy.getConsentPolicy().getPolicies())
            .flatMap(List::stream)
            .sorted(AnnotationAwareOrderComparator.INSTANCE)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        chainingConsentPolicy.addPolicies(newConsentPolicies);
        return chainingConsentPolicy;
    }

    @Override
    public RegisteredServicePrincipalAttributesRepository getPrincipalAttributesRepository() {
        val repositories = policies
            .stream()
            .sorted(AnnotationAwareOrderComparator.INSTANCE)
            .map(RegisteredServiceAttributeReleasePolicy::getPrincipalAttributesRepository)
            .sorted(AnnotationAwareOrderComparator.INSTANCE)
            .collect(Collectors.toList());
        return new ChainingPrincipalAttributesRepository(repositories);
    }

    @Override
    public synchronized Map<String, List<Object>> getAttributes(final RegisteredServiceAttributeReleasePolicyContext context) {
        try {
            context.getReleasingAttributes().clear();
            val merger = CoreAuthenticationUtils.getAttributeMerger(mergingPolicy);
            val attributes = new HashMap<String, List<Object>>();
            policies.stream().sorted(AnnotationAwareOrderComparator.INSTANCE).forEach(policy -> {
                LOGGER.trace("Fetching attributes from policy [{}] for principal [{}]",
                    policy.getName(), context.getPrincipal().getId());
                val policyAttributes = policy.getAttributes(context);
                merger.mergeAttributes(attributes, policyAttributes);
                LOGGER.trace("Attributes that remain, after the merge with attribute policy results, are [{}]", attributes);
                context.getReleasingAttributes().putAll(attributes);
            });
            return attributes;
        } finally {
            context.getReleasingAttributes().clear();
        }
    }

    @Override
    public Map<String, List<Object>> getConsentableAttributes(final RegisteredServiceAttributeReleasePolicyContext context) {
        val merger = CoreAuthenticationUtils.getAttributeMerger(mergingPolicy);
        val attributes = new HashMap<String, List<Object>>();
        policies.stream().sorted(AnnotationAwareOrderComparator.INSTANCE).forEach(policy -> {
            LOGGER.trace("Fetching consentable attributes from policy [{}] for principal [{}]",
                policy.getName(), context.getPrincipal().getId());
            val policyAttributes = policy.getConsentableAttributes(context);
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
     * Add all policies at once and then sort them.
     *
     * @param policies the policies
     */
    public void addPolicies(final RegisteredServiceAttributeReleasePolicy... policies) {
        this.policies.addAll(Arrays.stream(policies).collect(Collectors.toList()));
    }

    /**
     * Size int.
     *
     * @return the int
     */
    public int size() {
        return policies.size();
    }
}
