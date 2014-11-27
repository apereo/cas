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
package org.jasig.cas.authentication.handler;

import org.jasig.cas.authentication.principal.Credentials;

/**
 * Deprecated interface for authenticating user-supplied credential. This component has
 * been superseded by {@link org.jasig.cas.authentication.AuthenticationHandler} as of
 * CAS 4.0.
 * <p>
 * Validate Credential support for AuthenticationManagerImpl.
 * <p>
 * Determines that Credential are valid. Password-based credential may be
 * tested against an external LDAP, Kerberos, JDBC source. Certificates may be
 * checked against a list of CA's and do the usual chain validation.
 * Implementations must be parameterized with their sources of information.
 * <p>
 * Callers to this class should first call supports to determine if the
 * AuthenticationHandler can authenticate the credential provided.
 *
 * @author Scott Battaglia
 * @deprecated In favor of {@link org.jasig.cas.authentication.AuthenticationHandler}.
 * @since 3.0.0
 * @see org.jasig.cas.authentication.LegacyAuthenticationHandlerAdapter
 * @see org.jasig.cas.authentication.AuthenticationHandler
 */
@Deprecated
public interface AuthenticationHandler {

    /**
     * Method to determine if the credential supplied are valid.
     *
     * @param credential The credential to validate.
     * @return true if valid, return false otherwise.
     * @throws AuthenticationException An AuthenticationException can contain
     * details about why a particular authentication request failed.
     */
    boolean authenticate(Credentials credential)
        throws AuthenticationException;

    /**
     * Method to check if the handler knows how to handle the credential
     * provided. It may be a simple check of the Credential class or something
     * more complicated such as scanning the information contained in the
     * Credential object.
     *
     * @param credential The credential to check.
     * @return true if the handler supports the Credential, false othewrise.
     */
    boolean supports(Credentials credential);
}
