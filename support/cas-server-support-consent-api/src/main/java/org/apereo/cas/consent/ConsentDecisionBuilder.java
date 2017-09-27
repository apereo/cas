package org.apereo.cas.consent;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;

import java.util.Map;

/**
 * This is {@link ConsentDecisionBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public interface ConsentDecisionBuilder {

    /**
     * Update consent decision.
     *
     * @param consent    the consent
     * @param attributes the attributes
     * @return the consent decision
     */
    ConsentDecision update(ConsentDecision consent, Map<String, Object> attributes);

    /**
     * Build consent decision consent decision.
     *
     * @param service           the service
     * @param registeredService the registered service
     * @param principalId       the principal id
     * @param attributes        the attributes
     * @return the consent decision
     */
    ConsentDecision build(Service service,
                          RegisteredService registeredService,
                          String principalId,
                          Map<String, Object> attributes);

    /**
     * Is consent decision valid for attributes?
     *
     * @param decision   the decision
     * @param attributes the attributes
     * @return true /false
     */
    boolean doesAttributeReleaseRequireConsent(ConsentDecision decision,
                                               Map<String, Object> attributes);

    /**
     * Gets consentable attributes from the consent decision.
     * The operation is allowed to change attributes and/or unpack
     * attributes from encoded strings, etc.
     * @param decision the decision
     * @return the consentable attributes
     */
    Map<String, Object> getConsentableAttributesFrom(ConsentDecision decision);
}
