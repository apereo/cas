package org.apereo.cas.oidc.dynareg;

import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;

import java.io.Serial;


/**
 * This is {@link OidcClientRegistrationRequestSerializer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OidcClientRegistrationRequestSerializer extends AbstractJacksonBackedStringSerializer<OidcClientRegistrationRequest> {
    @Serial
    private static final long serialVersionUID = -4029907481854505324L;

    @Override
    public Class<OidcClientRegistrationRequest> getTypeToSerialize() {
        return OidcClientRegistrationRequest.class;
    }

    @Override
    protected boolean isDefaultTypingEnabled() {
        return false;
    }
}
