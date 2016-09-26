package org.apereo.cas.authentication.handler.support;

import com.google.common.collect.Lists;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.TokenConstants;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.integration.pac4j.authentication.handler.support.AbstractTokenWrapperAuthenticationHandler;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.jwt.config.encryption.RSAEncryptionConfiguration;
import org.pac4j.jwt.config.encryption.SecretEncryptionConfiguration;
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.pac4j.jwt.profile.JwtGenerator;

/**
 * This is {@link TokenAuthenticationHandler} that authenticates instances of {@link TokenCredential}.
 * There is no need for a separate {@link PrincipalResolver} component
 * as this handler will auto-populate the principal attributes itself.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public class TokenAuthenticationHandler extends AbstractTokenWrapperAuthenticationHandler {
    @Override
    protected HandlerResult postAuthenticate(final Credential credential, final HandlerResult result) {
        final TokenCredential tokenCredential = (TokenCredential) credential;
        tokenCredential.setId(result.getPrincipal().getId());
        return super.postAuthenticate(credential, result);
    }

    @Override
    protected Authenticator<TokenCredentials> getAuthenticator(final Credential credential) {
        final TokenCredential tokenCredential = (TokenCredential) credential;
        logger.debug("Locating token secret for service [{}]", tokenCredential.getService());

        final RegisteredService service = this.servicesManager.findServiceBy(tokenCredential.getService());
        final String signingSecret = getRegisteredServiceJwtSigningSecret(service);
        final String encryptionSecret = getRegisteredServiceJwtEncryptionSecret(service);

        if (StringUtils.isNotBlank(signingSecret)) {
            if (StringUtils.isBlank(encryptionSecret)) {
                logger.warn("JWT authentication is configured to share a single key for both signing/encryption");
                return new JwtAuthenticator(Lists.newArrayList(new SecretSignatureConfiguration(signingSecret, JWSAlgorithm.HS256)));
            }
            final JwtAuthenticator a = new JwtAuthenticator();
            a.setSignatureConfiguration(new SecretSignatureConfiguration(signingSecret, JWSAlgorithm.HS256));
            a.setEncryptionConfiguration(new SecretEncryptionConfiguration(encryptionSecret,
                    JWEAlgorithm.DIR, EncryptionMethod.A192CBC_HS384));
            return a;
        }
        logger.warn("No token signing secret is defined for service [{}]. Ensure [{}] property is defined for service",
                    service.getServiceId(), TokenConstants.PROPERTY_NAME_TOKEN_SECRET_SIGNING);
        return null;
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
            logger.debug("Service is not defined/found or its access is disabled in the registry");
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE);
        }
        if (service.getProperties().containsKey(propName)) {
            final RegisteredServiceProperty propSigning = service.getProperties().get(propName);
            final String tokenSigningSecret = propSigning.getValue();
            if (StringUtils.isNotBlank(tokenSigningSecret)) {
                logger.debug("Found the secret value {} for service [{}]", propName, service.getServiceId());
                return tokenSigningSecret;
            }
        }
        logger.warn("Service [{}] does not define a property [{}] in the registry",
                service.getServiceId(), propName);
        return null;
    }
}
