package org.apereo.cas.mfa.simple.ticket;

import module java.base;
import org.apereo.cas.ticket.PropertiesAwareTicket;
import org.apereo.cas.ticket.ServiceAwareTicket;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * This is {@link CasSimpleMultifactorAuthenticationTicket}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface CasSimpleMultifactorAuthenticationTicket extends ServiceAwareTicket, PropertiesAwareTicket {
    /**
     * MFA ticket prefix.
     */
    String PREFIX = "CASMFA";

    /**
     * Property that keeps the code sent to the user when the ticket is stored under another id,
     * as the stateless ticket registry does.
     */
    String PROPERTY_CODE = "code";

    /**
     * The code sent to the user for this ticket: the {@link #PROPERTY_CODE} property if present,
     * otherwise the ticket id.
     *
     * @param ticket the ticket
     * @return the code
     */
    static String getCode(final CasSimpleMultifactorAuthenticationTicket ticket) {
        return Objects.toString(ticket.getProperties().getOrDefault(PROPERTY_CODE, ticket.getId()));
    }
}
