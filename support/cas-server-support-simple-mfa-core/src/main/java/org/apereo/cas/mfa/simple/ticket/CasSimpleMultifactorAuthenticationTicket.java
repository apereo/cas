package org.apereo.cas.mfa.simple.ticket;

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
}
