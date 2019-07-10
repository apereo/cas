package org.apereo.cas.ticket.expiration;

import org.apereo.cas.authentication.RememberMeCredential;
import org.apereo.cas.ticket.TicketState;
import org.apereo.cas.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * Delegates to different expiration policies depending on whether remember me
 * is true or not.
 *
 * @author Scott Battaglia
 * @since 3.2.1
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Slf4j
@ToString(callSuper = true)
public class RememberMeDelegatingExpirationPolicy extends BaseDelegatingExpirationPolicy {
    /**
     * Remember-Me policy name.
     */
    public static final String POLICY_NAME_REMEMBER_ME = "REMEMBER_ME";
    private static final long serialVersionUID = -2735975347698196127L;


    @Override
    protected String getExpirationPolicyNameFor(final TicketState ticketState) {
        val attrs = ticketState.getAuthentication().getAttributes();
        val rememberMeRes = CollectionUtils.firstElement(attrs.get(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME));
        if (rememberMeRes.isEmpty()) {
            return POLICY_NAME_DEFAULT;
        }
        val b = (Boolean) rememberMeRes.get();
        if (b.equals(Boolean.FALSE)) {
            LOGGER.trace("Ticket is not associated with a remember-me authentication.");
            return POLICY_NAME_DEFAULT;
        }
        return POLICY_NAME_REMEMBER_ME;
    }
}
