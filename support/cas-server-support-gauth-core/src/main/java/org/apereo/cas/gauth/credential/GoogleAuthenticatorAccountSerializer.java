package org.apereo.cas.gauth.credential;

import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;

import org.springframework.context.ConfigurableApplicationContext;
import java.io.Serial;

/**
 * This is {@link GoogleAuthenticatorAccountSerializer}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public class GoogleAuthenticatorAccountSerializer extends AbstractJacksonBackedStringSerializer<GoogleAuthenticatorAccount> {
    @Serial
    private static final long serialVersionUID = 1466569521275630254L;

    protected GoogleAuthenticatorAccountSerializer(final ConfigurableApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public Class<GoogleAuthenticatorAccount> getTypeToSerialize() {
        return GoogleAuthenticatorAccount.class;
    }
}
