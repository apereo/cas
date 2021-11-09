package org.apereo.cas.interrupt.webflow.actions;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.interrupt.InterruptInquirer;
import org.apereo.cas.interrupt.InterruptResponse;
import org.apereo.cas.interrupt.webflow.InterruptUtils;
import org.apereo.cas.services.DefaultRegisteredServiceWebflowInterruptPolicy;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.model.TriStateBoolean;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.test.MockRequestContext;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link InquireInterruptActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("WebflowActions")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class InquireInterruptActionTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifyInterrupted() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);

        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        registeredService.setWebflowInterruptPolicy(new DefaultRegisteredServiceWebflowInterruptPolicy().setForceExecution(TriStateBoolean.TRUE));
        WebUtils.putRegisteredService(context, registeredService);

        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
        WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());

        val interrupt = mock(InterruptInquirer.class);
        when(interrupt.inquire(any(Authentication.class), any(RegisteredService.class),
            any(Service.class), any(Credential.class), any(RequestContext.class)))
            .thenReturn(InterruptResponse.interrupt());

        val action = new InquireInterruptAction(List.of(interrupt), casProperties, mock(CasCookieBuilder.class));
        var event = action.doExecute(context);
        assertNotNull(event);
        assertNotNull(InterruptUtils.getInterruptFrom(context));
        assertNotNull(WebUtils.getPrincipalFromRequestContext(context));
        assertEquals(event.getId(), CasWebflowConstants.TRANSITION_ID_INTERRUPT_REQUIRED);

        event = action.doExecute(context);
        assertNotNull(InterruptUtils.getInterruptFrom(context));
        assertNotNull(WebUtils.getPrincipalFromRequestContext(context));
        assertEquals(event.getId(), CasWebflowConstants.TRANSITION_ID_INTERRUPT_REQUIRED);
    }

    @Test
    public void verifyInterruptedAlready() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(
            Map.of(InquireInterruptAction.AUTHENTICATION_ATTRIBUTE_FINALIZED_INTERRUPT, List.of(Boolean.TRUE))), context);
        WebUtils.putRegisteredService(context, RegisteredServiceTestUtils.getRegisteredService());
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
        WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());

        val interrupt = mock(InterruptInquirer.class);
        when(interrupt.inquire(any(Authentication.class), any(RegisteredService.class),
            any(Service.class), any(Credential.class), any(RequestContext.class)))
            .thenReturn(InterruptResponse.none());

        val action = new InquireInterruptAction(List.of(interrupt), casProperties, mock(CasCookieBuilder.class));
        val event = action.doExecute(context);
        assertNotNull(event);
        assertEquals(event.getId(), CasWebflowConstants.TRANSITION_ID_INTERRUPT_SKIPPED);
    }


    @Test
    public void verifyInterruptFinalized() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
        WebUtils.putRegisteredService(context, RegisteredServiceTestUtils.getRegisteredService());
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
        WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
        WebUtils.putInterruptAuthenticationFlowFinalized(context);

        val interrupt = mock(InterruptInquirer.class);
        when(interrupt.inquire(any(Authentication.class), any(RegisteredService.class),
            any(Service.class), any(Credential.class), any(RequestContext.class)))
            .thenReturn(InterruptResponse.none());

        val action = new InquireInterruptAction(List.of(interrupt), casProperties, mock(CasCookieBuilder.class));
        val event = action.doExecute(context);
        assertNotNull(event);
        assertEquals(event.getId(), CasWebflowConstants.TRANSITION_ID_INTERRUPT_SKIPPED);
    }

    @Test
    public void verifyNotInterrupted() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
        WebUtils.putRegisteredService(context, RegisteredServiceTestUtils.getRegisteredService());
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
        WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());

        val interrupt = mock(InterruptInquirer.class);
        when(interrupt.inquire(any(Authentication.class), any(RegisteredService.class),
            any(Service.class), any(Credential.class), any(RequestContext.class)))
            .thenReturn(InterruptResponse.none());

        val action = new InquireInterruptAction(List.of(interrupt), casProperties, mock(CasCookieBuilder.class));
        val event = action.doExecute(context);
        assertNotNull(event);
        assertEquals(event.getId(), CasWebflowConstants.TRANSITION_ID_INTERRUPT_SKIPPED);
    }

    @Test
    public void verifyNotInterruptedAsFinalized() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication("casuser",
            Map.of(CasWebflowConstants.TRANSITION_ID_INTERRUPT_SKIPPED, List.of(Boolean.TRUE))), context);
        WebUtils.putRegisteredService(context, RegisteredServiceTestUtils.getRegisteredService());
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
        WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());

        val interrupt = mock(InterruptInquirer.class);
        when(interrupt.inquire(any(Authentication.class), any(RegisteredService.class),
            any(Service.class), any(Credential.class), any(RequestContext.class)))
            .thenReturn(InterruptResponse.none());

        val action = new InquireInterruptAction(List.of(interrupt), casProperties, mock(CasCookieBuilder.class));
        val event = action.doExecute(context);
        assertNotNull(event);
        assertEquals(event.getId(), CasWebflowConstants.TRANSITION_ID_INTERRUPT_SKIPPED);
    }
}
