package org.apereo.cas.support.oauth.web;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.category.MemcachedCategory;
import org.apereo.cas.config.MemcachedTicketRegistryConfiguration;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.code.OAuthCode;
import org.apereo.cas.ticket.registry.MemcachedTicketRegistry;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.Before;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.junit.Assert.*;

/**
 * This is {@link OAuth20AccessTokenControllerMemcachedTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Import(MemcachedTicketRegistryConfiguration.class)
@TestPropertySource(locations = {"classpath:/memcached-oauth.properties"})
@Slf4j
@Category(MemcachedCategory.class)
public class OAuth20AccessTokenControllerMemcachedTests extends AbstractOAuth20Tests {

    @Before
    public void initialize() {
        clearAllServices();
    }

    @Test
    public void verifyTicketRegistry() {
        assertTrue(this.ticketRegistry instanceof MemcachedTicketRegistry);
    }

    @Test
    public void verifyOAuthCodeIsAddedToMemcached() {
        final var p = createPrincipal();
        final var code = addCode(p, addRegisteredService());
        final Ticket ticket = this.ticketRegistry.getTicket(code.getId(), OAuthCode.class);
        assertNotNull(ticket);
    }
}
