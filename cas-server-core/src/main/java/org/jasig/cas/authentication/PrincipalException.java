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
package org.jasig.cas.authentication;

import java.util.Map;

/**
 * Describes a principal resolution error, which is a subcategory of authentication error.
 * Principal resolution necessarily happens after successful authentication for a given credential.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public class PrincipalException extends AuthenticationException {

    /** Serialization metadata. */
    private static final long serialVersionUID = -6590363469748313596L;

    /**
     * Creates a new instance.
     * @param message Error message.
     * @param handlerErrors Map of handler names to errors.
     * @param handlerSuccesses Map of handler names to authentication successes.
     */
    public PrincipalException(
            final String message,
            final Map<String, Class<? extends Exception>> handlerErrors,
            final Map<String, HandlerResult> handlerSuccesses) {
        super(message, handlerErrors, handlerSuccesses);
    }
}
