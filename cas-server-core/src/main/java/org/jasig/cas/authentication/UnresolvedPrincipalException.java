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

/**
 * Describes an error condition where a principal could not be resolved.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public class UnresolvedPrincipalException extends PrincipalException {

    /** Serialization version marker. */
    private static final long serialVersionUID = 380456166081802820L;

    /** Error message when there was no error that prevent principal resolution. */
    private static final String UNRESOLVED_PRINCIPAL = "No resolver produced a principal.";

    /**
     * Creates a new instance from an authentication event that was successful prior to principal resolution.
     *
     * @param authentication Authentication event.
     */
    public UnresolvedPrincipalException(final Authentication authentication) {
        super(UNRESOLVED_PRINCIPAL, authentication.getFailures(), authentication.getSuccesses());
    }

    /**
     * Creates a new instance from an authentication event that was successful prior to principal resolution.
     * This form should be used when a resolver exception prevented principal resolution.
     *
     * @param authentication Authentication event.
     * @param cause Exception that prevented principal resolution.
     */
    public UnresolvedPrincipalException(final Authentication authentication, final Exception cause) {
        super(cause.getMessage(), authentication.getFailures(), authentication.getSuccesses());
    }
}
