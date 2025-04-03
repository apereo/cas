package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCosmosDbTicketRegistryAutoConfiguration;
import org.apereo.cas.cosmosdb.CosmosDbObjectFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.registry.BaseTicketRegistryTests;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.TicketGrantingTicketIdGenerator;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CosmosDbTicketRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Azure")
@ImportAutoConfiguration(CasCosmosDbTicketRegistryAutoConfiguration.class)
@TestPropertySource(properties = {
    "cas.tgc.crypto.enabled=false",
    "cas.http-client.host-name-verifier=none",
    "cas.ticket.registry.cosmos-db.uri=${#environmentVariables['COSMOS_DB_URL']}",
    "cas.ticket.registry.cosmos-db.key=${#environmentVariables['COSMOS_DB_KEY']}",
    "cas.ticket.registry.cosmos-db.database=CasTicketRegistryDb",
    "cas.ticket.registry.cosmos-db.database-throughput=1000",
    "cas.ticket.registry.cosmos-db.max-retry-attempts-on-throttled-requests=5",
    "cas.ticket.registry.cosmos-db.indexing-mode=CONSISTENT"
})
@ResourceLock("cosmosdb-tickets")
@Getter
@Execution(ExecutionMode.SAME_THREAD)
@EnabledIfEnvironmentVariable(named = "COSMOS_DB_URL", matches = ".+")
@EnabledIfEnvironmentVariable(named = "COSMOS_DB_KEY", matches = ".+")
class CosmosDbTicketRegistryTests extends BaseTicketRegistryTests {
    private static final int COUNT = 10;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry newTicketRegistry;

    @RepeatedTest(1)
    @Tag("TicketRegistryTestWithEncryption")
    void verifyLargeDataset() {
        val ticketGrantingTickets = Stream.generate(() -> {
            val tgtId = new TicketGrantingTicketIdGenerator(10, StringUtils.EMPTY)
                .getNewTicketId(TicketGrantingTicket.PREFIX);
            return new TicketGrantingTicketImpl(tgtId,
                CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        }).limit(COUNT);

        var stopwatch = new StopWatch();
        stopwatch.start();
        newTicketRegistry.addTicket(ticketGrantingTickets);
        val size = newTicketRegistry.getTickets().size();
        stopwatch.stop();
        assertEquals(COUNT, size);
        var time = stopwatch.getTime(TimeUnit.SECONDS);
        assertTrue(time <= 20);
    }

    @AfterAll
    public static void shutdown() {
        val factory = ApplicationContextProvider.getApplicationContext().getBean(CosmosDbObjectFactory.class);
        factory.dropDatabase();
    }
}
