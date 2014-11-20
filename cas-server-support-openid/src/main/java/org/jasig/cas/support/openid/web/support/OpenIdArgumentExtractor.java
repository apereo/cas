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
package org.jasig.cas.support.openid.web.support;

import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.support.openid.OpenIdProtocolConstants;
import org.jasig.cas.support.openid.authentication.principal.OpenIdService;
import org.jasig.cas.web.support.AbstractArgumentExtractor;

import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * Constructs an OpenId Service.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public class OpenIdArgumentExtractor extends AbstractArgumentExtractor {

    @Override
    protected WebApplicationService extractServiceInternal(final HttpServletRequest request) {
        return createServiceFrom(request);
    }

    /**
     * Creates the service from the request.
     *
     * @param request the request
     * @return the OpenID service
     */
    protected OpenIdService createServiceFrom(final HttpServletRequest request) {
        final String service = request.getParameter(OpenIdProtocolConstants.PARAM_SERVICE);
        final String openIdIdentity = request.getParameter("openid.identity");
        final String signature = request.getParameter("openid.sig");

        if (openIdIdentity == null || !StringUtils.hasText(service)) {
            return null;
        }

        final String id = cleanupUrl(service);
        final String artifactId = request.getParameter("openid.assoc_handle");
        final ParameterList paramList = new ParameterList(request.getParameterMap());

        return new OpenIdService(id, service, artifactId, openIdIdentity,
                signature, paramList);
    }
}
