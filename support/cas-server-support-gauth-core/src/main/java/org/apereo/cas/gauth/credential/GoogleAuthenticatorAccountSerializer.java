package org.apereo.cas.gauth.credential;

import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;

/**
 * This is {@link GoogleAuthenticatorAccountSerializer}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public class GoogleAuthenticatorAccountSerializer extends AbstractJacksonBackedStringSerializer<GoogleAuthenticatorAccount> {
    private static final long serialVersionUID = 1466569521275630254L;

    @Override
    public Class<GoogleAuthenticatorAccount> getTypeToSerialize() {
        return GoogleAuthenticatorAccount.class;
    }
}
