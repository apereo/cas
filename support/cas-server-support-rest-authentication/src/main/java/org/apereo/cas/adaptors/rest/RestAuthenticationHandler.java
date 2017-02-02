package org.apereo.cas.adaptors.rest;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AccountDisabledException;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.SimplePrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
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

        try {
            final UsernamePasswordCredential creds = new UsernamePasswordCredential(c.getUsername(), c.getPassword());
            
            final ResponseEntity<SimplePrincipal> authenticationResponse = api.authenticate(creds);
            if (authenticationResponse.getStatusCode().value() == HttpStatus.OK.value()) {
                final SimplePrincipal principalFromRest = authenticationResponse.getBody();
                if (principalFromRest == null || StringUtils.isBlank(principalFromRest.getId())) {
                    throw new FailedLoginException("Could not determine authentication response from rest endpoint for "
                            + c.getUsername());
                }
                return createHandlerResult(c,
                        this.principalFactory.createPrincipal(principalFromRest.getId(), principalFromRest.getAttributes()),
                        new ArrayList<>());
            }
        } catch (final HttpClientErrorException e) {
            if (e.getStatusCode().value() == HttpStatus.FORBIDDEN.value()) {
                throw new AccountDisabledException("Could not authenticate forbidden account for " + c.getUsername());
            }
            if (e.getStatusCode().value() == HttpStatus.UNAUTHORIZED.value()) {
                throw new FailedLoginException("Could not authenticate account for " + c.getUsername());
            }
            if (e.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                throw new AccountNotFoundException("Could not locate account for " + c.getUsername());
            }
            throw new FailedLoginException("Rest endpoint returned an unknown status code "
                    + e.getStatusCode() + " for " + c.getUsername());
        }
        throw new FailedLoginException("Rest endpoint returned an unknown response for " + c.getUsername());
    }
}




