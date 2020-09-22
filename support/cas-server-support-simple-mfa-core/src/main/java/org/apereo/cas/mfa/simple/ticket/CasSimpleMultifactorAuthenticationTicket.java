package org.apereo.cas.mfa.simple.ticket;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.Ticket;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Map;

/**
 * This is {@link CasSimpleMultifactorAuthenticationTicket}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface CasSimpleMultifactorAuthenticationTicket extends Ticket {
    /**
     * MFA ticket prefix.
     */
    String PREFIX = "CASMFA";

    /**
     * Gets properties.
     *
     * @return the properties
     */
    Map<String, Object> getProperties();

    /**
     * Gets service.
     *
     * @return the service
     */
    Service getService();
}
