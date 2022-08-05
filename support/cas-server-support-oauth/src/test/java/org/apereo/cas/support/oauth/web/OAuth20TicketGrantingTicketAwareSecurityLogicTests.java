package org.apereo.cas.support.oauth.web;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
        profileManager.save(true, new BasicUserProfile(), false);
        JEESessionStore.INSTANCE.set(context, WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID, UUID.randomUUID().toString());
        val logic = new OAuth20TicketGrantingTicketAwareSecurityLogic(ticketGrantingTicketCookieGenerator, ticketRegistry);
        assertTrue(logic.loadProfiles(profileManager, context, JEESessionStore.INSTANCE, List.of()).isEmpty());
    }

    @Test
    public void verifyLoadWithValidTicket() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);
        val profileManager = new ProfileManager(context, JEESessionStore.INSTANCE);
        profileManager.save(true, new BasicUserProfile(), false);

        val tgt = new MockTicketGrantingTicket(UUID.randomUUID().toString());
        JEESessionStore.INSTANCE.set(context, WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID, tgt.getId());
        ticketRegistry.addTicket(tgt);
        val logic = new OAuth20TicketGrantingTicketAwareSecurityLogic(ticketGrantingTicketCookieGenerator, ticketRegistry);
        assertFalse(logic.loadProfiles(profileManager, context, JEESessionStore.INSTANCE, List.of()).isEmpty());
    }

    @Test
    public void verifyLoadNoProfileWhenNoTgtAvailable() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val context = new JEEContext(request, response);
        val profileManager = new ProfileManager(context, JEESessionStore.INSTANCE);
        profileManager.save(true, new BasicUserProfile(), false);
        val logic = new OAuth20TicketGrantingTicketAwareSecurityLogic(ticketGrantingTicketCookieGenerator, ticketRegistry);
        assertTrue(logic.loadProfiles(profileManager, context, JEESessionStore.INSTANCE, List.of()).isEmpty());
    }
}
