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
package org.jasig.cas.authentication;

/**
 * Describes an error condition where authentication was prevented for some reason, e.g. communication
 * error with back-end authentication store.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class PreventedException extends Exception {

    private static final long serialVersionUID = 4702274165911620708L;

    /**
     * Creates a new instance with the exception that prevented authentication.
     *
     * @param cause Error that prevented authentication.
     */
    public PreventedException(final Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance with an explanatory message and the exception that prevented authentication.
     *
     * @param message Descriptive error message.
     * @param cause Error that prevented authentication.
     */
    public PreventedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
