package org.apereo.cas.authentication.policy;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationPolicy;

/**
 * Authentication security policy that is satisfied iff all given credentials are successfully authenticated.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Slf4j
public class AllAuthenticationPolicy implements AuthenticationPolicy {


    @Override
    public boolean isSatisfiedBy(final Authentication authn) {
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
