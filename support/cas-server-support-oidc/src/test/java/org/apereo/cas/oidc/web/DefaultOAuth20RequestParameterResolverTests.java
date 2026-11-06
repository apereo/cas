package org.apereo.cas.oidc.web;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyCacheKey;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyUsage;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.web.DefaultOAuth20RequestParameterResolver;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.jwt.JsonWebTokenSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import lombok.val;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultOAuth20RequestParameterResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("OAuth")
class DefaultOAuth20RequestParameterResolverTests extends AbstractOidcTests {

    private static JWTClaimsSet getRequestObjectClaims() {
        return new JWTClaimsSet.Builder().subject("cas")
            .claim("scope", new String[]{"openid", "profile"})
            .claim("response", "code")
            .claim("client_id", List.of("client1", "client2"))
            .build();
    }

    private static MockHttpServletRequest getUnsignedJwtRequest() {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REQUEST, new PlainJWT(getRequestObjectClaims()).serialize());
        return request;
    }

    private MockHttpServletRequest getJwtRequest() {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REQUEST, oidcAccessTokenJwtBuilder.getDefaultTokenCipherExecutor().encode(getRequestObjectClaims().toString()));
        return request;
    }

    @Test
    void verifyRequestObjectUnpackedOncePerRequest() throws Throwable {
        val request = getUnsignedJwtRequest();
        val claims = PlainJWT.parse(request.getParameter(OAuth20Constants.REQUEST)).getJWTClaimsSet();
        val jwtBuilder = mock(JwtBuilder.class);
        when(jwtBuilder.getServicesManager()).thenReturn(servicesManager);
        when(jwtBuilder.unpack(any(Optional.class), anyString())).thenReturn(claims);
        val resolver = new DefaultOAuth20RequestParameterResolver(jwtBuilder);

        assertTrue(resolver.resolveRequestParameter(request, OAuth20Constants.SCOPE, List.class).orElseThrow().contains("openid"));
        assertEquals("code", resolver.resolveRequestParameter(request, "response").orElseThrow());
        verify(jwtBuilder, times(1)).unpack(any(Optional.class), anyString());

        resolver.resolveRequestParameter(getUnsignedJwtRequest(), "response");
        verify(jwtBuilder, times(2)).unpack(any(Optional.class), anyString());
    }

    /**
     * A request object is read once verified; an unsigned one, whose algorithm is {@code none}, is refused and none of
     * its parameters are used.
     */
    @Test
    void verifyJwtWithoutClientId() {
        val request = getJwtRequest();
        val scope = oauthRequestParameterResolver.resolveRequestParameter(request, OAuth20Constants.SCOPE, List.class);
        assertFalse(scope.isEmpty());
        assertTrue(scope.get().contains("openid"));
        assertTrue(scope.get().contains("profile"));

        val unsigned = getUnsignedJwtRequest();
        assertTrue(oauthRequestParameterResolver.resolveRequestParameter(unsigned, OAuth20Constants.SCOPE, List.class).isEmpty());
        assertFalse(oauthRequestParameterResolver.isRequestObjectVerified(unsigned));
    }

    /**
     * A request object that names other clients than the {@code client_id} parameter is not used (OpenID Connect Core
     * 1.0, section 6.1): the parameters come from the request itself.
     */
    @Test
    void verifyJwtWithClientId() throws Exception {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REQUEST, signWithServiceKey(registeredService, getRequestObjectClaims()));
        request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());

        val clientIds = oauthRequestParameterResolver.resolveRequestParameter(request, OAuth20Constants.CLIENT_ID, List.class);
        assertEquals(List.of(registeredService.getClientId()), clientIds.orElseThrow());
        assertTrue(oauthRequestParameterResolver.resolveRequestParameter(request, OAuth20Constants.SCOPE, List.class).isEmpty());
        assertFalse(oauthRequestParameterResolver.isRequestObjectVerified(request));
    }

    @Test
    void verifyParameterIsOnQueryString() {
        val request = new MockHttpServletRequest();
        request.setQueryString("client_id=myid&client_secret=mysecret");
        request.setParameter("client_id", "myid");
        request.setParameter("client_secret", "mysecret");
        assertTrue(oauthRequestParameterResolver.isParameterOnQueryString(request, OAuth20Constants.CLIENT_SECRET));
    }

    @Test
    void verifyQueryParameterAsNumber() {
        val request = new MockHttpServletRequest();
        request.setParameter("expiration", "10");
        request.setParameter("rate", "12.4365");
        assertTrue(oauthRequestParameterResolver.resolveRequestParameter(request, "expiration", Integer.class).isPresent());
        assertTrue(oauthRequestParameterResolver.resolveRequestParameter(request, "expiration", Long.class).isPresent());
        assertTrue(oauthRequestParameterResolver.resolveRequestParameter(request, "rate", Double.class).isPresent());
    }

    @Test
    void verifyScopesCanBeExtracted() {
        val request = new MockHttpServletRequest();
        request.setParameter("scope", "openid profile email");
        request.setParameter("keyword", "hello world");
        assertEquals(3, oauthRequestParameterResolver.resolveRequestScopes(request).size());
        assertEquals(2, oauthRequestParameterResolver.resolveRequestParameters(request, "keyword").size());
    }

    @Test
    void verifyParameterIsNotOnQueryString() {
        val request = new MockHttpServletRequest();
        request.setQueryString("client_id=myid");
        request.setParameter("client_id", "myid");
        request.setParameter("client_secret", "mysecret");
        assertFalse(oauthRequestParameterResolver.isParameterOnQueryString(request, OAuth20Constants.CLIENT_SECRET));
    }

    private String signWithServiceKey(final OidcRegisteredService service, final JWTClaimsSet claims) throws Exception {
        val serviceJsonWebKeys = oidcServiceJsonWebKeystoreCache.get(new OidcJsonWebKeyCacheKey(service, OidcJsonWebKeyUsage.SIGNING));
        val jsonWebKey = (PublicJsonWebKey) serviceJsonWebKeys.orElseThrow().getJsonWebKeys()
            .stream()
            .filter(key -> !OidcJsonWebKeyUsage.ENCRYPTION.is(key))
            .findFirst()
            .orElseThrow();
        return JsonWebTokenSigner
            .builder()
            .key(jsonWebKey.getPrivateKey())
            .algorithm(jsonWebKey.getAlgorithm())
            .build()
            .sign(JwtClaims.parse(claims.toString()));
    }

    @Test
    void verifyRequestAsSignedJwt() throws Exception {
        val service = getOidcRegisteredService("whatever");
        val claims = new JWTClaimsSet.Builder()
            .subject("cas")
            .claim("scope", new String[]{"openid", "profile"})
            .claim("aud", "https://server.example.com")
            .claim("client_notification_token", UUID.randomUUID().toString())
            .claim("client_id", List.of(service.getClientId()))
            .build();
        servicesManager.save(service);

        val signedJwt = signWithServiceKey(service, claims);

        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REQUEST, signedJwt);
        request.addParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        val scopes = oauthRequestParameterResolver.resolveRequestParameter(request, OAuth20Constants.SCOPE, List.class).orElseThrow();
        assertEquals(2, Objects.requireNonNull(scopes).size());
        val aud = oauthRequestParameterResolver.resolveRequestParameter(request, "aud", List.class).orElseThrow();
        assertEquals(1, Objects.requireNonNull(aud).size());
    }
}
