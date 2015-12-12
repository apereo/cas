package org.jasig.cas.authentication.handler.support;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.handler.PrincipalNameTransformer;
import org.jasig.cas.integration.pac4j.authentication.handler.support.TokenWrapperAuthenticationHandler;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceProperty;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.services.UnauthorizedServiceException;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

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
    @NotNull
    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Override
    protected boolean preAuthenticate(final Credential credential) {
        final TokenCredential tokenCredential = (TokenCredential) credential;
        final RegisteredService service = this.servicesManager.findServiceBy(tokenCredential.getService());
        if (service == null || !service.getAccessStrategy().isServiceAccessAllowed()) {
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE);
        }

        if (service.getProperties().containsKey(TokenCredential.TOKEN_PARAMETER)) {
            final RegisteredServiceProperty prop = service.getProperties().get(TokenCredential.TOKEN_PARAMETER);
            final String tokenSecret = prop.getValue();

            if (StringUtils.isNotBlank(tokenSecret)) {
                final JwtAuthenticator tokenAuthenticator = new JwtAuthenticator(tokenSecret);
                setAuthenticator(tokenAuthenticator);
                return super.preAuthenticate(credential);
            }
        }
        logger.warn("No token secret is defined for service [{}]. Ensure [{}] property is defined for service",
                service.getServiceId(), TokenCredential.TOKEN_PARAMETER);
        return false;
    }

    @Autowired(required=false)
    @Override
    public void setPrincipalNameTransformer(@Qualifier("tokenPrincipalNameTransformer")
                                            final PrincipalNameTransformer principalNameTransformer) {
        if (principalNameTransformer != null) {
            super.setPrincipalNameTransformer(principalNameTransformer);
        }
    }

}
