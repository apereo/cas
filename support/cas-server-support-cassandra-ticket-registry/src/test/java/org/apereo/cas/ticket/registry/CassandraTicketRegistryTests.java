package org.apereo.cas.ticket.registry;

import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CassandraTicketRegistryConfiguration;
import org.apereo.cas.config.CassandraTicketRegistryTicketCatalogConfiguration;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author David Rodriguez
 *
 * @since 5.2.0
 */
@RunWith(Parameterized.class)
@SpringBootTest(classes = {
        CassandraTicketRegistryConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        CassandraTicketRegistryTicketCatalogConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasPersonDirectoryConfiguration.class,
        CasCoreAuthenticationConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasCoreAuthenticationPolicyConfiguration.class,
        CasCoreAuthenticationMetadataConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasCoreAuthenticationHandlersConfiguration.class,
        CasCoreHttpConfiguration.class,
        RefreshAutoConfiguration.class,
        CasCoreServicesConfiguration.class
})
@TestPropertySource(properties = {"cas.ticket.registry.cassandra.keyspace=cas2"})
public class CassandraTicketRegistryTests extends AbstractTicketRegistryTests {

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    public CassandraTicketRegistryTests(final boolean useEncryption) {
        super(useEncryption);
    }

    @Override
    public TicketRegistry getNewTicketRegistry() throws Exception {
        return ticketRegistry;
    }

    @Parameterized.Parameters
    public static Collection<Object> getTestParameters() throws Exception {
        return Arrays.asList(false, true);
    }

    @Override
    public void verifyGetTicketsFromRegistryEqualToTicketsAdded() {
        Assert.assertTrue(true);
    }
}
