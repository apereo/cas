package org.jasig.cas.adaptors.trusted.authentication.handler.support;

import org.jasig.cas.adaptors.trusted.authentication.principal.PrincipalBearingCredential;
import org.jasig.cas.authentication.AbstractAuthenticationHandler;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.DefaultHandlerResult;
import org.jasig.cas.authentication.HandlerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
@Component("principalBearingCredentialsAuthenticationHandler")
public final class PrincipalBearingCredentialsAuthenticationHandler extends AbstractAuthenticationHandler {

    private final transient Logger logger = LoggerFactory.getLogger(this.getClass());

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
