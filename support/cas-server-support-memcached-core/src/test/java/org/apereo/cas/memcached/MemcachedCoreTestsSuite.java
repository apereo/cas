package org.apereo.cas.memcached;

import org.apereo.cas.memcached.kryo.CasKryoTranscoderTests;
import org.apereo.cas.memcached.kryo.ZonedDateTimeSerializerTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link MemcachedCoreTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    CasKryoTranscoderTests.class,
    ZonedDateTimeSerializerTests.class
})
public class MemcachedCoreTestsSuite {
}
