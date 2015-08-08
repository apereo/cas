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
package org.jasig.cas.ticket.proxy;

import org.jasig.cas.authentication.Credential;
import org.jasig.cas.ticket.TicketGrantingTicket;

/**
 * Abstraction for what needs to be done to handle proxies. Useful because the
 * generic flow for all authentication is similar the actions taken for proxying
 * are different. One can swap in/out implementations but keep the flow of
 * events the same.
 *
 * @author Scott Battaglia
 * @author Misagh Moayyed
 * @since 3.0.0
 */
public interface ProxyHandler {

    /**
     * Method to actually process the proxy request.
     *
     * @param credential The credential of the item that will be proxying.
     * @param proxyGrantingTicketId The ticketId for the PGT (which really is a TGT)
     * @return the String value that needs to be passed to the CAS client.
     */
    String handle(Credential credential, TicketGrantingTicket proxyGrantingTicketId);
    
    /**
     * Whether this handler can support the proxy request identified by the given credentials.
     *
     * @param credential the credential object containing the proxy request details.
     * @return true, if successful
     */
    boolean canHandle(Credential credential);
}
