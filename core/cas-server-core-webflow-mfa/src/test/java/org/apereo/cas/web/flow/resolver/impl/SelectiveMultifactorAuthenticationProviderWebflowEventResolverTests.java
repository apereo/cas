package org.apereo.cas.web.flow.resolver.impl;

import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
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
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.support.DefaultTargetStateResolver;
import org.springframework.webflow.engine.support.DefaultTransitionCriteria;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SelectiveMultifactorAuthenticationProviderWebflowEventResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Webflow")
public class SelectiveMultifactorAuthenticationProviderWebflowEventResolverTests extends BaseCasWebflowMultifactorAuthenticationTests {
    @Autowired
    @Qualifier("selectiveAuthenticationProviderWebflowEventResolver")
    private CasWebflowEventResolver selectiveAuthenticationProviderWebflowEventResolver;

    @Test
    public void verifyOperation() {
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
        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);

        val resolvedEvents = CollectionUtils.wrapHashSet(new EventFactorySupport().event(this, provider.getId()));
        WebUtils.putResolvedEventsAsAttribute(context, resolvedEvents);
        val result = selectiveAuthenticationProviderWebflowEventResolver.resolve(context);
        assertNotNull(result);
        assertNotNull(WebUtils.getResolvedMultifactorAuthenticationProviders(context));
        assertEquals(provider.getId(), result.iterator().next().getId());
    }

    @Test
    public void verifyNoProvider() {
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

        val resolvedEvents = CollectionUtils.wrapHashSet(new EventFactorySupport().event(this, "mfa-something"));
        WebUtils.putResolvedEventsAsAttribute(context, resolvedEvents);
        val result = selectiveAuthenticationProviderWebflowEventResolver.resolve(context);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

}
