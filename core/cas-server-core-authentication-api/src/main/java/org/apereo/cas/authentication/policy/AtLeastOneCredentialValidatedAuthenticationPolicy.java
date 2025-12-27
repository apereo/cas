package org.apereo.cas.authentication.policy;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationPolicyExecutionResult;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ConfigurableApplicationContext;

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
@Accessors(chain = true)
public class AtLeastOneCredentialValidatedAuthenticationPolicy extends BaseAuthenticationPolicy {

    @Serial
    private static final long serialVersionUID = -7484490540437793931L;

    /**
     * Flag to try all credentials before policy is satisfied. Defaults to {@code false}.
     */
    private final boolean tryAll;

    @Override
    public AuthenticationPolicyExecutionResult isSatisfiedBy(@Nullable final Authentication authn,
                                                             final Set<AuthenticationHandler> authenticationHandlers,
                                                             final ConfigurableApplicationContext applicationContext,
                                                             final Map<String, ? extends Serializable> context) throws Exception {
        if (authn == null) {
            LOGGER.warn("Authentication attempt is null and cannot satisfy policy");
            return AuthenticationPolicyExecutionResult.failure();
        }

        if (this.tryAll) {
            val match = authenticationHandlers.stream()
                .allMatch(handler -> authn.getSuccesses().containsKey(handler.getName()));
            if (!match) {
                LOGGER.warn("Authentication handlers qualified to handle this transaction, [{}], "
                            + "have not all completed a successful authentication event. Successful "
                            + "authentication events recorded currently are [{}]",
                    authenticationHandlers, authn.getSuccesses().keySet());
                return AuthenticationPolicyExecutionResult.failure();
            }
            LOGGER.debug("Authentication policy is satisfied with all authentication transactions");
            return AuthenticationPolicyExecutionResult.success(!authn.getSuccesses().isEmpty());
        }
        if (!authn.getSuccesses().isEmpty()) {
            LOGGER.debug("Authentication policy is satisfied having found at least one authentication transactions");
            return AuthenticationPolicyExecutionResult.success();
        }
        LOGGER.warn("Authentication policy failed to find a successful authentication transaction. Successful attempts are [{}]",
            authn.getSuccesses().keySet());
        return AuthenticationPolicyExecutionResult.failure();
    }
}
