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
package org.jasig.cas.web.support;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.authentication.principal.WebApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for handling argument extraction.
 *
 * @author Scott Battaglia
 * @since 3.1.2
 *
 */
public abstract class AbstractArgumentExtractor implements ArgumentExtractor {

    /**
     * Logger instance.
     */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public final WebApplicationService extractService(final HttpServletRequest request) {
        final WebApplicationService service = extractServiceInternal(request);

        if (service == null) {
            logger.debug("Extractor did not generate service.");
        } else {
            logger.debug("Extractor generated service for: {}", service.getId());
        }

        return service;
    }

    /**
     * Extract service from the request.
     *
     * @param request the request
     * @return the web application service
     */
    protected abstract WebApplicationService extractServiceInternal(final HttpServletRequest request);
}
