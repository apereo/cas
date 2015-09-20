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

import org.jasig.cas.support.saml.authentication.principal.SamlService;
import org.jasig.cas.util.UniqueTicketIdGenerator;
import org.jasig.cas.web.AbstractServletContextInitializer;
import org.jasig.cas.web.support.ArgumentExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebListener;
import java.util.List;
import java.util.Map;

/**
 * Initializes the CAS root servlet context to make sure
 * SAML validation endpoint can be activated by the main CAS servlet.
 * @author Misagh Moayyed
 * @since 4.2
 */
@WebListener
@Component
public class SamlServletContextListener extends AbstractServletContextInitializer {

    @Autowired
    @Qualifier("samlArgumentExtractor")
    private ArgumentExtractor samlArgumentExtractor;

    @Autowired
    @Qualifier("samlServiceTicketUniqueIdGenerator")
    private UniqueTicketIdGenerator samlServiceTicketUniqueIdGenerator;

    @Override
    public void initializeServletContext(final ServletContextEvent sce) {
        addEndpointMappingToCasServlet(sce, SamlProtocolConstants.ENDPOINT_SAML_VALIDATE);
    }

    @Override
    protected void initializeRootApplicationContext() {
        addArgumentExtractor(this.samlArgumentExtractor);
        addServiceTicketUniqueIdGenerator(SamlService.class.getCanonicalName(),
                this.samlServiceTicketUniqueIdGenerator);
    }

    @Override
    protected void initializeServletApplicationContext() {
        addControllerToCasServletHandlerMapping(SamlProtocolConstants.ENDPOINT_SAML_VALIDATE,
                "samlValidateController");
    }
}
