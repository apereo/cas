package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OAuth20RefreshTokenGrantTypeTokenRequestValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("OAuth")
public class OAuth20RefreshTokenGrantTypeTokenRequestValidatorTests extends AbstractOAuth20Tests {
    private static final String SUPPORTING_TICKET = "RT-SUPPORTING";

    private static final String NON_SUPPORTING_TICKET = "RT-NON-SUPPORTING";

    private static final String PROMISCUOUS_TICKET = "RT-PROMISCUOUS";

    @Autowired
    @Qualifier("oauthRefreshTokenGrantTypeTokenRequestValidator")
    private OAuth20TokenRequestValidator validator;

    @BeforeEach
    public void before() {
        servicesManager.deleteAll();
        val supportingService = RequestValidatorTestUtils.getService(
            RegisteredServiceTestUtils.CONST_TEST_URL,
            RequestValidatorTestUtils.SUPPORTING_CLIENT_ID,
            RequestValidatorTestUtils.SUPPORTING_CLIENT_ID,
            RequestValidatorTestUtils.SHARED_SECRET,
            CollectionUtils.wrapSet(OAuth20GrantTypes.REFRESH_TOKEN));
        val nonSupportingService = RequestValidatorTestUtils.getService(
            RegisteredServiceTestUtils.CONST_TEST_URL2,
            RequestValidatorTestUtils.NON_SUPPORTING_CLIENT_ID,
            RequestValidatorTestUtils.NON_SUPPORTING_CLIENT_ID,
            RequestValidatorTestUtils.SHARED_SECRET,
            CollectionUtils.wrapSet(OAuth20GrantTypes.PASSWORD));
        val promiscuousService = RequestValidatorTestUtils.getPromiscuousService(
            RegisteredServiceTestUtils.CONST_TEST_URL3,
            RequestValidatorTestUtils.PROMISCUOUS_CLIENT_ID,
            RequestValidatorTestUtils.PROMISCUOUS_CLIENT_ID,
            RequestValidatorTestUtils.SHARED_SECRET);

        servicesManager.save(supportingService);
        servicesManager.save(nonSupportingService);
        servicesManager.save(promiscuousService);

        registerTicket(SUPPORTING_TICKET, RequestValidatorTestUtils.SUPPORTING_CLIENT_ID);
        registerTicket(NON_SUPPORTING_TICKET, RequestValidatorTestUtils.NON_SUPPORTING_CLIENT_ID);
        registerTicket(PROMISCUOUS_TICKET, RequestValidatorTestUtils.PROMISCUOUS_CLIENT_ID);
    }

