package org.apereo.cas.oidc.token;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.attribute.AttributeDefinition;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.claims.OidcAttributeDefinition;
import org.apereo.cas.oidc.claims.OidcScopeFreeAttributeReleasePolicy;
import org.apereo.cas.services.OidcBackchannelTokenDeliveryModes;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceChainingAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredServiceOidcIdTokenExpirationPolicy;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenHashGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.idtoken.BaseIdTokenGeneratorService;
import org.apereo.cas.ticket.idtoken.IdTokenGenerationContext;
import org.apereo.cas.ticket.idtoken.OidcIdToken;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.Assert;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
    public OidcIdToken generate(final IdTokenGenerationContext context) throws Throwable {
        Assert.isAssignable(OidcRegisteredService.class, context.getRegisteredService().getClass(),
            "Registered service instance is not registered as an OpenID Connect application");
        if (!context.getAccessToken().getScopes().contains(OidcConstants.StandardScopes.OPENID.getScope())) {
            LOGGER.warn("Authentication request does not include the [{}] scope. "
                    + "Including this scope is a MUST for OpenID Connect and CAS will not produce an ID token without this scope.",
                OidcConstants.StandardScopes.OPENID.getScope());
            return null;
        }
        val claims = buildJwtClaims(context);

        var deviceSecret = StringUtils.EMPTY;
        if (context.getGrantType() == OAuth20GrantTypes.AUTHORIZATION_CODE
            && context.getAccessToken().getScopes().contains(OidcConstants.StandardScopes.DEVICE_SSO.getScope())) {
            deviceSecret = getConfigurationContext().getDeviceSecretGenerator().generate();
            claims.setStringClaim(OidcConstants.DS_HASH, getConfigurationContext().getDeviceSecretGenerator().hash(deviceSecret));
            if (context.getAccessToken().getTicketGrantingTicket() != null) {
                val encoded = (byte[]) getConfigurationContext().getTicketRegistry().getCipherExecutor()
                    .encode(context.getAccessToken().getTicketGrantingTicket().getId().getBytes(StandardCharsets.UTF_8));
                val sessionId = EncodingUtils.encodeUrlSafeBase64(encoded);
                claims.setStringClaim(OidcConstants.CLAIM_SESSION_REF, sessionId);
            }
        }

        val finalIdToken = encodeAndFinalizeToken(claims, context);
        return new OidcIdToken(finalIdToken, claims, deviceSecret);
    }

    @SuppressWarnings("LongFloatConversion")
    protected JwtClaims buildJwtClaims(final IdTokenGenerationContext context) throws Throwable {
        val accessToken = context.getAccessToken();
        LOGGER.trace("Attempting to produce claims for the id token [{}]", accessToken);
        val authentication = accessToken.getAuthentication();
        val activePrincipal = buildPrincipalForAttributeFilter(accessToken, context.getRegisteredService());
        val principal = getConfigurationContext().getProfileScopeToAttributesFilter()
            .filter(accessToken.getService(),
                activePrincipal, context.getRegisteredService(), accessToken);
        LOGGER.debug("Principal to use to build the ID token is [{}]", principal);

        val oidc = getConfigurationContext().getCasProperties().getAuthn().getOidc();
        val claims = new JwtClaims();

        val jwtId = getJwtId(accessToken);
        LOGGER.debug("Calculated ID token jti claim to be [{}]", jwtId);
        claims.setJwtId(jwtId);

        claims.setClaim(OidcConstants.CLAIM_SESSION_ID, DigestUtils.sha(jwtId));
        val oidcRegisteredService = (OidcRegisteredService) context.getRegisteredService();
        claims.setIssuer(getConfigurationContext().getIssuerService().determineIssuer(Optional.of(oidcRegisteredService)));
        val audience = context.getRegisteredService().getAudience().isEmpty()
            ? List.of(accessToken.getClientId())
            : new ArrayList<>(context.getRegisteredService().getAudience());
        claims.setAudience(audience);
        LOGGER.debug("Calculated ID token aud claim to be [{}]", audience);

        buildExpirationClaim(claims, oidcRegisteredService);

        claims.setIssuedAtToNow();
        claims.setNotBeforeMinutesInThePast(Beans.newDuration(oidc.getCore().getSkew()).toMinutes());
        claims.setSubject(principal.getId());

        buildAuthenticationContextClassRef(claims, authentication);

        val amrValues = buildAuthenticationMethods(authentication);
        if (!amrValues.isEmpty()) {
            LOGGER.debug("ID token amr claim calculated as [{}]", amrValues);
            claims.setStringListClaim(OidcConstants.AMR, amrValues.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
        }

        val attributes = authentication.getAttributes();
        claims.setStringClaim(OAuth20Constants.CLIENT_ID, context.getRegisteredService().getClientId());

        val authTime = accessToken.isStateless() || accessToken.getTicketGrantingTicket() == null
            ? authentication.getAuthenticationDate().toEpochSecond()
            : ((AuthenticationAwareTicket) accessToken.getTicketGrantingTicket()).getAuthentication().getAuthenticationDate().toEpochSecond();
        claims.setClaim(OidcConstants.CLAIM_AUTH_TIME, authTime);

        if (attributes.containsKey(OAuth20Constants.STATE)) {
            setClaim(claims, OAuth20Constants.STATE, attributes.get(OAuth20Constants.STATE).getFirst());
        }
        if (attributes.containsKey(OAuth20Constants.NONCE)) {
            setClaim(claims, OAuth20Constants.NONCE, attributes.get(OAuth20Constants.NONCE).getFirst());
        }
        generateAccessTokenHash(accessToken, oidcRegisteredService, claims);

        val includeClaims = context.getResponseType() != OAuth20ResponseTypes.CODE && context.getGrantType() != OAuth20GrantTypes.AUTHORIZATION_CODE;
        if (includeClaims || includeClaimsInIdTokenForcefully(context)) {
            FunctionUtils.doIf(includeClaimsInIdTokenForcefully(context),
                    __ -> LOGGER.warn("Individual claims requested by OpenID scopes are forced to be included in the ID token. "
                        + "This is a violation of the OpenID Connect specification and a workaround via dedicated CAS configuration. "
                        + "Claims should be requested from the userinfo/profile endpoints in exchange for an access token."))
                .accept(claims);
            collectIdTokenClaims(principal, context.getRegisteredService(), claims);
        } else {
            LOGGER.debug("Per OpenID Connect specification, individual claims requested by OpenID scopes "
                + "such as profile, email, address, etc. are only put "
                + "into the OpenID Connect ID token when the response type is set to id_token.");
        }
        claims.setStringClaim(OidcConstants.TXN, UUID.randomUUID().toString());

        if (context.getGrantType() == OAuth20GrantTypes.CIBA) {
            generateCibaClaims(context, claims);
        }
        return claims;
    }

    private boolean includeClaimsInIdTokenForcefully(final IdTokenGenerationContext context) {
        val oidcService = (OidcRegisteredService) context.getRegisteredService();
        val properties = getConfigurationContext().getCasProperties().getAuthn().getOidc();
        return properties.getIdToken().isIncludeIdTokenClaims() || oidcService.isIncludeIdTokenClaims();
    }

    private void generateCibaClaims(final IdTokenGenerationContext context, final JwtClaims claims) throws Throwable {
        val deliveryMode = OidcBackchannelTokenDeliveryModes.valueOf(
            ((OidcRegisteredService) context.getRegisteredService()).getBackchannelTokenDeliveryMode().toUpperCase(Locale.ENGLISH));
        if (deliveryMode == OidcBackchannelTokenDeliveryModes.PUSH) {
            val requestId = context.getAccessToken().getAuthentication().getSingleValuedAttribute(OidcConstants.AUTH_REQ_ID, String.class);
            claims.setStringClaim(OidcConstants.CLAIM_AUTH_REQ_ID, requestId);

            if (context.getRefreshToken() != null) {
                val jsonWebKey = getConfigurationContext().getIdTokenSigningAndEncryptionService()
                    .getJsonWebKeySigningKey(Optional.of(context.getRegisteredService()));
                val alg = getConfigurationContext().getIdTokenSigningAndEncryptionService()
                    .getJsonWebKeySigningAlgorithm(context.getRegisteredService(), jsonWebKey);
                val hash = OAuth20TokenHashGenerator.builder()
                    .token(context.getRefreshToken().getId())
                    .algorithm(alg)
                    .registeredService(context.getRegisteredService())
                    .build()
                    .generate();
                claims.setClaim(OidcConstants.CLAIM_RT_HASH, hash);
            }
        }
    }

    protected void buildExpirationClaim(final JwtClaims claims, final OidcRegisteredService registeredService) {
        val expirationPolicy = getConfigurationContext().getIdTokenExpirationPolicy().buildTicketExpirationPolicy();
        val timeoutInSeconds = Optional.ofNullable(registeredService.getIdTokenExpirationPolicy())
            .map(RegisteredServiceOidcIdTokenExpirationPolicy::getTimeToKill)
            .filter(StringUtils::isNotBlank)
            .map(ttl -> Beans.newDuration(ttl).toSeconds())
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
            .filter(attributes::containsKey)
            .map(attribute -> {
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
        return claims.contains(claimName)
            || claims.contains(mappedClaim)
            || isClaimDefinitionSupportedForRelease(mappedClaim)
            || isClaimReleasedAllowedByScopeFreePolicy(claimName, registeredService);
    }

    protected boolean isClaimReleasedAllowedByScopeFreePolicy(final String claimName, final RegisteredService registeredService) {
        if (registeredService.getAttributeReleasePolicy() instanceof final OidcScopeFreeAttributeReleasePolicy policy) {
            val allowedAttributes = policy.getAllowedAttributes();
            LOGGER.trace("Checking if claim [{}] is allowed by the scope-free policy [{}]", claimName, allowedAttributes);
            return !policy.claimsMustBeDefinedViaDiscovery() && allowedAttributes.contains(claimName);
        }
        if (registeredService.getAttributeReleasePolicy() instanceof final RegisteredServiceChainingAttributeReleasePolicy chain) {
            return chain
                .getPolicies()
                .stream()
                .filter(OidcScopeFreeAttributeReleasePolicy.class::isInstance)
                .map(OidcScopeFreeAttributeReleasePolicy.class::cast)
                .filter(policy -> !policy.claimsMustBeDefinedViaDiscovery())
                .anyMatch(policy -> policy.getAllowedAttributes().contains(claimName));
        }
        return false;
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
        var jwtId = ticket.getId();
        if (ticket.getTicketGrantingTicket() != null) {
            jwtId = ticket.getTicketGrantingTicket().getId();
        }
        return DigestUtils.sha512(jwtId);
    }

    protected void generateAccessTokenHash(final OAuth20AccessToken accessToken,
                                           final OidcRegisteredService registeredService,
                                           final JwtClaims claims) throws Throwable {
        val oidcIssuer = getConfigurationContext().getIssuerService().determineIssuer(Optional.of(registeredService));
        val cipher = OAuth20JwtAccessTokenEncoder.toEncodableCipher(getConfigurationContext(),
            registeredService, accessToken, oidcIssuer);
        val encodedAccessToken = cipher.encode(accessToken.getId());
        val jsonWebKey = getConfigurationContext().getIdTokenSigningAndEncryptionService()
            .getJsonWebKeySigningKey(Optional.of(registeredService));

        val alg = getConfigurationContext().getIdTokenSigningAndEncryptionService()
            .getJsonWebKeySigningAlgorithm(registeredService, jsonWebKey);
        val hash = OAuth20TokenHashGenerator.builder()
            .token(encodedAccessToken)
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

