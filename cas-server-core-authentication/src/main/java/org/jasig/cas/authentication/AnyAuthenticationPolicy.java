package org.jasig.cas.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Authentication policy that is satisfied by at least one successfully authenticated credential.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Component("anyAuthenticationPolicy")
public class AnyAuthenticationPolicy implements AuthenticationPolicy {

    /** Flag to try all credentials before policy is satisfied. Defaults to {@code false}.*/
    private boolean tryAll;

    /**
     * Sets the flag to try all credentials before the policy is satisfied.
     * This flag is disabled by default such that the policy is satisfied immediately upon the first
     * successfully authenticated credential. Defaults to {@code false}.
     *
     * @param tryAll True to force all credentials to be authenticated, false otherwise.
     */
    @Autowired
    public void setTryAll(@Value("${cas.authn.policy.any.tryall:false}") final boolean tryAll) {
        this.tryAll = tryAll;
    }

    @Override
    public boolean isSatisfiedBy(final Authentication authn) {
        if (this.tryAll) {
            return authn.getCredentials().size() == authn.getSuccesses().size() + authn.getFailures().size();
        }
        return !authn.getSuccesses().isEmpty();
    }
}
