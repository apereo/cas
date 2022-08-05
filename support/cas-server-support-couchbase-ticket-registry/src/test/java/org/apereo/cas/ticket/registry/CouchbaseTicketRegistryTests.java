package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CouchbaseTicketRegistryConfiguration;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.AlwaysExpiresExpirationPolicy;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.SpringBootDependencyInjectionTestExecutionListener;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CouchbaseTicketRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 * @deprecated 6.6
 */
@Tag("Couchbase")
@EnabledIfListeningOnPort(port = 8091)
@Import(CouchbaseTicketRegistryConfiguration.class)
@TestPropertySource(
    properties = {
        "cas.ticket.registry.couchbase.cluster-password=password",
        "cas.ticket.registry.couchbase.cluster-username=admin",
        "cas.ticket.registry.couchbase.scan-consistency=REQUEST_PLUS",
        "cas.ticket.registry.couchbase.bucket=testbucket"
    })
@Getter
@TestExecutionListeners({
    SpringBootDependencyInjectionTestExecutionListener.class,
    CouchbaseTicketRegistryTests.DisposingTestExecutionListener.class
})
@Deprecated(since = "6.6")
public class CouchbaseTicketRegistryTests extends BaseTicketRegistryTests {
    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry newTicketRegistry;

    @RepeatedTest(2)
    public void verifyAddAndLoadExpired() throws Exception {
        newTicketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
            CoreAuthenticationTestUtils.getAuthentication(),
            AlwaysExpiresExpirationPolicy.INSTANCE));
        val tickets = newTicketRegistry.getTickets();
        assertTrue(tickets.stream().noneMatch(t -> t.getId().equals(ticketGrantingTicketId)));
    }

    @RepeatedTest(1)
    public void verifyFails() {
        assertDoesNotThrow(() -> newTicketRegistry.addTicket((Ticket) null));
    }

    public static class DisposingTestExecutionListener implements TestExecutionListener {
        @Override
        public void afterTestClass(final TestContext testContext) throws Exception {
            var registry = testContext.getApplicationContext().getBean(TicketRegistry.BEAN_NAME, TicketRegistry.class);
            DisposableBean.class.cast(registry).destroy();
        }
    }
}
