package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.Service;
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
import org.jooq.lambda.Unchecked;

import java.io.Serial;
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
    @Serial
    private static final long serialVersionUID = 5018603912161923218L;

    private List<RegisteredServiceAccessStrategy> strategies = new ArrayList<>();

    private LogicalOperatorTypes operator = LogicalOperatorTypes.AND;

    /**
     * The unauthorized redirect url.
     */
    private URI unauthorizedRedirectUrl;

    private int order;

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
    public boolean isServiceAccessAllowed(final RegisteredService registeredService,
                                          final Service service) {
        if (operator == LogicalOperatorTypes.OR) {
            return strategies.stream().anyMatch(strategy -> strategy.isServiceAccessAllowed(registeredService, service));
        }
        return strategies.stream().allMatch(strategy -> strategy.isServiceAccessAllowed(registeredService, service));
    }

    @Override
    public boolean isServiceAccessAllowedForSso(final RegisteredService registeredService) {
        if (operator == LogicalOperatorTypes.OR) {
            return strategies.stream().anyMatch(strategy -> strategy.isServiceAccessAllowedForSso(registeredService));
        }
        return strategies.stream().allMatch(strategy -> strategy.isServiceAccessAllowedForSso(registeredService));
    }

    @Override
    public boolean authorizeRequest(final RegisteredServiceAccessStrategyRequest request) {
        if (operator == LogicalOperatorTypes.OR) {
            return strategies.stream().anyMatch(Unchecked.predicate(strategy -> strategy.authorizeRequest(request)));
        }
        return strategies.stream().allMatch(Unchecked.predicate(strategy -> strategy.authorizeRequest(request)));
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
