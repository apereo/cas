package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.consent.ConsentEngine;
import org.apereo.cas.web.BaseCasMvcEndpoint;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * This is {@link ConsentReviewEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@Endpoint(id = "consentReview", enableByDefault = false)
public class ConsentReviewEndpoint extends BaseCasMvcEndpoint {
    private final ConsentEngine consentEngine;

    public ConsentReviewEndpoint(final CasConfigurationProperties casProperties,
                                 final ConsentEngine consentEngine) {
        super(casProperties);
        this.consentEngine = consentEngine;
    }

    /**
     * Gets consent decisions.
     *
     * @param principal the principal
     * @return the consent decisions
     */
    @ReadOperation
    public Collection<Map<String, Object>> getConsentDecisions(@Selector final String principal) {
        LOGGER.debug("Fetching consent decisions for principal [{}]", principal);
        val consentDecisions = consentEngine.getConsentRepository().findConsentDecisions(principal);
        LOGGER.debug("Resolved consent decisions for principal [{}]: {}", principal, consentDecisions);
        val result = new HashSet<Map<String, Object>>();
        consentDecisions.forEach(d -> {
            val map = new HashMap<String, Object>();
            map.put("decision", d);
            map.put("principal", principal);
            map.put("attributes", this.consentEngine.resolveConsentableAttributesFrom(d));
            result.add(map);
        });
        return result;
    }

    /**
     * Delete consent decision.
     *
     * @param principal  the principal
     * @param decisionId the decision id
     * @return true/false
     */
    @DeleteOperation
    public boolean deleteConsentDecision(@Selector final String principal, @Selector final Long decisionId) {
        LOGGER.debug("Deleting consent decision with id [{}] for principal [{}].", decisionId, principal);
        return consentEngine.getConsentRepository().deleteConsentDecision(decisionId, principal);
    }
}
