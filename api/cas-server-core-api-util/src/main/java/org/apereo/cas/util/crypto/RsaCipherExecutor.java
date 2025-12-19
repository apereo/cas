package org.apereo.cas.util.crypto;

import module java.base;

/**
 * This is {@link RsaCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public interface RsaCipherExecutor {
    /**
     * Gets private key signing.
     *
     * @return the private key signing
     */
    PrivateKey getPrivateKeySigning();

    /**
     * Gets public key signing.
     *
     * @return the public key signing
     */
    PublicKey getPublicKeySigning();

    /**
     * Gets private key encryption.
     *
     * @return the private key encryption
     */
    PrivateKey getPrivateKeyEncryption();

    /**
     * Gets public key encryption.
     *
     * @return the public key encryption
     */
    PublicKey getPublicKeyEncryption();
}
