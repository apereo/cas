package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasIgniteTicketRegistryAutoConfiguration;
import org.apereo.cas.ha.ClusterTopologyManager;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.util.TicketGrantingTicketIdGenerator;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link IgniteTicketRegistry}.
 * <p>
 * Encryption is settled by the context rather than being written onto the registry bean before every
 * test method: the bean is shared, so methods running side by side would otherwise disagree about the
 * keys in force. There is no separate unencrypted run because the configuration starts an embedded
 * Ignite node on a fixed port, and a second context would be a second node contending for it.
 * <p>
 * The registry is also not emptied between methods. Ignite takes a table-level lock for an unqualified
 * {@code DELETE FROM}, which every concurrent read and write then conflicts with.
 *
 * @author Scott Battaglia
 * @author Timur Duehr timur.duehr@nccgroup.trust
 * @since 3.0.0
 */
@Tag("Ignite")
@Tag("TicketRegistryTestWithEncryption")
@Tag("SkipClearingTicketRegistry")
@ImportAutoConfiguration(CasIgniteTicketRegistryAutoConfiguration.class)
@TestPropertySource(
    properties = {
        "cas.ticket.registry.ignite.ignite-servers=localhost:47500",
        "cas.ticket.registry.ignite.initialize-cluster=true",
        "cas.ticket.registry.ignite.crypto.enabled=true"
    })
@Getter
class IgniteTicketRegistryTests extends BaseTicketRegistryTests {
    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry newTicketRegistry;

    @Autowired
    @Qualifier("igniteClusterTopologyManager")
    private ClusterTopologyManager igniteClusterTopologyManager;

    @Override
    protected boolean isCipherExecutorOwnedByContext() {
        return true;
    }

    @RepeatedTest(1)
    void verifyOperation() throws Exception {
        val results = igniteClusterTopologyManager.discoverMembers();
        assertFalse(results.isEmpty());
    }

    /**
     * Removing every session held for one principal must leave none behind for that principal.
     * The inherited version empties the registry and asserts it reports nothing at all, which
     * can only be true of a registry no other test is using -- and which, on Ignite, takes a
     * table lock that every concurrent operation then conflicts with.
     *
     * @throws Throwable in case of failure
     */
    @Override
    @RepeatedTest(2)
    void verifyGetTicketsIsZero() throws Throwable {
        val principal = UUID.randomUUID().toString();
        addSessionFor(principal);
        getNewTicketRegistry().deleteTicketsFor(principal);
        assertEquals(0, getNewTicketRegistry().countSessionsFor(principal));
    }

    /**
     * Removal reports how many it removed. Asked for one principal rather than for the whole
     * registry, which the inherited version empties and counts.
     *
     * @throws Throwable in case of failure
     */
    @Override
    @RepeatedTest(2)
    void verifyDeleteAllExistingTickets() throws Throwable {
        val principal = UUID.randomUUID().toString();
        addSessionFor(principal);
        assertEquals(1, getNewTicketRegistry().deleteTicketsFor(principal));
        assertEquals(0, getNewTicketRegistry().countSessionsFor(principal));
    }

    private void addSessionFor(final String principal) throws Throwable {
        val ticketGrantingTicketId = new TicketGrantingTicketIdGenerator(10, StringUtils.EMPTY)
            .getNewTicketId(TicketGrantingTicket.PREFIX);
        getNewTicketRegistry().addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
            CoreAuthenticationTestUtils.getAuthentication(principal), NeverExpiresExpirationPolicy.INSTANCE));
    }
}
