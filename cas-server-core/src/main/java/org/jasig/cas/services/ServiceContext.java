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
package org.jasig.cas.services;

import javax.validation.constraints.NotNull;

import org.jasig.cas.authentication.principal.Service;

/**
 * Simple container for holding a service principal and its corresponding registered serivce.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public class ServiceContext {

    /** Service principal. */
    @NotNull
    private final Service service;

    /** Registered service corresponding to service principal. */
    @NotNull
    private final RegisteredService registeredService;

    /**
     * Creates a new instance with required parameters.
     *
     * @param service Service principal.
     * @param registeredService Registered service corresponding to given service.
     */
    public ServiceContext(@NotNull final Service service, @NotNull final RegisteredService registeredService) {
        this.service = service;
        this.registeredService = registeredService;
        if (!registeredService.matches(service)) {
            throw new IllegalArgumentException("Registered service does not match given service.");
        }
    }

    /**
     * Gets the service principal.
     *
     * @return Non-null service principal.
     */
    public Service getService() {
        return service;
    }

    /**
     * Gets the registered service for the service principal.
     *
     * @return Non-null registered service.
     */
    public RegisteredService getRegisteredService() {
        return registeredService;
    }
}
