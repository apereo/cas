package org.apereo.cas.uma.ticket.rpt;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.ticket.BaseIdTokenGeneratorService;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.uma.ticket.permission.UmaPermissionTicket;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.profile.UserProfile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.UUID;

/**
 * This is {@link UmaIdTokenGeneratorService}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class UmaIdTokenGeneratorService extends BaseIdTokenGeneratorService {
    public UmaIdTokenGeneratorService(final OAuth20ConfigurationContext configurationContext) {
        super(configurationContext);
    }

    @Override
    public String generate(final HttpServletRequest request,
                           final HttpServletResponse response,
                           final OAuth20AccessToken accessToken,
                           final long timeoutInSeconds,
                           final OAuth20ResponseTypes responseType,
                           final OAuthRegisteredService registeredService) {

        val context = new JEEContext(request, response, getConfigurationContext().getSessionStore());
        LOGGER.debug("Attempting to produce claims for the rpt access token [{}]", accessToken);
        val authenticatedProfile = getAuthenticatedProfile(request, response);
        val claims = buildJwtClaims(request, accessToken, timeoutInSeconds,
            registeredService, authenticatedProfile, context, responseType);

        return encodeAndFinalizeToken(claims, registeredService, accessToken);
    }

    /**
     * Build jwt claims jwt claims.
     *
     * @param request          the request
     * @param accessToken      the access token
     * @param timeoutInSeconds the timeout in seconds
     * @param service          the service
     * @param profile          the profile
     * @param context          the context
     * @param responseType     the response type
     * @return the jwt claims
     */
    protected JwtClaims buildJwtClaims(final HttpServletRequest request,
                                       final OAuth20AccessToken accessToken,
                                       final long timeoutInSeconds,
                                       final OAuthRegisteredService service,
                                       final UserProfile profile,
                                       final JEEContext context,
                                       final OAuth20ResponseTypes responseType) {

        val permissionTicket = (UmaPermissionTicket) request.getAttribute(UmaPermissionTicket.class.getName());

        val claims = new JwtClaims();
        claims.setJwtId(UUID.randomUUID().toString());
        claims.setIssuer(getConfigurationContext().getCasProperties().getAuthn().getUma().getIssuer());
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
