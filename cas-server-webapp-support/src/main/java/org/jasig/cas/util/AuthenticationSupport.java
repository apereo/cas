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

package org.jasig.cas.util;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Principal;

import java.util.Map;

/**
 * Helper strategy API to ease retrieving CAS' <code>Authentication</code> object and its associated components
 * from available CAS SSO String token called <i>Ticket Granting Ticket (TGT)</i>
 *
 * <p>Note: this API is only intended to be called by CAS server code e.g. any custom CAS server overlay extension, etc.</p>
 *
 * <p>Concurrency semantics: implementations must be thread safe.</p>
 *
 * @author Dmitriy Kopylenko
 * @since 4.1
 */
public interface AuthenticationSupport {

    /**
     * Retrieve a valid {@link org.jasig.cas.authentication.Authentication} object identified by the provided TGT SSO token.
     * @param ticketGrantingTicketId an SSO token identifying the requested Authentication
     * @return valid Authentication OR <b>NULL</b> if there is no valid SSO session present identified by the provided TGT id SSO token
     */
    Authentication getAuthenticationFrom(String ticketGrantingTicketId);

    /**
     * Retrieve a valid {@link org.jasig.cas.authentication.principal.Principal}
     * object identified by the provided TGT SSO token.
     * @param ticketGrantingTicketId an SSO token identifying the requested authenticated
     * {@link org.jasig.cas.authentication.principal.Principal}
     * @return valid Principal OR <b>NULL</b> if there is no valid SSO session present identified by the provided TGT id SSO token
     * @throws RuntimeException
     */
    Principal getAuthenticatedPrincipalFrom(String ticketGrantingTicketId);

    /**
     * Retrieve a valid Principal's map of attributes identified by the provided TGT SSO token.
     * @param ticketGrantingTicketId an SSO token identifying the requested authenticated Principal's attributes
     * @return valid Principal's attributes OR <b>null</b> if there is no valid SSO session present
     * identified by the provided TGT id SSO token
     * @throws RuntimeException
     */
    Map<String, Object> getPrincipalAttributesFrom(String ticketGrantingTicketId);
}
