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
package org.jasig.cas.support.openid.web.mvc;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.openid4java.server.ServerManager;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletResponse;

import java.util.Map;

/**
 * Test case of the Smart OpenId Controller.
 * @author Frederic Esnault
 * @since 3.0.0
 */
public class SmartOpenIdControllerTest {
    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final HttpServletResponse response = new MockHttpServletResponse();
    private ServerManager manager;
    private final SmartOpenIdController smartOpenIdController = new SmartOpenIdController();

    @Before
    public void setUp() {
        manager = new ServerManager();
        manager.setOPEndpointUrl("https://localshot:8443/cas/login");
        manager.setEnforceRpId(false);
        smartOpenIdController.setServerManager(manager);
    }

    @Test
    public void verifyCanHandle() {
        request.addParameter("openid.mode", "associate");
        final boolean canHandle = smartOpenIdController.canHandle(request, response);
        request.removeParameter("openid.mode");
        assertEquals(true, canHandle);
    }

    @Test
    public void verifyCannotHandle() {
        request.addParameter("openid.mode", "anythingElse");
        final boolean canHandle = smartOpenIdController.canHandle(request, response);
        request.removeParameter("openid.mode");
        assertEquals(false, canHandle);
    }

    @Test
    public void verifyGetAssociationResponse() {
        request.addParameter("openid.mode", "associate");
        request.addParameter("openid.session_type", "DH-SHA1");
        request.addParameter("openid.assoc_type", "HMAC-SHA1");
        request.addParameter("openid.dh_consumer_public",
                "NzKoFMyrzFn/5iJFPdX6MVvNA/BChV1/sJdnYbupDn7ptn+cerwEzyFfWFx25KsoLSkxQCaSMmYtc1GPy/2GI1BSKSDhpdJmDBb"
                + "QRa/9Gs+giV/5fHcz/mHz8sREc7RTGI+0Ka9230arwrWt0fnoaJLRKEGUsmFR71rCo4EUOew=");
        final Map<String, String> assocResponse = smartOpenIdController.getAssociationResponse(request);
        assertTrue(assocResponse.containsKey("assoc_handle"));
        assertTrue(assocResponse.containsKey("expires_in"));
        assertTrue(assocResponse.containsKey("dh_server_public"));
        assertTrue(assocResponse.containsKey("enc_mac_key"));
        request.removeParameter("openid.mode");
    }
}
