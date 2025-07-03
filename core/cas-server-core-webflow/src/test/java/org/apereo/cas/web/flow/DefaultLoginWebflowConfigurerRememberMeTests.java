package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.credential.RememberMeUsernamePasswordCredential;
import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.engine.Flow;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultLoginWebflowConfigurerRememberMeTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("WebflowConfig")
@TestPropertySource(properties = "cas.ticket.tgt.remember-me.enabled=true")
class DefaultLoginWebflowConfigurerRememberMeTests extends BaseWebflowConfigurerTests {
    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
        flow.getVariable(CasWebflowConstants.VAR_ID_CREDENTIAL).create(context);
        val creds = context.getFlowScope().get(CasWebflowConstants.VAR_ID_CREDENTIAL, RememberMeUsernamePasswordCredential.class);
        assertNotNull(creds);
    }
}
