package org.apereo.cas.support.oauth.web.response.accesstoken;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.OAuth20TokenExchangeTypes;
import org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthAccessTokenExpirationPolicy;
import org.apereo.cas.support.oauth.validator.token.device.InvalidOAuth20DeviceTokenException;
import org.apereo.cas.support.oauth.validator.token.device.ThrottledOAuth20DeviceUserCodeApprovalException;
import org.apereo.cas.support.oauth.validator.token.device.UnapprovedOAuth20DeviceUserCodeException;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import lombok.val;
import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20DefaultTokenGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OAuthToken")
@TestPropertySource(properties = {
    "cas.authn.oauth.access-token.crypto.encryption.key=AZ5y4I9qzKPYUVNL2Td4RMbpg6Z-ldui8VEFg8hsj1M",
    "cas.authn.oauth.access-token.crypto.signing.key=cAPyoHMrOMWrwydOXzBA-ufZQM-TilnLjbRgMQWlUlwFmy07bOtAgCIdNBma3c5P4ae_JV6n1OpOAYqSh2NkmQ",
    "cas.authn.oauth.access-token.crypto.enabled=true",
    "cas.authn.oauth.device-token.refresh-interval=PT1S"
})
class OAuth20DefaultTokenGeneratorTests extends AbstractOAuth20Tests {
    @Test
    void verifyExchangeTokens() throws Throwable {

        val registeredService = getRegisteredService(UUID.randomUUID().toString(), "secret",
            Set.of(OAuth20GrantTypes.TOKEN_EXCHANGE));
        servicesManager.save(registeredService);

        val accessToken = getAccessToken(registeredService.getServiceId(), registeredService.getClientId());
        ticketRegistry.addTicket(accessToken);

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.REQUESTED_TOKEN_TYPE, OAuth20TokenExchangeTypes.ACCESS_TOKEN.getType());
        mockRequest.setParameter(OAuth20Constants.SUBJECT_TOKEN, accessToken.getId());
        
