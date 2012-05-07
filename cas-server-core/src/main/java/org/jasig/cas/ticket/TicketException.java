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
 * Generic ticket exception. Top of the TicketException heirarchy.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public abstract class TicketException extends Exception {

    /** Serializable Unique ID. */
    private static final long serialVersionUID = -6000583436059919480L;

    /** The code description of the TicketException. */
    private String code;

    /**
     * Constructs a new TicketException with the code identifying the exception
     * type.
     * 
     * @param code the code to describe what type of exception this is.
     */
    public TicketException(final String code) {
        this.code = code;
    }

    /**
     * Constructs a new TicketException with the code identifying the exception
     * and the original Throwable.
     * 
     * @param code the code to describe what type of exception this is.
     * @param throwable the original exception we are chaining.
     */
    public TicketException(final String code, final Throwable throwable) {
        super(throwable);
        this.code = code;
    }

    /**
     * @return Returns the code. If there is a chained exception it returns the
     * toString-ed version of the chained exception rather than the code.
     */
    public final String getCode() {
        return (this.getCause() != null) ? this.getCause().toString()
            : this.code;
    }
}
