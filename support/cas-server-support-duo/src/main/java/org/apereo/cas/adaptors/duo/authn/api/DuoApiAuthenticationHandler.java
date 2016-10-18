package org.apereo.cas.adaptors.duo.authn.api;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

/**
 * This is {@link DuoApiAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DuoApiAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {
    private DuoApiAuthenticationService duoApiAuthenticationService;

    public void setDuoApiAuthenticationService(final DuoApiAuthenticationService duoApiAuthenticationService) {
        this.duoApiAuthenticationService = duoApiAuthenticationService;
    }

    @Override
    protected HandlerResult doAuthentication(final Credential credential)
            throws GeneralSecurityException, PreventedException {

        final DuoApiCredential c = DuoApiCredential.class.cast(credential);

        if (this.duoApiAuthenticationService.authenticate(c)) {
            final Principal principal = c.getAuthentication().getPrincipal();
            return createHandlerResult(credential, principal, new ArrayList<>());
        }
        throw new FailedLoginException("Duo authentication has failed");
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof DuoApiCredential;
    }
}
