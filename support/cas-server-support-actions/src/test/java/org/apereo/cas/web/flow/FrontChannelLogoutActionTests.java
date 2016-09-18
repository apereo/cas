package org.apereo.cas.web.flow;

import org.apereo.cas.logout.DefaultSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.DefaultSingleLogoutServiceMessageHandler;
import org.apereo.cas.logout.LogoutManagerImpl;
import org.apereo.cas.logout.LogoutRequest;
import org.apereo.cas.logout.SamlCompliantLogoutMessageCreator;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;
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

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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

    private LogoutManagerImpl logoutManager;

    public FrontChannelLogoutActionTests() {
        MockitoAnnotations.initMocks(this);
    }

    @Before
    public void onSetUp() throws Exception {

        this.logoutManager = new LogoutManagerImpl(new SamlCompliantLogoutMessageCreator());
        final DefaultSingleLogoutServiceMessageHandler handler = new DefaultSingleLogoutServiceMessageHandler();
        
        handler.setLogoutMessageBuilder(new SamlCompliantLogoutMessageCreator());
        handler.setServicesManager(servicesManager);
        handler.setSingleLogoutServiceLogoutUrlBuilder(new DefaultSingleLogoutServiceLogoutUrlBuilder());
        this.logoutManager.setSingleLogoutServiceMessageHandler(handler);

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
    public void verifyLogoutNoIndex() throws Exception {
        WebUtils.putLogoutRequests(this.requestContext, Collections.<LogoutRequest>emptyList());
        final Event event = this.frontChannelLogoutAction.doExecute(this.requestContext);
        assertEquals(FrontChannelLogoutAction.FINISH_EVENT, event.getId());
    }    
}
