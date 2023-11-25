package org.apereo.cas.support.oauth.web;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.support.oauth.OAuth20Constants;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.jee.context.JEEContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultOAuth20RequestParameterResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("OAuth")
class DefaultOAuth20RequestParameterResolverTests extends AbstractOAuth20Tests {

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
    void verifyPlainJwtWithoutClientId() throws Throwable {
        val request = getJwtRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);
        val scope = oauthRequestParameterResolver.resolveRequestParameter(context, OAuth20Constants.SCOPE, List.class);
        assertFalse(scope.isEmpty());
        assertTrue(scope.get().contains("openid"));
        assertTrue(scope.get().contains("profile"));
    }

    @Test
    void verifyPlainJwtWithClientId() throws Throwable {
        val request = getJwtRequest();

        val registeredService = getRegisteredService(UUID.randomUUID().toString(), "secret");
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
    void verifyParameterIsNotOnQueryString() {
        val request = new MockHttpServletRequest();
        request.setQueryString("client_id=myid");
        request.setParameter("client_id", "myid");
        request.setParameter("client_secret", "mysecret");
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);
        assertFalse(oauthRequestParameterResolver.isParameterOnQueryString(context, OAuth20Constants.CLIENT_SECRET));
    }
}
