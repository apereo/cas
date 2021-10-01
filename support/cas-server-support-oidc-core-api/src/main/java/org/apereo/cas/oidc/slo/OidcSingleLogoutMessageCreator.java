package org.apereo.cas.oidc.slo;

import org.apereo.cas.logout.slo.SingleLogoutMessage;
import org.apereo.cas.logout.slo.SingleLogoutMessageCreator;
import org.apereo.cas.logout.slo.SingleLogoutRequestContext;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredServiceLogoutType;
import org.apereo.cas.util.DigestUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwt.JwtClaims;
import org.springframework.beans.factory.ObjectProvider;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

/**
 * The message creator for the OIDC protocol.
 *
 * @author Jerome LELEU
 * @since 6.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class OidcSingleLogoutMessageCreator implements SingleLogoutMessageCreator {

    private final ObjectProvider<OidcConfigurationContext> configurationProvider;

    @Override
    public SingleLogoutMessage create(final SingleLogoutRequestContext request) {
        val configurationContext = configurationProvider.getObject();

        val builder = SingleLogoutMessage.builder();
        if (request.getLogoutType() == RegisteredServiceLogoutType.BACK_CHANNEL) {
            LOGGER.trace("Building logout token for [{}]", request.getRegisteredService());

            val claims = buildJwtClaims(request);
            val logoutToken = configurationContext.getIdTokenSigningAndEncryptionService()
                .encode((OidcRegisteredService) request.getRegisteredService(), claims);
            return builder.payload(logoutToken).build();
        }
        return builder.payload(StringUtils.EMPTY).build();
    }

    /**
     * Build jwt claims.
     *
     * @param request the request
     * @return the jwt claims
     */
    protected JwtClaims buildJwtClaims(final SingleLogoutRequestContext request) {
        val configurationContext = configurationProvider.getObject();
        val claims = new JwtClaims();
        claims.setIssuer(configurationContext.getIssuerService().determineIssuer(Optional.empty()));
        claims.setSubject(request.getExecutionRequest().getTicketGrantingTicket().getAuthentication().getPrincipal().getId());
        claims.setAudience(((OidcRegisteredService) request.getRegisteredService()).getClientId());
        claims.setIssuedAtToNow();
        claims.setJwtId(UUID.randomUUID().toString());
        val events = new HashMap<String, Object>();
        events.put("http://schemas.openid.net/event/backchannel-logout", new HashMap<>());
        claims.setClaim("events", events);
        claims.setClaim(OidcConstants.CLAIM_SESSION_ID,
            DigestUtils.sha(request.getExecutionRequest().getTicketGrantingTicket().getId()));

        return claims;
    }
}
