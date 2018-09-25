package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationPolicy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * Authentication security policy that is satisfied iff a specified authentication handler successfully authenticates
 * at least one credential.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class RequiredHandlerAuthenticationPolicy implements AuthenticationPolicy {

    /**
     * Authentication handler name that is required to satisfy policy.
     */
    private final String requiredHandlerName;

    /**
     * Flag to try all credentials before policy is satisfied.
     */
    private final boolean tryAll;

    /**
     * Instantiates a new required handler authentication policy.
     *
     * @param requiredHandlerName the required handler name
     */
    public RequiredHandlerAuthenticationPolicy(final String requiredHandlerName) {
        this(requiredHandlerName, false);
    }

    @Override
    public boolean isSatisfiedBy(final Authentication authn) {
        var credsOk = true;
        val sum = authn.getSuccesses().size() + authn.getFailures().size();
        if (this.tryAll) {
            credsOk = authn.getCredentials().size() == sum;
        }

        if (!credsOk) {
            LOGGER.warn("Number of provided credentials [{}] does not match the sum of authentication successes and failures [{}]. "
                + "Successful authentication handlers are [{}]", authn.getCredentials().size(), sum, authn.getSuccesses().keySet());
            return false;
        }

        LOGGER.debug("Examining authentication successes for authentication handler [{}]", this.requiredHandlerName);
        if (StringUtils.isNotBlank(this.requiredHandlerName)) {
            credsOk = authn.getSuccesses().keySet()
                .stream()
                .anyMatch(s -> s.equalsIgnoreCase(this.requiredHandlerName));

            if (!credsOk) {
                LOGGER.warn("Required authentication handler [{}] is not present in the list of recorded successful authentications",
                    this.requiredHandlerName);
                return false;
            }
        }

        LOGGER.debug("Authentication policy is satisfied");
        return true;
    }
}
