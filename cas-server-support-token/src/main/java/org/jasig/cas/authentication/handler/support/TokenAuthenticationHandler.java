package org.jasig.cas.authentication.handler.support;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.TokenConstants;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.handler.PrincipalNameTransformer;
import org.jasig.cas.integration.pac4j.authentication.handler.support.AbstractTokenWrapperAuthenticationHandler;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceProperty;
import org.jasig.cas.services.UnauthorizedServiceException;
import org.pac4j.http.credentials.authenticator.Authenticator;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * This is {@link TokenAuthenticationHandler} that authenticates instances of {@link TokenCredential}.
 * There is no need for a separate {@link org.jasig.cas.authentication.principal.PrincipalResolver} component
 * as this handler will auto-populate the principal attributes itself.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Component("tokenAuthenticationHandler")
public class TokenAuthenticationHandler extends AbstractTokenWrapperAuthenticationHandler {
    @Override
    protected HandlerResult postAuthenticate(final Credential credential, final HandlerResult result) {
        final TokenCredential tokenCredential = (TokenCredential) credential;
        tokenCredential.setId(result.getPrincipal().getId());
        return super.postAuthenticate(credential, result);
    }

    @Override
    protected Authenticator getAuthenticator(final Credential credential) {
        final TokenCredential tokenCredential = (TokenCredential) credential;
        logger.debug("Locating token secret for service [{}]", tokenCredential.getService());

        final RegisteredService service = this.servicesManager.findServiceBy(tokenCredential.getService());
        final String signingSecret = getRegisteredServiceJwtSigningSecret(service);
        final String encryptionSecret = getRegisteredServiceJwtEncryptionSecret(service);

        if (StringUtils.isNotBlank(signingSecret)) {
            if (StringUtils.isBlank(encryptionSecret)) {
                logger.warn("JWT authentication is configured to share a single key for both signing/encryption");
                return new JwtAuthenticator(signingSecret);
            }
            return new JwtAuthenticator(signingSecret, encryptionSecret);
        }
        logger.warn("No token signing secret is defined for service [{}]. Ensure [{}] property is defined for service",
                    service.getServiceId(), TokenConstants.PROPERTY_NAME_TOKEN_SECRET_SIGNING);
        return null;
    }

    @Autowired(required=false)
    @Override
    public final void setPrincipalNameTransformer(@Qualifier("tokenPrincipalNameTransformer")
                                            final PrincipalNameTransformer principalNameTransformer) {
        if (principalNameTransformer != null) {
            super.setPrincipalNameTransformer(principalNameTransformer);
        }
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
