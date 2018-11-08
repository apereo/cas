package org.apereo.cas.ticket.device;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.AbstractTicket;
import org.apereo.cas.ticket.ExpirationPolicy;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * This is {@link DeviceTokenImpl}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Entity
@DiscriminatorValue(DeviceToken.PREFIX)
@NoArgsConstructor(force = true)
@Getter
public class DeviceTokenImpl extends AbstractTicket implements DeviceToken {
    private static final long serialVersionUID = 2339545346159721563L;

    private final Service service;

    private String userCode;

    public DeviceTokenImpl(final String id, final Service service,
                           final ExpirationPolicy expirationPolicy) {
        super(id, expirationPolicy);
        this.service = service;
    }

    @Override
    public String getPrefix() {
        return DeviceToken.PREFIX;
    }

    @Override
    public void assignUserCode(final DeviceUserCode userCode) {
        this.userCode = userCode.getId();
    }
}
