package org.apereo.cas.oidc.jwks.rotation;

import module java.base;
import org.apereo.cas.support.events.AbstractCasEvent;
import lombok.ToString;
import org.apereo.inspektr.common.web.ClientInfo;

/**
 * This is {@link OidcJsonWebKeystoreRotatedEvent}, published once keys in the keystore are rotated or revoked.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@ToString(callSuper = true)
public class OidcJsonWebKeystoreRotatedEvent extends AbstractCasEvent {
    @Serial
    private static final long serialVersionUID = -2791458463217034523L;

    public OidcJsonWebKeystoreRotatedEvent(final Object source, final ClientInfo clientInfo) {
        super(source, clientInfo);
    }
}
