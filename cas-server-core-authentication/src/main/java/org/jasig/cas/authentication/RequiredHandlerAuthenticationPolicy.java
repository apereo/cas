package org.jasig.cas.authentication;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/**
 * Authentication security policy that is satisfied iff a specified authentication handler successfully authenticates
 * at least one credential.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Component("requiredHandlerAuthenticationPolicy")
public class RequiredHandlerAuthenticationPolicy implements AuthenticationPolicy {

    /** Authentication handler name that is required to satisfy policy. */
    @NotNull
    private final String requiredHandlerName;

    /** Flag to try all credentials before policy is satisfied. */
    private boolean tryAll;

    /**
     * Instantiates a new required handler authentication policy.
     *
     * @param requiredHandlerName the required handler name
     */
    @Autowired
    public RequiredHandlerAuthenticationPolicy(@Value("${cas.authn.policy.req.handlername:handlerName}")
                                                   final String requiredHandlerName) {
        this.requiredHandlerName = requiredHandlerName;
    }

    /**
     * Sets the flag to try all credentials before the policy is satisfied.
     * This flag is disabled by default such that the policy is satisfied immediately upon the first
     * credential that is successfully authenticated by the required handler.
     *
     * @param tryAll True to force all credentials to be authenticated, false otherwise.
     */
    @Autowired
    public void setTryAll(@Value("${cas.authn.policy.req.tryall:false}")
                              final boolean tryAll) {
        this.tryAll = tryAll;
    }

    @Override
    public boolean isSatisfiedBy(final Authentication authn) {
        boolean credsOk = true;
        if (this.tryAll) {
            credsOk = authn.getCredentials().size() == authn.getSuccesses().size()
                + authn.getFailures().size();
        }
        return credsOk && StringUtils.isNotBlank(this.requiredHandlerName)
                    && authn.getSuccesses().containsKey(this.requiredHandlerName);
    }
}
