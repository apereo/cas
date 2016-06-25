package org.apereo.cas.adaptors.rest;

import org.apereo.cas.authentication.AccountDisabledException;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.ArrayList;

/**
 * This is {@link RestAuthenticationHandler} that authenticates uid/password against a remote
 * rest endpoint based on the status code received. Credentials are passed via basic authn.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class RestAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {
    private RestAuthenticationApi api;

    public void setApi(final RestAuthenticationApi api) {
        this.api = api;
    }

    @Override
    protected HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential c)
            throws GeneralSecurityException, PreventedException {
        final ResponseEntity<Principal> authenticationResponse = api.authenticate(c);
        if (authenticationResponse.getStatusCode().value() == HttpStatus.FORBIDDEN.value()) {
            throw new AccountDisabledException("Could not authenticate forbidden account for " + c.getUsername());
        }
        if (authenticationResponse.getStatusCode().value() == HttpStatus.UNAUTHORIZED.value()) {
            throw new FailedLoginException("Could not authenticate account for " + c.getUsername());
        }
        if (authenticationResponse.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
            throw new AccountNotFoundException("Could not locate account for " + c.getUsername());
        }

        if (authenticationResponse.getStatusCode().value() == HttpStatus.OK.value()) {
            final Principal principalFromRest = authenticationResponse.getBody();
            return createHandlerResult(c,
                    this.principalFactory.createPrincipal(principalFromRest.getName()),
                    new ArrayList<>());
        }
        throw new FailedLoginException("Could not authenticate account for " + c.getUsername());
    }
}




