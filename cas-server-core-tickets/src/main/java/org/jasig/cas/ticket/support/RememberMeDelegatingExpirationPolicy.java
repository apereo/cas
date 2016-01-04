package org.jasig.cas.ticket.support;

import org.jasig.cas.authentication.RememberMeCredential;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.TicketState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;

/**
 * Delegates to different expiration policies depending on whether remember me
 * is true or not.
 *
 * @author Scott Battaglia
 * @since 3.2.1
 *
 */
@Component("rememberMeDelegatingExpirationPolicy")
public final class RememberMeDelegatingExpirationPolicy extends AbstractCasExpirationPolicy {

    /** Serialization support. */
    private static final long serialVersionUID = -2735975347698196127L;

    /**
     * The Logger instance for this class. Using a transient instance field for the Logger doesn't work, on object
     * deserialization the field is null.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RememberMeDelegatingExpirationPolicy.class);


    @Nullable
    @Autowired(required=false)
    @Qualifier("rememberMeExpirationPolicy")
    private ExpirationPolicy rememberMeExpirationPolicy;

    @Nullable
    @Autowired(required=false)
    @Qualifier("sessionExpirationPolicy")
    private ExpirationPolicy sessionExpirationPolicy;

    /**
     * Instantiates a new Remember me delegating expiration policy.
     */
    public RememberMeDelegatingExpirationPolicy() {}

    @PostConstruct
    private void postConstruct() {
        if (rememberMeExpirationPolicy != null) {
            LOGGER.debug("Using remember-me expiration policy of {}", rememberMeExpirationPolicy);
        }
        if (sessionExpirationPolicy != null) {
            LOGGER.debug("Using session expiration policy of {}", sessionExpirationPolicy);
        }
    }

    @Override
    public boolean isExpired(final TicketState ticketState) {
        if (this.rememberMeExpirationPolicy != null && this.sessionExpirationPolicy != null) {

            final Boolean b = (Boolean) ticketState.getAuthentication().getAttributes().
                    get(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME);

            if (b == null || b.equals(Boolean.FALSE)) {
                LOGGER.debug("Ticket is not associated with a remember-me authentication. Invoking {}", sessionExpirationPolicy);
                return this.sessionExpirationPolicy.isExpired(ticketState);
            }

            LOGGER.debug("Ticket is associated with a remember-me authentication. Invoking {}", rememberMeExpirationPolicy);
            return this.rememberMeExpirationPolicy.isExpired(ticketState);
        }
        LOGGER.warn("No expiration policy settings are defined");
        return false;
    }

    public void setRememberMeExpirationPolicy(
        final ExpirationPolicy rememberMeExpirationPolicy) {
        this.rememberMeExpirationPolicy = rememberMeExpirationPolicy;
    }

    public void setSessionExpirationPolicy(final ExpirationPolicy sessionExpirationPolicy) {
        this.sessionExpirationPolicy = sessionExpirationPolicy;
    }
}
