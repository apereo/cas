package org.apereo.cas.web.flow;

import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.test.MockFlowExecutionContext;
import org.springframework.webflow.test.MockFlowSession;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegationWebflowUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Utility")
class DelegationWebflowUtilsTests {

    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create();

        val flow = new Flow("mockFlow");
        val flowSession = new MockFlowSession(flow);
        flowSession.setParent(new MockFlowSession(flow));
        val mockExecutionContext = new MockFlowExecutionContext(flowSession);
        context.setFlowExecutionContext(mockExecutionContext);

        assertTrue(DelegationWebflowUtils.getDelegatedAuthenticationProviderConfigurations(context).isEmpty());
        DelegationWebflowUtils.putDelegatedClientAuthenticationResolvedCredentials(context, List.of("C1"));
        assertNotNull(DelegationWebflowUtils.getDelegatedClientAuthenticationResolvedCredentials(context, String.class));
    }

}
