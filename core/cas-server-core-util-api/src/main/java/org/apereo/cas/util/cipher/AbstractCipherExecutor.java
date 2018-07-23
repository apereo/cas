package org.apereo.cas.util.cipher;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.crypto.PrivateKeyFactoryBean;
import org.apereo.cas.util.crypto.PublicKeyFactoryBean;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.keys.AesKey;
import org.jose4j.keys.RsaKeyUtil;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;

/**
 * Abstract cipher to provide common operations around signing objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
@Setter
@NoArgsConstructor
public abstract class AbstractCipherExecutor<T, R> implements CipherExecutor<T, R> {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private Key signingKey;

    /**
     * Extract private key from resource private key.
     *
     * @param signingSecretKey the signing secret key
     * @return the private key
     */
    @SneakyThrows
    public static PrivateKey extractPrivateKeyFromResource(final String signingSecretKey) {
        LOGGER.debug("Attempting to extract private key...");
        val resource = ResourceUtils.getResourceFrom(signingSecretKey);
        val factory = new PrivateKeyFactoryBean();
        factory.setAlgorithm(RsaKeyUtil.RSA);
        factory.setLocation(resource);
        factory.setSingleton(false);
        return factory.getObject();
    }

    /**
     * Extract public key from resource public key.
     *
     * @param secretKeyToUse the secret key to use
     * @return the public key
     */
    @SneakyThrows
    public static PublicKey extractPublicKeyFromResource(final String secretKeyToUse) {
        LOGGER.debug("Attempting to extract public key from [{}]...", secretKeyToUse);
        val resource = ResourceUtils.getResourceFrom(secretKeyToUse);
        val factory = new PublicKeyFactoryBean();
        factory.setAlgorithm(RsaKeyUtil.RSA);
        factory.setResource(resource);
        factory.setSingleton(false);
        return factory.getObject();
    }

    /**
     * Sign the array by first turning it into a base64 encoded string.
     *
     * @param value the value
     * @return the byte [ ]
     */
    protected byte[] sign(final byte[] value) {
        if (this.signingKey == null) {
            return value;
        }
        if ("RSA".equalsIgnoreCase(this.signingKey.getAlgorithm())) {
            return EncodingUtils.signJwsRSASha512(this.signingKey, value);
        }
        return EncodingUtils.signJwsHMACSha512(this.signingKey, value);

    }

    /**
     * Sets signing key. If the key provided is resolved as a private key,
     * then will create use the private key as is, and will sign values
     * using RSA. Otherwise, AES is used.
     *
     * @param signingSecretKey the signing secret key
     */
    protected void configureSigningKey(final String signingSecretKey) {
        try {
            if (ResourceUtils.doesResourceExist(signingSecretKey)) {
                configureSigningKeyFromPrivateKeyResource(signingSecretKey);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (this.signingKey == null) {
                setSigningKey(new AesKey(signingSecretKey.getBytes(StandardCharsets.UTF_8)));
                LOGGER.debug("Created signing key instance [{}] based on provided secret key", this.signingKey.getClass().getSimpleName());
            }
        }
    }

    /**
     * Configure signing key from private key resource.
     *
     * @param signingSecretKey the signing secret key
     * @throws Exception the exception
     */
    protected void configureSigningKeyFromPrivateKeyResource(final String signingSecretKey) throws Exception {
        val object = extractPrivateKeyFromResource(signingSecretKey);
        LOGGER.debug("Located signing key resource [{}]", signingSecretKey);
        setSigningKey(object);
    }

    /**
     * Verify signature.
     *
     * @param value the value
     * @return the value associated with the signature, which may have to
     * be decoded, or null.
     */
    protected byte[] verifySignature(final byte[] value) {
        if (this.signingKey == null) {
            return value;
        }
        return EncodingUtils.verifyJwsSignature(this.signingKey, value);
    }

    @Override
    public boolean isEnabled() {
        return this.signingKey != null;
    }
}
