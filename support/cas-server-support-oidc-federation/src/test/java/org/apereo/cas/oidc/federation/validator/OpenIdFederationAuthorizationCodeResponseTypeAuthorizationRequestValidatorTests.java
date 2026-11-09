package org.apereo.cas.oidc.federation.validator;

import module java.base;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.federation.AbstractOidcOpenIdProviderFederationTests;
import org.apereo.cas.oidc.federation.chain.OidcFederationDefaultTrustChainResolver;
import org.apereo.cas.oidc.federation.chain.OidcFederationResolutionUnavailableException;
import org.apereo.cas.oidc.federation.chain.OidcFederationTrustChainResolver;
import org.apereo.cas.oidc.federation.registration.OidcFederationAutomaticClientRegistration;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyCacheKey;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyUsage;
import org.apereo.cas.services.DefaultRegisteredServiceExpirationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.OidcSubjectTypes;
import org.apereo.cas.services.PairwiseOidcRegisteredServiceUsernameAttributeProvider;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20AuthorizationRequestValidator;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.DigestUtils;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityID;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatement;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatementClaimsSet;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityType;
import com.nimbusds.openid.connect.sdk.federation.trust.TrustChain;
import lombok.val;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jose4j.jwk.JsonWebKeySet;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.servlet.HandlerInterceptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * This is {@link OpenIdFederationAuthorizationCodeResponseTypeAuthorizationRequestValidatorTests}.
 * <p>
 * The {@code OIDCWeb} category runs test methods concurrently against one shared
 * {@link ServicesManager}, so no test here may clear the registry wholesale: doing so deletes the
 * services its siblings have just saved. Each test works with a client id of its own instead, which
 * is all the assertions actually depend on.
 *
 * @author Jerome LELEU
 * @since 8.1.0
 */
@Tag("OIDCWeb")
@Import(OpenIdFederationAuthorizationCodeResponseTypeAuthorizationRequestValidatorTests.OpenIdFederationAuthorizationCodeResponseTypeAuthorizationRequestValidatorTestConfiguration.class)
class OpenIdFederationAuthorizationCodeResponseTypeAuthorizationRequestValidatorTests extends AbstractOidcOpenIdProviderFederationTests {
    private static final String AUTHORIZE_URL = "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.AUTHORIZE_URL;

    private static final String PUSHED_AUTHORIZE_URL = "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.PUSHED_AUTHORIZE_URL;

    private static final String OP_ENTITY_ID = "https://sso.example.org/cas/oidc";

    private static final String REDIRECT_URI = "https://rp.example.org/callback";

    private static final RSAKey RELYING_PARTY_KEY = generateKey("rp-key");

    @Autowired
    @Qualifier("openIdFederationAuthorizationCodeResponseTypeAuthorizationRequestValidator")
    private OAuth20AuthorizationRequestValidator authorizationRequestValidator;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("oidcFederationAutomaticRegistrationInterceptor")
    private HandlerInterceptor automaticRegistrationInterceptor;

    @Autowired
    @Qualifier(OidcFederationAutomaticClientRegistration.BEAN_NAME)
    private OidcFederationAutomaticClientRegistration clientRegistration;

    @Autowired
    @Qualifier("oidcDefaultJsonWebKeystoreCache")
    private LoadingCache<OidcJsonWebKeyCacheKey, JsonWebKeySet> oidcDefaultJsonWebKeystoreCache;

    @Autowired
    @Qualifier(OidcIssuerService.BEAN_NAME)
    private OidcIssuerService oidcIssuerService;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;

    @Test
    void verifyMissingServiceGetsResolvedAndSaved() throws Throwable {
        val clientId = "https://rp-missing-" + UUID.randomUUID() + ".example.org";

        val registeredService = resolveByClientId(clientId);
        assertNotNull(registeredService);
        assertEquals(clientId, registeredService.getClientId());
        assertEquals("resolved-service", registeredService.getName());
        assertTrue(servicesManager.getAllServicesOfType(OAuthRegisteredService.class)
            .stream()
            .anyMatch(service -> clientId.equals(service.getClientId())));
    }

    @Test
    void verifyMissingServiceWithBlankClientIdIsIgnored() throws Throwable {
        val registeredService = resolveByClientId("   ");
        assertNull(registeredService);
        assertFalse(servicesManager.getAllServicesOfType(OAuthRegisteredService.class)
            .stream()
            .anyMatch(service -> StringUtils.isBlank(service.getClientId())));
    }

    @Test
    void verifyMissingServiceWhenResolverReturnsNoServiceIsIgnored() throws Throwable {
        val clientId = "https://rp-unresolved-" + UUID.randomUUID() + ".example.org";
        val registeredService = resolveByClientId(clientId);
        assertNull(registeredService);
        assertFalse(servicesManager.getAllServicesOfType(OAuthRegisteredService.class)
            .stream()
            .anyMatch(service -> clientId.equals(service.getClientId())));
    }

    @Test
    void verifyTemporaryServiceNearExpirationGetsRefreshed() throws Throwable {
        val clientId = "https://rp-refresh-" + UUID.randomUUID() + ".example.org";
        val existingService = buildOidcRegisteredService(clientId, "old-service");
        existingService.setExpirationPolicy(new DefaultRegisteredServiceExpirationPolicy(true,
            ZonedDateTime.now(Clock.systemUTC()).plusMinutes(2)));
        existingService.getProperties().put(OidcFederationDefaultTrustChainResolver.TEMPORARY_OPENIDFEDERATION_SERVICE,
            new DefaultRegisteredServiceProperty(Boolean.TRUE.toString()));
        servicesManager.save(existingService);

        val registeredService = resolveByClientId(clientId);
        assertNotNull(registeredService);
        assertEquals("new-service", registeredService.getName());
        assertEquals(existingService.getId(), registeredService.getId());
        assertEquals(1, servicesManager.getAllServicesOfType(OAuthRegisteredService.class)
            .stream()
            .filter(service -> clientId.equals(service.getClientId()))
            .count());
    }

    @Test
    void verifyConcurrentFirstContactRegistersOnce() throws Throwable {
        val clientId = "https://rp-missing-" + UUID.randomUUID() + ".example.org";
        try (val executor = Executors.newVirtualThreadPerTaskExecutor()) {
            val registrations = IntStream.range(0, 8)
                .mapToObj(_ -> executor.submit(() -> resolveByClientId(clientId)))
                .toList();
            for (val registration : registrations) {
                assertNotNull(registration.get());
            }
        }
        assertEquals(1, servicesManager.getAllServicesOfType(OAuthRegisteredService.class)
            .stream()
            .filter(service -> clientId.equals(service.getClientId()))
            .count());
    }

