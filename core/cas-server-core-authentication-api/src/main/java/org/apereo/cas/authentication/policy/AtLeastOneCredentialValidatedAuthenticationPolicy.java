package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationPolicyExecutionResult;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

/**
 * Authentication policy that is satisfied by at least one successfully authenticated credential.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Slf4j
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
@Setter
@Getter
@AllArgsConstructor
public class AtLeastOneCredentialValidatedAuthenticationPolicy extends BaseAuthenticationPolicy {

    private static final long serialVersionUID = -7484490540437793931L;

    /**
     * Flag to try all credentials before policy is satisfied. Defaults to {@code false}.
     */
    private final boolean tryAll;

    @Override
    public AuthenticationPolicyExecutionResult isSatisfiedBy(final Authentication authn,
                                                             final Set<AuthenticationHandler> authenticationHandlers,
                                                             final ConfigurableApplicationContext applicationContext,
                                                             final Optional<Serializable> assertion) throws Exception {
        if (this.tryAll) {
            val sum = authn.getSuccesses().size() + authn.getFailures().size();
            if (authenticationHandlers.size() != sum) {
                LOGGER.warn("Credentials count [{}] does not match the sum of authentication successes and failures [{}]",
                    authn.getCredentials().size(), sum);
                return AuthenticationPolicyExecutionResult.failure();
            }
            LOGGER.debug("Authentication policy is satisfied with all authentication transactions");
            return AuthenticationPolicyExecutionResult.success(!authn.getSuccesses().isEmpty());
        }
        if (!authn.getSuccesses().isEmpty()) {
            LOGGER.debug("Authentication policy is satisfied having found at least one authentication transactions");
            return AuthenticationPolicyExecutionResult.success();
        }
        LOGGER.warn("Authentication policy has failed to find a successful authentication transaction");
        return AuthenticationPolicyExecutionResult.failure();
    }
}
