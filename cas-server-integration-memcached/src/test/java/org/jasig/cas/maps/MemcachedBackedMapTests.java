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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Map;
import org.jasig.cas.maps.MemcachedBackedMap;
import org.jasig.cas.ticket.registry.support.MemcachedBaseTest;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Eric Domazlicky
 * @version $Revision$ $Date$ 
 */
public class MemcachedBackedMapTests extends MemcachedBaseTest {

	private MemcachedBackedMap memcachemap;
	private MemcachedClient client;

	@Before
	public void setUp() throws Exception {		
		super.setUp();
	
		try {					
			this.client = new MemcachedClient(AddrUtil.getAddresses(MemcachedServers));
			this.memcachemap = new MemcachedBackedMap(client,7200,"memcachedtests_");			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void addItem() {
		final String key = "MY_KEY";
		final String value = "MY_VALUE";
		this.memcachemap.put(key, value);
		assertEquals(value, this.memcachemap.get(key));
	}

	@Test
	public void addManyItems() {
		final int TOTAL_ITEMS = 100;

		for (int i = 0; i < TOTAL_ITEMS; i++)
			this.memcachemap.put("key" + i, "value" + i);
		

		for (int i = 0; i < TOTAL_ITEMS; i++) {			
			assertEquals("value" + i, this.memcachemap.get("key" + i));
		}
	}

	@Test
	public void addAndRemoveItem() {
		final String key1 = "MY_REALLY_KEY";
		final String value1 = "MY_VALUE";
		final String key2 = "MY_KEY2";
		final String value2 = "MY_VALUE2";

		this.memcachemap.put(key1, value1);
		this.memcachemap.put(key2, value2);
		assertEquals(value1, this.memcachemap.get(key1));
		assertEquals(value2, this.memcachemap.get(key2));		

		assertEquals(value1, this.memcachemap.remove(key1));
		assertEquals(value2, this.memcachemap.remove(key2));

		assertNull(this.memcachemap.get(key1));
		assertNull(this.memcachemap.get(key2));
	}

	@Test
	public void addNullKeyAndValue() {
		this.memcachemap.put(null, null);
		assertNull(this.memcachemap.get(null));
	}

	@Test
	public void addNullValue() {
		this.memcachemap.put("hello", null);
		assertNull(this.memcachemap.get("hello"));
	}
}
