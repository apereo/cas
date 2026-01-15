package org.apereo.cas.ticket.expiration;

import module java.base;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicketAwareTicket;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.Nulls;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link BaseDelegatingExpirationPolicy} that activates a number of inner expiration policies
 * depending on the outcome of predicates defined. The use case here is that an expiration policy
 * needs to be activated conditionally given certain properties of the authentication and/or
 * principal and so the predicate having determined the appropriate policy will
 * delegate the handling of the expiration appropriately.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Slf4j
@Getter
@Setter
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class BaseDelegatingExpirationPolicy extends AbstractCasExpirationPolicy {
    /**
     * Default policy name.
     */
    public static final String POLICY_NAME_DEFAULT = "DEFAULT";

    @Serial
    private static final long serialVersionUID = 5927936344949518688L;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private final Map<String, ExpirationPolicy> policies = new LinkedHashMap<>();

    /**
     * Add policy.
     *
     * @param policy the policy
     * @return the base delegating expiration policy
     */
    @CanIgnoreReturnValue
    public BaseDelegatingExpirationPolicy addPolicy(final ExpirationPolicy policy) {
        LOGGER.trace("Adding expiration policy [{}] with default name [{}]", policy, policy.getName());
        this.policies.put(policy.getName(), policy);
        return this;
    }

    /**
     * Add policy.
     *
     * @param name   the name
     * @param policy the policy
     * @return the base delegating expiration policy
     */
    @CanIgnoreReturnValue
    public BaseDelegatingExpirationPolicy addPolicy(final String name, final ExpirationPolicy policy) {
        LOGGER.trace("Adding expiration policy [{}] with name [{}]", policy, name);
        this.policies.put(name, policy);
        return this;
    }

    @Override
    public boolean isExpired(final TicketGrantingTicketAwareTicket ticketState) {
        val match = getExpirationPolicyFor(ticketState);
        if (match.isEmpty()) {
            LOGGER.warn("No expiration policy was found for ticket state [{}]. "
                + "Consider configuring a predicate that delegates to an expiration policy.", ticketState);
            return super.isExpired(ticketState);
        }
        val policy = match.get();
        LOGGER.trace("Activating expiration policy [{}] for ticket [{}]", policy.getName(), ticketState);
        return policy.isExpired(ticketState);
    }

    @Override
    public Long getTimeToLive(final Ticket ticketState) {
        val match = getExpirationPolicyFor((AuthenticationAwareTicket) ticketState);
        if (match.isEmpty()) {
            LOGGER.warn("No expiration policy was found for ticket state [{}] to calculate time-to-live. "
                + "Consider configuring a predicate that delegates to an expiration policy.", ticketState);
            return super.getTimeToLive(ticketState);
        }
        val policy = match.get();
        LOGGER.trace("Getting TTL from policy [{}] for ticket [{}]", policy.getName(), ticketState);
        return policy.getTimeToLive(ticketState);
    }

    @JsonIgnore
    @Override
    public Long getTimeToLive() {
        return this.policies.get(POLICY_NAME_DEFAULT).getTimeToLive();
    }

    protected Optional<ExpirationPolicy> getExpirationPolicyFor(final AuthenticationAwareTicket ticketState) {
        val name = getExpirationPolicyNameFor(ticketState);
        LOGGER.trace("Received expiration policy name [{}] to activate", name);
        if (StringUtils.isNotBlank(name) && policies.containsKey(name)) {
            val policy = policies.get(name);
            LOGGER.trace("Located expiration policy [{}] by name [{}]", policy, name);
            return Optional.of(policy);
        }
        LOGGER.warn("No expiration policy could be found by the name [{}] for ticket state [{}]", name, ticketState);
        return Optional.empty();
    }

    protected abstract String getExpirationPolicyNameFor(AuthenticationAwareTicket ticketState);

    @Override
    public ZonedDateTime toMaximumExpirationTime(final Ticket ticketState) {
        val result = getExpirationPolicyFor((AuthenticationAwareTicket) ticketState);
        return result.map(policy -> policy.toMaximumExpirationTime(ticketState))
            .orElseGet(() -> ZonedDateTime.now(Clock.systemUTC()));
    }
}
