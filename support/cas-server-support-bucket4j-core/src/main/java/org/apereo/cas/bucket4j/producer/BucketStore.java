package org.apereo.cas.bucket4j.producer;

import module java.base;
import io.github.bucket4j.AbstractBucket;

/**
 * This is {@link BucketStore}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@FunctionalInterface
public interface BucketStore {

    /**
     * Obtain bucket for the given key.
     *
     * @param key the key
     * @return the abstract bucket
     */
    AbstractBucket obtainBucket(String key);
}