    @Test
    void verifyMissingServiceWithNonFederatedEntityIdIsIgnored() throws Throwable {
        val clientId = "rp-non-federated-" + UUID.randomUUID();
        val registeredService = resolveByClientId(clientId);
        assertNull(registeredService);
        assertFalse(servicesManager.getAllServicesOfType(OAuthRegisteredService.class)
            .stream()
            .anyMatch(service -> clientId.equals(service.getClientId())));
    }

    @Test
    void verifyTemporaryServiceWithFarExpirationIsNotRefreshed() throws Throwable {
        val clientId = "https://rp-refresh-" + UUID.randomUUID() + ".example.org";
        val existingService = buildOidcRegisteredService(clientId, "old-service");
        existingService.setExpirationPolicy(new DefaultRegisteredServiceExpirationPolicy(true,
            ZonedDateTime.now(Clock.systemUTC()).plusMinutes(30)));
        existingService.getProperties().put(OidcFederationDefaultTrustChainResolver.TEMPORARY_OPENIDFEDERATION_SERVICE,
            new DefaultRegisteredServiceProperty(Boolean.TRUE.toString()));
        servicesManager.save(existingService);
        val registeredService = resolveByClientId(clientId);
        assertNotNull(registeredService);
        assertEquals("old-service", registeredService.getName());
    }

    @Test
    void verifyServiceWithoutTemporaryFlagIsNotRefreshed() throws Throwable {
        val clientId = "https://rp-refresh-" + UUID.randomUUID() + ".example.org";
        val existingService = buildOidcRegisteredService(clientId, "old-service");
        existingService.setExpirationPolicy(new DefaultRegisteredServiceExpirationPolicy(true,
            ZonedDateTime.now(Clock.systemUTC()).plusMinutes(2)));
        servicesManager.save(existingService);
        val registeredService = resolveByClientId(clientId);
        assertNotNull(registeredService);
        assertEquals("old-service", registeredService.getName());
    }

    @Test
    void verifyUnresolvedEntityIsNotResolvedAgain() throws Throwable {
        val clientId = "https://rp-unresolved-" + UUID.randomUUID() + ".example.org";
        assertNull(resolveByClientId(clientId));
        assertNull(resolveByClientId(clientId));
        assertEquals(1, TestOidcFederationTrustChainResolver.RESOLUTIONS.get(clientId).get());
    }

    @Test
    void verifyFailedRefreshKeepsRegistrationWithoutRetrying() throws Throwable {
        val clientId = "https://rp-stale-" + UUID.randomUUID() + ".example.org";
        val existingService = buildOidcRegisteredService(clientId, "old-service");
        existingService.setExpirationPolicy(new DefaultRegisteredServiceExpirationPolicy(true,
            ZonedDateTime.now(Clock.systemUTC()).plusMinutes(2)));
        existingService.getProperties().put(OidcFederationDefaultTrustChainResolver.TEMPORARY_OPENIDFEDERATION_SERVICE,
            new DefaultRegisteredServiceProperty(Boolean.TRUE.toString()));
        servicesManager.save(existingService);

        assertEquals("old-service", resolveByClientId(clientId).getName());
        assertEquals("old-service", resolveByClientId(clientId).getName());
        assertEquals(1, TestOidcFederationTrustChainResolver.RESOLUTIONS.get(clientId).get());
    }

    /**
     * A relying party that asks for pairwise subject identifiers is given them, with the pairwise subject salt of CAS,
     * and is not registered when no salt is defined.
     */
    @Test
    void verifyPairwiseRelyingPartyUsesPairwiseSubjectSalt() throws Throwable {
        assertNull(resolveByClientId("https://rp-pairwise-" + UUID.randomUUID() + ".example.org"));

        val registration = new OidcFederationAutomaticClientRegistration(servicesManager,
            new TestOidcFederationTrustChainResolver(), Duration.ofMinutes(5), "pairwise-salt");
        val registeredService = registration.register("https://rp-pairwise-" + UUID.randomUUID() + ".example.org");
        assertNotNull(registeredService);
        assertInstanceOf(PairwiseOidcRegisteredServiceUsernameAttributeProvider.class, registeredService.getUsernameAttributeProvider());
        assertFalse(PairwiseOidcRegisteredServiceUsernameAttributeProvider.hasSalt(registeredService), "The salt of CAS is not stored");
        val stored = servicesManager.findServiceBy(registeredService.getId());
        assertInstanceOf(PairwiseOidcRegisteredServiceUsernameAttributeProvider.class, stored.getUsernameAttributeProvider());
        assertFalse(PairwiseOidcRegisteredServiceUsernameAttributeProvider.hasSalt(stored));
    }

