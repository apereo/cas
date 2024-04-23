package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowConfigurer;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PasswordChangeActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowActions")
@TestPropertySource(properties = "cas.authn.pm.core.password-policy-pattern=P@ss.+")
class PasswordChangeActionTests extends BasePasswordManagementActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_PASSWORD_CHANGE)
    private Action passwordChangeAction;

    @Test
    void verifyFailNoCreds() throws Throwable {
        val context = MockRequestContext.create();
        val changeReq = new PasswordChangeRequest();
        changeReq.setUsername("casuser");
        changeReq.setPassword("123456".toCharArray());
        context.getFlowScope().put(PasswordManagementWebflowConfigurer.FLOW_VAR_ID_PASSWORD, changeReq);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, passwordChangeAction.execute(context).getId());
    }

    @Test
    void verifyFailsValidation() throws Throwable {
        val context = MockRequestContext.create();

        WebUtils.putCredential(context, RegisteredServiceTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));

        val changeReq = new PasswordChangeRequest();
        changeReq.setUsername("casuser");
        changeReq.setPassword("123456".toCharArray());
        context.getFlowScope().put(PasswordManagementWebflowConfigurer.FLOW_VAR_ID_PASSWORD, changeReq);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, passwordChangeAction.execute(context).getId());
    }

    @Test
    void verifyChange() throws Throwable {
        val context = MockRequestContext.create();

        WebUtils.putCredential(context,
            RegisteredServiceTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Th!$isT3$t"));

        val changeReq = new PasswordChangeRequest();
        changeReq.setUsername("casuser");
        changeReq.setPassword("P@ssword".toCharArray());
        changeReq.setConfirmedPassword("P@ssword".toCharArray());
        context.getFlowScope().put(PasswordManagementWebflowConfigurer.FLOW_VAR_ID_PASSWORD, changeReq);
        PasswordManagementWebflowUtils.putPasswordResetUsername(context, changeReq.getUsername());
        assertEquals(CasWebflowConstants.TRANSITION_ID_PASSWORD_UPDATE_SUCCESS, passwordChangeAction.execute(context).getId());
    }

    @Test
    void verifyChangeFails() throws Throwable {
        val context = MockRequestContext.create();

        WebUtils.putCredential(context,
            RegisteredServiceTestUtils.getCredentialsWithDifferentUsernameAndPassword("bad-credential", "P@ssword"));

        val changeReq = new PasswordChangeRequest();
        changeReq.setUsername("bad-credential");
        changeReq.setPassword("P@ssword".toCharArray());
        changeReq.setConfirmedPassword("P@ssword".toCharArray());
        context.getFlowScope().put(PasswordManagementWebflowConfigurer.FLOW_VAR_ID_PASSWORD, changeReq);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, passwordChangeAction.execute(context).getId());
    }

    @Test
    void verifyPasswordRejected() throws Throwable {
        val context = MockRequestContext.create();

        WebUtils.putCredential(context,
            RegisteredServiceTestUtils.getCredentialsWithDifferentUsernameAndPassword("error-credential", "P@ssword"));

        val changeReq = new PasswordChangeRequest();
        changeReq.setUsername("error-credential");
        changeReq.setPassword("P@ssword".toCharArray());
        changeReq.setConfirmedPassword("P@ssword".toCharArray());
        context.getFlowScope().put(PasswordManagementWebflowConfigurer.FLOW_VAR_ID_PASSWORD, changeReq);
        PasswordManagementWebflowUtils.putPasswordResetUsername(context, changeReq.getUsername());
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, passwordChangeAction.execute(context).getId());
    }

}
