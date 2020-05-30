package org.apereo.cas.util.crypto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.io.Resource;

import javax.crypto.Cipher;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 * FactoryBean for creating a public key from a file.
 *
 * @author Scott Battaglia
 * @author Misagh Moayyed
 * @since 3.1
 */
@Slf4j
@ToString(callSuper = true)
@Getter
@RequiredArgsConstructor
public class PublicKeyFactoryBean extends AbstractFactoryBean<PublicKey> {
    private static final String[] ALGORITHMS = {"RSA", "EC", "DSA"};
    private final Resource resource;

    /**
     * Initialize cipher based on service public key.
     *
     * @return the false if no public key is found
     * or if cipher cannot be initialized, etc.
     */
    public Cipher toCipher() {
        try {
            val publicKey = getObject();
            if (publicKey != null) {
                val algorithm = publicKey.getAlgorithm();
                val cipher = Cipher.getInstance(algorithm);
                cipher.init(Cipher.ENCRYPT_MODE, publicKey);
                LOGGER.trace("Initialized cipher in encrypt-mode via the public key algorithm [{}]", algorithm);
                return cipher;
            }
        } catch (final Exception e) {
            LOGGER.warn("Cipher could not be initialized. Error [{}]", e.getMessage());
        }
        return null;
    }

    @Override
    public Class getObjectType() {
        return PublicKey.class;
    }

    @Override
    protected PublicKey createInstance() throws Exception {
        var key = readPemPublicKey();
        if (key == null) {
            LOGGER.debug("Key [{}] is not in PEM format. Trying next...", this.resource);
            key = readDERPublicKey();
        }
        return key;
    }

    /**
     * Read pem public key.
     *
     * @return the public key
     * @throws Exception the exception
     */
    protected PublicKey readPemPublicKey() throws Exception {
        try (val reader = new PemReader(new InputStreamReader(this.resource.getInputStream(), StandardCharsets.UTF_8))) {
            val pemObject = reader.readPemObject();
            if (pemObject != null) {
                val content = pemObject.getContent();
                val pubSpec = new X509EncodedKeySpec(content);
                for (val algorithm : ALGORITHMS) {
                    try {
                        val factory = KeyFactory.getInstance(algorithm);
                        return factory.generatePublic(pubSpec);
                    } catch (final InvalidKeySpecException e) {
                        continue;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Read der public key.
     *
     * @return the public key
     * @throws Exception the exception
     */
    protected PublicKey readDERPublicKey() throws Exception {
        try (val pubKey = this.resource.getInputStream()) {
            val bytes = new byte[(int) this.resource.contentLength()];
            pubKey.read(bytes);
            val pubSpec = new X509EncodedKeySpec(bytes);
            for (val algorithm : ALGORITHMS) {
                try {
                    val factory = KeyFactory.getInstance(algorithm);
                    LOGGER.debug("Creating public key instance from [{}] using [{}]", this.resource.getFilename(), algorithm);
                    return factory.generatePublic(pubSpec);
                } catch (final InvalidKeySpecException e) {
                    continue;
                }
            }
            throw new InvalidKeySpecException("Unsupported key algorithm");
        }
    }
}
