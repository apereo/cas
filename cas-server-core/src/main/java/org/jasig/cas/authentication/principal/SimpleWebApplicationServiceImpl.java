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
package org.jasig.cas.authentication.principal;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.authentication.principal.Response.ResponseType;
import org.jasig.cas.util.HttpClient;
import org.springframework.util.StringUtils;

/**
 * Represents a service which wishes to use the CAS protocol.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.3 $ $Date: 2007/04/24 18:19:22 $
 * @since 3.1
 */
public final class SimpleWebApplicationServiceImpl extends
    AbstractWebApplicationService {

    private static final String CONST_PARAM_SERVICE = "service";

    private static final String CONST_PARAM_TARGET_SERVICE = "targetService";

    private static final String CONST_PARAM_TICKET = "ticket";

    private static final String CONST_PARAM_METHOD = "method";

    private final ResponseType responseType;

    /**
     * Unique Id for Serialization
     */
    private static final long serialVersionUID = 8334068957483758042L;
    
    public SimpleWebApplicationServiceImpl(final String id) {
        this(id, id, null, null, null);
    }

    public SimpleWebApplicationServiceImpl(final String id, final HttpClient httpClient) {
        this(id, id, null, null, httpClient);
    }

    private SimpleWebApplicationServiceImpl(final String id,
        final String originalUrl, final String artifactId,
        final ResponseType responseType, final HttpClient httpClient) {
        super(id, originalUrl, artifactId, httpClient);
        this.responseType = responseType;
    }
    
    public static SimpleWebApplicationServiceImpl createServiceFrom(final HttpServletRequest request) {
        return createServiceFrom(request, null);
    }

    public static SimpleWebApplicationServiceImpl createServiceFrom(
        final HttpServletRequest request, final HttpClient httpClient) {
        final String targetService = request
            .getParameter(CONST_PARAM_TARGET_SERVICE);
        final String method = request.getParameter(CONST_PARAM_METHOD);
        final String serviceToUse = StringUtils.hasText(targetService)
            ? targetService : request.getParameter(CONST_PARAM_SERVICE);

        if (!StringUtils.hasText(serviceToUse)) {
            return null;
        }

        final String id = cleanupUrl(serviceToUse);
        final String artifactId = request.getParameter(CONST_PARAM_TICKET);

        return new SimpleWebApplicationServiceImpl(id, serviceToUse,
            artifactId, "POST".equals(method) ? ResponseType.POST
                : ResponseType.REDIRECT, httpClient);
    }

    public Response getResponse(final String ticketId) {
        final Map<String, String> parameters = new HashMap<String, String>();

        if (StringUtils.hasText(ticketId)) {
            parameters.put(CONST_PARAM_TICKET, ticketId);
        }

        if (ResponseType.POST == this.responseType) {
            return Response.getPostResponse(getOriginalUrl(), parameters);
        }
        return Response.getRedirectResponse(getOriginalUrl(), parameters);
    }
}
