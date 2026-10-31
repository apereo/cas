package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.OAuth20TokenExchangeTypes;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20UserProfile;
import org.apereo.cas.ticket.OAuth20UnauthorizedScopeRequestException;
import org.apereo.cas.token.JwtBuilder;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AccessTokenTokenExchangeGrantRequestExtractorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("OAuth")
class AccessTokenTokenExchangeGrantRequestExtractorTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier("accessTokenTokenExchangeGrantRequestExtractor")
    private AccessTokenGrantRequestExtractor extractor;

    @Test
    void verifyExtractionWithJwtType() throws Throwable {
        val registeredService = addRegisteredService(Set.of(OAuth20GrantTypes.TOKEN_EXCHANGE));
        val request = new MockHttpServletRequest();

        val payload = JwtBuilder.JwtRequest
            .builder()
            .registeredService(Optional.of(registeredService))
            .serviceAudience(Set.of(UUID.randomUUID().toString()))
            .issuer(registeredService.getClientId())
            .jwtId(UUID.randomUUID().toString())
            .subject(UUID.randomUUID().toString())
            .issueDate(new Date())
            .build();

        val jwtString = accessTokenJwtBuilder.build(payload);

        val subjectToken = getAccessToken(registeredService.getServiceId(), registeredService.getClientId());
        when(subjectToken.getScopes()).thenReturn(Set.of("read"));
        ticketRegistry.addTicket(subjectToken);

        request.addParameter(OAuth20Constants.SUBJECT_TOKEN, subjectToken.getId());
        request.addParameter(OAuth20Constants.SUBJECT_TOKEN_TYPE, OAuth20TokenExchangeTypes.ACCESS_TOKEN.getType());

        request.addParameter(OAuth20Constants.ACTOR_TOKEN, jwtString);
        request.addParameter(OAuth20Constants.ACTOR_TOKEN_TYPE, OAuth20TokenExchangeTypes.JWT.getType());

        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.TOKEN_EXCHANGE.getType());
        request.addParameter(OAuth20Constants.AUDIENCE, registeredService.getClientId());

        val response = new MockHttpServletResponse();

        val userProfile = new DefaultOAuth20UserProfile(registeredService.getClientId());
        userProfile.addAttribute(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        userProfile.addAttributes((Map) RegisteredServiceTestUtils.getTestAttributes());
        storeProfileIntoSession(request, userProfile);

        val tokenContext = extractor.extract(request, response);
        assertNotNull(tokenContext);
    }

    @Test
    void verifyExtractionWithAccessTokenType() throws Throwable {
        val service = addRegisteredService(Set.of(OAuth20GrantTypes.TOKEN_EXCHANGE));
        val request = new MockHttpServletRequest();

        val subjectToken = getAccessToken(service.getServiceId(), service.getClientId());
        when(subjectToken.getScopes()).thenReturn(Set.of("read"));
        ticketRegistry.addTicket(subjectToken);

        val actorToken = getAccessToken(randomServiceUrl(), UUID.randomUUID().toString());
        ticketRegistry.addTicket(actorToken);

        request.addParameter(OAuth20Constants.SUBJECT_TOKEN, subjectToken.getId());
        request.addParameter(OAuth20Constants.SUBJECT_TOKEN_TYPE, OAuth20TokenExchangeTypes.ACCESS_TOKEN.getType());

        request.addParameter(OAuth20Constants.ACTOR_TOKEN, actorToken.getId());
        request.addParameter(OAuth20Constants.ACTOR_TOKEN_TYPE, OAuth20TokenExchangeTypes.ACCESS_TOKEN.getType());

        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.TOKEN_EXCHANGE.getType());
        request.addParameter(OAuth20Constants.AUDIENCE, service.getClientId());

        val response = new MockHttpServletResponse();
        val userProfile = new DefaultOAuth20UserProfile(service.getClientId());
        userProfile.addAttribute(OAuth20Constants.CLIENT_ID, service.getClientId());
        storeProfileIntoSession(request, userProfile);
        assertEquals(OAuth20ResponseTypes.NONE, extractor.getResponseType());
        assertTrue(extractor.supports(request));
        val tokenContext = extractor.extract(request, response);
        assertNotNull(tokenContext);
        assertNotNull(tokenContext.getSubjectToken());
        assertFalse(tokenContext.getTokenExchangeAudience().isEmpty());
        assertNull(tokenContext.getTokenExchangeResource());
        assertEquals(OAuth20TokenExchangeTypes.ACCESS_TOKEN, tokenContext.getSubjectTokenType());
        assertEquals(OAuth20TokenExchangeTypes.ACCESS_TOKEN, tokenContext.getRequestedTokenType());
        assertNotNull(tokenContext.getActorToken());
    }

    @Test
    void verifyRequestedScopesCannotExceedSubjectToken() throws Throwable {
        val service = addRegisteredService(Set.of(OAuth20GrantTypes.TOKEN_EXCHANGE));
        service.setScopes(Set.of("read", "write"));
        servicesManager.save(service);

        val subjectToken = getAccessToken(service.getServiceId(), service.getClientId());
        when(subjectToken.getScopes()).thenReturn(Set.of("read"));
        ticketRegistry.addTicket(subjectToken);

        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.SUBJECT_TOKEN, subjectToken.getId());
        request.addParameter(OAuth20Constants.SUBJECT_TOKEN_TYPE, OAuth20TokenExchangeTypes.ACCESS_TOKEN.getType());
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.TOKEN_EXCHANGE.getType());
        request.addParameter(OAuth20Constants.SCOPE, "write");
        val response = new MockHttpServletResponse();

        val userProfile = new DefaultOAuth20UserProfile(service.getClientId());
        userProfile.addAttribute(OAuth20Constants.CLIENT_ID, service.getClientId());
        storeProfileIntoSession(request, userProfile);

        assertThrows(OAuth20UnauthorizedScopeRequestException.class, () -> extractor.extract(request, response));
    }
}
