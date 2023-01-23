package org.apereo.cas.support.oauth.web;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.web.cookie.CasCookieBuilder;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.profile.BasicUserProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.session.JEESessionStore;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20TicketGrantingTicketAwareSecurityLogicTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("OAuthToken")
public class OAuth20TicketGrantingTicketAwareSecurityLogicTests extends AbstractOAuth20Tests {
    @Mock
    private CasCookieBuilder ticketGrantingTicketCookieGenerator;

    @BeforeEach
    public void initialize() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void verifyLoadWithBadTicketInSession() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);
        val profileManager = new ProfileManager(context, JEESessionStore.INSTANCE);
        val profile = new BasicUserProfile();
        profile.addAttribute(TicketGrantingTicket.class.getName(), UUID.randomUUID().toString());
        profileManager.save(true, profile, false);
        val logic = new OAuth20TicketGrantingTicketAwareSecurityLogic(ticketGrantingTicketCookieGenerator, ticketRegistry);
        assertTrue(logic.loadProfiles(new CallContext(context, JEESessionStore.INSTANCE), profileManager, List.of()).isEmpty());
    }

    @Test
    public void verifyLoadWithValidTicket() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);
        val profileManager = new ProfileManager(context, JEESessionStore.INSTANCE);
        profileManager.save(true, new BasicUserProfile(), false);

        val tgt = new MockTicketGrantingTicket(UUID.randomUUID().toString());
        val profile = new BasicUserProfile();
        profile.addAttribute(TicketGrantingTicket.class.getName(), tgt.getId());
        ticketRegistry.addTicket(tgt);
        profileManager.save(true, profile, false);
        val logic = new OAuth20TicketGrantingTicketAwareSecurityLogic(ticketGrantingTicketCookieGenerator, ticketRegistry);
        assertFalse(logic.loadProfiles(new CallContext(context, JEESessionStore.INSTANCE), profileManager, List.of()).isEmpty());
    }

    @Test
    public void verifyLoadNoProfileWhenNoTgtAvailable() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val context = new JEEContext(request, response);
        val profileManager = new ProfileManager(context, JEESessionStore.INSTANCE);
        profileManager.save(true, new BasicUserProfile(), false);
        val logic = new OAuth20TicketGrantingTicketAwareSecurityLogic(ticketGrantingTicketCookieGenerator, ticketRegistry);
        assertTrue(logic.loadProfiles(new CallContext(context, JEESessionStore.INSTANCE), profileManager, List.of()).isEmpty());
    }
}
