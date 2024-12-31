package org.apereo.cas.oidc.web;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.web.cookie.CasCookieBuilder;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.profile.BasicUserProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.session.JEESessionStore;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OidcAuthenticationAuthorizeSecurityLogicTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("OIDC")
class OidcAuthenticationAuthorizeSecurityLogicTests extends AbstractOidcTests {

    private TicketGrantingTicket ticketGrantingTicket;

    @Override
    @BeforeEach
    protected void initialize() throws Throwable {
        super.initialize();
        ticketGrantingTicketCookieGenerator = mock(CasCookieBuilder.class);
        ticketGrantingTicket = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(ticketGrantingTicket);
    }

    @Test
    void verifyOperation() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        when(ticketGrantingTicketCookieGenerator.retrieveCookieValue(request)).thenReturn(ticketGrantingTicket.getId());

        val context = new JEEContext(request, response);
        val profileManager = new ProfileManager(context, new JEESessionStore());
        profileManager.save(true, new BasicUserProfile(), false);
        val logic = new OidcAuthenticationAuthorizeSecurityLogic(ticketGrantingTicketCookieGenerator,
            ticketRegistry, oauthRequestParameterResolver);
        assertFalse(logic.loadProfiles(new CallContext(context, new JEESessionStore()), profileManager, List.of()).isEmpty());
        request.setQueryString("prompt=login");
        assertTrue(logic.loadProfiles(new CallContext(context, new JEESessionStore()), profileManager, List.of()).isEmpty());
    }

    @Test
    void verifyMaxAgeOperation() {
        val request = new MockHttpServletRequest();
        request.addParameter(OidcConstants.MAX_AGE, "5");
        val response = new MockHttpServletResponse();

        when(ticketGrantingTicketCookieGenerator.retrieveCookieValue(request)).thenReturn(ticketGrantingTicket.getId());

        val context = new JEEContext(request, response);
        val profileManager = new ProfileManager(context, new JEESessionStore());
        var profile = new BasicUserProfile();
        profile.addAuthenticationAttribute(
            CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE,
            ZonedDateTime.now(Clock.systemUTC()).minusSeconds(30));

        profileManager.save(true, profile, false);
        val logic = new OidcAuthenticationAuthorizeSecurityLogic(ticketGrantingTicketCookieGenerator,
            ticketRegistry, oauthRequestParameterResolver);
        assertTrue(logic.loadProfiles(new CallContext(context, new JEESessionStore()), profileManager, List.of()).isEmpty());
    }

    @Test
    void verifyLoadNoProfileWhenNoTgtAvailable() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val context = new JEEContext(request, response);
        val profileManager = new ProfileManager(context, new JEESessionStore());
        profileManager.save(true, new BasicUserProfile(), false);
        val logic = new OidcAuthenticationAuthorizeSecurityLogic(ticketGrantingTicketCookieGenerator,
            ticketRegistry, oauthRequestParameterResolver);
        assertTrue(logic.loadProfiles(new CallContext(context, new JEESessionStore()), profileManager, List.of()).isEmpty());
    }
}