    /**
     * A relying party that cannot be registered now is neither remembered as unresolved nor answered as an unknown
     * client: its authorization request is answered as temporarily unavailable, and it is resolved again later.
     */
    @Test
    void verifyUnavailableRegistrationIsNotAnUnknownClient() throws Throwable {
        val clientId = "https://rp-unavailable-" + UUID.randomUUID() + ".example.org";
        assertThrows(OidcFederationResolutionUnavailableException.class, () -> clientRegistration.register(clientId));
        assertNull(resolveByClientId(clientId));
        val result = mockMvc.perform(authorize(clientId)).andReturn();
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE.value(), result.getResponse().getStatus());
        assertTrue(result.getResponse().getContentAsString().contains("temporarily_unavailable"));
        assertTrue(TestOidcFederationTrustChainResolver.RESOLUTIONS.get(clientId).get() >= 3);
    }

    /**
     * A sent trust chain that cannot be verified now leaves the relying party to be resolved through its trust anchors.
     */
    @Test
    void verifyUnavailableSentTrustChainFallsBackToResolution() throws Throwable {
        val clientId = "https://rp-sentunavailable-" + UUID.randomUUID() + ".example.org";
        val registeredService = clientRegistration.register(clientId, List.of("sent-statement"), _ -> true);
        assertNotNull(registeredService);
        assertEquals("resolved-service", registeredService.getName());
    }

    /**
     * A sent trust chain whose relying party declares keys inline is verified only when the request is signed with them,
     * so that a replayed chain does not make CAS fetch what its verification needs.
     */
    @Test
    void verifySentTrustChainIsVerifiedOnlyWhenSignedWithDeclaredKeys() throws Throwable {
        val clientId = "https://rp-sentdeclared-" + UUID.randomUUID() + ".example.org";
        val leafKey = generateKey("leaf-key");
        val trustAnchorKey = generateKey("ta-key");
        val now = Instant.now(Clock.systemUTC());
        val leafClaims = new EntityStatementClaimsSet(new EntityID(clientId), new EntityID(clientId),
            Date.from(now), Date.from(now.plusSeconds(3600)), new JWKSet(leafKey.toPublicJWK()));
        leafClaims.setMetadata(EntityType.OPENID_RELYING_PARTY,
            new JSONObject(Map.of("jwks", new JWKSet(RELYING_PARTY_KEY.toPublicJWK()).toJSONObject())));
        val superiorClaims = new EntityStatementClaimsSet(new EntityID("https://ta.example.org"), new EntityID(clientId),
            Date.from(now), Date.from(now.plusSeconds(3600)), new JWKSet(leafKey.toPublicJWK()));
        val trustChain = List.of(EntityStatement.sign(leafClaims, leafKey).getSignedStatement().serialize(),
            EntityStatement.sign(superiorClaims, trustAnchorKey).getSignedStatement().serialize());

        assertNull(clientRegistration.register(clientId, trustChain, _ -> false));
        assertFalse(TestOidcFederationTrustChainResolver.SENT_TRUST_CHAINS.containsKey(clientId));
        val otherClientId = "https://rp-sentdeclared-" + UUID.randomUUID() + ".example.org";
        assertNull(clientRegistration.register(otherClientId, trustChain, _ -> true));
        assertTrue(TestOidcFederationTrustChainResolver.SENT_TRUST_CHAINS.containsKey(otherClientId));

        val withoutKeysId = "https://rp-sentdeclared-" + UUID.randomUUID() + ".example.org";
        val withoutKeysClaims = new EntityStatementClaimsSet(new EntityID(withoutKeysId), new EntityID(withoutKeysId),
            Date.from(now), Date.from(now.plusSeconds(3600)), new JWKSet(leafKey.toPublicJWK()));
        withoutKeysClaims.setMetadata(EntityType.OPENID_RELYING_PARTY, new JSONObject(Map.of("jwks_uri", "https://rp.example.org/jwks")));
        val withoutKeysSuperiorClaims = new EntityStatementClaimsSet(new EntityID("https://ta.example.org"), new EntityID(withoutKeysId),
            Date.from(now), Date.from(now.plusSeconds(3600)), new JWKSet(leafKey.toPublicJWK()));
        val withoutKeys = List.of(EntityStatement.sign(withoutKeysClaims, leafKey).getSignedStatement().serialize(),
            EntityStatement.sign(withoutKeysSuperiorClaims, trustAnchorKey).getSignedStatement().serialize());
        assertEquals(1, TrustChain.parseSerialized(withoutKeys).getSuperiorStatements().size());
        assertNull(clientRegistration.register(withoutKeysId, withoutKeys, _ -> false));
        assertTrue(TestOidcFederationTrustChainResolver.SENT_TRUST_CHAINS.containsKey(withoutKeysId),
            "A chain that declares no keys inline is verified, which needs its keys");
        val unreadableId = "https://rp-sentdeclared-" + UUID.randomUUID() + ".example.org";
        assertNull(clientRegistration.register(unreadableId, List.of("unreadable-statement"), _ -> false));
        assertTrue(TestOidcFederationTrustChainResolver.SENT_TRUST_CHAINS.containsKey(unreadableId),
            "A chain that cannot be read is verified, which refuses it");
    }

    @Test
    void verifyHttpEntityIdIsIgnored() throws Throwable {
        val clientId = "http://rp-missing-" + UUID.randomUUID() + ".example.org";
        assertNull(resolveByClientId(clientId));
    }

    @Test
    void verifyAuthorizationWithoutRequestObjectIsRefused() throws Throwable {
        val clientId = newSignedClientId();
        val result = mockMvc.perform(authorize(clientId)).andReturn();
        assertRefused(result, "Automatic registration requires a signed request object or a pushed authorization request");
        assertTrue(servicesManager.getAllServicesOfType(OAuthRegisteredService.class)
            .stream()
            .anyMatch(service -> clientId.equals(service.getClientId())));
    }

    /**
     * The authorization endpoints also answer outside the OpenID Connect base endpoint, and at encoded paths that decode
     * to them; the proof is required there too.
     */
    @Test
    void verifyAuthorizationOutsideBaseEndpointIsRefused() throws Throwable {
        for (val uri : List.of("/cas/" + OidcConstants.AUTHORIZE_URL, "/cas/custom/" + OidcConstants.AUTHORIZE_URL,
            "/cas/" + OidcConstants.BASE_OIDC_URL + "/oidcAuthoriz%65", "/cas/" + OidcConstants.BASE_OIDC_URL + "/oidcAuthorize;x=y")) {
            val result = mockMvc.perform(authorize(uri, newSignedClientId())).andReturn();
            assertRefused(result, "Automatic registration requires a signed request object or a pushed authorization request");
        }
        val clientId = newSignedClientId();
        val pushed = post("/cas/" + OidcConstants.PUSHED_AUTHORIZE_URL)
            .param(OAuth20Constants.CLIENT_ID, clientId)
            .param(OAuth20Constants.REDIRECT_URI, REDIRECT_URI)
            .param(OAuth20Constants.RESPONSE_TYPE, OAuth20Constants.CODE)
            .with(withHttpRequestProcessor());
        clientAssertionParameters(clientId, List.of(OP_ENTITY_ID, "https://other.example.org")).forEach(pushed::param);
        val result = mockMvc.perform(pushed).andReturn();
        assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
        val body = result.getResponse().getContentAsString();
        assertTrue(body.contains("Client assertion aud must only be the OP Entity Identifier"), body);
        assertTrue(servicesManager.getAllServicesOfType(OAuthRegisteredService.class)
            .stream()
            .anyMatch(service -> clientId.equals(service.getClientId())));
    }

    @Test
    void verifySignedRequestObjectIsAcceptedAgainButJtiIsNotReused() throws Throwable {
        val clientId = newSignedClientId();
        val jwtId = UUID.randomUUID().toString();
        val requestObject = signRequestObject(requestObjectClaims(clientId, jwtId).build(), RELYING_PARTY_KEY);
        assertTrue(preHandle(AUTHORIZE_URL, clientId, Map.of(OAuth20Constants.REQUEST, requestObject)).getLeft());
        assertTrue(preHandle(AUTHORIZE_URL, clientId, Map.of(OAuth20Constants.REQUEST, requestObject)).getLeft());

        val reused = signRequestObject(requestObjectClaims(clientId, jwtId).claim(OAuth20Constants.STATE, "other").build(), RELYING_PARTY_KEY);
        val result = preHandle(AUTHORIZE_URL, clientId, Map.of(OAuth20Constants.REQUEST, reused));
        assertFalse(result.getLeft());
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getRight().getStatus());
        assertTrue(result.getRight().getContentAsString().contains("Request object jti has already been used"));
        assertRefused(mockMvc.perform(authorize(clientId).param(OAuth20Constants.REQUEST, reused)).andReturn(),
            "Request object jti has already been used");
    }

    /**
     * A used request object is remembered for as long as it could be accepted, its expiration plus the allowed clock
     * skew, and at least for the lifetime of transient session tickets, so that it cannot be replayed once its record
     * would otherwise have expired.
     */
    @Test
    void verifyUsedRequestObjectIsRememberedWhileItCanBeAccepted() throws Throwable {
        val clientId = newSignedClientId();
        val shortLived = UUID.randomUUID().toString();
        assertTrue(preHandle(AUTHORIZE_URL, clientId, Map.of(OAuth20Constants.REQUEST,
            signRequestObject(requestObjectClaims(clientId, shortLived).build(), RELYING_PARTY_KEY))).getLeft());
        assertTrue(usedRequestObject(clientId, shortLived).getExpirationPolicy().getTimeToLive() >= 90);

        val longLived = UUID.randomUUID().toString();
        assertTrue(preHandle(AUTHORIZE_URL, clientId, Map.of(OAuth20Constants.REQUEST, signRequestObject(requestObjectClaims(clientId, longLived)
            .expirationTime(Date.from(Instant.now().plusSeconds(540))).build(), RELYING_PARTY_KEY))).getLeft());
        val timeToLive = usedRequestObject(clientId, longLived).getExpirationPolicy().getTimeToLive();
        assertTrue(timeToLive > 540 && timeToLive <= 600, String.valueOf(timeToLive));
    }

    @Test
    void verifyInvalidRequestObjectsAreRefused() throws Throwable {
        val clientId = newSignedClientId();
        val wrongAudience = requestObjectClaims(clientId, UUID.randomUUID().toString()).audience("https://other.example.org").build();
        val extraAudience = requestObjectClaims(clientId, UUID.randomUUID().toString()).audience(List.of(OP_ENTITY_ID, "https://other.example.org")).build();
        val withSubject = requestObjectClaims(clientId, UUID.randomUUID().toString()).subject(clientId).build();
        val expired = requestObjectClaims(clientId, UUID.randomUUID().toString()).expirationTime(Date.from(Instant.now().minusSeconds(600))).build();
        val wrongIssuer = requestObjectClaims(clientId, UUID.randomUUID().toString()).issuer("https://other.example.org").build();
        val wrongClient = requestObjectClaims(clientId, UUID.randomUUID().toString()).claim(OAuth20Constants.CLIENT_ID, "https://other.example.org").build();
        val withoutJti = requestObjectClaims(clientId, UUID.randomUUID().toString()).jwtID(null).build();
        val longLived = requestObjectClaims(clientId, UUID.randomUUID().toString())
            .expirationTime(Date.from(Instant.now().plusSeconds(3600))).build();
        val longLivedSinceIssued = requestObjectClaims(clientId, UUID.randomUUID().toString())
            .issueTime(Date.from(Instant.now().minusSeconds(3600))).build();

        for (val claims : List.of(wrongAudience, extraAudience, withSubject, expired, wrongIssuer, wrongClient)) {
            assertRefused(mockMvc.perform(authorize(clientId).param(OAuth20Constants.REQUEST, signRequestObject(claims, RELYING_PARTY_KEY))).andReturn(), null);
        }
        assertRefused(mockMvc.perform(authorize(clientId).param(OAuth20Constants.REQUEST, signRequestObject(withoutJti, RELYING_PARTY_KEY))).andReturn(),
            "Request object has no jti");
        for (val claims : List.of(longLived, longLivedSinceIssued)) {
            assertRefused(mockMvc.perform(authorize(clientId).param(OAuth20Constants.REQUEST, signRequestObject(claims, RELYING_PARTY_KEY))).andReturn(),
                "Request object must not be valid for more than 10 minutes");
        }
        val unknownKey = signRequestObject(requestObjectClaims(clientId, UUID.randomUUID().toString()).build(), generateKey(RELYING_PARTY_KEY.getKeyID()));
        assertRefused(mockMvc.perform(authorize(clientId).param(OAuth20Constants.REQUEST, unknownKey)).andReturn(),
            "Request object signature or claims are invalid");
        val unsigned = new PlainJWT(requestObjectClaims(clientId, UUID.randomUUID().toString()).build()).serialize();
        assertRefused(mockMvc.perform(authorize(clientId).param(OAuth20Constants.REQUEST, unsigned)).andReturn(),
            "Request object signature or claims are invalid");
        val encryptedToUnknownKey = encryptRequestObject(signRequestObject(
            requestObjectClaims(clientId, UUID.randomUUID().toString()).build(), RELYING_PARTY_KEY), generateKey("unknown-key"));
        assertRefused(mockMvc.perform(authorize(clientId).param(OAuth20Constants.REQUEST, encryptedToUnknownKey)).andReturn(),
            "Request object is invalid");
        val encryptedWithoutSignature = encryptRequestObject(new PlainJWT(
            requestObjectClaims(clientId, UUID.randomUUID().toString()).build()).serialize(), resolveEncryptionKey());
        assertRefused(mockMvc.perform(authorize(clientId).param(OAuth20Constants.REQUEST, encryptedWithoutSignature)).andReturn(),
            "Request object signature or claims are invalid");
    }

    /**
     * The client whose keys must be proven (OpenID Federation 1.0, section 12.1.1) is the client the authorization
     * request is for: the {@code client_id} of the request object when the request carries no {@code client_id}
     * parameter, and on the OAuth authorization endpoint as well as on the OpenID Connect one.
     */
    @Test
    void verifyClientOfRequestObjectMustProveItsKeys() throws Throwable {
        val clientId = newSignedClientId();
        val unsigned = new PlainJWT(requestObjectClaims(clientId, UUID.randomUUID().toString()).build()).serialize();
        assertRefused(mockMvc.perform(get(AUTHORIZE_URL)
            .param(OAuth20Constants.REQUEST, unsigned)
            .param(OAuth20Constants.REDIRECT_URI, REDIRECT_URI)
            .param(OAuth20Constants.RESPONSE_TYPE, OAuth20Constants.CODE)
            .with(withHttpRequestProcessor())).andReturn(), "Request object signature or claims are invalid");

        val signed = signRequestObject(requestObjectClaims(clientId, UUID.randomUUID().toString()).build(), RELYING_PARTY_KEY);
        val request = new MockHttpServletRequest("GET", AUTHORIZE_URL);
        request.addParameter(OAuth20Constants.REQUEST, signed);
        assertTrue(automaticRegistrationInterceptor.preHandle(request, new MockHttpServletResponse(), new Object()));

        val oauthAuthorizeUrl = "/cas" + OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.AUTHORIZE_URL;
        assertRefused(mockMvc.perform(get(oauthAuthorizeUrl)
            .param(OAuth20Constants.CLIENT_ID, clientId)
            .param(OAuth20Constants.REDIRECT_URI, REDIRECT_URI)
            .param(OAuth20Constants.RESPONSE_TYPE, OAuth20Constants.CODE)
            .with(withHttpRequestProcessor())).andReturn(), "Automatic registration requires a signed request object or a pushed authorization request");
        assertRefused(mockMvc.perform(get(oauthAuthorizeUrl)
            .param(OAuth20Constants.REQUEST, unsigned)
            .param(OAuth20Constants.REDIRECT_URI, REDIRECT_URI)
            .param(OAuth20Constants.RESPONSE_TYPE, OAuth20Constants.CODE)
            .with(withHttpRequestProcessor())).andReturn(), "Request object signature or claims are invalid");
    }

    /**
     * A request object signed by the relying party and encrypted to the OP (OpenID Connect Core 1.0, section 6.3.1) is
     * decrypted, and its nested JWT checked like a signed request object; replay is tracked on the encrypted object.
     */
    @Test
    void verifyEncryptedRequestObjectIsAccepted() throws Throwable {
        val clientId = newSignedClientId();
        val jwtId = UUID.randomUUID().toString();
        val requestObject = encryptRequestObject(signRequestObject(requestObjectClaims(clientId, jwtId).build(), RELYING_PARTY_KEY),
            resolveEncryptionKey());
        assertTrue(preHandle(AUTHORIZE_URL, clientId, Map.of(OAuth20Constants.REQUEST, requestObject)).getLeft());
        assertTrue(preHandle(AUTHORIZE_URL, clientId, Map.of(OAuth20Constants.REQUEST, requestObject)).getLeft());
        val reused = encryptRequestObject(signRequestObject(requestObjectClaims(clientId, jwtId).build(), RELYING_PARTY_KEY),
            resolveEncryptionKey());
        assertRefused(mockMvc.perform(authorize(clientId).param(OAuth20Constants.REQUEST, reused)).andReturn(),
            "Request object jti has already been used");
    }

    @Test
    void verifyPushedAuthorizationRequestIsLeftToClientAuthentication() throws Throwable {
        val clientId = newSignedClientId();
        assertTrue(preHandle(PUSHED_AUTHORIZE_URL, clientId, Map.of()).getLeft());
        assertTrue(servicesManager.getAllServicesOfType(OAuthRegisteredService.class)
            .stream()
            .anyMatch(service -> clientId.equals(service.getClientId())));
        assertTrue(preHandle(AUTHORIZE_URL, clientId, Map.of(OidcConstants.REQUEST_URI, "urn:ietf:params:oauth:request_uri:pushed")).getLeft());
        assertTrue(preHandle("/cas/" + OidcConstants.BASE_OIDC_URL + "/" + OidcConstants.ACCESS_TOKEN_URL, clientId, Map.of()).getLeft());
        assertTrue(preHandle(AUTHORIZE_URL, "http://rp-signed-" + UUID.randomUUID() + ".example.org", Map.of()).getLeft());
    }

    /**
     * At the pushed authorization endpoint, the client assertion of an automatically registered relying party must be
     * addressed only to the OP Entity Identifier (OpenID Federation 1.0, section 12.1.1.2).
     */
    @Test
    void verifyPushedClientAssertionIsAddressedOnlyToTheProvider() throws Throwable {
        val clientId = newSignedClientId();
        for (val audience : List.of(List.of(OP_ENTITY_ID, "https://other.example.org"), List.of(OP_ENTITY_ID + "/token"))) {
            val refused = preHandle(PUSHED_AUTHORIZE_URL, clientId, clientAssertionParameters(clientId, audience));
            assertFalse(refused.getLeft(), audience::toString);
            assertEquals(HttpStatus.UNAUTHORIZED.value(), refused.getRight().getStatus());
            assertTrue(refused.getRight().getContentAsString().contains("invalid_client"));
        }
        assertTrue(preHandle(PUSHED_AUTHORIZE_URL, clientId, clientAssertionParameters(clientId, List.of(OP_ENTITY_ID))).getLeft());
    }

    private static Map<String, String> clientAssertionParameters(final String clientId, final List<String> audience) throws Exception {
        val claims = new JWTClaimsSet.Builder()
            .issuer(clientId)
            .subject(clientId)
            .audience(audience)
            .jwtID(UUID.randomUUID().toString())
            .issueTime(new Date())
            .expirationTime(Date.from(Instant.now().plusSeconds(60)))
            .build();
        val assertion = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(RELYING_PARTY_KEY.getKeyID()).build(), claims);
        assertion.sign(new RSASSASigner(RELYING_PARTY_KEY));
        return Map.of(OAuth20Constants.CLIENT_ASSERTION_TYPE, OAuth20Constants.CLIENT_ASSERTION_TYPE_JWT_BEARER,
            OAuth20Constants.CLIENT_ASSERTION, assertion.serialize());
    }

    /**
     * A relying party that rolls its keys over (OpenID Federation 1.0, section 12.3) is resolved again once when its
     * request object is signed with a key its registration does not hold, and not again for a while after that.
     */
    @Test
    void verifyRolledOverRequestObjectKeyIsResolvedAgain() throws Throwable {
        val clientId = "https://rp-rotated-" + UUID.randomUUID() + ".example.org";
        TestOidcFederationTrustChainResolver.PUBLISHED_KEYS.put(clientId, RELYING_PARTY_KEY);
        assertTrue(preHandle(AUTHORIZE_URL, clientId, Map.of(OAuth20Constants.REQUEST,
            signRequestObject(requestObjectClaims(clientId, UUID.randomUUID().toString()).build(), RELYING_PARTY_KEY))).getLeft());
        assertTrue(preHandle(AUTHORIZE_URL, clientId, Map.of(OAuth20Constants.REQUEST,
            signRequestObject(requestObjectClaims(clientId, UUID.randomUUID().toString()).build(), RELYING_PARTY_KEY))).getLeft());
        assertEquals(1, TestOidcFederationTrustChainResolver.RESOLUTIONS.get(clientId).get());

        val rolledOverKey = generateKey("rp-key-next");
        TestOidcFederationTrustChainResolver.PUBLISHED_KEYS.put(clientId, rolledOverKey);
        assertTrue(preHandle(AUTHORIZE_URL, clientId, Map.of(OAuth20Constants.REQUEST,
            signRequestObject(requestObjectClaims(clientId, UUID.randomUUID().toString()).build(), rolledOverKey))).getLeft());
        assertEquals(2, TestOidcFederationTrustChainResolver.RESOLUTIONS.get(clientId).get());

        val unpublishedKey = signRequestObject(requestObjectClaims(clientId, UUID.randomUUID().toString()).build(), generateKey("rp-key-unknown"));
        assertFalse(preHandle(AUTHORIZE_URL, clientId, Map.of(OAuth20Constants.REQUEST, unpublishedKey)).getLeft());
        assertEquals(2, TestOidcFederationTrustChainResolver.RESOLUTIONS.get(clientId).get());
    }

    @Test
    void verifyRolledOverClientAssertionKeyIsResolvedAgain() throws Throwable {
        val clientId = "https://rp-rotated-" + UUID.randomUUID() + ".example.org";
        TestOidcFederationTrustChainResolver.PUBLISHED_KEYS.put(clientId, RELYING_PARTY_KEY);
        assertTrue(preHandle(PUSHED_AUTHORIZE_URL, clientId, Map.of()).getLeft());

        val rolledOverKey = generateKey("rp-key-next");
        TestOidcFederationTrustChainResolver.PUBLISHED_KEYS.put(clientId, rolledOverKey);
        val claims = new JWTClaimsSet.Builder()
            .issuer(clientId)
            .subject(clientId)
            .audience(OP_ENTITY_ID)
            .jwtID(UUID.randomUUID().toString())
            .expirationTime(Date.from(Instant.now().plusSeconds(60)))
            .build();
        val assertion = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rolledOverKey.getKeyID()).build(), claims);
        assertion.sign(new RSASSASigner(rolledOverKey));
        val request = new MockHttpServletRequest("POST", "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.ACCESS_TOKEN_URL);
        request.addParameter(OAuth20Constants.CLIENT_ASSERTION_TYPE, OAuth20Constants.CLIENT_ASSERTION_TYPE_JWT_BEARER);
        request.addParameter(OAuth20Constants.CLIENT_ASSERTION, assertion.serialize());
        assertTrue(automaticRegistrationInterceptor.preHandle(request, new MockHttpServletResponse(), new Object()));

        assertEquals(2, TestOidcFederationTrustChainResolver.RESOLUTIONS.get(clientId).get());
        assertTrue(servicesManager.getAllServicesOfType(OidcRegisteredService.class)
            .stream()
            .anyMatch(service -> clientId.equals(service.getClientId()) && service.getJwks().contains(rolledOverKey.getKeyID())));
    }

    /**
     * The genuine trust chain a relying party published before rolling its keys over, which anyone can fetch, does not
     * keep its replaced keys when someone else replays it in a request the relying party did not sign: the registration
     * is resolved instead, and the relying party's requests signed with its new key are accepted.
     */
    @Test
    void verifyReplayedTrustChainDoesNotBlockKeyRollover() throws Throwable {
        val clientId = "https://rp-rotated-" + UUID.randomUUID() + ".example.org";
        TestOidcFederationTrustChainResolver.PUBLISHED_KEYS.put(clientId, RELYING_PARTY_KEY);
        assertTrue(preHandle(AUTHORIZE_URL, clientId, Map.of(OAuth20Constants.REQUEST,
            signRequestObject(requestObjectClaims(clientId, UUID.randomUUID().toString()).build(), RELYING_PARTY_KEY))).getLeft());

        val rolledOverKey = generateKey("rp-key-next");
        TestOidcFederationTrustChainResolver.PUBLISHED_KEYS.put(clientId, rolledOverKey);
        val header = new JWSHeader.Builder(JWSAlgorithm.RS256)
            .keyID("attacker")
            .type(new JOSEObjectType("oauth-authz-req+jwt"))
            .customParam("trust_chain", List.of(TestOidcFederationTrustChainResolver.REPLAYED_TRUST_CHAIN))
            .build();
        val replayed = new SignedJWT(header, requestObjectClaims(clientId, UUID.randomUUID().toString()).build());
        replayed.sign(new RSASSASigner(generateKey("attacker")));
        assertFalse(preHandle(AUTHORIZE_URL, clientId, Map.of(OAuth20Constants.REQUEST, replayed.serialize())).getLeft());
        assertEquals(2, TestOidcFederationTrustChainResolver.RESOLUTIONS.get(clientId).get());

        assertTrue(preHandle(AUTHORIZE_URL, clientId, Map.of(OAuth20Constants.REQUEST,
            signRequestObject(requestObjectClaims(clientId, UUID.randomUUID().toString()).build(), rolledOverKey))).getLeft());
        assertEquals(2, TestOidcFederationTrustChainResolver.RESOLUTIONS.get(clientId).get());
    }

    /**
     * The trust chain a relying party sends in the {@code trust_chain} header of its request object is handed to the
     * registration (OpenID Federation 1.0, section 12.1.1.1).
     */
    @Test
    void verifyTrustChainHeaderIsUsedForRegistration() throws Throwable {
        val clientId = newSignedClientId();
        val header = new JWSHeader.Builder(JWSAlgorithm.RS256)
            .keyID(RELYING_PARTY_KEY.getKeyID())
            .type(new JOSEObjectType("oauth-authz-req+jwt"))
            .customParam("trust_chain", List.of("first-statement", "second-statement"))
            .build();
        val requestObject = new SignedJWT(header, requestObjectClaims(clientId, UUID.randomUUID().toString()).build());
        requestObject.sign(new RSASSASigner(RELYING_PARTY_KEY));
        assertTrue(preHandle(AUTHORIZE_URL, clientId, Map.of(OAuth20Constants.REQUEST, requestObject.serialize())).getLeft());
        assertEquals(List.of("first-statement", "second-statement"), TestOidcFederationTrustChainResolver.SENT_TRUST_CHAINS.get(clientId));
    }

    /**
     * A sent trust chain may register a relying party or replace its registration, but only with an entity
     * configuration not issued before the one the registration was built from, and only when the request that carries
     * it is signed with the keys it resolves to: statements are public, so an older chain, or any chain replayed with a
     * request the relying party did not sign, must not keep keys it has replaced. The trust chains are resolved
     * instead. A chain as new as the registration, signed with its keys, is used.
     */
    @Test
    void verifySentTrustChainCannotRollBackRegistration() throws Throwable {
        val clientId = "https://rp-sent-" + UUID.randomUUID() + ".example.org";
        assertEquals("sent-service", clientRegistration.register(clientId, List.of("100"), _ -> true).getName());
        val rolledBack = clientRegistration.reevaluate(clientId, _ -> true, List.of("50"));
        assertEquals("fetched-service", rolledBack.getName());
        assertEquals("200", issuedAt(rolledBack));

        val sameAgeClientId = "https://rp-sent-" + UUID.randomUUID() + ".example.org";
        val registered = clientRegistration.register(sameAgeClientId, List.of("100"), _ -> true);
        assertEquals("sent-service", registered.getName());
        val sameAge = clientRegistration.reevaluate(sameAgeClientId, service -> service.getId() == registered.getId(), List.of("100"));
        assertEquals("sent-service", sameAge.getName());
        assertEquals("100", issuedAt(sameAge));

        val unsignedClientId = "https://rp-sent-" + UUID.randomUUID() + ".example.org";
        assertEquals("fetched-service", clientRegistration.register(unsignedClientId, List.of("100"), _ -> false).getName());
        val unsigned = clientRegistration.reevaluate(unsignedClientId, _ -> true, List.of("300"));
        assertEquals("fetched-service", unsigned.getName());
        assertEquals("200", issuedAt(unsigned));

        val otherClientId = "https://rp-sent-" + UUID.randomUUID() + ".example.org";
        assertEquals("sent-service", clientRegistration.register(otherClientId, List.of("100"), _ -> true).getName());
        val newer = clientRegistration.reevaluate(otherClientId, service -> !"150".equals(issuedAt(service)), List.of("150"));
        assertEquals("sent-service", newer.getName());
        assertEquals("150", issuedAt(newer));
    }

    private static String issuedAt(final OAuthRegisteredService service) {
        return service.getProperties()
            .get(OidcFederationDefaultTrustChainResolver.OPENIDFEDERATION_ENTITY_CONFIGURATION_ISSUED_AT).getValue(String.class);
    }

    private TransientSessionTicket usedRequestObject(final String clientId, final String jwtId) {
        val ticketId = TransientSessionTicketFactory.normalizeTicketId("OidcFederationRequestObject:" + clientId + ':' + DigestUtils.sha256(jwtId));
        return Objects.requireNonNull(ticketRegistry.getTicket(ticketId, TransientSessionTicket.class));
    }

    private Pair<Boolean, MockHttpServletResponse> preHandle(final String uri, final String clientId,
                                                             final Map<String, String> parameters)
            throws Exception {
        val request = new MockHttpServletRequest("GET", uri);
        request.addParameter(OAuth20Constants.CLIENT_ID, clientId);
        request.addParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        request.addParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20Constants.CODE);
        parameters.forEach(request::addParameter);
        val response = new MockHttpServletResponse();
        return Pair.of(automaticRegistrationInterceptor.preHandle(request, response, new Object()), response);
    }

    private MockHttpServletRequestBuilder authorize(final String clientId) {
        return authorize(AUTHORIZE_URL, clientId);
    }

    private MockHttpServletRequestBuilder authorize(final String uri, final String clientId) {
        return get(URI.create(uri))
            .param(OAuth20Constants.CLIENT_ID, clientId)
            .param(OAuth20Constants.REDIRECT_URI, REDIRECT_URI)
            .param(OAuth20Constants.RESPONSE_TYPE, OAuth20Constants.CODE)
            .param(OAuth20Constants.SCOPE, OidcConstants.StandardScopes.OPENID.getScope())
            .with(withHttpRequestProcessor());
    }

    private static void assertRefused(final MvcResult result, final String description) throws Exception {
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
        val body = result.getResponse().getContentAsString();
        assertTrue(body.contains(OAuth20Constants.INVALID_REQUEST));
        if (description != null) {
            assertTrue(body.contains(description), body);
        }
    }

    private static String newSignedClientId() {
        return "https://rp-signed-" + UUID.randomUUID() + ".example.org";
    }

    private static JWTClaimsSet.Builder requestObjectClaims(final String clientId, final String jwtId) {
        return new JWTClaimsSet.Builder()
            .issuer(clientId)
            .audience(OP_ENTITY_ID)
            .jwtID(jwtId)
            .issueTime(new Date())
            .expirationTime(Date.from(Instant.now().plusSeconds(60)))
            .claim(OAuth20Constants.CLIENT_ID, clientId)
            .claim(OAuth20Constants.RESPONSE_TYPE, OAuth20Constants.CODE)
            .claim(OAuth20Constants.REDIRECT_URI, REDIRECT_URI)
            .claim(OAuth20Constants.SCOPE, OidcConstants.StandardScopes.OPENID.getScope());
    }

    private static String signRequestObject(final JWTClaimsSet claims, final RSAKey key) throws Exception {
        val header = new JWSHeader.Builder(JWSAlgorithm.RS256)
            .keyID(key.getKeyID())
            .type(new JOSEObjectType("oauth-authz-req+jwt"))
            .build();
        val jwt = new SignedJWT(header, claims);
        jwt.sign(new RSASSASigner(key));
        return jwt.serialize();
    }

    private RSAKey resolveEncryptionKey() throws Exception {
        val issuer = oidcIssuerService.determineIssuer(Optional.empty());
        val keystore = Objects.requireNonNull(oidcDefaultJsonWebKeystoreCache.get(new OidcJsonWebKeyCacheKey(issuer, OidcJsonWebKeyUsage.ENCRYPTION)));
        return RSAKey.parse(keystore.getJsonWebKeys().getFirst().toJson());
    }

    private static String encryptRequestObject(final String nestedJwt, final RSAKey key) throws Exception {
        val jwe = new JWEObject(new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM)
            .keyID(key.getKeyID())
            .contentType("JWT")
            .build(), new Payload(nestedJwt));
        jwe.encrypt(new RSAEncrypter(key));
        return jwe.serialize();
    }

    private static RSAKey generateKey(final String keyId) {
        try {
            return new RSAKeyGenerator(2048).keyID(keyId).generate();
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private OAuthRegisteredService resolveByClientId(final String clientId) {
        val validator = (OpenIdFederationAuthorizationCodeResponseTypeAuthorizationRequestValidator) authorizationRequestValidator;
        return validator.getRegisteredServiceByClientId(clientId);
    }

    private static OidcRegisteredService buildOidcRegisteredService(final String clientId, final String name) {
        val service = new OidcRegisteredService();
        service.setName(name);
        service.setClientId(clientId);
        service.setClientSecret(UUID.randomUUID().toString());
        service.setServiceId("https://example.org/.*");
        service.assignIdIfNecessary();
        return service;
    }

    private static OidcRegisteredService buildAutomaticallyRegisteredService(final String clientId, final String name, final String issuedAt) {
        val service = buildOidcRegisteredService(clientId, name);
        service.setExpirationPolicy(new DefaultRegisteredServiceExpirationPolicy(true, ZonedDateTime.now(Clock.systemUTC()).plusDays(1)));
        service.getProperties().put(OidcFederationDefaultTrustChainResolver.TEMPORARY_OPENIDFEDERATION_SERVICE,
            new DefaultRegisteredServiceProperty(Boolean.TRUE.toString()));
        service.getProperties().put(OidcFederationDefaultTrustChainResolver.OPENIDFEDERATION_ENTITY_CONFIGURATION_ISSUED_AT,
            new DefaultRegisteredServiceProperty(issuedAt));
        return service;
    }

    @TestConfiguration(value = "OpenIdFederationAuthorizationCodeResponseTypeAuthorizationRequestValidatorTestConfiguration", proxyBeanMethods = false)
    static class OpenIdFederationAuthorizationCodeResponseTypeAuthorizationRequestValidatorTestConfiguration {
        @Bean(name = OidcFederationTrustChainResolver.BEAN_NAME)
        public OidcFederationTrustChainResolver oidcFederationTrustChainResolver() {
            return new TestOidcFederationTrustChainResolver();
        }
    }

    static class TestOidcFederationTrustChainResolver implements OidcFederationTrustChainResolver {
        static final Map<String, AtomicInteger> RESOLUTIONS = new ConcurrentHashMap<>();

        static final Map<String, RSAKey> PUBLISHED_KEYS = new ConcurrentHashMap<>();

        static final Map<String, List<String>> SENT_TRUST_CHAINS = new ConcurrentHashMap<>();

        static final String REPLAYED_TRUST_CHAIN = "replayed-statement";

        @Override
        public Optional<OidcRegisteredService> resolveSentTrustChain(final String clientId, final List<String> trustChain) {
            if (!trustChain.isEmpty()) {
                SENT_TRUST_CHAINS.put(clientId, trustChain);
                if (clientId.contains("rp-sentunavailable-")) {
                    throw new OidcFederationResolutionUnavailableException("Trust anchor of the sent chain cannot be reached now");
                }
                if (clientId.contains("rp-sent-")) {
                    return Optional.of(buildAutomaticallyRegisteredService(clientId, "sent-service", trustChain.getFirst()));
                }
                if (clientId.contains("rp-rotated-") && trustChain.contains(REPLAYED_TRUST_CHAIN)) {
                    val service = buildSignedService(clientId, RELYING_PARTY_KEY);
                    service.getProperties().put(OidcFederationDefaultTrustChainResolver.OPENIDFEDERATION_ENTITY_CONFIGURATION_ISSUED_AT,
                        new DefaultRegisteredServiceProperty(String.valueOf(Long.MAX_VALUE)));
                    return Optional.of(service);
                }
            }
            return Optional.empty();
        }

        @Override
        public Optional<OidcRegisteredService> resolveTrustChains(final String clientId) {
            RESOLUTIONS.computeIfAbsent(clientId, _ -> new AtomicInteger()).incrementAndGet();
            if (clientId.contains("rp-missing-")) {
                return Optional.of(buildOidcRegisteredService(clientId, "resolved-service"));
            }
            if (clientId.contains("rp-pairwise-")) {
                val service = buildOidcRegisteredService(clientId, "pairwise-service");
                service.setSubjectType(OidcSubjectTypes.PAIRWISE.getType());
                return Optional.of(service);
            }
            if (clientId.contains("rp-refresh-")) {
                return Optional.of(buildOidcRegisteredService(clientId, "new-service"));
            }
            if (clientId.contains("rp-sent-")) {
                return Optional.of(buildAutomaticallyRegisteredService(clientId, "fetched-service", "200"));
            }
            if (clientId.contains("rp-signed-") || clientId.contains("rp-rotated-")) {
                return Optional.of(buildSignedService(clientId, PUBLISHED_KEYS.getOrDefault(clientId, RELYING_PARTY_KEY)));
            }
            if (clientId.contains("rp-unavailable-")) {
                throw new OidcFederationResolutionUnavailableException("Trust anchor cannot be reached now");
            }
            if (clientId.contains("rp-sentunavailable-")) {
                return Optional.of(buildOidcRegisteredService(clientId, "resolved-service"));
            }
            return Optional.empty();
        }

        private static OidcRegisteredService buildSignedService(final String clientId, final RSAKey key) {
            val service = buildOidcRegisteredService(clientId, "signed-service");
            service.setClientSecret(null);
            service.setServiceId(Pattern.quote(REDIRECT_URI));
            service.setScopes(new HashSet<>(List.of(OidcConstants.StandardScopes.OPENID.getScope())));
            service.setTokenEndpointAuthenticationMethod("private_key_jwt");
            service.setJwks(new JWKSet(key.toPublicJWK()).toString());
            service.setExpirationPolicy(new DefaultRegisteredServiceExpirationPolicy(true,
                ZonedDateTime.now(Clock.systemUTC()).plusDays(1)));
            service.getProperties().put(OidcFederationDefaultTrustChainResolver.TEMPORARY_OPENIDFEDERATION_SERVICE,
                new DefaultRegisteredServiceProperty(Boolean.TRUE.toString()));
            return service;
        }
    }
}
