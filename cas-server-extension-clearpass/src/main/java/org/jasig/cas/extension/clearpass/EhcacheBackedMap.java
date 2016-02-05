package org.jasig.cas.extension.clearpass;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * EhCache-backed implementation of a Map for caching a set of Strings.
 *
 * @deprecated As of 4.1, use {@link org.jasig.cas.authentication.CacheCredentialsMetaDataPopulator} instead.
 * @author Scott Battaglia
 * @since 1.0
 */
@Deprecated
public final class EhcacheBackedMap implements Map<String, String> {

    @NotNull
    private final Cache cache;

    /**
     * Instantiates a new ehcache backed map.
     *
     * @param cache the cache
     */
    public EhcacheBackedMap(final Cache cache) {
        this.cache = cache;
    }

    @Override
    public int size() {
        return this.cache.getSize();
    }

    @Override
    public boolean isEmpty() {
        return this.cache.getSize() == 0;
    }

    @Override
    public boolean containsKey(final Object key) {
        return get(key) != null;
    }

    @Override
    public boolean containsValue(final Object value) {
        final Collection<String> col = values();
        return col.contains(value);
    }

    @Override
    public String get(final Object key) {
        final Element element = this.cache.get(key);

        return element == null ? null : (String) element.getValue();
    }

    @Override
    public String put(final String key, final String value) {
        this.cache.put(new Element(key, value));
        return value;
    }

    @Override
    public String remove(final Object key) {
        final String keyValue = get(key);
        this.cache.remove(key);
        return keyValue;
    }

    @Override
    public void putAll(final Map<? extends String, ? extends String> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        this.cache.removeAll();
    }

    @Override
    public Set<String> keySet() {
        return new HashSet<String>(this.cache.getKeys());
    }

    @Override
    public Collection<String> values() {
        return keySet().stream().map(this::get).filter(value -> value != null).collect(Collectors.toList());
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        final Set<Entry<String, String>> entries = new HashSet<>();

        keySet().stream().forEach(key -> {
            final Element element = this.cache.get(key);

            if (element != null) {
                entries.add(new ElementMapEntry(element));
            }
        });

        return entries;

    }

    protected static final class ElementMapEntry implements Map.Entry<String, String> {

        private final Element element;

        /**
         * Instantiates a new element map entry.
         *
         * @param element the element
         */
        public ElementMapEntry(final Element element) {
            this.element = element;
        }
        @Override
        public String getKey() {
            return (String) element.getKey();
        }

        @Override
        public String getValue() {
            return (String) element.getValue();
        }

        @Override
        public String setValue(final String value) {
            throw new UnsupportedOperationException("Operation Not Supported");
        }
    }
}
