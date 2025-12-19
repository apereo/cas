package org.apereo.cas.support.oauth.validator.token;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20TokenExchangeTypes;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.token.JwtBuilder;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20TokenExchangeGrantTypeTokenRequestValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("OAuth")
class OAuth20TokenExchangeGrantTypeTokenRequestValidatorTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier("oauthTokenExchangeGrantTypeTokenRequestValidator")
    private OAuth20TokenRequestValidator validator;

    private JEEContext context;
    private MockHttpServletRequest request;

    @BeforeEach
    void setup() {
        request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.USER_AGENT, "Firefox");
        val response = new MockHttpServletResponse();
        context = new JEEContext(request, response);
        val profile = new CommonProfile();
        profile.setClientName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);
        profile.setId("casuser");
        new ProfileManager(context, oauthDistributedSessionStore).save(true, profile, false);
    }

    @Test
    void verifySupports() throws Throwable {
        val service = addRegisteredService(Set.of(OAuth20GrantTypes.TOKEN_EXCHANGE), UUID.randomUUID().toString(), UUID.randomUUID().toString());
        request.addParameter(OAuth20Constants.SUBJECT_TOKEN, UUID.randomUUID().toString());
        request.addParameter(OAuth20Constants.SUBJECT_TOKEN_TYPE, OAuth20TokenExchangeTypes.ACCESS_TOKEN.getType());
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.TOKEN_EXCHANGE.getType());
        request.addParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        assertTrue(validator.supports(context));
        assertEquals(Ordered.LOWEST_PRECEDENCE, validator.getOrder());
    }

    @Test
    void verifyWithoutSubjectToken() {
        val service = addRegisteredService(Set.of(OAuth20GrantTypes.TOKEN_EXCHANGE), UUID.randomUUID().toString(), UUID.randomUUID().toString());
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.TOKEN_EXCHANGE.getType());
        request.addParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        assertThrows(IllegalArgumentException.class, () -> validator.validate(context));
        request.addParameter(OAuth20Constants.SUBJECT_TOKEN_TYPE, OAuth20TokenExchangeTypes.ACCESS_TOKEN.getType());
        assertThrows(IllegalArgumentException.class, () -> validator.validate(context));
    }

    @MethodSource("contextProvider")
    @ParameterizedTest
    void verifyServicePassingWithTicket(final Object subject, final OAuth20TokenExchangeTypes type,
                                        final OAuthRegisteredService registeredService,
                                        final Boolean expectation) throws Throwable {
        request.setParameter(OAuth20Constants.SUBJECT_TOKEN, subject.toString());
        if (subject instanceof final Ticket at) {
            ticketRegistry.addTicket(at);
        }
        request.addParameter(OAuth20Constants.AUDIENCE, UUID.randomUUID().toString());
        request.addParameter(OAuth20Constants.SUBJECT_TOKEN_TYPE, type.getType());
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.TOKEN_EXCHANGE.getType());

        servicesManager.save(registeredService);
        assertEquals(expectation, validator.validate(context));
    }

    static Stream<Arguments> contextProvider() throws Exception {
        val claims = JWTClaimsSet.parse(Map.of(
            "sub", UUID.randomUUID().toString(),
            "aud", UUID.randomUUID().toString(),
            "iat", ZonedDateTime.now(Clock.systemUTC()).toEpochSecond(),
            "nbf", ZonedDateTime.now(Clock.systemUTC()).toEpochSecond(),
            "exp", ZonedDateTime.now(Clock.systemUTC()).plusHours(2).toEpochSecond(),
            "iss", RegisteredServiceTestUtils.CONST_TEST_URL
        ));
        val jwtRegisteredService = getRegisteredService(claims.getIssuer(), claims.getSubject(),
            UUID.randomUUID().toString(), Set.of(OAuth20GrantTypes.TOKEN_EXCHANGE));
        val jwt = JwtBuilder.buildPlain(claims, Optional.of(jwtRegisteredService));

        val accessToken = getAccessToken(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString());
        val accessTokenRegisteredService = getRegisteredService(accessToken.getService().getId(),
            accessToken.getClientId(), UUID.randomUUID().toString(), Set.of(OAuth20GrantTypes.TOKEN_EXCHANGE));
        
        val accessTokenBadGrant = getAccessToken(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString());
        val accessTokenBadGrantService = getRegisteredService(accessTokenBadGrant.getService().getId(),
            accessTokenBadGrant.getClientId(), UUID.randomUUID().toString(), Set.of());

        return Stream.of(
            Arguments.of(accessToken, OAuth20TokenExchangeTypes.ACCESS_TOKEN, accessTokenRegisteredService, true),
            Arguments.of(accessTokenBadGrant, OAuth20TokenExchangeTypes.ACCESS_TOKEN, accessTokenBadGrantService, false),
            Arguments.of(jwt, OAuth20TokenExchangeTypes.JWT, jwtRegisteredService, true)
        );
    }
}
