package org.apereo.cas.adaptors.trusted.authentication.handler.support;

import org.apereo.cas.adaptors.trusted.authentication.principal.PrincipalBearingCredential;
import org.apereo.cas.authentication.AbstractAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

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
@Slf4j
public class PrincipalBearingCredentialsAuthenticationHandler extends AbstractAuthenticationHandler {
    public PrincipalBearingCredentialsAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                                            final PrincipalFactory principalFactory, final Integer order) {
        super(name, servicesManager, principalFactory, order);
    }

    @Override
    public AuthenticationHandlerExecutionResult authenticate(final Credential credential) {
        LOGGER.debug("Trusting credential for: [{}]", credential);
        val bearingCredential = (PrincipalBearingCredential) credential;
        return new DefaultAuthenticationHandlerExecutionResult(this, bearingCredential, bearingCredential.getPrincipal());
    }

    @Override
    public boolean supports(final Class<? extends Credential> clazz) {
        return PrincipalBearingCredential.class.isAssignableFrom(clazz);
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof PrincipalBearingCredential;
    }
}
