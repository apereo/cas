package org.apereo.cas.support.oauth.validator.token;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OAuth20RefreshTokenGrantTypeTokenRequestValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("OAuth")
@TestPropertySource(properties = "cas.authn.oauth.session-replication.replicate-sessions=false")
class OAuth20RefreshTokenGrantTypeTokenRequestValidatorTests extends AbstractOAuth20Tests {
    private static final String SUPPORTING_TICKET = "RT-SUPPORTING";

    private static final String NON_SUPPORTING_TICKET = "RT-NON-SUPPORTING";

    private static final String PROMISCUOUS_TICKET = "RT-PROMISCUOUS";

    private static final String SUPPORTING_CLIENT_ID = UUID.randomUUID().toString();

    private static final String NON_SUPPORTING_CLIENT_ID = UUID.randomUUID().toString();

    private static final String PROMISCUOUS_CLIENT_ID = UUID.randomUUID().toString();
    
    @Autowired
    @Qualifier("oauthRefreshTokenGrantTypeTokenRequestValidator")
    private OAuth20TokenRequestValidator validator;

    @BeforeEach
    void before() throws Throwable {
        val supportingService = RequestValidatorTestUtils.getService(
            RegisteredServiceTestUtils.CONST_TEST_URL,
            SUPPORTING_CLIENT_ID,
            SUPPORTING_CLIENT_ID,
            RequestValidatorTestUtils.SHARED_SECRET,
            CollectionUtils.wrapSet(OAuth20GrantTypes.REFRESH_TOKEN));
        val nonSupportingService = RequestValidatorTestUtils.getService(
            RegisteredServiceTestUtils.CONST_TEST_URL2,
            NON_SUPPORTING_CLIENT_ID,
            NON_SUPPORTING_CLIENT_ID,
            RequestValidatorTestUtils.SHARED_SECRET,
            CollectionUtils.wrapSet(OAuth20GrantTypes.PASSWORD));
        val promiscuousService = RequestValidatorTestUtils.getPromiscuousService(
            RegisteredServiceTestUtils.CONST_TEST_URL3,
            PROMISCUOUS_CLIENT_ID,
            PROMISCUOUS_CLIENT_ID,
            RequestValidatorTestUtils.SHARED_SECRET);

        servicesManager.save(supportingService, nonSupportingService, promiscuousService);

        registerTicket(SUPPORTING_TICKET, SUPPORTING_CLIENT_ID);
        registerTicket(NON_SUPPORTING_TICKET, NON_SUPPORTING_CLIENT_ID);
        registerTicket(PROMISCUOUS_TICKET, PROMISCUOUS_CLIENT_ID);
    }

    @Test
    void verifyRefreshTokenFromAnotherClientId() throws Throwable {
        val request = new MockHttpServletRequest();

        val profile = new CommonProfile();
        profile.setClientName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);
        profile.setId(SUPPORTING_CLIENT_ID);
        storeProfileIntoSession(request, profile);

