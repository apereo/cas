package org.apereo.cas.util.cache;

import lombok.val;

import java.io.Closeable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * This is {@link DistributedCacheManager} that acts as a facade for a cache implementation.
 * It's designed via generics to accept a key, a value associated with that key and an output object.
 * While mostly value and output are one of the same, these are made separate intentionally
 * to avoid serialization issues and provide flexibility to provide transformations on the final result.
 *
 * @param <K> the type parameter
 * @param <V> the type parameter
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@FunctionalInterface
public interface DistributedCacheManager<K extends Serializable, V extends DistributedCacheObject> extends Closeable {

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
     * Gets all items in the cache.
     *
     * @return the all
     */
    default Collection<V> getAll() {
        return new ArrayList<>(0);
    }

    /**
     * Set item in the cache.
     *
     * @param key  the key
     * @param item the item to store in the cache
     */
    default void set(final K key, final V item) {
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
     * update key/item from the cache and overwrite.
     *
     * @param key  the key
     * @param item the item
     */
    default void update(final K key, final V item) {
    }

    /**
     * Remove key/item from the cache.
     *
     * @param key  the key
     * @param item the item
     */
    default void remove(final K key, final V item) {
    }

    /**
     * Gets the cache impl name.
     *
     * @return the name
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Find values matching this predicate.
     *
     * @param filter the filter
     * @return the collection
     */
    default Collection<V> findAll(final Predicate<V> filter) {
        return new ArrayList<>(0);
    }

    /**
     * Find values matching this predicate.
     *
     * @param filter the filter
     * @return the collection
     */
    default Optional<V> find(final Predicate<V> filter) {
        val results = findAll(filter);
        if (results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(results.iterator().next());
    }
}
