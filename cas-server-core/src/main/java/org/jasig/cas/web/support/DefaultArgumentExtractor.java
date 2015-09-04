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

import org.jasig.cas.authentication.principal.ServiceFactory;
import org.jasig.cas.authentication.principal.WebApplicationService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import java.util.List;

/**
 * The default argument extractor is responsible for creating service
 * objects based on requests. The task of creating services is delegated to
 * a service factory that is pluggable for each instance of the extractor.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public final class DefaultArgumentExtractor extends AbstractArgumentExtractor {

    /**
     * Instantiates a new argument extractor.
     */
    public DefaultArgumentExtractor() {
        super();
    }

    /**
     * Instantiates a new argument extractor.
     *
     * @param serviceFactory the service factory
     */
    public DefaultArgumentExtractor(final ServiceFactory<? extends WebApplicationService> serviceFactory) {
        super(serviceFactory);
    }

    /**
     * Instantiates a new argument extractor.
     *
     * @param serviceFactoryList the service factory list
     */
    public DefaultArgumentExtractor(@Min(1)
                                    final List<ServiceFactory<? extends WebApplicationService>> serviceFactoryList) {
        super(serviceFactoryList);
    }

    @Override
    public WebApplicationService extractServiceInternal(final HttpServletRequest request) {
        for (final ServiceFactory<? extends WebApplicationService> factory : getServiceFactories()) {
            final WebApplicationService service = factory.createService(request);
            if (service != null) {
                logger.debug("Created {} based on {}", service, factory);
                return service;
            }
        }
        logger.debug("No service could be extracted based on the given request");
        return null;
    }
}
