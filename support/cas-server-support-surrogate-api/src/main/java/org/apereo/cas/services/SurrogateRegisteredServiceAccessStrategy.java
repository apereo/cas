package org.apereo.cas.services;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link SurrogateRegisteredServiceAccessStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SurrogateRegisteredServiceAccessStrategy extends BaseSurrogateRegisteredServiceAccessStrategy {

    private static final long serialVersionUID = -1688944419711632962L;

    private boolean surrogateEnabled;

    private Map<String, Set<String>> surrogateRequiredAttributes = new HashMap<>(0);

    @Override
    public boolean doPrincipalAttributesAllowServiceAccess(final String principal, final Map<String, Object> attributes) {
        if (isSurrogateAuthenticationSession(attributes)) {
            return isSurrogateEnabled() && doPrincipalAttributesAllowSurrogateServiceAccess(attributes);
        }
        return super.doPrincipalAttributesAllowServiceAccess(principal, attributes);
    }

    /**
     * Do principal attributes allow surrogate service access?.
     *
     * @param principalAttributes the principal attributes
     * @return true/false
     */
    protected boolean doPrincipalAttributesAllowSurrogateServiceAccess(final Map<String, Object> principalAttributes) {
        if (!enoughRequiredAttributesAvailableToProcess(principalAttributes, this.surrogateRequiredAttributes)) {
            LOGGER.debug("Surrogate access is denied. There are not enough attributes available to satisfy the requirements [{}]",
                this.surrogateRequiredAttributes);
            return false;
        }
        if (!doRequiredAttributesAllowPrincipalAccess(principalAttributes, this.surrogateRequiredAttributes)) {
            LOGGER.debug("Surrogate access is denied. The principal does not have the required attributes [{}] specified by this strategy",
                this.surrogateRequiredAttributes);
            return false;
        }
        return true;
    }
}
