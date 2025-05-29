package org.apereo.cas.uma.ticket.rpt;

import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.idtoken.BaseIdTokenGeneratorService;
import org.apereo.cas.ticket.idtoken.IdTokenGenerationContext;
import org.apereo.cas.ticket.idtoken.OidcIdToken;
import org.apereo.cas.uma.UmaConfigurationContext;
import org.apereo.cas.uma.ticket.permission.UmaPermissionTicket;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.pac4j.core.profile.UserProfile;
import org.springframework.beans.factory.ObjectProvider;
import java.util.ArrayList;
import java.util.UUID;

/**
 * This is {@link UmaIdTokenGeneratorService}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class UmaIdTokenGeneratorService extends BaseIdTokenGeneratorService<UmaConfigurationContext> {
    public UmaIdTokenGeneratorService(final ObjectProvider<UmaConfigurationContext> configurationContext) {
        super(configurationContext);
    }

    @Override
    public OidcIdToken generate(final IdTokenGenerationContext context) throws Throwable {
        val timeoutInSeconds = Beans.newDuration(getConfigurationContext().getCasProperties()
            .getAuthn().getOauth().getUma().getRequestingPartyToken().getMaxTimeToLiveInSeconds()).toSeconds();
        LOGGER.debug("Attempting to produce claims for the RPT access token [{}]", context.getAccessToken());
        val claims = buildJwtClaims(context.getAccessToken(), timeoutInSeconds,
            context.getUserProfile(), context.getRegisteredService(), context.getResponseType());
        val finalToken = encodeAndFinalizeToken(claims, context);
        return new OidcIdToken(finalToken, claims, null);
    }

    protected JwtClaims buildJwtClaims(final OAuth20AccessToken accessToken,
                                       final long timeoutInSeconds,
                                       final UserProfile profile,
                                       final OAuthRegisteredService service,
                                       final OAuth20ResponseTypes responseType) {
        val permissionTicket = (UmaPermissionTicket) profile.getAttribute(UmaPermissionTicket.class.getName());
        val claims = new JwtClaims();
        claims.setJwtId(UUID.randomUUID().toString());
        claims.setIssuer(getConfigurationContext().getCasProperties().getAuthn().getOauth().getUma().getCore().getIssuer());
        claims.setAudience(String.valueOf(permissionTicket.getResourceSet().getId()));

        val expirationDate = NumericDate.now();
        expirationDate.addSeconds(timeoutInSeconds);
        claims.setExpirationTime(expirationDate);
        claims.setIssuedAtToNow();
        claims.setSubject(profile.getId());

        permissionTicket.getClaims().forEach((k, v) -> claims.setStringListClaim(k, v.toString()));
        claims.setStringListClaim(OAuth20Constants.SCOPE, new ArrayList<>(permissionTicket.getScopes()));
        claims.setStringListClaim(OAuth20Constants.CLIENT_ID, service.getClientId());

        return claims;
    }
}
