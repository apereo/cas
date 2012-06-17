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

import java.io.Serializable;

/**
 * Interface for the generic concept of a ticket.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public interface Ticket extends Serializable {

    /**
     * Method to retrieve the id.
     * 
     * @return the id
     */
    String getId();

    /**
     * Determines if the ticket is expired. Most common implementations might
     * collaborate with <i>ExpirationPolicy </i> strategy.
     * 
     * @see org.jasig.cas.ticket.ExpirationPolicy
     */
    boolean isExpired();

    /**
     * Method to retrive the TicketGrantingTicket that granted this ticket.
     * 
     * @return the ticket or null if it has no parent
     */
    TicketGrantingTicket getGrantingTicket();

    /**
     * Method to return the time the Ticket was created.
     * 
     * @return the time the ticket was created.
     */
    long getCreationTime();
    
    /**
     * Returns the number of times this ticket was used.
     * @return
     */
    int getCountOfUses();
}
