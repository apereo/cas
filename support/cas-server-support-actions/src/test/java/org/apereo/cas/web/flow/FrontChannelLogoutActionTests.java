package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.logout.DefaultLogoutExecutionPlan;
import org.apereo.cas.logout.DefaultLogoutManager;
import org.apereo.cas.logout.SamlCompliantLogoutMessageCreator;
import org.apereo.cas.logout.slo.DefaultSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.slo.DefaultSingleLogoutServiceMessageHandler;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.http.SimpleHttpClientFactoryBean;
import org.apereo.cas.web.SimpleUrlValidatorFactoryBean;
import org.apereo.cas.web.flow.logout.FrontChannelLogoutAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.test.MockFlowExecutionContext;
import org.springframework.webflow.test.MockFlowExecutionKey;

import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Jerome Leleu
 * @since 4.0.0
 */
public class FrontChannelLogoutActionTests {

    private static final String FLOW_EXECUTION_KEY = "12234";
    private FrontChannelLogoutAction frontChannelLogoutAction;

    private RequestContext requestContext;

    @Mock
    private ServicesManager servicesManager;

    public FrontChannelLogoutActionTests() {
        MockitoAnnotations.initMocks(this);
    }

    @Before
    public void onSetUp() {
        val validator = new SimpleUrlValidatorFactoryBean(false).getObject();

        val handler = new DefaultSingleLogoutServiceMessageHandler(new SimpleHttpClientFactoryBean().getObject(),
            new SamlCompliantLogoutMessageCreator(), servicesManager, new DefaultSingleLogoutServiceLogoutUrlBuilder(validator), false,
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()));

        val plan = new DefaultLogoutExecutionPlan();
        plan.registerSingleLogoutServiceMessageHandler(handler);
        val logoutManager = new DefaultLogoutManager(new SamlCompliantLogoutMessageCreator(), false, plan);

        this.frontChannelLogoutAction = new FrontChannelLogoutAction(logoutManager);

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        this.requestContext = mock(RequestContext.class);
        val servletExternalContext = mock(ServletExternalContext.class);
        when(this.requestContext.getExternalContext()).thenReturn(servletExternalContext);
        when(servletExternalContext.getNativeRequest()).thenReturn(request);
        when(servletExternalContext.getNativeResponse()).thenReturn(response);
        val flowScope = new LocalAttributeMap();
        when(this.requestContext.getFlowScope()).thenReturn(flowScope);
        val mockFlowExecutionKey = new MockFlowExecutionKey(FLOW_EXECUTION_KEY);
        val mockFlowExecutionContext = new MockFlowExecutionContext();
        mockFlowExecutionContext.setKey(mockFlowExecutionKey);
        when(this.requestContext.getFlowExecutionContext()).thenReturn(mockFlowExecutionContext);
    }

    @Test
    public void verifyLogoutNoIndex() {
        WebUtils.putLogoutRequests(this.requestContext, new ArrayList<>(0));
        val event = this.frontChannelLogoutAction.doExecute(this.requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_FINISH, event.getId());
    }
}
