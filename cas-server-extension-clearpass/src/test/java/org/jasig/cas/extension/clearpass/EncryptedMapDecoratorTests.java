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
package org.jasig.cas.extension.clearpass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.jasig.cas.extension.clearpass.EhcacheBackedMap;
import org.jasig.cas.extension.clearpass.EncryptedMapDecorator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 1.0.6
 */
public class EncryptedMapDecoratorTests {

	private Map<String, String>		map;

	private EncryptedMapDecorator	decorator;

	@Before
	public void setUp() throws Exception {
		try {
			final Cache cache = new Cache("name", 200, false, false, 100, 100);
			CacheManager.getInstance().addCache(cache);
			this.map = new EhcacheBackedMap(cache);
			this.decorator = new EncryptedMapDecorator(map);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@After
	public void tearDown() throws Exception {
		CacheManager.getInstance().removalAll();
	}

	@Test
	public void addItem() {
		final String key = "MY_KEY";
		final String value = "MY_VALUE";
		this.decorator.put(key, value);
		assertEquals(value, this.decorator.get(key));
		assertNull(this.map.get(key));
	}

	@Test
	public void addManyItems() {
		final int TOTAL_ITEMS = 100;

		for (int i = 0; i < TOTAL_ITEMS; i++)
			this.decorator.put("key" + i, "value" + i);

		assertEquals(this.decorator.size(), TOTAL_ITEMS);

		for (int i = 0; i < TOTAL_ITEMS; i++) {
			assertNull(this.map.get("key" + i));
			assertEquals("value" + i, this.decorator.get("key" + i));
		}
	}

	@Test
	public void addAndRemoveItem() {
		final String key1 = "MY_REALLY_KEY";
		final String value1 = "MY_VALUE";
		final String key2 = "MY_KEY2";
		final String value2 = "MY_VALUE2";

		this.decorator.put(key1, value1);
		this.decorator.put(key2, value2);
		assertEquals(value1, this.decorator.get(key1));
		assertEquals(value2, this.decorator.get(key2));
		assertNull(this.map.get(key1));
		assertNull(this.map.get(key2));

		assertEquals(value1, this.decorator.remove(key1));
		assertEquals(value2, this.decorator.remove(key2));

		assertNull(this.decorator.get(key1));
		assertNull(this.decorator.get(key2));
	}

	@Test
	public void addNullKeyAndValue() {
		this.decorator.put(null, null);
		assertNull(this.decorator.get(null));
	}

	@Test
	public void addNullValue() {
		this.decorator.put("hello", null);
		assertNull(this.decorator.get("hello"));
	}
}
