package org.apereo.cas;

import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;

/**
 * This is {@link ClientRegistrationRequestSerializer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class ClientRegistrationRequestSerializer extends AbstractJacksonBackedStringSerializer<ClientRegistrationRequest> {
    private static final long serialVersionUID = -4029907481854505324L;

    @Override
    protected Class<ClientRegistrationRequest> getTypeToSerialize() {
        return ClientRegistrationRequest.class;
    }

    @Override
    protected boolean isDefaultTypingEnabled() {
        return false;
    }
}
