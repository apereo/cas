package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationPolicy;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Set;

/**
 * Authentication policy that is satisfied by at least one successfully authenticated credential.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Slf4j
@Setter
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class AtLeastOneCredentialValidatedAuthenticationPolicy implements AuthenticationPolicy {

    /**
     * Flag to try all credentials before policy is satisfied. Defaults to {@code false}.
     */
    private final boolean tryAll;

    @Override
    public boolean isSatisfiedBy(final Authentication authn, final Set<AuthenticationHandler> authenticationHandlers) throws Exception {
        if (this.tryAll) {
            val sum = authn.getSuccesses().size() + authn.getFailures().size();
            if (authenticationHandlers.size() != sum) {
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
