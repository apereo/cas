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
     * @param cipherName          the cipher name
     */
    public WebflowConversationStateCipherExecutor(final String secretKeyEncryption, final String secretKeySigning,
                                                  final String secretKeyAlg, final int signingKeySize,
                                                  final int encryptionKeySize, final String cipherName){
        super(secretKeyEncryption, secretKeySigning, signingKeySize, encryptionKeySize, cipherName);
        setSecretKeyAlgorithm(secretKeyAlg);
    }

    public WebflowConversationStateCipherExecutor(final String encryptionSecretKey, final String signingSecretKey,
                                                  final String secretKeyAlg, final int signingKeySize, final int encryptionKeySize) {
        this(encryptionSecretKey, signingSecretKey, secretKeyAlg, signingKeySize, encryptionKeySize, "webflow");
    }

    @Override
    public String getName() {
        return "Spring Webflow Session State Management";
    }

    @Override
    protected String getEncryptionKeySetting() {
        return "cas." + this.cipherName + ".crypto.encryption.key";
    }

    @Override
    protected String getSigningKeySetting() {
        return "cas." + this.cipherName + ".crypto.signing.key";
    }
}
