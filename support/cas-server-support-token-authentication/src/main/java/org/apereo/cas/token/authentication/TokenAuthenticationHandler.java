package org.apereo.cas.token.authentication;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.integration.pac4j.authentication.handler.support.AbstractTokenWrapperAuthenticationHandler;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.token.TokenConstants;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.jwt.config.encryption.SecretEncryptionConfiguration;
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * This is {@link TokenAuthenticationHandler} that authenticates instances of {@link TokenCredential}.
 * There is no need for a separate {@link PrincipalResolver} component
 * as this handler will auto-populate the principal attributes itself.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public class TokenAuthenticationHandler extends AbstractTokenWrapperAuthenticationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(TokenAuthenticationHandler.class);

    public TokenAuthenticationHandler(final String name, final ServicesManager servicesManager, final PrincipalFactory principalFactory,
                                      final PrincipalNameTransformer principalNameTransformer) {
        super(name, servicesManager, principalFactory, null, principalNameTransformer);
    }

    @Override
    public HandlerResult postAuthenticate(final Credential credential, final HandlerResult result) {
        final TokenCredential tokenCredential = (TokenCredential) credential;
        tokenCredential.setId(result.getPrincipal().getId());
        return super.postAuthenticate(credential, result);
    }

    @Override
    protected Authenticator<TokenCredentials> getAuthenticator(final Credential credential) {
        final TokenCredential tokenCredential = (TokenCredential) credential;
        LOGGER.debug("Locating token secret for service [{}]", tokenCredential.getService());

        final RegisteredService service = this.servicesManager.findServiceBy(tokenCredential.getService());
        final String signingSecret = getRegisteredServiceJwtSigningSecret(service);
        final String encryptionSecret = getRegisteredServiceJwtEncryptionSecret(service);

        final String signingSecretAlg =
                StringUtils.defaultString(getRegisteredServiceJwtSecret(service, TokenConstants.PROPERTY_NAME_TOKEN_SECRET_SIGNING_ALG),
                        JWSAlgorithm.HS256.getName());

        final String encryptionSecretAlg =
                StringUtils.defaultString(getRegisteredServiceJwtSecret(service, TokenConstants.PROPERTY_NAME_TOKEN_SECRET_ENCRYPTION_ALG),
                        JWEAlgorithm.DIR.getName());

        final String encryptionSecretMethod =
                StringUtils.defaultString(getRegisteredServiceJwtSecret(service, TokenConstants.PROPERTY_NAME_TOKEN_SECRET_ENCRYPTION_METHOD),
                        EncryptionMethod.A192CBC_HS384.getName());

        if (StringUtils.isNotBlank(signingSecret)) {
            Set<Algorithm> sets = new HashSet<>();
            sets.addAll(JWSAlgorithm.Family.EC);
            sets.addAll(JWSAlgorithm.Family.HMAC_SHA);
            sets.addAll(JWSAlgorithm.Family.RSA);
            sets.addAll(JWSAlgorithm.Family.SIGNATURE);

            final JWSAlgorithm signingAlg = findAlgorithmFamily(sets, signingSecretAlg);

            final JwtAuthenticator a = new JwtAuthenticator();
            a.setSignatureConfiguration(new SecretSignatureConfiguration(signingSecret, signingAlg));

            if (StringUtils.isNotBlank(encryptionSecret)) {
                sets = new HashSet<>();
                sets.addAll(JWEAlgorithm.Family.AES_GCM_KW);
                sets.addAll(JWEAlgorithm.Family.AES_KW);
                sets.addAll(JWEAlgorithm.Family.ASYMMETRIC);
                sets.addAll(JWEAlgorithm.Family.ECDH_ES);
                sets.addAll(JWEAlgorithm.Family.PBES2);
                sets.addAll(JWEAlgorithm.Family.RSA);
                sets.addAll(JWEAlgorithm.Family.SYMMETRIC);

                final JWEAlgorithm encAlg = findAlgorithmFamily(sets, encryptionSecretAlg);

                sets = new HashSet<>();
                sets.addAll(EncryptionMethod.Family.AES_CBC_HMAC_SHA);
                sets.addAll(EncryptionMethod.Family.AES_GCM);

                final EncryptionMethod encMethod = findAlgorithmFamily(sets, encryptionSecretMethod);
                a.setEncryptionConfiguration(new SecretEncryptionConfiguration(encryptionSecret, encAlg, encMethod));
            } else {
                LOGGER.warn("JWT authentication is configured to share a single key for both signing/encryption");
            }
            return a;
        }
        LOGGER.warn("No token signing secret is defined for service [{}]. Ensure [{}] property is defined for service",
                service.getServiceId(), TokenConstants.PROPERTY_NAME_TOKEN_SECRET_SIGNING);
        return null;
    }

    private static <T extends Algorithm> T findAlgorithmFamily(final Set<Algorithm> family, final String alg) {
        return (T) family.stream().filter(l -> l.getName().equalsIgnoreCase(alg)).findFirst().get();
    }

    /**
     * Gets registered service jwt encryption secret.
     *
     * @param service the service
     * @return the registered service jwt secret
     */
    private String getRegisteredServiceJwtEncryptionSecret(final RegisteredService service) {
        return getRegisteredServiceJwtSecret(service, TokenConstants.PROPERTY_NAME_TOKEN_SECRET_ENCRYPTION);
    }

    /**
     * Gets registered service jwt signing secret.
     *
     * @param service the service
     * @return the registered service jwt secret
     */
    private String getRegisteredServiceJwtSigningSecret(final RegisteredService service) {
        return getRegisteredServiceJwtSecret(service, TokenConstants.PROPERTY_NAME_TOKEN_SECRET_SIGNING);
    }

    /**
     * Gets registered service jwt secret.
     *
     * @param service  the service
     * @param propName the prop name
     * @return the registered service jwt secret
     */
    protected String getRegisteredServiceJwtSecret(final RegisteredService service, final String propName) {
        if (service == null || !service.getAccessStrategy().isServiceAccessAllowed()) {
            LOGGER.debug("Service is not defined/found or its access is disabled in the registry");
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE);
        }
        if (service.getProperties().containsKey(propName)) {
            final RegisteredServiceProperty propSigning = service.getProperties().get(propName);
            final String tokenSigningSecret = propSigning.getValue();
            if (StringUtils.isNotBlank(tokenSigningSecret)) {
                LOGGER.debug("Found the secret value [{}] for service [{}]", propName, service.getServiceId());
                return tokenSigningSecret;
            }
        }
        LOGGER.warn("Service [{}] does not define a property [{}] in the registry",
                service.getServiceId(), propName);
        return null;
    }
}
