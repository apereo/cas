package org.apereo.cas.web.flow.actions;

import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedClientAuthenticationFailureActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
@Tag("Delegation")
class DelegatedClientAuthenticationFailureActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_FAILURE)
    private Action delegatedAuthenticationFailureAction;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyFailsOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext).withUserAgent().setClientInfo();

        assertNull(delegatedAuthenticationFailureAction.execute(context));
        assertFalse(context.getFlowScope().contains("code"));
        context.setParameter("error_description", "fail");
        context.getFlowScope().put(CasWebflowConstants.ATTRIBUTE_ERROR_ROOT_CAUSE_EXCEPTION, new RuntimeException());

        assertNull(delegatedAuthenticationFailureAction.execute(context));
        assertTrue(context.getFlowScope().contains("code"));
        assertTrue(context.getFlowScope().contains("description"));
    }
}
