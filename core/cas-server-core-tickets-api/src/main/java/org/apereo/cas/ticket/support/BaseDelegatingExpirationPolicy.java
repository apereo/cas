package org.apereo.cas.ticket.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public abstract class BaseDelegatingExpirationPolicy extends AbstractCasExpirationPolicy {
    private static final long serialVersionUID = 5927936344949518688L;

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseDelegatingExpirationPolicy.class);

    private final Map<String, ExpirationPolicy> policies = new LinkedHashMap<>();

    private final ExpirationPolicy defaultExpirationPolicy;

    public BaseDelegatingExpirationPolicy() {
        this(null);
    }

    public BaseDelegatingExpirationPolicy(final ExpirationPolicy defaultExpirationPolicy) {
        this.defaultExpirationPolicy = defaultExpirationPolicy;
    }

    /**
     * Add policy.
     *
     * @param policy    the policy
     */
    public void addPolicy(final ExpirationPolicy policy) {
        LOGGER.debug("Adding expiration policy [{}] with name [{}]", policy, policy.getName());
        this.policies.put(policy.getName(), policy);
    }

    /**
     * Add policy.
     *
     * @param name   the name
     * @param policy the policy
     */
    public void addPolicy(final String name, final ExpirationPolicy policy) {
        LOGGER.debug("Adding expiration policy [{}] with name [{}]", policy, name);
        this.policies.put(name, policy);
    }

    /**
     * Add policy.
     *
     * @param name   the name
     * @param policy the policy
     */
    public void addPolicy(final Enum name, final ExpirationPolicy policy) {
        LOGGER.debug("Adding expiration policy [{}] with name [{}]", policy, name);
        addPolicy(name.name(), policy);
    }
    
    @Override
    public boolean isExpired(final TicketState ticketState) {
        final Optional<ExpirationPolicy> match = getExpirationPolicyFor(ticketState);
        if (!match.isPresent()) {
            LOGGER.warn("No expiration policy was found for ticket state [{}]. "
                    + "Consider configuring a predicate that delegates to an expiration policy.", ticketState);
            return false;
        }
        final ExpirationPolicy policy = match.get();
        LOGGER.debug("Activating expiration policy [{}] for ticket [{}]", policy, ticketState);
        return policy.isExpired(ticketState);
    }

    @JsonIgnore
    @Override
    public Long getTimeToLive() {
        if (this.defaultExpirationPolicy == null) {
            return 0L;
        }
        return this.defaultExpirationPolicy.getTimeToLive();
    }

    @JsonIgnore
    @Override
    public Long getTimeToIdle() {
        if (this.defaultExpirationPolicy == null) {
            return 0L;
        }
        return this.defaultExpirationPolicy.getTimeToIdle();
    }

    public Map<String, ExpirationPolicy> getPolicies() {
        return policies;
    }

    public ExpirationPolicy getDefaultExpirationPolicy() {
        return defaultExpirationPolicy;
    }
    
    /**
     * Gets expiration policy by its name.
     *
     * @param ticketState the ticket state
     * @return the expiration policy for
     */
    protected Optional<ExpirationPolicy> getExpirationPolicyFor(final TicketState ticketState) {
        final String name = getExpirationPolicyNameFor(ticketState);
        LOGGER.debug("Received expiration policy name [{}] to activate", name);
        if (StringUtils.isNotBlank(name) && policies.containsKey(name)) {
            final ExpirationPolicy policy = policies.get(name);
            LOGGER.debug("Located expiration policy [{}] by name [{}]", policy, name);
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
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final BaseDelegatingExpirationPolicy rhs = (BaseDelegatingExpirationPolicy) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(this.policies, rhs.policies)
                .append(this.defaultExpirationPolicy, rhs.defaultExpirationPolicy)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(policies)
                .append(defaultExpirationPolicy)
                .toHashCode();
    }
}
