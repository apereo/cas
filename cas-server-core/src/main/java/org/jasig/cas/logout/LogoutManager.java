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
package org.jasig.cas.logout;

import java.util.List;

import org.jasig.cas.ticket.TicketGrantingTicket;

/**
 * A logout manager handles the Single Log Out process.
 *
 * @author Jerome Leleu
 * @since 4.0.0
 */
public interface LogoutManager {

    /**
     * Perform a back channel logout for a given ticket granting ticket and returns all the logout requests.
     *
     * @param ticket a given ticket granting ticket.
     * @return all logout requests.
     */
    List<LogoutRequest> performLogout(TicketGrantingTicket ticket);

    /**
     * Create a logout message for front channel logout.
     *
     * @param logoutRequest the logout request.
     * @return a front SAML logout message.
     */
    String createFrontChannelLogoutMessage(LogoutRequest logoutRequest);
}
