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
package org.jasig.cas.authentication.handler;

import org.jasig.cas.authentication.principal.Credentials;

/**
 * Deprecated interface for authenticating user-supplied credentials. This component has
 * been superseded by {@link org.jasig.cas.authentication.AuthenticationHandler} as of
 * CAS 4.0.
 *
 * @author Scott Battaglia
 * @since 3.0
 * @see org.jasig.cas.authentication.LegacyAuthenticationHandlerAdapter
 * @see org.jasig.cas.authentication.AuthenticationHandler
 */
@Deprecated
public interface AuthenticationHandler {

    /**
     * Method to determine if the credentials supplied are valid.
     * 
     * @param credentials The credentials to validate.
     * @return true if valid, return false otherwise.
     * @throws AuthenticationException An AuthenticationException can contain
     * details about why a particular authentication request failed.
     */
    boolean authenticate(Credentials credentials)
        throws AuthenticationException;

    /**
     * Method to check if the handler knows how to handle the credentials
     * provided. It may be a simple check of the Credentials class or something
     * more complicated such as scanning the information contained in the
     * Credentials object.
     * 
     * @param credentials The credentials to check.
     * @return true if the handler supports the Credentials, false othewrise.
     */
    boolean supports(Credentials credentials);
}
