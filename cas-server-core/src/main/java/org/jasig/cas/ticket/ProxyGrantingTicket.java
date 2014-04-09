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
package org.jasig.cas.ticket;

/**
 * Interface for a proxy granting ticket. A proxy-granting ticket is an opaque string that is
 * used by a service to obtain proxy tickets for obtaining access to a back-end service on behalf of a client.
 * Proxy-granting tickets are obtained from CAS upon validation of a service ticket or a proxy ticket. 
 * 
 * @author Misagh Moayyed
 * @since 4.1
 */
public interface ProxyGrantingTicket extends TicketGrantingTicket {
    
    /** The prefix to use when generating an id for a proxy granting ticket. */
    String PREFIX = "PGT";
}
