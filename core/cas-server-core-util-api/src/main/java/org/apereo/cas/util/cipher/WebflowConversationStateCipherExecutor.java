package org.apereo.cas.util.cipher;


import lombok.NoArgsConstructor;

/**
 * This is {@link WebflowConversationStateCipherExecutor}, that reads webflow keys
 * from CAS configuration and presents a cipher.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@NoArgsConstructor
public class WebflowConversationStateCipherExecutor extends BaseBinaryCipherExecutor {

    public WebflowConversationStateCipherExecutor(final String secretKeyEncryption, final String secretKeySigning,
                                                  final String secretKeyAlg, final int signingKeySize,
                                                  final int encryptionKeySize, final String cipherName) {
        super(secretKeyEncryption, secretKeySigning, signingKeySize, encryptionKeySize, cipherName);
        setSecretKeyAlgorithm(secretKeyAlg);
    }

    public WebflowConversationStateCipherExecutor(final String encryptionSecretKey, final String signingSecretKey,
                                                  final String secretKeyAlg, final int signingKeySize,
                                                  final int encryptionKeySize) {
        this(encryptionSecretKey, signingSecretKey, secretKeyAlg, signingKeySize, encryptionKeySize, "webflow");
    }

    @Override
    public String getName() {
        return "Spring Webflow Session State Management";
    }

    @Override
    public String getEncryptionKeySetting() {
        return "cas." + this.cipherName + ".crypto.encryption.key";
    }

    @Override
    public String getSigningKeySetting() {
        return "cas." + this.cipherName + ".crypto.signing.key";
    }
}
