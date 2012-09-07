/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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

package org.jasig.cas.maps;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.CacheMap;
import net.spy.memcached.MemcachedClient;


/**
 * Memcached-backed implementation of a Map for caching a set of Strings.
 *
 * @author Eric Domazlicky
 * @version $Revision$ $Date$
 * @since 1.0
 */
public class MemcachedBackedMap   implements Map<String,String> {

	private final CacheMap cacheMap;
	
	private final int mapTimeOut;
	private final String mapPrefix;
	private final static String nullPlaceholder = "#null#";
	private final static String MemcachedNotSupported = "This operation is not supported on an Memcache-backed Map";
	
	 /** Memcached client */
    @NotNull
    private final MemcachedClient client;
	
	public MemcachedBackedMap(final MemcachedClient client, final int mapTimeOut,final String mapPrefix) {		
		this.mapTimeOut = mapTimeOut;
		this.mapPrefix = mapPrefix;
        this.client = client;
		cacheMap = new CacheMap(client,mapTimeOut,mapPrefix);       
	}
	
	private String replaceIfNull(final String value) {
		if(value==null)
			return nullPlaceholder;		
			
		return value;
	}
	
	private String checkIfNull(final String value) {
		if(value==null)
			return null;
		else if(value.equals(nullPlaceholder))
			return null;
			
		return value;
	}
	
	
	public int size() {
        throw new UnsupportedOperationException(MemcachedNotSupported);
    }

    public boolean isEmpty() {
        throw new UnsupportedOperationException(MemcachedNotSupported);
    }

    public boolean containsKey(final Object key) {
        return cacheMap.get(key) != null;
    }

    public boolean containsValue(final Object value) {
        throw new UnsupportedOperationException(MemcachedNotSupported);
    }
	
	public void putAll(final Map<? extends String, ? extends String> m) {
        for (final Map.Entry<? extends String, ? extends String> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public String get(final Object key) {
        return checkIfNull((String)this.cacheMap.get(replaceIfNull((String)key)));
    }

    public String put(final String key, final String value) {
        this.cacheMap.put(replaceIfNull(key),replaceIfNull(value));
        return value;
    }
	
	public void clear() {
		throw new UnsupportedOperationException(MemcachedNotSupported);
	}
	
	public Set<String> keySet() {
		throw new UnsupportedOperationException(MemcachedNotSupported);
	}

    public String remove(final Object key) {        
		String retvalue = get(key);
        this.cacheMap.remove(replaceIfNull((String)key));
        return retvalue;
    }
	
	public Collection<String> values() {
	    throw new UnsupportedOperationException(MemcachedNotSupported);
	}
	
	 public Set<Entry<String, String>> entrySet() {
        throw new UnsupportedOperationException(MemcachedNotSupported);
    }


}