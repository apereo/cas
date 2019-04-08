package org.apereo.cas.services;

import org.apereo.cas.ticket.TicketState;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link ChainingRegisteredServiceSingleSignOnParticipationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString(callSuper = true)
@Getter
@Setter
@EqualsAndHashCode
@Slf4j
@NoArgsConstructor
@JsonIgnoreProperties("order")
public class ChainingRegisteredServiceSingleSignOnParticipationPolicy implements RegisteredServiceSingleSignOnParticipationPolicy {
    private static final long serialVersionUID = -2923946898337761319L;

    private List<RegisteredServiceSingleSignOnParticipationPolicy> policies = new ArrayList<>();

    /**
     * Add provider.
     *
     * @param policy the provider
     */
    public void addPolicy(final @NonNull RegisteredServiceSingleSignOnParticipationPolicy policy) {
        policies.add(policy);
    }

    /**
     * Add providers.
     *
     * @param policy the provider
     */
    public void addPolicies(final @NonNull List<RegisteredServiceSingleSignOnParticipationPolicy> policy) {
        policies.addAll(policy);
    }

    /**
     * Add policies.
     *
     * @param policy the policy
     */
    public void addPolicies(final @NonNull RegisteredServiceSingleSignOnParticipationPolicy... policy) {
        policies.addAll(Arrays.stream(policy).collect(Collectors.toList()));
    }

    @Override
    public boolean shouldParticipateInSso(final TicketState ticketState) {
        AnnotationAwareOrderComparator.sortIfNecessary(this.policies);
        return policies.stream().allMatch(p -> p.shouldParticipateInSso(ticketState));
    }
}
