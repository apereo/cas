package org.apereo.cas.ticket.device;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.AbstractTicket;
import org.apereo.cas.ticket.ExpirationPolicy;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * This is {@link OAuth20DefaultDeviceToken}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Entity
@DiscriminatorValue(OAuth20DeviceToken.PREFIX)
@NoArgsConstructor(force = true)
@Getter
public class OAuth20DefaultDeviceToken extends AbstractTicket implements OAuth20DeviceToken {
    private static final long serialVersionUID = 2339545346159721563L;

    private final Service service;

    private String userCode;

    public OAuth20DefaultDeviceToken(final String id, final Service service,
                                     final ExpirationPolicy expirationPolicy) {
        super(id, expirationPolicy);
        this.service = service;
    }

    @Override
    public String getPrefix() {
        return OAuth20DeviceToken.PREFIX;
    }

    @Override
    public void assignUserCode(final OAuth20DeviceUserCode userCode) {
        this.userCode = userCode.getId();
    }
}
