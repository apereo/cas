package org.apereo.cas.util.cipher;

import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.crypto.IdentifiableKey;
import org.apereo.cas.util.crypto.PrivateKeyFactoryBean;
import org.apereo.cas.util.crypto.PublicKeyFactoryBean;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.jwt.JsonWebTokenSigner;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.keys.AesKey;
import org.jose4j.keys.RsaKeyUtil;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Abstract cipher to provide common operations around signing objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Accessors(chain = true)
public abstract class AbstractCipherExecutor<T, R> implements CipherExecutor<T, R> {
    private static final BigInteger RSA_PUBLIC_KEY_EXPONENT = BigInteger.valueOf(65537);

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private Key signingKey;

    private Map<String, Object> signingOpHeaders = new LinkedHashMap<>();

    private Map<String, Object> encryptionOpHeaders = new LinkedHashMap<>();

    private Map<String, Object> commonHeaders = new LinkedHashMap<>();

    private String signingAlgorithm;

    /**
     * Extract private key from resource private key.
     *
     * @param signingSecretKey the signing secret key
     * @return the private key
     */
    public static PrivateKey extractPrivateKeyFromResource(final String signingSecretKey) {
        return FunctionUtils.doAndThrowUnchecked(() -> {
            LOGGER.debug("Attempting to extract private key...");
            val resource = ResourceUtils.getResourceFrom(signingSecretKey);
            val factory = new PrivateKeyFactoryBean();
            factory.setAlgorithm(RsaKeyUtil.RSA);
            factory.setLocation(resource);
            factory.setSingleton(false);
            return factory.getObject();
        }, e -> new IllegalArgumentException("Unable to extract private key from location %s".formatted(signingSecretKey)));
    }

    /**
     * Extract public key from resource public key.
     *
     * @param secretKeyToUse the secret key to use
     * @return the public key
     */
    public static PublicKey extractPublicKeyFromResource(final String secretKeyToUse) {
        return FunctionUtils.doUnchecked(() -> {
            LOGGER.debug("Attempting to extract public key from [{}]...", secretKeyToUse);
            val resource = ResourceUtils.getResourceFrom(secretKeyToUse);
            val factory = new PublicKeyFactoryBean(resource, RsaKeyUtil.RSA);
            factory.setSingleton(false);
            return factory.getObject();
        });
    }

    @Override
    public boolean isEnabled() {
        return this.signingKey != null;
    }

    /**
     * Sign the array by first turning it into a base64 encoded string.
     *
     * @param value      the value
     * @param signingKey the signing key
     * @return the byte [ ]
     */
    protected byte[] sign(final byte[] value, final Key signingKey) {
        if (signingKey == null) {
            return value;
        }
        return signWith(value, getSigningAlgorithmFor(signingKey));
    }

    /**
     * Sign value with given parameters.
     *
     * @param value          the value
     * @param algHeaderValue the alg header value
     * @return the byte [ ]
     */
    protected byte[] signWith(final byte[] value, final String algHeaderValue) {
        return signWith(value, algHeaderValue, this.signingKey);
    }

    /**
     * Sign with byte [ ].
     *
     * @param value          the value
     * @param algHeaderValue the alg header value
     * @param key            the key
     * @return the byte [ ]
     */
    protected byte[] signWith(final byte[] value, final String algHeaderValue, final Key key) {
        val headers = new LinkedHashMap<>(commonHeaders);
        headers.putAll(getSigningOpHeaders());

        return JsonWebTokenSigner.builder()
            .key(key)
            .headers(headers)
            .algorithm(algHeaderValue)
            .build()
            .sign(value);
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
        } finally {
            if (this.signingKey == null) {
                setSigningKey(new AesKey(signingSecretKey.getBytes(StandardCharsets.UTF_8)));
                LOGGER.trace("Created signing key instance [{}] based on provided secret key", this.signingKey.getClass().getSimpleName());
            }
        }
    }

    protected void configureSigningKeyFromPrivateKeyResource(final String signingSecretKey) {
        val object = extractPrivateKeyFromResource(signingSecretKey);
        LOGGER.trace("Located signing key resource [{}]", signingSecretKey);
        setSigningKey(object);
    }

    protected byte[] verifySignature(final byte[] value, final Key givenKey) {
        if (givenKey == null) {
            return value;
        }
        try {
            val activeSigningKey = givenKey instanceof final IdentifiableKey idk ? idk.getKey() : givenKey;
            if (activeSigningKey instanceof final RSAPrivateKey privKey) {
                val keySpec = new RSAPublicKeySpec(privKey.getModulus(), RSA_PUBLIC_KEY_EXPONENT);
                val pubKey = KeyFactory.getInstance("RSA").generatePublic(keySpec);
                return EncodingUtils.verifyJwsSignature(pubKey, value);
            }
            return EncodingUtils.verifyJwsSignature(activeSigningKey, value);
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Gets signing algorithm for.
     *
     * @param signingKey the signing key
     * @return the signing algorithm for
     */
    protected String getSigningAlgorithmFor(final Key signingKey) {
        return Optional.ofNullable(signingAlgorithm)
            .orElseGet(() -> "RSA".equalsIgnoreCase(signingKey.getAlgorithm())
                ? AlgorithmIdentifiers.RSA_USING_SHA512
                : AlgorithmIdentifiers.HMAC_SHA512);
    }
}
