package org.apereo.cas.services;

import org.apereo.cas.configuration.support.TriStateBoolean;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.val;
import org.jspecify.annotations.NonNull;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
@NoArgsConstructor
@JsonIgnoreProperties("order")
public class ChainingRegisteredServiceSingleSignOnParticipationPolicy implements RegisteredServiceSingleSignOnParticipationPolicy {
    @Serial
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
        policies.addAll(Arrays.stream(policy).toList());
    }

    @Override
    public boolean shouldParticipateInSso(final RegisteredService registeredService, final AuthenticationAwareTicket ticketState) {
        return policies.stream()
            .allMatch(p -> p.shouldParticipateInSso(registeredService, ticketState));
    }

    @JsonIgnore
    @Override
    public TriStateBoolean getCreateCookieOnRenewedAuthentication() {
        val result = policies
            .stream()
            .filter(p -> p.getCreateCookieOnRenewedAuthentication() != null)
            .allMatch(p -> p.getCreateCookieOnRenewedAuthentication().isTrue() || p.getCreateCookieOnRenewedAuthentication().isUndefined());
        return TriStateBoolean.fromBoolean(result);
    }
}
