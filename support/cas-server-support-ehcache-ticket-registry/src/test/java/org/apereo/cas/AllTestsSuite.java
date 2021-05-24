package org.apereo.cas;

import org.apereo.cas.ticket.registry.CachesEndpointTests;
import org.apereo.cas.ticket.registry.EhCacheTicketRegistryTests;
import org.junit.jupiter.api.Tag;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;


/**
 * Test suite that runs all test in a batch.
 *
 * @author Hal Deadman
 * @since 6.1.0
 */
@SelectClasses({
    CachesEndpointTests.class,
    EhCacheTicketRegistryTests.class
})
@Tag("Ehcache")
@Suite
public class AllTestsSuite {
}
