package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link ChainingRegisteredServiceDelegatedAuthenticationPolicy}.
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
public class ChainingRegisteredServiceDelegatedAuthenticationPolicy implements RegisteredServiceDelegatedAuthenticationPolicy {
    @Serial
    private static final long serialVersionUID = -2127874606493954025L;

    private List<RegisteredServiceDelegatedAuthenticationPolicy> strategies = new ArrayList<>();

    private LogicalOperatorTypes operator = LogicalOperatorTypes.AND;

    /**
     * Add policy/strategy.
     *
     * @param policy the provider
     */
    public void addStrategy(final @NonNull RegisteredServiceDelegatedAuthenticationPolicy policy) {
        strategies.add(policy);
    }

    @Override
    @JsonIgnore
    public Collection<String> getAllowedProviders() {
        return strategies.stream()
            .map(RegisteredServiceDelegatedAuthenticationPolicy::getAllowedProviders)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());
    }

    @Override
    @JsonIgnore
    public String getSelectionStrategy() {
        return strategies.stream()
            .map(RegisteredServiceDelegatedAuthenticationPolicy::getSelectionStrategy)
            .filter(StringUtils::isNotBlank)
            .findFirst()
            .orElse(StringUtils.EMPTY);
    }

    @Override
    @JsonIgnore
    public boolean isExclusive() {
        if (operator == LogicalOperatorTypes.OR) {
            return strategies.stream().anyMatch(RegisteredServiceDelegatedAuthenticationPolicy::isExclusive);
        }
        return strategies.stream().allMatch(RegisteredServiceDelegatedAuthenticationPolicy::isExclusive);
    }

    @Override
    @JsonIgnore
    public boolean isPermitUndefined() {
        if (operator == LogicalOperatorTypes.OR) {
            return strategies.stream().anyMatch(RegisteredServiceDelegatedAuthenticationPolicy::isPermitUndefined);
        }
        return strategies.stream().allMatch(RegisteredServiceDelegatedAuthenticationPolicy::isPermitUndefined);
    }

    @Override
    @JsonIgnore
    public boolean isProviderAllowed(final String provider, final RegisteredService registeredService) {
        if (operator == LogicalOperatorTypes.OR) {
            return strategies.stream().anyMatch(policy -> policy.isProviderAllowed(provider, registeredService));
        }
        return strategies.stream().allMatch(policy -> policy.isProviderAllowed(provider, registeredService));

    }

    @Override
    @JsonIgnore
    public boolean isProviderRequired() {
        if (operator == LogicalOperatorTypes.OR) {
            return strategies.stream().anyMatch(RegisteredServiceDelegatedAuthenticationPolicy::isProviderRequired);
        }
        return strategies.stream().allMatch(RegisteredServiceDelegatedAuthenticationPolicy::isProviderRequired);
    }

    @JsonIgnore
    @Override
    public boolean isExclusiveToProvider(final String name) {
        if (operator == LogicalOperatorTypes.OR) {
            return strategies.stream().anyMatch(strategy -> strategy.isExclusiveToProvider(name));
        }
        return strategies.stream().allMatch(strategy -> strategy.isExclusiveToProvider(name));
    }
}
