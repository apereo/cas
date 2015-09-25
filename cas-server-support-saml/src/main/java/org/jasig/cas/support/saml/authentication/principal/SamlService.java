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
package org.jasig.cas.support.saml.authentication.principal;

import org.jasig.cas.authentication.principal.AbstractWebApplicationService;
import org.jasig.cas.authentication.principal.ResponseBuilder;
import org.jasig.cas.authentication.principal.WebApplicationService;

/**
 * Class to represent that this service wants to use SAML. We use this in
 * combination with the CentralAuthenticationServiceImpl to choose the right
 * UniqueTicketIdGenerator.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public final class SamlService extends AbstractWebApplicationService {

    /**
     * Unique Id for serialization.
     */
    private static final long serialVersionUID = -6867572626767140223L;

    private final String requestId;


    /**
     * Instantiates a new SAML service.
     *
     * @param id the service id
     * @param originalUrl the original url
     * @param artifactId the artifact id
     * @param requestId the request id
     * @param responseBuilder the response builder
     */
    protected SamlService(final String id, final String originalUrl,
                          final String artifactId, final String requestId,
                          final ResponseBuilder<WebApplicationService> responseBuilder) {
        super(id, originalUrl, artifactId, responseBuilder);
        this.requestId = requestId;
    }

    public String getRequestID() {
        return this.requestId;
    }
}
