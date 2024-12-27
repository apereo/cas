package org.apereo.cas.oidc.web;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyCacheKey;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyUsage;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.util.jwt.JsonWebTokenSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import lombok.val;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.jee.context.JEEContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultOAuth20RequestParameterResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("OAuth")
class DefaultOAuth20RequestParameterResolverTests extends AbstractOidcTests {

    private static MockHttpServletRequest getJwtRequest() {
        val request = new MockHttpServletRequest();
        val claims = new JWTClaimsSet.Builder().subject("cas")
            .claim("scope", new String[]{"openid", "profile"})
            .claim("response", "code")
            .claim("client_id", List.of("client1", "client2"))
            .build();
        val jwt = new PlainJWT(claims);
        val jwtString = jwt.serialize();
        request.removeAllParameters();
        request.addParameter(OAuth20Constants.REQUEST, jwtString);
        return request;
    }

    @Test
    void verifyPlainJwtWithoutClientId() {
        val request = getJwtRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);
        val scope = oauthRequestParameterResolver.resolveRequestParameter(context, OAuth20Constants.SCOPE, List.class);
        assertFalse(scope.isEmpty());
        assertTrue(scope.get().contains("openid"));
        assertTrue(scope.get().contains("profile"));
    }

    @Test
    void verifyPlainJwtWithClientId() {
        val request = getJwtRequest();

        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);
        request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());

        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);
        val scope = oauthRequestParameterResolver.resolveRequestParameter(context, OAuth20Constants.CLIENT_ID, List.class);
        assertFalse(scope.isEmpty());
        assertTrue(scope.get().contains("client1"));
        assertTrue(scope.get().contains("client2"));
    }

    @Test
    void verifyParameterIsOnQueryString() {
        val request = new MockHttpServletRequest();
        request.setQueryString("client_id=myid&client_secret=mysecret");
        request.setParameter("client_id", "myid");
        request.setParameter("client_secret", "mysecret");
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);
        assertTrue(oauthRequestParameterResolver.isParameterOnQueryString(context, OAuth20Constants.CLIENT_SECRET));
    }

    @Test
    void verifyQueryParameterAsNumber() {
        val request = new MockHttpServletRequest();
        request.setParameter("expiration", "10");
        request.setParameter("rate", "12.4365");
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);
        assertTrue(oauthRequestParameterResolver.resolveRequestParameter(context, "expiration", Integer.class).isPresent());
        assertTrue(oauthRequestParameterResolver.resolveRequestParameter(context, "expiration", Long.class).isPresent());
        assertTrue(oauthRequestParameterResolver.resolveRequestParameter(context, "rate", Double.class).isPresent());
    }

    @Test
    void verifyScopesCanBeExtracted() {
        val request = new MockHttpServletRequest();
        request.setParameter("scope", "openid profile email");
        request.setParameter("keyword", "hello world");
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);
        assertEquals(3, oauthRequestParameterResolver.resolveRequestScopes(context).size());
        assertEquals(2, oauthRequestParameterResolver.resolveRequestParameters(context, "keyword").size());
    }

    @Test
    void verifyParameterIsNotOnQueryString() {
        val request = new MockHttpServletRequest();
        request.setQueryString("client_id=myid");
        request.setParameter("client_id", "myid");
        request.setParameter("client_secret", "mysecret");
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);
        assertFalse(oauthRequestParameterResolver.isParameterOnQueryString(context, OAuth20Constants.CLIENT_SECRET));
    }

    @Test
    void verifyRequestAsSignedJwt() throws Exception {
        val service = getOidcRegisteredService("whatever");
        val serviceJsonWebKeys = oidcServiceJsonWebKeystoreCache.get(new OidcJsonWebKeyCacheKey(service, OidcJsonWebKeyUsage.SIGNING));
        val jsonWebKey = (PublicJsonWebKey) serviceJsonWebKeys.get().getJsonWebKeys().getFirst();
        val claims = new JWTClaimsSet.Builder()
            .subject("cas")
            .claim("scope", new String[]{"openid", "profile"})
            .claim("aud", "https://server.example.com")
            .claim("client_notification_token", UUID.randomUUID().toString())
            .claim("client_id", List.of(service.getClientId()))
            .build();
        servicesManager.save(service);

        val signedJwt = JsonWebTokenSigner
            .builder()
            .key(jsonWebKey.getPrivateKey())
            .algorithm(jsonWebKey.getAlgorithm())
            .build()
            .sign(JwtClaims.parse(claims.toString()));

        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REQUEST, signedJwt);
        request.addParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);
        val scopes = oauthRequestParameterResolver.resolveRequestParameter(context, OAuth20Constants.SCOPE, List.class).orElseThrow();
        assertEquals(2, Objects.requireNonNull(scopes).size());
        val aud = oauthRequestParameterResolver.resolveRequestParameter(context, "aud", List.class).orElseThrow();
        assertEquals(1, Objects.requireNonNull(aud).size());
    }
}
