package org.apereo.cas.ticket.registry;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link MemcachedTicketRegistryEncryptedTests}.
 *
 * @author Middleware Services
 * @since 3.0.0
 * @deprecated Since 7.0.0
 */
@Tag("TicketRegistryTestWithEncryption")
@TestPropertySource(properties = "cas.ticket.registry.memcached.crypto.enabled=true")
@Deprecated(since = "7.0.0")
class MemcachedTicketRegistryEncryptedTests extends BaseMemcachedTicketRegistryTests {
}
