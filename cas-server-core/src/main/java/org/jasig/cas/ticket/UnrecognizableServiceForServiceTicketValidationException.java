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
 * An exception that may be thrown during service ticket validation
 * to indicate that the service ticket is not valid and was not originally
 * issued for the submitted service.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class UnrecognizableServiceForServiceTicketValidationException extends TicketValidationException {
    /** The code description. */
    protected static final String CODE = "INVALID_SERVICE";

    private static final long serialVersionUID = -8076771862820008358L;

    /**
     * Instantiates a new Unrecognizable service for service ticket validation exception.
     *
     * @param service the service
     */
    public UnrecognizableServiceForServiceTicketValidationException(final Service service) {
        super(CODE, service);
    }
}
