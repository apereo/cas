package org.apereo.cas.oidc.federation.validator;

import module java.base;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.federation.AbstractOidcOpenIdProviderFederationTests;
import org.apereo.cas.oidc.federation.chain.OidcFederationDefaultTrustChainResolver;
import org.apereo.cas.oidc.federation.chain.OidcFederationTrustChainResolver;
import org.apereo.cas.oidc.federation.registration.OidcFederationAutomaticClientRegistration;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyCacheKey;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyUsage;
import org.apereo.cas.services.DefaultRegisteredServiceExpirationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20AuthorizationRequestValidator;
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
import lombok.val;
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

        for (val claims : List.of(wrongAudience, extraAudience, withSubject, expired, wrongIssuer, wrongClient)) {
            assertRefused(mockMvc.perform(authorize(clientId).param(OAuth20Constants.REQUEST, signRequestObject(claims, RELYING_PARTY_KEY))).andReturn(), null);
        }
        assertRefused(mockMvc.perform(authorize(clientId).param(OAuth20Constants.REQUEST, signRequestObject(withoutJti, RELYING_PARTY_KEY))).andReturn(),
            "Request object has no jti");
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
     * A sent trust chain may register a relying party or replace its registration, but not with an entity configuration
     * older than the one the registration was built from: the trust chains are resolved instead.
     */
    @Test
    void verifySentTrustChainCannotRollBackRegistration() throws Throwable {
        val clientId = "https://rp-sent-" + UUID.randomUUID() + ".example.org";
        assertEquals("sent-service", clientRegistration.register(clientId, List.of("100")).getName());
        val rolledBack = clientRegistration.reevaluate(clientId, _ -> true, List.of("50"));
        assertEquals("fetched-service", rolledBack.getName());
        assertEquals("200", rolledBack.getProperties()
            .get(OidcFederationDefaultTrustChainResolver.OPENIDFEDERATION_ENTITY_CONFIGURATION_ISSUED_AT).getValue(String.class));

        val otherClientId = "https://rp-sent-" + UUID.randomUUID() + ".example.org";
        assertEquals("sent-service", clientRegistration.register(otherClientId, List.of("100")).getName());
        val newer = clientRegistration.reevaluate(otherClientId, _ -> true, List.of("150"));
        assertEquals("sent-service", newer.getName());
        assertEquals("150", newer.getProperties()
            .get(OidcFederationDefaultTrustChainResolver.OPENIDFEDERATION_ENTITY_CONFIGURATION_ISSUED_AT).getValue(String.class));
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
        return get(AUTHORIZE_URL)
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

        @Override
        public Optional<OidcRegisteredService> resolveTrustChains(final String clientId, final List<String> trustChain) {
            if (!trustChain.isEmpty()) {
                SENT_TRUST_CHAINS.put(clientId, trustChain);
                if (clientId.contains("rp-sent-")) {
                    return Optional.of(buildAutomaticallyRegisteredService(clientId, "sent-service", trustChain.getFirst()));
                }
            }
            return resolveTrustChains(clientId);
        }

        @Override
        public Optional<OidcRegisteredService> resolveTrustChains(final String clientId) {
            RESOLUTIONS.computeIfAbsent(clientId, _ -> new AtomicInteger()).incrementAndGet();
            if (clientId.contains("rp-missing-")) {
                return Optional.of(buildOidcRegisteredService(clientId, "resolved-service"));
            }
            if (clientId.contains("rp-refresh-")) {
                return Optional.of(buildOidcRegisteredService(clientId, "new-service"));
            }
            if (clientId.contains("rp-sent-")) {
                return Optional.of(buildAutomaticallyRegisteredService(clientId, "fetched-service", "200"));
            }
            if (clientId.contains("rp-signed-") || clientId.contains("rp-rotated-")) {
                val service = buildOidcRegisteredService(clientId, "signed-service");
                service.setClientSecret(null);
                service.setServiceId(Pattern.quote(REDIRECT_URI));
                service.setScopes(new HashSet<>(List.of(OidcConstants.StandardScopes.OPENID.getScope())));
                service.setTokenEndpointAuthenticationMethod("private_key_jwt");
                service.setJwks(new JWKSet(PUBLISHED_KEYS.getOrDefault(clientId, RELYING_PARTY_KEY).toPublicJWK()).toString());
                service.setExpirationPolicy(new DefaultRegisteredServiceExpirationPolicy(true,
                    ZonedDateTime.now(Clock.systemUTC()).plusDays(1)));
                service.getProperties().put(OidcFederationDefaultTrustChainResolver.TEMPORARY_OPENIDFEDERATION_SERVICE,
                    new DefaultRegisteredServiceProperty(Boolean.TRUE.toString()));
                return Optional.of(service);
            }
            return Optional.empty();
        }
    }
}
