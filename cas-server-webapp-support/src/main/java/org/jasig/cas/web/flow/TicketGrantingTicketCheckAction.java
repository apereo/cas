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
package org.jasig.cas.web.flow;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.util.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.validation.constraints.NotNull;

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
 * @since 4.0.0
 */
public class TicketGrantingTicketCheckAction extends AbstractAction {

    /**
     * TGT does not exist event ID={@value}.
     **/
    public static final String NOT_EXISTS = "notExists";

    /**
     * TGT invalid event ID={@value}.
     **/
    public static final String INVALID = "invalid";

    /**
     * TGT valid event ID={@value}.
     **/
    public static final String VALID = "valid";

    /**
     * The Central authentication service.
     */
    @NotNull
    private final CentralAuthenticationService centralAuthenticationService;


    /**
     * Creates a new instance with the given ticket registry.
     *
     * @param centralAuthenticationService the central authentication service
     */
    public TicketGrantingTicketCheckAction(final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    /**
     * Determines whether the TGT in the flow request context is valid.
     *
     * @param requestContext Flow request context.
     *
     * @throws Exception in case ticket cannot be retrieved from the service layer
     * @return {@link #NOT_EXISTS}, {@link #INVALID}, or {@link #VALID}.
     */
    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final String tgtId = WebUtils.getTicketGrantingTicketId(requestContext);
        if (!StringUtils.hasText(tgtId)) {
            return new Event(this, NOT_EXISTS);
        }

        String eventId = INVALID;
        try {
            final Ticket ticket = this.centralAuthenticationService.getTicket(tgtId, Ticket.class);
            if (ticket != null && !ticket.isExpired()) {
                eventId = VALID;
            }
        } catch (final TicketException e) {
            logger.trace("Could not retrieve ticket id {} from registry.", e);
        }
        return new Event(this,  eventId);
    }
}
