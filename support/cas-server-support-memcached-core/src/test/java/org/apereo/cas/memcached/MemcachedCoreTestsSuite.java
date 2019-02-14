package org.apereo.cas.memcached;

import org.apereo.cas.memcached.kryo.CasKryoTranscoderTests;
import org.apereo.cas.memcached.kryo.ZonedDateTimeSerializerTests;

import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link MemcachedCoreTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SelectClasses({
    CasKryoTranscoderTests.class,
    ZonedDateTimeSerializerTests.class
})
public class MemcachedCoreTestsSuite {
}
