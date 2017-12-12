package org.apereo.cas.authentication;

import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Map;

/**
 * This is {@link AzureActiveDirectoryAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class AzureActiveDirectoryAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureActiveDirectoryAuthenticationHandler.class);

    public AzureActiveDirectoryAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                                     final PrincipalFactory principalFactory) {
        super(name, servicesManager, principalFactory, null);
    }

    /**
     * Determines if this handler can support the credentials provided.
     *
     * @param credentials the credentials to test
     * @return true if supported, otherwise false
     */
    @Override
    public boolean supports(final Credential credentials) {
        return credentials != null && AzureActiveDirectoryCredential.class.isAssignableFrom(credentials.getClass());
    }

    @Override
    protected HandlerResult doAuthentication(final Credential credential) throws GeneralSecurityException {
        final AzureActiveDirectoryCredential credentials = (AzureActiveDirectoryCredential) credential;
        if (credentials != null) {
            final Map attributes = credentials.getClaims();
            final Principal principal = this.principalFactory.createPrincipal(credentials.getId(), attributes);
            LOGGER.debug("Authentication principal [{}] from credential [{}]", principal, credential);
            return this.createHandlerResult(credentials, principal, new ArrayList<>());
        }
        throw new FailedLoginException();
    }

}

