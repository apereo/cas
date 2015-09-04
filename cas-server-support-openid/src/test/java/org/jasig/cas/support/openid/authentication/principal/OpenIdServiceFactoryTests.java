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

package org.jasig.cas.support.openid.authentication.principal;

import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.support.openid.OpenIdProtocolConstants;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.*;

/**
 * Test cases for {@link OpenIdServiceFactory}.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class OpenIdServiceFactoryTests {

    @Test
    public void verifyServiceCreationSuccessfullyById() {

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(OpenIdProtocolConstants.OPENID_RETURNTO, "test");
        request.addParameter(OpenIdProtocolConstants.OPENID_IDENTITY, "identity");
        final OpenIdServiceFactory factory = new OpenIdServiceFactory();
        final WebApplicationService service = factory.createService(request);
        assertNotNull(service);
    }

    @Test
    public void verifyServiceCreationMissingReturn() {

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(OpenIdProtocolConstants.OPENID_IDENTITY, "identity");
        final OpenIdServiceFactory factory = new OpenIdServiceFactory();
        final WebApplicationService service = factory.createService(request);
        assertNull(service);
    }

    @Test
    public void verifyServiceCreationMissingId() {

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(OpenIdProtocolConstants.OPENID_RETURNTO, "test");
        final OpenIdServiceFactory factory = new OpenIdServiceFactory();
        final WebApplicationService service = factory.createService(request);
        assertNull(service);
    }

}
