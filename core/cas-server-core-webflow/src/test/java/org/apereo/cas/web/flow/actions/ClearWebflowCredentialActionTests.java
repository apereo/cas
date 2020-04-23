package org.apereo.cas.web.flow.actions;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.util.MockServletContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.FlowVariable;
import org.springframework.webflow.engine.VariableValueFactory;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ClearWebflowCredentialActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreUtilConfiguration.class
})
@Tag("Webflow")
public class ClearWebflowCredentialActionTests {

    @Test
    public void verifyOperation() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val action = new ClearWebflowCredentialAction();
        context.setCurrentEvent(null);
        assertNull(action.execute(context));

        context.setCurrentEvent(new Event(this, CasWebflowConstants.TRANSITION_ID_SUCCESS));
        assertNull(action.execute(context));

        WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
        val flow = (Flow) context.getActiveFlow();
        val factory = mock(VariableValueFactory.class);
        when(factory.createInitialValue(any())).thenReturn(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
        val variable = new FlowVariable(CasWebflowConstants.VAR_ID_CREDENTIAL, factory);
        flow.addVariable(variable);
        context.setCurrentEvent(new Event(this, CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE));
        assertNull(action.execute(context));
        assertNotNull(WebUtils.getCredential(context));
    }
}
