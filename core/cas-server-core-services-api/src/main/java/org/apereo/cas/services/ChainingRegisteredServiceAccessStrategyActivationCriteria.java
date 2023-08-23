package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.jooq.lambda.Unchecked;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is {@link ChainingRegisteredServiceAccessStrategyActivationCriteria}.
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
public class ChainingRegisteredServiceAccessStrategyActivationCriteria implements RegisteredServiceAccessStrategyActivationCriteria {
    @Serial
    private static final long serialVersionUID = 5118603912161923218L;

    private List<RegisteredServiceAccessStrategyActivationCriteria> conditions = new ArrayList<>();

    private LogicalOperatorTypes operator = LogicalOperatorTypes.AND;

    /**
     * Add policy/strategy.
     *
     * @param policy the provider
     */
    public void addCondition(final @NonNull RegisteredServiceAccessStrategyActivationCriteria policy) {
        conditions.add(policy);
    }

    /**
     * Add conditions.
     *
     * @param policies the policies
     */
    public void addConditions(final RegisteredServiceAccessStrategyActivationCriteria... policies) {
        Arrays.stream(policies).forEach(this::addCondition);
    }

    @Override
    public boolean shouldActivate(final RegisteredServiceAccessStrategyRequest request) {
        if (operator == LogicalOperatorTypes.OR) {
            return conditions
                .stream()
                .anyMatch(Unchecked.predicate(condition -> condition.shouldActivate(request)));
        }
        return conditions
            .stream()
            .allMatch(Unchecked.predicate(condition -> condition.shouldActivate(request)));
    }

    @Override
    public boolean isAllowIfInactive() {
        if (operator == LogicalOperatorTypes.OR) {
            return conditions
                .stream()
                .anyMatch(RegisteredServiceAccessStrategyActivationCriteria::isAllowIfInactive);
        }
        return conditions
            .stream()
            .allMatch(RegisteredServiceAccessStrategyActivationCriteria::isAllowIfInactive);
    }
}
