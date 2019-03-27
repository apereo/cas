package org.apereo.cas.integration.pac4j;

import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketComponentSerializationConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreTicketsSerializationConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.J2EContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpSessionEvent;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DistributedJ2ESessionStoreTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketsSerializationConfiguration.class,
    CasDefaultServiceTicketIdGeneratorsConfiguration.class,
    CasCoreTicketComponentSerializationConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    RefreshAutoConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DistributedJ2ESessionStoreTests {
    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier("defaultTicketFactory")
    private TicketFactory ticketFactory;

    @Test
    public void verifyOperation() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val store = new DistributedJ2ESessionStore(this.ticketRegistry, this.ticketFactory);
        val context = new J2EContext(request, response, store);

        assertNotNull(request.getSession());

        store.set(context, "attribute", "test");
        var value = store.get(context, "attribute");
        assertNotNull(value);
        assertEquals("test", value);

        store.set(context, "attribute", "test2");
        value = store.get(context, "attribute");
        assertNotNull(value);
        assertEquals("test2", value);

        store.sessionDestroyed(new HttpSessionEvent(request.getSession()));
        store.handle(new MockTicketGrantingTicket("casuser"));
        value = store.get(context, "attribute");
        assertNull(value);
    }
}
