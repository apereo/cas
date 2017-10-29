package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationPolicy;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;

/**
 * This is {@link RestfulAuthenticationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class RestfulAuthenticationPolicy implements AuthenticationPolicy {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestfulAuthenticationPolicy.class);

    private final RestTemplate restTemplate;
    private final String endpoint;

    public RestfulAuthenticationPolicy(final RestTemplate restTemplate, final String endpoint) {
        this.restTemplate = restTemplate;
        this.endpoint = endpoint;
    }

    @Override
    public boolean isSatisfiedBy(final Authentication authentication) throws Exception {
        try {
            final HttpHeaders acceptHeaders = new HttpHeaders();
            acceptHeaders.setAccept(CollectionUtils.wrap(MediaType.APPLICATION_JSON));
            final HttpEntity<Principal> entity = new HttpEntity<>(authentication.getPrincipal(), acceptHeaders);
            LOGGER.warn("Checking authentication policy for [{}] via POST at [{}]", authentication.getPrincipal(), this.endpoint);
            final ResponseEntity<String> resp = restTemplate.exchange(this.endpoint, HttpMethod.POST, entity, String.class);
            if (resp == null) {
                LOGGER.warn("[{}] returned no responses", this.endpoint);
                throw new GeneralSecurityException("No response returned from REST endpoint to determine authentication policy");
            }
            if (resp.getStatusCode() != HttpStatus.OK) {
                final Exception ex = handleResponseStatusCode(resp.getStatusCode(), authentication.getPrincipal());
                throw new GeneralSecurityException(ex);
            }
            return true;
        } catch (final HttpClientErrorException e) {
            final Exception ex = handleResponseStatusCode(e.getStatusCode(), authentication.getPrincipal());
            throw new GeneralSecurityException(ex);
        }
    }

    private Exception handleResponseStatusCode(final HttpStatus statusCode, final Principal p) {
        if (statusCode == HttpStatus.FORBIDDEN || statusCode == HttpStatus.METHOD_NOT_ALLOWED) {
            return new AccountDisabledException("Could not authenticate forbidden account for " + p.getId());
        }
        if (statusCode == HttpStatus.UNAUTHORIZED) {
            return new FailedLoginException("Could not authenticate account for " + p.getId());
        }
        if (statusCode == HttpStatus.NOT_FOUND) {
            return new AccountNotFoundException("Could not locate account for " + p.getId());
        }
        if (statusCode == HttpStatus.LOCKED) {
            return new AccountLockedException("Could not authenticate locked account for " + p.getId());
        }
        if (statusCode == HttpStatus.PRECONDITION_FAILED) {
            return new AccountExpiredException("Could not authenticate expired account for " + p.getId());
        }
        if (statusCode == HttpStatus.PRECONDITION_REQUIRED) {
            return new AccountPasswordMustChangeException("Account password must change for " + p.getId());
        }
        return new FailedLoginException("Rest endpoint returned an unknown status code " + statusCode);
    }
}
