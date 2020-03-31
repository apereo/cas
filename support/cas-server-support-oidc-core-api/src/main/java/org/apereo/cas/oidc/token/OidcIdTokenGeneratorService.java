package org.apereo.cas.oidc.token;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20AccessTokenAtHashGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.ticket.BaseIdTokenGeneratorService;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DigestUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.profile.UserProfile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * This is {@link OidcIdTokenGeneratorService}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Getter
public class OidcIdTokenGeneratorService extends BaseIdTokenGeneratorService {

    public OidcIdTokenGeneratorService(final OAuth20ConfigurationContext configurationContext) {
        super(configurationContext);
    }

    @Override
    public String generate(final HttpServletRequest request,
                           final HttpServletResponse response,
                           final OAuth20AccessToken accessToken,
                           final long timeoutInSeconds,
                           final OAuth20ResponseTypes responseType,
                           final OAuthRegisteredService registeredService) {

        if (!(registeredService instanceof OidcRegisteredService)) {
            throw new IllegalArgumentException("Registered service instance is not an OIDC service");
        }

        val oidcRegisteredService = (OidcRegisteredService) registeredService;
        val context = new JEEContext(request, response, getConfigurationContext().getSessionStore());
        LOGGER.trace("Attempting to produce claims for the id token [{}]", accessToken);
        val authenticatedProfile = getAuthenticatedProfile(request, response);
        val claims = buildJwtClaims(request, accessToken, timeoutInSeconds,
            oidcRegisteredService, authenticatedProfile, context, responseType);

        return encodeAndFinalizeToken(claims, oidcRegisteredService, accessToken);
    }


