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
import org.jasig.cas.authentication.principal.Response.ResponseType;
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

    private final ResponseType responseType;

    private static final long serialVersionUID = 8334068957483758042L;

    /**
     * Instantiates a new simple web application service impl.
     *
     * @param id the id
     */
    public SimpleWebApplicationServiceImpl(final String id) {
        this(id, id, null, null);
    }

    /**
     * Instantiates a new simple web application service impl.
     *
     * @param id the id
     * @param originalUrl the original url
     * @param artifactId the artifact id
     * @param responseType the response type
     */
    public SimpleWebApplicationServiceImpl(final String id,
        final String originalUrl, final String artifactId,
        final ResponseType responseType) {
        super(id, originalUrl, artifactId);
        this.responseType = responseType;
    }

        final String targetService = request.getParameter(CONST_PARAM_TARGET_SERVICE);
        final String service = request.getParameter(CONST_PARAM_SERVICE);
        final String serviceAttribute = (String) request.getAttribute(CONST_PARAM_SERVICE);
        final String serviceToUse;
        if (StringUtils.hasText(targetService)) {
            serviceToUse = targetService;
        } else if (StringUtils.hasText(service)) {
            serviceToUse = service;
        } else {
            serviceToUse = serviceAttribute;
        }

    @Override
    public Response getResponse(final String ticketId) {
        final Map<String, String> parameters = new HashMap<String, String>();

        if (StringUtils.hasText(ticketId)) {
            parameters.put(CasProtocolConstants.PARAM_TICKET, ticketId);
        }

        if (ResponseType.POST == this.responseType) {
            return Response.getPostResponse(getOriginalUrl(), parameters);
        }
        return Response.getRedirectResponse(getOriginalUrl(), parameters);
    }
}
