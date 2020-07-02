package org.apereo.cas.util.cache;

import org.apereo.cas.util.PublisherIdentifier;

import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This is {@link MappableDistributedCacheManager}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiredArgsConstructor
public class MappableDistributedCacheManager<K extends Serializable, V extends DistributedCacheObject>
    extends BaseDistributedCacheManager<K, V> {

    /**
     * (Distributed) map instances that holds the data.
     */
    protected final Map<String, V> mapInstance;

    @Override
    public V get(final K key) {
        if (contains(key)) {
            val cacheKey = buildKey(key);
            return this.mapInstance.get(cacheKey);
        }
        return null;
    }

    @Override
    public Collection<V> getAll() {
        return this.mapInstance.values();
    }

    @Override
    public DistributedCacheManager<K, V, PublisherIdentifier> set(final K key,
                                                                  final V item,
                                                                  final boolean publish) {
        this.mapInstance.put(buildKey(key), item);
        return this;
    }

    @Override
    public boolean contains(final K key) {
        return this.mapInstance.containsKey(buildKey(key));
    }

    @Override
    public DistributedCacheManager<K, V, PublisherIdentifier> update(final K key, final V item,
                                                                     final boolean publish) {
        remove(key, item, publish);
        set(key, item, publish);
        return this;
    }

    @Override
    public DistributedCacheManager<K, V, PublisherIdentifier> remove(final K key, final V item,
                                                                     final boolean publish) {
        val cacheKey = buildKey(key);
        this.mapInstance.remove(cacheKey);
        return this;
    }

    @Override
    public Collection<V> findAll(final Predicate<V> filter) {
        return getAll().stream().filter(filter).collect(Collectors.toList());
    }

    protected String buildKey(final K key) {
        return key.toString();
    }
}
