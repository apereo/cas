package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ConfigurableApplicationContext;
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
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
@Setter
@Getter
@AllArgsConstructor
public class RestfulAuthenticationPolicy extends BaseAuthenticationPolicy {
    private static final long serialVersionUID = -7688729533538097898L;

    private String endpoint;

    private String basicAuthUsername;

    private String basicAuthPassword;

    public RestfulAuthenticationPolicy(final String endpoint) {
        this.endpoint = endpoint;
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

    @Override
    public boolean isSatisfiedBy(final Authentication authentication,
                                 final Set<AuthenticationHandler> authenticationHandlers,
                                 final ConfigurableApplicationContext applicationContext) throws Exception {
        val principal = authentication.getPrincipal();
        try {
            val entity = buildHttpEntity(principal);
            LOGGER.debug("Checking authentication policy for [{}] via POST at [{}]", principal, this.endpoint);

            val restTemplate = new RestTemplate();
            val resp = restTemplate.exchange(this.endpoint, HttpMethod.POST, entity, String.class);
            val statusCode = resp.getStatusCode();
            if (statusCode != HttpStatus.OK) {
                val ex = handleResponseStatusCode(statusCode, principal);
                throw new GeneralSecurityException(ex);
            }
            return true;
        } catch (final HttpClientErrorException | HttpServerErrorException e) {
            val ex = handleResponseStatusCode(e.getStatusCode(), authentication.getPrincipal());
            throw new GeneralSecurityException(ex);
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        return false;
    }

    /**
     * Build http entity.
     *
     * @param principal the principal
     * @return the http entity
     */
    protected HttpEntity<Principal> buildHttpEntity(final Principal principal) {
        val headers = new HttpHeaders();
        headers.setAccept(CollectionUtils.wrap(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (StringUtils.isNotBlank(this.basicAuthUsername) && StringUtils.isNotBlank(this.basicAuthPassword)) {
            headers.putAll(HttpUtils.createBasicAuthHeaders(basicAuthUsername, basicAuthPassword));
        }
        return new HttpEntity<>(principal, headers);
    }
}
