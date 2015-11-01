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

package org.jasig.cas.support.saml;

import org.jasig.cas.web.AbstractServletContextInitializer;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;

/**
 * Initializes the CAS root servlet context to make sure
 * SAML IdP can be activated by the main CAS servlet.
 * @author Misagh Moayyed
 * @since 4.2
 */
@WebListener
@Component
public class SamlIdPServletContextListener extends AbstractServletContextInitializer {

    @Override
    public void initializeServletContext(final ServletContextEvent event) {
        if (WebUtils.isCasServletInitializing(event)) {
            addEndpointMappingToCasServlet(event, SamlIdPConstants.ENDPOINT_IDP_METADATA);
            addEndpointMappingToCasServlet(event, SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_POST_CALLBACK);
            addEndpointMappingToCasServlet(event, SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_POST);
        }
    }

}
