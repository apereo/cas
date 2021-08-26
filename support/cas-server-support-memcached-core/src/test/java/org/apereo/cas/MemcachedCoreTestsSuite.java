package org.apereo.cas;

import org.apereo.cas.memcached.MemcachedPooledClientConnectionFactoryTests;
import org.apereo.cas.memcached.MemcachedUtilsTests;
import org.apereo.cas.memcached.kryo.CasKryoPoolTests;
import org.apereo.cas.memcached.kryo.CasKryoTranscoderTests;
import org.apereo.cas.memcached.kryo.ZonedDateTimeSerializerTests;
import org.apereo.cas.memcached.kryo.serial.ImmutableNativeJavaListSerializerTests;
import org.apereo.cas.memcached.kryo.serial.ImmutableNativeJavaMapSerializerTests;
import org.apereo.cas.memcached.kryo.serial.ImmutableNativeJavaSetSerializerTests;
import org.apereo.cas.memcached.kryo.serial.URLSerializerTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link MemcachedCoreTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SelectClasses({
    CasKryoTranscoderTests.class,
    MemcachedUtilsTests.class,
    URLSerializerTests.class,
    CasKryoPoolTests.class,
    ImmutableNativeJavaMapSerializerTests.class,
    ImmutableNativeJavaListSerializerTests.class,
    ImmutableNativeJavaSetSerializerTests.class,
    MemcachedPooledClientConnectionFactoryTests.class,
    ZonedDateTimeSerializerTests.class
})
@Suite
public class MemcachedCoreTestsSuite {
}
