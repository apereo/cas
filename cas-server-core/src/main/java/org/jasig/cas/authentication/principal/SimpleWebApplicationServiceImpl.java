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

import org.jasig.cas.authentication.principal.Response.ResponseType;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
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

    private static final String CONST_PARAM_SERVICE = "service";

    private static final String CONST_PARAM_TARGET_SERVICE = "targetService";

    private static final String CONST_PARAM_TICKET = "ticket";

    private static final String CONST_PARAM_METHOD = "method";

    private final ResponseType responseType;

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
    private SimpleWebApplicationServiceImpl(final String id,
        final String originalUrl, final String artifactId,
        final ResponseType responseType) {
        super(id, originalUrl, artifactId);
        this.responseType = responseType;
    }

    /**
     * Creates the service from the request.
     *
     * @param request the request
     * @return the simple web application service impl
     */
    public static SimpleWebApplicationServiceImpl createServiceFrom(
        final HttpServletRequest request) {
        final String targetService = request.getParameter(CONST_PARAM_TARGET_SERVICE);
        final String service = request.getParameter(CONST_PARAM_SERVICE);
        final String serviceAttribute = (String) request.getAttribute(CONST_PARAM_SERVICE);
        final String method = request.getParameter(CONST_PARAM_METHOD);
        final String serviceToUse;
        if (StringUtils.hasText(targetService)) {
            serviceToUse = targetService;
        } else if (StringUtils.hasText(service)) {
            serviceToUse = service;
        } else {
            serviceToUse = serviceAttribute;
        }

        if (!StringUtils.hasText(serviceToUse)) {
            return null;
        }

        final String id = cleanupUrl(serviceToUse);
        final String artifactId = request.getParameter(CONST_PARAM_TICKET);

        return new SimpleWebApplicationServiceImpl(id, serviceToUse,
            artifactId, "POST".equals(method) ? ResponseType.POST
                : ResponseType.REDIRECT);
    }

    @Override
    public Response getResponse(final String ticketId) {
        final Map<String, String> parameters = new HashMap<>();

        if (StringUtils.hasText(ticketId)) {
            parameters.put(CONST_PARAM_TICKET, ticketId);
        }

        if (ResponseType.POST == this.responseType) {
            return Response.getPostResponse(getOriginalUrl(), parameters);
        }
        return Response.getRedirectResponse(getOriginalUrl(), parameters);
    }
}
