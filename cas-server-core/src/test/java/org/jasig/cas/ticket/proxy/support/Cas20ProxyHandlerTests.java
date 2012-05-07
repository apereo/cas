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

import java.net.URL;

import org.jasig.cas.authentication.principal.HttpBasedServiceCredentials;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.jasig.cas.util.HttpClient;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class Cas20ProxyHandlerTests extends TestCase {

    private Cas20ProxyHandler handler;

    protected void setUp() throws Exception {
        this.handler = new Cas20ProxyHandler();
        this.handler.setHttpClient(new HttpClient());
        this.handler.setUniqueTicketIdGenerator(new DefaultUniqueTicketIdGenerator());
    }

    public void testValidProxyTicketWithoutQueryString() throws Exception {
        assertNotNull(this.handler.handle(new HttpBasedServiceCredentials(
            new URL("http://www.rutgers.edu/")), "proxyGrantingTicketId"));
    }

    public void testValidProxyTicketWithQueryString() throws Exception {
        assertNotNull(this.handler.handle(new HttpBasedServiceCredentials(
            new URL("http://www.rutgers.edu/?test=test")),
            "proxyGrantingTicketId"));
    }

    public void testNonValidProxyTicket() throws Exception {
        final HttpClient httpClient = new HttpClient();
        httpClient.setAcceptableCodes(new int[] {900});
        this.handler.setHttpClient(httpClient);
        assertNull(this.handler.handle(new HttpBasedServiceCredentials(new URL(
            "http://www.rutgers.edu")), "proxyGrantingTicketId"));
    }
}
