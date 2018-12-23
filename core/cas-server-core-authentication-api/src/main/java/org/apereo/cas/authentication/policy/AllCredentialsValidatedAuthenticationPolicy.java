package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationPolicy;

import lombok.extern.slf4j.Slf4j;

import java.util.Set;

/**
 * Authentication security policy that is satisfied iff all given credentials are successfully authenticated.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Slf4j
public class AllCredentialsValidatedAuthenticationPolicy implements AuthenticationPolicy {


    @Override
    public boolean isSatisfiedBy(final Authentication authn, final Set<AuthenticationHandler> authenticationHandlers) {
        LOGGER.debug("Successful authentications: [{}], credentials: [{}]", authn.getSuccesses().keySet(), authn.getCredentials());
        if (authn.getSuccesses().size() != authn.getCredentials().size()) {
            LOGGER.warn("Number of successful authentications, [{}], does not match the number of provided credentials, [{}].",
                authn.getSuccesses().size(), authn.getCredentials().size());
            return false;
        }
        LOGGER.debug("Authentication policy is satisfied.");
        return true;
    }
}
