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

package org.jasig.cas.services;

import org.jasig.cas.authentication.principal.Principal;

import java.io.Serializable;

/**
 * Provides an abstraction over the storage and persistence
 * mechanism of attribute release and consent. Implementations
 * should locate whether attribute release consented for a particular service
 * by a given principal is available.
 * @author Misagh Moayyed
 * @since 4.2
 */
public interface AttributeReleaseConsentStrategy extends Serializable {

    /**
     * Determines whether the given principal consents
     * to the release of attributes for this service.
     * @param service service for which consent is required
     * @param principal principal authorizing consent
     * @return true if consented, false otherwise.
     */
    boolean isAttributeReleaseConsented(RegisteredService service, Principal principal);

    /**
     * Remembers/stores that given principal consents
     * to the release of attributes for this service.
     * @param service service for which consent is required
     * @param principal principal authorizing consent
     */
    void setAttributeReleaseConsented(RegisteredService service, Principal principal);
}

