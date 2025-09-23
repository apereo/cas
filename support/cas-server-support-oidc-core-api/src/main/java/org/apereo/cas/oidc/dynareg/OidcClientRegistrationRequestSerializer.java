package org.apereo.cas.oidc.dynareg;

import org.apereo.cas.util.serialization.BaseJacksonSerializer;

import org.springframework.context.ConfigurableApplicationContext;
import java.io.Serial;


/**
 * This is {@link OidcClientRegistrationRequestSerializer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OidcClientRegistrationRequestSerializer extends BaseJacksonSerializer<OidcClientRegistrationRequest> {
    @Serial
    private static final long serialVersionUID = -4029907481854505324L;

    public OidcClientRegistrationRequestSerializer(final ConfigurableApplicationContext applicationContext) {
        super(applicationContext, OidcClientRegistrationRequest.class);
    }
    
    @Override
    protected boolean isDefaultTypingEnabled() {
        return false;
    }
}
