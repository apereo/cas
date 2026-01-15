package org.apereo.cas.oidc.ticket;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.web.controllers.ciba.CibaRequestContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcDefaultCibaRequestFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("OIDC")
class OidcDefaultCibaRequestFactoryTests extends AbstractOidcTests {
    @Test
    void verifyOperation() throws Throwable {
        val cibaRequestContext = CibaRequestContext.builder()
            .scope(Set.of(OidcConstants.StandardScopes.OPENID.getScope()))
            .clientId(UUID.randomUUID().toString())
            .clientNotificationToken(UUID.randomUUID().toString())
            .bindingMessage(UUID.randomUUID().toString())
            .build();
        val factory = (OidcCibaRequestFactory) defaultTicketFactory.get(OidcCibaRequest.class);
        val ticket = factory.create(cibaRequestContext);
        assertNotNull(ticket);
        assertTrue(ticket.getId().startsWith(OidcCibaRequest.PREFIX));
        assertSame(OidcCibaRequest.class, factory.getTicketType());
        assertEquals(ticket.getClientId(), cibaRequestContext.getClientId());
        val authentication = ticket.getAuthentication();
        assertEquals(authentication.getAttributes().get(OidcConstants.CLIENT_NOTIFICATION_TOKEN).getFirst(), cibaRequestContext.getClientNotificationToken());
        assertEquals(authentication.getAttributes().get(OidcConstants.BINDING_MESSAGE).getFirst(), cibaRequestContext.getBindingMessage());
    }

    @Test
    void verifyOperationWithExpiry() throws Throwable {
        val cibaRequestContext = CibaRequestContext.builder()
            .scope(Set.of(OidcConstants.StandardScopes.OPENID.getScope()))
            .requestedExpiry(30)
            .build();
        val factory = (OidcCibaRequestFactory) defaultTicketFactory.get(OidcCibaRequest.class);
        val ticket = factory.create(cibaRequestContext);
        assertNotNull(ticket);
        assertTrue(ticket.getId().startsWith(OidcCibaRequest.PREFIX));
        assertSame(OidcCibaRequest.class, factory.getTicketType());
    }
}
