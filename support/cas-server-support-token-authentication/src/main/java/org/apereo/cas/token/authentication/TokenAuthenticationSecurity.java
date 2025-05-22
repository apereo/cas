package org.apereo.cas.token.authentication;

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
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.util.Base64;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.keys.RsaKeyUtil;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.jwt.config.encryption.EncryptionConfiguration;
import org.pac4j.jwt.config.encryption.RSAEncryptionConfiguration;
import org.pac4j.jwt.config.encryption.SecretEncryptionConfiguration;
import org.pac4j.jwt.config.signature.RSASignatureConfiguration;
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration;
import org.pac4j.jwt.config.signature.SignatureConfiguration;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.pac4j.jwt.profile.JwtGenerator;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This is {@link TokenAuthenticationSecurity}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class TokenAuthenticationSecurity {
    private RegisteredServiceSecurityConfiguration securityConfiguration;

    /**
     * Generate a token for authentication.
     *
     * @param authentication the authentication
     * @return the string
     */
    public String generateTokenFor(final Authentication authentication) {
        val claims = new HashMap<String, Object>(authentication.getAttributes());
        claims.putAll(authentication.getPrincipal().getAttributes());
        claims.put("sub", authentication.getPrincipal().getId());
        return toGenerator().generate(CollectionUtils.toSingleValuedMap(claims, List.of("jti", "iss", "nbf", "iat", "exp")));
    }

    /**
     * Validate token and build user profile.
     *
     * @param token the token
     * @return the user profile
     */
    public UserProfile validateToken(final String token) {
        return toAuthenticator().validateToken(token);
    }

    /**
     * For registered service.
     *
     * @param registeredService the service
     * @return the token authentication security
     */
    public static TokenAuthenticationSecurity forRegisteredService(final RegisteredService registeredService) {
        val securityConfiguration = new RegisteredServiceSecurityConfiguration();
        val signingConfig = getSignatureConfiguration(registeredService);
        val encConfig = getEncryptionConfiguration(registeredService);
        securityConfiguration.setEncryptionConfiguration(encConfig);
        securityConfiguration.setSignatureConfiguration(signingConfig);
        return new TokenAuthenticationSecurity(securityConfiguration);
    }

    /**
     * To generator.
     *
     * @return the jwt generator
     */
    public JwtGenerator toGenerator() {
        val generator = new JwtGenerator();
        FunctionUtils.doIfNotNull(securityConfiguration.getSignatureConfiguration(), generator::setSignatureConfiguration);
        FunctionUtils.doIfNotNull(securityConfiguration.getEncryptionConfiguration(), generator::setEncryptionConfiguration);
        return generator;
    }

    /**
     * To authenticator.
     *
     * @return the jwt authenticator
     */
    public JwtAuthenticator toAuthenticator() {
        val authn = new JwtAuthenticator();
        FunctionUtils.doIfNotNull(securityConfiguration.getEncryptionConfiguration(), authn::setEncryptionConfiguration);
        FunctionUtils.doIfNotNull(securityConfiguration.getSignatureConfiguration(), authn::setSignatureConfiguration);
        return authn;
    }

    private static String getRegisteredServiceJwtProperty(final RegisteredService registeredService,
                                                          final RegisteredServiceProperties propName) {
        if (registeredService == null || !registeredService.getAccessStrategy().isServiceAccessAllowed(registeredService, null)) {
            LOGGER.debug("Service is not defined/found or its access is disabled in the registry");
            throw UnauthorizedServiceException.denied("Denied");
        }
        if (propName.isAssignedTo(registeredService)) {
            val propertyValue = propName.getPropertyValue(registeredService).value();
            return SpringExpressionLanguageValueResolver.getInstance().resolve(propertyValue);
        }
        LOGGER.trace("Service [{}] does not define a property [{}] in the registry", registeredService.getServiceId(), propName);
        return null;
    }

    private static SignatureConfiguration getSignatureConfiguration(final RegisteredService registeredService) {
        val signingSecret = getRegisteredServiceJwtSigningSecret(registeredService);
        if (StringUtils.isNotBlank(signingSecret)) {
            val signingAlg = determineSigningAlgorithm(registeredService);
            if (JWSAlgorithm.Family.HMAC_SHA.contains(signingAlg)) {
                val secretBytes = getSecretBytes(signingSecret, areSecretsBase64Encoded(registeredService));
                return new SecretSignatureConfiguration(secretBytes, signingAlg);
            }
            if (JWSAlgorithm.Family.RSA.contains(signingAlg)) {
                val privateKey = getRsaPrivateKey(signingSecret);
                
                val encryptionSecret = getRegisteredServiceJwtEncryptionSecret(registeredService);
                val publicKey = StringUtils.isNotBlank(encryptionSecret) ? getRsaPublicKey(encryptionSecret) : null;
                
                val config = new RSASignatureConfiguration();
                config.setAlgorithm(signingAlg);
                config.setPrivateKey(privateKey);
                config.setPublicKey(publicKey);
                return config;
            }
        }
        return null;
    }

    private static EncryptionConfiguration getEncryptionConfiguration(final RegisteredService registeredService) {
        val encryptionSecret = getRegisteredServiceJwtEncryptionSecret(registeredService);
        if (StringUtils.isNotBlank(encryptionSecret)) {
            val encryptionAlgorithm = determineEncryptionAlgorithm(registeredService);
            val encryptionMethod = determineEncryptionMethod(registeredService);
            if (JWEAlgorithm.Family.RSA.contains(encryptionAlgorithm)) {
                val publicKey = getRsaPublicKey(encryptionSecret);

                val signingSecret = getRegisteredServiceJwtSigningSecret(registeredService);
                val privateKey = StringUtils.isNotBlank(signingSecret) ? getRsaPrivateKey(signingSecret) : null;

                val config = new RSAEncryptionConfiguration();
                config.setAlgorithm(encryptionAlgorithm);
                config.setMethod(encryptionMethod);
                config.setPublicKey(publicKey);
                config.setPrivateKey(privateKey);
                return config;
            }
            val encSecretBytes = getSecretBytes(encryptionSecret, areSecretsBase64Encoded(registeredService));
            return new SecretEncryptionConfiguration(encSecretBytes, encryptionAlgorithm, encryptionMethod);
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

    private static String getRegisteredServiceJwtEncryptionSecret(final RegisteredService registeredService) {
        return getRegisteredServiceJwtProperty(registeredService, RegisteredServiceProperties.TOKEN_SECRET_ENCRYPTION);
    }

    private static String getRegisteredServiceJwtSigningSecret(final RegisteredService registeredService) {
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

    @Data
    @Setter
    private static final class RegisteredServiceSecurityConfiguration {
        private SignatureConfiguration signatureConfiguration;
        private EncryptionConfiguration encryptionConfiguration;
    }
}
