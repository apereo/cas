package org.apereo.cas.pm.web.flow;

import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.test.MockRequestContext;

import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PasswordManagementWebflowConfigurerEnabledTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@TestPropertySource(properties = "cas.authn.pm.core.enabled=true")
@Tag("WebflowConfig")
public class PasswordManagementWebflowConfigurerEnabledTests extends PasswordManagementWebflowConfigurerDisabledTests {
    @Override
    @SneakyThrows
    protected void verifyPasswordManagementStates(final Flow flow) {
        super.verifyPasswordManagementStates(flow);
        var state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_PASSWORD_CHANGE_ACTION);
        assertNotNull(state);

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val event = StreamSupport.stream(flow.getStartActionList().spliterator(), false)
            .filter(act -> act instanceof ConsumerExecutionAction)
            .findFirst()
            .orElseThrow()
            .execute(context);
        assertNull(event);
        assertTrue(WebUtils.isPasswordManagementEnabled(context));
    }
}

