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

package org.jasig.cas.authentication.principal;

import org.jasig.cas.CasProtocolConstants;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.*;

/**
 * Test cases for {@link WebApplicationServiceFactory}.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class WebApplicationServiceFactoryTests {

    @Test
    public void verifyServiceCreationSuccessfullyById() {
        final WebApplicationServiceFactory factory = new WebApplicationServiceFactory();
        final WebApplicationService service = factory.createService("testservice");
        assertNotNull(service);
    }

    @Test
    public void verifyServiceCreationSuccessfullyByService() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "test");
        final WebApplicationServiceFactory factory = new WebApplicationServiceFactory();
        final WebApplicationService service = factory.createService(request);
        assertNotNull(service);
    }

    @Test
    public void verifyServiceCreationSuccessfullyByTargetService() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_TARGET_SERVICE, "test");
        final WebApplicationServiceFactory factory = new WebApplicationServiceFactory();
        final WebApplicationService service = factory.createService(request);
        assertNotNull(service);
    }

    @Test
    public void verifyServiceCreationSuccessfullyByTargetServiceAndTicket() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_TARGET_SERVICE, "test");
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, "ticket");
        request.addParameter(CasProtocolConstants.PARAMETER_METHOD, "post");
        final WebApplicationServiceFactory factory = new WebApplicationServiceFactory();

        final WebApplicationService service = factory.createService(request);
        assertNotNull(service);
        assertEquals(service.getArtifactId(), "ticket");
    }

    @Test
    public void verifyServiceCreationNoService() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, "ticket");
        final WebApplicationServiceFactory factory = new WebApplicationServiceFactory();

        final WebApplicationService service = factory.createService(request);
        assertNull(service);
    }
}
