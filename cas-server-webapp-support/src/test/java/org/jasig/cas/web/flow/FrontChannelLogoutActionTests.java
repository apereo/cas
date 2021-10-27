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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.Inflater;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.logout.LogoutManager;
import org.jasig.cas.logout.LogoutManagerImpl;
import org.jasig.cas.logout.LogoutRequest;
import org.jasig.cas.logout.LogoutRequestStatus;
import org.jasig.cas.logout.SamlCompliantLogoutMessageCreator;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.util.SimpleHttpClient;
import org.jasig.cas.web.support.WebUtils;
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
public class FrontChannelLogoutActionTests {

    private static final String FLOW_EXECUTION_KEY = "12234";

    private static final String TICKET_ID = "ST-XXX";

    private FrontChannelLogoutAction frontChannelLogoutAction;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    private RequestContext requestContext;

    @Before
    public void onSetUp() throws Exception {
        final LogoutManager logoutManager = new LogoutManagerImpl(mock(ServicesManager.class),
                new SimpleHttpClient(), new SamlCompliantLogoutMessageCreator());
        this.frontChannelLogoutAction = new FrontChannelLogoutAction(logoutManager);

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
    public void testLogoutNoRequest() throws Exception {
        this.requestContext.getFlowScope().put(FrontChannelLogoutAction.LOGOUT_INDEX, 0);
        final Event event = this.frontChannelLogoutAction.doExecute(this.requestContext);
        assertEquals(FrontChannelLogoutAction.FINISH_EVENT, event.getId());
    }

    @Test
    public void testLogoutNoIndex() throws Exception {
        WebUtils.putLogoutRequests(this.requestContext, Collections.<LogoutRequest>emptyList());
        final Event event = this.frontChannelLogoutAction.doExecute(this.requestContext);
        assertEquals(FrontChannelLogoutAction.FINISH_EVENT, event.getId());
    }

    @Test
    public void testLogoutOneLogoutRequestSuccess() throws Exception {
        final LogoutRequest logoutRequest = new LogoutRequest("", null);
        logoutRequest.setStatus(LogoutRequestStatus.SUCCESS);
        WebUtils.putLogoutRequests(this.requestContext, Collections.<LogoutRequest>emptyList());
        this.requestContext.getFlowScope().put(FrontChannelLogoutAction.LOGOUT_INDEX, 0);
        final Event event = this.frontChannelLogoutAction.doExecute(this.requestContext);
        assertEquals(FrontChannelLogoutAction.FINISH_EVENT, event.getId());
    }

    @Test
    public void testLogoutOneLogoutRequestNotAttempted() throws Exception {
        final String FAKE_URL = "http://url";
        LogoutRequest logoutRequest = new LogoutRequest(TICKET_ID, new SimpleWebApplicationServiceImpl(FAKE_URL));
        WebUtils.putLogoutRequests(this.requestContext, Arrays.asList(logoutRequest));
        this.requestContext.getFlowScope().put(FrontChannelLogoutAction.LOGOUT_INDEX, 0);
        final Event event = this.frontChannelLogoutAction.doExecute(this.requestContext);
        assertEquals(FrontChannelLogoutAction.REDIRECT_APP_EVENT, event.getId());
        List<LogoutRequest> list = WebUtils.getLogoutRequests(this.requestContext);
        assertEquals(1, list.size());
        final String url = (String) event.getAttributes().get("logoutUrl");
        assertTrue(url.startsWith(FAKE_URL + "?SAMLRequest="));
        final byte[] samlMessage = Base64.decodeBase64(URLDecoder.decode(StringUtils.substringAfter(url,  "?SAMLRequest="), "UTF-8"));
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
