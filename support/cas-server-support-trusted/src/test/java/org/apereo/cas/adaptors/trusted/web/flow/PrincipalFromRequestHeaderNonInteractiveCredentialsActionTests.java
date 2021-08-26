package org.apereo.cas.adaptors.trusted.web.flow;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.message.DefaultMessageContext;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link PrincipalFromRequestHeaderNonInteractiveCredentialsActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("WebflowActions")
@TestPropertySource(properties = "cas.authn.adaptive.policy.reject-ip-addresses=1.2.3.4")
public class PrincipalFromRequestHeaderNonInteractiveCredentialsActionTests extends BaseNonInteractiveCredentialsActionTests {
    @Autowired
    @Qualifier("principalFromRemoteHeaderPrincipalAction")
    private PrincipalFromRequestExtractorAction action;

    @Test
    public void verifyRemoteUserExists() throws Exception {
        val context = new MockRequestContext();
        val messageContext = (DefaultMessageContext) context.getMessageContext();
        messageContext.setMessageSource(mock(MessageSource.class));
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

        val principal = mock(Principal.class);
        when(principal.getName()).thenReturn("casuser");
        request.setUserPrincipal(principal);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, this.action.execute(context).getId());

        request.setRemoteUser("test");
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, this.action.execute(context).getId());

        request.addHeader("principal", "casuser");
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, this.action.execute(context).getId());
    }


    @Test
    public void verifyError() throws Exception {
        val context = new MockRequestContext();
        val messageContext = (DefaultMessageContext) context.getMessageContext();
        messageContext.setMessageSource(mock(MessageSource.class));

        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        request.setRemoteUser("xyz");
        request.addParameter(casProperties.getAuthn().getMfa().getTriggers().getHttp().getRequestParameter(), "mfa-whatever");
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, this.action.execute(context).getId());
    }

    @Test
    public void verifyAdaptiveError() throws Exception {
        val context = new MockRequestContext();
        val messageContext = (DefaultMessageContext) context.getMessageContext();
        messageContext.setMessageSource(mock(MessageSource.class));

        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        request.setRemoteUser("xyz");
        request.setRemoteAddr("1.2.3.4");
        request.setLocalAddr("1.2.3.4");
        request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "FIREFOX");
        request.addParameter("geolocation", "1000,1000,1000,1000");
        ClientInfoHolder.setClientInfo(new ClientInfo(request));

        request.addParameter(casProperties.getAuthn().getMfa().getTriggers().getHttp().getRequestParameter(), "mfa-whatever");
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, this.action.execute(context).getId());
    }
}
