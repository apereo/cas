package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is {@link ChainingRegisteredServiceAttributeReleaseActivationCriteria}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Slf4j
@Accessors(chain = true)
public class ChainingRegisteredServiceAttributeReleaseActivationCriteria implements RegisteredServiceAttributeReleaseActivationCriteria {
    @Serial
    private static final long serialVersionUID = 6942510462696845607L;

    private List<RegisteredServiceAttributeReleaseActivationCriteria> conditions = new ArrayList<>();

    private LogicalOperatorTypes operator = LogicalOperatorTypes.AND;

    /**
     * Add condition.
     *
     * @param criteria the provider
     */
    public void addCondition(final @NonNull RegisteredServiceAttributeReleaseActivationCriteria criteria) {
        conditions.add(criteria);
    }

    /**
     * Add conditions.
     *
     * @param criteria the policies
     */
    public void addConditions(final RegisteredServiceAttributeReleaseActivationCriteria... criteria) {
        Arrays.stream(criteria).forEach(this::addCondition);
    }

    @Override
    public boolean shouldActivate(final RegisteredServiceAttributeReleasePolicyContext context) {
        return operator == LogicalOperatorTypes.OR
            ? conditions.stream().anyMatch(condition -> condition.shouldActivate(context))
            : conditions.stream().allMatch(condition -> condition.shouldActivate(context));
    }

}
