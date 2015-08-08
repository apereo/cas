package org.jasig.cas.support.wsfederation.authentication.handler.support;

import org.jasig.cas.support.wsfederation.authentication.principal.WsFederationCredential;
import org.jasig.cas.MessageDescriptor;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.jasig.cas.authentication.principal.Principal;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

/**
 * This handler authenticates Security token/credentials.
 *
 * @author John Gasper
 * @since 4.2.0
 */
public final class WsFederationAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    /**
     * Determines if this handler can support the credentials provided.
     *
     * @param credentials the credentials to test
     * @return true if supported, otherwise false
     */
    @Override
    public boolean supports(final Credential credentials) {
        return credentials != null && WsFederationCredential.class.isAssignableFrom(credentials.getClass());
    }

    @Override
    protected HandlerResult doAuthentication(final Credential credential) throws GeneralSecurityException, PreventedException {
        final WsFederationCredential wsFederationCredentials = (WsFederationCredential) credential;
        if (wsFederationCredentials != null) {
            final Principal principal = this.principalFactory.createPrincipal(wsFederationCredentials.getId(),
                    wsFederationCredentials.getAttributes());

            return this.createHandlerResult(wsFederationCredentials, principal, new ArrayList<MessageDescriptor>());
        }
        throw new FailedLoginException();
    }

}
