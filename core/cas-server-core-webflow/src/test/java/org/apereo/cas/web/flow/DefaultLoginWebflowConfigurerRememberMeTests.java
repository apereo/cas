package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.credential.RememberMeUsernamePasswordCredential;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultLoginWebflowConfigurerRememberMeTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("WebflowConfig")
@TestPropertySource(properties = "cas.ticket.tgt.remember-me.enabled=true")
public class DefaultLoginWebflowConfigurerRememberMeTests extends BaseWebflowConfigurerTests {
    @Test
    public void verifyOperation() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
        flow.getVariable(CasWebflowConstants.VAR_ID_CREDENTIAL).create(context);
        val creds = context.getFlowScope().get(CasWebflowConstants.VAR_ID_CREDENTIAL, RememberMeUsernamePasswordCredential.class);
        assertNotNull(creds);
    }
}
