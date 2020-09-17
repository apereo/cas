package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.Authentication;

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
 * authentication handler is not part of successful authn handlers.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
@Setter
@Getter
public class ExcludedAuthenticationHandlerAuthenticationPolicy extends BaseAuthenticationHandlerAuthenticationPolicy {

    private static final long serialVersionUID = -3871692225877293627L;

    public ExcludedAuthenticationHandlerAuthenticationPolicy(final Set<String> handlerNames, final boolean tryAll) {
        super(handlerNames, tryAll);
    }

    @Override
    boolean isSatisfiedByInternal(final Authentication authn) {
        if (!getHandlerNames().isEmpty()) {
            val credsOk = authn.getSuccesses()
                .keySet()
                .stream()
                .anyMatch(s -> getHandlerNames().contains(s));

            if (credsOk) {
                LOGGER.warn("Excluded authentication handler(s) [{}] found in authentication attempt", getHandlerNames());
                return false;
            }
        }
        LOGGER.trace("Authentication policy is satisfied");
        return true;
    }
}
