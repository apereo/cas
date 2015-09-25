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

package org.jasig.cas.authentication.principal;

import javax.servlet.http.HttpServletRequest;

/**
 * The {@link ServiceFactory} is responsible for creating service objects.
 * @author Misagh Moayyed
 * @param <T> the type parameter
 * @since 4.2
 */
public interface ServiceFactory<T extends Service> {

    /**
     * Create service object based on the parameters of the request.
     *
     * @param request the request
     * @return the service
     */
    T createService(HttpServletRequest request);

    /**
     * Create service based on an identifier.
     *
     * @param id the id
     * @return the service object
     */
    T createService(String id);


    /**
     * Create the service object based on an identifier.
     * Allows the final service object to be casted to the desired service class
     * that may not immediately inherit from {@link Service} itself.
     *
     * @param <T> the type parameter
     * @param id the id
     * @param clazz the clazz
     * @return the t
     */
    <T extends Service> T createService(String id, Class<? extends Service> clazz);

    /**
     * Create service based on the given parameters provided by the http request.
     * Allows the final service object to be casted to the desired service class
     * that may not immediately inherit from {@link Service} itself.
     *
     * @param <T> the type parameter
     * @param request the request
     * @param clazz the clazz
     * @return the t
     */
    <T extends Service> T createService(HttpServletRequest request, Class<? extends Service> clazz);
}
