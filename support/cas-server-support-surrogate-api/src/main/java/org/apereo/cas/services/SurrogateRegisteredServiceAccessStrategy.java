package org.apereo.cas.services;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
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
    
    private Map<String, Set<String>> surrogateRequiredAttributes = new HashMap<>();

    public boolean isSurrogateEnabled() {
        return surrogateEnabled;
    }

    public void setSurrogateEnabled(final boolean surrogateEnabled) {
        this.surrogateEnabled = surrogateEnabled;
    }

    public Map<String, Set<String>> getSurrogateRequiredAttributes() {
        return surrogateRequiredAttributes;
    }

    public void setSurrogateRequiredAttributes(final Map<String, Set<String>> surrogateRequiredAttributes) {
        this.surrogateRequiredAttributes = surrogateRequiredAttributes;
    }

    @Override
    public boolean doPrincipalAttributesAllowServiceAccess(final String principal, final Map<String, Object> attributes) {
        if (isSurrogateAuthenticationSession(attributes)) {
            if (!isSurrogateEnabled()) {
                return false;
            }
            return doPrincipalAttributesAllowSurrogateServiceAccess(attributes);
        }
        return super.doPrincipalAttributesAllowServiceAccess(principal, attributes);
    }

    /**
     * Do principal attributes allow surrogate service access?.
     *
     * @param principalAttributes the principal attributes
     * @return the boolean
     */
    protected boolean doPrincipalAttributesAllowSurrogateServiceAccess(final Map<String, Object> principalAttributes) {
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
     * Is surrogate authentication session?.
     *
     * @param attributes the attributes
     * @return true /false
     */
    protected boolean isSurrogateAuthenticationSession(final Map<String, Object> attributes) {
        return attributes.containsKey(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED);
    }


    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final SurrogateRegisteredServiceAccessStrategy rhs = (SurrogateRegisteredServiceAccessStrategy) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(this.surrogateEnabled, rhs.surrogateEnabled)
                .append(this.surrogateRequiredAttributes, rhs.surrogateRequiredAttributes)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(surrogateEnabled)
                .append(surrogateRequiredAttributes)
                .toHashCode();
    }
}
