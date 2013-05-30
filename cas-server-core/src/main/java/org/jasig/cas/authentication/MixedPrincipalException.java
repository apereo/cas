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

import org.jasig.cas.authentication.principal.Principal;

/**
 * Describes an error condition where non-identical principals have been resolved while authenticating
 * multiple credentials.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public class MixedPrincipalException extends PrincipalException {

    /** Serialization version marker. */
    private static final long serialVersionUID = -9040132618070273997L;

    /** First resolved principal. */
    private final Principal first;

    /** Second resolved principal. */
    private final Principal second;

    /**
     * Creates a new instance from what would otherwise have been a successful authentication event and the two
     * disparate principals resolved.
     *
     * @param authentication Authentication event.
     * @param a First resolved principal.
     * @param b Second resolved principal.
     */
    public MixedPrincipalException(final Authentication authentication, final Principal a, final Principal b) {
        super(a + " != " + b, authentication.getFailures(), authentication.getSuccesses());
        this.first = a;
        this.second = b;
    }

    /**
     * Gets the first resolved principal.
     *
     * @return First resolved principal.
     */
    public Principal getFirst() {
        return this.first;
    }

    /**
     * Gets the second resolved principal.
     *
     * @return Second resolved principal.
     */
    public Principal getSecond() {
        return this.second;
    }
}
