package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationPolicyExecutionResult;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Set;

/**
 * Authentication security policy that is satisfied iff a specified
 * authentication handler successfully authenticates
 * at least one credential.
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
public class RequiredAuthenticationHandlerAuthenticationPolicy extends BaseAuthenticationHandlerAuthenticationPolicy {

    private static final long serialVersionUID = -3871692225877293627L;

    public RequiredAuthenticationHandlerAuthenticationPolicy(final String requiredHandlerNames) {
        super(requiredHandlerNames);
    }

    public RequiredAuthenticationHandlerAuthenticationPolicy(final Set<String> handlerNames, final boolean tryAll) {
        super(handlerNames, tryAll);
    }

    @Override
    public AuthenticationPolicyExecutionResult isSatisfiedByInternal(final Authentication authn) {
        LOGGER.debug("Examining authentication successes for authentication handler [{}]", getHandlerNames());
        if (!getHandlerNames().isEmpty()) {
            val credsOk = authn.getSuccesses()
                .keySet()
                .stream()
                .anyMatch(s -> getHandlerNames().contains(s));

            if (!credsOk) {
                LOGGER.warn("Required authentication handler(s) [{}] is present in the list of successful authentications [{}]",
                    getHandlerNames(), authn.getSuccesses().keySet());
                return AuthenticationPolicyExecutionResult.failure();
            }
        }
        LOGGER.trace("Authentication policy is satisfied");
        return AuthenticationPolicyExecutionResult.success();
    }
}
