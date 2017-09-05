package org.apereo.cas.services;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.AuthenticationCredentialsLocalBinder;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link SurrogateRegisteredServiceAccessStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SurrogateRegisteredServiceAccessStrategy extends DefaultRegisteredServiceAccessStrategy {
    private static final long serialVersionUID = -1688944419711632962L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SurrogateRegisteredServiceAccessStrategy.class);

    private boolean surrogateEnabled;
    private boolean surrogateSsoEnabled;
    private Map<String, Set<String>> surrogateRequiredAttributes = new HashMap<>();

    public boolean isSurrogateEnabled() {
        return surrogateEnabled;
    }

    public void setSurrogateEnabled(final boolean surrogateEnabled) {
        this.surrogateEnabled = surrogateEnabled;
    }

    public boolean isSurrogateSsoEnabled() {
        return surrogateSsoEnabled;
    }

    public void setSurrogateSsoEnabled(final boolean surrogateSsoEnabled) {
        this.surrogateSsoEnabled = surrogateSsoEnabled;
    }

    public Map<String, Set<String>> getSurrogateRequiredAttributes() {
        return surrogateRequiredAttributes;
    }

    public void setSurrogateRequiredAttributes(final Map<String, Set<String>> surrogateRequiredAttributes) {
        this.surrogateRequiredAttributes = surrogateRequiredAttributes;
    }

    @Override
    public boolean isServiceAccessAllowed() {
        return isSurrogateAuthenticationSession() ? this.surrogateEnabled : super.isEnabled();
    }

    @Override
    public boolean isServiceAccessAllowedForSso() {
        return isSurrogateAuthenticationSession() ? this.surrogateSsoEnabled : super.isSsoEnabled();
    }

    @Override
    public boolean doPrincipalAttributesAllowServiceAccess(final String principal, final Map<String, Object> principalAttributes) {
        if (isSurrogateAuthenticationSession()) {
            return doPrincipalAttributesAllowSurrogateServiceAccess(principalAttributes);
        }
        return super.doPrincipalAttributesAllowServiceAccess(principal, principalAttributes);
    }

    private boolean doPrincipalAttributesAllowSurrogateServiceAccess(final Map<String, Object> principalAttributes) {
        if (!enoughRequiredAttributesAvailableToProcess(principalAttributes, this.surrogateRequiredAttributes)) {
            LOGGER.debug("Surrogate access is denied. There are not enough attributes available to satisfy requirements");
            return false;
        }

        if (!doRequiredAttributesAllowPrincipalAccess(principalAttributes, this.surrogateRequiredAttributes)) {
            LOGGER.debug("Surrogate access is denied. The principal does not have the required attributes specified by this strategy");
            return false;
        }
        return true;
    }

    /**
     * Is surrogate authentication session ?.
     *
     * @return true/false
     */
    protected boolean isSurrogateAuthenticationSession() {
        final AuthenticationBuilder authBuilder = AuthenticationCredentialsLocalBinder.getCurrentAuthenticationBuilder();
        if (authBuilder != null) {
            return authBuilder.hasAttribute(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED);
        }
        final Authentication auth = AuthenticationCredentialsLocalBinder.getCurrentAuthentication();
        if (auth != null) {
            return auth.getAttributes().containsKey(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED);
        }
        return false;
    }
}
