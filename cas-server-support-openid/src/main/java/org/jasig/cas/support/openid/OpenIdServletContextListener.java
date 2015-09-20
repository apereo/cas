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

package org.jasig.cas.support.openid;

import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.principal.PrincipalResolver;
import org.jasig.cas.support.openid.authentication.principal.OpenIdService;
import org.jasig.cas.util.UniqueTicketIdGenerator;
import org.jasig.cas.web.AbstractServletContextInitializer;
import org.jasig.cas.web.support.ArgumentExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;

/**
 * Initializes the CAS root servlet context to make sure
 * OpenID endpoint can be activated by the main CAS servlet.
 * @author Misagh Moayyed
 * @since 4.2
 */
@WebListener
@Component
public class OpenIdServletContextListener extends AbstractServletContextInitializer {

    @Autowired
    @Qualifier("serviceTicketUniqueIdGenerator")
    private UniqueTicketIdGenerator serviceTicketUniqueIdGenerator;

    @Autowired
    @Qualifier("openIdCredentialsAuthenticationHandler")
    private AuthenticationHandler openIdCredentialsAuthenticationHandler;

    @Autowired
    @Qualifier("openIdPrincipalResolver")
    private PrincipalResolver openIdPrincipalResolver;

    @Autowired
    @Qualifier("openIdArgumentExtractor")
    private ArgumentExtractor openIdArgumentExtractor;

    @Override
    protected void initializeRootApplicationContext() {
        addAuthenticationHandlerPrincipalResolver(openIdCredentialsAuthenticationHandler, openIdPrincipalResolver);
    }

    @Override
    protected void initializeServletApplicationContext() {
        addControllerToCasServletHandlerMapping(OpenIdConstants.ENDPOINT_OPENID, "openIdProviderController");
        addServiceTicketUniqueIdGenerator(OpenIdService.class.getCanonicalName(), this.serviceTicketUniqueIdGenerator);
        addArgumentExtractor(this.openIdArgumentExtractor);
    }

    @Override
    protected void initializeServletContext(final ServletContextEvent event) {
        addEndpointMappingToCasServlet(event, OpenIdConstants.ENDPOINT_OPENID);
    }
}