        val mv = generateAccessTokenResponseAndGetModelAndView(registeredService,
            RegisteredServiceTestUtils.getAuthentication("casuser"),
            OAuth20GrantTypes.TOKEN_EXCHANGE, mockRequest);
        assertNotNull(mv);
        val id = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();
        val at = ticketRegistry.getTicket(id, OAuth20AccessToken.class);
        assertNotNull(at);
        val attributes = at.getAuthentication().getPrincipal().getAttributes();
        assertTrue(attributes.containsKey("givenName"));
        assertTrue(attributes.containsKey("uid"));
        assertTrue(attributes.containsKey("username"));
        assertTrue(mv.getModel().containsKey(OAuth20Constants.EXPIRES_IN));
        assertTrue(mv.getModel().containsKey(OAuth20Constants.TOKEN_TYPE));
        assertTrue(mv.getModel().containsKey(OAuth20Constants.ISSUED_TOKEN_TYPE));
    }

    @Test
    void verifyRequestedClaims() throws Throwable {
        val registeredService = getRegisteredService(UUID.randomUUID().toString(), "secret", new LinkedHashSet<>());
        servicesManager.save(registeredService);
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        val claims = "\"userinfo\": {\"given_name\": {\"essential\": true}}";
        mockRequest.addParameter(OAuth20Constants.CLAIMS, claims);
        val mv = generateAccessTokenResponseAndGetModelAndView(registeredService,
            RegisteredServiceTestUtils.getAuthentication("casuser"),
            OAuth20GrantTypes.AUTHORIZATION_CODE, mockRequest);
        assertNotNull(mv);
        val id = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();
        val at = ticketRegistry.getTicket(id, OAuth20AccessToken.class);
        assertTrue(at.getAuthentication().getAttributes().containsKey("given_name"));
    }

    @Test
    void verifyAccessTokenNeverAdded() throws Throwable {
        val registeredService = getRegisteredService(UUID.randomUUID().toString(), "secret", new LinkedHashSet<>());
        registeredService.setAccessTokenExpirationPolicy(new DefaultRegisteredServiceOAuthAccessTokenExpirationPolicy()
            .setMaxTimeToLive("PT0S").setTimeToKill("PT0S"));
        servicesManager.save(registeredService);
        val mv = generateAccessTokenResponseAndGetModelAndView(registeredService);
        assertFalse(mv.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));
        assertFalse(mv.getModel().containsKey(OAuth20Constants.TOKEN_TYPE));
    }

    @Test
    void verifyAccessTokenAsJwt() throws Throwable {
        val registeredService = getRegisteredService(UUID.randomUUID().toString(), "secret", new LinkedHashSet<>());
        registeredService.setJwtAccessToken(true);
        servicesManager.save(registeredService);

        val mv = generateAccessTokenResponseAndGetModelAndView(registeredService);
        assertTrue(mv.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));

        val at = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();
        val decoded = oauthAccessTokenJwtCipherExecutor.decode(at).toString();
        assertNotNull(decoded);

        val jwt = JwtClaims.parse(decoded);
        assertNotNull(jwt);

        val ticketId = jwt.getJwtId();
        assertNotNull(ticketId);
        assertNotNull(ticketRegistry.getTicket(ticketId, OAuth20AccessToken.class));
    }

    @Test
    void verifySlowDown() throws Throwable {
        val token = defaultDeviceTokenFactory.createDeviceCode(RegisteredServiceTestUtils.getService("https://device.oauth.org"));
        ticketRegistry.addTicket(token);
        val userCode = defaultDeviceUserCodeFactory.createDeviceUserCode(token.getService());
        token.setUserCode(userCode.getId());
        ticketRegistry.addTicket(userCode);
        val tokenRequestContext = AccessTokenRequestContext.builder()
            .responseType(OAuth20ResponseTypes.DEVICE_CODE)
            .deviceCode(token.getId())
            .authentication(RegisteredServiceTestUtils.getAuthentication())
            .registeredService(getRegisteredService(UUID.randomUUID().toString(), "secret"))
            .build();
        assertThrows(ThrottledOAuth20DeviceUserCodeApprovalException.class, () -> oauthTokenGenerator.generate(tokenRequestContext));
    }

    @Test
    void verifyUnapproved() throws Throwable {
        val token = defaultDeviceTokenFactory.createDeviceCode(
            RegisteredServiceTestUtils.getService("https://device.oauth.org"));
        ticketRegistry.addTicket(token);
        val userCode = defaultDeviceUserCodeFactory.createDeviceUserCode(token.getService());
        token.setUserCode(userCode.getId());
        ticketRegistry.addTicket(userCode);

        Thread.sleep(2000);
        val tokenRequestContext = AccessTokenRequestContext.builder()
            .responseType(OAuth20ResponseTypes.DEVICE_CODE)
            .deviceCode(token.getId())
            .authentication(RegisteredServiceTestUtils.getAuthentication())
            .registeredService(getRegisteredService(UUID.randomUUID().toString(), "secret"))
            .build();
        assertThrows(UnapprovedOAuth20DeviceUserCodeException.class, () -> oauthTokenGenerator.generate(tokenRequestContext));
    }

    @Test
    void verifyExpiredUserCode() throws Throwable {
        val token = defaultDeviceTokenFactory.createDeviceCode(
            RegisteredServiceTestUtils.getService("https://device.oauth.org"));
        ticketRegistry.addTicket(token);
        val userCode = defaultDeviceUserCodeFactory.createDeviceUserCode(token.getService());
        token.setUserCode(userCode.getId());
        ticketRegistry.addTicket(userCode);

        Thread.sleep(2000);
        val holder = AccessTokenRequestContext.builder()
            .responseType(OAuth20ResponseTypes.DEVICE_CODE)
            .deviceCode(token.getId())
            .authentication(RegisteredServiceTestUtils.getAuthentication())
            .registeredService(getRegisteredService(UUID.randomUUID().toString(), "secret"))
            .build();
        userCode.markTicketExpired();
        assertThrows(InvalidOAuth20DeviceTokenException.class, () -> oauthTokenGenerator.generate(holder));
    }

    @Test
    void verifyDeviceCodeExpired() throws Throwable {
        val token = defaultDeviceTokenFactory.createDeviceCode(
            RegisteredServiceTestUtils.getService("https://device.oauth.org"));
        ticketRegistry.addTicket(token);
        val userCode = defaultDeviceUserCodeFactory.createDeviceUserCode(token.getService());
        token.setUserCode(userCode.getId());
        ticketRegistry.addTicket(userCode);
        Thread.sleep(2000);
        val holder = AccessTokenRequestContext.builder()
            .responseType(OAuth20ResponseTypes.DEVICE_CODE)
            .deviceCode(token.getId())
            .authentication(RegisteredServiceTestUtils.getAuthentication())
            .registeredService(getRegisteredService(UUID.randomUUID().toString(), "secret"))
            .build();
        token.markTicketExpired();
        assertThrows(InvalidOAuth20DeviceTokenException.class, () -> oauthTokenGenerator.generate(holder));
    }

    @Test
    void verifyAccessTokenIsRefreshed() throws Throwable {
        val registeredService = getRegisteredService(UUID.randomUUID().toString(), "secret", new LinkedHashSet<>());
        registeredService.setJwtAccessToken(true);
        servicesManager.save(registeredService);
        val authentication = RegisteredServiceTestUtils.getAuthentication("casuser");

        var mv = generateAccessTokenResponseAndGetModelAndView(registeredService, authentication, OAuth20GrantTypes.AUTHORIZATION_CODE);
        assertTrue(mv.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));
        val at = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();
        val decoded = oauthAccessTokenJwtCipherExecutor.decode(at).toString();
        assertNotNull(decoded);
        val jwt = JwtClaims.parse(decoded);
        assertNotNull(jwt);
        assertNotNull(jwt.getIssuedAt());
        assertNotEquals(authentication.getAuthenticationDate().toInstant().toEpochMilli(), jwt.getIssuedAt().getValueInMillis());
        assertNotNull(jwt.getExpirationTime());

        Thread.sleep(2000);

        mv = generateAccessTokenResponseAndGetModelAndView(registeredService, authentication, OAuth20GrantTypes.REFRESH_TOKEN);
        assertTrue(mv.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));
        val refreshedAt = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();
        val refreshedDecoded = oauthAccessTokenJwtCipherExecutor.decode(refreshedAt).toString();
        assertNotNull(refreshedDecoded);
        val refreshedJwt = JwtClaims.parse(refreshedDecoded);
        assertNotNull(refreshedJwt);
        assertNotNull(refreshedJwt.getIssuedAt());
        assertNotEquals(authentication.getAuthenticationDate().toInstant().toEpochMilli(), refreshedJwt.getIssuedAt().getValueInMillis());
        assertNotNull(refreshedJwt.getExpirationTime());
        assertNotEquals(jwt.getExpirationTime().getValue(), refreshedJwt.getExpirationTime().getValue());
    }

    @Test
    void verifyCustomizedRTWithNullAuthentication() throws Throwable {
        val registeredService = getRegisteredService(UUID.randomUUID().toString(), "secret", new LinkedHashSet<>());
        servicesManager.save(registeredService);
        val service = RegisteredServiceTestUtils.getService(SERVICE_URL);

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        val mockResponse = new MockHttpServletResponse();

        val holder = AccessTokenRequestContext.builder()
                .clientId(registeredService.getClientId())
                .service(service)
                .authentication(null)
                .registeredService(registeredService)
                .grantType(OAuth20GrantTypes.AUTHORIZATION_CODE)
                .responseType(OAuth20ResponseTypes.CODE)
                .ticketGrantingTicket(new MockTicketGrantingTicket("casuser"))
                .claims(oauthRequestParameterResolver.resolveRequestClaims(new JEEContext(mockRequest, mockResponse)))
                .build();

        val generatedToken = ((OAuth20DefaultTokenGenerator) oauthTokenGenerator).generateAccessTokenOAuthGrantTypes(holder);
        assertNotNull(generatedToken);
    }
}
