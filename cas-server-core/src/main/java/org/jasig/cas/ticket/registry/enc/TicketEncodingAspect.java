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

package org.jasig.cas.ticket.registry.enc;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.util.CipherExecutor;
import org.jasig.cas.util.CompressionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
@Aspect
public final class TicketEncodingAspect {
    /** Logger instance. */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final CipherExecutor cipherExecutor;

    /**
     * Flag to enable ticket encoding.
     * By default, encoding is enabled.
     */
    private boolean enabled = true;

    /**
     * Instantiates a new Ticket encoding aspect.
     * @param cipherExecutor the cipher executor
     */
    public TicketEncodingAspect(final CipherExecutor cipherExecutor) {
        this.cipherExecutor = cipherExecutor;
    }

    /**
     * Encodes sensitive ticket data prior to adding to the registry.
     *
     * @param pjp Proceeding join point.
     *
     * @throws Throwable On errors.
     */
    @Around("execution(void org.jasig.cas.ticket.registry.*TicketRegistry.addTicket(..))")
    public void encodeTicket(final ProceedingJoinPoint pjp) throws Throwable {
        final Ticket ticket = (Ticket) pjp.getArgs()[0];
        if (!this.enabled) {
            logger.info("Ticket encoding is disabled. Proceeding as usual...");
            pjp.proceed(pjp.getArgs());
        } else {
            logger.info("Encoding [{}]", ticket);

            final String encodedTicketObject = CompressionUtils.encodeObject(ticket, cipherExecutor);
            final String encodedTicketId = CompressionUtils.encodeBase64(ticket.getId().getBytes());
            final EncodedTicket encodedTicket = new EncodedTicket(ticket, encodedTicketObject, encodedTicketId);
            logger.info("Created [{}]", ticket);
            pjp.proceed(new Object[] {encodedTicket});
        }
    }

    /**
     * Encodes a ticket ID prior to deletion.
     *
     * @param pjp Proceeding join point.
     *
     * @throws Throwable On errors.
     * @return the result of the joinpoint
     */
    @Around("execution(boolean org.jasig.cas.ticket.registry.*TicketRegistry.deleteTicket(..))")
    public Object encodeTicketId(final ProceedingJoinPoint pjp) throws Throwable {
        if (!this.enabled) {
            logger.info("Ticket encoding is disabled. Proceeding as usual...");
            return pjp.proceed(pjp.getArgs());
        }

        final String ticketId = (String) pjp.getArgs()[0];
        final String encodedId = CompressionUtils.encodeObject(ticketId.getBytes(), cipherExecutor);
        logger.info("Encoded ticket id [{}] to [{}]", ticketId, encodedId);
        return pjp.proceed(new Object[] {encodedId});
    }

    /**
     * Decodes sensitive ticket data after retrieval from registry.
     *
     * @param pjp Proceeding join point.
     *
     * @throws Throwable On errors.
     * @return ticket or null
     */
    @Around("execution(* org.jasig.cas.ticket.registry.*TicketRegistry.getTicket(..))")
    public Object decodeTicket(final ProceedingJoinPoint pjp) throws Throwable {
        if (!this.enabled) {
            logger.info("Ticket encoding is disabled. Proceeding as usual...");
            return pjp.proceed(pjp.getArgs());
        }

        final String ticketId = (String) pjp.getArgs()[0];
        final Object[] encodedId = new Object[] {CompressionUtils.encodeObject(ticketId.getBytes(), this.cipherExecutor)};

        final Object result = pjp.proceed(encodedId);
        Ticket ticket = null;
        if (result != null) {
            logger.info("Attempting to decode [{}]",  result);
            if (result instanceof EncodedTicket) {
                final EncodedTicket encodedTicket = (EncodedTicket) result;

                ticket = CompressionUtils.decodeObject(encodedTicket.getEncoded(),
                        this.cipherExecutor, Ticket.class);
                logger.info("Decoded [{}]",  ticket);
            } else {
                throw new IllegalArgumentException("Expected EncodedTicket but was " + result);
            }
            return ticket;
        }
        logger.info("Cannot decode ticket id [{}] because it cannot be found in the registry. "
                + "The ticket may have expired/removed from the registry.", pjp.getArgs());
        return null;
    }

    /**
     * Decodes sensitive ticket data after retrieval from registry.
     *
     * @param pjp Proceeding join point.
     *
     * @throws Throwable On errors.
     * @return set of tickets decoded
     */
    @Around("execution(* org.jasig.cas.ticket.registry.*TicketRegistry.getTickets())")
    public Object decodeTickets(final ProceedingJoinPoint pjp) throws Throwable {
        final Collection<?> items = (Collection) pjp.proceed();

        if (!this.enabled) {
            logger.info("Ticket encoding is disabled. Proceeding as usual...");
            return items;
        }

        final Set<Ticket> tickets = new HashSet<>(items.size());

        for (final Object item : items) {
            if (item instanceof EncodedTicket) {
                final EncodedTicket encodedTicket = (EncodedTicket) item;
                final Ticket ticket = CompressionUtils.decodeObject(encodedTicket.getEncoded(),
                        this.cipherExecutor, Ticket.class);
                logger.info("Decoded [{}]",  ticket);
                tickets.add(ticket);
            } else {
                throw new IllegalArgumentException("Expected EncodedTicket");
            }
        }
        return tickets;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }
}
