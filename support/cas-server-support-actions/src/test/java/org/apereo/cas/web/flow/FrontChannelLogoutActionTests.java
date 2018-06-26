package org.apereo.cas.web.flow;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.logout.DefaultLogoutManager;
import org.apereo.cas.logout.DefaultSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.DefaultSingleLogoutServiceMessageHandler;
import org.apereo.cas.logout.LogoutExecutionPlan;
import org.apereo.cas.logout.SamlCompliantLogoutMessageCreator;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.http.SimpleHttpClientFactoryBean;
import org.apereo.cas.web.SimpleUrlValidatorFactoryBean;
import org.apereo.cas.web.flow.logout.FrontChannelLogoutAction;
import org.apereo.cas.web.support.WebUtils;
import org.junit.Test;
import org.junit.Before;
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
@Slf4j
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
        final var validator = new SimpleUrlValidatorFactoryBean(false).getObject();

        final var handler = new DefaultSingleLogoutServiceMessageHandler(new SimpleHttpClientFactoryBean().getObject(),
            new SamlCompliantLogoutMessageCreator(), servicesManager, new DefaultSingleLogoutServiceLogoutUrlBuilder(validator), false,
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()));
        final var logoutManager = new DefaultLogoutManager(new SamlCompliantLogoutMessageCreator(),
            handler, false, mock(LogoutExecutionPlan.class));

        this.frontChannelLogoutAction = new FrontChannelLogoutAction(logoutManager);

        final var request = new MockHttpServletRequest();
        final var response = new MockHttpServletResponse();
        this.requestContext = mock(RequestContext.class);
        final var servletExternalContext = mock(ServletExternalContext.class);
        when(this.requestContext.getExternalContext()).thenReturn(servletExternalContext);
        when(servletExternalContext.getNativeRequest()).thenReturn(request);
        when(servletExternalContext.getNativeResponse()).thenReturn(response);
        final var flowScope = new LocalAttributeMap();
        when(this.requestContext.getFlowScope()).thenReturn(flowScope);
        final var mockFlowExecutionKey = new MockFlowExecutionKey(FLOW_EXECUTION_KEY);
        final var mockFlowExecutionContext = new MockFlowExecutionContext();
        mockFlowExecutionContext.setKey(mockFlowExecutionKey);
        when(this.requestContext.getFlowExecutionContext()).thenReturn(mockFlowExecutionContext);
    }

    @Test
    public void verifyLogoutNoIndex() {
        WebUtils.putLogoutRequests(this.requestContext, new ArrayList<>(0));
        final var event = this.frontChannelLogoutAction.doExecute(this.requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_FINISH, event.getId());
    }
}
