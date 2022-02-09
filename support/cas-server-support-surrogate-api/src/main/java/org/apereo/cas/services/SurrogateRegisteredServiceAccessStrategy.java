package org.apereo.cas.services;

import org.apereo.cas.services.util.RegisteredServiceAccessStrategyEvaluator;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link SurrogateRegisteredServiceAccessStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SurrogateRegisteredServiceAccessStrategy extends BaseSurrogateRegisteredServiceAccessStrategy {

    private static final long serialVersionUID = -1688944419711632962L;

    /**
     * Defines the attribute aggregation behavior when checking for required attributes.
     * Default requires that all attributes be present and match the principal's.
     */
    protected boolean requireAllAttributes = true;

    /**
     * Indicates whether matching on required attribute values
     * should be done in a case-insensitive manner.
     */
    protected boolean caseInsensitive;

    private boolean surrogateEnabled;

    private Map<String, Set<String>> surrogateRequiredAttributes = new HashMap<>(0);

    @Override
    public boolean doPrincipalAttributesAllowServiceAccess(final String principal, final Map<String, Object> attributes) {
        if (isSurrogateAuthenticationSession(attributes)) {
            return isSurrogateEnabled() && doPrincipalAttributesAllowSurrogateServiceAccess(principal, attributes);
        }
        return true;
    }

    /**
     * Do principal attributes allow surrogate service access?.
     *
     * @param principal           the principal
     * @param principalAttributes the principal attributes
     * @return true /false
     */
    protected boolean doPrincipalAttributesAllowSurrogateServiceAccess(final String principal,
                                                                       final Map<String, Object> principalAttributes) {
        return RegisteredServiceAccessStrategyEvaluator.builder()
            .caseInsensitive(this.caseInsensitive)
            .requireAllAttributes(this.requireAllAttributes)
            .requiredAttributes(this.surrogateRequiredAttributes)
            .rejectedAttributes(new LinkedHashMap<>(0))
            .build()
            .evaluate(principal, principalAttributes);
    }
}
