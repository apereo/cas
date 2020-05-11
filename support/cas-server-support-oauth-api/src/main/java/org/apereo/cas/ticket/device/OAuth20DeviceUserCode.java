package org.apereo.cas.ticket.device;

import org.apereo.cas.ticket.Ticket;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * This is {@link OAuth20DeviceUserCode}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface OAuth20DeviceUserCode extends Ticket {
    /**
     * Prefix generally applied to unique ids.
     */
    String PREFIX = "ODUC";

    /**
     * Indicate whether the provided user code is approved
     * by the end-user.
     *
     * @return true/false
     */
    boolean isUserCodeApproved();

    /**
     * Approve user code.
     */
    void approveUserCode();

    /**
     * Gets device code.
     *
     * @return the device code
     */
    String getDeviceCode();
}
