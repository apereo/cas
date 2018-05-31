package org.apereo.cas.consent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Endpoint(id = "attribute-consent", enableByDefault = false)
@RequiredArgsConstructor
public class AttributeConsentReportEndpoint {
    private final ConsentRepository consentRepository;
    private final ConsentEngine consentEngine;

    /**
     * Consent decisions collection.
     *
     * @param principal the principal
     * @return the collection
     */
    @ReadOperation
    public Collection<Map<String, Object>> consentDecisions(@Selector final String principal) {
        final Collection<Map<String, Object>> result = new HashSet<>();
        LOGGER.debug("Fetching consent decisions for principal [{}]", principal);
        final var consentDecisions = this.consentRepository.findConsentDecisions(principal);
        LOGGER.debug("Resolved consent decisions for principal [{}]: {}", principal, consentDecisions);

        consentDecisions.stream().forEach(d -> {
            final Map<String, Object> map = new HashMap<>();
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
     * @return the boolean
     */
    @DeleteOperation
    public boolean revokeConsents(@Selector final String principal, @Selector final long decisionId) {
        LOGGER.debug("Deleting consent decisions for principal [{}].", principal);
        return this.consentRepository.deleteConsentDecision(decisionId, principal);
    }

}
