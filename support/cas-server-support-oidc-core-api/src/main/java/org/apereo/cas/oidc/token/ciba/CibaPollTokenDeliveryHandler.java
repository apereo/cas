package org.apereo.cas.oidc.token.ciba;

import module java.base;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.ticket.OidcCibaRequest;
import org.apereo.cas.services.OidcBackchannelTokenDeliveryModes;
import org.apereo.cas.services.OidcRegisteredService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This is {@link CibaPollTokenDeliveryHandler}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
@Getter
@Slf4j
public class CibaPollTokenDeliveryHandler implements CibaTokenDeliveryHandler {
    private final OidcBackchannelTokenDeliveryModes deliveryMode = OidcBackchannelTokenDeliveryModes.POLL;

    private final OidcConfigurationContext configurationContext;

    @Override
    public Map<String, ?> deliver(final OidcRegisteredService registeredService, final OidcCibaRequest cibaRequest) throws Throwable {
        LOGGER.debug("Marking CIBA authentication request [{}] as ready", cibaRequest.getEncodedId());
        configurationContext.getTicketRegistry().updateTicket(cibaRequest.markTicketReady());
        return Map.of(OidcConstants.AUTH_REQ_ID, cibaRequest.getEncodedId());
    }
}
