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
package org.jasig.cas;

import javax.validation.constraints.NotNull;

import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Abstraction on top of the ticket registries to help work with it.
 *
 * @author Jerome Leleu
 * @since 4.1.0
 */
public class StorageHelper {

    /** Log instance for logging events, info, warnings, errors, etc. */
    private final static Logger logger = LoggerFactory.getLogger(StorageHelper.class);
    
    /** TicketRegistry for storing and retrieving tickets as needed. */
    @NotNull
    private final TicketRegistry ticketRegistry;

    /** New Ticket Registry for storing and retrieving services tickets. Can point to the same one as the ticketRegistry variable. */
    @NotNull
    private final TicketRegistry serviceTicketRegistry;

    /**
     * Build from the underlying ticket registries.
     *
     * @param ticketRegistry the TGT ticket registry
     * @param serviceTicketRegistry the ST ticket registry
     */
    public StorageHelper(final TicketRegistry ticketRegistry, final TicketRegistry serviceTicketRegistry) {
        this.ticketRegistry = ticketRegistry;
        if (serviceTicketRegistry == null) {
            this.serviceTicketRegistry = ticketRegistry;
        } else {
            this.serviceTicketRegistry = serviceTicketRegistry;
        }
    }

    /**
     * Get a ticket granting ticket from an identifier.
     *
     * @param ticketId the identifier.
     * @return the retrieved ticket granting ticket.
     * @throws InvalidTicketException
     */
    public TicketGrantingTicket getTicketGrantingTicket(final String ticketId) throws InvalidTicketException {
        return getTicket(ticketId, TicketGrantingTicket.class);
    }

    /**
     * Get a service ticket from an identifier.
     *
     * @param serviceTicketId the identifier.
     * @return the retrieved service ticket.
     * @throws InvalidTicketException
     */
    public ServiceTicket getServiceTicket(final String serviceTicketId) throws InvalidTicketException {
        final ServiceTicket serviceTicket = this.serviceTicketRegistry.getTicket(serviceTicketId, ServiceTicket.class);

        if (serviceTicket == null || serviceTicket.isExpired()) {
            logger.debug("ServiceTicket [{}] has expired or cannot be found in the ticket registry", serviceTicketId);
            
            if (serviceTicket.isExpired()) {
                this.serviceTicketRegistry.deleteTicket(serviceTicketId);
            }

            throw new InvalidTicketException(serviceTicketId);
        }
        
        return serviceTicket;
    }

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    private <T extends Ticket> T getTicket(@NotNull final String ticketId, @NotNull final Class<? extends Ticket> clazz)
            throws InvalidTicketException {
        Assert.notNull(ticketId, "ticketId cannot be null");
        final Ticket ticket = this.ticketRegistry.getTicket(ticketId, clazz);

        if (ticket == null) {
            logger.debug("Ticket [{}] by type [{}] cannot be found in the ticket registry.", ticketId, clazz.getSimpleName());
            throw new InvalidTicketException(ticketId);
        }

        if (ticket instanceof TicketGrantingTicket) {
            synchronized (ticket) {
                if (ticket.isExpired()) {
                    this.ticketRegistry.deleteTicket(ticketId);
                    logger.debug("Ticket [{}] has expired and is now deleted from the ticket registry.", ticketId);
                    throw new InvalidTicketException(ticketId);
                }
            }
        }
        return (T) ticket;
    }

    /**
     * Add a ticket granting ticket in the registry.
     *
     * @param ticketGrantingTicket the ticket granting ticket to add.
     */
    public void addTicketGrantingTicket(final TicketGrantingTicket ticketGrantingTicket) {
        this.ticketRegistry.addTicket(ticketGrantingTicket);
    }

    /**
     * Add a service ticket in the registry.
     *
     * @param serviceTicket the service ticket to add.
     */
    public void addServiceTicket(final ServiceTicket serviceTicket) {
        this.serviceTicketRegistry.addTicket(serviceTicket);
    }    

    /**
     * Delete a ticket granting ticket from the registry.
     *
     * @param tgtId the identifier of the TGT to delete.
     */
    public void deleteTGT(final String tgtId) {
        this.ticketRegistry.deleteTicket(tgtId);
    }
}