        val response = new MockHttpServletResponse();
        request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.getType());
        request.setParameter(OAuth20Constants.CLIENT_ID, PROMISCUOUS_CLIENT_ID);
        request.setParameter(OAuth20Constants.CLIENT_SECRET, RequestValidatorTestUtils.SHARED_SECRET);
        request.setParameter(OAuth20Constants.REFRESH_TOKEN, SUPPORTING_TICKET);
        assertFalse(validator.validate(new JEEContext(request, response)));
    }

    @Test
    void verifyOperationClientSecretPost() throws Throwable {
        val request = new MockHttpServletRequest();

        val profile = new CommonProfile();
        profile.setClientName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);
        profile.setId(SUPPORTING_CLIENT_ID);

        storeProfileIntoSession(request, profile);

        val response = new MockHttpServletResponse();
        request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.getType());
        request.setParameter(OAuth20Constants.CLIENT_ID, SUPPORTING_CLIENT_ID);
        request.setParameter(OAuth20Constants.CLIENT_SECRET, RequestValidatorTestUtils.SHARED_SECRET);
        request.setParameter(OAuth20Constants.REFRESH_TOKEN, SUPPORTING_TICKET);

        assertTrue(validator.validate(new JEEContext(request, response)));

        profile.setId(NON_SUPPORTING_CLIENT_ID);
        storeProfileIntoSession(request, profile);

        request.setParameter(OAuth20Constants.CLIENT_ID, NON_SUPPORTING_CLIENT_ID);
        request.setParameter(OAuth20Constants.CLIENT_SECRET, RequestValidatorTestUtils.SHARED_SECRET);
        request.setParameter(OAuth20Constants.REFRESH_TOKEN, NON_SUPPORTING_TICKET);
        assertFalse(validator.validate(new JEEContext(request, response)));

        profile.setId(PROMISCUOUS_CLIENT_ID);
        storeProfileIntoSession(request, profile);
        
        request.setParameter(OAuth20Constants.CLIENT_ID, PROMISCUOUS_CLIENT_ID);
        request.setParameter(OAuth20Constants.CLIENT_SECRET, RequestValidatorTestUtils.SHARED_SECRET);
        request.setParameter(OAuth20Constants.REFRESH_TOKEN, PROMISCUOUS_TICKET);
        assertTrue(validator.validate(new JEEContext(request, response)));
    }

    @Test
    void verifyOperationClientSecretBasic() throws Throwable {
        val request = new MockHttpServletRequest();

        val profile = new CommonProfile();
        profile.setClientName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);
        profile.setId(SUPPORTING_CLIENT_ID);
        storeProfileIntoSession(request, profile);

        val response = new MockHttpServletResponse();
        request.addHeader(HttpHeaders.AUTHORIZATION,
            "Basic " + EncodingUtils.encodeBase64(SUPPORTING_CLIENT_ID + ':' + RequestValidatorTestUtils.SHARED_SECRET));
        request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.getType());
        request.setParameter(OAuth20Constants.REFRESH_TOKEN, SUPPORTING_TICKET);

        assertTrue(validator.validate(new JEEContext(request, response)));

        profile.setId(NON_SUPPORTING_CLIENT_ID);
        storeProfileIntoSession(request, profile);
        request.removeHeader(HttpHeaders.AUTHORIZATION);
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + EncodingUtils.encodeBase64(NON_SUPPORTING_CLIENT_ID
            + ':' + RequestValidatorTestUtils.SHARED_SECRET));
        request.setParameter(OAuth20Constants.REFRESH_TOKEN, NON_SUPPORTING_TICKET);
        assertFalse(validator.validate(new JEEContext(request, response)));

        profile.setId(PROMISCUOUS_CLIENT_ID);
        storeProfileIntoSession(request, profile);
        request.removeHeader(HttpHeaders.AUTHORIZATION);
        request.addHeader(HttpHeaders.AUTHORIZATION,
            "Basic " + EncodingUtils.encodeBase64(PROMISCUOUS_CLIENT_ID + ':' + RequestValidatorTestUtils.SHARED_SECRET));
        request.setParameter(OAuth20Constants.REFRESH_TOKEN, PROMISCUOUS_TICKET);
        assertTrue(validator.validate(new JEEContext(request, response)));
    }

    private void registerTicket(final String name, final String clientId) throws Throwable {
        val tgt = new MockTicketGrantingTicket("casuser");
        val token = mock(OAuth20RefreshToken.class);
        when(token.getId()).thenReturn(name);
        val service = RegisteredServiceTestUtils.getService(name);
        when(token.getService()).thenReturn(service);
        when(token.isExpired()).thenReturn(false);
        when(token.getAuthentication()).thenReturn(tgt.getAuthentication());
        when(token.getTicketGrantingTicket()).thenReturn(tgt);
        when(token.getClientId()).thenReturn(clientId);
        when(token.getExpirationPolicy()).thenReturn(NeverExpiresExpirationPolicy.INSTANCE);
        when(token.getCreationTime()).thenReturn(ZonedDateTime.now(Clock.systemUTC()));
        ticketRegistry.addTicket(token);
    }
}
