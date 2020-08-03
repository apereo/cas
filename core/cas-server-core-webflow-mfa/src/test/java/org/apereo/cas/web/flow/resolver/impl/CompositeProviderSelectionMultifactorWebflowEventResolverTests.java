package org.apereo.cas.web.flow.resolver.impl;

import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.authentication.ChainingMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.DefaultChainingMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.expression.support.LiteralExpression;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.support.DefaultTargetStateResolver;
import org.springframework.webflow.engine.support.DefaultTransitionCriteria;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CompositeProviderSelectionMultifactorWebflowEventResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Webflow")
public class CompositeProviderSelectionMultifactorWebflowEventResolverTests extends BaseCasWebflowMultifactorAuthenticationTests {
    @Autowired
    @Qualifier("compositeProviderSelectionMultifactorWebflowEventResolver")
    private CasWebflowEventResolver compositeResolver;

    @Test
    public void verifyComposite() {
        val provider = new DefaultChainingMultifactorAuthenticationProvider(
            new DefaultMultifactorAuthenticationFailureModeEvaluator(casProperties));

        val event = new EventFactorySupport().event(this,
            ChainingMultifactorAuthenticationProvider.DEFAULT_IDENTIFIER,
            new LocalAttributeMap<>(MultifactorAuthenticationProvider.class.getName(), provider));
        val resolvedEvents = CollectionUtils.wrapHashSet(event);

        assertCompositeProvider(provider, resolvedEvents);
    }

    @Test
    public void verifyNoComposite() {
        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val resolvedEvents = CollectionUtils.wrapHashSet(new EventFactorySupport().event(this, provider.getId()));
        assertCompositeProvider(provider, resolvedEvents);
    }

    private void assertCompositeProvider(final MultifactorAuthenticationProvider provider, final Set<Event> resolvedEvents) {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val service = RegisteredServiceTestUtils.getRegisteredService();
        servicesManager.save(service);
        WebUtils.putRegisteredService(context, service);
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(), context);
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());

        val targetResolver = new DefaultTargetStateResolver(TestMultifactorAuthenticationProvider.ID);
        val transition = new Transition(new DefaultTransitionCriteria(
            new LiteralExpression(TestMultifactorAuthenticationProvider.ID)), targetResolver);
        context.getRootFlow().getGlobalTransitionSet().add(transition);
        WebUtils.putResolvedEventsAsAttribute(context, resolvedEvents);
        val result = compositeResolver.resolve(context);
        assertNotNull(result);
        assertNotNull(WebUtils.getResolvedMultifactorAuthenticationProviders(context));
        assertEquals(provider.getId(), result.iterator().next().getId());
    }
}
