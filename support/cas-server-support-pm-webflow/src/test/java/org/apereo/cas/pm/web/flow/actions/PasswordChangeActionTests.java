package org.apereo.cas.pm.web.flow.actions;

import module java.base;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowConfigurer;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.engine.State;
import org.springframework.webflow.engine.Transition;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PasswordChangeActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowActions")
@EnabledIfListeningOnPort(port = 25000)
class PasswordChangeActionTests {

    @Nested
    @TestPropertySource(properties = {
            "cas.authn.pm.core.password-policy-pattern=P@ss.+",
            "cas.authn.pm.reset.confirmation-mail.from=cas@example.org"
    })
    class DefaultPasswordChangeActionTests extends BasePasswordManagementActionTests {
        @Test
        void verifyFailNoCredentials() throws Throwable {
            val context = createFailingRequestContext();
            val changeReq = new PasswordChangeRequest();
            changeReq.setUsername("casuser");
            changeReq.setPassword("123456".toCharArray());
            context.getFlowScope().put(PasswordManagementWebflowConfigurer.FLOW_VAR_ID_PASSWORD, changeReq);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, passwordChangeAction.execute(context).getId());
            assertCode(context, PasswordChangeAction.PASSWORD_VALIDATION_FAILURE_CODE);
        }

        @Test
        void verifyFailsValidation() throws Throwable {
            val context = createFailingRequestContext();
            WebUtils.putCredential(context, RegisteredServiceTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));

            val changeReq = new PasswordChangeRequest();
            changeReq.setUsername("casuser");
            changeReq.setPassword("123456".toCharArray());
            context.getFlowScope().put(PasswordManagementWebflowConfigurer.FLOW_VAR_ID_PASSWORD, changeReq);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, passwordChangeAction.execute(context).getId());
            assertCode(context, PasswordChangeAction.PASSWORD_VALIDATION_FAILURE_CODE);

            val targetStateResolver = ((Transition) context.getCurrentTransition()).getTargetStateResolver();
            assertNotNull(targetStateResolver.resolveTargetState(
                    (Transition) context.getCurrentTransition(),
                    (State) context.getCurrentState(), context));
        }

        @Test
        void verifyChange() throws Throwable {
            val context = MockRequestContext.create(applicationContext);

            val credential = RegisteredServiceTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Th!$isT3$t");
            WebUtils.putCredential(context, credential);

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
            val context = createFailingRequestContext();

            val credential = RegisteredServiceTestUtils.getCredentialsWithDifferentUsernameAndPassword("bad-credential", "P@ssword");
            WebUtils.putCredential(context, credential);

            val changeReq = new PasswordChangeRequest();
            changeReq.setUsername("bad-credential");
            changeReq.setPassword("P@ssword".toCharArray());
            changeReq.setConfirmedPassword("P@ssword".toCharArray());
            context.getFlowScope().put(PasswordManagementWebflowConfigurer.FLOW_VAR_ID_PASSWORD, changeReq);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, passwordChangeAction.execute(context).getId());
            assertCode(context, PasswordChangeAction.PASSWORD_VALIDATION_FAILURE_CODE);
        }

        @Test
        void verifyPasswordRejected() throws Throwable {
            val context = createFailingRequestContext();

            val credential = RegisteredServiceTestUtils.getCredentialsWithDifferentUsernameAndPassword("error-credential", "P@ssword");
            WebUtils.putCredential(context, credential);

            val changeReq = new PasswordChangeRequest();
            changeReq.setUsername("error-credential");
            changeReq.setPassword("P@ssword".toCharArray());
            changeReq.setConfirmedPassword("P@ssword".toCharArray());
            context.getFlowScope().put(PasswordManagementWebflowConfigurer.FLOW_VAR_ID_PASSWORD, changeReq);
            PasswordManagementWebflowUtils.putPasswordResetUsername(context, changeReq.getUsername());
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, passwordChangeAction.execute(context).getId());
            assertCode(context, "pm.updateFailure");
        }

        @Test
        void verifyCurrentPasswordWrong() throws Throwable {
            val context = createFailingRequestContext();

            val credential = RegisteredServiceTestUtils.getCredentialsWithDifferentUsernameAndPassword("error-credential", "P@ssword");
            WebUtils.putCredential(context, credential);

            val changeReq = new PasswordChangeRequest();
            changeReq.setUsername("bad-credential");
            changeReq.setPassword("P@ssword".toCharArray());
            changeReq.setConfirmedPassword("P@ssword".toCharArray());
            changeReq.setCurrentPassword("B@dP@ssword".toCharArray());
            context.getFlowScope().put(PasswordManagementWebflowConfigurer.FLOW_VAR_ID_PASSWORD, changeReq);
            PasswordManagementWebflowUtils.putPasswordResetUsername(context, changeReq.getUsername());
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, passwordChangeAction.execute(context).getId());
            assertCode(context, "pm.updateFailure");
        }

        @Test
        void verifyCurrentPasswordMissingLoginWebflow() throws Throwable {
            val context = createFailingRequestContext().setFlowExecutionContext("login");

            val credential = RegisteredServiceTestUtils.getCredentialsWithDifferentUsernameAndPassword("error-credential", "P@ssword");
            WebUtils.putCredential(context, credential);

            val changeReq = new PasswordChangeRequest();
            changeReq.setUsername("bad-credential");
            changeReq.setPassword("P@ssword".toCharArray());
            changeReq.setConfirmedPassword("P@ssword".toCharArray());
            context.getFlowScope().put(PasswordManagementWebflowConfigurer.FLOW_VAR_ID_PASSWORD, changeReq);
            PasswordManagementWebflowUtils.putPasswordResetUsername(context, changeReq.getUsername());
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, passwordChangeAction.execute(context).getId());
            assertCode(context, PasswordChangeAction.PASSWORD_VALIDATION_FAILURE_CODE);
        }
    }

    @Nested
    @TestPropertySource(properties = {
            "cas.authn.pm.change.current-password-required=false",
            "cas.authn.pm.core.password-policy-pattern=P@ss.+",
            "cas.authn.pm.reset.confirmation-mail.from=cas@example.org"
    })
    class CurrentPasswordUnncessaryPasswordChangeActionTests extends BasePasswordManagementActionTests {
        @Test
        void verifyCurrentPasswordMissingLoginWebflow() throws Throwable {
            val context = createFailingRequestContext().setFlowExecutionContext("login");

            val credential = RegisteredServiceTestUtils.getCredentialsWithDifferentUsernameAndPassword("error-credential", "P@ssword");
            WebUtils.putCredential(context, credential);

            val changeReq = new PasswordChangeRequest();
            changeReq.setUsername("bad-credential");
            changeReq.setPassword("P@ssword".toCharArray());
            changeReq.setConfirmedPassword("P@ssword".toCharArray());
            context.getFlowScope().put(PasswordManagementWebflowConfigurer.FLOW_VAR_ID_PASSWORD, changeReq);
            PasswordManagementWebflowUtils.putPasswordResetUsername(context, changeReq.getUsername());
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, passwordChangeAction.execute(context).getId());
            assertCode(context, "pm.updateFailure");
        }
    }
}
