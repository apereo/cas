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

import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.principal.Response;
import org.jasig.cas.support.openid.AbstractOpenIdTests;
import org.jasig.cas.support.openid.OpenIdProtocolConstants;
import org.junit.Before;
import org.junit.Test;
import org.openid4java.association.Association;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.1
 */
public class OpenIdServiceTests extends AbstractOpenIdTests {


    private OpenIdService openIdService;

    private final MockHttpServletRequest request = new MockHttpServletRequest();

    private Association association;

    @Before
    public void setUp() throws Exception {
        request.addParameter(OpenIdProtocolConstants.OPENID_IDENTITY, "http://openid.ja-sig.org/battags");
        request.addParameter(OpenIdProtocolConstants.OPENID_RETURNTO, "http://www.ja-sig.org/?service=fa");
        request.addParameter(OpenIdProtocolConstants.OPENID_MODE, "checkid_setup");
        association = sharedAssociations.generate(Association.TYPE_HMAC_SHA1, 2);
    }

    @Test
    public void verifyGetResponse() {
        try {
            request.removeParameter(OpenIdProtocolConstants.OPENID_ASSOCHANDLE);
            request.addParameter(OpenIdProtocolConstants.OPENID_ASSOCHANDLE, association.getHandle());

            openIdService = openIdServiceFactory.createService(request);

            final String tgt = centralAuthenticationService.createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword()).getId();
            final String st = centralAuthenticationService.grantServiceTicket(tgt, openIdService).getId();
            centralAuthenticationService.validateServiceTicket(st, openIdService);

            final Response response = this.openIdService.getResponse(st);
            assertNotNull(response);

            assertEquals(association.getHandle(), response.getAttributes().get(OpenIdProtocolConstants.OPENID_ASSOCHANDLE));
            assertEquals("http://www.ja-sig.org/?service=fa", response.getAttributes().get(OpenIdProtocolConstants.OPENID_RETURNTO));
            assertEquals("http://openid.ja-sig.org/battags", response.getAttributes().get(OpenIdProtocolConstants.OPENID_IDENTITY));

            final Response response2 = this.openIdService.getResponse(null);
            assertEquals("cancel", response2.getAttributes().get(OpenIdProtocolConstants.OPENID_MODE));
        } catch (final Exception e) {
            logger.debug("Exception during verification of service ticket", e);
        }

    }


    @Test
    public void verifyExpiredAssociationGetResponse() {

        try {
            request.removeParameter(OpenIdProtocolConstants.OPENID_ASSOCHANDLE);
            request.addParameter(OpenIdProtocolConstants.OPENID_ASSOCHANDLE, association.getHandle());

            openIdService = openIdServiceFactory.createService(request);

            final String tgt = centralAuthenticationService.createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword()).getId();
            final String st = centralAuthenticationService.grantServiceTicket(tgt, openIdService).getId();
            centralAuthenticationService.validateServiceTicket(st, openIdService);

            synchronized (this) {
                try {
                    this.wait(3000);
                } catch (final InterruptedException ie) {
                    fail("Could not wait long enough to check association expiry date");
                }
            }

            final Response response = this.openIdService.getResponse(st);
            assertNotNull(response);

            assertEquals(2, response.getAttributes().size());
            assertEquals("cancel", response.getAttributes().get("openid.mode"));
        } catch (final Exception e) {
            logger.debug("Exception during verification of service ticket", e);
        }
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

        final OpenIdService o1 = openIdServiceFactory.createService(request);
        final OpenIdService o2 = openIdServiceFactory.createService(request);

        assertTrue(o1.equals(o2));
        assertFalse(o1.equals(new Object()));
    }
}
