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
package org.jasig.cas.support.pac4j;

import org.jasig.cas.support.pac4j.authentication.ClientAuthenticationMetaDataPopulator;
import org.jasig.cas.support.pac4j.authentication.handler.support.ClientAuthenticationHandler;
import org.jasig.cas.web.AbstractServletContextInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.servlet.annotation.WebListener;

/**
 * Initializes the pac4j support for authentication delegation.
 *
 * @author Jerome Leleu
 * @since 4.2.0
 */
@WebListener
@Component
public class Pac4jServletContextListener extends AbstractServletContextInitializer {

    @Autowired
    @Qualifier("clientAuthenticationHandler")
    private ClientAuthenticationHandler clientAuthenticationHandler;

    @Autowired
    @Qualifier("clientAuthenticationMetaDataPopulator")
    private ClientAuthenticationMetaDataPopulator clientAuthenticationMetaDataPopulator;

    @Override
    protected void initializeRootApplicationContext() {
        addAuthenticationHandler(clientAuthenticationHandler);
        addAuthenticationMetadataPopulator(clientAuthenticationMetaDataPopulator);
    }
}
