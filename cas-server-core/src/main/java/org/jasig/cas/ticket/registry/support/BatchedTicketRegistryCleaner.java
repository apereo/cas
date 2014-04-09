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
package org.jasig.cas.ticket.registry.support;

import org.apache.commons.collections.CollectionUtils;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.registry.BatchableTicketRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.Collection;

/**
 * This class tries to address the issue with ${@link DefaultTicketRegistryCleaner} when used in conjunction with a
 * ${@link org.jasig.cas.ticket.registry.JpaTicketRegistry} discussed in the below thread by processing the tickets in
 * the registry in batches. This should prevent the CAS server from running out of memory in certain scenarios (ie when
 * a large number of tickets get created in a short period of time) when the ticket registry cleaner runs.
 *
 * http://jasig.275507.n4.nabble.com/JpaTicketRegistry-A-Sinking-Ship-td4256973.html
 *
 * @author Ahsan Rabbani
 * @since 4.0
 */
public class BatchedTicketRegistryCleaner extends AbstractTicketRegistryCleaner {

    /** The Commons Logging instance. */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final int DEFAULT_BATCH_SIZE = 2000;

    private int batchSize = DEFAULT_BATCH_SIZE;

    @PostConstruct
    public void init() {
        Assert.isInstanceOf(BatchableTicketRegistry.class, ticketRegistry, "ticketRegistry must be of type BatchableTicketRegistry");
    }

    @Override
    public void clean() {
        logger.info("Beginning ticket cleanup.");

        logger.debug("Attempting to acquire ticket cleanup lock.");
        if (!lock.acquire()) {
            logger.info("Could not obtain lock.  Aborting cleanup.");
            return;
        }
        logger.debug("Acquired lock.  Proceeding with cleanup.");

        try {
            logger.info("Processing TicketGrantingTickets for cleanup.");
            int numTicketsRemoved = cleanTickets(
                    new BatchedTicketRetriever() {
                        @Override
                        public Collection<Ticket> getBatch(final int offset, final int batchSize) {
                            return ((BatchableTicketRegistry) ticketRegistry).getTicketGrantingTicketBatch(offset, batchSize);
                        }
                    }
            );
            logger.info("{} total TicketGrantingTickets removed.", numTicketsRemoved);

            logger.info("Processing ServiceTickets for cleanup.");
            numTicketsRemoved = cleanTickets(
                    new BatchedTicketRetriever() {
                        @Override
                        public Collection<Ticket> getBatch(final int offset, final int batchSize) {
                            return ((BatchableTicketRegistry) ticketRegistry).getServiceTicketBatch(offset, batchSize);
                        }
                    }
            );
            logger.info("{} total ServiceTickets removed.", numTicketsRemoved);
        } finally {
            logger.debug("Releasing ticket cleanup lock.");
            lock.release();
        }

        logger.info("Finished ticket cleanup.");
    }

    int cleanTickets(final BatchedTicketRetriever ticketRetriever) {
        int offset = 0;
        int numTicketsRemoved = 0;

        Collection<Ticket> ticketBatch;
        while (CollectionUtils.isNotEmpty(ticketBatch = ticketRetriever.getBatch(offset, batchSize))) {
            int ticketsRemovedFromBatch = registryCleanerHelper.deleteExpiredTickets(
                    ticketRegistry,
                    logoutManager,
                    ticketBatch,
                    logUserOutOfServices
            );
            offset += (ticketBatch.size() - ticketsRemovedFromBatch);
            numTicketsRemoved += ticketsRemovedFromBatch;
        }

        return numTicketsRemoved;
    }

    interface BatchedTicketRetriever {
        Collection<Ticket> getBatch(int offset, int batchSize);
    }

    /**
     * Batch size to use when retrieving tickets from the registry to process. The default is 2000.
     *
     * @param batchSize batch size to use when retrieving tickets from the registry
     */
    public void setBatchSize(final int batchSize) {
        this.batchSize = batchSize;
    }

}
