package org.apereo.cas.adaptors.rest;

import org.apereo.cas.DefaultMessageDescriptor;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.support.password.PasswordExpiringWarningMessageDescriptor;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link RestAuthenticationHandler} that authenticates uid/password against a remote
 * rest endpoint based on the status code received. Credentials are passed via basic authn.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class RestAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    /**
     * Header name that explains the password expiration date.
     */
    public static final String HEADER_NAME_CAS_PASSWORD_EXPIRATION_DATE = "X-CAS-PasswordExpirationDate";
    /**
     * Header name that explains the warnings.
     */
    public static final String HEADER_NAME_CAS_WARNING = "X-CAS-Warning";

    private final RestAuthenticationApi api;

    public RestAuthenticationHandler(final String name, final RestAuthenticationApi api,
                                     final ServicesManager servicesManager,
                                     final PrincipalFactory principalFactory) {
        super(name, servicesManager, principalFactory, null);
        this.api = api;
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential,
                                                                                        final String originalPassword)
        throws GeneralSecurityException {
        try {
            val creds = new UsernamePasswordCredential(credential.getUsername(), credential.getPassword());
            val authenticationResponse = api.authenticate(creds);
            if (authenticationResponse.getStatusCode() == HttpStatus.OK) {
                val principalFromRest = authenticationResponse.getBody();
                if (principalFromRest == null || StringUtils.isBlank(principalFromRest.getId())) {
                    throw new FailedLoginException("Could not determine authentication response from rest endpoint for " + credential.getUsername());
                }
                val principal = principalFactory.createPrincipal(principalFromRest.getId(), principalFromRest.getAttributes());
                return createHandlerResult(credential, principal, getWarnings(authenticationResponse));
            }
        } catch (final HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw new AccountDisabledException("Could not authenticate forbidden account for " + credential.getUsername());
            }
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new FailedLoginException("Could not authenticate account for " + credential.getUsername());
            }
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new AccountNotFoundException("Could not locate account for " + credential.getUsername());
            }
            if (e.getStatusCode() == HttpStatus.LOCKED) {
                throw new AccountLockedException("Could not authenticate locked account for " + credential.getUsername());
            }
            if (e.getStatusCode() == HttpStatus.PRECONDITION_FAILED) {
                throw new AccountExpiredException("Could not authenticate expired account for " + credential.getUsername());
            }
            if (e.getStatusCode() == HttpStatus.PRECONDITION_REQUIRED) {
                throw new AccountPasswordMustChangeException("Account password must change for " + credential.getUsername());
            }
            throw new FailedLoginException("Rest endpoint returned an unknown status code "
                + e.getStatusCode() + " for " + credential.getUsername());
        }
        throw new FailedLoginException("Rest endpoint returned an unknown response for " + credential.getUsername());
    }

    /**
     * Resolve {@link MessageDescriptor warnings} from the {@link ResponseEntity authenticationResponse}.
     *
     * @param authenticationResponse The response sent by the REST authentication endpoint
     * @return The warnings for the created {@link AuthenticationHandlerExecutionResult}
     */
    protected List<MessageDescriptor> getWarnings(final ResponseEntity<?> authenticationResponse) {
        val messageDescriptors = new ArrayList<MessageDescriptor>(2);

        val passwordExpirationDate = authenticationResponse.getHeaders()
            .getFirstZonedDateTime(HEADER_NAME_CAS_PASSWORD_EXPIRATION_DATE);
        if (passwordExpirationDate != null) {
            val days = Duration.between(Instant.now(Clock.systemUTC()), passwordExpirationDate).toDays();
            messageDescriptors.add(new PasswordExpiringWarningMessageDescriptor(null, days));
        }

        val warnings = authenticationResponse.getHeaders().get(HEADER_NAME_CAS_WARNING);
        if (warnings != null) {
            warnings.stream()
                .map(DefaultMessageDescriptor::new)
                .forEach(messageDescriptors::add);
        }

        return messageDescriptors;
    }
}




