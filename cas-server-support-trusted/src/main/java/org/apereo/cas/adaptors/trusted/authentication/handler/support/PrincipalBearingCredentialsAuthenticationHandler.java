package org.apereo.cas.adaptors.trusted.authentication.handler.support;

import org.apereo.cas.adaptors.trusted.authentication.principal.PrincipalBearingCredential;
import org.apereo.cas.authentication.AbstractAuthenticationHandler;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultHandlerResult;
import org.apereo.cas.authentication.HandlerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.GeneralSecurityException;

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

    private transient Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public HandlerResult authenticate(final Credential credential) throws GeneralSecurityException {
        logger.debug("Trusting credential for: {}", credential);
        return new DefaultHandlerResult(
                this, (PrincipalBearingCredential) credential, this.principalFactory.createPrincipal(credential.getId()));
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof PrincipalBearingCredential;
    }
}
