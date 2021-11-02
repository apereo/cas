package org.apereo.cas.util.cipher;

import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.crypto.DecryptionException;
import org.apereo.cas.util.gen.Base64RandomStringGenerator;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.OctJwkGenerator;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.spec.AlgorithmParameterSpec;

/**
 * This is {@link BaseBinaryCipherExecutor}.
 * <p>
 * A implementation that is based on algorithms
 * provided by the default platform's JCE. By default AES encryption is
 * used.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
@Getter
@Setter
public abstract class BaseBinaryCipherExecutor extends AbstractCipherExecutor<byte[], byte[]> {
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;

    private static final int IV_LENGTH = 16;

    private static final int MINIMUM_ENCRYPTION_KEY_LENGTH = 32;

    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";

    /**
     * Name of the cipher/component whose keys are generated here.
     */
    protected final String cipherName;

    private final SecretKeySpec encryptionKey;

    private final AlgorithmParameterSpec parameterSpec;

    /**
     * Secret key IV algorithm. Default is {@code AES}.
     */
    private String secretKeyAlgorithm = "AES";

    private byte[] encryptionSecretKey;

    protected BaseBinaryCipherExecutor(final String encryptionSecretKey, final String signingSecretKey,
                                       final int signingKeySize, final int encryptionKeySize,
                                       final String cipherName) {
        this.cipherName = cipherName;
        ensureSigningKeyExists(signingSecretKey, signingKeySize);
        ensureEncryptionKeyExists(encryptionSecretKey, encryptionKeySize);
        this.encryptionKey = new SecretKeySpec(this.encryptionSecretKey, this.secretKeyAlgorithm);
        this.parameterSpec = buildParameterSpec(encryptionKeySize);
    }

    private static String generateOctetJsonWebKeyOfSize(final int size) {
        val octetKey = OctJwkGenerator.generateJwk(size);
        val params = octetKey.toParams(JsonWebKey.OutputControlLevel.INCLUDE_SYMMETRIC);
        return params.get("k").toString();
    }

    private static AlgorithmParameterSpec buildParameterSpec(final int encryptionKeySize) {
        if (encryptionKeySize <= MINIMUM_ENCRYPTION_KEY_LENGTH) {
            val iv = new byte[IV_LENGTH];
            RandomUtils.getNativeInstance().nextBytes(iv);
            return new IvParameterSpec(iv);
        }

        val iv = new byte[GCM_IV_LENGTH];
        RandomUtils.getNativeInstance().nextBytes(iv);
        return new GCMParameterSpec(GCM_TAG_LENGTH, iv);
    }

    @Override
    @SneakyThrows
    public byte[] encode(final byte[] value, final Object[] parameters) {
        val aesCipher = Cipher.getInstance(CIPHER_ALGORITHM);
        aesCipher.init(Cipher.ENCRYPT_MODE, this.encryptionKey, this.parameterSpec);
        val result = aesCipher.doFinal(value);
        return sign(result);
    }

    @Override
    public byte[] decode(final byte[] value, final Object[] parameters) {
        try {
            val verifiedValue = verifySignature(value);
            val aesCipher = Cipher.getInstance(CIPHER_ALGORITHM);
            aesCipher.init(Cipher.DECRYPT_MODE, this.encryptionKey, this.parameterSpec);
            return aesCipher.doFinal(verifiedValue);
        } catch (final Exception e) {
            if (LOGGER.isTraceEnabled()) {
                throw new DecryptionException(e);
            }
            throw new DecryptionException();
        }
    }

    /**
     * Gets encryption key setting.
     *
     * @return the encryption key setting
     */
    protected abstract String getEncryptionKeySetting();

    /**
     * Gets signing key setting.
     *
     * @return the signing key setting
     */
    protected abstract String getSigningKeySetting();

    @SneakyThrows
    private void ensureEncryptionKeyExists(final String encryptionSecretKey, final int encryptionKeySize) {
        final byte[] genEncryptionKey;
        if (StringUtils.isBlank(encryptionSecretKey)) {
            LOGGER.warn("Secret key for encryption is not defined under [{}]. CAS will attempt to auto-generate the encryption key",
                getEncryptionKeySetting());

            if (encryptionKeySize <= MINIMUM_ENCRYPTION_KEY_LENGTH) {
                val key = new Base64RandomStringGenerator(encryptionKeySize).getNewString();
                LOGGER.warn("Generated encryption key [{}] of size [{}]. The generated key MUST be added to CAS settings under setting [{}].",
                    key, encryptionKeySize, getEncryptionKeySetting());
                genEncryptionKey = EncodingUtils.decodeBase64(key);
            } else {
                val keyGenerator = KeyGenerator.getInstance(this.secretKeyAlgorithm);
                keyGenerator.init(encryptionKeySize);
                val secretKey = keyGenerator.generateKey();
                genEncryptionKey = secretKey.getEncoded();
                LOGGER.warn("Generated encryption key [{}] of size [{}]. The generated key MUST be added to CAS settings under setting [{}].",
                    EncodingUtils.encodeBase64(genEncryptionKey), encryptionKeySize, getEncryptionKeySetting());
            }
        } else if (encryptionKeySize <= MINIMUM_ENCRYPTION_KEY_LENGTH) {
            val base64 = EncodingUtils.isBase64(encryptionSecretKey);
            val key = base64 ? EncodingUtils.decodeBase64(encryptionSecretKey) : ArrayUtils.EMPTY_BYTE_ARRAY;
            if (base64 && key.length == encryptionKeySize) {
                LOGGER.trace("Secret key for encryption defined under [{}] is Base64 encoded.", getEncryptionKeySetting());
                genEncryptionKey = key;
            } else if (encryptionSecretKey.length() != encryptionKeySize) {
                LOGGER.warn("Secret key for encryption defined under [{}] is Base64 encoded but the size does not match the key size [{}].",
                    getEncryptionKeySetting(), encryptionKeySize);
                genEncryptionKey = encryptionSecretKey.getBytes(StandardCharsets.UTF_8);
            } else {
                LOGGER.warn("Secret key for encryption defined under [{}] is not Base64 encoded. Clear the setting to regenerate (Recommended) or replace with"
                            + " [{}].", getEncryptionKeySetting(), EncodingUtils.encodeBase64(encryptionSecretKey));
                genEncryptionKey = encryptionSecretKey.getBytes(StandardCharsets.UTF_8);
            }
        } else {
            genEncryptionKey = EncodingUtils.decodeBase64(encryptionSecretKey);
        }
        this.encryptionSecretKey = genEncryptionKey;
    }

    private void ensureSigningKeyExists(final String signingSecretKey, final int signingKeySize) {
        var signingKeyToUse = signingSecretKey;
        if (StringUtils.isBlank(signingKeyToUse)) {
            LOGGER.warn("Secret key for signing is not defined under [{}]. CAS will attempt to auto-generate the signing key",
                getSigningKeySetting());
            signingKeyToUse = generateOctetJsonWebKeyOfSize(signingKeySize);
            LOGGER.warn("Generated signing key [{}] of size [{}]. The generated key MUST be added to CAS settings under setting [{}].",
                signingKeyToUse, signingKeySize, getSigningKeySetting());
        }
        configureSigningKey(signingKeyToUse);
    }

}
