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
package org.jasig.cas.services;

/**
 * Exception thrown when a service attempts to use SSO when it should not be
 * allowed to.
 *
 * @author Scott Battaglia

 * @since 3.1
 */
public class UnauthorizedSsoServiceException extends
    UnauthorizedServiceException {

    /**
     * Comment for <code>serialVersionUID</code>.
     */
    private static final long serialVersionUID = 8909291297815558561L;

    /** The code description. */
    private static final String CODE = "service.not.authorized.sso";

    /**
     * Instantiates a new unauthorized sso service exception.
     */
    public UnauthorizedSsoServiceException() {
        this(CODE);
    }

    /**
     * Instantiates a new unauthorized sso service exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public UnauthorizedSsoServiceException(final String message,
        final Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new unauthorized sso service exception.
     *
     * @param message the message
     */
    public UnauthorizedSsoServiceException(final String message) {
        super(message);
    }

}
