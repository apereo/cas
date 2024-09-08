package org.apereo.cas.gauth.credential;

import org.apereo.cas.util.serialization.BaseJacksonSerializer;

import org.springframework.context.ConfigurableApplicationContext;
import java.io.Serial;

/**
 * This is {@link GoogleAuthenticatorAccountSerializer}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public class GoogleAuthenticatorAccountSerializer extends BaseJacksonSerializer<GoogleAuthenticatorAccount> {
    @Serial
    private static final long serialVersionUID = 1466569521275630254L;

    protected GoogleAuthenticatorAccountSerializer(final ConfigurableApplicationContext applicationContext) {
        super(applicationContext, GoogleAuthenticatorAccount.class);
    }
}
