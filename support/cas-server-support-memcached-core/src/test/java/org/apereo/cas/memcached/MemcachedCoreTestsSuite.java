package org.apereo.cas.memcached;

import org.apereo.cas.memcached.kryo.CasKryoTranscoderTests;
import org.apereo.cas.memcached.kryo.ZonedDateTimeSerializerTests;
import org.apereo.cas.memcached.kryo.serial.ImmutableNativeJavaListSerializerTests;
import org.apereo.cas.memcached.kryo.serial.ImmutableNativeJavaMapSerializerTests;
import org.apereo.cas.memcached.kryo.serial.ImmutableNativeJavaSetSerializerTests;
import org.apereo.cas.memcached.kryo.serial.URLSerializerTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

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
    ImmutableNativeJavaMapSerializerTests.class,
    ImmutableNativeJavaListSerializerTests.class,
    ImmutableNativeJavaSetSerializerTests.class,
    MemcachedPooledClientConnectionFactoryTests.class,
    ZonedDateTimeSerializerTests.class
})
@RunWith(JUnitPlatform.class)
public class MemcachedCoreTestsSuite {
}
