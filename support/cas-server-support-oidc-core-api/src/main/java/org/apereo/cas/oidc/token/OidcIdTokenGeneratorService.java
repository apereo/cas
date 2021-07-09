package org.apereo.cas.oidc.token;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20AccessTokenAtHashGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.ticket.BaseIdTokenGeneratorService;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DigestUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.profile.UserProfile;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link OidcIdTokenGeneratorService}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class OidcIdTokenGeneratorService extends BaseIdTokenGeneratorService<OidcConfigurationContext> {

    public OidcIdTokenGeneratorService(final OidcConfigurationContext configurationContext) {
        super(configurationContext);
    }

    @Override
    public String generate(final HttpServletRequest request,
                           final HttpServletResponse response,
                           final OAuth20AccessToken accessToken,
                           final long timeoutInSeconds,
                           final OAuth20ResponseTypes responseType,
                           final OAuthRegisteredService registeredService) {
        Assert.isAssignable(OidcRegisteredService.class, registeredService.getClass(), "Registered service instance is not an OIDC service");
        val oidcRegisteredService = (OidcRegisteredService) registeredService;
        val context = new JEEContext(request, response);
        LOGGER.trace("Attempting to produce claims for the id token [{}]", accessToken);
        val authenticatedProfile = getAuthenticatedProfile(request, response);
        LOGGER.debug("Current user profile to use for ID token is [{}]", authenticatedProfile);
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

        val tgt = accessToken.getTicketGrantingTicket();
        val jwtId = getJwtId(tgt);
        claims.setJwtId(jwtId);
        claims.setClaim(OidcConstants.CLAIM_SESSIOND_ID, DigestUtils.sha(jwtId));

        claims.setIssuer(getConfigurationContext().getIssuerService().determineIssuer(Optional.empty()));
        claims.setAudience(accessToken.getClientId());

        val expirationDate = NumericDate.now();
        expirationDate.addSeconds(timeoutInSeconds);
        claims.setExpirationTime(expirationDate);
        claims.setIssuedAtToNow();
        claims.setNotBeforeMinutesInThePast((float) Beans.newDuration(oidc.getCore().getSkew()).toMinutes());
        claims.setSubject(principal.getId());

        val mfa = getConfigurationContext().getCasProperties().getAuthn().getMfa();
        val attributes = authentication.getAttributes();

        if (attributes.containsKey(mfa.getCore().getAuthenticationContextAttribute())) {
            val val = CollectionUtils.toCollection(attributes.get(mfa.getCore().getAuthenticationContextAttribute()));
            claims.setStringClaim(OidcConstants.ACR, val.iterator().next().toString());
        }
        if (attributes.containsKey(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS)) {
            val val = CollectionUtils.toCollection(attributes.get(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS));
            claims.setStringListClaim(OidcConstants.AMR, val.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
        }

        claims.setStringClaim(OAuth20Constants.CLIENT_ID, service.getClientId());
        claims.setClaim(OidcConstants.CLAIM_AUTH_TIME, tgt.getAuthentication().getAuthenticationDate().toEpochSecond());

        if (attributes.containsKey(OAuth20Constants.STATE)) {
            claims.setClaim(OAuth20Constants.STATE, attributes.get(OAuth20Constants.STATE).get(0));
        }
        if (attributes.containsKey(OAuth20Constants.NONCE)) {
            claims.setClaim(OAuth20Constants.NONCE, attributes.get(OAuth20Constants.NONCE).get(0));
        }
        generateAccessTokenHash(accessToken, service, claims);
        LOGGER.trace("Comparing principal attributes [{}] with supported claims [{}]",
            principal.getAttributes(), oidc.getDiscovery().getClaims());

        principal.getAttributes()
            .entrySet()
            .stream()
            .filter(entry -> {
                if (oidc.getDiscovery().getClaims().contains(entry.getKey())) {
                    LOGGER.trace("Found supported claim [{}]", entry.getKey());
                    return true;
                }
                LOGGER.warn("Claim [{}] is not defined as a supported claim among [{}]. Skipping...",
                    entry.getKey(), oidc.getDiscovery().getClaims());
                return false;
            })
            .forEach(entry -> handleMappedClaimOrDefault(entry.getKey(), principal, claims, entry.getValue()));

        if (!claims.hasClaim(OidcConstants.CLAIM_PREFERRED_USERNAME)) {
            handleMappedClaimOrDefault(OidcConstants.CLAIM_PREFERRED_USERNAME,
                principal, claims, principal.getId());
        }

        return claims;
    }

    /**
     * Handle mapped claim or default.
     *
     * @param claimName    the claim name
     * @param principal    the principal
     * @param claims       the claims
     * @param defaultValue the default value
     */
    protected void handleMappedClaimOrDefault(final String claimName,
                                              final Principal principal,
                                              final JwtClaims claims,
                                              final Object defaultValue) {
        val mapper = getConfigurationContext().getAttributeToScopeClaimMapper();
        val attribute = mapper.containsMappedAttribute(claimName)
            ? mapper.getMappedAttribute(claimName)
            : claimName;

        val attributeValues = principal.getAttributes().containsKey(attribute)
            ? principal.getAttributes().get(attribute)
            : defaultValue;

        LOGGER.trace("Handling claim [{}] with value(s) [{}]", attribute, attributeValues);
        val collectionValues = CollectionUtils.toCollection(attributeValues)
            .stream()
            .map(value -> {
                val bool = BooleanUtils.toBooleanObject(value.toString());
                return Objects.requireNonNullElse(bool, value);
            })
            .collect(Collectors.toCollection(ArrayList::new));
        
        if (collectionValues.size() == 1) {
            claims.setClaim(attribute, collectionValues.get(0));
        } else if (collectionValues.size() > 1) {
            claims.setClaim(attribute, collectionValues);
        }
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
            .issuer(getConfigurationContext().getIssuerService().determineIssuer(Optional.of(registeredService)))
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

