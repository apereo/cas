package org.apereo.cas.authentication.policy;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authentication security policy that is satisfied iff a specified authentication handler successfully authenticates
 * at least one credential.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class RequiredHandlerAuthenticationPolicy implements AuthenticationPolicy {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequiredHandlerAuthenticationPolicy.class);

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

    /**
     * Instantiates a new Required handler authentication policy.
     *
     * @param requiredHandlerName the required handler name
     * @param tryAll              Sets the flag to try all credentials before the policy is satisfied.
     *                            This flag is disabled by default such that the policy is satisfied immediately upon the first
     *                            credential that is successfully authenticated by the required handler.
     */
    public RequiredHandlerAuthenticationPolicy(final String requiredHandlerName, final boolean tryAll) {
        this.requiredHandlerName = requiredHandlerName;
        this.tryAll = tryAll;
    }

    @Override
    public boolean isSatisfiedBy(final Authentication authn) {
        boolean credsOk = true;
        if (this.tryAll) {
            credsOk = authn.getCredentials().size() == authn.getSuccesses().size() + authn.getFailures().size();
        }

        if (!credsOk) {
            LOGGER.warn("Number of provided credentials does not match the sum of authentication successes and failures");
            return false;
        }

        LOGGER.debug("Examining authentication successes for authentication handler [{}]", this.requiredHandlerName);
        if (StringUtils.isNotBlank(this.requiredHandlerName)) {
            credsOk = authn.getSuccesses().keySet()
                    .stream()
                    .filter(s -> s.equalsIgnoreCase(this.requiredHandlerName))
                    .findAny()
                    .isPresent();

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
