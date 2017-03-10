package org.apereo.cas.util.cipher;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.util.EncodingUtils;
import org.jose4j.keys.AesKey;

import java.nio.charset.StandardCharsets;

/**
 * Abstract cipher to provide common operations around signing objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public abstract class AbstractCipherExecutor<T, R> implements CipherExecutor<T, R> {

    private AesKey signingKey;

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

    public void setSigningKey(final String signingSecretKey) {
        this.signingKey = new AesKey(signingSecretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Sign the array by first turning it into a base64 encoded string.
     *
     * @param value the value
     * @return the byte [ ]
     */
    protected byte[] sign(final byte[] value) {
        return EncodingUtils.signJws(this.signingKey, value);
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
