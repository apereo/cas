package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.integration.pac4j.authentication.handler.support.UsernamePasswordWrapperAuthenticationHandler;
import org.apereo.cas.services.ServicesManager;

import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link CouchDbAuthenticationHandler}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Getter
@Setter
public class CouchDbAuthenticationHandler extends UsernamePasswordWrapperAuthenticationHandler {
    public CouchDbAuthenticationHandler(final String name, final ServicesManager servicesManager, final PrincipalFactory principalFactory, final int order) {
        super(name, servicesManager, principalFactory, order);
    }
}
