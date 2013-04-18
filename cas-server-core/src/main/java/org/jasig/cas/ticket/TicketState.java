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

import org.jasig.cas.authentication.Authentication;

/**
 * @author Scott Battaglia

 * @since 3.0.5
 */
public interface TicketState {

    /**
     * Returns the number of times a ticket was used.
     *
     * @return the number of times the ticket was used.
     */
    int getCountOfUses();

    /**
     * Returns the last time the ticket was used.
     *
     * @return the last time the ticket was used.
     */
    long getLastTimeUsed();

    /**
     * Get the second to last time used.
     *
     * @return the previous time used.
     */

    long getPreviousTimeUsed();

    /**
     * Get the time the ticket was created.
     *
     * @return the creation time of the ticket.
     */
    long getCreationTime();

    /**
     * Authentication information from the ticket. This may be null.
     *
     * @return the authentication information.
     */
    Authentication getAuthentication();
}
