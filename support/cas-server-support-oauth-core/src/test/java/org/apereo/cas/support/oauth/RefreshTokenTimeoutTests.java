package org.apereo.cas.support.oauth;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.AbstractWebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.accesstoken.AccessTokenImpl;
import org.apereo.cas.ticket.refreshtoken.RefreshToken;
import org.apereo.cas.ticket.refreshtoken.RefreshTokenImpl;
import org.apereo.cas.ticket.support.HardTimeoutExpirationPolicy;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * This is {@link RefreshTokenTimeoutTests}.
 *
 * @since 5.3.0
 */
public class RefreshTokenTimeoutTests {

    private static final UniqueTicketIdGenerator ID_GENERATOR = new DefaultUniqueTicketIdGenerator(64);

    private static final ExpirationPolicy EXP_POLICY_TGT = new HardTimeoutExpirationPolicy(1000);

    private static final ExpirationPolicy EXP_POLICY_AT = new HardTimeoutExpirationPolicy(1000);
    private static final ExpirationPolicy EXP_POLICY_RT = new HardTimeoutExpirationPolicy(1000);

    @Test
    public void verifyAccessTokenExpiryWhenTgtIsExpired() {
        final TicketGrantingTicket tgt = newTGT();
        final AccessToken at = newAt(tgt);

        assertFalse("Access token should not be expired", at.isExpired());

        tgt.markTicketExpired();

        assertFalse("Access token should not be expired when TGT is expired", at.isExpired());
    }

    @Test
    public void verifyRefreshTokenExpiryWhenTgtIsExpired() {
        final TicketGrantingTicket tgt = newTGT();
        final AccessToken at = newAt(tgt);
        final RefreshToken rt = newRt(at);

        assertFalse("Refresh token should not be expired", rt.isExpired());

        tgt.markTicketExpired();

        assertFalse("Refresh token should not be expired when TGT is expired", rt.isExpired());
    }

    static TicketGrantingTicket newTGT() {
        final Principal principal = new DefaultPrincipalFactory().createPrincipal(
                "bob", Collections.singletonMap("displayName", "Bob"));
        return new TicketGrantingTicketImpl(
                ID_GENERATOR.getNewTicketId(TicketGrantingTicket.PREFIX),
                CoreAuthenticationTestUtils.getAuthentication(principal),
                EXP_POLICY_TGT);
    }

    static AccessToken newAt(final TicketGrantingTicket tgt) {
        final Service testService = getService("https://service.example.com");
        final AccessToken at = new AccessTokenImpl(
                ID_GENERATOR.getNewTicketId(AccessToken.PREFIX),
                testService,
                tgt.getAuthentication(),
                EXP_POLICY_AT,
                tgt,
                new ArrayList<>()
        );
        tgt.getDescendantTickets().add(at.getId());
        return at;
    }

    static RefreshToken newRt(final AccessToken at) {
        final RefreshToken rt = new RefreshTokenImpl(
                ID_GENERATOR.getNewTicketId(RefreshToken.PREFIX),
                at.getService(),
                at.getAuthentication(),
                EXP_POLICY_RT,
                at.getTicketGrantingTicket(),
                new ArrayList<>()
        );
        at.getTicketGrantingTicket().getDescendantTickets().add(rt.getId());
        return rt;
    }

    static AbstractWebApplicationService getService(final String name) {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", name);
        return (AbstractWebApplicationService) new WebApplicationServiceFactory().createService(request);
    }
}
