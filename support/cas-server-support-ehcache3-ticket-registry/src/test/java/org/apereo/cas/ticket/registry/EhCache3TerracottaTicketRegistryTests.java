package org.apereo.cas.ticket.registry;

import org.apereo.cas.config.Ehcache3TicketRegistryConfiguration;
import org.apereo.cas.config.Ehcache3TicketRegistryTicketCatalogConfiguration;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

/**
 * Unit test for {@link EhCache3TicketRegistry}.
 *
 * @author Hal Deadman
 * @since 6.2.0
 */
@Import({
    Ehcache3TicketRegistryConfiguration.class,
    Ehcache3TicketRegistryTicketCatalogConfiguration.class,
    BaseTicketRegistryTests.SharedTestConfiguration.class
})
@TestPropertySource(properties =
    "cas.ticket.registry.ehcache3.terracotta.terracotta-cluster-uri=terracotta://localhost:9410/cas-application")
@EnabledIfListeningOnPort(port = 9410)
@Tag("Ehcache")
@Getter
public class EhCache3TerracottaTicketRegistryTests extends BaseTicketRegistryTests {
    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry newTicketRegistry;
}
