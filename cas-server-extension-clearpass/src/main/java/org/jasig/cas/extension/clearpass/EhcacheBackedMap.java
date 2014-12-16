/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.extension.clearpass;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * EhCache-backed implementation of a Map for caching a set of Strings.
 *
 * @author Scott Battaglia
 * @since 1.0
 */
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
        for (final Map.Entry<? extends String, ? extends String> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
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
        final Set<String> keys = keySet();
        final Collection<String> values = new ArrayList<>();

        for (final String key : keys) {
            final String value = get(key);
            if (value != null) {
                values.add(value);
            }
        }

        return values;
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        final Set<String> keys = keySet();
        final Set<Entry<String, String>> entries = new HashSet<>();

        for (final String key : keys) {
            final Element element = this.cache.get(key);

            if (element != null) {
                entries.add(new ElementMapEntry(element));
            }
        }

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
