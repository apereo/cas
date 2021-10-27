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

package org.jasig.cas.logout;

import org.jasig.cas.authentication.principal.SingleLogoutService;

import java.io.Serializable;
import java.net.URL;

/**
 * Identifies a CAS logout request and its properties.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public interface LogoutRequest extends Serializable {
    /**
     * Gets status of the request.
     *
     * @return the status
     */
    LogoutRequestStatus getStatus();

    /**
     * Sets status of the request.
     *
     * @param status the status
     */
    void setStatus(LogoutRequestStatus status);

    /**
     * Gets ticket id.
     *
     * @return the ticket id
     */
    String getTicketId();

    /**
     * Gets service.
     *
     * @return the service
     */
    SingleLogoutService getService();

    /**
     * Gets logout url.
     *
     * @return the logout url
     */
    URL getLogoutUrl();

}
