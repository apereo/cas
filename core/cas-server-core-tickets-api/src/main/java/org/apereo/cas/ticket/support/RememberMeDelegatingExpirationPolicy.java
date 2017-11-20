package org.apereo.cas.ticket.support;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apereo.cas.authentication.RememberMeCredential;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Delegates to different expiration policies depending on whether remember me
 * is true or not.
 *
 * @author Scott Battaglia
 * @since 3.2.1
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RememberMeDelegatingExpirationPolicy extends BaseDelegatingExpirationPolicy {
    private static final Logger LOGGER = LoggerFactory.getLogger(RememberMeDelegatingExpirationPolicy.class);

    private static final long serialVersionUID = -2735975347698196127L;

    /**
     * Policy types.
     */
    public enum PolicyTypes {
        /**
         * Remember me policy type.
         */
        REMEMBER_ME,
        /**
         * Default policy type.
         */
        DEFAULT
    }

    /**
     * Instantiates a new Remember me delegating expiration policy.
     *
     * @param policy the policy
     */
    @JsonCreator
    public RememberMeDelegatingExpirationPolicy(@JsonProperty("policy") final ExpirationPolicy policy) {
        super(policy);
    }

    @Override
    protected String getExpirationPolicyNameFor(final TicketState ticketState) {
        final Map<String, Object> attrs = ticketState.getAuthentication().getAttributes();
        final Boolean b = (Boolean) attrs.get(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME);

        if (b == null || b.equals(Boolean.FALSE)) {
            LOGGER.debug("Ticket is not associated with a remember-me authentication.");
            return PolicyTypes.DEFAULT.name();
        }
        return PolicyTypes.REMEMBER_ME.name();
    }
}
