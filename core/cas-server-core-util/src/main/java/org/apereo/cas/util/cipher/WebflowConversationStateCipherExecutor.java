package org.apereo.cas.util.cipher;

/**
 * This is {@link WebflowConversationStateCipherExecutor}, that reads webflow keys
 * from CAS configuration and presents a cipher.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class WebflowConversationStateCipherExecutor extends BaseBinaryCipherExecutor {

    /**
     * Instantiates a new webflow cipher executor.
     *
     * @param secretKeyEncryption the secret key encryption
     * @param secretKeySigning    the secret key signing
     * @param secretKeyAlg        the secret key alg
     * @param signingKeySize      the signing key size
     * @param encryptionKeySize   the encryption key size
     */
    public WebflowConversationStateCipherExecutor(final String secretKeyEncryption,
                                                  final String secretKeySigning,
                                                  final String secretKeyAlg,
                                                  final int signingKeySize,
                                                  final int encryptionKeySize){
        super(secretKeyEncryption, secretKeySigning, signingKeySize, encryptionKeySize);
        setSecretKeyAlgorithm(secretKeyAlg);
    }

    @Override
    public String getName() {
        return "Spring Webflow";
    }
}
