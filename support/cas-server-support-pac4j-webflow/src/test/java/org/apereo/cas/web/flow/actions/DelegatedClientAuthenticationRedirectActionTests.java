package org.apereo.cas.web.flow.actions;

import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.util.LinkedHashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DelegatedClientAuthenticationRedirectActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("WebflowAuthenticationActions")
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
public class DelegatedClientAuthenticationRedirectActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_REDIRECT)
    private Action delegatedAuthenticationRedirectToClientAction;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Test
    public void verifyRedirect() throws Exception {
        val context = getMockRequestContext();
        val sessionTicket = getTransientSessionTicket("CasClient");
        context.getFlowScope().put(TransientSessionTicket.class.getName(), sessionTicket);
        val result = delegatedAuthenticationRedirectToClientAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
        val external = (MockExternalContext) context.getExternalContext();
        assertNotNull(external.getExternalRedirectUrl());
        assertTrue(external.getExternalRedirectRequested());
    }

    @Test
    public void verifyPost() throws Exception {
        val context = getMockRequestContext();
        val sessionTicket = getTransientSessionTicket("SAML2ClientPostBinding");
        context.getFlowScope().put(TransientSessionTicket.class.getName(), sessionTicket);
        val result = delegatedAuthenticationRedirectToClientAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
        val external = (MockExternalContext) context.getExternalContext();
        assertFalse(external.getExternalRedirectRequested());
        assertTrue(external.isResponseComplete());
        val response = (MockHttpServletResponse) WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
        assertTrue(MediaType.parseMediaType(response.getContentType()).equalsTypeAndSubtype(MediaType.TEXT_HTML));
        assertNotNull(response.getContentAsString());
    }

    private static MockRequestContext getMockRequestContext() {
        val context = new MockRequestContext();
        val externalContext = new MockExternalContext();
        externalContext.setNativeRequest(new MockHttpServletRequest());
        externalContext.setNativeResponse(new MockHttpServletResponse());
        context.setExternalContext(externalContext);
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        return context;
    }

    private TransientSessionTicket getTransientSessionTicket(final String client) {
        val properties = new LinkedHashMap<String, Object>();
        properties.put(Client.class.getName(), client);

        val sessionTicket = mock(TransientSessionTicket.class);
        var service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId());
        registeredService.getProperties().put(RegisteredServiceProperties.DELEGATED_AUTHN_FORCE_AUTHN.getPropertyName(),
            new DefaultRegisteredServiceProperty("true"));
        servicesManager.save(registeredService);

        when(sessionTicket.getService()).thenReturn(service);
        when(sessionTicket.getProperties()).thenReturn(properties);
        when(sessionTicket.getProperty(anyString(), any())).thenReturn(client);
        return sessionTicket;
    }
}
