package org.apereo.cas.authentication;

import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;

import java.security.GeneralSecurityException;

/**
 * This is {@link CouchbaseAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CouchbaseAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {
    public CouchbaseAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                          final PrincipalFactory principalFactory, final Integer order) {
        super(name, servicesManager, principalFactory, order);
    }

    @Override
    protected HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential transformedCredential,
                                                                 final String originalPassword) throws GeneralSecurityException, PreventedException {
        return null;
    }
}
