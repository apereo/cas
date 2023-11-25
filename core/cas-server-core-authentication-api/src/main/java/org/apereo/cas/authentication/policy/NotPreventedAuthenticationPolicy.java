package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationPolicyExecutionResult;
import org.apereo.cas.authentication.PreventedException;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * Authentication policy that defines success as at least one authentication success and no authentication attempts
 * that were prevented by system errors. This policy may be a desirable alternative to {@link AtLeastOneCredentialValidatedAuthenticationPolicy}
 * for cases where deployers wish to fail closed for indeterminate security events.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Slf4j
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@EqualsAndHashCode(callSuper = true)
@Setter
@Getter
@Accessors(chain = true)
public class NotPreventedAuthenticationPolicy extends AtLeastOneCredentialValidatedAuthenticationPolicy {

    @Serial
    private static final long serialVersionUID = -591956246302374794L;

    public NotPreventedAuthenticationPolicy() {
        super(true);
    }

    @Override
    public AuthenticationPolicyExecutionResult isSatisfiedBy(final Authentication authentication,
                                                             final Set<AuthenticationHandler> authenticationHandlers,
                                                             final ConfigurableApplicationContext applicationContext,
                                                             final Map<String, ? extends Serializable> context) throws Exception {
        val fail = authentication.getFailures().values()
            .stream()
            .anyMatch(failure -> failure.getClass().isAssignableFrom(PreventedException.class));
        if (fail) {
            LOGGER.warn("Authentication policy has failed given at least one authentication failure is found to prevent authentication");
            return AuthenticationPolicyExecutionResult.failure();
        }
        return super.isSatisfiedBy(authentication, authenticationHandlers, applicationContext, context);
    }
}
