package org.apereo.cas.adaptors.trusted.authentication.handler.support;

import org.apereo.cas.adaptors.trusted.authentication.principal.PrincipalBearingCredential;
import org.apereo.cas.authentication.AbstractAuthenticationHandler;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultHandlerResult;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AuthenticationHandler which authenticates Principal-bearing credentials.
 * Authentication must have occurred at the time the Principal-bearing
 * credentials were created, so we perform no further authentication. Thus
 * merely by being presented a PrincipalBearingCredential, this handler returns
 * true.
 *
 * @author Andrew Petro
 * @since 3.0.0
 */
public class PrincipalBearingCredentialsAuthenticationHandler extends AbstractAuthenticationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrincipalBearingCredentialsAuthenticationHandler.class);

    public PrincipalBearingCredentialsAuthenticationHandler(final String name, final ServicesManager servicesManager, final PrincipalFactory principalFactory) {
        super(name, servicesManager, principalFactory, null);
    }

    @Override
    public HandlerResult authenticate(final Credential credential) {
        LOGGER.debug("Trusting credential for: [{}]", credential);
        final PrincipalBearingCredential bearingCredential = (PrincipalBearingCredential) credential;
        return new DefaultHandlerResult(this, bearingCredential, bearingCredential.getPrincipal());
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof PrincipalBearingCredential;
    }
}
