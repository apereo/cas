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

import java.util.Map;

/**
 * Defines the general contract of the attribute release policy for a registered service.
 * An instance of this attribute filter may determine how principal/global attributes are translated to a
 * map of attributes that may be released for a registered service.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
public interface RegisteredServiceAttributeFilter {
    /**
     * Filters the received principal attributes for the given registered service.
     *
     * @param principalId the principal id for whom attributes are to be released
     * @param givenAttributes the map for the original given attributes
     * @param svc the registered service for which attributes are to be released
     * @return a map that contains the filtered attributes.
     */
    Map<String, Object> filter(final String principalId, final Map<String, Object> givenAttributes,
            final RegisteredService svc);
}
