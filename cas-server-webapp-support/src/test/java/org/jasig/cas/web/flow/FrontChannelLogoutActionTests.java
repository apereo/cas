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
package org.jasig.cas.web.flow;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.Inflater;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.authentication.principal.SingleLogoutService;
import org.jasig.cas.logout.LogoutManager;
import org.jasig.cas.logout.LogoutManagerImpl;
import org.jasig.cas.logout.DefaultLogoutRequest;
import org.jasig.cas.logout.LogoutRequest;
import org.jasig.cas.logout.LogoutRequestStatus;
import org.jasig.cas.logout.SamlCompliantLogoutMessageCreator;
import org.jasig.cas.mock.MockTicketGrantingTicket;
import org.jasig.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.jasig.cas.services.LogoutType;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceImpl;
import org.jasig.cas.services.ServicesManager;

import org.jasig.cas.util.CompressionUtils;
import org.jasig.cas.util.http.SimpleHttpClientFactoryBean;
import org.jasig.cas.web.support.WebUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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

    private static final String TEST_URL = "https://www.apereo.org";

    private FrontChannelLogoutAction frontChannelLogoutAction;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    private RequestContext requestContext;

    @Mock
    private ServicesManager servicesManager;

    private LogoutManager logoutManager;

    public FrontChannelLogoutActionTests() {
        MockitoAnnotations.initMocks(this);
    }

    @Before
    public void onSetUp() throws Exception {

        this.logoutManager = new LogoutManagerImpl(this.servicesManager,
                new SimpleHttpClientFactoryBean().getObject(), new SamlCompliantLogoutMessageCreator());
        this.frontChannelLogoutAction = new FrontChannelLogoutAction(this.logoutManager);

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
    public void verifyLogoutNoRequest() throws Exception {
        this.requestContext.getFlowScope().put(FrontChannelLogoutAction.LOGOUT_INDEX, 0);
        final Event event = this.frontChannelLogoutAction.doExecute(this.requestContext);
        assertEquals(FrontChannelLogoutAction.FINISH_EVENT, event.getId());
    }

    @Test
    public void verifyLogoutNoIndex() throws Exception {
        WebUtils.putLogoutRequests(this.requestContext, Collections.<LogoutRequest>emptyList());
        final Event event = this.frontChannelLogoutAction.doExecute(this.requestContext);
        assertEquals(FrontChannelLogoutAction.FINISH_EVENT, event.getId());
    }

    @Test
    public void verifyLogoutOneLogoutRequestSuccess() throws Exception {
        final DefaultLogoutRequest logoutRequest = new DefaultLogoutRequest("", null, null);
        logoutRequest.setStatus(LogoutRequestStatus.SUCCESS);
        WebUtils.putLogoutRequests(this.requestContext, Collections.<LogoutRequest>emptyList());
        this.requestContext.getFlowScope().put(FrontChannelLogoutAction.LOGOUT_INDEX, 0);
        final Event event = this.frontChannelLogoutAction.doExecute(this.requestContext);
        assertEquals(FrontChannelLogoutAction.FINISH_EVENT, event.getId());
    }

    @Test
    public void verifyLogoutOneLogoutRequestNotAttempted() throws Exception {
        final LogoutRequest logoutRequest = new DefaultLogoutRequest(TICKET_ID,
                new SimpleWebApplicationServiceImpl(TEST_URL),
                new URL(TEST_URL));
        final Event event = getLogoutEvent(Arrays.asList(logoutRequest));

        assertEquals(FrontChannelLogoutAction.REDIRECT_APP_EVENT, event.getId());
        final List<LogoutRequest> list = WebUtils.getLogoutRequests(this.requestContext);
        assertEquals(1, list.size());
        final String url = (String) event.getAttributes().get(FrontChannelLogoutAction.DEFAULT_FLOW_ATTRIBUTE_LOGOUT_URL);
        assertTrue(url.startsWith(TEST_URL + "?" + FrontChannelLogoutAction.DEFAULT_LOGOUT_PARAMETER + "="));
        final byte[] samlMessage = CompressionUtils.decodeBase64ToByteArray(
                URLDecoder.decode(StringUtils.substringAfter(url, "?" + FrontChannelLogoutAction.DEFAULT_LOGOUT_PARAMETER + "="), "UTF-8"));
        final Inflater decompresser = new Inflater();
        decompresser.setInput(samlMessage);
        final byte[] result = new byte[1000];
        decompresser.inflate(result);
        decompresser.end();
        final String message = new String(result);
        assertTrue(message.startsWith("<samlp:LogoutRequest xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" ID=\""));
        assertTrue(message.contains("<samlp:SessionIndex>" + TICKET_ID + "</samlp:SessionIndex>"));
    }

    @Test
    public void verifyLogoutUrlForServiceIsUsed() throws Exception {
        final RegisteredService svc = getRegisteredService();
        when(this.servicesManager.findServiceBy(any(SingleLogoutService.class))).thenReturn(svc);

        final SingleLogoutService service = mock(SingleLogoutService.class);
        when(service.getId()).thenReturn(svc.getServiceId());
        when(service.getOriginalUrl()).thenReturn(svc.getServiceId());

        final MockTicketGrantingTicket tgt = new MockTicketGrantingTicket("test");
        tgt.getServices().put("service", service);
        final Event event = getLogoutEvent(this.logoutManager.performLogout(tgt));
        assertEquals(FrontChannelLogoutAction.REDIRECT_APP_EVENT, event.getId());
        final List<LogoutRequest> list = WebUtils.getLogoutRequests(this.requestContext);
        assertEquals(1, list.size());
        final String url = (String) event.getAttributes().get(FrontChannelLogoutAction.DEFAULT_FLOW_ATTRIBUTE_LOGOUT_URL);
        assertTrue(url.startsWith(svc.getLogoutUrl().toExternalForm()));

    }

    private RegisteredService getRegisteredService() throws MalformedURLException {
        final RegisteredServiceImpl svc = new RegisteredServiceImpl();
        svc.setServiceId("https://www.github.com");
        svc.setLogoutUrl(new URL("https://www.google.com"));
        svc.setName("Service logout test");
        svc.setLogoutType(LogoutType.FRONT_CHANNEL);
        svc.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(true, true));
        return svc;
    }

    private Event getLogoutEvent(final List<LogoutRequest> requests) throws Exception {
        WebUtils.putLogoutRequests(this.requestContext, requests);
        this.requestContext.getFlowScope().put(FrontChannelLogoutAction.LOGOUT_INDEX, 0);
        return this.frontChannelLogoutAction.doExecute(this.requestContext);
    }
}
