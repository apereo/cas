package org.apereo.cas.pm.web.flow.actions;

import module java.base;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link VerifySecurityQuestionsActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("WebflowActions")
class VerifySecurityQuestionsActionTests extends BasePasswordManagementActionTests {

    @Test
    void verifyAction() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setParameter("q0", "securityAnswer1");
        context.getFlowScope().put("username", "casuser");
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, verifySecurityQuestionsAction.execute(context).getId());
    }

    @Test
    void verifyFailsAction() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.getFlowScope().put("username", "casuser");
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, verifySecurityQuestionsAction.execute(context).getId());
    }
}
