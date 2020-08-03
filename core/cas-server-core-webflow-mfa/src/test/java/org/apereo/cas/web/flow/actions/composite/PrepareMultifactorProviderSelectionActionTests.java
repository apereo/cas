package org.apereo.cas.web.flow.actions.composite;

import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.authentication.ChainingMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.DefaultChainingMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.execution.ViewFactory;
import org.springframework.webflow.test.MockFlowExecutionContext;
import org.springframework.webflow.test.MockFlowSession;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link PrepareMultifactorProviderSelectionActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Webflow")
public class PrepareMultifactorProviderSelectionActionTests extends BaseCasWebflowMultifactorAuthenticationTests {
    @Autowired
    @Qualifier("prepareMultifactorProviderSelectionAction")
    private Action action;

    @Test
    public void verifyOperation() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val flowSession = new MockFlowSession(new Flow(CasWebflowConfigurer.FLOW_ID_LOGIN));
        flowSession.setState(new ViewState(flowSession.getDefinitionInternal(), "viewState", mock(ViewFactory.class)));
        val exec = new MockFlowExecutionContext(flowSession);
        val context = new MockRequestContext(exec);
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val provider = new DefaultChainingMultifactorAuthenticationProvider(
            new DefaultMultifactorAuthenticationFailureModeEvaluator(casProperties));
        val attributes = new LocalAttributeMap(RegisteredService.class.getName(), RegisteredServiceTestUtils.getRegisteredService());
        attributes.put(MultifactorAuthenticationProvider.class.getName(), provider);

        val event = new EventFactorySupport().event(this,
            ChainingMultifactorAuthenticationProvider.DEFAULT_IDENTIFIER,
            attributes);
        context.setCurrentEvent(event);
        assertNull(action.execute(context));
        assertNotNull(WebUtils.getSelectableMultifactorAuthenticationProviders(context));
    }
}
