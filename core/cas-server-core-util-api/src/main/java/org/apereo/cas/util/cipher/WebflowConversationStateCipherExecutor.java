package org.apereo.cas.util.cipher;


import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.util.crypto.CipherExecutor;
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

    public WebflowConversationStateCipherExecutor(final String secretKeyAlg, final int signingKeySize,
                                                  final int encryptionKeySize) {
        this(null, null, secretKeyAlg, signingKeySize, encryptionKeySize, "webflow");
    }

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

    /**
     * From properties to cipher executor.
     *
     * @param crypto the crypto
     * @return the cipher executor
     */
    public static CipherExecutor from(final EncryptionRandomizedSigningJwtCryptographyProperties crypto) {
        return new WebflowConversationStateCipherExecutor(
            crypto.getEncryption().getKey(),
            crypto.getSigning().getKey(),
            crypto.getAlg(),
            crypto.getSigning().getKeySize(),
            crypto.getEncryption().getKeySize()
        );
    }
}
