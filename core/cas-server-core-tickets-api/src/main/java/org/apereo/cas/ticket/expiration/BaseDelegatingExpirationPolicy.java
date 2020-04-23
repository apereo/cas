package org.apereo.cas.ticket.expiration;

import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketState;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

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
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class BaseDelegatingExpirationPolicy extends AbstractCasExpirationPolicy {
    /**
     * Default policy name.
     */
    public static final String POLICY_NAME_DEFAULT = "DEFAULT";

    private static final int MAP_SIZE = 8;

    private static final long serialVersionUID = 5927936344949518688L;

    private final Map<String, ExpirationPolicy> policies = new LinkedHashMap<>(MAP_SIZE);

    /**
     * Add policy.
     *
     * @param policy the policy
     */
    public void addPolicy(final ExpirationPolicy policy) {
        LOGGER.trace("Adding expiration policy [{}] with name [{}]", policy, policy.getName());
        this.policies.put(policy.getName(), policy);
    }

    /**
     * Add policy.
     *
     * @param name   the name
     * @param policy the policy
     */
    public void addPolicy(final String name, final ExpirationPolicy policy) {
        LOGGER.trace("Adding expiration policy [{}] with name [{}]", policy, name);
        this.policies.put(name, policy);
    }

    @Override
    public boolean isExpired(final TicketState ticketState) {
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

    /**
     * Checks the given ticketState and gets the timeToLive for the relevant expiration policy.
     *
     * @param ticketState The ticketState to get the delegated expiration policy for
     * @return The TTL for the relevant expiration policy
     */
    @Override
    public Long getTimeToLive(final TicketState ticketState) {
        val match = getExpirationPolicyFor(ticketState);
        if (match.isEmpty()) {
            LOGGER.warn("No expiration policy was found for ticket state [{}]. "
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

    @JsonIgnore
    @Override
    public Long getTimeToIdle() {
        return this.policies.get(POLICY_NAME_DEFAULT).getTimeToIdle();
    }

    /**
     * Gets expiration policy by its name.
     *
     * @param ticketState the ticket state
     * @return the expiration policy for
     */
    protected Optional<ExpirationPolicy> getExpirationPolicyFor(final TicketState ticketState) {
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

    /**
     * Gets expiration policy name for.
     *
     * @param ticketState the ticket state
     * @return the expiration policy name for
     */
    protected abstract String getExpirationPolicyNameFor(TicketState ticketState);

}
