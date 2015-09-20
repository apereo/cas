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

package org.jasig.cas.support.oauth;

import org.jasig.cas.support.oauth.services.OAuthCallbackAuthorizeService;
import org.jasig.cas.web.AbstractServletContextInitializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;
import java.security.SecureRandom;

/**
 * Initializes the CAS root servlet context to make sure
 * OAuth endpoint can be activated by the main CAS servlet.
 * @author Misagh Moayyed
 * @since 4.2
 */
@WebListener
@Component
public class OAuthServletContextListener extends AbstractServletContextInitializer {

    @Value("${server.prefix}" + OAuthConstants.ENDPOINT_OAUTH2_CALLBACK_AUTHORIZE)
    private String callbackAuthorizeUrl;


    @Override
    protected void initializeRootApplicationContext() {
        super.initializeRootApplicationContext();
    }

    @Override
    protected void initializeServletApplicationContext() {
        addControllerToCasServletHandlerMapping(OAuthConstants.ENDPOINT_OAUTH2, "oauth20WrapperController");

        final OAuthCallbackAuthorizeService service = new OAuthCallbackAuthorizeService();
        service.setId(new SecureRandom().nextLong());
        service.setName(service.getClass().getSimpleName());
        service.setDescription("OAuth Wrapper Callback Url");
        service.setServiceId(this.callbackAuthorizeUrl);

        addRegisteredServiceToServicesManager(service);
    }

    @Override
    protected void initializeServletContext(final ServletContextEvent event) {
        addEndpointMappingToCasServlet(event, OAuthConstants.ENDPOINT_OAUTH2);
    }
}
