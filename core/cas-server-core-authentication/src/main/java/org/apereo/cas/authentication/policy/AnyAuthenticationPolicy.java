package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authentication policy that is satisfied by at least one successfully authenticated credential.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class AnyAuthenticationPolicy implements AuthenticationPolicy {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnyAuthenticationPolicy.class);

    /**
     * Flag to try all credentials before policy is satisfied. Defaults to {@code false}.
     */
    private boolean tryAll;

    /**
     * Instantiates a new Any authentication policy.
     */
    public AnyAuthenticationPolicy() {
    }

    /**
     * Instantiates a new Any authentication policy.
     *
     * @param tryAll the try all
     */
    public AnyAuthenticationPolicy(final boolean tryAll) {
        this.tryAll = tryAll;
    }

    /**
     * Sets the flag to try all credentials before the policy is satisfied.
     * This flag is disabled by default such that the policy is satisfied immediately upon the first
     * successfully authenticated credential. Defaults to {@code false}.
     *
     * @param tryAll True to force all credentials to be authenticated, false otherwise.
     */
    public void setTryAll(final boolean tryAll) {
        this.tryAll = tryAll;
    }

    @Override
    public boolean isSatisfiedBy(final Authentication authn) throws Exception {
        if (this.tryAll) {
            if (authn.getCredentials().size() != authn.getSuccesses().size() + authn.getFailures().size()) {
                LOGGER.warn("Number of provided credentials does not match the sum of authentication successes and failures");
                return false;
            }
            LOGGER.debug("Authentication policy is satisfied with all authentication transactions");
            return true;
        }
        if (!authn.getSuccesses().isEmpty()) {
            LOGGER.debug("Authentication policy is satisfied having found at least one authentication transactions");
            return true;
        }
        LOGGER.warn("Authentication policy has failed to find a successful authentication transaction");
        return false;
    }
}
