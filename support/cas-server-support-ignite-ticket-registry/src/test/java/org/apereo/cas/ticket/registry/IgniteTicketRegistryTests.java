package org.apereo.cas.ticket.registry;

import lombok.Getter;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.IgniteTicketRegistryConfiguration;
import org.apereo.cas.config.IgniteTicketRegistryTicketCatalogConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.TicketGrantingTicketExpirationPolicy;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.time.Clock;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Unit test for {@link IgniteTicketRegistry}.
 *
 * @author Scott Battaglia
 * @author Timur Duehr timur.duehr@nccgroup.trust
 * @since 3.0.0
 */
@Tag("Ignite")
@SpringBootTest(classes = {
    IgniteTicketRegistryConfiguration.class,
    IgniteTicketRegistryTicketCatalogConfiguration.class,
    BaseTicketRegistryTests.SharedTestConfiguration.class
},
    properties = {
        "cas.ticket.registry.ignite.tickets-cache.write-synchronization-mode=FULL_ASYNC",
        "cas.ticket.registry.ignite.tickets-cache.atomicity-mode=ATOMIC",
        "cas.ticket.registry.ignite.tickets-cache.cache-mode=REPLICATED",
        "cas.ticket.registry.ignite.ignite-address[0]=localhost:47500",
        "cas.ticket.registry.ignite.key-store-file-path=${java.io.tmpdir}/ignite-keystore.jks",
        "cas.ticket.registry.ignite.key-store-type=pkcs12",
        "cas.ticket.registry.ignite.key-store-password=changeit",
        "cas.ticket.registry.ignite.key-algorithm=SunX509",
        "cas.ticket.registry.ignite.protocol=TLS",

        "cas.ticket.registry.ignite.trust-store-file-path=${java.io.tmpdir}/ignite-keystore.jks",
        "cas.ticket.registry.ignite.trust-store-password=changeit",
        "cas.ticket.registry.ignite.trust-store-type=pkcs12"
    })
@Getter
public class IgniteTicketRegistryTests extends BaseTicketRegistryTests {
    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry newTicketRegistry;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("igniteConfiguration")
    private IgniteConfiguration igniteConfiguration;

    @BeforeAll
    public static void beforeAll() throws Exception {
        val ks = KeyStore.getInstance("pkcs12");
        val password = "changeit".toCharArray();
        ks.load(null, password);
        try (val fos = new FileOutputStream(new File(FileUtils.getTempDirectory(), "ignite-keystore.jks"))) {
            ks.store(fos, password);
        }
    }

    @RepeatedTest(1)
    public void verifyDeleteUnknown() {
        val catalog = mock(TicketCatalog.class);
        val registry = new IgniteTicketRegistry(catalog, igniteConfiguration,
            casProperties.getTicket().getRegistry().getIgnite());
        assertTrue(registry.deleteSingleTicket("unknownticket"));
        registry.initialize();
        registry.destroy();
    }

    @Autowired
    private TicketCatalog ticketCatalog;

    @RepeatedTest(1)
    public void verifyDeleteKnownByTicketId() {

        final long maxTimeToLive = 42L;
        val expirationPolicy = new TicketGrantingTicketExpirationPolicy(maxTimeToLive, 23L);
        val ticketGrantingTicket = new TicketGrantingTicketImpl(TicketGrantingTicket.PREFIX + "-test", CoreAuthenticationTestUtils.getAuthentication(), expirationPolicy);
        expirationPolicy.setClock(Clock.fixed(ticketGrantingTicket.getCreationTime().plusSeconds(maxTimeToLive).plusNanos(1).toInstant(), ZoneOffset.UTC));
        assertTrue(ticketGrantingTicket.isExpired());

        val registry = new IgniteTicketRegistry(ticketCatalog, igniteConfiguration, casProperties.getTicket().getRegistry().getIgnite());
        registry.initialize();

        registry.addTicket(ticketGrantingTicket);
        val deletedTicketCount = registry.deleteTicket(ticketGrantingTicket.getId());

        registry.destroy();

        assertEquals(1, deletedTicketCount);
    }
}
