package org.apereo.cas;

import org.apereo.cas.pm.web.flow.ForgotUsernameCaptchaWebflowConfigurerTests;
import org.apereo.cas.pm.web.flow.PasswordManagementCaptchaWebflowConfigurerTests;
import org.apereo.cas.pm.web.flow.PasswordManagementSingleSignOnParticipationStrategyTests;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowConfigurerDisabledTests;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowConfigurerEnabledTests;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowUtilsTests;
import org.apereo.cas.pm.web.flow.actions.HandlePasswordExpirationWarningMessagesActionTests;
import org.apereo.cas.pm.web.flow.actions.InitPasswordChangeActionTests;
import org.apereo.cas.pm.web.flow.actions.InitPasswordResetActionTests;
import org.apereo.cas.pm.web.flow.actions.PasswordChangeActionTests;
import org.apereo.cas.pm.web.flow.actions.SendForgotUsernameInstructionsActionEmailMessageBodyTests;
import org.apereo.cas.pm.web.flow.actions.SendForgotUsernameInstructionsActionTests;
import org.apereo.cas.pm.web.flow.actions.SendPasswordResetInstructionsActionTests;
import org.apereo.cas.pm.web.flow.actions.ValidatePasswordResetTokenActionTests;
import org.apereo.cas.pm.web.flow.actions.VerifyPasswordResetRequestActionTests;
import org.apereo.cas.pm.web.flow.actions.VerifySecurityQuestionsActionTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    VerifySecurityQuestionsActionTests.class,
    SendPasswordResetInstructionsActionTests.class,
    InitPasswordResetActionTests.class,
    InitPasswordChangeActionTests.class,
    PasswordManagementWebflowUtilsTests.class,
    SendForgotUsernameInstructionsActionTests.class,
    PasswordChangeActionTests.class,
    ForgotUsernameCaptchaWebflowConfigurerTests.class,
    ValidatePasswordResetTokenActionTests.class,
    SendForgotUsernameInstructionsActionEmailMessageBodyTests.class,
    PasswordManagementCaptchaWebflowConfigurerTests.class,
    PasswordManagementSingleSignOnParticipationStrategyTests.class,
    PasswordManagementWebflowConfigurerEnabledTests.class,
    PasswordManagementWebflowConfigurerDisabledTests.class,
    VerifyPasswordResetRequestActionTests.class,
    HandlePasswordExpirationWarningMessagesActionTests.class
})
@Suite
public class AllTestsSuite {
}
