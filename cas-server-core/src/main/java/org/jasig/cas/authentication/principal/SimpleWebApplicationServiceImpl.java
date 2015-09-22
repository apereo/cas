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

import org.jasig.cas.CasProtocolConstants;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a service which wishes to use the CAS protocol.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public final class SimpleWebApplicationServiceImpl extends AbstractWebApplicationService {

    private static final long serialVersionUID = 8334068957483758042L;

    private final Response.ResponseType responseType;

    /**
     * Instantiates a new simple web application service impl.
     *
     * @param id the id
     * @param originalUrl the original url
     * @param artifactId the artifact id
     * @param responseType the response type
     */
    protected SimpleWebApplicationServiceImpl(final String id,
        final String originalUrl, final String artifactId,
        final Response.ResponseType responseType) {
        super(id, originalUrl, artifactId);
        this.responseType = responseType;
    }

    @Override
    public Response getResponse(final String ticketId) {
        final Map<String, String> parameters = new HashMap<>();

        if (StringUtils.hasText(ticketId)) {
            parameters.put(CasProtocolConstants.PARAMETER_TICKET, ticketId);
        }

        if (Response.ResponseType.POST == this.responseType) {
            return DefaultResponse.getPostResponse(getOriginalUrl(), parameters);
        }
        return DefaultResponse.getRedirectResponse(getOriginalUrl(), parameters);
    }
}
