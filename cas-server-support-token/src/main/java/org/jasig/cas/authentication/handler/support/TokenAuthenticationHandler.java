package org.jasig.cas.authentication.handler.support;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.TokenConstants;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.handler.PrincipalNameTransformer;
import org.jasig.cas.integration.pac4j.authentication.handler.support.TokenWrapperAuthenticationHandler;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceProperty;
import org.jasig.cas.services.UnauthorizedServiceException;
import org.pac4j.http.profile.HttpProfile;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.pac4j.jwt.profile.JwtGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.security.GeneralSecurityException;

/**
 * This is {@link TokenAuthenticationHandler} that authenticates instances of {@link TokenCredential}.
 * There is no need for a separate {@link org.jasig.cas.authentication.principal.PrincipalResolver} component
 * as this handler will auto-populate the principal attributes itself.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Component("tokenAuthenticationHandler")
public class TokenAuthenticationHandler extends TokenWrapperAuthenticationHandler {

    @Override
    protected final HandlerResult doAuthentication(final Credential credential) throws GeneralSecurityException, PreventedException {
        final TokenCredential tokenCredential = (TokenCredential) credential;
        logger.debug("Locating token secret for service [{}]", tokenCredential.getService());

        final RegisteredService service = this.servicesManager.findServiceBy(tokenCredential.getService());
        final String tokenSecret = getRegisteredServiceJwtSecret(service);

        if (StringUtils.isNotBlank(tokenSecret)) {
            final JwtAuthenticator tokenAuthenticator = new JwtAuthenticator(tokenSecret);
            setAuthenticator(tokenAuthenticator);
            final HandlerResult result = super.doAuthentication(credential);
            tokenCredential.setId(result.getPrincipal().getId());
            logger.info("Authenticated token credential successfully as [{}]", tokenCredential.getId());
            return result;
        } else {
            logger.warn("No token secret is defined for service [{}]. Ensure [{}] property is defined for service",
                    service.getServiceId(), TokenConstants.PARAMETER_NAME_TOKEN);
        }
        throw new PreventedException(new IllegalArgumentException("Token secret is undefined"));
    }

    @Autowired(required=false)
    @Override
    public final void setPrincipalNameTransformer(@Qualifier("tokenPrincipalNameTransformer")
                                            final PrincipalNameTransformer principalNameTransformer) {
        if (principalNameTransformer != null) {
            super.setPrincipalNameTransformer(principalNameTransformer);
        }
    }

    /** Gets registered service jwt secret.
    *
    * @param service the service
    * @return the registered service jwt secret
    */
    protected String getRegisteredServiceJwtSecret(final RegisteredService service) {
        if (service == null || !service.getAccessStrategy().isServiceAccessAllowed()) {
            logger.debug("Service is not defined or its access is disabled in the registry");
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE);
        }

        if (service.getProperties().containsKey(TokenConstants.PROPERTY_NAME_TOKEN_SECRET)) {
            final RegisteredServiceProperty prop = service.getProperties().get(TokenConstants.PROPERTY_NAME_TOKEN_SECRET);
            final String tokenSecret = prop.getValue();

            if (StringUtils.isNotBlank(tokenSecret)) {
                logger.debug("Found {} for service [{}]",
                        TokenConstants.PROPERTY_NAME_TOKEN_SECRET, service.getServiceId());
                return tokenSecret;
            }
        }
        logger.debug("Service [{}] does not define a property [{}] in the registry", service.getServiceId(),
                TokenConstants.PROPERTY_NAME_TOKEN_SECRET);
        return null;
    }

}
