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
package org.jasig.cas.ticket;

import org.jasig.cas.authentication.principal.Service;

/**
 * Exception to alert that there was an error validating the ticket.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 * @deprecated As of 4.1, the class is required to note its abstractness in the name and will be renamed in the future.
 */
@Deprecated
public abstract class TicketValidationException extends TicketException {
    /** The code description. */
    protected static final String CODE = "INVALID_TICKET";

    /** Unique Serial ID. */
    private static final long serialVersionUID = 3257004341537093175L;

    private final Service service;

    /**
     * Constructs a TicketValidationException with the default exception code
     * and the original exception that was thrown.
     * @param service original service
     */
    public TicketValidationException(final Service service) {
        this(CODE, service);
    }

    /**
     * Instantiates a new Ticket validation exception.
     *
     * @param code the code
     * @param service the service
     * @since 4.1
     */
    public TicketValidationException(final String code, final Service service) {
        super(code);
        this.service = service;
    }

    public Service getOriginalService() {
        return this.service;
    }

}
