package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationPolicy;

import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Authentication security policy that is satisfied iff all given authentication handlers are successfully authenticated.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Slf4j
public class AllAuthenticationHandlersSucceededAuthenticationPolicy implements AuthenticationPolicy {

    @Override
    public boolean isSatisfiedBy(final Authentication authn, final Set<AuthenticationHandler> authenticationHandlers) {
        LOGGER.debug("Successful authentications: [{}], current authentication handlers [{}]", authn.getSuccesses().keySet(),
            authenticationHandlers.stream().map(AuthenticationHandler::getName).collect(Collectors.joining(",")));

        if (authn.getSuccesses().size() != authenticationHandlers.size()) {
            LOGGER.warn("Number of successful authentications, [{}], does not match the number of authentication handlers, [{}].",
                authn.getSuccesses().size(), authenticationHandlers.size());
            return false;
        }

        return true;
    }
}
