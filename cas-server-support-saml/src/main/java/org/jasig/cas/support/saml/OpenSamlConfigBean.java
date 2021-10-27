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


import net.shibboleth.utilities.java.support.xml.ParserPool;
import org.opensaml.core.config.ConfigurationService;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.config.XMLObjectProviderRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

/**
 * Load the opensaml config context.
 * @author Misagh Moayyed
 * @since 4.1
 */
public final class OpenSamlConfigBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenSamlConfigBean.class);

    @Autowired
    @NotNull
    private ParserPool parserPool;

    /**
     * Instantiates the config bean.
     */
    public OpenSamlConfigBean() {}

    /**
     * Gets the configured parser pool.
     *
     * @return the parser pool
     */
    public ParserPool getParserPool() {
        return parserPool;
    }

    /**
     * Initialize opensaml.
     */
    @PostConstruct
    public void init() {
        LOGGER.debug("Initializing OpenSaml configuration...");
        Assert.notNull(this.parserPool, "parserPool cannot be null");

        try {
            InitializationService.initialize();
        } catch (final InitializationException e) {
            throw new RuntimeException("Exception initializing OpenSAML", e);
        }

        XMLObjectProviderRegistry registry;
        synchronized(ConfigurationService.class) {
            registry = ConfigurationService.get(XMLObjectProviderRegistry.class);
            if (registry == null) {
                LOGGER.debug("XMLObjectProviderRegistry did not exist in ConfigurationService, will be created");
                registry = new XMLObjectProviderRegistry();
                ConfigurationService.register(XMLObjectProviderRegistry.class, registry);
            }
        }

        registry.setParserPool(parserPool);
    }
}
