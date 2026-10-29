package org.apereo.cas.token.authentication;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.crypto.PrivateKeyFactoryBean;
import org.apereo.cas.util.crypto.PublicKeyFactoryBean;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.AESDecrypter;
import com.nimbusds.jose.crypto.AESEncrypter;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.util.Base64;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.keys.RsaKeyUtil;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link TokenAuthenticationSecurity}, which signs, encrypts and validates the JWTs of token authentication with
 * the secrets and algorithms that a registered service defines in its properties. Tokens are signed JWS, optionally
 * nested in a JWE with the {@code JWT} content type, the shapes pac4j's {@code JwtGenerator} produced, so tokens that
 * applications already build keep working. A service that defines no signing secret neither issues nor accepts tokens.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class TokenAuthenticationSecurity {
    private static final String LEGACY_PROFILE_CLASS_PREFIX = "org.pac4j.";

    private static final String LEGACY_TYPED_ID_SEPARATOR = "#";

    private static final String LEGACY_INTERNAL_CLAIM_PREFIX = "$int_";

    private final @Nullable TokenSigning signing;

    private final @Nullable TokenEncryption encryption;

    /**
     * Generate a token for the authentication: its attributes and the principal's attributes as claims, single values
     * unwrapped, and the principal as the subject. Nothing is generated when the service defines no signing secret.
     *
     * @param authentication the authentication
     * @return the token, if the service can sign one
     */
    public Optional<String> generateTokenFor(final Authentication authentication) {
        val tokenSigning = signing;
        val tokenEncryption = encryption;
        if (tokenSigning == null) {
            LOGGER.debug("No signing secret is defined for the service, so no token is generated");
            return Optional.empty();
        }
        val claims = new HashMap<String, Object>(authentication.getAttributes());
        claims.putAll(authentication.getPrincipal().getAttributes());
        claims.put(JWTClaimNames.SUBJECT, authentication.getPrincipal().getId());
        val builder = new JWTClaimsSet.Builder();
        CollectionUtils.toSingleValuedMap(claims, List.of(JWTClaimNames.JWT_ID, JWTClaimNames.ISSUER,
            JWTClaimNames.NOT_BEFORE, JWTClaimNames.ISSUED_AT, JWTClaimNames.EXPIRATION_TIME)).forEach(builder::claim);
        return Optional.of(FunctionUtils.doUnchecked(() -> {
            val signedJwt = tokenSigning.sign(builder.build());
            return tokenEncryption != null ? tokenEncryption.encrypt(signedJwt) : signedJwt.serialize();
        }));
    }

    /**
     * Validate the token. The token must be signed with the service's signing secret, and may be encrypted with its
     * encryption secret; an unsigned token, an expired one or one without a subject is refused, and so is every token
     * when the service defines no signing secret. A subject that pac4j typed with its profile class
     * ({@code org.pac4j...CommonProfile#casuser}) is read without the type, and pac4j's internal {@code $int_} claims are dropped.
     * Claims whose value is a map are also added one by one, one level deep, as pac4j did.
     *
     * @param token the token
     * @return the subject and claims of the token, if it is valid
     */
    public Optional<AuthenticatedToken> validateToken(final String token) {
        if (signing == null) {
            LOGGER.warn("No signing secret is defined for the service, so the token cannot be accepted");
            return Optional.empty();
        }
        try {
            val jwt = JWTParser.parse(token);
            if (jwt instanceof PlainJWT) {
                LOGGER.warn("An unsigned token cannot be accepted");
                return Optional.empty();
            }
            var signedJwt = jwt instanceof final SignedJWT signed ? signed : null;
            if (jwt instanceof final EncryptedJWT encryptedJwt) {
                val header = encryptedJwt.getHeader();
                if (encryption == null || !encryption.supports(header.getAlgorithm(), header.getEncryptionMethod())) {
                    LOGGER.warn("No encryption secret of the service can decrypt the token");
                    return Optional.empty();
                }
                encryption.decrypt(encryptedJwt);
                signedJwt = encryptedJwt.getPayload().toSignedJWT();
            }
            if (signedJwt == null) {
                LOGGER.warn("An unsigned token cannot be accepted");
                return Optional.empty();
            }
            if (!signing.supports(signedJwt.getHeader().getAlgorithm()) || !signing.verify(signedJwt)) {
                LOGGER.warn("The signature of the token cannot be verified");
                return Optional.empty();
            }
            return buildAuthenticatedToken(signedJwt.getJWTClaimsSet());
        } catch (final ParseException | JOSEException e) {
            LOGGER.warn("Unable to validate the token: [{}]", e.getMessage());
            LOGGER.debug(e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Create a token security instance for a registered service.
     *
     * @param registeredService the service
     * @return the token authentication security
     */
    public static TokenAuthenticationSecurity forRegisteredService(final RegisteredService registeredService) {
        return new TokenAuthenticationSecurity(getSigning(registeredService), getEncryption(registeredService));
    }

    private static Optional<AuthenticatedToken> buildAuthenticatedToken(final JWTClaimsSet claims) {
        val subject = sanitizeSubject(Objects.toString(claims.getSubject(), StringUtils.EMPTY));
        if (StringUtils.isBlank(subject)) {
            LOGGER.warn("The token has no subject");
            return Optional.empty();
        }
        val expirationTime = claims.getExpirationTime();
        if (expirationTime != null && expirationTime.before(new Date())) {
            LOGGER.warn("The token expired at [{}]", expirationTime);
            return Optional.empty();
        }
        val attributes = new LinkedHashMap<String, Object>();
        claims.getClaims().forEach((name, value) -> {
            if (!JWTClaimNames.SUBJECT.equals(name) && !name.startsWith(LEGACY_INTERNAL_CLAIM_PREFIX)) {
                if (value instanceof final Map<?, ?> nested) {
                    nested.forEach((nestedName, nestedValue) -> addClaim(attributes, String.valueOf(nestedName), nestedValue));
                }
                addClaim(attributes, name, value);
            }
        });
        return Optional.of(new AuthenticatedToken(subject, attributes));
    }

    private static void addClaim(final Map<String, Object> attributes, final String name, final @Nullable Object value) {
        if (value != null) {
            attributes.merge(name, value, (existing, given) -> existing instanceof final Collection<?> existingValues
                && given instanceof final Collection<?> givenValues
                ? Stream.concat(existingValues.stream(), givenValues.stream()).distinct().toList()
                : given);
        }
    }

    private static String sanitizeSubject(final String subject) {
        val type = StringUtils.substringBefore(subject, LEGACY_TYPED_ID_SEPARATOR);
        return subject.contains(LEGACY_TYPED_ID_SEPARATOR) && type.startsWith(LEGACY_PROFILE_CLASS_PREFIX)
            ? subject.substring(type.length() + 1)
            : subject;
    }

    private static @Nullable String getRegisteredServiceJwtProperty(final RegisteredService registeredService,
                                                                    final RegisteredServiceProperties propName) {
        if (registeredService == null || !registeredService.getAccessStrategy().isServiceAccessAllowed(registeredService, null)) {
            LOGGER.debug("Service is not defined/found or its access is disabled in the registry");
            throw UnauthorizedServiceException.denied("Denied");
        }
        if (propName.isAssignedTo(registeredService)) {
            val propertyValue = Objects.requireNonNull(propName.getPropertyValue(registeredService)).value();
            return SpringExpressionLanguageValueResolver.getInstance().resolve(propertyValue);
        }
        LOGGER.trace("Service [{}] does not define a property [{}] in the registry", registeredService.getServiceId(), propName);
        return null;
    }

    private static @Nullable TokenSigning getSigning(final RegisteredService registeredService) {
        val signingSecret = getRegisteredServiceJwtSigningSecret(registeredService);
        if (StringUtils.isNotBlank(signingSecret)) {
            val signingAlg = determineSigningAlgorithm(registeredService);
            if (JWSAlgorithm.Family.HMAC_SHA.contains(signingAlg)) {
                val secretBytes = getSecretBytes(signingSecret, areSecretsBase64Encoded(registeredService));
                return new SecretTokenSigning(secretBytes, signingAlg);
            }
            if (JWSAlgorithm.Family.RSA.contains(signingAlg)) {
                val privateKey = getRsaPrivateKey(signingSecret);
                val encryptionSecret = getRegisteredServiceJwtEncryptionSecret(registeredService);
                val publicKey = StringUtils.isNotBlank(encryptionSecret) ? getRsaPublicKey(encryptionSecret) : null;
                return new RsaTokenSigning(signingAlg, privateKey, publicKey);
            }
            LOGGER.warn("Signing algorithm [{}] of service [{}] is not supported for token authentication; use an HMAC or RSA algorithm",
                signingAlg.getName(), registeredService.getName());
        }
        return null;
    }

    private static @Nullable TokenEncryption getEncryption(final RegisteredService registeredService) {
        val encryptionSecret = getRegisteredServiceJwtEncryptionSecret(registeredService);
        if (StringUtils.isNotBlank(encryptionSecret)) {
            val encryptionAlgorithm = determineEncryptionAlgorithm(registeredService);
            val encryptionMethod = determineEncryptionMethod(registeredService);
            if (JWEAlgorithm.Family.RSA.contains(encryptionAlgorithm)) {
                val publicKey = getRsaPublicKey(encryptionSecret);
                val signingSecret = getRegisteredServiceJwtSigningSecret(registeredService);
                val privateKey = StringUtils.isNotBlank(signingSecret) ? getRsaPrivateKey(signingSecret) : null;
                return new RsaTokenEncryption(encryptionAlgorithm, encryptionMethod, publicKey, privateKey);
            }
            val encSecretBytes = getSecretBytes(encryptionSecret, areSecretsBase64Encoded(registeredService));
            return new SecretTokenEncryption(encSecretBytes, encryptionAlgorithm, encryptionMethod);
        }
        return null;
    }

    private static RSAPublicKey getRsaPublicKey(final String encryptionSecret) {
        return FunctionUtils.doUnchecked(() -> {
            val resource = ResourceUtils.getResourceFrom(encryptionSecret);
            val factory = new PublicKeyFactoryBean(resource, RsaKeyUtil.RSA);
            factory.setSingleton(false);
            return (RSAPublicKey) factory.getObject();
        });
    }

    private static RSAPrivateKey getRsaPrivateKey(final String signingSecret) {
        return FunctionUtils.doUnchecked(() -> {
            val resource = ResourceUtils.getResourceFrom(signingSecret);
            val factory = new PrivateKeyFactoryBean();
            factory.setAlgorithm(RsaKeyUtil.RSA);
            factory.setLocation(resource);
            factory.setSingleton(false);
            return (RSAPrivateKey) factory.getObject();
        });
    }

    private static JWSAlgorithm determineSigningAlgorithm(final RegisteredService registeredService) {
        val serviceSigningAlg = getRegisteredServiceJwtProperty(registeredService, RegisteredServiceProperties.TOKEN_SECRET_SIGNING_ALG);
        val signingSecretAlg = StringUtils.defaultIfBlank(serviceSigningAlg, JWSAlgorithm.HS256.getName());
        val sets = new HashSet<Algorithm>();
        sets.addAll(JWSAlgorithm.Family.EC);
        sets.addAll(JWSAlgorithm.Family.HMAC_SHA);
        sets.addAll(JWSAlgorithm.Family.RSA);
        sets.addAll(JWSAlgorithm.Family.SIGNATURE);
        return findAlgorithmFamily(sets, signingSecretAlg, JWSAlgorithm.class);
    }

    private static EncryptionMethod determineEncryptionMethod(final RegisteredService registeredService) {
        val encryptionMethods = new HashSet<Algorithm>();
        encryptionMethods.addAll(EncryptionMethod.Family.AES_CBC_HMAC_SHA);
        encryptionMethods.addAll(EncryptionMethod.Family.AES_GCM);
        val encryptionMethod = getRegisteredServiceJwtProperty(registeredService, RegisteredServiceProperties.TOKEN_SECRET_ENCRYPTION_METHOD);
        val encryptionSecretMethod = StringUtils.defaultIfBlank(encryptionMethod, EncryptionMethod.A192CBC_HS384.getName());
        return findAlgorithmFamily(encryptionMethods, encryptionSecretMethod, EncryptionMethod.class);
    }

    private static JWEAlgorithm determineEncryptionAlgorithm(final RegisteredService registeredService) {
        val sets = new HashSet<Algorithm>();
        sets.addAll(JWEAlgorithm.Family.AES_GCM_KW);
        sets.addAll(JWEAlgorithm.Family.AES_KW);
        sets.addAll(JWEAlgorithm.Family.ASYMMETRIC);
        sets.addAll(JWEAlgorithm.Family.ECDH_ES);
        sets.addAll(JWEAlgorithm.Family.PBES2);
        sets.addAll(JWEAlgorithm.Family.RSA);
        sets.addAll(JWEAlgorithm.Family.SYMMETRIC);
        val encryptionAlg = getRegisteredServiceJwtProperty(registeredService, RegisteredServiceProperties.TOKEN_SECRET_ENCRYPTION_ALG);
        val encryptionSecretAlg = StringUtils.defaultIfBlank(encryptionAlg, JWEAlgorithm.DIR.getName());
        return findAlgorithmFamily(sets, encryptionSecretAlg, JWEAlgorithm.class);
    }

    private static @Nullable String getRegisteredServiceJwtEncryptionSecret(final RegisteredService registeredService) {
        return getRegisteredServiceJwtProperty(registeredService, RegisteredServiceProperties.TOKEN_SECRET_ENCRYPTION);
    }

    private static @Nullable String getRegisteredServiceJwtSigningSecret(final RegisteredService registeredService) {
        return getRegisteredServiceJwtProperty(registeredService, RegisteredServiceProperties.TOKEN_SECRET_SIGNING);
    }

    private static boolean areSecretsBase64Encoded(final RegisteredService registeredService) {
        val secretIsBase64 = getRegisteredServiceJwtProperty(registeredService, RegisteredServiceProperties.TOKEN_SECRETS_ARE_BASE64_ENCODED);
        return BooleanUtils.toBoolean(secretIsBase64);
    }

    private static <T extends Algorithm> T findAlgorithmFamily(final Set<Algorithm> family,
                                                               final String alg,
                                                               final Class<T> clazz) {
        val result = family
            .stream()
            .filter(algorithm -> algorithm.getName().equalsIgnoreCase(alg))
            .findFirst();
        if (result.isPresent()) {
            val algorithm = result.get();
            if (!clazz.isAssignableFrom(algorithm.getClass())) {
                throw new ClassCastException("Result [%s is of type %s when we were expecting %s"
                    .formatted(algorithm, algorithm.getClass(), clazz));
            }
            return (T) algorithm;
        }
        throw new IllegalArgumentException("Unable to find algorithm " + alg);
    }

    private static byte[] getSecretBytes(final String secret, final boolean secretIsBase64Encoded) {
        return secretIsBase64Encoded ? new Base64(secret).decode() : secret.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * The subject and claims of a valid token.
     *
     * @param subject the subject, without the profile type pac4j may have added
     * @param claims  the claims, without the subject
     */
    public record AuthenticatedToken(String subject, Map<String, Object> claims) {
    }

    private sealed interface TokenSigning {
        boolean supports(JWSAlgorithm algorithm);

        SignedJWT sign(JWTClaimsSet claims) throws JOSEException;

        boolean verify(SignedJWT jwt) throws JOSEException;
    }

    private sealed interface TokenEncryption {
        boolean supports(JWEAlgorithm algorithm, EncryptionMethod method);

        JWEEncrypter encrypter() throws JOSEException;

        JWEDecrypter decrypter() throws JOSEException;

        JWEAlgorithm algorithm();

        EncryptionMethod method();

        /**
         * Encrypt the signed token as the payload of a JWE whose content type is {@code JWT}.
         *
         * @param signedJwt the signed token
         * @return the encrypted token
         * @throws JOSEException when the token cannot be encrypted
         */
        default String encrypt(final SignedJWT signedJwt) throws JOSEException {
            val header = new JWEHeader.Builder(algorithm(), method()).contentType("JWT").build();
            val jwe = new JWEObject(header, new Payload(signedJwt));
            jwe.encrypt(encrypter());
            return jwe.serialize();
        }

        /**
         * Decrypt the token.
         *
         * @param jwt the encrypted token
         * @throws JOSEException when the token cannot be decrypted
         */
        default void decrypt(final EncryptedJWT jwt) throws JOSEException {
            jwt.decrypt(decrypter());
        }
    }

    @RequiredArgsConstructor
    private static final class SecretTokenSigning implements TokenSigning {
        private final byte[] secret;

        private final JWSAlgorithm algorithm;

        @Override
        public boolean supports(final JWSAlgorithm algorithm) {
            return MACVerifier.SUPPORTED_ALGORITHMS.contains(algorithm);
        }

        @Override
        public SignedJWT sign(final JWTClaimsSet claims) throws JOSEException {
            val jwt = new SignedJWT(new JWSHeader(algorithm), claims);
            jwt.sign(new MACSigner(secret));
            return jwt;
        }

        @Override
        public boolean verify(final SignedJWT jwt) throws JOSEException {
            return jwt.verify(new MACVerifier(secret));
        }
    }

    @RequiredArgsConstructor
    private static final class RsaTokenSigning implements TokenSigning {
        private final JWSAlgorithm algorithm;

        private final RSAPrivateKey privateKey;

        private final @Nullable RSAPublicKey publicKey;

        @Override
        public boolean supports(final JWSAlgorithm algorithm) {
            return RSASSAVerifier.SUPPORTED_ALGORITHMS.contains(algorithm);
        }

        @Override
        public SignedJWT sign(final JWTClaimsSet claims) throws JOSEException {
            val jwt = new SignedJWT(new JWSHeader(algorithm), claims);
            jwt.sign(new RSASSASigner(privateKey));
            return jwt;
        }

        @Override
        public boolean verify(final SignedJWT jwt) throws JOSEException {
            if (publicKey == null) {
                throw new JOSEException("No public key is defined to verify the signature");
            }
            return jwt.verify(new RSASSAVerifier(publicKey));
        }
    }

    @RequiredArgsConstructor
    private static final class SecretTokenEncryption implements TokenEncryption {
        private final byte[] secret;

        private final JWEAlgorithm algorithm;

        private final EncryptionMethod method;

        @Override
        public boolean supports(final JWEAlgorithm algorithm, final EncryptionMethod method) {
            return (isDirect(algorithm) && DirectDecrypter.SUPPORTED_ENCRYPTION_METHODS.contains(method))
                || (AESDecrypter.SUPPORTED_ALGORITHMS.contains(algorithm) && AESDecrypter.SUPPORTED_ENCRYPTION_METHODS.contains(method));
        }

        @Override
        public JWEEncrypter encrypter() throws JOSEException {
            return isDirect(algorithm) ? new DirectEncrypter(secret) : new AESEncrypter(secret);
        }

        @Override
        public JWEDecrypter decrypter() throws JOSEException {
            return isDirect(algorithm) ? new DirectDecrypter(secret) : new AESDecrypter(secret);
        }

        @Override
        public JWEAlgorithm algorithm() {
            return algorithm;
        }

        @Override
        public EncryptionMethod method() {
            return method;
        }

        private static boolean isDirect(final JWEAlgorithm algorithm) {
            return DirectDecrypter.SUPPORTED_ALGORITHMS.contains(algorithm);
        }
    }

    @RequiredArgsConstructor
    private static final class RsaTokenEncryption implements TokenEncryption {
        private final JWEAlgorithm algorithm;

        private final EncryptionMethod method;

        private final RSAPublicKey publicKey;

        private final @Nullable RSAPrivateKey privateKey;

        @Override
        public boolean supports(final JWEAlgorithm algorithm, final EncryptionMethod method) {
            return RSADecrypter.SUPPORTED_ALGORITHMS.contains(algorithm) && RSADecrypter.SUPPORTED_ENCRYPTION_METHODS.contains(method);
        }

        @Override
        public JWEEncrypter encrypter() {
            return new RSAEncrypter(publicKey);
        }

        @Override
        public JWEDecrypter decrypter() throws JOSEException {
            if (privateKey == null) {
                throw new JOSEException("No private key is defined to decrypt the token");
            }
            return new RSADecrypter(privateKey);
        }

        @Override
        public JWEAlgorithm algorithm() {
            return algorithm;
        }

        @Override
        public EncryptionMethod method() {
            return method;
        }
    }
}
