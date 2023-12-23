package org.apereo.cas.ticket;

import org.apereo.cas.authentication.principal.Service;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * This is {@link ServiceAwareTicket}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface ServiceAwareTicket extends Ticket {
    /**
     * Gets service.
     *
     * @return the service
     */
    Service getService();
}
