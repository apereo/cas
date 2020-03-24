package org.apereo.cas.token.authentication;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.integration.pac4j.authentication.handler.support.AbstractTokenWrapperAuthenticationHandler;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.util.Base64;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.jwt.config.encryption.SecretEncryptionConfiguration;
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;

import java.util.HashSet;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * This is {@link TokenAuthenticationHandler} that authenticates instances of {@link TokenCredential}.
 * There is no need for a separate {@link PrincipalResolver} component
 * as this handler will auto-populate the principal attributes itself.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Slf4j
public class TokenAuthenticationHandler extends AbstractTokenWrapperAuthenticationHandler {


    public TokenAuthenticationHandler(final String name, final ServicesManager servicesManager, final PrincipalFactory principalFactory,
                                      final PrincipalNameTransformer principalNameTransformer) {
        super(name, servicesManager, principalFactory, null, principalNameTransformer);
    }

    private static <T extends Algorithm> T findAlgorithmFamily(final Set<Algorithm> family,
                                                               final String alg, final Class<T> clazz) {
        val result = family
            .stream()
            .filter(l -> l.getName().equalsIgnoreCase(alg))
            .findFirst();
        if (result.isPresent()) {
            val algorithm = result.get();
            if (!clazz.isAssignableFrom(algorithm.getClass())) {
                throw new ClassCastException("Result [" + algorithm
                    + " is of type " + algorithm.getClass()
                    + " when we were expecting " + clazz);
            }
            return (T) algorithm;
        }
        throw new IllegalArgumentException("Unable to find algorithm " + alg);
    }

    @Override
    public AuthenticationHandlerExecutionResult postAuthenticate(final Credential credential, final AuthenticationHandlerExecutionResult result) {
        val tokenCredential = (TokenCredential) credential;
        tokenCredential.setId(result.getPrincipal().getId());
        return super.postAuthenticate(credential, result);
    }

    @Override
    protected Authenticator<TokenCredentials> getAuthenticator(final Credential credential) {
        val tokenCredential = (TokenCredential) credential;
        LOGGER.debug("Locating token secret for service [{}]", tokenCredential.getService());

        val service = this.servicesManager.findServiceBy(tokenCredential.getService());
        val signingSecret = getRegisteredServiceJwtSigningSecret(service);

        if (StringUtils.isNotBlank(signingSecret)) {
            val serviceSigningAlg = getRegisteredServiceJwtProperty(service,
                RegisteredServiceProperties.TOKEN_SECRET_SIGNING_ALG);

            Set<Algorithm> sets = new HashSet<>(0);
            sets.addAll(JWSAlgorithm.Family.EC);
            sets.addAll(JWSAlgorithm.Family.HMAC_SHA);
            sets.addAll(JWSAlgorithm.Family.RSA);
            sets.addAll(JWSAlgorithm.Family.SIGNATURE);
            val signingSecretAlg = StringUtils.defaultString(serviceSigningAlg, JWSAlgorithm.HS256.getName());
            val signingAlg = findAlgorithmFamily(sets, signingSecretAlg, JWSAlgorithm.class);

            val jwtAuthenticator = new JwtAuthenticator();

            val secretIsBase64String = getRegisteredServiceJwtProperty(service,
                RegisteredServiceProperties.TOKEN_SECRETS_ARE_BASE64_ENCODED);
            val secretsAreBase64Encoded = BooleanUtils.toBoolean(secretIsBase64String);
            val secretBytes = getSecretBytes(signingSecret, secretsAreBase64Encoded);
            jwtAuthenticator.setSignatureConfiguration(new SecretSignatureConfiguration(secretBytes, signingAlg));

            val encryptionSecret = getRegisteredServiceJwtEncryptionSecret(service);
            if (StringUtils.isNotBlank(encryptionSecret)) {
                sets = new HashSet<>(0);
                sets.addAll(JWEAlgorithm.Family.AES_GCM_KW);
                sets.addAll(JWEAlgorithm.Family.AES_KW);
                sets.addAll(JWEAlgorithm.Family.ASYMMETRIC);
                sets.addAll(JWEAlgorithm.Family.ECDH_ES);
                sets.addAll(JWEAlgorithm.Family.PBES2);
                sets.addAll(JWEAlgorithm.Family.RSA);
                sets.addAll(JWEAlgorithm.Family.SYMMETRIC);

                val encryptionAlg = getRegisteredServiceJwtProperty(service,
                    RegisteredServiceProperties.TOKEN_SECRET_ENCRYPTION_ALG);

                val encryptionSecretAlg = StringUtils.defaultString(encryptionAlg, JWEAlgorithm.DIR.getName());
                val encAlg = findAlgorithmFamily(sets, encryptionSecretAlg, JWEAlgorithm.class);

                sets = new HashSet<>(0);
                sets.addAll(EncryptionMethod.Family.AES_CBC_HMAC_SHA);
                sets.addAll(EncryptionMethod.Family.AES_GCM);

                val encryptionMethod = getRegisteredServiceJwtProperty(service,
                    RegisteredServiceProperties.TOKEN_SECRET_ENCRYPTION_METHOD);

                val encryptionSecretMethod = StringUtils.defaultString(encryptionMethod, EncryptionMethod.A192CBC_HS384.getName());
                val encMethod = findAlgorithmFamily(sets, encryptionSecretMethod, EncryptionMethod.class);
                val encSecretBytes = getSecretBytes(encryptionSecret, secretsAreBase64Encoded);
                jwtAuthenticator.setEncryptionConfiguration(new SecretEncryptionConfiguration(encSecretBytes, encAlg, encMethod));
            } else {
                LOGGER.info("No token encryption secret is defined for service [{}]. You may want to use the [{}] property",
                        service.getServiceId(),
                        RegisteredServiceProperties.TOKEN_SECRET_ENCRYPTION.getPropertyName());
            }
            return jwtAuthenticator;
        }
        LOGGER.warn("No token signing secret is defined for service [{}]. Ensure [{}] property is defined for service",
            service.getServiceId(),
            RegisteredServiceProperty.RegisteredServiceProperties.TOKEN_SECRET_SIGNING.getPropertyName());
        return null;
    }

