package org.apereo.cas.support.oauth.services;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

import java.io.Serializable;

/**
 * This is {@link OAuth20RegisteredServiceCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class OAuth20RegisteredServiceCipherExecutor extends BaseStringCipherExecutor {
    /**
     * Prefix inserted at the beginning of a value to indicate it's encrypted.
     */
    public static final String ENCRYPTED_VALUE_PREFIX = "{cas-cipher}";

    public OAuth20RegisteredServiceCipherExecutor(final String secretKeyEncryption,
                                                  final String secretKeySigning,
                                                  final String alg,
                                                  final boolean encryptionEnabled,
                                                  final boolean signingEnabled,
                                                  final int signingKeySize,
                                                  final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, alg, encryptionEnabled,
            signingEnabled, signingKeySize, encryptionKeySize);
    }


    public OAuth20RegisteredServiceCipherExecutor() {
        super(null, null, 0, 0);
    }

    @Override
    public String getName() {
        return "OAuth Registered Service";
    }

    @Override
    public String decode(final Serializable value, final Object[] parameters) {
        var currentValue = value.toString();
        if (currentValue.startsWith(ENCRYPTED_VALUE_PREFIX)) {
            currentValue = currentValue.substring(ENCRYPTED_VALUE_PREFIX.length());
            return super.decode(currentValue, parameters);
        }
        return currentValue;
    }

    @Override
    public String encode(final Serializable value, final Object[] parameters) {
        return ENCRYPTED_VALUE_PREFIX + super.encode(value, parameters);
    }

    @Override
    protected String getEncryptionKeySetting() {
        return "cas.authn.oauth.crypto.encryption.key";
    }

    @Override
    protected String getSigningKeySetting() {
        return "cas.authn.oauth.crypto.signing.key";
    }

}
