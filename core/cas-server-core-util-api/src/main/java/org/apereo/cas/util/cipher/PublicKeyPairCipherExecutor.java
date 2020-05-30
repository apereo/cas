package org.apereo.cas.util.cipher;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;

import java.io.Serializable;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * This is {@link PublicKeyPairCipherExecuto}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
public class PublicKeyPairCipherExecutor extends BaseStringCipherExecutor {
    private final PrivateKey privateKeySigning;
    private final PublicKey publicKeySigning;

    private final PrivateKey privateKeyEncryption;
    private final PublicKey publicKeyEncryption;

    public PublicKeyPairCipherExecutor(final KeyPair signing, final KeyPair encryption) {
        this(signing.getPrivate(), signing.getPublic(), encryption.getPrivate(), encryption.getPublic());
    }

    public PublicKeyPairCipherExecutor(final KeyPair signing) {
        this(signing.getPrivate(), signing.getPublic(), null, null);
    }

    public PublicKeyPairCipherExecutor(final String privateKeySigning, final String publicKeySigning,
                                    final String privateKeyEncryption, final String publicKeyEncryption) {
        this.privateKeySigning = extractPrivateKeyFromResource(privateKeySigning);
        this.publicKeySigning = extractPublicKeyFromResource(publicKeySigning);

        this.privateKeyEncryption = StringUtils.isNotBlank(privateKeyEncryption) ? extractPrivateKeyFromResource(privateKeyEncryption) : null;
        this.publicKeyEncryption = StringUtils.isNotBlank(publicKeyEncryption) ? extractPublicKeyFromResource(publicKeyEncryption) : null;
    }

    public PublicKeyPairCipherExecutor(final String privateKeySigning, final String publicKeySigning) {
        this(privateKeySigning, publicKeySigning, null, null);
    }

    @Override
    public String encode(final Serializable value, final Object[] parameters) {
        configureSigningParametersForEncoding();
        configureEncryptionParametersForEncoding();
        return super.encode(value, parameters);
    }

    @Override
    public String decode(final Serializable value, final Object[] parameters) {
        configureSigningParametersForDecoding();
        configureEncryptionParametersForDecoding();
        return super.decode(value, parameters);
    }

    private void configureSigningParametersForDecoding() {
        setSigningKey(publicKeySigning);
    }

    private void configureEncryptionParametersForDecoding() {
        setSecretKeyEncryptionKey(privateKeyEncryption);
        setContentEncryptionAlgorithmIdentifier(ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
        String keyManagementAlgorithm = null;
        if (publicKeyEncryption != null) {
            keyManagementAlgorithm = KEY_MANAGEMENT_ALGORITHM_IDENTIFIERS_MAP.get(privateKeyEncryption.getAlgorithm());
            if (keyManagementAlgorithm == null) {
                throw new IllegalArgumentException("Unsupported private key");
            }
        }
        setEncryptionAlgorithm(keyManagementAlgorithm);
    }

    private void configureEncryptionParametersForEncoding() {
        setSecretKeyEncryptionKey(publicKeyEncryption);
        setContentEncryptionAlgorithmIdentifier(ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
        String keyManagementAlgorithm = null;
        if (publicKeyEncryption != null) {
            keyManagementAlgorithm = KEY_MANAGEMENT_ALGORITHM_IDENTIFIERS_MAP.get(publicKeyEncryption.getAlgorithm());
            if (keyManagementAlgorithm == null) {
                throw new IllegalArgumentException("Unsupported public key");
            }
        }
        setEncryptionAlgorithm(keyManagementAlgorithm);
    }

    private void configureSigningParametersForEncoding() {
        setSigningKey(privateKeySigning);
    }
}
