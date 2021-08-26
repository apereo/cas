package org.apereo.cas.ticket.registry;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link MemcachedTicketRegistrySerialTranscoderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@EnabledIfPortOpen(port = 11211)
@Tag("Memcached")
@TestPropertySource(properties = "cas.ticket.registry.memcached.transcoder=SERIAL")
public class MemcachedTicketRegistrySerialTranscoderTests extends MemcachedTicketRegistryTests {
}
