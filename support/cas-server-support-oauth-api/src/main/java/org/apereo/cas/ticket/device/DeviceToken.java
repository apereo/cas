package org.apereo.cas.ticket.device;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.Ticket;

/**
 * This is {@link DeviceToken}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface DeviceToken extends Ticket {
    /**
     * Prefix generally applied to unique ids.
     */
    String PREFIX = "OT";

    /**
     * Gets device code.
     *
     * @return the device code
     */
    String getDeviceCode();

    /**
     * Gets user code.
     *
     * @return the user code
     */
    String getUserCode();

    /**
     * Gets service.
     *
     * @return the service
     */
    Service getService();

    /**
     * Indicate whether the provided user code is approved
     * by the end-user.
     *
     * @return the boolean
     */
    boolean isUserCodeApproved();

    /**
     * Approve user code.
     */
    void approveUserCode();
}
