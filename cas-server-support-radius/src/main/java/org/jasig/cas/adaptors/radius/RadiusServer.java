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
package org.jasig.cas.adaptors.radius;

import org.jasig.cas.authentication.PreventedException;

/**
 * Interface representing a Radius Server.
 *
 * @author Scott Battaglia
 * @author Misagh Moayyed
 * @since 3.1
 */
public interface RadiusServer {
    
    /** The default port for accounting.
     * @since 4.1.0
     **/
    int DEFAULT_PORT_ACCOUNTING = 1813;
    
    /** The default port for authentication.
     * @since 4.1.0
     **/
    int DEFAULT_PORT_AUTHENTICATION = 1812;
    
    /**
     * Method to authenticate a set of credentials.
     *
     * @param username Non-null username to authenticate.
     * @param password Password to authenticate.
     *
     * @return {@link RadiusResponse} on success, null otherwise.
     *
     * @throws PreventedException On indeterminate case where authentication was prevented by a system (e.g. IO) error.
     */
    RadiusResponse authenticate(String username, String password) throws PreventedException;

}
