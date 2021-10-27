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
package org.jasig.cas.ticket.proxy;

import org.jasig.cas.authentication.principal.Credentials;

/**
 * Abstraction for what needs to be done to handle proxies. Useful because the
 * generic flow for all authentication is similar the actions taken for proxying
 * are different. One can swap in/out implementations but keep the flow of
 * events the same.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 * <p>
 * This is a published and supported CAS Server 3 API.
 * </p>
 */
public interface ProxyHandler {

    /**
     * Method to actually process the proxy request.
     * 
     * @param credentials The credentials of the item that will be proxying.
     * @param proxyGrantingTicketId The ticketId for the ProxyGrantingTicket (in
     * CAS 3 this is a TicketGrantingTicket)
     * @return the String value that needs to be passed to the CAS client.
     */
    String handle(Credentials credentials, String proxyGrantingTicketId);
}
