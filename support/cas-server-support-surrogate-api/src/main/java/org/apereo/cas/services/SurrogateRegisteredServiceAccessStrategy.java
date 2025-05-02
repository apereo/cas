package org.apereo.cas.services;

import org.apereo.cas.services.util.RegisteredServiceAccessStrategyEvaluator;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
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

    @Serial
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
    
    private Map<String, Set<String>> surrogateRequiredAttributes = new HashMap<>();

    @Override
    public boolean authorizeRequest(final RegisteredServiceAccessStrategyRequest request) {
        return !isSurrogateAuthenticationSession(request) || doPrincipalAttributesAllowSurrogateServiceAccess(request);
    }

    protected boolean doPrincipalAttributesAllowSurrogateServiceAccess(final RegisteredServiceAccessStrategyRequest request) {
        return RegisteredServiceAccessStrategyEvaluator.builder()
            .caseInsensitive(this.caseInsensitive)
            .requireAllAttributes(this.requireAllAttributes)
            .requiredAttributes(this.surrogateRequiredAttributes)
            .rejectedAttributes(new LinkedHashMap<>())
            .build()
            .apply(request);
    }
}
