package org.apereo.cas;

import org.apereo.cas.bucket4j.consumer.DefaultBucketConsumerTests;
import org.apereo.cas.bucket4j.producer.BucketProducerTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    DefaultBucketConsumerTests.class,
    BucketProducerTests.class
})
@Suite
public class AllTestsSuite {
}
