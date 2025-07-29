package org.apereo.cas.oidc.slo;

import org.apereo.cas.logout.slo.SingleLogoutMessage;
import org.apereo.cas.logout.slo.SingleLogoutMessageCreator;
import org.apereo.cas.logout.slo.SingleLogoutRequestContext;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.util.DigestUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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

    protected final ObjectProvider<OidcConfigurationContext> configurationProvider;

    @Override
    public SingleLogoutMessage create(final SingleLogoutRequestContext request) throws Throwable {
        val configurationContext = configurationProvider.getObject();

        val builder = SingleLogoutMessage.builder();
        LOGGER.trace("Building logout token for [{}]", request.getRegisteredService());
        val claims = buildJwtClaims(request);
        val logoutToken = configurationContext.getIdTokenSigningAndEncryptionService()
            .encode((OAuthRegisteredService) request.getRegisteredService(), claims);
        return builder.payload(logoutToken).build();
    }

    protected JwtClaims buildJwtClaims(final SingleLogoutRequestContext request) {
        val executionRequest = request.getExecutionRequest();
        val claims = new JwtClaims();
        claims.setSubject(executionRequest.getTicketGrantingTicket().getAuthentication().getPrincipal().getId());
        val sid = DigestUtils.sha(DigestUtils.sha512(executionRequest.getTicketGrantingTicket().getId()));
        claims.setClaim(OidcConstants.CLAIM_SESSION_ID, sid);
        claims.setIssuer(determineIssuer(request));
        claims.setAudience(((OAuthRegisteredService) request.getRegisteredService()).getClientId());
        claims.setIssuedAtToNow();
        claims.setJwtId(UUID.randomUUID().toString());
        claims.setExpirationTimeMinutesInTheFuture(1);
        val events = new HashMap<String, Object>();
        events.put("http://schemas.openid.net/event/backchannel-logout", new HashMap<>());
        claims.setClaim("events", events);
        return claims;
    }

    protected String determineIssuer(final SingleLogoutRequestContext sloRequest) {
        val configurationContext = configurationProvider.getObject();
        if (sloRequest.getRegisteredService() instanceof OidcRegisteredService oidcRegisteredService) {
            return configurationContext.getIssuerService().determineIssuer(Optional.of(oidcRegisteredService));
        }
        return configurationContext.getIssuerService().determineIssuer(Optional.empty());
    }
}
