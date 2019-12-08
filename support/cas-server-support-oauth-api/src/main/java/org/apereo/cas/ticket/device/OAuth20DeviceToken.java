package org.apereo.cas.ticket.device;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketState;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * This is {@link OAuth20DeviceToken}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface OAuth20DeviceToken extends Ticket, TicketState {
    /**
     * Prefix generally applied to unique ids.
     */
    String PREFIX = "ODT";

    /**
     * Gets service.
     *
     * @return the service
     */
    Service getService();

    /**
     * Gets user code.
     *
     * @return the user code
     */
    String getUserCode();

    /**
     * Instantiates a new Assign user code.
     *
     * @param userCode the user code
     */
    void assignUserCode(OAuth20DeviceUserCode userCode);
}