    /**
     * Produce claims as jwt.
     *
     * @param request          the request
     * @param accessToken      the access token
     * @param timeoutInSeconds the timeoutInSeconds
     * @param service          the service
     * @param profile          the user profile
     * @param context          the context
     * @param responseType     the response type
     * @return the jwt claims
     */
    protected JwtClaims buildJwtClaims(final HttpServletRequest request,
                                       final OAuth20AccessToken accessToken,
                                       final long timeoutInSeconds,
                                       final OidcRegisteredService service,
                                       final UserProfile profile,
                                       final JEEContext context,
                                       final OAuth20ResponseTypes responseType) {
        val authentication = accessToken.getAuthentication();

        val principal = this.getConfigurationContext().getProfileScopeToAttributesFilter()
            .filter(accessToken.getService(), authentication.getPrincipal(), service, context, accessToken);

        val oidc = getConfigurationContext().getCasProperties().getAuthn().getOidc();

        val claims = new JwtClaims();

        val jwtId = getJwtId(accessToken.getTicketGrantingTicket());
        claims.setJwtId(jwtId);
        claims.setClaim(OidcConstants.CLAIM_SESSIOND_ID, DigestUtils.sha(jwtId));

        claims.setIssuer(oidc.getIssuer());
        claims.setAudience(accessToken.getClientId());

        val expirationDate = NumericDate.now();
        expirationDate.addSeconds(timeoutInSeconds);
        claims.setExpirationTime(expirationDate);
        claims.setIssuedAtToNow();
        claims.setNotBeforeMinutesInThePast(oidc.getSkew());
        claims.setSubject(principal.getId());

        val mfa = getConfigurationContext().getCasProperties().getAuthn().getMfa();
        val attributes = authentication.getAttributes();

        if (attributes.containsKey(mfa.getAuthenticationContextAttribute())) {
            val val = CollectionUtils.toCollection(attributes.get(mfa.getAuthenticationContextAttribute()));
            claims.setStringClaim(OidcConstants.ACR, val.iterator().next().toString());
        }
        if (attributes.containsKey(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS)) {
            val val = CollectionUtils.toCollection(attributes.get(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS));
            claims.setStringListClaim(OidcConstants.AMR, val.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
        }

        claims.setStringClaim(OAuth20Constants.CLIENT_ID, service.getClientId());
        claims.setClaim(OidcConstants.CLAIM_AUTH_TIME, accessToken.getAuthentication().getAuthenticationDate().toEpochSecond());

        if (attributes.containsKey(OAuth20Constants.STATE)) {
            claims.setClaim(OAuth20Constants.STATE, attributes.get(OAuth20Constants.STATE).get(0));
        }
        if (attributes.containsKey(OAuth20Constants.NONCE)) {
            claims.setClaim(OAuth20Constants.NONCE, attributes.get(OAuth20Constants.NONCE).get(0));
        }
        generateAccessTokenHash(accessToken, service, claims);
        LOGGER.trace("Comparing principal attributes [{}] with supported claims [{}]", principal.getAttributes(), oidc.getClaims());

        principal.getAttributes().entrySet()
            .stream()
            .filter(entry -> {
                if (oidc.getClaims().contains(entry.getKey())) {
                    LOGGER.trace("Found supported claim [{}]", entry.getKey());
                    return true;
                }
                LOGGER.warn("Claim [{}] is not defined as a supported claim among [{}]. Skipping...",
                    entry.getKey(), oidc.getClaims());
                return false;
            })
            .forEach(entry -> {
                val claimValue = CollectionUtils.toCollection(entry.getValue());
                if (claimValue.size() == 1) {
                    val value = CollectionUtils.firstElement(claimValue);
                    value.ifPresent(v -> {
                        val bool = BooleanUtils.toBooleanObject(v.toString());
                        claims.setClaim(entry.getKey(), Objects.requireNonNullElse(bool, v));
                    });
                } else {
                    claims.setClaim(entry.getKey(), claimValue);
                }
            });

        if (!claims.hasClaim(OidcConstants.CLAIM_PREFERRED_USERNAME)) {
            claims.setClaim(OidcConstants.CLAIM_PREFERRED_USERNAME, principal.getId());
        }

        return claims;
    }

    /**
     * Gets oauth service ticket.
     *
     * @param tgt the tgt
     * @return the o auth service ticket
     */
    protected String getJwtId(final TicketGrantingTicket tgt) {
        val oAuthCallbackUrl = getConfigurationContext().getCasProperties().getServer().getPrefix()
            + OAuth20Constants.BASE_OAUTH20_URL + '/'
            + OAuth20Constants.CALLBACK_AUTHORIZE_URL_DEFINITION;

        val oAuthServiceTicket = Stream.concat(
            tgt.getServices().entrySet().stream(),
            tgt.getProxyGrantingTickets().entrySet().stream())
            .filter(e -> {
                val service = getConfigurationContext().getServicesManager().findServiceBy(e.getValue());
                return service != null && service.getServiceId().equals(oAuthCallbackUrl);
            })
            .findFirst();
        if (oAuthServiceTicket.isEmpty()) {
            LOGGER.trace("Cannot find ticket issued to [{}] as part of the authentication context", oAuthCallbackUrl);
            return tgt.getId();
        }
        return oAuthServiceTicket.get().getKey();
    }

    /**
     * Generate access token hash string.
     *
     * @param accessToken       the access token
     * @param registeredService the service
     * @param claims            the claims
     */
    protected void generateAccessTokenHash(final OAuth20AccessToken accessToken,
                                           final OidcRegisteredService registeredService,
                                           final JwtClaims claims) {
        val encodedAccessToken = OAuth20JwtAccessTokenEncoder.builder()
            .accessToken(accessToken)
            .registeredService(registeredService)
            .service(accessToken.getService())
            .accessTokenJwtBuilder(getConfigurationContext().getAccessTokenJwtBuilder())
            .casProperties(getConfigurationContext().getCasProperties())
            .build()
            .encode();

        val alg = getConfigurationContext().getIdTokenSigningAndEncryptionService().getJsonWebKeySigningAlgorithm(registeredService);
        val hash = OAuth20AccessTokenAtHashGenerator.builder()
            .encodedAccessToken(encodedAccessToken)
            .algorithm(alg)
            .registeredService(registeredService)
            .build()
            .generate();
        claims.setClaim(OidcConstants.CLAIM_AT_HASH, hash);
    }

}

