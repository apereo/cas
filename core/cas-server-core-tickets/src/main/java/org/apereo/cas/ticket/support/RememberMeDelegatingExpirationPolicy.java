package org.apereo.cas.ticket.support;

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
 *
 */
public class RememberMeDelegatingExpirationPolicy extends AbstractCasExpirationPolicy {

    /** Serialization support. */
    private static final long serialVersionUID = -2735975347698196127L;

    /**
     * The Logger instance for this class. Using a transient instance field for the Logger doesn't work, on object
     * deserialization the field is null.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RememberMeDelegatingExpirationPolicy.class);
    
    private ExpirationPolicy rememberMeExpirationPolicy;

    private ExpirationPolicy sessionExpirationPolicy;

    /**
     * Instantiates a new Remember me delegating expiration policy.
     */
    public RememberMeDelegatingExpirationPolicy() {}

    @PostConstruct
    private void postConstruct() {
        if (this.rememberMeExpirationPolicy != null) {
            LOGGER.debug("Using remember-me expiration policy of {}", this.rememberMeExpirationPolicy);
        }
        if (this.sessionExpirationPolicy != null) {
            LOGGER.debug("Using session expiration policy of {}", this.sessionExpirationPolicy);
        }
    }

    @Override
    public boolean isExpired(final TicketState ticketState) {
        if (this.rememberMeExpirationPolicy != null && this.sessionExpirationPolicy != null) {

            final Boolean b = (Boolean) ticketState.getAuthentication().getAttributes().
                    get(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME);

            if (b == null || b.equals(Boolean.FALSE)) {
                LOGGER.debug("Ticket is not associated with a remember-me authentication. Invoking {}", this.sessionExpirationPolicy);
                return this.sessionExpirationPolicy.isExpired(ticketState);
            }

            LOGGER.debug("Ticket is associated with a remember-me authentication. Invoking {}", this.rememberMeExpirationPolicy);
            return this.rememberMeExpirationPolicy.isExpired(ticketState);
        }
        LOGGER.warn("No expiration policy settings are defined");
        return false;
    }

    @Override
    public Long getTimeToLive() {
        if (this.rememberMeExpirationPolicy != null) {
            return this.rememberMeExpirationPolicy.getTimeToLive();
        }
        return 0L;
    }

    @Override
    public Long getTimeToIdle() {
        if (this.rememberMeExpirationPolicy != null) {
            return this.rememberMeExpirationPolicy.getTimeToIdle();
        }
        return 0L;
    }

    public void setRememberMeExpirationPolicy(
        final ExpirationPolicy rememberMeExpirationPolicy) {
        this.rememberMeExpirationPolicy = rememberMeExpirationPolicy;
    }

    public void setSessionExpirationPolicy(final ExpirationPolicy sessionExpirationPolicy) {
        this.sessionExpirationPolicy = sessionExpirationPolicy;
    }
}
