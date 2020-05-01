package org.apereo.cas.util.cipher;

import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.crypto.PrivateKeyFactoryBean;
import org.apereo.cas.util.crypto.PublicKeyFactoryBean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.provider.JCEECPrivateKey;
import org.bouncycastle.jce.provider.JCEECPublicKey;
import org.jooq.lambda.Unchecked;
import org.jose4j.keys.AesKey;
import org.jose4j.keys.RsaKeyUtil;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.EllipticCurve;
import java.security.spec.InvalidKeySpecException;
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
@NoArgsConstructor
@Getter
public abstract class AbstractCipherExecutor<T, R> implements CipherExecutor<T, R> {
    private static final int MAP_SIZE = 8;
    private static final BigInteger RSA_PUBLIC_KEY_EXPONENT = BigInteger.valueOf(65537);

    private static Map<PrivateKey, PublicKey> KEYPAIRMAP = new LinkedHashMap<>();

    @RequiredArgsConstructor
    private static class CasECParameterSpec {
        private int cofactor;
        private EllipticCurve curve;
        private ECPoint generator;
        private BigInteger order;

        CasECParameterSpec(final ECParameterSpec param) {
            this.cofactor = param.getCofactor();
            this.curve = param.getCurve();
            this.generator = param.getGenerator();
            this.order = param.getOrder();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj != null && obj instanceof CasECParameterSpec) {
                val spec2 = (CasECParameterSpec) obj;
                return this.cofactor == spec2.cofactor
                    && this.curve.equals(spec2.curve)
                    && this.generator.equals(spec2.generator)
                    && this.order.equals(spec2.order);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Integer.valueOf(cofactor).hashCode()
                   ^ this.curve.hashCode()
                   ^ this.generator.hashCode()
                   ^ this.order.hashCode();
        }
    }

    @FunctionalInterface
    private interface SignJwsAlgorithmLookupInterface {
        EncodingUtils.SignJwsAlgorithm getSignJwsAlgorithm(Key key);
    }

    @FunctionalInterface
    private interface GeneratePublicKeyFromPrivateKeyInterface {
        PublicKey generatePublicKey(PrivateKey privateKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException;
    }

    private static Map<String, SignJwsAlgorithmLookupInterface> SIGN_JWS_ALGORITHM_LOOKUP_INTERFACE_MAP = new LinkedHashMap<>();

    private static Map<CasECParameterSpec, EncodingUtils.SignJwsAlgorithm> SIGN_JWS_ALGORITHM_EC_MAP = new LinkedHashMap<>();

    private static final Map<String, GeneratePublicKeyFromPrivateKeyInterface> GENERATE_PUBLIC_KEY_FROM_PRIVATE_KEY_INTERFACE_MAP
        = new LinkedHashMap<>();

