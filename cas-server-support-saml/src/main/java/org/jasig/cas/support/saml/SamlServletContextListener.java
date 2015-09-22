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

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.support.saml.authentication.principal.GoogleAccountsServiceFactory;
import org.jasig.cas.support.saml.authentication.principal.SamlService;
import org.jasig.cas.support.saml.authentication.principal.SamlServiceFactory;
import org.jasig.cas.util.PrivateKeyFactoryBean;
import org.jasig.cas.util.PublicKeyFactoryBean;
import org.jasig.cas.util.UniqueTicketIdGenerator;
import org.jasig.cas.web.AbstractServletContextInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

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
    @Qualifier("samlServiceFactory")
    private SamlServiceFactory samlServiceFactory;

    @Autowired
    @Qualifier("googleAccountsServiceFactory")
    private GoogleAccountsServiceFactory googleAccountsServiceFactory;

    @Autowired
    @Qualifier("samlServiceTicketUniqueIdGenerator")
    private UniqueTicketIdGenerator samlServiceTicketUniqueIdGenerator;

    @Override
    public void initializeServletContext(final ServletContextEvent sce) {
        addEndpointMappingToCasServlet(sce, SamlProtocolConstants.ENDPOINT_SAML_VALIDATE);
    }

    @Override
    protected void initializeRootApplicationContext() {
        addServiceFactory(samlServiceFactory);
        addServiceFactory(googleAccountsServiceFactory);
        addServiceTicketUniqueIdGenerator(SamlService.class.getCanonicalName(),
                this.samlServiceTicketUniqueIdGenerator);
    }

    @Override
    protected void initializeServletApplicationContext() {
        addControllerToCasServletHandlerMapping(SamlProtocolConstants.ENDPOINT_SAML_VALIDATE,
                "samlValidateController");
    }

    @Component
    @Configuration
    private static class GoogleAppsConfigurationInitializer {

        private final Logger logger = LoggerFactory.getLogger(getClass());

        @Value("${cas.saml.googleapps.publickey.file:}")
        private String publicKeyLocation;

        @Value("${cas.saml.googleapps.privatekey.file:}")
        private String privateKeyLocation;


        @Value("${cas.saml.googleapps.key.alg:}")
        private String keyAlgorithm;

        protected GoogleAppsConfigurationInitializer() {}

        @Bean(name="googleAppsPrivateKey", autowire = Autowire.BY_NAME)
        public PrivateKey getGoogleAppsPrivateKey() throws Exception {
            if (!isValidConfiguration()) {
                logger.debug("Google Apps private key bean will not be created, because it's not configured");
                return null;

            }
            final PrivateKeyFactoryBean bean = new PrivateKeyFactoryBean();

            if (this.privateKeyLocation.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
                bean.setLocation(new ClassPathResource(
                    StringUtils.removeStart(this.privateKeyLocation, ResourceUtils.CLASSPATH_URL_PREFIX)));
            } else {
                bean.setLocation(new FileSystemResource(this.privateKeyLocation));
            }

            bean.setAlgorithm(this.keyAlgorithm);
            bean.afterPropertiesSet();

            logger.debug("Creating Google Apps private key instance via {}", this.publicKeyLocation);
            return bean.getObject();
        }

        @Bean(name="googleAppsPublicKey", autowire = Autowire.BY_NAME)
        public PublicKey getGoogleAppsPublicKey() throws Exception {
            if (!isValidConfiguration()) {
                logger.debug("Google Apps public key bean will not be created, because it's not configured");
                return null;
            }

            final PublicKeyFactoryBean bean = new PublicKeyFactoryBean();
            if (this.publicKeyLocation.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
                bean.setLocation(new ClassPathResource(
                    StringUtils.removeStart(this.publicKeyLocation, ResourceUtils.CLASSPATH_URL_PREFIX)));
            } else {
                bean.setLocation(new FileSystemResource(this.publicKeyLocation));
            }

            bean.setAlgorithm(this.keyAlgorithm);
            bean.afterPropertiesSet();

            logger.debug("Creating Google Apps public key instance via {}", this.publicKeyLocation);
            return bean.getObject();
        }

        @Bean(name="serviceFactoryList")
        public List getServiceFactoryList() {
            return new ArrayList();
        }

        private boolean isValidConfiguration() {
            return StringUtils.isNotBlank(this.privateKeyLocation)
                    || StringUtils.isNotBlank(this.publicKeyLocation)
                    || StringUtils.isNotBlank(this.keyAlgorithm);
        }
    }

}
