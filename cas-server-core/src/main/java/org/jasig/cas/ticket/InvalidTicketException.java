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

/**
 * TicketException to alert that a Ticket was not found or that it is expired.
 *
 * @author Scott Battaglia
 * @since 3.0
 */
public class InvalidTicketException extends TicketException {

    private static final long serialVersionUID = 9141891414482490L;

    private String ticketId = null;

    /** The code description. */
    private static final String CODE = "INVALID_TICKET";

    /**
     * Constructs a InvalidTicketException with the default exception code.
     * @param ticketId the ticket id that originally caused this exception to be thrown.
     */
    public InvalidTicketException(final String ticketId) {
        super(CODE);
        this.ticketId = ticketId;
    }

    /**
     * Constructs a InvalidTicketException with the default exception code and
     * the original exception that was thrown.
     *
     * @param throwable the chained exception
     * @param ticketId the ticket id that originally caused this exception to be thrown.
     */
    public InvalidTicketException(final Throwable throwable, final String ticketId) {
        super(CODE, throwable);
    }

    /**
     * Returns the ticket id that causes this exception.
     * @return the ticket id
     * @see InvalidTicketException#ticketId
     */
    @Override
    public String getMessage() {
        return this.ticketId;
    }
}
