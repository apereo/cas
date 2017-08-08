package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.integration.pac4j.authentication.handler.support.UsernamePasswordWrapperAuthenticationHandler;
import org.apereo.cas.services.ServicesManager;

/**
 * An authentication handler to verify credentials against a MongoDb instance.
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public class MongoAuthenticationHandler extends UsernamePasswordWrapperAuthenticationHandler {

    public MongoAuthenticationHandler(final String name, final ServicesManager servicesManager, final PrincipalFactory principalFactory) {
        super(name, servicesManager, principalFactory, null);
    }
}
