package org.apereo.cas.support.oauth.web;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.validation.Assertion;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
import static org.mockito.Mockito.*;

/**
 * This is {@link OAuth20TicketGrantingTicketAwareSecurityLogicTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("OAuthToken")
class OAuth20TicketGrantingTicketAwareSecurityLogicTests extends AbstractOAuth20Tests {

    @Test
    void verifyStatelessOperation() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val assertion = mock(Assertion.class);
        when(assertion.isStateless()).thenReturn(Boolean.TRUE);
        when(assertion.getPrimaryAuthentication()).thenReturn(RegisteredServiceTestUtils.getAuthentication());
        val profile = new BasicUserProfile();
        profile.addAttribute(Principal.class.getName(), RegisteredServiceTestUtils.getPrincipal("casuser"));
        profile.addAttribute("stateless", Boolean.TRUE);
        val context = new JEEContext(request, response);
        val profileManager = new ProfileManager(context, new JEESessionStore());
        profileManager.save(true, profile, false);
        val logic = new OAuth20TicketGrantingTicketAwareSecurityLogic(mock(CasCookieBuilder.class), ticketRegistry);
        assertFalse(logic.loadProfiles(new CallContext(context, new JEESessionStore()), profileManager, List.of()).isEmpty());
        
    }

    @Test
    void verifyLoadWithBadTicketInSession() throws Throwable {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);
        val profileManager = new ProfileManager(context, new JEESessionStore());
        val profile = new BasicUserProfile();
        profile.addAttribute(TicketGrantingTicket.class.getName(), UUID.randomUUID().toString());
        profileManager.save(true, profile, false);
        val logic = new OAuth20TicketGrantingTicketAwareSecurityLogic(mock(CasCookieBuilder.class), ticketRegistry);
        assertTrue(logic.loadProfiles(new CallContext(context, new JEESessionStore()), profileManager, List.of()).isEmpty());
    }

    @Test
    void verifyLoadWithValidTicket() throws Throwable {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);
        val profileManager = new ProfileManager(context, new JEESessionStore());
        profileManager.save(true, new BasicUserProfile(), false);

        val tgt = new MockTicketGrantingTicket(UUID.randomUUID().toString());
        val profile = new BasicUserProfile();
        profile.addAttribute(TicketGrantingTicket.class.getName(), tgt.getId());
        ticketRegistry.addTicket(tgt);
        profileManager.save(true, profile, false);
        val logic = new OAuth20TicketGrantingTicketAwareSecurityLogic(mock(CasCookieBuilder.class), ticketRegistry);
        assertFalse(logic.loadProfiles(new CallContext(context, new JEESessionStore()), profileManager, List.of()).isEmpty());
    }

    @Test
    void verifyLoadNoProfileWhenNoTgtAvailable() throws Throwable {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val context = new JEEContext(request, response);
        val profileManager = new ProfileManager(context, new JEESessionStore());
        profileManager.save(true, new BasicUserProfile(), false);
        val logic = new OAuth20TicketGrantingTicketAwareSecurityLogic(mock(CasCookieBuilder.class), ticketRegistry);
        assertTrue(logic.loadProfiles(new CallContext(context, new JEESessionStore()), profileManager, List.of()).isEmpty());
    }
}
