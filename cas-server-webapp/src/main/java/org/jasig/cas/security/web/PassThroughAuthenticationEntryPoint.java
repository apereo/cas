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
package org.jasig.cas.security.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Web authentication entry point that simply passes through without any authentication operation.
 * This type of entry point is useful for IP-based filtering using the Spring EL <code>hasIpAddress(...)</code>
 * expression.
 *
 * @author Middleware Services
 * @version $Revision: $
 */
public final class PassThroughAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Permits the request to proceed in all cases.
     *
     * @param request Servlet request.
     * @param response Servlet response.
     * @param authException Authentication exception from higher up in filter chain.
     * @throws IOException Never thrown.
     * @throws ServletException Never thrown.
     */
    public void commence(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final AuthenticationException authException)
            throws IOException, ServletException {

        logger.debug("Permitting {} to pass through", request.getRemoteAddr());
    }
}
