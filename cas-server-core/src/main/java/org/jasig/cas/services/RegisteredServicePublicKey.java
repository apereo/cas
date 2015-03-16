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

import java.security.PublicKey;

/**
 * Represents a public key for a CAS registered service.
 * @author Misagh Moayyed
 * @since 4.1
 */
public interface RegisteredServicePublicKey {

    /**
     * Gets location to the public key file.
     *
     * @return the location
     */
    String getLocation();

    /**
     * Gets algorithm for the public key.
     *
     * @return the algorithm
     */
    String getAlgorithm();

    /**
     * Create instance.
     *
     * @return the public key
     * @throws Exception the exception thrown if the public key cannot be created
     */
    PublicKey createInstance() throws Exception;
}