    @Test
    public void verifyRefreshTokenFromAnotherClientId() {
        val request = new MockHttpServletRequest();

        val profile = new CommonProfile();
        profile.setClientName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);
        profile.setId(RequestValidatorTestUtils.SUPPORTING_CLIENT_ID);
        val session = request.getSession(true);
        assertNotNull(session);
        session.setAttribute(Pac4jConstants.USER_PROFILES,
            CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

        val response = new MockHttpServletResponse();
        request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.getType());
        request.setParameter(OAuth20Constants.CLIENT_ID, RequestValidatorTestUtils.PROMISCUOUS_CLIENT_ID);
        request.setParameter(OAuth20Constants.CLIENT_SECRET, RequestValidatorTestUtils.SHARED_SECRET);
        request.setParameter(OAuth20Constants.REFRESH_TOKEN, SUPPORTING_TICKET);
        assertFalse(validator.validate(new JEEContext(request, response)));
    }

    @Test
    public void verifyOperationClientSecretPost() {
        val request = new MockHttpServletRequest();

        val profile = new CommonProfile();
        profile.setClientName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);
        profile.setId(RequestValidatorTestUtils.SUPPORTING_CLIENT_ID);
        val session = request.getSession(true);
        assertNotNull(session);
        session.setAttribute(Pac4jConstants.USER_PROFILES,
            CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

        val response = new MockHttpServletResponse();
        request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.getType());
        request.setParameter(OAuth20Constants.CLIENT_ID, RequestValidatorTestUtils.SUPPORTING_CLIENT_ID);
        request.setParameter(OAuth20Constants.CLIENT_SECRET, RequestValidatorTestUtils.SHARED_SECRET);
        request.setParameter(OAuth20Constants.REFRESH_TOKEN, SUPPORTING_TICKET);

        assertTrue(validator.validate(new JEEContext(request, response)));

        profile.setId(RequestValidatorTestUtils.NON_SUPPORTING_CLIENT_ID);
        session.setAttribute(Pac4jConstants.USER_PROFILES,
            CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));
        request.setParameter(OAuth20Constants.CLIENT_ID, RequestValidatorTestUtils.NON_SUPPORTING_CLIENT_ID);
        request.setParameter(OAuth20Constants.CLIENT_SECRET, RequestValidatorTestUtils.SHARED_SECRET);
        request.setParameter(OAuth20Constants.REFRESH_TOKEN, NON_SUPPORTING_TICKET);
        assertFalse(validator.validate(new JEEContext(request, response)));

        profile.setId(RequestValidatorTestUtils.PROMISCUOUS_CLIENT_ID);
        session.setAttribute(Pac4jConstants.USER_PROFILES,
            CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));
        request.setParameter(OAuth20Constants.CLIENT_ID, RequestValidatorTestUtils.PROMISCUOUS_CLIENT_ID);
        request.setParameter(OAuth20Constants.CLIENT_SECRET, RequestValidatorTestUtils.SHARED_SECRET);
        request.setParameter(OAuth20Constants.REFRESH_TOKEN, PROMISCUOUS_TICKET);
        assertTrue(this.validator.validate(new JEEContext(request, response)));
    }

    @Test
    public void verifyOperationClientSecretBasic() {
        val request = new MockHttpServletRequest();

        val profile = new CommonProfile();
        profile.setClientName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);
        profile.setId(RequestValidatorTestUtils.SUPPORTING_CLIENT_ID);
        val session = request.getSession(true);
        assertNotNull(session);
        session.setAttribute(Pac4jConstants.USER_PROFILES,
            CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

        val response = new MockHttpServletResponse();
        request.addHeader("Authorization",
            "Basic " + EncodingUtils.encodeBase64(RequestValidatorTestUtils.SUPPORTING_CLIENT_ID + ':' + RequestValidatorTestUtils.SHARED_SECRET));
        request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.getType());
        request.setParameter(OAuth20Constants.REFRESH_TOKEN, SUPPORTING_TICKET);

        assertTrue(this.validator.validate(new JEEContext(request, response)));

        profile.setId(RequestValidatorTestUtils.NON_SUPPORTING_CLIENT_ID);
        session.setAttribute(Pac4jConstants.USER_PROFILES,
            CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));
        request.removeHeader("Authorization");
        request.addHeader("Authorization", "Basic " + EncodingUtils.encodeBase64(RequestValidatorTestUtils.NON_SUPPORTING_CLIENT_ID
            + ':' + RequestValidatorTestUtils.SHARED_SECRET));
        request.setParameter(OAuth20Constants.REFRESH_TOKEN, NON_SUPPORTING_TICKET);
        assertFalse(validator.validate(new JEEContext(request, response)));

        profile.setId(RequestValidatorTestUtils.PROMISCUOUS_CLIENT_ID);
        session.setAttribute(Pac4jConstants.USER_PROFILES,
            CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));
        request.removeHeader("Authorization");
        request.addHeader("Authorization",
            "Basic " + EncodingUtils.encodeBase64(RequestValidatorTestUtils.PROMISCUOUS_CLIENT_ID + ':' + RequestValidatorTestUtils.SHARED_SECRET));
        request.setParameter(OAuth20Constants.REFRESH_TOKEN, PROMISCUOUS_TICKET);
        assertTrue(validator.validate(new JEEContext(request, response)));
    }

    private void registerTicket(final String name, final String clientId) {
        val tgt = new MockTicketGrantingTicket("casuser");
        val token = mock(OAuth20RefreshToken.class);
        when(token.getId()).thenReturn(name);
        when(token.getService()).thenReturn(RegisteredServiceTestUtils.getService(name));
        when(token.isExpired()).thenReturn(false);
        when(token.getAuthentication()).thenReturn(tgt.getAuthentication());
        when(token.getTicketGrantingTicket()).thenReturn(tgt);
        when(token.getClientId()).thenReturn(clientId);
        ticketRegistry.addTicket(token);
    }
}
