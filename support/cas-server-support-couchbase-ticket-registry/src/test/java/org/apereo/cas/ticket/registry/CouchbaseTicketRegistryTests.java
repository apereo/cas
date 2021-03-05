package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CouchbaseTicketRegistryConfiguration;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.AlwaysExpiresExpirationPolicy;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CouchbaseTicketRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Couchbase")
@EnabledIfPortOpen(port = 8091)
@SpringBootTest(classes = {
    CouchbaseTicketRegistryConfiguration.class,
    BaseTicketRegistryTests.SharedTestConfiguration.class
},
    properties = {
        "cas.ticket.registry.couchbase.cluster-password=password",
        "cas.ticket.registry.couchbase.cluster-username=admin",
        "cas.ticket.registry.couchbase.scan-consistency=REQUEST_PLUS",
        "cas.ticket.registry.couchbase.bucket=testbucket"
    })
@Getter
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CouchbaseTicketRegistryTests extends BaseTicketRegistryTests {
    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry newTicketRegistry;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @RepeatedTest(1)
    @Order(Integer.MAX_VALUE)
    public void verifyDestroyOperation() {
        assertNotNull(newTicketRegistry);
        applicationContext.getBeanFactory().destroyBean(newTicketRegistry);
    }

    @RepeatedTest(2)
    public void verifyAddAndLoadExpired() {
        newTicketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
            CoreAuthenticationTestUtils.getAuthentication(),
            AlwaysExpiresExpirationPolicy.INSTANCE));
        val tickets = newTicketRegistry.getTickets();
        assertTrue(tickets.stream().noneMatch(t -> t.getId().equals(ticketGrantingTicketId)));
    }

    @RepeatedTest(1)
    public void verifyFails() {
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                newTicketRegistry.addTicket(null);
            }
        });
    }
}
