package org.apereo.cas.consent;

import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;

import java.io.Serializable;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * This is {@link ConsentEngine}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface ConsentEngine extends Serializable {

    /**
     * Store consent decision.
     *
     * @param service           the service
     * @param registeredService the registered service
     * @param authentication    the authentication
     * @param reminder          the reminder
     * @param reminderTimeUnit  the reminder time unit
     * @param options           the options
     * @return the stored decision
     */
    ConsentDecision storeConsentDecision(Service service,
                                 RegisteredService registeredService,
                                 Authentication authentication,
                                 long reminder,
                                 ChronoUnit reminderTimeUnit,
                                 ConsentOptions options);

    /**
     * Find consent decision consent decision.
     *
     * @param service           the service
     * @param registeredService the registered service
     * @param authentication    the authentication
     * @return the consent decision
     */
    ConsentDecision findConsentDecision(Service service,
                                         RegisteredService registeredService,
                                         Authentication authentication);

    /**
     * Gets consentable attributes.
     *
     * @param authentication    the authentication
     * @param service           the service
     * @param registeredService the registered service
     * @return the consentable attributes
     */
    Map<String, Object> resolveConsentableAttributesFrom(Authentication authentication,
                                                         Service service,
                                                         RegisteredService registeredService);

    /**
     * Gets consentable attributes from an existing consent decision.
     * Typically decisions are signed and encoded, so this op will need to ensure
     * the correct attribute names and values in the existing decision record are produced.
     * @param decision the decision
     * @return the consentable attributes
     */
    Map<String, Object> resolveConsentableAttributesFrom(ConsentDecision decision);

    /**
     * Is consent required?
     *
     * @param service           the service
     * @param registeredService the registered service
     * @param authentication    the authentication
     * @return true /false
     */
    Pair<Boolean, ConsentDecision> isConsentRequiredFor(Service service, RegisteredService registeredService, Authentication authentication);
}
