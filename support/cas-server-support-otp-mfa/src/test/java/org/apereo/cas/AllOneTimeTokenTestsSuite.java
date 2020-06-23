package org.apereo.cas;

import org.apereo.cas.otp.repository.token.CachingOneTimeTokenRepositoryTests;
import org.apereo.cas.otp.web.flow.OneTimeTokenAccountCheckRegistrationActionTests;
import org.apereo.cas.otp.web.flow.OneTimeTokenAccountConfirmSelectionRegistrationActionTests;
import org.apereo.cas.otp.web.flow.OneTimeTokenAccountCreateRegistrationActionTests;
import org.apereo.cas.otp.web.flow.OneTimeTokenAccountSaveRegistrationActionTests;
import org.apereo.cas.otp.web.flow.OneTimeTokenAuthenticationWebflowActionTests;
import org.apereo.cas.otp.web.flow.OneTimeTokenAuthenticationWebflowEventResolverTests;
import org.apereo.cas.otp.web.flow.rest.OneTimeTokenQRGeneratorControllerTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllOneTimeTokenTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SelectClasses({
    CachingOneTimeTokenRepositoryTests.class,
    OneTimeTokenAccountConfirmSelectionRegistrationActionTests.class,
    OneTimeTokenAccountCreateRegistrationActionTests.class,
    OneTimeTokenAuthenticationWebflowEventResolverTests.class,
    OneTimeTokenAuthenticationWebflowActionTests.class,
    OneTimeTokenAccountSaveRegistrationActionTests.class,
    OneTimeTokenAccountCheckRegistrationActionTests.class,
    OneTimeTokenQRGeneratorControllerTests.class
})
@RunWith(JUnitPlatform.class)
public class AllOneTimeTokenTestsSuite {
}
