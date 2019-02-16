package org.apereo.cas.support.oauth.web.response.accesstoken.response;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.AbstractOAuth20Tests;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGeneratedResult;
import org.apereo.cas.ticket.accesstoken.DefaultAccessTokenFactory;
import org.apereo.cas.ticket.support.HardTimeoutExpirationPolicy;
import org.apereo.cas.token.JWTBuilder;

import com.nimbusds.jwt.JWTParser;
import lombok.val;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;

import static org.junit.Assert.*;

/**
 * This is {@link OAuth20DefaultAccessTokenResponseGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class OAuth20DefaultAccessTokenResponseGeneratorTests extends AbstractOAuth20Tests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void initialize() {
        clearAllServices();
    }


    @Test
    public void verifyAccessTokenAsDefault() throws Exception {
        val registeredService = getRegisteredService("example", "secret", new LinkedHashSet<>());
        registeredService.setJwtAccessToken(false);
        servicesManager.save(registeredService);

        val mv = getModelAndView(registeredService);
        assertTrue(mv.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));
        assertTrue(mv.getModel().containsKey(OAuth20Constants.EXPIRES_IN));
        assertTrue(mv.getModel().containsKey(OAuth20Constants.SCOPE));
        assertTrue(mv.getModel().containsKey(OAuth20Constants.TOKEN_TYPE));

        thrown.expect(ParseException.class);
        val at = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();
        JWTParser.parse(at);
    }

    @Test
    public void verifyAccessTokenAsJwt() throws Exception {
        val registeredService = getRegisteredService("example", "secret", new LinkedHashSet<>());
        registeredService.setJwtAccessToken(true);
        servicesManager.save(registeredService);

        val mv = getModelAndView(registeredService);
        assertTrue(mv.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));

        val at = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();
        val jwt = JWTParser.parse(at);
        assertNotNull(jwt);
    }

    @Test
    public void verifyAccessTokenAsJwtPerService() throws Exception {
        val registeredService = getRegisteredService("example", "secret", new LinkedHashSet<>());
        registeredService.setJwtAccessToken(true);

        val signingKey = new DefaultRegisteredServiceProperty();
        signingKey.addValue("pR3Vizkn5FSY5xCg84cIS4m-b6jomamZD68C8ash-TlNmgGPcoLgbgquxHPoi24tRmGpqHgM4mEykctcQzZ-Xg");
        registeredService.getProperties().put(
            RegisteredServiceProperty.RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_SIGNING_KEY.getPropertyName(), signingKey);

        val encKey = new DefaultRegisteredServiceProperty();
        encKey.addValue("0KVXaN-nlXafRUwgsr3H_l6hkufY7lzoTy7OVI5pN0E");
        registeredService.getProperties().put(
            RegisteredServiceProperty.RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_ENCRYPTION_KEY.getPropertyName(), encKey);

        servicesManager.save(registeredService);

        val mv = getModelAndView(registeredService);
        assertTrue(mv.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));

        val at = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();
        val jwt = JWTParser.parse(at);
        assertNotNull(jwt);
    }

    private ModelAndView getModelAndView(final OAuthRegisteredService registeredService) {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        val mockResponse = new MockHttpServletResponse();

        val service = RegisteredServiceTestUtils.getService("example");

        val factory = new DefaultAccessTokenFactory(new HardTimeoutExpirationPolicy(30),
            new JWTBuilder("cas.example.org", new OAuth20JwtAccessTokenCipherExecutor(), servicesManager,
                new RegisteredServiceJWTAccessTokenCipherExecutor()));

        val accessToken = factory.create(service,
            RegisteredServiceTestUtils.getAuthentication("casuser"),
            new MockTicketGrantingTicket("casuser"),
            new ArrayList<>());

        val genBuilder = OAuth20TokenGeneratedResult.builder();
        val generatedToken = genBuilder.registeredService(registeredService)
            .grantType(OAuth20GrantTypes.AUTHORIZATION_CODE)
            .responseType(OAuth20ResponseTypes.CODE)
            .accessToken(accessToken)
            .build();

        val builder = OAuth20AccessTokenResponseResult.builder();
        val result = builder
            .registeredService(registeredService)
            .responseType(OAuth20ResponseTypes.CODE)
            .service(service)
            .generatedToken(generatedToken)
            .build();
        return accessTokenResponseGenerator.generate(mockRequest, mockResponse, result);
    }
}
