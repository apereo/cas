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
 * Represents a service using CAS that comes from the web.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public interface WebApplicationService extends Service {

    /**
     * Constructs the url to redirect the service back to.
     *
     * @param ticketId the service ticket to provide to the service.
     * @return the redirect url.
     */
    Response getResponse(String ticketId);

    /**
     * Retrieves the artifact supplied with the service. May be null.
     *
     * @return the artifact if it exists, null otherwise.
     */
    String getArtifactId();

    /**
     * Return the original url provided (as <code>service</code> or <code>targetService</code> request parameter).
     * Used to reconstruct the redirect url.
     *
     * @return the original url provided.
     */
    String getOriginalUrl();
}
