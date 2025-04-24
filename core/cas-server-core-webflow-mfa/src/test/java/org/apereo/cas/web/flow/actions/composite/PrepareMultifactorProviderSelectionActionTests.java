package org.apereo.cas.web.flow.actions.composite;

import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.authentication.ChainingMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.DefaultChainingMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.bypass.DefaultChainingMultifactorAuthenticationBypassProvider;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.ViewFactory;
import org.springframework.webflow.test.MockFlowExecutionContext;
import org.springframework.webflow.test.MockFlowSession;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link PrepareMultifactorProviderSelectionActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WebflowMfaActions")
class PrepareMultifactorProviderSelectionActionTests extends BaseCasWebflowMultifactorAuthenticationTests {
    @Autowired
    @Qualifier("prepareMultifactorProviderSelectionAction")
    private Action action;

    @Test
    void verifyOperation() throws Throwable {
        val flow = new Flow(CasWebflowConfigurer.FLOW_ID_LOGIN);
        flow.setApplicationContext(applicationContext);
        val flowSession = new MockFlowSession(flow);
        flowSession.setState(new ViewState(flowSession.getDefinitionInternal(), "viewState", mock(ViewFactory.class)));
        val exec = new MockFlowExecutionContext(flowSession);

        val context = MockRequestContext.create(applicationContext);
        context.setFlowExecutionContext(exec);

        val chain = new DefaultChainingMultifactorAuthenticationProvider(applicationContext,
            new DefaultMultifactorAuthenticationFailureModeEvaluator(casProperties));
        val provider = new TestMultifactorAuthenticationProvider();
        provider.setBypassEvaluator(new DefaultChainingMultifactorAuthenticationBypassProvider(applicationContext));
        chain.addMultifactorAuthenticationProvider(provider);
        val attributes = new LocalAttributeMap(RegisteredService.class.getName(), RegisteredServiceTestUtils.getRegisteredService());
        attributes.put(MultifactorAuthenticationProvider.class.getName(), chain);

        val event = new EventFactorySupport().event(this,
            ChainingMultifactorAuthenticationProvider.DEFAULT_IDENTIFIER,
            attributes);
        context.setCurrentEvent(event);
        assertNull(action.execute(context));
        assertNotNull(MultifactorAuthenticationWebflowUtils.getSelectableMultifactorAuthenticationProviders(context));
    }
}
