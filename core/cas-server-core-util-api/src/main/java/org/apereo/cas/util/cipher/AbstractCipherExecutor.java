package org.apereo.cas.util.cipher;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.crypto.PrivateKeyFactoryBean;
import org.jose4j.keys.AesKey;
import org.jose4j.keys.RsaKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;
import java.security.Key;

/**
 * Abstract cipher to provide common operations around signing objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public abstract class AbstractCipherExecutor<T, R> implements CipherExecutor<T, R> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCipherExecutor.class);

    private Key signingKey;

    /**
     * Instantiates a new cipher executor.
     */
    protected AbstractCipherExecutor() {
    }

    /**
     * Instantiates a new cipher executor.
     *
     * @param signingSecretKey the signing key
     */
    public AbstractCipherExecutor(final String signingSecretKey) {
        setSigningKey(signingSecretKey);
    }


    /**
     * Sign the array by first turning it into a base64 encoded string.
     *
     * @param value the value
     * @return the byte [ ]
     */
    protected byte[] sign(final byte[] value) {
        return EncodingUtils.signJwsHMACSha512(this.signingKey, value);
    }

    /**
     * Sets signing key. If the key provided is resolved as a private key,
     * then will create use the private key as is, and will sign values
     * using RSA. Otherwise, AES is used.
     *
     * @param signingSecretKey the signing secret key
     */
    public void setSigningKey(final String signingSecretKey) {
        try {
            if (ResourceUtils.isFile(signingSecretKey) && ResourceUtils.doesResourceExist(signingSecretKey)) {
                final Resource resource = ResourceUtils.getResourceFrom(signingSecretKey);
                LOGGER.debug("Located signing key resource [{}]. Attempting to extract private key...", resource);

                final PrivateKeyFactoryBean factory = new PrivateKeyFactoryBean();
                factory.setAlgorithm(RsaKeyUtil.RSA);
                factory.setLocation(resource);
                factory.setSingleton(false);
                this.signingKey = factory.getObject();
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (this.signingKey == null) {
                this.signingKey = new AesKey(signingSecretKey.getBytes(StandardCharsets.UTF_8));
                LOGGER.debug("Created signing key instance [{}] based on provided secret key", this.signingKey.getClass().getSimpleName());
            }
        }
    }

    /**
     * Verify signature.
     *
     * @param value the value
     * @return the value associated with the signature, which may have to
     * be decoded, or null.
     */
    protected byte[] verifySignature(final byte[] value) {
        return EncodingUtils.verifyJwsSignature(this.signingKey, value);
    }

    @Override
    public boolean isEnabled() {
        return this.signingKey != null;
    }
}