    /**
     * Gets registered service jwt encryption secret.
     *
     * @param service the service
     * @return the registered service jwt secret
     */
    private String getRegisteredServiceJwtEncryptionSecret(final RegisteredService service) {
        return getRegisteredServiceJwtProperty(service, RegisteredServiceProperty.RegisteredServiceProperties.TOKEN_SECRET_ENCRYPTION);
    }

    /**
     * Gets registered service jwt signing secret.
     *
     * @param service the service
     * @return the registered service jwt secret
     */
    private String getRegisteredServiceJwtSigningSecret(final RegisteredService service) {
        return getRegisteredServiceJwtProperty(service, RegisteredServiceProperty.RegisteredServiceProperties.TOKEN_SECRET_SIGNING);
    }

    /**
     * Gets registered service jwt secret.
     *
     * @param service  the service
     * @param propName the prop name
     * @return the registered service jwt secret
     */
    protected String getRegisteredServiceJwtProperty(final RegisteredService service, final RegisteredServiceProperty.RegisteredServiceProperties propName) {
        if (service == null || !service.getAccessStrategy().isServiceAccessAllowed()) {
            LOGGER.debug("Service is not defined/found or its access is disabled in the registry");
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE);
        }
        if (propName.isAssignedTo(service)) {
            return propName.getPropertyValue(service).getValue();
        }
        LOGGER.trace("Service [{}] does not define a property [{}] in the registry", service.getServiceId(), propName);
        return null;
    }

    /**
     * Convert secret to bytes honoring {@link RegisteredServiceProperty.RegisteredServiceProperties#TOKEN_SECRETS_ARE_BASE64_ENCODED}
     * config parameter.
     *
     * @param secret                - String to be represented to byte[]
     * @param secretIsBase64Encoded - is this a base64 encoded #secret?
     * @return byte[] representation of #secret
     */
    private static byte[] getSecretBytes(final String secret, final boolean secretIsBase64Encoded) {
        return secretIsBase64Encoded ? new Base64(secret).decode() : secret.getBytes(UTF_8);
    }
}
