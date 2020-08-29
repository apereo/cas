
package org.apereo.cas;

import org.apereo.cas.pm.web.flow.PasswordManagementCaptchaWebflowConfigurerTests;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowConfigurerDisabledTests;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowConfigurerEnabledTests;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowUtilsTests;
import org.apereo.cas.pm.web.flow.actions.HandlePasswordExpirationWarningMessagesActionTests;
import org.apereo.cas.pm.web.flow.actions.InitPasswordChangeActionTests;
import org.apereo.cas.pm.web.flow.actions.InitPasswordResetActionTests;
import org.apereo.cas.pm.web.flow.actions.PasswordChangeActionTests;
import org.apereo.cas.pm.web.flow.actions.SendForgotUsernameInstructionsActionTests;
import org.apereo.cas.pm.web.flow.actions.SendPasswordResetInstructionsActionTests;
import org.apereo.cas.pm.web.flow.actions.VerifyPasswordResetRequestActionSecurityQuestionsDisabledTests;
import org.apereo.cas.pm.web.flow.actions.VerifyPasswordResetRequestActionTests;
import org.apereo.cas.pm.web.flow.actions.VerifySecurityQuestionsActionTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

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
    VerifyPasswordResetRequestActionSecurityQuestionsDisabledTests.class,
    PasswordManagementCaptchaWebflowConfigurerTests.class,
    PasswordManagementWebflowConfigurerEnabledTests.class,
    PasswordManagementWebflowConfigurerDisabledTests.class,
    VerifyPasswordResetRequestActionTests.class,
    HandlePasswordExpirationWarningMessagesActionTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
