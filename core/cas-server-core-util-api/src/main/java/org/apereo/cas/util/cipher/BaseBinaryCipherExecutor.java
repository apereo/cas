package org.apereo.cas.util.cipher;

import module java.base;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.crypto.DecryptionException;
import org.apereo.cas.util.crypto.PropertyBoundCipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.gen.Base64RandomStringGenerator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.OctJwkGenerator;

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
@Accessors(chain = true)
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public abstract class BaseBinaryCipherExecutor extends AbstractCipherExecutor<byte[], byte[]>
    implements PropertyBoundCipherExecutor<byte[], byte[]> {
    private static final int GCM_TAG_LENGTH = 128;

    private static final int MINIMUM_ENCRYPTION_KEY_LENGTH = 32;

    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";

    /**
     * Name of the cipher/component whose keys are generated here.
     */
    protected String cipherName;

    private SecretKeySpec encryptionKey;

    private AlgorithmParameterSpec parameterSpec;

    /**
     * Secret key IV algorithm. Default is {@code AES}.
     */
    private String secretKeyAlgorithm = "AES";

    private byte[] encryptionSecretKey;

    private boolean signingEnabled = true;

    protected BaseBinaryCipherExecutor(final String encryptionSecretKey, final String signingSecretKey,
                                       final int signingKeySize, final int encryptionKeySize,
                                       final String cipherName) {
        this.cipherName = cipherName;
        try (val executor = Executors.newVirtualThreadPerTaskExecutor()) {
            val signingCertTask = Unchecked.runnable(() -> ensureSigningKeyExists(signingSecretKey, signingKeySize));
            val encryptionCertTask = Unchecked.runnable(() -> ensureEncryptionKeyExists(encryptionSecretKey, encryptionKeySize));
            executor.execute(signingCertTask);
            executor.execute(encryptionCertTask);
        }
        this.encryptionKey = new SecretKeySpec(this.encryptionSecretKey, this.secretKeyAlgorithm);
        this.parameterSpec = buildParameterSpec(encryptionKeySize);
    }

    private static String generateOctetJsonWebKeyOfSize(final int size) {
        val octetKey = OctJwkGenerator.generateJwk(size);
        val params = octetKey.toParams(JsonWebKey.OutputControlLevel.INCLUDE_SYMMETRIC);
        return params.get("k").toString();
    }

    @Override
    public byte[] encode(final byte[] value, final Object[] parameters) {
        return FunctionUtils.doUnchecked(() -> {
            val aesCipher = Cipher.getInstance(CIPHER_ALGORITHM);
            aesCipher.init(Cipher.ENCRYPT_MODE, this.encryptionKey, this.parameterSpec);
            val result = aesCipher.doFinal(value);
            return signingEnabled ? sign(result, getSigningKey()) : result;
        });
    }

    @Override
    public byte[] decode(final byte[] value, final Object[] parameters) {
        try {
            val verifiedValue = signingEnabled ? verifySignature(value, getSigningKey()) : value;
            val aesCipher = Cipher.getInstance(CIPHER_ALGORITHM);
            aesCipher.init(Cipher.DECRYPT_MODE, this.encryptionKey, this.parameterSpec);
            return aesCipher.doFinal(verifiedValue);
        } catch (final Exception e) {
            throw LOGGER.isTraceEnabled() ? new DecryptionException(e) : new DecryptionException();
        }
    }

    @Override
    public PropertyBoundCipherExecutor<byte[], byte[]> withSigningDisabled() {
        val cipher = new BaseBinaryCipherExecutor() {
        };
        cipher.setCipherName(getCipherName());
        cipher.setEncryptionKey(getEncryptionKey());
        cipher.setParameterSpec(getParameterSpec());
        cipher.setCommonHeaders(getCommonHeaders());
        cipher.setEncryptionOpHeaders(getEncryptionOpHeaders());
        cipher.setEncryptionSecretKey(getEncryptionSecretKey());
        cipher.setSecretKeyAlgorithm(getSecretKeyAlgorithm());
        cipher.setSigningEnabled(false);
        cipher.setSigningKey(getSigningKey());
        cipher.setSigningOpHeaders(getSigningOpHeaders());
        cipher.setSigningAlgorithm(getSigningAlgorithm());
        return cipher;
    }
    
    private AlgorithmParameterSpec buildParameterSpec(final int encryptionKeySize) {
        val iv = new byte[encryptionSecretKey.length];
        if (encryptionKeySize > MINIMUM_ENCRYPTION_KEY_LENGTH) {
            System.arraycopy(this.encryptionSecretKey, 0, iv, 0, encryptionSecretKey.length);
            return new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        }
        return new IvParameterSpec(iv);
    }

    private void ensureEncryptionKeyExists(final String encryptionSecretKey, final int encryptionKeySize) {
        final byte[] genEncryptionKey;
        if (StringUtils.isBlank(encryptionSecretKey)) {
            LOGGER.warn("Secret key for encryption is undefined under [{}]. CAS will attempt to auto-generate the encryption key", getEncryptionKeySetting());

            if (encryptionKeySize <= MINIMUM_ENCRYPTION_KEY_LENGTH) {
                val key = new Base64RandomStringGenerator(encryptionKeySize).getNewString();
                val prop = String.format("%s=%s", getEncryptionKeySetting(), key);
                issueWarningToAddKeyToSettings("encryption", encryptionKeySize, key, prop);
                genEncryptionKey = EncodingUtils.decodeBase64(key);
            } else {
                val keyGenerator = FunctionUtils.doUnchecked(() -> KeyGenerator.getInstance(this.secretKeyAlgorithm));
                keyGenerator.init(encryptionKeySize);
                val secretKey = keyGenerator.generateKey();
                genEncryptionKey = secretKey.getEncoded();
                val encodedKey = EncodingUtils.encodeBase64(genEncryptionKey);
                val prop = String.format("%s=%s", getEncryptionKeySetting(), encodedKey);
                issueWarningToAddKeyToSettings("encryption", encryptionKeySize, encodedKey, prop);
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
            val prop = String.format("%s=%s", getSigningKeySetting(), signingKeyToUse);
            issueWarningToAddKeyToSettings("signing", signingKeySize, signingKeyToUse, prop);
        }
        configureSigningKey(signingKeyToUse);
    }
    
    //CHECKSTYLE:OFF
    private static void issueWarningToAddKeyToSettings(final String keyType, final int encryptionKeySize, final String key, final String prop) {
        LOGGER.warn("Generated {} key [{}] of size [{}]. The generated key MUST be added to CAS settings:\n\n\t{}\n\n",
            keyType, key, encryptionKeySize, prop);
    }
    //CHECKSTYLE:ON
    
}
