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
package org.jasig.cas.ticket.registry;

import org.jasig.cas.ticket.Ticket;

import java.util.Collection;

/**
 * Interface for a ticket registry that allows retrieving tickets from it in batches.
 *
 * @author Ahsan Rabbani
 * @since 4.0
 */
public interface BatchableTicketRegistry extends TicketRegistry {

    /**
     * Retrieve a batch of TicketGrantingTickets starting from the given offset. Ordering is determined by the
     * implementing class.
     *
     * @param offset    the number of rows to skip from the start of the result set
     * @param batchSize the number of tickets to retrieve in the batch. The actual number returned could be less.
     * @return collection of tickets currently stored in the registry. Tickets might or might not be valid i.e. expired.
     */
    Collection<Ticket> getTicketGrantingTicketBatch(int offset, int batchSize);

    /**
     * Retrieve a batch of ServiceTickets starting from the given offset. Ordering is determined by the implementing
     * class.
     *
     * @param offset    the number of rows to skip from the start of the result set
     * @param batchSize the number of tickets to retrieve in the batch. The actual number returned could be less.
     * @return collection of tickets currently stored in the registry. Tickets might or might not be valid i.e. expired.
     */
    Collection<Ticket> getServiceTicketBatch(int offset, int batchSize);

}
