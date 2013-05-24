/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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
package org.jasig.cas.web.flow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Iterator;
import java.util.zip.Inflater;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.logout.LogoutManager;
import org.jasig.cas.logout.LogoutManagerImpl;
import org.jasig.cas.logout.LogoutRequest;
import org.jasig.cas.logout.LogoutRequestStatus;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.util.HttpClient;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.test.MockFlowExecutionContext;
import org.springframework.webflow.test.MockFlowExecutionKey;

/**
 * @author Jerome Leleu
 * @since 4.0.0
 */
public class FrontLogoutActionTests {

    private static final String FLOW_EXECUTION_KEY = "12234";

    private static final String TICKET_ID = "ST-XXX";

    private FrontLogoutAction frontLogoutAction;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    private RequestContext requestContext;

    @Before
    public void onSetUp() throws Exception {
        final LogoutManager logoutManager = new LogoutManagerImpl(mock(ServicesManager.class), new HttpClient());
        this.frontLogoutAction = new FrontLogoutAction(logoutManager);

        this.request = new MockHttpServletRequest();
        this.response = new MockHttpServletResponse();
        this.requestContext = mock(RequestContext.class);
        final ServletExternalContext servletExternalContext = mock(ServletExternalContext.class);
        when(this.requestContext.getExternalContext()).thenReturn(servletExternalContext);
        when(servletExternalContext.getNativeRequest()).thenReturn(request);
        when(servletExternalContext.getNativeResponse()).thenReturn(response);
        final LocalAttributeMap flowScope = new LocalAttributeMap();
        when(this.requestContext.getFlowScope()).thenReturn(flowScope);
        final MockFlowExecutionKey mockFlowExecutionKey = new MockFlowExecutionKey(FLOW_EXECUTION_KEY);
        final MockFlowExecutionContext mockFlowExecutionContext = new MockFlowExecutionContext();
        mockFlowExecutionContext.setKey(mockFlowExecutionKey);
        when(this.requestContext.getFlowExecutionContext()).thenReturn(mockFlowExecutionContext);
    }

    @Test
    public void testLogoutNoIterator() throws Exception {
        final Event event = this.frontLogoutAction.doExecute(this.requestContext);
        assertEquals(FrontLogoutAction.FINISH_EVENT, event.getId());
    }

    @Test
    public void testLogoutOneLogoutRequestSuccess() throws Exception {
        final LogoutRequest logoutRequest = new LogoutRequest("", null);
        logoutRequest.setStatus(LogoutRequestStatus.SUCCESS);
        this.requestContext.getFlowScope().put(FrontLogoutAction.LOGOUT_REQUESTS, Arrays.asList(logoutRequest).iterator());
        final Event event = this.frontLogoutAction.doExecute(this.requestContext);
        assertEquals(FrontLogoutAction.FINISH_EVENT, event.getId());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testLogoutOneLogoutRequestNotAttempted() throws Exception {
        final String FAKE_URL = "http://url";
        LogoutRequest logoutRequest = new LogoutRequest(TICKET_ID, new SimpleWebApplicationServiceImpl(FAKE_URL));
        this.requestContext.getFlowScope().put(FrontLogoutAction.LOGOUT_REQUESTS, Arrays.asList(logoutRequest).iterator());
        final Event event = this.frontLogoutAction.doExecute(this.requestContext);
        assertEquals(FrontLogoutAction.REDIRECT_APP_EVENT, event.getId());
        Iterator<LogoutRequest> iterator =
                (Iterator<LogoutRequest>) this.requestContext.getFlowScope().get(FrontLogoutAction.LOGOUT_REQUESTS);
        assertFalse(iterator.hasNext());
        final String url = (String) event.getAttributes().get("logoutUrl");
        assertTrue(url.startsWith(FAKE_URL + "?SAMLRequest="));
        assertTrue(url.endsWith("&RelayState=" + FLOW_EXECUTION_KEY));
        final byte[] samlMessage = Base64.decodeBase64(StringUtils.substringBetween(url,  "?SAMLRequest=", "&RelayState="));
        final Inflater decompresser = new Inflater();
        decompresser.setInput(samlMessage);
        final byte[] result = new byte[1000];
        decompresser.inflate(result);
        decompresser.end();
        final String message = new String(result);
        assertTrue(message.startsWith("<samlp:LogoutRequest xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" ID=\""));
        assertTrue(message.indexOf("<samlp:SessionIndex>" + TICKET_ID + "</samlp:SessionIndex>") >= 0);
    }
}
