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

package org.jasig.cas.ticket.enc;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.jasig.cas.ticket.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ticket implementation that encodes a source ticket and stores the encoded
 * representation internally such that the original ticket can be produced by
 * calling the {@link org.jasig.cas.ticket.enc.ReversibleEncoder#decode(String)} ()} method.
 *
 * @author Marvin S. Addison
 * @since 4.1
 */
@Aspect
public final class TicketEncodingAspect {
    /** Logger instance. */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** Performs the encoding/decoding work. */
    @NotNull
    private ReversibleEncoder encoder;

    /**
     * Encodes sensitive ticket data prior to adding to the registry.
     *
     * @param pjp Proceeding join point.
     *
     * @throws Throwable On errors.
     */
    @Around("execution(void org.jasig.cas.ticket.registry.*TicketRegistry.addTicket(Ticket))")
    public void encodeTicket(final ProceedingJoinPoint pjp) throws Throwable {
        Ticket ticket = (Ticket) pjp.getArgs()[0];
        logger.debug("Encoding [{}]",  ticket);
        ticket = new EncodedTicket(this.encoder, ticket);
        logger.debug("Created [{}]",  ticket);
        pjp.proceed(new Object[] {ticket});
    }

    /**
     * Encodes a ticket ID prior to deletion.
     *
     * @param pjp Proceeding join point.
     *
     * @throws Throwable On errors.
     * @return the result of the joinpoint
     */
    @Around("execution(boolean org.jasig.cas.ticket.registry.*TicketRegistry.deleteTicket(String))")
    public Object encodeTicketId(final ProceedingJoinPoint pjp) throws Throwable {
        return pjp.proceed(encodeTicketIdArgs(pjp.getArgs()));
    }

    /**
     * Decodes sensitive ticket data after retrieval from registry.
     *
     * @param pjp Proceeding join point.
     *
     * @throws Throwable On errors.
     * @return ticket or null
     */
    @Around("execution(Ticket org.jasig.cas.ticket.registry.*TicketRegistry.getTicket(String))")
    public Object decodeTicket(final ProceedingJoinPoint pjp) throws Throwable {
        final Object result = pjp.proceed(encodeTicketIdArgs(pjp.getArgs()));
        Ticket ticket = null;
        if (result != null) {
            logger.debug("Attempting to decode [{}]",  result);
            if (result instanceof EncodedTicket) {
                ticket = ((EncodedTicket) result).decode();
                logger.debug("Decoded [{}]",  ticket);
            } else {
                throw new IllegalArgumentException("Expected EncodedTicket but was " + result);
            }
            return ticket;
        }
        logger.debug("Refusing to decode null ticket");
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
    @Around("execution(Collection org.jasig.cas.ticket.registry.*TicketRegistry.getTickets())")
    public Object decodeTickets(final ProceedingJoinPoint pjp) throws Throwable {
        final Collection<?> items = (Collection) pjp.proceed();
        final Set<Ticket> tickets = new HashSet<Ticket>(items.size());

        for (final Object item : items) {
            if (item instanceof EncodedTicket) {
                final Ticket ticket = ((EncodedTicket) item).decode();
                logger.debug("Decoded [{}]",  ticket);
                tickets.add(ticket);
            } else {
                throw new IllegalArgumentException("Expected EncodedTicket");
            }
        }
        return tickets;
    }

    /**
     * Set the encoder instance.
     * @param encoder Encoder/decoder to use for securing sensitive ticket data.
     */
    public void setEncoder(final ReversibleEncoder encoder) {
        this.encoder = encoder;
    }

    /**
     * Encode ticket id as argument.
     *
     * @param originalArgs the original args
     * @return the object aray containing the encoded id
     */
    private Object[] encodeTicketIdArgs(final Object[] originalArgs) {
        final String ticketId = (String) originalArgs[0];
        logger.debug("Encoding ticket [{}]",  ticketId);
        final String encodedId = EncodedTicket.encodeId(this.encoder, ticketId);
        logger.debug("Encoded ticket id [{}]", encodedId);
        return new Object[] {encodedId};
    }
}
