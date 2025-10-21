package org.apereo.cas.ticket.registry.key;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.config.CasRedisCoreAutoConfiguration;
import org.apereo.cas.config.CasRedisTicketRegistryAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.ticket.registry.BaseTicketRegistryTests;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultRedisKeyGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@EnabledIfListeningOnPort(port = 6379)
@Tag("Redis")
@SpringBootTest(
    classes = BaseTicketRegistryTests.SharedTestConfiguration.class,
    properties = {
        "cas.ticket.tgt.core.service-tracking-policy=ALL",
        "cas.ticket.registry.cleaner.schedule.enabled=false"
    })
@ExtendWith(CasTestExtension.class)
@ImportAutoConfiguration({
    CasRedisCoreAutoConfiguration.class,
    CasRedisTicketRegistryAutoConfiguration.class
})
class DefaultRedisKeyGeneratorTests {
    @Autowired
    @Qualifier("redisKeyGeneratorFactory")
    private RedisKeyGeneratorFactory redisKeyGeneratorFactory;

    @ParameterizedTest
    @ValueSource(strings = {
        TicketGrantingTicket.PREFIX,
        ServiceTicket.PREFIX,
        ProxyTicket.PROXY_TICKET_PREFIX,
        ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX
    })
    void verifyTickets(final String prefix) {
        assertFalse(redisKeyGeneratorFactory.getRedisKeyGenerators().isEmpty());
        val generator = redisKeyGeneratorFactory.getRedisKeyGenerator(prefix).orElseThrow();
        assertTrue(generator.isTicketKeyGenerator());

        val id = UUID.randomUUID().toString();
        val ticketKey = generator.forPrefixAndId(prefix, id);
        assertNotNull(ticketKey);
        assertTrue(ticketKey.startsWith(generator.getKeyspace()));
        assertTrue(ticketKey.endsWith(id));

        val rawKey = generator.rawKey(ticketKey);
        assertEquals(rawKey, id);
        assertNotNull(generator.getKeyspace());
        assertEquals(RedisKeyGenerator.REDIS_NAMESPACE_TICKETS, generator.getNamespace());
        assertThrows(IllegalArgumentException.class, () -> RedisKeyGenerator.parse(id));

        val parsed = RedisKeyGenerator.parse(ticketKey);
        assertEquals(generator.getNamespace(), parsed.getNamespace());
        assertEquals(generator.getPrefix(), parsed.getPrefix());
        assertEquals(id, parsed.getId());
    }

    @Test
    void verifyPrincipals() {
        val generator = redisKeyGeneratorFactory.getRedisKeyGenerator(Principal.class.getName()).orElseThrow();
        assertFalse(generator.isTicketKeyGenerator());
        val id = UUID.randomUUID().toString();
        val principalKey = generator.forId(id);
        assertNotNull(principalKey);
        assertTrue(principalKey.startsWith(generator.getNamespace()));
        assertTrue(principalKey.endsWith(id));
        val parsed = RedisKeyGenerator.parse(principalKey);
        assertEquals(generator.getNamespace(), parsed.getNamespace());
        assertEquals(id, parsed.getId());
    }
}
