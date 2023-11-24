package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockFlowExecutionContext;
import org.springframework.webflow.test.MockFlowSession;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PrepareForPasswordlessAuthenticationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("WebflowAuthenticationActions")
class PrepareForPasswordlessAuthenticationActionTests extends BasePasswordlessAuthenticationActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_PASSWORDLESS_PREPARE_LOGIN)
    private Action prepareLoginAction;

    @Test
    void verifyAction() throws Throwable {
        val flow = new Flow(CasWebflowConfigurer.FLOW_ID_LOGIN);
        flow.setApplicationContext(applicationContext);
        val exec = new MockFlowExecutionContext(new MockFlowSession(flow));
        val context = MockRequestContext.create(applicationContext);
        context.setFlowExecutionContext(exec);
        assertEquals(CasWebflowConstants.TRANSITION_ID_PASSWORDLESS_GET_USERID, prepareLoginAction.execute(context).getId());

        val account = PasswordlessUserAccount.builder()
            .email("email")
            .phone("phone")
            .username("casuser")
            .name("casuser")
            .build();
        PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, account);
        assertNull(prepareLoginAction.execute(context));
    }
}
