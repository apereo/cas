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

import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.support.oauth.services.OAuthCallbackAuthorizeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebListener;
import java.security.SecureRandom;
import java.util.Map;

/**
 * Initializes the CAS root servlet context to make sure
 * OAuth endpoint can be activated by the main CAS servlet.
 * @author Misagh Moayyed
 * @since 4.2
 */
@WebListener
@Component
public class OAuthServletContextListener implements ServletContextListener, ApplicationContextAware {

    private static final String CAS_SERVLET_NAME = "cas";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${server.prefix}" + OAuthConstants.ENDPOINT_OAUTH2_CALLBACK_AUTHORIZE)
    private String callbackAuthorizeUrl;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        logger.info("Initializing OAuth servlet context...");
        final ServletRegistration registration = sce.getServletContext().getServletRegistration(CAS_SERVLET_NAME);
        registration.addMapping(OAuthConstants.ENDPOINT_OAUTH2);
        logger.info("Added [{}] to {} servlet context", OAuthConstants.ENDPOINT_OAUTH2, CAS_SERVLET_NAME);
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {}

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        try {
            if (applicationContext.getParent() != null) {
                logger.info("Initializing OAuth application context");
                final SimpleUrlHandlerMapping handlerMappingC = applicationContext.getBean("handlerMappingC",
                        SimpleUrlHandlerMapping.class);
                final Controller controller = applicationContext.getBean("oauth20WrapperController",
                        Controller.class);
                final Map<String, Object> urlMap = (Map<String, Object>) handlerMappingC.getUrlMap();
                urlMap.put(OAuthConstants.ENDPOINT_OAUTH2, controller);
                handlerMappingC.initApplicationContext();

                final OAuthCallbackAuthorizeService service = new OAuthCallbackAuthorizeService();
                service.setId(new SecureRandom().nextLong());
                service.setName(service.getClass().getSimpleName());
                service.setDescription("OAuth Wrapper Callback Url");
                service.setServiceId(this.callbackAuthorizeUrl);

                this.servicesManager.save(service);

                logger.info("Initialized OAuth application context successfully");
            }
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
