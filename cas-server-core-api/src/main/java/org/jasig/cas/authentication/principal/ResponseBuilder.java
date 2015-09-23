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

/**
 * Represents the task of building a CAS response
 * that is returned by a service.
 * @param <T>   the type parameter
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public interface ResponseBuilder<T extends WebApplicationService> {

    /**
     * Build response. The implementation must produce
     * a response that is based on the type passed. Given
     * the protocol used, the ticket id may be passed
     * as part of the response. If the response type
     * is not recognized, an error must be thrown back.
     *
     * @param service the service
     * @param ticketId the ticket id
     * @return the response
     */
    Response build(T service, String ticketId);
}
