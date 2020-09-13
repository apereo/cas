package org.apereo.cas.web.flow.resolver.impl;

import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultCasDelegatingWebflowEventResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowEvents")
public class DefaultCasDelegatingWebflowEventResolverTests extends BaseCasWebflowMultifactorAuthenticationTests {
    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;

    @Test
    public void verifyOperationNoCredential() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val event = initialAuthenticationAttemptWebflowEventResolver.resolveSingle(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, event.getId());
    }

    @Test
    public void verifyOperationFails() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val event = initialAuthenticationAttemptWebflowEventResolver.resolveSingle(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, event.getId());
    }

    @Test
    public void verifyServiceDisallowed() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val id = UUID.randomUUID().toString();
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(id);
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy().setEnabled(false));
        servicesManager.save(registeredService);
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService(id));
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(), context);
        val event = initialAuthenticationAttemptWebflowEventResolver.resolveSingle(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
    }

}
