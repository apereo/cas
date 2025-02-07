package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.ChainingPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.RegisteredServicePrincipalAttributesRepository;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;
import org.apereo.cas.services.consent.ChainingRegisteredServiceConsentPolicy;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
public class ChainingAttributeReleasePolicy implements RegisteredServiceChainingAttributeReleasePolicy {

    @Serial
    private static final long serialVersionUID = 3795054936775326709L;

    private List<RegisteredServiceAttributeReleasePolicy> policies = new ArrayList<>();

    private PrincipalAttributesCoreProperties.MergingStrategyTypes mergingPolicy =
        PrincipalAttributesCoreProperties.MergingStrategyTypes.REPLACE;

    private int order;

    @Override
    public RegisteredServiceConsentPolicy getConsentPolicy() {
        val chainingConsentPolicy = new ChainingRegisteredServiceConsentPolicy();
        val newConsentPolicies = policies
            .stream()
            .filter(Objects::nonNull)
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
            .filter(Objects::nonNull)
            .sorted(AnnotationAwareOrderComparator.INSTANCE)
            .map(RegisteredServiceAttributeReleasePolicy::getPrincipalAttributesRepository)
            .sorted(AnnotationAwareOrderComparator.INSTANCE)
            .collect(Collectors.toList());
        return new ChainingPrincipalAttributesRepository(repositories);
    }

    @Override
    public Map<String, List<Object>> getAttributes(final RegisteredServiceAttributeReleasePolicyContext context) {
        try {
            val merger = CoreAuthenticationUtils.getAttributeMerger(mergingPolicy);
            val attributes = new HashMap<String, List<Object>>();
            policies
                .stream()
                .filter(Objects::nonNull)
                .filter(policy -> policy.getActivationCriteria() == null || policy.getActivationCriteria().shouldActivate(context))
                .sorted(AnnotationAwareOrderComparator.INSTANCE)
                .filter(context.getAttributeReleasePolicyPredicate())
                .forEach(Unchecked.consumer(policy -> {
                    LOGGER.trace("Fetching attributes from policy [{}] for principal [{}]",
                        policy.getName(), context.getPrincipal().getId());
                    val policyAttributes = policy.getAttributes(context);
                    val results = new HashMap<>(merger.mergeAttributes(attributes, policyAttributes));
                    LOGGER.trace("Attributes that remain, after the merge with attribute policy results, are [{}]", results);
                    attributes.clear();
                    attributes.putAll(results);
                    context.getReleasingAttributes().clear();
                    context.getReleasingAttributes().putAll(attributes);
                }));
            return attributes;
        } finally {
            context.getReleasingAttributes().clear();
        }
    }

    @Override
    public Map<String, List<Object>> getConsentableAttributes(final RegisteredServiceAttributeReleasePolicyContext context) {
        val merger = CoreAuthenticationUtils.getAttributeMerger(mergingPolicy);
        val attributes = new HashMap<String, List<Object>>();
        policies
            .stream()
            .filter(Objects::nonNull)
            .filter(policy -> policy.getActivationCriteria() == null || policy.getActivationCriteria().shouldActivate(context))
            .sorted(AnnotationAwareOrderComparator.INSTANCE)
            .forEach(Unchecked.consumer(policy -> {
                LOGGER.trace("Fetching consentable attributes from policy [{}] for principal [{}]",
                    policy.getName(), context.getPrincipal().getId());
                val policyAttributes = policy.getConsentableAttributes(context);
                merger.mergeAttributes(attributes, policyAttributes);
                LOGGER.trace("Attributes that remain, after the merge with consentable attribute policy results, are [{}]", attributes);
            }));
        return attributes;
    }

    @Override
    @CanIgnoreReturnValue
    public RegisteredServiceChainingAttributeReleasePolicy addPolicies(final RegisteredServiceAttributeReleasePolicy... policies) {
        this.policies.addAll(Arrays.stream(policies).toList());
        return this;
    }

    @Override
    public int size() {
        return policies.size();
    }

    
}
