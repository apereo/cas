package org.apereo.cas.ticket.expiration;

import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.ticket.TicketState;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * Delegates to different expiration policies depending on whether surrogate
 * is true or not.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Slf4j
@ToString(callSuper = true)
public class SurrogateSessionExpirationPolicy extends BaseDelegatingExpirationPolicy {
    /**
     * Surrogate policy name.
     */
    public static final String POLICY_NAME_SURROGATE = "SURROGATE";

    private static final long serialVersionUID = -2735975347698196127L;

    @Override
    protected String getExpirationPolicyNameFor(final TicketState ticketState) {
        val attributes = ticketState.getAuthentication().getAttributes();
        if (attributes.containsKey(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_PRINCIPAL)
            && attributes.containsKey(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_USER)) {
            LOGGER.trace("Ticket is associated with a surrogate authentication.");
            return POLICY_NAME_SURROGATE;
        }
        LOGGER.trace("Ticket is not associated with a surrogate authentication.");
        return POLICY_NAME_DEFAULT;
    }
}
