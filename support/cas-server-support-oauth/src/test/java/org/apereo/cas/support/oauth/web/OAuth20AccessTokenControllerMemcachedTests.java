package org.apereo.cas.support.oauth.web;

import org.apereo.cas.category.MemcachedCategory;
import org.apereo.cas.config.MemcachedTicketRegistryConfiguration;
import org.apereo.cas.ticket.code.OAuthCode;
import org.apereo.cas.ticket.registry.MemcachedTicketRegistry;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.NoOpCondition;

import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
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
@Category(MemcachedCategory.class)
@ConditionalIgnore(condition = NoOpCondition.class, port = 11211)
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
        val p = createPrincipal();
        val code = addCode(p, addRegisteredService());
        val ticket = this.ticketRegistry.getTicket(code.getId(), OAuthCode.class);
        assertNotNull(ticket);
    }
}
