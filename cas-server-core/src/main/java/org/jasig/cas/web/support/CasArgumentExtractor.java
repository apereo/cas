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
package org.jasig.cas.web.support;

import org.jasig.cas.CasProtocolConstants;
import org.jasig.cas.authentication.principal.Response;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Implements the traditional CAS2 protocol.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public final class CasArgumentExtractor extends AbstractArgumentExtractor {

    @Override
    public WebApplicationService extractServiceInternal(final HttpServletRequest request) {
        return createServiceFrom(request);
    }

    /**
     * Creates the service from the request.
     *
     * @param request the request
     * @return the simple web application service impl
     */
    protected SimpleWebApplicationServiceImpl createServiceFrom(final HttpServletRequest request) {
        final String targetService = request.getParameter(CasProtocolConstants.PARAM_TARGET_SERVICE);
        final String method = request.getParameter(CasProtocolConstants.PARAM_METHOD);
        final String serviceToUse = StringUtils.hasText(targetService)
                ? targetService : request.getParameter(CasProtocolConstants.PARAM_TARGET_SERVICE);

        if (!StringUtils.hasText(serviceToUse)) {
            return null;
        }

        final String id = cleanupUrl(serviceToUse);
        final String artifactId = request.getParameter(CasProtocolConstants.PARAM_TICKET);

        return new SimpleWebApplicationServiceImpl(id, serviceToUse,
                artifactId, "POST".equals(method) ? Response.ResponseType.POST
                : Response.ResponseType.REDIRECT);
    }


}
