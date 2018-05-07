package org.apereo.cas.token.authentication;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
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

    @Override
    public AuthenticationHandlerExecutionResult postAuthenticate(final Credential credential, final AuthenticationHandlerExecutionResult result) {
        final var tokenCredential = (TokenCredential) credential;
        tokenCredential.setId(result.getPrincipal().getId());
        return super.postAuthenticate(credential, result);
    }

    @Override
    protected Authenticator<TokenCredentials> getAuthenticator(final Credential credential) {
        final var tokenCredential = (TokenCredential) credential;
        LOGGER.debug("Locating token secret for service [{}]", tokenCredential.getService());

        final var service = this.servicesManager.findServiceBy(tokenCredential.getService());
        final var signingSecret = getRegisteredServiceJwtSigningSecret(service);
        final var encryptionSecret = getRegisteredServiceJwtEncryptionSecret(service);

        final var serviceSigningAlg = getRegisteredServiceJwtProperty(service,
            RegisteredServiceProperties.TOKEN_SECRET_SIGNING_ALG);
        final var signingSecretAlg = StringUtils.defaultString(serviceSigningAlg, JWSAlgorithm.HS256.getName());

        final var encryptionAlg = getRegisteredServiceJwtProperty(service,
            RegisteredServiceProperties.TOKEN_SECRET_ENCRYPTION_ALG);
        final var encryptionSecretAlg = StringUtils.defaultString(encryptionAlg, JWEAlgorithm.DIR.getName());

        final var encryptionMethod = getRegisteredServiceJwtProperty(service,
            RegisteredServiceProperties.TOKEN_SECRET_ENCRYPTION_METHOD);
        final var encryptionSecretMethod = StringUtils.defaultString(encryptionMethod, EncryptionMethod.A192CBC_HS384.getName());
        final var secretIsBase64String = getRegisteredServiceJwtProperty(service,
            RegisteredServiceProperties.TOKEN_SECRETS_ARE_BASE64_ENCODED);
        final var secretsAreBase64Encoded = BooleanUtils.toBoolean(secretIsBase64String);

        if (StringUtils.isNotBlank(signingSecret)) {
            Set<Algorithm> sets = new HashSet<>();
            sets.addAll(JWSAlgorithm.Family.EC);
            sets.addAll(JWSAlgorithm.Family.HMAC_SHA);
            sets.addAll(JWSAlgorithm.Family.RSA);
            sets.addAll(JWSAlgorithm.Family.SIGNATURE);

            final var signingAlg = findAlgorithmFamily(sets, signingSecretAlg, JWSAlgorithm.class);

            final var jwtAuthenticator = new JwtAuthenticator();
            final var secretBytes = getSecretBytes(signingSecret, secretsAreBase64Encoded);
            jwtAuthenticator.setSignatureConfiguration(new SecretSignatureConfiguration(secretBytes, signingAlg));

            if (StringUtils.isNotBlank(encryptionSecret)) {
                sets = new HashSet<>();
                sets.addAll(JWEAlgorithm.Family.AES_GCM_KW);
                sets.addAll(JWEAlgorithm.Family.AES_KW);
                sets.addAll(JWEAlgorithm.Family.ASYMMETRIC);
                sets.addAll(JWEAlgorithm.Family.ECDH_ES);
                sets.addAll(JWEAlgorithm.Family.PBES2);
                sets.addAll(JWEAlgorithm.Family.RSA);
                sets.addAll(JWEAlgorithm.Family.SYMMETRIC);

                final var encAlg = findAlgorithmFamily(sets, encryptionSecretAlg, JWEAlgorithm.class);

                sets = new HashSet<>();
                sets.addAll(EncryptionMethod.Family.AES_CBC_HMAC_SHA);
                sets.addAll(EncryptionMethod.Family.AES_GCM);

                final var encMethod = findAlgorithmFamily(sets, encryptionSecretMethod, EncryptionMethod.class);
                final var encSecretBytes = getSecretBytes(encryptionSecret, secretsAreBase64Encoded);
                jwtAuthenticator.setEncryptionConfiguration(new SecretEncryptionConfiguration(encSecretBytes, encAlg, encMethod));
            } else {
                LOGGER.warn("JWT authentication is configured to share jwtAuthenticator single key for both signing/encryption");
            }
            return jwtAuthenticator;
        }
        LOGGER.warn("No token signing secret is defined for service [{}]. Ensure [{}] property is defined for service",
            service.getServiceId(),
            RegisteredServiceProperty.RegisteredServiceProperties.TOKEN_SECRET_SIGNING.getPropertyName());
        return null;
    }

    private static <T extends Algorithm> T findAlgorithmFamily(final Set<Algorithm> family,
                                                               final String alg, final Class<T> clazz) {
        final var result = family
            .stream()
            .filter(l -> l.getName().equalsIgnoreCase(alg))
            .findFirst()
            .get();
        if (!clazz.isAssignableFrom(result.getClass())) {
            throw new ClassCastException("Result [" + result
                + " is of type " + result.getClass()
                + " when we were expecting " + clazz);
        }
        return (T) result;
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
        LOGGER.warn("Service [{}] does not define a property [{}] in the registry", service.getServiceId(), propName);
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
    private byte[] getSecretBytes(final String secret, final boolean secretIsBase64Encoded) {
        return secretIsBase64Encoded ? new Base64(secret).decode() : secret.getBytes(UTF_8);
    }
}
