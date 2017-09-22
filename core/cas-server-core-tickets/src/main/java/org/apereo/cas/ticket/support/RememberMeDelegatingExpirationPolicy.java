package org.apereo.cas.ticket.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.authentication.RememberMeCredential;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

/**
 * Delegates to different expiration policies depending on whether remember me
 * is true or not.
 *
 * @author Scott Battaglia
 * @since 3.2.1
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include= JsonTypeInfo.As.PROPERTY)
public class RememberMeDelegatingExpirationPolicy extends AbstractCasExpirationPolicy {

    /**
     * Serialization support.
     */
    private static final long serialVersionUID = -2735975347698196127L;

    /**
     * The Logger instance for this class. Using a transient instance field for the Logger doesn't work, on object
     * deserialization the field is null.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RememberMeDelegatingExpirationPolicy.class);

    @JsonProperty
    private ExpirationPolicy rememberMeExpirationPolicy;

    @JsonProperty
    private ExpirationPolicy sessionExpirationPolicy;

    /**
     * Instantiates a new Remember me delegating expiration policy.
     */
    public RememberMeDelegatingExpirationPolicy() {
    }

    /**
     * Instantiates a new Remember me delegating expiration policy.
     *
     * @param rememberMeExpirationPolicy the remember me expiration policy
     * @param sessionExpirationPolicy    the session expiration policy
     */
    public RememberMeDelegatingExpirationPolicy(final ExpirationPolicy rememberMeExpirationPolicy,
                                                final ExpirationPolicy sessionExpirationPolicy) {
        this.rememberMeExpirationPolicy = rememberMeExpirationPolicy;
        this.sessionExpirationPolicy = sessionExpirationPolicy;
    }

    @PostConstruct
    private void postConstruct() {
        if (this.rememberMeExpirationPolicy != null) {
            LOGGER.debug("Using remember-me expiration policy of [{}]", this.rememberMeExpirationPolicy);
        }
        if (this.sessionExpirationPolicy != null) {
            LOGGER.debug("Using session expiration policy of [{}]", this.sessionExpirationPolicy);
        }
    }

    @Override
    public boolean isExpired(final TicketState ticketState) {
        if (this.rememberMeExpirationPolicy != null && this.sessionExpirationPolicy != null) {

            final Boolean b = (Boolean) ticketState.getAuthentication().getAttributes().
                    get(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME);

            if (b == null || b.equals(Boolean.FALSE)) {
                LOGGER.debug("Ticket is not associated with a remember-me authentication. Invoking [{}]", this.sessionExpirationPolicy);
                return this.sessionExpirationPolicy.isExpired(ticketState);
            }

            LOGGER.debug("Ticket is associated with a remember-me authentication. Invoking [{}]", this.rememberMeExpirationPolicy);
            return this.rememberMeExpirationPolicy.isExpired(ticketState);
        }
        LOGGER.warn("No expiration policy settings are defined");
        return false;
    }

    @JsonIgnore
    @Override
    public Long getTimeToLive() {
        if (this.rememberMeExpirationPolicy != null) {
            return this.rememberMeExpirationPolicy.getTimeToLive();
        }
        return 0L;
    }

    @JsonIgnore
    @Override
    public Long getTimeToIdle() {
        if (this.rememberMeExpirationPolicy != null) {
            return this.rememberMeExpirationPolicy.getTimeToIdle();
        }
        return 0L;
    }

    public void setRememberMeExpirationPolicy(final ExpirationPolicy rememberMeExpirationPolicy) {
        this.rememberMeExpirationPolicy = rememberMeExpirationPolicy;
    }

    public void setSessionExpirationPolicy(final ExpirationPolicy sessionExpirationPolicy) {
        this.sessionExpirationPolicy = sessionExpirationPolicy;
    }


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
        final RememberMeDelegatingExpirationPolicy rhs = (RememberMeDelegatingExpirationPolicy) obj;
        return new EqualsBuilder()
                .append(this.rememberMeExpirationPolicy, rhs.rememberMeExpirationPolicy)
                .append(this.sessionExpirationPolicy, rhs.sessionExpirationPolicy)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(rememberMeExpirationPolicy)
                .append(sessionExpirationPolicy)
                .toHashCode();
    }
}
