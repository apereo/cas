package org.apereo.cas;

import org.apereo.cas.ticket.registry.MemcachedTicketRegistrySerialTranscoderTests;
import org.apereo.cas.ticket.registry.MemcachedTicketRegistryTests;
import org.apereo.cas.ticket.registry.MemcachedTicketRegistryWhalinTranscoderTests;
import org.apereo.cas.ticket.registry.MemcachedTicketRegistryWhalinV1TranscoderTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Test suite that runs all test in a batch.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SelectClasses({
    MemcachedTicketRegistryTests.class,
    MemcachedTicketRegistryWhalinTranscoderTests.class,
    MemcachedTicketRegistryWhalinV1TranscoderTests.class,
    MemcachedTicketRegistrySerialTranscoderTests.class
})
@Suite
public class CasMemcachedTestsSuite {
}
