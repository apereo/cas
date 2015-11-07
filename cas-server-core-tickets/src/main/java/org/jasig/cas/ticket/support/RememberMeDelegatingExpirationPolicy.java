package org.jasig.cas.ticket.support;

import org.jasig.cas.authentication.RememberMeCredential;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.TicketState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

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

    @Override
    public boolean isExpired(final TicketState ticketState) {
        if (this.rememberMeExpirationPolicy != null
            && this.sessionExpirationPolicy != null) {

            final Boolean b = (Boolean) ticketState.getAuthentication().getAttributes().
                get(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME);

            if (b == null || b.equals(Boolean.FALSE)) {
                return this.sessionExpirationPolicy.isExpired(ticketState);
            }

            return this.rememberMeExpirationPolicy.isExpired(ticketState);
        }
        logger.warn("No expiration policy settings are defined");
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
