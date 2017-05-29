package org.apereo.cas.authentication;

import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.GeneralSecurityException;

/**
 * This is {@link CassandraAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CassandraAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraAuthenticationHandler.class);

    public CassandraAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                          final PrincipalFactory principalFactory, final Integer order) {
        super(name, servicesManager, principalFactory, order);
    }

    @Override
    protected HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential transformedCredential,
                                                                 final String originalPassword) throws GeneralSecurityException, PreventedException {
        return null;
    }
}
