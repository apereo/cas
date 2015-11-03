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

import org.jasig.cas.authentication.AuthenticationManager;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.web.support.ArgumentExtractor;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Scott Battaglia

 * @since 3.0.0
 */
@ContextConfiguration(locations = {
        "classpath:/core-context.xml"
})
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class AbstractCentralAuthenticationServiceTest {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired(required = true)
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired(required = true)
    private TicketRegistry ticketRegistry;

    @Autowired(required = true)
    private AuthenticationManager authenticationManager;

    @Autowired(required = true)
    private ServicesManager servicesManager;

    @Autowired
    private ArgumentExtractor argumentExtractor;

    public ArgumentExtractor getArgumentExtractor() {
        return argumentExtractor;
    }

    public AuthenticationManager getAuthenticationManager() {
        return this.authenticationManager;
    }

    public CentralAuthenticationService getCentralAuthenticationService() {
        return this.centralAuthenticationService;
    }

    public void setCentralAuthenticationService(final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    public TicketRegistry getTicketRegistry() {
        return this.ticketRegistry;
    }

    public ServicesManager getServicesManager() {
        return this.servicesManager;
    }
}
