package org.apereo.cas;

import java.io.Closeable;

/**
 * This is {@link DistributedCacheManager} that acts as a facade for a cache implementation.
 * It's designed via generics to accept a key, a value associated with that key and an output object.
 * While mostly value and output are one of the same, these are made separate intentionally
 * to avoid serialization issues and provide flexibility to provide transformations on the final result.
 *
 * @param <K> the type parameter
 * @param <V> the type parameter
 * @param <C> the type parameter
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@FunctionalInterface
public interface DistributedCacheManager<K, V, C> extends Closeable {

    /**
     * Get item.
     *
     * @param key the key
     * @return the item or null if not found.
     */
    default V get(final K key) {
        return null;
    }

    /**
     * Set item in the cache.
     *
     * @param key  the key
     * @param item the item to store in the cache
     */
    default void set(final K key, final C item) {
    }

    /**
     * Contains key in the cache?
     *
     * @param key the key
     * @return true /false
     */
    default boolean contains(final K key) {
        return false;
    }

    /**
     * Remove key/item from the cache.
     *
     * @param key the key
     */
    default void remove(final K key) {
    }

    /**
     * Gets the cache impl name.
     *
     * @return the name
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
}
