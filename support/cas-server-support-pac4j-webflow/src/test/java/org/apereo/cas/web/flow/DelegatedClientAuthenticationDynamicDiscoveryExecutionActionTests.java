package org.apereo.cas.web.flow;

import org.apereo.cas.util.MockServletContext;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.message.MessageContext;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.test.MockParameterMap;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DelegatedClientAuthenticationDynamicDiscoveryExecutionActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.pac4j.core.discovery-selection.selection-type=DYNAMIC",
        "cas.authn.pac4j.core.discovery-selection.json.location=classpath:delegated-discovery.json"
    })
@Tag("WebflowAuthenticationActions")
public class DelegatedClientAuthenticationDynamicDiscoveryExecutionActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_DYNAMIC_DISCOVERY_EXECUTION)
    private Action delegatedAuthenticationDiscoveryAction;

    @Test
    public void verifyOperationWithClient() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.addParameter("username", "cas@example.org");
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        val result = delegatedAuthenticationDiscoveryAction.execute(context);
        assertNotNull(result);
        assertEquals(CasWebflowConstants.TRANSITION_ID_REDIRECT, result.getId());
        assertTrue(context.getRequestScope()
            .contains(DelegatedClientAuthenticationDynamicDiscoveryExecutionAction.REQUEST_SCOPE_ATTR_PROVIDER_REDIRECT_URL, String.class));
    }

    @Test
    public void verifyOperationWithoutClient() throws Exception {
        val context = mock(RequestContext.class);
        when(context.getMessageContext()).thenReturn(mock(MessageContext.class));
        when(context.getFlowScope()).thenReturn(new LocalAttributeMap<>());
        when(context.getRequestParameters()).thenReturn(new MockParameterMap());
        when(context.getRequestScope()).thenReturn(new LocalAttributeMap<>());
        val request = new MockHttpServletRequest();
        request.addParameter("username", "cas@test.org");
        val response = new MockHttpServletResponse();
        when(context.getExternalContext()).thenReturn(new ServletExternalContext(new org.springframework.mock.web.MockServletContext(), request, response));
        val result = delegatedAuthenticationDiscoveryAction.execute(context);
        assertNotNull(result);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, result.getId());
    }
}
