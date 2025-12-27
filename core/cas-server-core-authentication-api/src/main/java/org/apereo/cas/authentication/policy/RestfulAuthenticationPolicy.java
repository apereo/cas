package org.apereo.cas.authentication.policy;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationPolicyExecutionResult;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.core.authentication.RestAuthenticationPolicyProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.hc.core5.http.HttpResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

/**
 * This is {@link RestfulAuthenticationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true, exclude = "properties")
@Setter
@Getter
@AllArgsConstructor
@Accessors(chain = true)
@Slf4j
public class RestfulAuthenticationPolicy extends BaseAuthenticationPolicy {
    @Serial
    private static final long serialVersionUID = -7688729533538097898L;

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final RestAuthenticationPolicyProperties properties;

    private static Exception handleResponseStatusCode(final HttpStatus statusCode, final Principal principal) {
        return switch (statusCode) {
            case FORBIDDEN, METHOD_NOT_ALLOWED -> new AccountDisabledException("Could not authenticate forbidden account for " + principal.getId());
            case UNAUTHORIZED -> new FailedLoginException("Could not authenticate account for " + principal.getId());
            case NOT_FOUND -> new AccountNotFoundException("Could not locate account for " + principal.getId());
            case LOCKED -> new AccountLockedException("Could not authenticate locked account for " + principal.getId());
            case PRECONDITION_FAILED -> new AccountExpiredException("Could not authenticate expired account for " + principal.getId());
            case PRECONDITION_REQUIRED -> new AccountPasswordMustChangeException("Account password must change for " + principal.getId());
            default -> new FailedLoginException("Rest endpoint returned an unknown status code " + statusCode);
        };
    }

    @Override
    public AuthenticationPolicyExecutionResult isSatisfiedBy(@Nullable final Authentication authentication,
                                                             final Set<AuthenticationHandler> authenticationHandlers,
                                                             final ConfigurableApplicationContext applicationContext,
                                                             final Map<String, ? extends Serializable> context) throws Exception {
        if (authentication == null) {
            LOGGER.warn("Authentication attempt is null and cannot satisfy policy");
            return AuthenticationPolicyExecutionResult.failure();
        }
        HttpResponse response = null;
        val principal = authentication.getPrincipal();
        try {
            val entity = MAPPER.writeValueAsString(principal);
            val headers = CollectionUtils.<String, String>wrap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            headers.putAll(Objects.requireNonNull(properties).getHeaders());
            val exec = HttpExecutionRequest.builder()
                .url(properties.getUrl())
                .basicAuthPassword(properties.getBasicAuthUsername())
                .basicAuthUsername(properties.getBasicAuthPassword())
                .method(HttpMethod.POST)
                .entity(entity)
                .headers(headers)
                .maximumRetryAttempts(properties.getMaximumRetryAttempts())
                .build();
            response = HttpUtils.execute(exec);
            val statusCode = HttpStatus.valueOf(response.getCode());
            if (statusCode != HttpStatus.OK) {
                val ex = handleResponseStatusCode(statusCode, principal);
                throw new GeneralSecurityException(ex);
            }
            return AuthenticationPolicyExecutionResult.success();
        } finally {
            HttpUtils.close(response);
        }
    }
}
