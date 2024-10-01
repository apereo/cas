package org.apereo.cas.ticket.registry;

import org.apereo.cas.config.CasIgniteTicketRegistryAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.Getter;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.TestPropertySource;
import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyStore;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link IgniteTicketRegistry}.
 *
 * @author Scott Battaglia
 * @author Timur Duehr timur.duehr@nccgroup.trust
 * @since 3.0.0
 */
@Tag("Ignite")
@ImportAutoConfiguration(CasIgniteTicketRegistryAutoConfiguration.class)
@TestPropertySource(
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
class IgniteTicketRegistryTests extends BaseTicketRegistryTests {
    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry newTicketRegistry;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("igniteConfiguration")
    private IgniteConfiguration igniteConfiguration;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

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
    void verifyDeleteUnknown() throws Throwable {
        val catalog = mock(TicketCatalog.class);
        val registry = new IgniteTicketRegistry(CipherExecutor.noOp(), ticketSerializationManager, catalog, applicationContext,
            igniteConfiguration, casProperties.getTicket().getRegistry().getIgnite());
        registry.initialize();
        assertTrue(registry.deleteSingleTicket(new MockTicketGrantingTicket(RegisteredServiceTestUtils.getAuthentication())) > 0);
        registry.destroy();
    }
}
