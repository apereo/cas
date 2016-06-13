package org.apereo.cas;

import org.apereo.cas.util.BinaryCipherExecutor;

/**
 * This is {@link WebflowCipherExecutor}, that reads webflow keys
 * from CAS configuration and presents a cipher.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class WebflowCipherExecutor extends BinaryCipherExecutor {

    /**
     * Instantiates a new webflow cipher executor.
     *
     * @param secretKeyEncryption the secret key encryption
     * @param secretKeySigning    the secret key signing
     * @param secretKeyAlg        the secret key alg
     * @param signingKeySize      the signing key size
     * @param encryptionKeySize   the encryption key size
     */
    public WebflowCipherExecutor(final String secretKeyEncryption,
                                 final String secretKeySigning,
                                 final String secretKeyAlg,
                                 final int signingKeySize,
                                 final int encryptionKeySize){
        super(secretKeyEncryption, secretKeySigning, signingKeySize, encryptionKeySize);
        setSecretKeyAlgorithm(secretKeyAlg);
    }
}
