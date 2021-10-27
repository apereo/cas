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
package org.jasig.cas.web.flow;

import javax.validation.constraints.NotNull;

import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.util.StringUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Webflow action that checks whether the TGT in the request context is valid. There are three possible outcomes:
 *
 * <ol>
 *     <li>{@link #NOT_EXISTS} - TGT not found in flow request context.</li>
 *     <li>{@link #INVALID} TGT has expired or is not found in ticket registry.</li>
 *     <li>{@link #VALID} - TGT found in ticket registry and has not expired.</li>
 * </ol>
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public class TicketGrantingTicketCheckAction {

    /** TGT does not exist event ID={@value}. */
    public static final String NOT_EXISTS = "notExists";

    /** TGT invalid event ID={@value}. */
    public static final String INVALID = "invalid";

    /** TGT valid event ID={@value}. */
    public static final String VALID = "valid";

    /** Ticket registry searched for TGT by ID. */
    @NotNull
    private final TicketRegistry ticketRegistry;


    /**
     * Creates a new instance with the given ticket registry.
     *
     * @param registry Ticket registry to query for valid tickets.
     */
    public TicketGrantingTicketCheckAction(final TicketRegistry registry) {
        this.ticketRegistry = registry;
    }

    /**
     * Determines whether the TGT in the flow request context is valid.
     *
     * @param requestContext Flow request context.
     *
     * @return {@link #NOT_EXISTS}, {@link #INVALID}, or {@link #VALID}.
     */
    public Event checkValidity(final RequestContext requestContext) {

        final String tgtId = WebUtils.getTicketGrantingTicketId(requestContext);
        if (!StringUtils.hasText(tgtId)) {
            return new Event(this, NOT_EXISTS);
        }

        final Ticket ticket = this.ticketRegistry.getTicket(tgtId);
        return new Event(this, ticket != null && !ticket.isExpired() ? VALID : INVALID);
    }
}
