package org.apereo.cas.authentication.policy;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationPolicy;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * Authentication policy that is satisfied by at least one successfully authenticated credential.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Slf4j
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnyAuthenticationPolicy implements AuthenticationPolicy {

    /**
     * Flag to try all credentials before policy is satisfied. Defaults to {@code false}.
     */
    private boolean tryAll;

    @Override
    public boolean isSatisfiedBy(final Authentication authn) throws Exception {
        if (this.tryAll) {
            final int sum = authn.getSuccesses().size() + authn.getFailures().size();
            if (authn.getCredentials().size() != sum) {
                LOGGER.warn("Number of provided credentials [{}] does not match the sum of authentication successes and failures [{}]", authn.getCredentials().size(), sum);
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
