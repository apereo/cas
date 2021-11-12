package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link ChainingRegisteredServiceAccessStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@JsonIgnoreProperties("order")
public class ChainingRegisteredServiceAccessStrategy implements RegisteredServiceAccessStrategy {
    private static final long serialVersionUID = 5018603912161923218L;

    private List<RegisteredServiceAccessStrategy> strategies = new ArrayList<>();

    private RegisteredServiceChainOperatorTypes operator = RegisteredServiceChainOperatorTypes.AND;

    /**
     * The unauthorized redirect url.
     */
    private URI unauthorizedRedirectUrl;

    /**
     * Add policy/strategy.
     *
     * @param policy the provider
     */
    public void addStrategy(final @NonNull RegisteredServiceAccessStrategy policy) {
        strategies.add(policy);
    }

    /**
     * Add strategies.
     *
     * @param policies the policies
     */
    public void addStrategies(final RegisteredServiceAccessStrategy... policies) {
        Arrays.stream(policies).forEach(this::addStrategy);
    }

    @Override
    @JsonIgnore
    public boolean isServiceAccessAllowed() {
        if (operator == RegisteredServiceChainOperatorTypes.OR) {
            return strategies.stream().anyMatch(RegisteredServiceAccessStrategy::isServiceAccessAllowed);
        }
        return strategies.stream().allMatch(RegisteredServiceAccessStrategy::isServiceAccessAllowed);
    }

    @Override
    @JsonIgnore
    public void setServiceAccessAllowed(final boolean enabled) {
        strategies.forEach(strategy -> strategy.setServiceAccessAllowed(enabled));
    }

    @Override
    @JsonIgnore
    public boolean isServiceAccessAllowedForSso() {
        if (operator == RegisteredServiceChainOperatorTypes.OR) {
            return strategies.stream().anyMatch(RegisteredServiceAccessStrategy::isServiceAccessAllowedForSso);
        }
        return strategies.stream().allMatch(RegisteredServiceAccessStrategy::isServiceAccessAllowedForSso);
    }

    @Override
    public boolean doPrincipalAttributesAllowServiceAccess(final String principal, final Map<String, Object> attributes) {
        if (operator == RegisteredServiceChainOperatorTypes.OR) {
            return strategies.stream().anyMatch(strategy -> strategy.doPrincipalAttributesAllowServiceAccess(principal, attributes));
        }
        return strategies.stream().allMatch(strategy -> strategy.doPrincipalAttributesAllowServiceAccess(principal, attributes));
    }

    @Override
    @JsonIgnore
    public RegisteredServiceDelegatedAuthenticationPolicy getDelegatedAuthenticationPolicy() {
        val policy = new ChainingRegisteredServiceDelegatedAuthenticationPolicy();
        policy.setOperator(this.operator);
        strategies.stream()
            .map(RegisteredServiceAccessStrategy::getDelegatedAuthenticationPolicy)
            .forEach(policy::addStrategy);
        return policy;
    }

    @Override
    @JsonIgnore
    public Map<String, Set<String>> getRequiredAttributes() {
        val results = new LinkedHashMap<String, List<Object>>();
        val merger = CoreAuthenticationUtils.getAttributeMerger(PrincipalAttributesCoreProperties.MergingStrategyTypes.MULTIVALUED);
        strategies.forEach(strategy -> {
            val requiredAttributes = strategy.getRequiredAttributes()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, mapper -> new ArrayList<>(mapper.getValue())));
            merger.mergeAttributes(results, (Map) requiredAttributes);
        });
        return (Map) results
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, mapper -> new LinkedHashSet<>(mapper.getValue())));
    }
}
