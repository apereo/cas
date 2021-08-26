package org.apereo.cas.support.oauth.services;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.Ordered;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OAuth20AuthenticationServiceSelectionStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuth")
public class OAuth20AuthenticationServiceSelectionStrategyTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier("oauth20AuthenticationRequestServiceSelectionStrategy")
    private AuthenticationServiceSelectionStrategy strategy;

    @Test
    public void verifyNullService() {
        assertNotNull(strategy.resolveServiceFrom(mock(Service.class)));
    }

    @Test
    public void verifyGrantType() {
        val request = new MockHttpServletRequest();
        request.addHeader("X-" + CasProtocolConstants.PARAMETER_SERVICE, RegisteredServiceTestUtils.CONST_TEST_URL2);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, new MockHttpServletResponse()));
        val service = strategy.resolveServiceFrom(RegisteredServiceTestUtils.getService("https://example.org?"
            + OAuth20Constants.CLIENT_ID + '=' + CLIENT_ID + '&'
            + OAuth20Constants.GRANT_TYPE + '=' + OAuth20GrantTypes.CLIENT_CREDENTIALS.getType()));
        assertNotNull(service);
        assertTrue(service.getAttributes().containsKey(OAuth20Constants.CLIENT_ID));
        assertTrue(service.getAttributes().containsKey(OAuth20Constants.GRANT_TYPE));
        assertEquals(Ordered.HIGHEST_PRECEDENCE, strategy.getOrder());
    }

    @Test
    public void verifyJwtRequest() {
        val claims = new JWTClaimsSet.Builder().subject("cas")
            .claim("scope", new String[]{"profile"})
            .claim("redirect_uri", REDIRECT_URI)
            .claim("grant_type", OAuth20GrantTypes.CLIENT_CREDENTIALS.getType())
            .claim("client_id", CLIENT_ID)
            .build();
        val jwt = new PlainJWT(claims);
        val jwtRequest = jwt.serialize();

        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REQUEST, jwtRequest);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, new MockHttpServletResponse()));
        val service = strategy.resolveServiceFrom(RegisteredServiceTestUtils.getService("https://example.org?"
            + OAuth20Constants.REQUEST + '=' + jwtRequest));

        assertNotNull(service);
        assertTrue(service.getAttributes().containsKey(OAuth20Constants.CLIENT_ID));
    }

    @Test
    public void verifyBadRequest() {
        val request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, new MockHttpServletResponse()));
        val service = strategy.resolveServiceFrom(RegisteredServiceTestUtils.getService("https://example.org"));
        assertNotNull(service);
        assertTrue(service.getAttributes().isEmpty());
    }
}
