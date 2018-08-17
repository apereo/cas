package org.apereo.cas.util.cipher;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;

import java.io.Serializable;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * This is {@link RsaKeyPairCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
public class RsaKeyPairCipherExecutor extends BaseStringCipherExecutor {
    private final PrivateKey privateKeySigning;
    private final PublicKey publicKeySigning;

    private final PrivateKey privateKeyEncryption;
    private final PublicKey publicKeyEncryption;

    public RsaKeyPairCipherExecutor(final KeyPair signing, final KeyPair encryption) {
        this(signing.getPrivate(), signing.getPublic(), encryption.getPrivate(), encryption.getPublic());
    }

    public RsaKeyPairCipherExecutor(final KeyPair signing) {
        this(signing.getPrivate(), signing.getPublic(), null, null);
    }

    public RsaKeyPairCipherExecutor(final String privateKeySigning, final String publicKeySigning,
                                    final String privateKeyEncryption, final String publicKeyEncryption) {
        this.privateKeySigning = extractPrivateKeyFromResource(privateKeySigning);
        this.publicKeySigning = extractPublicKeyFromResource(publicKeySigning);

        this.privateKeyEncryption = StringUtils.isNotBlank(privateKeyEncryption) ? extractPrivateKeyFromResource(privateKeyEncryption) : null;
        this.publicKeyEncryption = StringUtils.isNotBlank(publicKeyEncryption) ? extractPublicKeyFromResource(publicKeyEncryption) : null;
    }

    public RsaKeyPairCipherExecutor(final String privateKeySigning, final String publicKeySigning) {
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
        setEncryptionAlgorithm(KeyManagementAlgorithmIdentifiers.RSA_OAEP_256);
    }

    private void configureEncryptionParametersForEncoding() {
        setSecretKeyEncryptionKey(publicKeyEncryption);
        setContentEncryptionAlgorithmIdentifier(ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
        setEncryptionAlgorithm(KeyManagementAlgorithmIdentifiers.RSA_OAEP_256);
    }

    private void configureSigningParametersForEncoding() {
        setSigningKey(privateKeySigning);
    }
}
