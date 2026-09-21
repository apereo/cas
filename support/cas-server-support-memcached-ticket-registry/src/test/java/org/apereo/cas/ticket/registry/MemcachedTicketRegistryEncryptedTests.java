package org.apereo.cas.ticket.registry;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * The same suite as {@link MemcachedTicketRegistryTests} against a registry that encrypts.
 * <p>
 * The keys are left unset on purpose: {@code crypto.enabled} alone has CAS generate them when the
 * cipher bean is built, which is once per context. Generating them per test method is what the
 * registry tests used to do, and is why concurrent methods could not read back what they wrote.
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
