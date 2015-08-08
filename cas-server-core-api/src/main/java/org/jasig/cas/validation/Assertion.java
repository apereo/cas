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
package org.jasig.cas.validation;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;

import java.util.List;

/**
 * Represents a security assertion obtained from a successfully validated ticket.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @see org.jasig.cas.CentralAuthenticationService#validateServiceTicket(String, org.jasig.cas.authentication.principal.Service)
 * @since 3.0.0
 */
public interface Assertion {

    /**
     * Gets the authentication event that is basis of this assertion.
     *
     * @return Non-null primary authentication event.
     */
    Authentication getPrimaryAuthentication();

    /**
     * Gets a list of all authentications that have occurred during a CAS SSO session.
     *
     * @return Non-null, non-empty list of authentications in leaf-first order (i.e. authentications on the root ticket
     * occur at the end).
     */
    List<Authentication> getChainedAuthentications();

    /**
     * True if the validated ticket was granted in the same transaction as that
     * in which its grantor GrantingTicket was originally issued.
     *
     * @return true if validated ticket was granted simultaneous with its
     * grantor's issuance
     */
    boolean isFromNewLogin();

    /**
     * Method to obtain the service for which we are asserting this ticket is
     * valid for.
     *
     * @return the service for which we are asserting this ticket is valid for.
     */
    Service getService();

}
