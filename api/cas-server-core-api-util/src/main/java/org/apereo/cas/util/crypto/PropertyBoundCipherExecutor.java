package org.apereo.cas.util.crypto;

/**
 * This is {@link PropertyBoundCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public interface PropertyBoundCipherExecutor<I, O> extends CipherExecutor<I, O> {
    default String getEncryptionKeySetting() {
        return "N/A";
    }

    default String getSigningKeySetting() {
        return "N/A";
    }
}
