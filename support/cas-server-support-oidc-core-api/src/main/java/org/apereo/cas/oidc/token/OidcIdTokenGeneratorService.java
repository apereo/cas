package org.apereo.cas.oidc.token;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.attribute.AttributeDefinition;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.claims.OidcAttributeDefinition;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceOidcIdTokenExpirationPolicy;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20AccessTokenAtHashGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.BaseIdTokenGeneratorService;
import org.apereo.cas.ticket.OidcIdToken;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.function.FunctionUtils;
import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.pac4j.core.profile.UserProfile;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.Assert;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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

    public OidcIdTokenGeneratorService(final ObjectProvider<OidcConfigurationContext> configurationContext) {
        super(configurationContext);
    }

    private static void setClaim(final JwtClaims claims, final String claimName, final Object claimValue) {
        if (claimValue != null && StringUtils.isNotBlank(claimValue.toString())) {
            claims.setClaim(claimName, claimValue);
        }
    }

    @Audit(action = AuditableActions.OIDC_ID_TOKEN,
        actionResolverName = AuditActionResolvers.OIDC_ID_TOKEN_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.OIDC_ID_TOKEN_RESOURCE_RESOLVER)
    @Override
    public OidcIdToken generate(final OAuth20AccessToken accessToken,
                                final UserProfile userProfile,
                                final OAuth20ResponseTypes responseType,
                                final OAuth20GrantTypes grantType,
                                final OAuthRegisteredService registeredService) throws Throwable {
        Assert.isAssignable(OidcRegisteredService.class, registeredService.getClass(),
            "Registered service instance is not an OIDC service");
        if (!accessToken.getScopes().contains(OidcConstants.StandardScopes.OPENID.getScope())) {
            LOGGER.warn("Authentication request does not include the [{}] scope. "
                + "Including this scope is a MUST for OpenID Connect and CAS will not produce an ID token without this scope.",
                OidcConstants.StandardScopes.OPENID.getScope());
            return null;
        }
        val oidcRegisteredService = (OidcRegisteredService) registeredService;
        LOGGER.trace("Attempting to produce claims for the id token [{}]", accessToken);
        val claims = buildJwtClaims(accessToken, oidcRegisteredService, responseType, grantType);
        val finalIdToken = encodeAndFinalizeToken(claims, oidcRegisteredService, accessToken);
        return new OidcIdToken(finalIdToken, claims);
    }

    protected JwtClaims buildJwtClaims(final OAuth20AccessToken accessToken,
                                       final OidcRegisteredService registeredService,
                                       final OAuth20ResponseTypes responseType,
                                       final OAuth20GrantTypes grantType) throws Throwable {
        val authentication = accessToken.getAuthentication();
        val activePrincipal = buildPrincipalForAttributeFilter(accessToken, registeredService);
        val principal = getConfigurationContext().getProfileScopeToAttributesFilter()
            .filter(accessToken.getService(), activePrincipal, registeredService, accessToken);
        LOGGER.debug("Principal to use to build the ID token is [{}]", principal);

        val oidc = getConfigurationContext().getCasProperties().getAuthn().getOidc();
        val claims = new JwtClaims();

        val jwtId = getJwtId(accessToken);
        LOGGER.debug("Calculated ID token jti claim to be [{}]", jwtId);
        claims.setJwtId(jwtId);

        claims.setClaim(OidcConstants.CLAIM_SESSION_ID, DigestUtils.sha(jwtId));
        claims.setIssuer(getConfigurationContext().getIssuerService().determineIssuer(Optional.ofNullable(registeredService)));
        val audience = registeredService.getAudience().isEmpty()
            ? List.of(accessToken.getClientId())
            : new ArrayList<>(registeredService.getAudience());
        claims.setAudience(audience);
        LOGGER.debug("Calculated ID token aud claim to be [{}]", audience);

        buildExpirationClaim(claims, registeredService);

        claims.setIssuedAtToNow();
        claims.setNotBeforeMinutesInThePast((float) Beans.newDuration(oidc.getCore().getSkew()).toMinutes());
        claims.setSubject(principal.getId());

        buildAuthenticationContextClassRef(claims, authentication);

        val amrValues = buildAuthenticationMethods(authentication);
        if (!amrValues.isEmpty()) {
            LOGGER.debug("ID token amr claim calculated as [{}]", amrValues);
            claims.setStringListClaim(OidcConstants.AMR, amrValues.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
        }

        val attributes = authentication.getAttributes();
        claims.setStringClaim(OAuth20Constants.CLIENT_ID, registeredService.getClientId());

        val authTime = accessToken.isStateless()
            ? authentication.getAuthenticationDate().toEpochSecond()
            : ((AuthenticationAwareTicket) accessToken.getTicketGrantingTicket()).getAuthentication().getAuthenticationDate().toEpochSecond();
        claims.setClaim(OidcConstants.CLAIM_AUTH_TIME, authTime);

        if (attributes.containsKey(OAuth20Constants.STATE)) {
            setClaim(claims, OAuth20Constants.STATE, attributes.get(OAuth20Constants.STATE).getFirst());
        }
        if (attributes.containsKey(OAuth20Constants.NONCE)) {
            setClaim(claims, OAuth20Constants.NONCE, attributes.get(OAuth20Constants.NONCE).getFirst());
        }
        generateAccessTokenHash(accessToken, registeredService, claims);

        val includeClaims = responseType != OAuth20ResponseTypes.CODE && grantType != OAuth20GrantTypes.AUTHORIZATION_CODE;
        if (includeClaims || oidc.getIdToken().isIncludeIdTokenClaims()) {
            FunctionUtils.doIf(oidc.getIdToken().isIncludeIdTokenClaims(),
                    __ -> LOGGER.warn("Individual claims requested by OpenID scopes are forced to be included in the ID token. "
                        + "This is a violation of the OpenID Connect specification and a workaround via dedicated CAS configuration. "
                        + "Claims should be requested from the userinfo/profile endpoints in exchange for an access token."))
                .accept(claims);
            collectIdTokenClaims(principal, registeredService, claims);
        } else {
            LOGGER.debug("Per OpenID Connect specification, individual claims requested by OpenID scopes "
                + "such as profile, email, address, etc. are only put "
                + "into the OpenID Connect ID token when the response type is set to id_token.");
        }
        claims.setStringClaim(OidcConstants.TXN, UUID.randomUUID().toString());
        return claims;
    }

    protected void buildExpirationClaim(final JwtClaims claims, final OidcRegisteredService registeredService) {
        val expirationPolicy = getConfigurationContext().getIdTokenExpirationPolicy().buildTicketExpirationPolicy();
        val timeoutInSeconds = Optional.ofNullable(registeredService.getIdTokenExpirationPolicy())
            .map(RegisteredServiceOidcIdTokenExpirationPolicy::getTimeToKill)
            .filter(StringUtils::isNotBlank)
            .map(ttl -> Beans.newDuration(ttl).getSeconds())
            .orElseGet(expirationPolicy::getTimeToLive);
        LOGGER.debug("ID token expiration policy set to expire the ID token in [{}]", timeoutInSeconds);

        val expirationDate = NumericDate.now();
        expirationDate.addSeconds(timeoutInSeconds);
        claims.setExpirationTime(expirationDate);

        LOGGER.debug("Calculated ID token expiration claim to be [{}]", expirationDate);
    }

    protected void buildAuthenticationContextClassRef(final JwtClaims claims,
                                                      final Authentication authentication) {
        val mfa = getConfigurationContext().getCasProperties().getAuthn().getMfa();
        val oidc = getConfigurationContext().getCasProperties().getAuthn().getOidc();

        val attributes = authentication.getAttributes();
        val mappedAcrValues = org.springframework.util.StringUtils.commaDelimitedListToSet(mfa.getCore().getAuthenticationContextAttribute())
            .stream()
            .map(attribute -> {
                if (attributes.containsKey(attribute)) {
                    val acrValues = CollectionUtils.toCollection(attributes.get(attribute));
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
                    return acrMapped;
                }
                return null;
            })
            .filter(Objects::nonNull)
            .toList();

        if (!mappedAcrValues.isEmpty()) {
            FunctionUtils.doIf(mappedAcrValues.size() == 1,
                    __ -> claims.setStringClaim(OidcConstants.ACR, mappedAcrValues.getFirst()),
                    __ -> claims.setStringListClaim(OidcConstants.ACR, mappedAcrValues))
                .accept(mappedAcrValues);
        }
    }

    private Principal buildPrincipalForAttributeFilter(final OAuth20AccessToken accessToken,
                                                       final RegisteredService registeredService) throws Throwable {
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
        getConfigurationContext().getIdTokenClaimCollectors()
            .forEach(collector -> collector.conclude(claims));
    }

    private boolean isClaimSupportedForRelease(final String claimName, final RegisteredService registeredService) {
        val mapper = getConfigurationContext().getAttributeToScopeClaimMapper();
        val mappedClaim = mapper.toMappedClaimName(claimName, registeredService);
        val oidc = getConfigurationContext().getCasProperties().getAuthn().getOidc();
        val claims = oidc.getDiscovery().getClaims();
        LOGGER.trace("Checking if any of [{}] are specified in the list of discovery claims [{}]", ImmutableSet.of(claimName, mappedClaim), claims);
        return claims.contains(claimName) || claims.contains(mappedClaim) || isClaimDefinitionSupportedForRelease(mappedClaim);
    }

    private boolean isClaimDefinitionSupportedForRelease(final String claimName) {
        val oidc = getConfigurationContext().getCasProperties().getAuthn().getOidc();
        val claims = oidc.getDiscovery().getClaims();
        val definitionName = getConfigurationContext().getAttributeDefinitionStore()
            .locateAttributeDefinitionByName(claimName)
            .filter(OidcAttributeDefinition.class::isInstance)
            .map(AttributeDefinition::getKey)
            .orElse(claimName);
        LOGGER.trace("Checking if attribute definition [{}] is specified in the list of discovery claims [{}]", definitionName, claims);
        return claims.contains(definitionName);
    }

    protected void handleMappedClaimOrDefault(final String claimName,
                                              final RegisteredService registeredService,
                                              final Principal principal,
                                              final JwtClaims claims,
                                              final Object defaultValue) {
        val mapper = getConfigurationContext().getAttributeToScopeClaimMapper();
        val collectionValues = mapper.mapClaim(claimName, registeredService, principal, defaultValue);
        val collectors = getConfigurationContext().getIdTokenClaimCollectors();
        collectors.forEach(collector -> collector.collect(claims, claimName, collectionValues));
    }

    protected String getJwtId(final OAuth20AccessToken ticket) {
        val oAuthCallbackUrl = getConfigurationContext().getCasProperties().getServer().getPrefix()
            + OAuth20Constants.BASE_OAUTH20_URL + '/'
            + OAuth20Constants.CALLBACK_AUTHORIZE_URL_DEFINITION;
        var jwtId = ticket.isStateless() ? ticket.getId() : ticket.getTicketGrantingTicket().getId();
        if (ticket instanceof final TicketGrantingTicket tgt) {
            val streamServices = new LinkedHashMap<String, Service>();
            val services = tgt.getServices();
            streamServices.putAll(services);
            streamServices.putAll(tgt.getProxyGrantingTickets());
            jwtId = streamServices
                .entrySet()
                .stream()
                .filter(e -> {
                    val service = getConfigurationContext().getServicesManager().findServiceBy(e.getValue());
                    return service != null && service.getServiceId().equals(oAuthCallbackUrl);
                })
                .findFirst()
                .map(Map.Entry::getKey)
                .orElseGet(ticket::getId);
        }
        return jwtId;
    }

    protected void generateAccessTokenHash(final OAuth20AccessToken accessToken,
                                           final OidcRegisteredService registeredService,
                                           final JwtClaims claims) throws Throwable {
        val oidcIssuer = getConfigurationContext().getIssuerService().determineIssuer(Optional.of(registeredService));
        val cipher = OAuth20JwtAccessTokenEncoder.toEncodableCipher(getConfigurationContext().getAccessTokenJwtBuilder(),
            registeredService, accessToken, accessToken.getService(), oidcIssuer,
            getConfigurationContext().getCasProperties());
        val encodedAccessToken = cipher.encode(accessToken.getId());
        val jsonWebKey = getConfigurationContext().getIdTokenSigningAndEncryptionService()
            .getJsonWebKeySigningKey(Optional.of(registeredService));

        val alg = getConfigurationContext().getIdTokenSigningAndEncryptionService()
            .getJsonWebKeySigningAlgorithm(registeredService, jsonWebKey);
        val hash = OAuth20AccessTokenAtHashGenerator.builder()
            .encodedAccessToken(encodedAccessToken)
            .algorithm(alg)
            .registeredService(registeredService)
            .build()
            .generate();
        claims.setClaim(OidcConstants.CLAIM_AT_HASH, hash);
    }

    protected Set<Object> buildAuthenticationMethods(final Authentication authentication) {
        val allAttributes = new HashMap<>(authentication.getAttributes());
        allAttributes.putAll(authentication.getPrincipal().getAttributes());
        return Stream.of(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS, AuthenticationManager.AUTHENTICATION_METHOD_ATTRIBUTE)
            .filter(allAttributes::containsKey)
            .map(name -> CollectionUtils.toCollection(allAttributes.get(name)))
            .findFirst()
            .orElseGet(Set::of);
    }
}

