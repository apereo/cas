package org.apereo.cas.ticket.accesstoken;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.web.AbstractOAuth20Tests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

/**
 * This is {@link DefaultAccessTokenFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OAuth")
public class DefaultAccessTokenFactoryTests extends AbstractOAuth20Tests {
    @Test
    public void verifyJwtToken() {
        val authn = RegisteredServiceTestUtils.getAuthentication();
        val tgt = new MockTicketGrantingTicket(authn.getPrincipal().getId());
        val service = RegisteredServiceTestUtils.getService("https://example.org/jwt-access-token");
        val at = this.defaultAccessTokenFactory.create(service, authn, tgt, List.of("something"), CLIENT_ID, Map.of());

    }
}
