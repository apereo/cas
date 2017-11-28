package org.apereo.cas.adaptors.rest;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.SimplePrincipal;
import org.apereo.cas.services.ServicesManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
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

    private final RestAuthenticationApi api;

    public RestAuthenticationHandler(final String name, final RestAuthenticationApi api, final ServicesManager servicesManager,
                                     final PrincipalFactory principalFactory) {
        super(name, servicesManager, principalFactory, null);
        this.api = api;
    }

    @Override
    protected HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential c, final String originalPassword)
            throws GeneralSecurityException {

        try {
            final UsernamePasswordCredential creds = new UsernamePasswordCredential(c.getUsername(), c.getPassword());
            
            final ResponseEntity<SimplePrincipal> authenticationResponse = api.authenticate(creds);
            if (authenticationResponse.getStatusCode() == HttpStatus.OK) {
                final SimplePrincipal principalFromRest = authenticationResponse.getBody();
                if (principalFromRest == null || StringUtils.isBlank(principalFromRest.getId())) {
                    throw new FailedLoginException("Could not determine authentication response from rest endpoint for " + c.getUsername());
                }
                final Principal principal = this.principalFactory.createPrincipal(principalFromRest.getId(), principalFromRest.getAttributes());
                return createHandlerResult(c, principal, new ArrayList<>());
            }
        } catch (final HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw new AccountDisabledException("Could not authenticate forbidden account for " + c.getUsername());
            }
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new FailedLoginException("Could not authenticate account for " + c.getUsername());
            }
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new AccountNotFoundException("Could not locate account for " + c.getUsername());
            }
            if (e.getStatusCode() == HttpStatus.LOCKED) {
                throw new AccountLockedException("Could not authenticate locked account for " + c.getUsername());
            }
            if (e.getStatusCode() == HttpStatus.PRECONDITION_FAILED) {
                throw new AccountExpiredException("Could not authenticate expired account for " + c.getUsername());
            }
            if (e.getStatusCode() == HttpStatus.PRECONDITION_REQUIRED) {
                throw new AccountPasswordMustChangeException("Account password must change for " + c.getUsername());
            }
            throw new FailedLoginException("Rest endpoint returned an unknown status code "
                    + e.getStatusCode() + " for " + c.getUsername());
        }
        throw new FailedLoginException("Rest endpoint returned an unknown response for " + c.getUsername());
    }
}




