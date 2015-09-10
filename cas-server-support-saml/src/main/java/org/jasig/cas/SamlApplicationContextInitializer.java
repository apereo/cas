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

package org.jasig.cas;

import org.jasig.cas.support.saml.SamlProtocolConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.mvc.Controller;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * This component is loaded by the CAS servlet web application context
 * automatically. It is responsible for registering beans
 * into the web application context.
 * @author Misagh Moayyed
 * @since 4.2
 */
@Component
public class SamlApplicationContextInitializer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("handlerMappingC")
    private SimpleUrlHandlerMapping handlerMappingC;


    @Autowired
    @Qualifier("samlValidateController")
    private Controller samlValidateController;

    /**
     * Initializes the CAS servlet application context
     * to make sure the saml validation controller can respond
     * to requests.
     */
    @PostConstruct
    public void initialize() {
        logger.info("Initializing Saml application context");
        final Map<String, Object> urlMap = (Map<String, Object>) handlerMappingC.getUrlMap();
        urlMap.put(SamlProtocolConstants.ENDPOINT_SAML_VALIDATE, this.samlValidateController);
        handlerMappingC.initApplicationContext();
    }
}
