package org.apereo.cas.ticket.device;

import org.apereo.cas.ticket.AbstractTicket;
import org.apereo.cas.ticket.ExpirationPolicy;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * This is {@link DeviceUserCodeImpl}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Entity
@DiscriminatorValue(DeviceUserCode.PREFIX)
@NoArgsConstructor(force = true)
@Getter
public class DeviceUserCodeImpl extends AbstractTicket implements DeviceUserCode {
    private static final long serialVersionUID = 2339545346159721563L;

    private final String deviceCode;
    private boolean userCodeApproved;

    public DeviceUserCodeImpl(final String id, final String deviceCode, final ExpirationPolicy expirationPolicy) {
        super(id, expirationPolicy);
        this.deviceCode = deviceCode;
    }

    @Override
    public String getPrefix() {
        return DeviceUserCode.PREFIX;
    }

    @Override
    public void approveUserCode() {
        this.userCodeApproved = true;
    }
}
