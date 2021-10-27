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
package org.jasig.cas.ticket.proxy.support;

import static org.junit.Assert.*;

import java.net.URL;

import org.jasig.cas.authentication.HttpBasedServiceCredential;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.jasig.cas.util.SimpleHttpClient;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Scott Battaglia

 * @since 3.0
 */
public class Cas20ProxyHandlerTests {

    private Cas20ProxyHandler handler;

    @Before
    public void setUp() throws Exception {
        this.handler = new Cas20ProxyHandler();
        this.handler.setHttpClient(new SimpleHttpClient());
        this.handler.setUniqueTicketIdGenerator(new DefaultUniqueTicketIdGenerator());
    }

    @Test
    public void testValidProxyTicketWithoutQueryString() throws Exception {
        assertNotNull(this.handler.handle(new HttpBasedServiceCredential(
            new URL("http://www.rutgers.edu/")), "proxyGrantingTicketId"));
    }

    @Test
    public void testValidProxyTicketWithQueryString() throws Exception {
        assertNotNull(this.handler.handle(new HttpBasedServiceCredential(
            new URL("http://www.rutgers.edu/?test=test")),
            "proxyGrantingTicketId"));
    }

    @Test
    public void testNonValidProxyTicket() throws Exception {
        final SimpleHttpClient httpClient = new SimpleHttpClient();
        httpClient.setAcceptableCodes(new int[] {900});
        this.handler.setHttpClient(httpClient);
        assertNull(this.handler.handle(new HttpBasedServiceCredential(new URL(
            "http://www.rutgers.edu")), "proxyGrantingTicketId"));
    }
}
