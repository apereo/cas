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
package org.jasig.cas.ticket.registry.support;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import org.junit.Assume; 
import net.spy.memcached.AddrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 /**
 * Utility functions for Memcached tests 
 * @author Eric Domazlicky
 * @version $Revision$ $Date$ 
 */
public class MemcachedBaseTest {

	protected ApplicationContext context;
	protected String MemcachedServers;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public void setUp() throws Exception {		
		context = new ClassPathXmlApplicationContext("/ticketRegistry-test.xml");
		MemcachedServers = (String)context.getBean("MemcachedServers");
		// Memcached is a required external test fixture.
        // Abort tests if one ore more memcached servers are unavailable
        final boolean environmentOk = isMemcachedListening(MemcachedServers);
        if (!environmentOk) {
            logger.warn("Aborting test since one or more configured memcached servers are unavailable");
        }
        Assume.assumeTrue(environmentOk);
	}

	// check to see if Memcached is listening on all configured servers
	public static boolean isMemcachedListening(String MemcachedServers) {
		List<InetSocketAddress> hosts = AddrUtil.getAddresses(MemcachedServers);
		boolean allListening = true;
		for(InetSocketAddress host : hosts) {			
			Socket socket = null;
			try {
				socket = new Socket(host.getHostName(), host.getPort());
				//return true;
			} catch (Exception e) {
				allListening = false;
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
		return allListening;
    }

}