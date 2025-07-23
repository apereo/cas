package org.apereo.cas.oidc.token.ciba;

import org.apereo.cas.oidc.ticket.OidcCibaRequest;
import org.apereo.cas.services.OidcBackchannelTokenDeliveryModes;
import org.apereo.cas.services.OidcRegisteredService;
import org.apache.commons.lang3.Strings;
import java.util.Map;

/**
 * This is {@link CibaTokenDeliveryHandler}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public interface CibaTokenDeliveryHandler {

    /**
     * Gets supported delivery mode.
     *
     * @return the delivery mode
     */
    OidcBackchannelTokenDeliveryModes getDeliveryMode();
    
    /**
     * Deliver the tokens.
     *
     * @param registeredService the registered service
     * @param cibaRequest       the ciba request
     * @return delivery payload
     */
    Map<String, ?> deliver(OidcRegisteredService registeredService, OidcCibaRequest cibaRequest) throws Throwable;

    /**
     * Supports client application.
     *
     * @param registeredService the registered service
     * @return true/false
     */
    default boolean supports(final OidcRegisteredService registeredService) {
        return Strings.CI.equals(registeredService.getBackchannelTokenDeliveryMode(), getDeliveryMode().getMode());
    }
}