    static {
        Security.addProvider(new BouncyCastleProvider());

        try {
            /* generate all EC curves parameters for comparision */
            val keyPairGenerator = KeyPairGenerator.getInstance("EC");

            keyPairGenerator.initialize(new ECGenParameterSpec("secp256r1"));
            val secp256r1 = new CasECParameterSpec(((ECPrivateKey) keyPairGenerator.genKeyPair().getPrivate()).getParams());
            SIGN_JWS_ALGORITHM_EC_MAP.put(secp256r1, EncodingUtils.SIGN_JWS_EC_P256);

            keyPairGenerator.initialize(new ECGenParameterSpec("secp384r1"));
            val secp384r1 = new CasECParameterSpec(((ECPrivateKey) keyPairGenerator.genKeyPair().getPrivate()).getParams());
            SIGN_JWS_ALGORITHM_EC_MAP.put(secp384r1, EncodingUtils.SIGN_JWS_EC_P384);

            keyPairGenerator.initialize(new ECGenParameterSpec("secp521r1"));
            val secp521r1 = new CasECParameterSpec(((ECPrivateKey) keyPairGenerator.genKeyPair().getPrivate()).getParams());
            SIGN_JWS_ALGORITHM_EC_MAP.put(secp521r1, EncodingUtils.SIGN_JWS_EC_P521);


            /* SIGN_JWS_ALGORITHM_LOOKUP_INTERFACE_MAP method definitions */
            SIGN_JWS_ALGORITHM_LOOKUP_INTERFACE_MAP.put("RSA", key -> EncodingUtils.SIGN_JWS_RSA_SHA512);

            SIGN_JWS_ALGORITHM_LOOKUP_INTERFACE_MAP.put("EC", key -> {
                if (key != null && key instanceof ECPrivateKey) {
                    val param = new CasECParameterSpec(((ECPrivateKey) key).getParams());
                    val result = SIGN_JWS_ALGORITHM_EC_MAP.get((CasECParameterSpec) param);
                    if (result != null) {
                        return result;
                    }
                }
                throw new IllegalArgumentException("Unsupported EC parameter");
            });


            /* GENERATE_PUBLIC_KEY_FROM_PRIVATE_KEY_INTERFACE_MAP method definitions */
            GENERATE_PUBLIC_KEY_FROM_PRIVATE_KEY_INTERFACE_MAP.put("RSA", key -> {
                val privKey = (RSAPrivateKey) key;
                val keySpec = new RSAPublicKeySpec(privKey.getModulus(), RSA_PUBLIC_KEY_EXPONENT);
                val newPubKey = KeyFactory.getInstance("RSA", BouncyCastleProvider.PROVIDER_NAME).generatePublic(keySpec);
                synchronized(KEYPAIRMAP) {
                    KEYPAIRMAP.put(privKey, newPubKey);
                }
                return newPubKey;
            });

            GENERATE_PUBLIC_KEY_FROM_PRIVATE_KEY_INTERFACE_MAP.put("EC", key -> {
                val privKey = new JCEECPrivateKey((ECPrivateKey) key);
                val ecKeyParam = privKey.getParameters();
                val ecPointQ = ecKeyParam.getG().multiply(privKey.getD());
                val bPubKeySpec = new org.bouncycastle.jce.spec.ECPublicKeySpec(ecPointQ, ecKeyParam);
                val bPubKey = new JCEECPublicKey("EC", bPubKeySpec);
                val jPubKeySpec = new ECPublicKeySpec(bPubKey.getW(), bPubKey.getParams());
                val newPubKey = KeyFactory.getInstance("EC").generatePublic(jPubKeySpec);
                synchronized(KEYPAIRMAP) {
                    KEYPAIRMAP.put(privKey, newPubKey);
                }
                return newPubKey;
            });
        } catch (final NoSuchAlgorithmException e) {
            /* should never reach this line */
            LOGGER.error("Unable to create KeyPairGenerator");
            throw new IllegalArgumentException(e);
        } catch (final InvalidAlgorithmParameterException e) {
            /* should never reach this line too */
            LOGGER.error("Unable to create KeyPairGenerator");
            throw new IllegalArgumentException(e);
        }
    }
    
    private Key signingKey;

    private Map<String, Object> customHeaders = new LinkedHashMap<>(MAP_SIZE);
    
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
        val factory = new PublicKeyFactoryBean(resource, RsaKeyUtil.RSA);
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
        val keyAlgorithm = this.signingKey.getAlgorithm();
        return SIGN_JWS_ALGORITHM_LOOKUP_INTERFACE_MAP
            .getOrDefault(keyAlgorithm, anyKey -> EncodingUtils.SIGN_JWS_HMAC_SHA512)
            .getSignJwsAlgorithm(this.signingKey)
            .signJws(this.signingKey, value, this.customHeaders);
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

    /**
     * Configure signing key from private key resource.
     *
     * @param signingSecretKey the signing secret key
     */
    protected void configureSigningKeyFromPrivateKeyResource(final String signingSecretKey) {
        val object = extractPrivateKeyFromResource(signingSecretKey);
        LOGGER.trace("Located signing key resource [{}]", signingSecretKey);
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
        try {
            if (this.signingKey instanceof PrivateKey) {
                val privKey = (PrivateKey) this.signingKey;
                PublicKey pubKey = Optional.ofNullable(KEYPAIRMAP.get(privKey))
                    .orElseGet(Unchecked.supplier(() -> {
                        val newPubKey = GENERATE_PUBLIC_KEY_FROM_PRIVATE_KEY_INTERFACE_MAP.get(privKey.getAlgorithm())
                            .generatePublicKey(privKey);
                        if (newPubKey == null) {
                            throw new IllegalArgumentException("Unsupported key algorithm");
                        }
                        return newPubKey;
                    }));
                return EncodingUtils.verifyJwsSignature(pubKey, value);
            }
            return EncodingUtils.verifyJwsSignature(this.signingKey, value);
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public boolean isEnabled() {
        return this.signingKey != null;
    }
}
