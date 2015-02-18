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
package org.jasig.cas.ticket.proxy.support;

import org.jasig.cas.authentication.Credential;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.proxy.ProxyHandler;

/**
 * Dummy ProxyHandler that does nothing. Useful for Cas 1.0 compliance as CAS
 * 1.0 has no proxying capabilities.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public final class Cas10ProxyHandler implements ProxyHandler {

    @Override
    public String handle(final Credential credential,
        final TicketGrantingTicket proxyGrantingTicketId) {
        return null;
    }

    @Override
    public boolean canHandle(final Credential credential) {
        return false;
    }
}
