package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.OAuth20TokenExchangeTypes;
import org.apereo.cas.token.JwtBuilder;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

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
        ticketRegistry.addTicket(subjectToken);

        request.addParameter(OAuth20Constants.SUBJECT_TOKEN, subjectToken.getId());
        request.addParameter(OAuth20Constants.SUBJECT_TOKEN_TYPE, OAuth20TokenExchangeTypes.ACCESS_TOKEN.getType());

        request.addParameter(OAuth20Constants.ACTOR_TOKEN, jwtString);
        request.addParameter(OAuth20Constants.ACTOR_TOKEN_TYPE, OAuth20TokenExchangeTypes.JWT.getType());

        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.TOKEN_EXCHANGE.getType());
        request.addParameter(OAuth20Constants.AUDIENCE, registeredService.getClientId());

        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);

        val userProfile = new CommonProfile();
        userProfile.setId("casuser");
        userProfile.addAttributes((Map) RegisteredServiceTestUtils.getTestAttributes());
        new ProfileManager(context, oauthDistributedSessionStore).save(true, userProfile, false);

        val tokenContext = extractor.extract(context);
        assertNotNull(tokenContext);
    }

    @Test
    void verifyExtractionWithAccessTokenType() throws Throwable {
        val service = addRegisteredService(Set.of(OAuth20GrantTypes.TOKEN_EXCHANGE));
        val request = new MockHttpServletRequest();

        val subjectToken = getAccessToken(service.getServiceId(), service.getClientId());
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
        val context = new JEEContext(request, response);
        assertEquals(OAuth20ResponseTypes.NONE, extractor.getResponseType());
        assertTrue(extractor.supports(context));
        val tokenContext = extractor.extract(context);
        assertNotNull(tokenContext);
        assertNotNull(tokenContext.getSubjectToken());
        assertFalse(tokenContext.getTokenExchangeAudience().isEmpty());
        assertNull(tokenContext.getTokenExchangeResource());
        assertEquals(OAuth20TokenExchangeTypes.ACCESS_TOKEN, tokenContext.getSubjectTokenType());
        assertEquals(OAuth20TokenExchangeTypes.ACCESS_TOKEN, tokenContext.getRequestedTokenType());
        assertNotNull(tokenContext.getActorToken());
    }
}
