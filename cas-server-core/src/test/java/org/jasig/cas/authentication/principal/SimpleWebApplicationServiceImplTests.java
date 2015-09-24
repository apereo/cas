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

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Scott Battaglia
 * @author Arnaud Lesueur
 * @since 3.1
 *
 */
public class SimpleWebApplicationServiceImplTests {

    @Test
    public void verifyResponse() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "service");
        final WebApplicationService impl = new WebApplicationServiceFactory().createService(request);

        final Response response = impl.getResponse("ticketId");
        assertNotNull(response);
        assertEquals(Response.ResponseType.REDIRECT, response.getResponseType());
    }

    @Test
    public void verifyCreateSimpleWebApplicationServiceImplFromServiceAttribute() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("service", "service");
        final WebApplicationService impl = new WebApplicationServiceFactory().createService(request);
        assertNotNull(impl);
    }

    @Test
    public void verifyResponseForJsession() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "http://www.cnn.com/;jsession=test");
        final WebApplicationService impl = new WebApplicationServiceFactory().createService(request);

        assertEquals("http://www.cnn.com/", impl.getId());
    }

    @Test
    public void verifyResponseWithNoTicket() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "service");
        final WebApplicationService impl = new WebApplicationServiceFactory().createService(request);

        final Response response = impl.getResponse(null);
        assertNotNull(response);
        assertEquals(Response.ResponseType.REDIRECT, response.getResponseType());
        assertFalse(response.getUrl().contains("ticket="));
    }

    @Test
    public void verifyResponseWithNoTicketAndNoParameterInServiceURL() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "http://foo.com/");
        final WebApplicationService impl = new WebApplicationServiceFactory().createService(request);

        final Response response = impl.getResponse(null);
        assertNotNull(response);
        assertEquals(Response.ResponseType.REDIRECT, response.getResponseType());
        assertFalse(response.getUrl().contains("ticket="));
        assertEquals("http://foo.com/", response.getUrl());
    }

    @Test
    public void verifyResponseWithNoTicketAndOneParameterInServiceURL() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "http://foo.com/?param=test");
        final WebApplicationService impl = new WebApplicationServiceFactory().createService(request);

        final Response response = impl.getResponse(null);
        assertNotNull(response);
        assertEquals(Response.ResponseType.REDIRECT, response.getResponseType());
        assertEquals("http://foo.com/?param=test", response.getUrl());
    }
}
