package org.apereo.cas.consent;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

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
 * This is {@link AttributeConsentReportEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@Endpoint(id = "attributeConsent", enableByDefault = false)
public class AttributeConsentReportEndpoint extends BaseCasActuatorEndpoint {
    private final ConsentRepository consentRepository;
    private final ConsentEngine consentEngine;

    public AttributeConsentReportEndpoint(final CasConfigurationProperties casProperties,
                                          final ConsentRepository consentRepository,
                                          final ConsentEngine consentEngine) {
        super(casProperties);
        this.consentRepository = consentRepository;
        this.consentEngine = consentEngine;
    }

    /**
     * Consent decisions collection.
     *
     * @param principal the principal
     * @return the collection
     */
    @ReadOperation
    public Collection<Map<String, Object>> consentDecisions(@Selector final String principal) {
        val result = new HashSet<Map<String, Object>>();
        LOGGER.debug("Fetching consent decisions for principal [{}]", principal);
        val consentDecisions = this.consentRepository.findConsentDecisions(principal);
        LOGGER.debug("Resolved consent decisions for principal [{}]: [{}]", principal, consentDecisions);

        consentDecisions.forEach(d -> {
            val map = new HashMap<String, Object>();
            map.put("decision", d);
            map.put("attributes", this.consentEngine.resolveConsentableAttributesFrom(d));
            result.add(map);
        });
        return result;
    }


    /**
     * Revoke consents.
     *
     * @param principal  the principal
     * @param decisionId the decision id
     * @return true/false
     */
    @DeleteOperation
    public boolean revokeConsents(@Selector final String principal, @Selector final long decisionId) {
        LOGGER.debug("Deleting consent decisions for principal [{}].", principal);
        return this.consentRepository.deleteConsentDecision(decisionId, principal);
    }

}
