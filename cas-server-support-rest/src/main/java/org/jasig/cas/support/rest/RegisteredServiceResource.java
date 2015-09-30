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

package org.jasig.cas.support.rest;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@link org.springframework.web.bind.annotation.RestController} implementation of a REST API
 * that allows for registration of CAS services. Services will automatically be put in in the service registry,
 * but are always disabled by default until they are approved by the explicit permission of the CAS deployer.
 * @author Misagh Moayyed
 * @since 4.2
 */
@RestController("/v1")
public class RegisteredServiceResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisteredServiceResource.class);

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;


}
