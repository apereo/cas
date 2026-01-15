package org.apereo.cas.ticket.device;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.AbstractTicket;
import org.apereo.cas.ticket.ExpirationPolicy;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This is {@link OAuth20DefaultDeviceUserCode}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@NoArgsConstructor(force = true)
@Getter
@Setter
public class OAuth20DefaultDeviceUserCode extends AbstractTicket implements OAuth20DeviceUserCode {
    @Serial
    private static final long serialVersionUID = 2339545346159721563L;

    private boolean userCodeApproved;

    private final Service service;
    
    public OAuth20DefaultDeviceUserCode(final String id, final Service service, final ExpirationPolicy expirationPolicy) {
        super(id, expirationPolicy);
        this.service = service;
    }

    @Override
    public String getPrefix() {
        return OAuth20DeviceUserCode.PREFIX;
    }

}
