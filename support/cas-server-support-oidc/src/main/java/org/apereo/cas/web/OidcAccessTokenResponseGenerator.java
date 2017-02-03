package org.apereo.cas.web;

import com.fasterxml.jackson.core.JsonGenerator;
import org.apereo.cas.OidcConstants;
import org.apereo.cas.OidcTokenSigningService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.OAuthConstants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.OAuth20AccessTokenResponseGenerator;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.refreshtoken.RefreshToken;
import org.apereo.cas.util.CollectionUtils;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * This is {@link OidcAccessTokenResponseGenerator}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OidcAccessTokenResponseGenerator extends OAuth20AccessTokenResponseGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(OidcAccessTokenResponseGenerator.class);

    private final String issuer;
    private final int skew;
    private final OidcTokenSigningService signingService;

    public OidcAccessTokenResponseGenerator(final String issuer,
                                            final int skew,
                                            final OidcTokenSigningService signingService) {
        this.issuer = issuer;
        this.skew = skew;
        this.signingService = signingService;
    }

    @Override
    protected void generateJsonInternal(final HttpServletRequest request, final HttpServletResponse response, final JsonGenerator jsonGenerator,
                                        final AccessToken accessTokenId, final RefreshToken refreshTokenId, final long timeout,
                                        final Service service, final OAuthRegisteredService registeredService) throws Exception {

        super.generateJsonInternal(request, response, jsonGenerator, accessTokenId, refreshTokenId, timeout, service, registeredService);
        final OidcRegisteredService oidcRegisteredService = (OidcRegisteredService) registeredService;

        final J2EContext context = new J2EContext(request, response);
        final ProfileManager manager = new ProfileManager(context);
        final Optional<UserProfile> profile = manager.get(true);

        LOGGER.debug("Attempting to produce claims for the id token [{}]", accessTokenId);
        final JwtClaims claims = produceIdTokenClaims(request, accessTokenId, timeout,
                oidcRegisteredService, profile.get(), context);
        LOGGER.debug("Produce claims for the id token [{}] as [{}]", accessTokenId, claims);

        final String idToken = this.signingService.signIdTokenClaim(oidcRegisteredService, claims);
        jsonGenerator.writeStringField(OidcConstants.ID_TOKEN, idToken);
    }

    /**
     * Produce id token claims jwt claims.
     *
     * @param request       the request
     * @param accessTokenId the access token id
     * @param timeout       the timeout
     * @param service       the service
     * @param profile       the user profile
     * @param context       the context
     * @return the jwt claims
     */
    protected JwtClaims produceIdTokenClaims(final HttpServletRequest request, final AccessToken accessTokenId, final long timeout,
                                             final OidcRegisteredService service, final UserProfile profile, final J2EContext context) {
        final Authentication authentication = accessTokenId.getAuthentication();
        final Principal principal = authentication.getPrincipal();

        final JwtClaims claims = new JwtClaims();
        claims.setJwtId(UUID.randomUUID().toString());
        claims.setIssuer(this.issuer);
        claims.setAudience(service.getClientId());

        final NumericDate expirationDate = NumericDate.now();
        expirationDate.addSeconds(timeout);
        claims.setExpirationTime(expirationDate);
        claims.setIssuedAtToNow();
        claims.setNotBeforeMinutesInThePast(this.skew);
        claims.setSubject(principal.getId());

        if (authentication.getAttributes().containsKey(casProperties.getAuthn().getMfa().getAuthenticationContextAttribute())) {
            final Collection<Object> val = CollectionUtils.toCollection(
                    authentication.getAttributes().get(casProperties.getAuthn().getMfa().getAuthenticationContextAttribute()));
            claims.setStringClaim(OidcConstants.ACR, val.iterator().next().toString());
        }
        if (authentication.getAttributes().containsKey(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS)) {
            final Collection<Object> val = CollectionUtils.toCollection(
                    authentication.getAttributes().get(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS));
            claims.setStringListClaim(OidcConstants.AMR, val.toArray(new String[]{}));
        }

        claims.setClaim(OAuthConstants.STATE, authentication.getAttributes().get(OAuthConstants.STATE));
        claims.setClaim(OAuthConstants.NONCE, authentication.getAttributes().get(OAuthConstants.NONCE));

        principal.getAttributes().entrySet().stream()
                .filter(entry -> OidcConstants.CLAIMS.contains(entry.getKey()))
                .forEach(entry -> claims.setClaim(entry.getKey(), entry.getValue()));

        if (!claims.hasClaim(OidcConstants.CLAIM_PREFERRED_USERNAME)) {
            claims.setClaim(OidcConstants.CLAIM_PREFERRED_USERNAME, profile.getId());
        }

        return claims;
    }
}

