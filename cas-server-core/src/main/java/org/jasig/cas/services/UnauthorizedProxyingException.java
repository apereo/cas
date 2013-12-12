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
package org.jasig.cas.services;

/**
 * Exception thrown when a service attempts to proxy when it is not allowed to.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public class UnauthorizedProxyingException extends UnauthorizedServiceException {

    /**
     * Comment for <code>serialVersionUID</code>.
     */
    private static final long serialVersionUID = -7307803750894078575L;

    /** The code description. */
    public static final String CODE = "UNAUTHORIZED_SERVICE_PROXY";

    /**
     * Instantiates a new unauthorized proxying exception.
     */
    public UnauthorizedProxyingException() {
        super(CODE);
    }

    /**
     * Instantiates a new unauthorized proxying exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public UnauthorizedProxyingException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new unauthorized proxying exception.
     *
     * @param message the message
     */
    public UnauthorizedProxyingException(final String message) {
        super(message);
    }
}
