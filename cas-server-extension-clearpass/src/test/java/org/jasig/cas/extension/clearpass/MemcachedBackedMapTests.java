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

import java.util.Arrays;
import java.io.IOException;
import java.util.Map;
import java.net.Socket;
import org.jasig.cas.extension.clearpass.MemcachedBackedMap;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Eric Domazlicky
 * @version $Revision$ $Date$
 * @since 1.0.6
 */
public class MemcachedBackedMapTests {

	private MemcachedBackedMap memcachemap;
	private MemcachedClient client;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Before
	public void setUp() throws Exception {
		
		// Memcached is a required external test fixture.
        // Abort tests if there is no memcached server available on localhost:11211.
        final boolean environmentOk = isMemcachedListening();
        if (!environmentOk) {
            logger.warn("Aborting test since no memcached server is available on localhost.");
        }
        Assume.assumeTrue(environmentOk);
	
		try {		
			String hostnames[] = {"localhost:11211"};
			this.client = new MemcachedClient(AddrUtil.getAddresses(Arrays.asList(hostnames)));
			this.memcachemap = new MemcachedBackedMap(client,7200,"memcachedtests_");			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	private boolean isMemcachedListening() {
        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", 11211);
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // Ignore errors on close
                }
            }
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
