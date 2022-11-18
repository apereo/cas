package org.apereo.cas.oidc.token;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20AccessTokenAtHashGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.ticket.AuthenticatedServicesAwareTicketGrantingTicket;
import org.apereo.cas.ticket.BaseIdTokenGeneratorService;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.pac4j.core.profile.UserProfile;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link OidcIdTokenGeneratorService}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class OidcIdTokenGeneratorService extends BaseIdTokenGeneratorService<OidcConfigurationContext> {

    public OidcIdTokenGeneratorService(final ObjectProvider<OidcConfigurationContext> configurationContext) {
        super(configurationContext);
    }

    private static void setClaim(final JwtClaims claims, final String claimName, final Object claimValue) {
        if (claimValue != null && StringUtils.isNotBlank(claimValue.toString())) {
            claims.setClaim(claimName, claimValue);
        }
    }

    @Override
    public String generate(final OAuth20AccessToken accessToken,
                           final UserProfile userProfile,
                           final OAuth20ResponseTypes responseType,
                           final OAuth20GrantTypes grantType,
                           final OAuthRegisteredService registeredService) throws Exception {
        val timeout = getConfigurationContext().getIdTokenExpirationPolicy().buildTicketExpirationPolicy().getTimeToLive();
        Assert.isAssignable(OidcRegisteredService.class, registeredService.getClass(),
            "Registered service instance is not an OIDC service");

        val oidcRegisteredService = (OidcRegisteredService) registeredService;
        LOGGER.trace("Attempting to produce claims for the id token [{}]", accessToken);
        val claims = buildJwtClaims(accessToken, timeout, oidcRegisteredService, responseType, grantType);
        return encodeAndFinalizeToken(claims, oidcRegisteredService, accessToken);
    }

    /**
     * Produce claims as jwt.
     * As per OpenID Connect Core section 5.4, 'The Claims requested by the profile,
     * email, address, and phone scope values are returned from the UserInfo Endpoint',
     * except for response_type=id_token, where they are returned in the id_token
     * (as there is no access token issued that could be used to access the userinfo endpoint).
     *
     * @param accessToken       the access token
     * @param timeoutInSeconds  the timeoutInSeconds
     * @param registeredService the service
     * @param responseType      the response type
     * @param grantType         the grant type
     * @return the jwt claims
     */
    protected JwtClaims buildJwtClaims(final OAuth20AccessToken accessToken,
                                       final long timeoutInSeconds,
                                       final OidcRegisteredService registeredService,
                                       final OAuth20ResponseTypes responseType,
                                       final OAuth20GrantTypes grantType) {
        val authentication = accessToken.getAuthentication();
        val activePrincipal = buildPrincipalForAttributeFilter(accessToken, registeredService);
        val principal = getConfigurationContext().getProfileScopeToAttributesFilter()
            .filter(accessToken.getService(), activePrincipal, registeredService, accessToken);
        LOGGER.debug("Principal to use to build the ID token is [{}]", principal);

        val oidc = getConfigurationContext().getCasProperties().getAuthn().getOidc();
        val claims = new JwtClaims();

        val tgt = accessToken.getTicketGrantingTicket();
        val jwtId = getJwtId(tgt);
        LOGGER.debug("Calculated ID token jti claim to be [{}]", jwtId);
        claims.setJwtId(jwtId);

        claims.setClaim(OidcConstants.CLAIM_SESSION_ID, DigestUtils.sha(jwtId));

        claims.setIssuer(getConfigurationContext().getIssuerService().determineIssuer(Optional.ofNullable(registeredService)));
        claims.setAudience(accessToken.getClientId());

        val expirationDate = NumericDate.now();
        expirationDate.addSeconds(timeoutInSeconds);
        claims.setExpirationTime(expirationDate);
        LOGGER.debug("Calculated ID token expiration claim to be [{}]", expirationDate);
        claims.setIssuedAtToNow();
        claims.setNotBeforeMinutesInThePast((float) Beans.newDuration(oidc.getCore().getSkew()).toMinutes());

        val subject = registeredService.getUsernameAttributeProvider().resolveUsername(principal,
            accessToken.getService(), registeredService);
        LOGGER.debug("Calculated ID token subject claim to be [{}]", subject);
        claims.setSubject(principal.getId());

        val mfa = getConfigurationContext().getCasProperties().getAuthn().getMfa();
        val attributes = authentication.getAttributes();

        if (attributes.containsKey(mfa.getCore().getAuthenticationContextAttribute())) {
            val acrValues = CollectionUtils.toCollection(attributes.get(mfa.getCore().getAuthenticationContextAttribute()));
            val authnContexts = oidc.getCore().getAuthenticationContextReferenceMappings();
            val mappings = CollectionUtils.convertDirectedListToMap(authnContexts);
            val acrMapped = acrValues
                .stream()
                .map(acrValue ->
                    mappings.entrySet()
                        .stream()
                        .filter(entry -> entry.getValue().equalsIgnoreCase(acrValue.toString()))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElseGet(acrValue::toString))
                .collect(Collectors.joining(" "));
            LOGGER.debug("ID token acr claim calculated as [{}]", acrMapped);
            claims.setStringClaim(OidcConstants.ACR, acrMapped);
        }
        if (attributes.containsKey(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS)) {
            val val = CollectionUtils.toCollection(attributes.get(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS));
            LOGGER.debug("ID token amr claim calculated as [{}]", val);
            claims.setStringListClaim(OidcConstants.AMR, val.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
        }

        claims.setStringClaim(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        claims.setClaim(OidcConstants.CLAIM_AUTH_TIME, tgt.getAuthentication().getAuthenticationDate().toEpochSecond());

        if (attributes.containsKey(OAuth20Constants.STATE)) {
            setClaim(claims, OAuth20Constants.STATE, attributes.get(OAuth20Constants.STATE).get(0));
        }
        if (attributes.containsKey(OAuth20Constants.NONCE)) {
            setClaim(claims, OAuth20Constants.NONCE, attributes.get(OAuth20Constants.NONCE).get(0));
        }
        generateAccessTokenHash(accessToken, registeredService, claims);

        val includeClaims = responseType != OAuth20ResponseTypes.CODE && grantType != OAuth20GrantTypes.AUTHORIZATION_CODE;
        if (includeClaims || oidc.getIdToken().isIncludeIdTokenClaims()) {
            FunctionUtils.doIf(oidc.getIdToken().isIncludeIdTokenClaims(),
                    ignore -> LOGGER.warn("Individual claims requested by OpenID scopes are forced to be included in the ID token. "
                                          + "This is a violation of the OpenID Connect specification and a workaround via dedicated CAS configuration. "
                                          + "Claims should be requested from the userinfo/profile endpoints in exchange for an access token."))
                .accept(claims);
            collectIdTokenClaims(principal, registeredService, claims);
        } else {
            LOGGER.debug("Per OpenID Connect specification, individual claims requested by OpenID scopes "
                         + "such as profile, email, address, etc. are only put "
                         + "into the OpenID Connect ID token when the response type is set to id_token.");
        }

        return claims;
    }

    private Principal buildPrincipalForAttributeFilter(final OAuth20AccessToken accessToken,
                                                       final RegisteredService registeredService) {
        val authentication = accessToken.getAuthentication();
        val attributes = new HashMap<>(authentication.getPrincipal().getAttributes());
        val authnAttributes = getConfigurationContext().getAuthenticationAttributeReleasePolicy()
            .getAuthenticationAttributesForRelease(authentication, registeredService);
        attributes.putAll(authnAttributes);
        return getConfigurationContext().getPrincipalFactory().createPrincipal(authentication.getPrincipal().getId(), attributes);
    }

    protected void collectIdTokenClaims(final Principal principal,
                                        final RegisteredService registeredService,
                                        final JwtClaims claims) {
        val oidc = getConfigurationContext().getCasProperties().getAuthn().getOidc();
        LOGGER.trace("Comparing principal attributes [{}] with supported claims [{}]",
            principal.getAttributes(), oidc.getDiscovery().getClaims());
        principal.getAttributes()
            .entrySet()
            .stream()
            .filter(entry -> {
                if (isClaimSupportedForRelease(entry.getKey(), registeredService)) {
                    LOGGER.trace("Found supported claim [{}]", entry.getKey());
                    return true;
                }
                LOGGER.debug("Claim [{}] is not defined as a supported claim among [{}]. Skipping...",
                    entry.getKey(), oidc.getDiscovery().getClaims());
                return false;
            })
            .forEach(entry -> handleMappedClaimOrDefault(entry.getKey(), registeredService, principal, claims, entry.getValue()));

        if (!claims.hasClaim(OidcConstants.CLAIM_PREFERRED_USERNAME)) {
            handleMappedClaimOrDefault(OidcConstants.CLAIM_PREFERRED_USERNAME,
                registeredService, principal, claims, principal.getId());
        }
    }

    private boolean isClaimSupportedForRelease(final String claimName, final RegisteredService registeredService) {
        val mapper = getConfigurationContext().getAttributeToScopeClaimMapper();
        val mappedClaim = mapper.toMappedClaimName(claimName, registeredService);
        val oidc = getConfigurationContext().getCasProperties().getAuthn().getOidc();
        return oidc.getDiscovery().getClaims().contains(claimName) || oidc.getDiscovery().getClaims().contains(mappedClaim);
    }

    /**
     * Handle mapped claim or default.
     *
     * @param claimName         the claim name
     * @param registeredService the registered service
     * @param principal         the principal
     * @param claims            the claims
     * @param defaultValue      the default value
     */
    protected void handleMappedClaimOrDefault(final String claimName,
                                              final RegisteredService registeredService,
                                              final Principal principal,
                                              final JwtClaims claims,
                                              final Object defaultValue) {
        val mapper = getConfigurationContext().getAttributeToScopeClaimMapper();
        val collectionValues = mapper.mapClaim(claimName, registeredService, principal, defaultValue);
        getConfigurationContext().getIdTokenClaimCollector().collect(claims, claimName, collectionValues);
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

        val streamServices = new LinkedHashMap<String, Service>();
        if (tgt instanceof AuthenticatedServicesAwareTicketGrantingTicket) {
            val services = ((AuthenticatedServicesAwareTicketGrantingTicket) tgt).getServices();
            streamServices.putAll(services);
        }
        streamServices.putAll(tgt.getProxyGrantingTickets());

        val oAuthServiceTicket = streamServices.entrySet()
            .stream()
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
            .encode(accessToken.getId());

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

