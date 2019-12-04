package org.apereo.cas.azure.ad.authentication;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;

import java.security.GeneralSecurityException;

/**
 * This is {@link AzureActiveDirectoryAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public class AzureActiveDirectoryAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {
    public AzureActiveDirectoryAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                                     final PrincipalFactory principalFactory, final Integer order) {
        super(name, servicesManager, principalFactory, order);
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential,
                                                                                        final String originalPassword)
        throws GeneralSecurityException, PreventedException {
        return null;
    }
}
