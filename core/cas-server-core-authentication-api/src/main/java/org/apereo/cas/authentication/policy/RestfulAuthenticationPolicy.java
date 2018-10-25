package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationPolicy;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.CollectionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.Set;

/**
 * This is {@link RestfulAuthenticationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class RestfulAuthenticationPolicy implements AuthenticationPolicy {
    private final transient RestTemplate restTemplate;
    private final String endpoint;

    @Override
    public boolean isSatisfiedBy(final Authentication authentication, final Set<AuthenticationHandler> authenticationHandlers) throws Exception {
        val principal = authentication.getPrincipal();
        try {
            val acceptHeaders = new HttpHeaders();
            acceptHeaders.setAccept(CollectionUtils.wrap(MediaType.APPLICATION_JSON));
            val entity = new HttpEntity<Principal>(principal, acceptHeaders);
            LOGGER.warn("Checking authentication policy for [{}] via POST at [{}]", principal, this.endpoint);
            val resp = restTemplate.exchange(this.endpoint, HttpMethod.POST, entity, String.class);
            val statusCode = resp.getStatusCode();
            if (statusCode != HttpStatus.OK) {
                val ex = handleResponseStatusCode(statusCode, principal);
                throw new GeneralSecurityException(ex);
            }
            return true;
        } catch (final HttpClientErrorException | HttpServerErrorException.InternalServerError e) {
            val ex = handleResponseStatusCode(e.getStatusCode(), authentication.getPrincipal());
            throw new GeneralSecurityException(ex);
        }
    }

    private static Exception handleResponseStatusCode(final HttpStatus statusCode, final Principal p) {
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
