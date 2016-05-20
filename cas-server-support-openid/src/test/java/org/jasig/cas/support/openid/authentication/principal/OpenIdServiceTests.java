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

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Response;
import org.jasig.cas.util.ApplicationContextProvider;
import org.junit.Before;
import org.junit.Test;
import org.openid4java.association.Association;
import org.openid4java.server.ServerAssociationStore;
import org.openid4java.server.ServerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.1
 */
public class OpenIdServiceTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenIdServiceTests.class);

    private OpenIdService openIdService;
    private ApplicationContext context;
    private CentralAuthenticationService cas;
    private ServerManager manager;
    private ServerAssociationStore sharedAssociations;
    private final MockHttpServletRequest request = new MockHttpServletRequest();

    @Before
    public void setUp() throws Exception {
        request.addParameter("openid.identity", "http://openid.ja-sig.org/battags");
        request.addParameter("openid.return_to", "http://www.ja-sig.org/?service=fa");
        request.addParameter("openid.mode", "checkid_setup");
        sharedAssociations = mock(ServerAssociationStore.class);
        manager = new ServerManager();
        manager.setOPEndpointUrl("https://localshot:8443/cas/login");
        manager.setEnforceRpId(false);
        manager.setSharedAssociations(sharedAssociations);
        context = mock(ApplicationContext.class);
        cas = mock(CentralAuthenticationService.class);

        when(context.getBean("serverManager")).thenReturn(manager);
        when(context.getBean("centralAuthenticationService", CentralAuthenticationService.class)).thenReturn(cas);

        final ApplicationContextProvider contextProvider = new ApplicationContextProvider();
        contextProvider.setApplicationContext(context);
    }

    @Test
    public void verifyGetResponse() {
        openIdService = OpenIdService.createServiceFrom(request, null);
        final Response response = this.openIdService.getResponse("test");
        try {
            verify(cas, never()).validateServiceTicket("test", openIdService);
        } catch (final Exception e) {
            LOGGER.debug("Exception during verification of service ticket", e);
        }
        assertNotNull(response);

        assertEquals("test", response.getAttributes().get("openid.assoc_handle"));
        assertEquals("http://www.ja-sig.org/?service=fa", response.getAttributes().get("openid.return_to"));
        assertEquals("http://openid.ja-sig.org/battags", response.getAttributes().get("openid.identity"));

        final Response response2 = this.openIdService.getResponse(null);
        assertEquals("cancel", response2.getAttributes().get("openid.mode"));
    }

    @Test
    public void verifySmartModeGetResponse() {
        request.addParameter("openid.assoc_handle", "test");
        openIdService = OpenIdService.createServiceFrom(request, null);
        Association association = null;
        try {
            association = Association.generate(Association.TYPE_HMAC_SHA1, "test", 60);
        } catch (final Exception e) {
            fail("Could not generate association");
        }
        when(sharedAssociations.load("test")).thenReturn(association);
        final Response response = this.openIdService.getResponse("test");
        try {
            verify(cas).validateServiceTicket("test", openIdService);
        } catch (final Exception e) {
            fail("Error while validating ticket");
        }

        request.removeParameter("openid.assoc_handle");
        assertNotNull(response);

        assertEquals("test", response.getAttributes().get("openid.assoc_handle"));
        assertEquals("http://www.ja-sig.org/?service=fa", response.getAttributes().get("openid.return_to"));
        assertEquals("http://openid.ja-sig.org/battags", response.getAttributes().get("openid.identity"));
    }

    @Test
    public void verifyExpiredAssociationGetResponse() {
        request.addParameter("openid.assoc_handle", "test");
        openIdService = OpenIdService.createServiceFrom(request, null);
        Association association = null;
        try {
            association = Association.generate(Association.TYPE_HMAC_SHA1, "test", 2);
        } catch (final Exception e) {
            fail("Could not generate association");
        }
        when(sharedAssociations.load("test")).thenReturn(association);
        synchronized (this) {
            try {
                this.wait(3000);
            } catch (final InterruptedException ie) {
                fail("Could not wait long enough to check association expiry date");
            }
        }
        final Response response = this.openIdService.getResponse("test");
        request.removeParameter("openid.assoc_handle");
        assertNotNull(response);

        assertEquals(1, response.getAttributes().size());
        assertEquals("cancel", response.getAttributes().get("openid.mode"));
    }

    @Test
    public void verifyEquals() {
        final MockHttpServletRequest request1 = new MockHttpServletRequest();
        request1.addParameter("openid.identity", "http://openid.ja-sig.org/battags");
        request1.addParameter("openid.return_to", "http://www.ja-sig.org/?service=fa");
        request1.addParameter("openid.mode", "openid.checkid_setup");

        final MockHttpServletRequest request2 = new MockHttpServletRequest();
        request2.addParameter("openid.identity", "http://openid.ja-sig.org/battags");
        request2.addParameter("openid.return_to", "http://www.ja-sig.org/?service=fa");

        final OpenIdService o1 = OpenIdService.createServiceFrom(request1, null);
        final OpenIdService o2 = OpenIdService.createServiceFrom(request2, null);

        assertTrue(o1.equals(o2));
        assertFalse(o1.equals(new Object()));
    }
}
